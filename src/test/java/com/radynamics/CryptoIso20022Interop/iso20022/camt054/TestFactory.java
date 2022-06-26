package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.Config;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.exchange.DemoExchange;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;
import com.radynamics.CryptoIso20022Interop.iso20022.PaymentConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.TestWalletInfoProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.CryptoIso20022Interop.transformation.MemoryAccountMappingSource;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

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
        ledger.setNetwork(new NetworkInfo(Network.Test, null));
        var i = new TransformInstruction(ledger, Config.fallback(ledger), new MemoryAccountMappingSource(ledger));
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
        return PaymentConverter.toPayment(list.toArray(new Transaction[0]), targetCcy);
    }

    private static Transaction createTransaction1(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rhEo7YkHrxMzqwPhCASpeNwL2HNMqfsb87", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountLedgerUnit(36.35);
        t.setId("E43D83F7869885BFE92C29A6A7CF48F9B9B2FE1CEB95384707584A9DB3E288EA");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
        t.setInvoiceId("RG-00123.45");

        return t;
    }

    private static Transaction createTransaction2(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountLedgerUnit(50.00);
        t.setId("57237F065509B36FB3B31DA771B6AFBBF943E3D3E9D64A3548A6C52BD7CE9415");
        t.setBooked(LocalDateTime.of(2021, 02, 21, 9, 10, 11).atZone(ZoneId.systemDefault()));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.SwissQrBill, "210000000003139471430009017"));

        return t;
    }

    public static Transaction createTransactionScor(Ledger ledger) {
        var t = ledger.createTransaction();
        t.setSenderWallet(ledger.createWallet("rsDoF5udkeSJQcKNqPgHvqEyVBEX4ttoi4", null));
        t.setReceiverWallet(ledger.createWallet("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY", null));
        t.setAmountLedgerUnit(391.00);
        t.setId("4CA4105CBC1288D9C3FB5140C61097B247523AB86192C87B89121F4877351DD9");
        t.setBooked(LocalDateTime.of(2021, 12, 28, 11, 15, 11).atZone(ZoneId.systemDefault()));
        t.addStructuredReference(StructuredReferenceFactory.create(ReferenceType.Scor, "RF712348231"));

        return t;
    }

    public static void addAccountMapping(TransformInstruction ti, Account account, String walletPublicKey) {
        var mapping = new AccountMapping(ti.getLedger().getId());
        mapping.setAccount(account);
        mapping.setWallet(ti.getLedger().createWallet(walletPublicKey, ""));
        ti.getAccountMappingSource().add(mapping);
    }

    public static ZonedDateTime createCreationDate() {
        return ZonedDateTime.of(2021, 06, 01, 16, 46, 10, 0, ZoneId.of("UTC"));
    }
}
