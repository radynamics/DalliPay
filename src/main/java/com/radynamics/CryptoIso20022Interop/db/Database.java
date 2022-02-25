package com.radynamics.CryptoIso20022Interop.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String sql = "CREATE TABLE IF NOT EXISTS config (\n"
                + "	    id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	    key text NOT NULL UNIQUE,\n"
                + "	    value text NOT NULL\n"
                + "   );";
        conn.createStatement().execute(sql);
    }

    static String singleString(ResultSet rs, String column) throws Exception {
        if (!rs.next()) {
            throw new DbException("No record found");
        }
        var value = rs.getString("value");
        if (rs.next()) {
            throw new DbException(String.format("More than one record found for %s in %s", value, column));
        }
        return value;
    }
}
