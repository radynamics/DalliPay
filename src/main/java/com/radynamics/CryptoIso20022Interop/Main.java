package com.radynamics.CryptoIso20022Interop;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracle;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.db.Database;
import com.radynamics.CryptoIso20022Interop.exchange.Coinbase;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProviderFactory;
import com.radynamics.CryptoIso20022Interop.transformation.DbAccountMappingSource;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import com.radynamics.CryptoIso20022Interop.ui.Consts;
import com.radynamics.CryptoIso20022Interop.ui.LoginForm;
import com.radynamics.CryptoIso20022Interop.ui.MainForm;
import com.radynamics.CryptoIso20022Interop.ui.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);

    private static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        var action = getParam(args, "-a");
        var inputFileName = getParam(args, "-in"); // "pain_001_Beispiel_QRR_SCOR.xml"
        var outputFileName = getParam(args, "-out"); // "test_camt054.xml"
        var walletPublicKey = getParam(args, "-wallet");
        var networkId = getParam(args, "-n", "live"); // live, test
        var configFilePath = getParam(args, "-c", "config.json");
        var db = getParam(args, "-db");
        Database.dbFile = db == null ? Database.defaultFile() : Path.of(db).toFile();

        try {
            var now = LocalDateTime.now();
            var start = now.minusDays(7);
            var from = getParam(args, "-from", start.format(DateFormatter)); // timerange in UTC
            var until = getParam(args, "-until", now.format(DateFormatter)); // timerange in UTC
            // TODO: validate format
            var period = DateTimeRange.of(LocalDateTime.parse(from, DateFormatter), LocalDateTime.parse(until, DateFormatter));

            try {
                if (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Taskbar.getTaskbar().setIconImage(Utils.getProductIcon());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            var f = new File(configFilePath);

            var ledger = LedgerFactory.create(LedgerId.Xrpl);
            var config = f.exists() ? Config.load(ledger, configFilePath) : Config.fallback(ledger);

            var wallet = StringUtils.isAllEmpty(walletPublicKey) ? null : ledger.createWallet(walletPublicKey, null);

            javax.swing.SwingUtilities.invokeLater(() -> {
                FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", Utils.toHexString(Consts.ColorAccent)));
                FlatLightLaf.setup();

                var existsDb = Database.exists();
                if (existsDb && !login()) {
                    return;
                }

                if (!existsDb && !askNewPassword()) {
                    return;
                }

                var transformInstruction = createTransformInstruction(ledger, config, NetworkConverter.from(networkId));
                var frm = new MainForm(transformInstruction);
                frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frm.setSize(1450, 768);
                frm.setLocationByPlatform(true);
                frm.setPeriod(period);
                frm.setReceivingWallet(wallet);

                if (action != null) {
                    switch (action) {
                        case "pain001ToCrypto":
                            if (wallet != null) {
                                transformInstruction.setStaticSender(wallet.getPublicKey(), wallet.getSecret());
                            }
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
        try (var repo = new ConfigRepo()) {
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(repo.getExchangeRateProvider()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(Coinbase.ID));
        }
        t.getExchangeRateProvider().init();
        t.setHistoricExchangeRateSource(ExchangeRateProviderFactory.create(XrplPriceOracle.ID, config.getNetwork(Network.Live)));
        t.getHistoricExchangeRateSource().init();
        return t;
    }

    private static boolean login() {
        var frm = new LoginForm();
        if (!frm.showLogin()) {
            return false;
        }

        Database.password = frm.getPassword();
        return true;
    }

    private static boolean askNewPassword() {
        var frm = new LoginForm();
        if (!frm.showNewPassword()) {
            return false;
        }

        Database.password = frm.getPassword();
        return true;
    }

    private static String getParam(String[] args, String param) {
        return getParam(args, param, null);
    }

    private static String getParam(String[] args, String param, String defaultValue) {
        var index = ArrayUtils.indexOf(args, param);
        if (index == -1) {
            return defaultValue;
        }

        if (args.length <= index + 1) {
            throw new RuntimeException(String.format("No value present for argument %s", param));
        }
        return args[index + 1];
    }
}