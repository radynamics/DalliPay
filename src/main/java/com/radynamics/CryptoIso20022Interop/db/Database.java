package com.radynamics.CryptoIso20022Interop.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Optional;

public class Database {
    final static Logger log = LogManager.getLogger(Database.class);
    private static String fileName = "cryptoIso20022Interop.db";

    public static Connection connect() {
        var dbPath = fileName;
        String url = String.format("jdbc:sqlite:%s", dbPath);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            if (conn == null) {
                throw new DbException(String.format("Could not open db %s", dbPath));
            }
            conn.setAutoCommit(false);
            createTables(conn);
            var m = new DbMigration(conn);
            m.migrateToLatest();

            return conn;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                    conn.close();
                }
            } catch (SQLException e2) {
                log.error(e2.getMessage(), e2);
            }
            return null;
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        var sql = "CREATE TABLE IF NOT EXISTS config (\n"
                + "	    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	    key text NOT NULL UNIQUE,\n"
                + "	    value text NOT NULL\n"
                + "   );";
        conn.createStatement().execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS accountmapping (\n"
                + "	   id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	   ledgerId integer NOT NULL,\n"
                + "	   bankAccount text NOT NULL,\n"
                + "	   walletPublicKey text NOT NULL\n"
                + ");";
        conn.createStatement().execute(sql);
    }

    public static Optional<String> singleString(ResultSet rs, String column) throws Exception {
        if (!rs.next()) {
            return Optional.empty();
        }
        var value = rs.getString("value");
        if (rs.next()) {
            throw new DbException(String.format("More than one record found for %s in %s", value, column));
        }
        return Optional.of(value);
    }

    public static void executeUpdate(PreparedStatement ps, int expectedRowsAffected) throws SQLException {
        var affected = ps.executeUpdate();
        if (affected != expectedRowsAffected) {
            throw new SQLException(String.format("%s rows affected but expected %s", affected, expectedRowsAffected));
        }
    }
}
