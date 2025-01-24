package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.Config;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionStateListener;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.exchange.DemoExchange;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.PaymentConverter;
import com.radynamics.dallipay.iso20022.TestWalletInfoProvider;
import com.radynamics.dallipay.iso20022.creditorreference.ReferenceType;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.dallipay.iso20022.pain001.TestTransaction;
import com.radynamics.dallipay.transformation.AccountMappingSourceException;
import com.radynamics.dallipay.transformation.MemoryAccountMappingSource;
import com.radynamics.dallipay.transformation.TransactionTranslator;
import com.radynamics.dallipay.transformation.TransformInstruction;
import jakarta.ws.rs.NotSupportedException;
import okhttp3.HttpUrl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class TestFactory {
    public static TransformInstruction createTransformInstruction() {
        return createTransformInstruction(LedgerFactory.create("xrpl"));
    }

    public static TransformInstruction createTransformInstruction(Ledger ledger) {
        ledger.setInfoProvider(new WalletInfoProvider[]{new TestWalletInfoProvider()});
        ledger.setNetwork(NetworkInfo.createTestnet(HttpUrl.get("https://test.url"), "TestUrl"));
        var i = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(false));
        var exchange = new DemoExchange();
        exchange.load();
        i.setExchangeRateProvider(exchange);
        i.setTargetCcy(ledger.getNativeCcySymbol());

        return i;
    }

    public static Payment[] createTransactions(Ledger ledger, String targetCcy) {
        var list = new ArrayList<Transaction>();
        list.add(createTransaction1(ledger));
        list.add(createTransaction2(ledger));
        list.add(createTransactionScor(ledger));
        return PaymentConverter.toPayment(list.toArray(new Transaction[0]), new Currency(targetCcy));
    }

    public static Payment[] createTransactionsMultiCcy(Ledger ledger, TransformInstruction ti) {
        var list = new ArrayList<Transaction>();
        list.add(TestFactory.createTransaction1(ledger));
        list.add(TestFactory.createTransaction1(ledger, 7777.77, "XYZ"));
        return toPayments(ti, list);
    }

    private static Payment[] toPayments(TransformInstruction ti, ArrayList<Transaction> list) {
        var t = new TransactionTranslator(ti, new CurrencyConverter(ti.getExchangeRateProvider().latestRates()));
        final Currency ccyAsReceived = null;
        return t.apply(PaymentConverter.toPayment(list.toArray(new Transaction[0]), ccyAsReceived));
    }

    public static Payment[] createTransactionsMaxRmtInfUstrd(Ledger ledger, TransformInstruction ti) {
        var list = new ArrayList<Transaction>();
        {
            var t = new TestTransaction(ledger, Money.of(100d, new Currency("TEST")));
            list.add(t);
            t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
            t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
            t.setId("E43D83F7869885BFE92C29A6A7CF48F9B9B2FE1CEB95384707584A9DB3E288EA");
            t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
            t.addMessage("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789");
        }
        {
            var t = new TestTransaction(ledger, Money.of(100d, new Currency("TEST")));
            list.add(t);
            t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
            t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
            t.setId("A13D83F7869885BFE92C29A6A7CF48F9B9B2FE1CEB95384707584A9DB3E288EA");
            t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
            t.addMessage("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789");
            t.addMessage("0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789");
        }

        return toPayments(ti, list);
    }

    private static Transaction createTransaction1(Ledger ledger) {
        return createTransaction1(ledger, 36.35, ledger.getNativeCcySymbol());
    }

    private static Transaction createTransaction1(Ledger ledger, double amt, String ccy) {
        var t = new TestTransaction(ledger, amt, ccy);
        t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmount(Money.of(amt, new Currency(ccy)));
        t.setId("E43D83F7869885BFE92C29A6A7CF48F9B9B2FE1CEB95384707584A9DB3E288EA");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
        t.setInvoiceId("RG-00123.45");

        return t;
    }

    private static Transaction createTransaction2(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmount(Money.of(50.00, t.getAmount().getCcy()));
        t.setId("57237F065509B36FB3B31DA771B6AFBBF943E3D3E9D64A3548A6C52BD7CE9415");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.SwissQrBill, "210000000003139471430009017"));

        return t;
    }

    public static Transaction createTransactionScor(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmount(Money.of(391.00, t.getAmount().getCcy()));
        t.setId("4CA4105CBC1288D9C3FB5140C61097B247523AB86192C87B89121F4877351DD9");
        t.setBooked(LocalDateTime.of(2021, 12, 28, 11, 15, 11).atZone(ZoneId.systemDefault()));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.Scor, "RF712348231"));

        return t;
    }

    public static void addAccountMapping(TransformInstruction ti, Account account, String walletPublicKey) {
        var mapping = new AccountMapping(ti.getLedger());
        mapping.setAccount(account);
        mapping.setWallet(ti.getLedger().createWallet(walletPublicKey, ""));
        mapping.setPartyId(MemoryAccountMappingSource.DummyPartyId);
        try {
            ti.getAccountMappingSource().open();
            ti.getAccountMappingSource().add(mapping);
        } catch (AccountMappingSourceException e) {
            e.printStackTrace();
        } finally {
            try {
                ti.getAccountMappingSource().close();
            } catch (AccountMappingSourceException e) {
                e.printStackTrace();
            }
        }
    }

    public static ZonedDateTime createCreationDate() {
        return ZonedDateTime.of(2021, 06, 01, 16, 46, 10, 0, ZoneId.of("UTC"));
    }

    public static TransactionSubmitter createSubmitter() {
        return new TransactionSubmitter() {
            @Override
            public String getId() {
                return "testSubmitter";
            }

            @Override
            public Ledger getLedger() {
                return null;
            }

            @Override
            public void submit(Transaction[] transactions) {
                throw new NotSupportedException();
            }

            @Override
            public PrivateKeyProvider getPrivateKeyProvider() {
                return null;
            }

            @Override
            public TransactionSubmitterInfo getInfo() {
                return null;
            }

            @Override
            public void addStateListener(TransactionStateListener l) {
                throw new NotSupportedException();
            }

            @Override
            public boolean supportIssuedTokens() {
                return true;
            }

            @Override
            public boolean supportsPathFinding() {
                return true;
            }

            @Override
            public boolean supportsPayload() {
                return true;
            }

            @Override
            public void deleteSettings() {
            }
        };
    }
}
