package com.radynamics.dallipay.iso20022.pain001;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.*;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class CsvReader implements PaymentInstructionReader {
    private final Ledger ledger;
    private boolean skipFirstLine = true;
    private char separator = ';';

    public CsvReader(Ledger ledger) {
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
                var dfs = new DecimalFormatSymbols();
                dfs.setDecimalSeparator('.');
                var df = new DecimalFormat();
                df.setDecimalFormatSymbols(dfs);
                t.setAmount(Money.of(df.parse(r.amount).doubleValue(), new Currency(r.currency)));
            }

            if (!StringUtils.isEmpty(r.referenceNo)) {
                var typeText = StructuredReferenceFactory.detectType(r.referenceNo);
                t.addStructuredReference(StructuredReferenceFactory.create(typeText, r.referenceNo));
            }
            if (!StringUtils.isEmpty(r.freeTextMessage)) {
                t.addMessage(r.freeTextMessage);
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
        try (var reader = new CSVReaderBuilder(new InputStreamReader(input))
                .withSkipLines(skipFirstLine ? 1 : 0)
                .withCSVParser(new RFC4180ParserBuilder().withSeparator(separator).build())
                .build()) {
            String[] values;
            var lineNumber = 0;
            while ((values = reader.readNext()) != null) {
                if (values.length < 8) {
                    throw new IOException(String.format("Reading line %s failed.", lineNumber));
                }

                records.add(readRecord(values));
                lineNumber++;
            }
        }
        return records;
    }

    private Record readRecord(String[] values) {
        var r = new Record();
        r.amount = values[0];
        r.currency = values[1];
        r.senderAccount = values[2];
        r.senderName = values[3];
        r.receiverAccount = values[4];
        r.receiverName = values[5];
        r.referenceNo = values[6];
        r.freeTextMessage = values[7];
        return r;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    public boolean skipFirstLine() {
        return skipFirstLine;
    }

    public void setSkipFirstLine(boolean skipFirstLine) {
        this.skipFirstLine = skipFirstLine;
    }

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    private static class Record {
        public String amount;
        public String currency;
        public String senderAccount;
        public String senderName;
        public String receiverAccount;
        public String receiverName;
        public String referenceNo;
        public String freeTextMessage;

        @Override
        public String toString() {
            return "amount: %s, ccy: %s, senderAccount: %s, senderName: %s".formatted(amount, currency, senderAccount, senderName);
        }
    }
}
