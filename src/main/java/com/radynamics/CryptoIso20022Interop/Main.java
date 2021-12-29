package com.radynamics.CryptoIso20022Interop;

import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkConverter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.Camt054Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.Pain001Reader;
import com.radynamics.CryptoIso20022Interop.transformation.JsonReader;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;
import org.apache.commons.lang3.ArrayUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main {
    private static TransformInstruction transformInstruction;

    private static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    //private static final SimpleDateFormat DateFormatter = new SimpleDateFormat(DATETIME_PATTERN);
    private static final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static void main(String[] args) {
        var action = getParam(args, "-a");
        var inputFileName = getParam(args, "-in"); // "pain_001_Beispiel_QRR_SCOR.xml"
        var outputFileName = getParam(args, "-out"); // "test_camt054.xml"
        var trainsformInstructionFileName = getParam(args, "-ti", "transforminstruction.json");
        var walletPublicKey = getParam(args, "-wallet");
        var walletSecret = getParam(args, "-walletSecret");
        var networkId = getParam(args, "-n", "test"); // live, test

        try {
            var r = new JsonReader();
            transformInstruction = r.read(new FileInputStream(trainsformInstructionFileName));
            transformInstruction.getLedger().setNetwork(NetworkConverter.from(networkId));

            switch (action) {
                case "pain001ToCrypto":
                    transformInstruction.setStaticSender(walletPublicKey, walletSecret);
                    processPain001(new FileInputStream(inputFileName));
                    break;
                case "cryptoToCamt054":
                    var now = LocalDateTime.now();
                    var start = now.minusDays(7);
                    var from = getParam(args, "-from", start.format(DateFormatter)); // timerange in UTC
                    var until = getParam(args, "-until", now.format(DateFormatter)); // timerange in UTC
                    // TODO: validate format
                    var period = DateTimeRange.of(LocalDateTime.parse(from, DateFormatter), LocalDateTime.parse(until, DateFormatter));

                    // TODO: add option to keep ledger's native currency or convert into specified currency.
                    createCamt054(outputFileName, new Wallet(walletPublicKey), period);
                    break;
                default:
                    throw new RuntimeException(String.format("unknown action %s", action));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private static void processPain001(InputStream input) throws Exception {
        var exchange = transformInstruction.getExchange();
        exchange.load();

        var r = new Pain001Reader(transformInstruction.getLedger(), transformInstruction, new CurrencyConverter(exchange.rates()));
        var payments = r.read(input);
        System.out.println(String.format("%s payments read from pain001", payments.length));

        transformInstruction.getLedger().send(payments);
    }

    private static void createCamt054(String outputFileName, Wallet wallet, DateTimeRange period) throws Exception {
        var exchange = transformInstruction.getExchange();
        exchange.load();

        var w = new Camt054Writer(transformInstruction.getLedger(), transformInstruction, new CurrencyConverter(exchange.rates()));

        var from = Date.from(period.getStart().atZone(ZoneId.systemDefault()).toInstant());
        var until = Date.from(period.getEnd().atZone(ZoneId.systemDefault()).toInstant());
        var payments = transformInstruction.getLedger().listPayments(wallet, period);

        var s = CamtConverter.toXml(w.create(payments));
        var outputStream = new FileOutputStream(outputFileName);
        s.writeTo(outputStream);

        System.out.println(String.format("%s written", outputFileName));
    }
}