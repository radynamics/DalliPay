package com.radynamics.CryptoIso20022Interop;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Secrets {
    private String xummApiKey;

    public void read() throws IOException {
        var in = getClass().getClassLoader().getResourceAsStream("secrets.json");
        if (in == null) {
            throw new FileNotFoundException("Missing secrets.json. Create one based on secrets.example.json.");
        }

        var reader = new BufferedReader(new InputStreamReader(in));
        var sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }
        var json = new JSONObject(sb.toString());

        xummApiKey = json.getString("xummApiKey");
    }

    public String getXummApiKey() {
        return xummApiKey;
    }
}
