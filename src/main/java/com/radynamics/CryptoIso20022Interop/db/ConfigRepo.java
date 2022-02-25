package com.radynamics.CryptoIso20022Interop.db;

import java.sql.Connection;

public class ConfigRepo {
    private final Connection conn;

    public ConfigRepo(Connection conn) {
        this.conn = conn;
    }

    public String single(String key) throws Exception {
        var ps = conn.prepareStatement("SELECT value FROM config WHERE key = ?");
        ps.setString(1, key);

        return Database.singleString(ps.executeQuery(), "value");
    }
}
