package com.radynamics.CryptoIso20022Interop.db;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteException;
import org.sqlite.mc.SQLiteMCSqlCipherConfig;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Database {
    final static Logger log = LogManager.getLogger(Database.class);
    public static File dbFile = defaultFile();
    public static String password = null;

    public static File defaultFile() {
        var home = SystemUtils.IS_OS_WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
        return Paths.get(home, "CryptoIso20022Interop", "cryptoIso20022Interop.db").toFile();
    }

    public static Connection connect() {
        if (defaultFile().equals(dbFile)) {
            createDefaultDirectory();
        }

        Connection conn = null;
        try {
            conn = connect(password);
            if (conn == null) {
                log.error(String.format("Could not open db %s", dbFile));
                return null;
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

    private static Connection connect(String password) throws SQLException {
        String url = String.format("jdbc:sqlite:file:%s", dbFile);
        try {
            return SQLiteMCSqlCipherConfig.getV4Defaults().withKey(password).createConnection(url);
        } catch (SQLiteException sle) {
            if (sle.getResultCode().name().equals("SQLITE_NOTADB")) {
                return null;
            }
            throw sle;
        }
    }

    public static boolean isPasswordAcceptable(String password) {
        return !"".equals(password);
    }

    public static void changePassword(String newPassword) throws Exception {
        if (!isPasswordAcceptable(newPassword)) {
            throw new Exception("new password cannot be empty");
        }

        var conn = Database.connect();
        conn.createStatement().execute(String.format("PRAGMA rekey='%s'", newPassword));
        conn.close();
        password = newPassword;
    }

    private static void createDefaultDirectory() {
        var parent = defaultFile().getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
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

    public static boolean isReadable(String password) {
        try {
            var conn = connect(password);
            if (conn == null) {
                return false;
            }
            conn.prepareStatement("SELECT 1 FROM sqlite_sequence").executeQuery();
            conn.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean exists() {
        return dbFile.exists();
    }
}
