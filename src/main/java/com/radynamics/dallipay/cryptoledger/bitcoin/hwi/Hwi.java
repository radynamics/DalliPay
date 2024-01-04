package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Hwi {
    private File executable;
    private Device signingDevice;

    public WalletProcessPsbtResult signPsbt(WalletCreateFundedPsbtResult funded) throws SigningException {
        init();
        if (signingDevice == null) {
            throw new SigningException("Cannot proceed due not signing device was found.");
        }

        var args = new String[]{
                "-f", signingDevice.fingerprint(),
                // TODO: handle main/testnet
                "--chain", "test",
                "signtx", funded.psbt()
        };
        var result = execObject(args);
        return new WalletProcessPsbtResult(result.getString("psbt"), result.getBoolean("complete"));
    }

    private JSONObject execObject(String[] args) throws SigningException {
        return exec(args).getJSONObject(0);
    }

    private JSONArray execArray(String[] args) throws SigningException {
        return exec(args);
    }

    private JSONArray exec(String[] args) throws SigningException {
        var arguments = new ArrayList<String>();
        arguments.add(executable.getAbsolutePath());
        arguments.addAll(List.of(args));
        var cmd = arguments.toArray(new String[0]);

        String response;
        try {
            var proc = Runtime.getRuntime().exec(cmd);
            var br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            var sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.lineSeparator());
            }

            response = sb.toString();
        } catch (IOException e) {
            throw new SigningException(e.getMessage(), e);
        }

        JSONObject asObject = null;
        JSONArray asArray = null;

        try {
            asObject = new JSONObject(response);
        } catch (Exception ignored) {
        }
        try {
            asArray = new JSONArray(response);
        } catch (Exception ignored) {
        }

        if (asArray != null) {
            return asArray;
        }

        if (asObject == null) {
            throw new SigningException("hwi didn't return a json response. Params: %s, returned: %s".formatted(String.join(" ", args), response));
        }

        if (asObject.has("error")) {
            throw new SigningException("%s (Code %s)".formatted(asObject.getString("error"), asObject.getInt("code")));
        }

        return new JSONArray(asObject);
    }

    public void init() throws SigningException {
        if (executable == null) {
            executable = getExecutable();
        }

        if (signingDevice == null) {
            var devices = enumerate();
            signingDevice = devices.stream().findFirst().orElse(null);
        }
    }

    private ArrayList<Device> enumerate() throws SigningException {
        var result = execArray(new String[]{"enumerate"});

        var items = new ArrayList<Device>();
        for (int i = 0; i < result.length(); i++) {
            var obj = result.getJSONObject(i);
            items.add(new Device(obj.getString("type"), obj.getString("path"), obj.getString("model"), obj.getString("fingerprint")));
        }
        return items;
    }

    private File getExecutable() {
        // TODO: ensure hwi binary is present at temp location or extract if needed
        throw new NotImplementedException();
    }
}
