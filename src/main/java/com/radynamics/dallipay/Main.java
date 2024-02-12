package com.radynamics.dallipay;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.db.Database;
import com.radynamics.dallipay.transformation.TransformInstructionFactory;
import com.radynamics.dallipay.ui.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

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
            var endOfToday = com.radynamics.dallipay.iso20022.Utils.endOfToday().toLocalDateTime();
            var until = getParam(args, "-until", endOfToday.format(DateFormatter)); // timerange in UTC
            // TODO: validate format
            var untilDt = LocalDateTime.parse(until, DateFormatter).atZone(ZoneId.systemDefault());
            var period = DateTimeRange.of(LocalDateTime.parse(from, DateFormatter), untilDt.toLocalDateTime());

            try {
                if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Taskbar.getTaskbar().setIconImage(Utils.getProductIcon());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

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

                var ledger = LedgerFactory.create(getLastUsedLedger().orElse(LedgerId.Bitcoin));
                var wallet = StringUtils.isAllEmpty(walletPublicKey) ? null : ledger.createWallet(walletPublicKey, null);

                var transformInstruction = TransformInstructionFactory.create(ledger, configFilePath, networkId);
                if (ledger.getNetwork() != null && !networkAvailable(ledger)) {
                    log.warn("No connection could be established to %s.".formatted(ledger.getNetwork().getUrl()));
                    transformInstruction.setNetwork(null);
                }

                var frm = new MainForm(!existsDb);
                frm.setTransformInstruction(transformInstruction);
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

    private static boolean networkAvailable(Ledger ledger) {
        if (ledger.getNetwork() == null) {
            return false;
        }
        try {
            ledger.getEndpointInfo(ledger.getNetwork());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Optional<LedgerId> getLastUsedLedger() {
        try (var repo = new ConfigRepo()) {
            var value = repo.getLastUsedLedger();
            return value == null ? Optional.empty() : Optional.of(value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
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