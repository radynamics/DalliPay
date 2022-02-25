package com.radynamics.CryptoIso20022Interop.db;

import java.sql.Connection;

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

    public String single(String key) throws Exception {
        var ps = conn.prepareStatement("SELECT value FROM config WHERE key = ?");
        ps.setString(1, key);

        return Database.singleString(ps.executeQuery(), "value");
    }
}
