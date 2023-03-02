package com.radynamics.dallipay.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class DbMigration {
    final static Logger log = LogManager.getLogger(DbMigration.class);
    private final Connection conn;

    public DbMigration(Connection conn) {
        this.conn = conn;
    }

    public void migrateToLatest() throws Exception {
        ensureVersion(1, this::migrateTo1);
        ensureVersion(2, this::migrateTo2);

        conn.commit();
    }

    private void ensureVersion(int version, Callable<Void> migration) throws Exception {
        var current = getDbVersion();
        if (current < version) {
            migration.call();
            setDbVersion(version);
        }
    }

    private int getDbVersion() throws SQLException {
        var ps = conn.prepareStatement("SELECT value FROM config WHERE key = ?");
        ps.setString(1, "dbVersion");

        var rs = ps.executeQuery();
        return rs.next() ? rs.getInt("value") : 0;
    }

    private void setDbVersion(int value) throws SQLException {
        var ps = conn.prepareStatement("UPDATE config SET value = ? WHERE key = 'dbVersion'");
        ps.setString(1, String.valueOf(value));
        ps.executeUpdate();
    }

    private Void migrateTo1() throws SQLException {
        insertConfig("dbVersion", "0");
        insertConfig("xrplPriceOracleConfig", "{\"version\":1,\"ccyPairs\":[{\"first\":\"XRP\",\"second\":\"USD\",\"issuer\":\"r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE\",\"receiver\":\"rXUMMaPpZqPutoRszR29jtC8amWq3APkx\"},{\"first\":\"XRP\",\"second\":\"JPY\",\"issuer\":\"r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE\",\"receiver\":\"rrJPYwVRyWFcwfaNMm83QEaCexEpKnkEg\"}]}");
        return null;
    }

    private Void migrateTo2() throws SQLException {
        var ps = conn.prepareStatement("ALTER TABLE accountmapping ADD COLUMN partyId TEXT NOT NULL DEFAULT ''");
        ps.execute();
        return null;
    }

    private void insertConfig(String key, String value) throws SQLException {
        var ps = conn.prepareStatement("INSERT INTO config (key, value) VALUES (?, ?)");
        ps.setString(1, key);
        ps.setString(2, value);
        ps.executeUpdate();
    }
}
