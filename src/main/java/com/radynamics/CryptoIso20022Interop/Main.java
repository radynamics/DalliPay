package com.radynamics.CryptoIso20022Interop;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracle;
import com.radynamics.CryptoIso20022Interop.exchange.Coinbase;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProviderFactory;
import com.radynamics.CryptoIso20022Interop.transformation.DbAccountMappingSource;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.Consts;
import com.radynamics.CryptoIso20022Interop.ui.MainForm;
import com.radynamics.CryptoIso20022Interop.ui.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

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

            var f = new File(configFilePath);

            var ledger = LedgerFactory.create(LedgerId.Xrpl);
            var config = f.exists() ? Config.load(ledger, configFilePath) : Config.fallback(ledger);
            transformInstruction = createTransformInstruction(ledger, config, NetworkConverter.from(networkId));

            javax.swing.SwingUtilities.invokeLater(() -> {
                FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", Utils.toHexString(Consts.ColorAccent)));
                FlatLightLaf.setup();

                var frm = new MainForm(transformInstruction);
                frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frm.setSize(1450, 768);
                frm.setLocationByPlatform(true);
                frm.setPeriod(period);
                frm.setReceivingWallet(wallet);

                if (action != null) {
                    switch (action) {
                        case "pain001ToCrypto":
                            transformInstruction.setStaticSender(wallet.getPublicKey(), wallet.getSecret());
                            frm.setInputFileName(inputFileName);
                            break;
                        case "cryptoToCamt054":
                            frm.setOutputFileName(outputFileName);
                            break;
                        default:
                            throw new RuntimeException(String.format("unknown action %s", action));
                    }
                }

                frm.setVisible(true);
            });
        } catch (Exception e) {
            log.error(String.format("Error during %s", action), e);
        }
    }

    private static TransformInstruction createTransformInstruction(Ledger ledger, Config config, Network network) {
        var t = new TransformInstruction(ledger, config, new DbAccountMappingSource(ledger.getId()));
        t.setNetwork(network);
        t.setExchangeRateProvider(ExchangeRateProviderFactory.create(Coinbase.ID));
        t.getExchangeRateProvider().init();
        t.setHistoricExchangeRateSource(ExchangeRateProviderFactory.create(XrplPriceOracle.ID, config.getNetwork(Network.Live)));
        t.getHistoricExchangeRateSource().init();
        return t;
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