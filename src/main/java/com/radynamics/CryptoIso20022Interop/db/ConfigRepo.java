package com.radynamics.CryptoIso20022Interop.db;

import java.sql.Connection;
import java.util.Optional;

public class ConfigRepo implements AutoCloseable {
    private final Connection conn;

    private ConfigRepo(Connection conn) {
        if (conn == null) throw new IllegalArgumentException("Parameter 'conn' cannot be null");
        this.conn = conn;
    }

    public ConfigRepo() {
        this(Database.connect());
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }

    public Optional<String> single(String key) throws Exception {
        var ps = conn.prepareStatement("SELECT value FROM config WHERE key = ?");
        ps.setString(1, key);

        return Database.singleString(ps.executeQuery(), "value");
    }

    public void saveOrUpdate(String key, String value) throws Exception {
        String sql = "INSERT OR REPLACE INTO config (id, key, value) \n"
                + "	    VALUES ((SELECT id FROM config WHERE key = ?), ?, ?);";
        var ps = conn.prepareStatement(sql);
        ps.setString(1, key);
        ps.setString(2, key);
        ps.setString(3, value);

        ps.executeUpdate();
    }

    public Connection getConnection() {
        return conn;
    }
}