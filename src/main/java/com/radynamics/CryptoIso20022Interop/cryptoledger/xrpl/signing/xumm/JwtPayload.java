package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.signing.xumm;

import org.json.JSONObject;

import java.util.Base64;
import java.util.Date;

public class JwtPayload {
    private final JSONObject payload;

    private JwtPayload(JSONObject payload) {
        if (payload == null) throw new IllegalArgumentException("Parameter 'payload' cannot be null");
        this.payload = payload;
    }

    public static JwtPayload create(String accessToken) {
        var chunks = accessToken.split("\\.");
        if (chunks.length != 3) {
            return null;
        }
        return new JwtPayload(new JSONObject(new String(Base64.getDecoder().decode(chunks[1]))));
    }

    public Date expiration() {
        return new Date(payload.getLong("exp") * 1000);
    }

    public boolean expired() {
        return expiration().before(new Date());
    }
}
