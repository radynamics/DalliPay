package com.radynamics.dallipay.cryptoledger.bitcoin.hwi;

import com.google.common.io.ByteStreams;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import com.radynamics.dallipay.iso20022.Utils;
import com.radynamics.dallipay.util.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Hwi {
    private final static Logger log = LogManager.getLogger(Hwi.class);
    private File executable;
    private Device signingDevice;
    private String chain;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public WalletProcessPsbtResult signPsbt(WalletCreateFundedPsbtResult funded) throws SigningException {
        init();

        if (signingDevice == null) {
            var devices = enumerate();
            signingDevice = devices.stream().findFirst().orElse(null);
        }
        assertExecutablePresent();
        assertSigningDevicePresent();

        var args = new String[]{
                "-f", signingDevice.fingerprint(),
                "--chain", chain,
                "signtx", funded.psbt()
        };
        var result = execObject(args);
        if (!result.has("error")) {
            return new WalletProcessPsbtResult(result.getString("psbt"), result.getBoolean("signed"), false);
        }

        final var CODE_CANCELLED = -14;
        if (result.getInt("code") == CODE_CANCELLED) {
            return new WalletProcessPsbtResult(null, false, true);
        }

        var exceptionMessage = "%s (Code %s)".formatted(result.getString("error"), result.getInt("code"));
        final var CODE_NO_KEY_FOUND_FOR_INPUT = -7;
        // Occurs, if user used another wallet and therefore a different wallet rpc was used in bitcoinCore to fund the tx.
        if (result.getInt("code") == CODE_NO_KEY_FOUND_FOR_INPUT) {
            throw new SigningException(res.getString("hwi.senderWalletUnknown").formatted(signingDevice.type(), exceptionMessage));
        }
        throw new SigningException(exceptionMessage);
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

        return new JSONArray().put(asObject);
    }

    public void init() throws SigningException {
        if (executable == null) {
            executable = getExecutable();
            assertExecutablePresent();
        }
    }

    public ArrayList<Device> enumerate() throws SigningException {
        init();
        var result = execArray(new String[]{"enumerate"});

        if (result.length() == 1 && result.getJSONObject(0).has("error")) {
            var error = result.getJSONObject(0);
            throw new SigningException("%s (Code %s)".formatted(error.getString("error"), error.getInt("code")));
        }

        var items = new ArrayList<Device>();
        for (int i = 0; i < result.length(); i++) {
            var obj = result.getJSONObject(i);
            items.add(new Device(obj.getString("type"), obj.getString("path"), obj.getString("model"), obj.getString("fingerprint")));
        }
        return items;
    }

    private synchronized File getExecutable() {
        // Delete first if present to ensure that not an older/outdated version is being used.
        var tmpDir = System.getProperty("java.io.tmpdir");
        var execDir = Paths.get(tmpDir, ".dalliPay").toFile();
        final String HWI_DIR = "hwi";
        var hwiHome = new File(execDir, HWI_DIR);
        if (hwiHome.exists()) {
            Utils.deleteDirectory(hwiHome);
        }

        try {
            var platform = Platform.current();
            var osArch = System.getProperty("os.arch");
            var ownerExecutableWritable = PosixFilePermissions.fromString("rwxr--r--");
            InputStream inputStream;
            Path tempExecPath;
            if (platform == Platform.WINDOWS) {
                Files.createDirectories(hwiHome.toPath());
                inputStream = Hwi.class.getResourceAsStream("/native/windows/x64/hwi.exe");
                tempExecPath = Files.createTempFile(hwiHome.toPath(), HWI_DIR, null);
            } else if (platform == Platform.OSX) {
                if (osArch.equals("aarch64")) {
                    inputStream = Hwi.class.getResourceAsStream("/native/osx/aarch64/hwi");
                } else {
                    inputStream = Hwi.class.getResourceAsStream("/native/osx/x64/hwi");
                }
                tempExecPath = Files.createDirectories(hwiHome.toPath(), PosixFilePermissions.asFileAttribute(ownerExecutableWritable));
            } else if (osArch.equals("aarch64")) {
                inputStream = Hwi.class.getResourceAsStream("/native/linux/aarch64/hwi");
                tempExecPath = Files.createTempFile(HWI_DIR, null, PosixFilePermissions.asFileAttribute(ownerExecutableWritable));
            } else {
                inputStream = Hwi.class.getResourceAsStream("/native/linux/x64/hwi");
                tempExecPath = Files.createTempFile(HWI_DIR, null, PosixFilePermissions.asFileAttribute(ownerExecutableWritable));
            }

            if (inputStream == null) {
                throw new IllegalStateException("Cannot load " + HWI_DIR + " from classpath");
            }

            var tempExec = tempExecPath.toFile();
            tempExec.deleteOnExit();
            try (var tempExecStream = new BufferedOutputStream(new FileOutputStream(tempExec))) {
                ByteStreams.copy(inputStream, tempExecStream);
                inputStream.close();
                tempExecStream.flush();
            }
            return tempExec;
        } catch (Exception e) {
            log.error("Error initializing HWI", e);
            return null;
        }
    }

    private void assertExecutablePresent() throws SigningException {
        if (executable == null) {
            throw new SigningException("Cannot proceed due hwi executable is not initialized.");
        }
    }

    private void assertSigningDevicePresent() throws SigningException {
        if (signingDevice == null) {
            throw new SigningException(res.getString("hwi.noSigningDevice"));
        }
    }

    public void chain(String chain) {
        this.chain = chain;
    }
}
