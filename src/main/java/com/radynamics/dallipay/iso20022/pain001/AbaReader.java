package com.radynamics.dallipay.iso20022.pain001;

import com.opencsv.exceptions.CsvValidationException;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.*;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AbaReader implements PaymentInstructionReader {
    private final Ledger ledger;

    /*
    Australian ABA format reader (https://www.cemtexaba.com/aba-format)
     */
    public AbaReader(Ledger ledger) {
        this.ledger = ledger;
    }

    public Payment[] read(InputStream input) throws Exception {
        var records = readRecords(input);
        if (records.size() == 0) {
            return new Payment[0];
        }

        var list = new ArrayList<Payment>();
        for (var r : records) {
            var senderAccount = getAccount(r.senderAccount);
            var receiverAccount = getAccount(r.receiverAccount);

            var t = new Payment(ledger.createTransaction());
            t.setSenderAccount(senderAccount);
            t.setSenderWallet(ReaderUtils.toValidWalletOrNull(ledger, senderAccount));
            t.setSenderAddress(getAddress(r.senderName));
            t.setReceiverAccount(receiverAccount);
            t.setReceiverWallet(ReaderUtils.toValidWalletOrNull(ledger, receiverAccount));
            t.setReceiverAddress(getAddress(r.receiverName));
            t.setOrigin(Origin.Imported);
            if (StringUtils.isEmpty(r.amount) || StringUtils.isEmpty(r.currency)) {
                t.setAmountUnknown();
            } else {
                var df = new DecimalFormat();
                var dollars = df.parse(r.amount.substring(0, 8)).doubleValue();
                var cents = df.parse(r.amount.substring(8, 10)).doubleValue() / 100d;
                t.setAmount(Money.of(dollars + cents, new Currency(r.currency)));
            }
            list.add(t);
        }
        return list.toArray(new Payment[0]);
    }

    private Account getAccount(String acct) {
        if (StringUtils.isEmpty(acct)) {
            return null;
        }
        return IbanAccount.isValid(acct) ? new IbanAccount(acct) : new OtherAccount(acct);
    }

    private Address getAddress(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return new Address(name);
    }

    private ArrayList<Record> readRecords(InputStream input) throws IOException, CsvValidationException {
        var records = new ArrayList<Record>();
        try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            var line = reader.readLine();
            while (line != null) {
                if (isDetailRecord(line)) {
                    records.add(readRecord(line));
                }
                line = reader.readLine();
            }
        }
        return records;
    }

    private boolean isDetailRecord(String line) {
        return line.length() > 97 && line.charAt(0) == '1';
    }

    private Record readRecord(String line) {
        var r = new Record();
        // https://www.cemtexaba.com/aba-format/cemtex-aba-file-format-details
        r.amount = line.substring(20, 30);
        r.currency = "AUD";
        r.senderAccount = line.substring(88, 96).trim();
        r.senderName = line.substring(96, 112).trim();
        r.receiverAccount = line.substring(8, 17).trim();
        r.receiverName = line.substring(30, 62).trim();
        return r;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public JPanel createParameterPanel() {
        return null;
    }

    @Override
    public boolean applyParameters(JPanel parameterPanel) {
        return true;
    }

    private static class Record {
        public String amount;
        public String currency;
        public String senderAccount;
        public String senderName;
        public String receiverAccount;
        public String receiverName;

        @Override
        public String toString() {
            return "amount: %s, ccy: %s, senderAccount: %s, senderName: %s".formatted(amount, currency, senderAccount, senderName);
        }
    }
}
