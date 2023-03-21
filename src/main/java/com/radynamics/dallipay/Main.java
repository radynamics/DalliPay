package com.radynamics.dallipay;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.db.Database;
import com.radynamics.dallipay.exchange.Coinbase;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import com.radynamics.dallipay.transformation.DbAccountMappingSource;
import com.radynamics.dallipay.transformation.TransformInstruction;
import com.radynamics.dallipay.ui.*;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);

    private static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        var action = getParam(args, "-a");
        var inputFileName = getParam(args, "-in"); // "pain_001_Beispiel_QRR_SCOR.xml"
        var outputFileName = getParam(args, "-out"); // "test_camt054.xml"
        var walletPublicKey = getParam(args, "-wallet");
        var networkId = getParam(args, "-n"); // livenet, testnet
        var configFilePath = getParam(args, "-c", "config.json");
        var db = getParam(args, "-db");
        var password = getParam(args, "-p", null);
        Database.dbFile = db == null ? Database.defaultFile() : Path.of(db).toFile();

        try {
            var now = LocalDateTime.now();
            var start = now.minusDays(7);
            var from = getParam(args, "-from", start.format(DateFormatter)); // timerange in UTC
            var until = getParam(args, "-until", now.format(DateFormatter)); // timerange in UTC
            // TODO: validate format
            var untilEndOfDay = com.radynamics.dallipay.iso20022.Utils.endOfDay(LocalDateTime.parse(until, DateFormatter).atZone(ZoneId.systemDefault()));
            var period = DateTimeRange.of(LocalDateTime.parse(from, DateFormatter), untilEndOfDay.toLocalDateTime());

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
                if (!existsDb && !showTerms()) {
                    return;
                }
                if (existsDb && !login(password)) {
                    return;
                }

                if (!existsDb && !askNewPassword()) {
                    return;
                }

                var transformInstruction = createTransformInstruction(ledger, config, getNetworkOrDefault(config, networkId));
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

    private static NetworkInfo getNetworkOrDefault(Config config, String networkId) {
        if (!StringUtils.isEmpty(networkId)) {
            var networkByParam = config.getNetwork(networkId.toLowerCase(Locale.ROOT));
            if (networkByParam.isPresent()) {
                return networkByParam.get();
            }
        }

        HttpUrl lastUsed = null;
        try (var repo = new ConfigRepo()) {
            lastUsed = repo.getLastUsedRpcUrl();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (lastUsed == null) {
            return config.getDefaultNetworkInfo();
        }

        for (var ni : config.getNetworkInfos()) {
            if (ni.getUrl().equals(lastUsed)) {
                return ni;
            }
        }

        return NetworkInfo.create(lastUsed);
    }

    private static TransformInstruction createTransformInstruction(Ledger ledger, Config config, NetworkInfo network) {
        var t = new TransformInstruction(ledger, config, new DbAccountMappingSource(ledger.getId()));
        t.setNetwork(network);
        try (var repo = new ConfigRepo()) {
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(repo.getExchangeRateProvider()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            t.setExchangeRateProvider(ExchangeRateProviderFactory.create(Coinbase.ID));
        }
        t.getExchangeRateProvider().init();

        // Different ledgers/sidechains may provide different sources for historic exchange rates.
        t.setHistoricExchangeRateSource(ledger.createHistoricExchangeRateSource());
        t.getHistoricExchangeRateSource().init();
        return t;
    }

    private static boolean showTerms() {
        var frm = new TermsForm();
        return frm.show();
    }

    private static boolean login(String password) {
        if (password != null) {
            if (Database.isReadable(password)) {
                Database.password = password;
                return true;
            }
            return false;
        }

        var frm = new LoginForm();
        if (!frm.showLogin()) {
            return false;
        }

        Database.password = frm.getPassword();
        return true;
    }

    private static boolean askNewPassword() {
        var frm = new LoginForm();
        if (!frm.showNewPassword(null)) {
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