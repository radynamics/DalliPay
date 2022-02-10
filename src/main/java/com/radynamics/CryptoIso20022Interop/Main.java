package com.radynamics.CryptoIso20022Interop;

import com.formdev.flatlaf.FlatLightLaf;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkConverter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.transformation.JsonReader;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.MainForm;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);
    private static TransformInstruction transformInstruction;

    private static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    //private static final SimpleDateFormat DateFormatter = new SimpleDateFormat(DATETIME_PATTERN);
    private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        initLogger();

        var action = getParam(args, "-a");
        var inputFileName = getParam(args, "-in"); // "pain_001_Beispiel_QRR_SCOR.xml"
        var outputFileName = getParam(args, "-out"); // "test_camt054.xml"
        var trainsformInstructionFileName = getParam(args, "-ti", "transforminstruction.json");
        var walletPublicKey = getParam(args, "-wallet");
        var walletSecret = getParam(args, "-walletSecret");
        var networkId = getParam(args, "-n", "test"); // live, test
        var configFilePath = getParam(args, "-c", "config.json");

        try {
            var now = LocalDateTime.now();
            var start = now.minusDays(7);
            var from = getParam(args, "-from", start.format(DateFormatter)); // timerange in UTC
            var until = getParam(args, "-until", now.format(DateFormatter)); // timerange in UTC
            // TODO: validate format
            var period = DateTimeRange.of(LocalDateTime.parse(from, DateFormatter), LocalDateTime.parse(until, DateFormatter));

            Wallet wallet = StringUtils.isAllEmpty(walletPublicKey) ? null : new Wallet(walletPublicKey, walletSecret);

            var r = new JsonReader();
            transformInstruction = r.read(new FileInputStream(trainsformInstructionFileName), configFilePath, NetworkConverter.from(networkId));

            javax.swing.SwingUtilities.invokeLater(() -> {
                FlatLightLaf.setup();

                var frm = new MainForm(transformInstruction);
                frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frm.setSize(1280, 768);
                frm.setLocationByPlatform(true);
                frm.setPeriod(period);
                frm.setReceivingWallet(wallet);

                switch (action) {
                    case "pain001ToCrypto":
                        transformInstruction.setStaticSender(wallet.getPublicKey(), wallet.getSecret());
                        frm.setInputFileName(inputFileName);
                        break;
                    case "cryptoToCamt054":
                        // TODO: add option to keep ledger's native currency or convert into specified currency.
                        frm.setOutputFileName(outputFileName);
                        break;
                    default:
                        throw new RuntimeException(String.format("unknown action %s", action));
                }

                frm.setVisible(true);
            });
        } catch (Exception e) {
            log.error(String.format("Error during %s", action), e);
        }
    }

    private static void initLogger() {
        if (!System.getProperties().containsKey("log4j.configurationFile")) {
            System.setProperty("log4j.configurationFile", Main.class.getClassLoader().getResource("config/log4j2.xml").toString());
        }
    }

    private static String getParam(String[] args, String param) {
        return getParam(args, param, null);
    }

    private static String getParam(String[] args, String param, String defaultValue) {
        // TODO: validate parameter value existence
        var index = ArrayUtils.indexOf(args, param);
        if (index == -1) {
            return defaultValue;
        }
        return args[index + 1];
    }
}