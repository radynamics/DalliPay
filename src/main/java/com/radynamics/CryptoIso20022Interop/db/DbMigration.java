package com.radynamics.CryptoIso20022Interop.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DbMigration {
    final static Logger log = LogManager.getLogger(DbMigration.class);
    private final Connection conn;

    public DbMigration(Connection conn) {
        this.conn = conn;
    }

    public void migrateToLatest() throws SQLException {
        var current = getDbVersion();
        if (current < 1) {
            migrateTo1();
            setDbVersion(1);
        }

        conn.commit();
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

    private void migrateTo1() throws SQLException {
        insertConfig("dbVersion", "0");
        insertConfig("xrplPriceOracleConfig", "{\"version\":1,\"ccyPairs\":[{\"first\":\"XRP\",\"second\":\"USD\",\"issuer\":\"r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE\",\"receiver\":\"rXUMMaPpZqPutoRszR29jtC8amWq3APkx\"},{\"first\":\"XRP\",\"second\":\"JPY\",\"issuer\":\"r9PfV3sQpKLWxccdg3HL2FXKxGW2orAcLE\",\"receiver\":\"rrJPYwVRyWFcwfaNMm83QEaCexEpKnkEg\"}]}");
    }

    private void insertConfig(String key, String value) throws SQLException {
        var ps = conn.prepareStatement("INSERT INTO config (key, value) VALUES (?, ?)");
        ps.setString(1, key);
        ps.setString(2, value);
        ps.executeUpdate();
    }
}
