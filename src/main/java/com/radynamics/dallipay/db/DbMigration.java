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
        ensureVersion(3, this::migrateTo3);

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
        insertConfig("xrplPriceOracleConfig", "{\"ccyPairs\":[{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"BRL\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"KRW\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"AUD\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"USD\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"JPY\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"TRY\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"GBP\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"EUR\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"MXN\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"IDR\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"RUB\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"CHF\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"MYR\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"THB\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"SGD\"},{\"receiver\":\"rpXCfDds782Bd6eK9Hsn15RDnGMtxf752m\",\"first\":\"XRP\",\"issuer\":\"rDLx56UDgChRy3HqwkFSDBpX4hL6sEgmtx\",\"second\":\"ZAR\"}],\"version\":1}");
        return null;
    }

    private Void migrateTo2() throws SQLException {
        var ps = conn.prepareStatement("ALTER TABLE accountmapping ADD COLUMN partyId TEXT NOT NULL DEFAULT ''");
        ps.execute();
        return null;
    }

    private Void migrateTo3() throws SQLException {
        var ps = conn.prepareStatement("UPDATE config SET key = 'Xrpl_priceOracleConfig' WHERE key = 'xrplPriceOracleConfig'");
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
