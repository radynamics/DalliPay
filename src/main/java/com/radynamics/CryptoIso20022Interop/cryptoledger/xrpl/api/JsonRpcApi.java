package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.memo.PayloadConverter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.WalletConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.logging.log4j.LogManager;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.*;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class JsonRpcApi implements TransactionSource {
    private final Ledger ledger;
    private final NetworkInfo network;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
    }

    @Override
    public Transaction[] listPayments(Wallet wallet, DateTimeRange period) throws Exception {
        var xrplClient = new XrplClient(network.getUrl());
        var c = new LedgerRangeConverter(xrplClient);
        var ledgerRange = c.convert(period);

        var params = AccountTransactionsRequestParams.builder()
                .account(Address.of(wallet.getPublicKey()))
                .ledgerIndexMinimum(LedgerIndexBound.of(ledgerRange.getStart().unsignedIntegerValue().intValue()))
                .ledgerIndexMaximum(LedgerIndexBound.of(ledgerRange.getEnd().unsignedIntegerValue().intValue()))
                .build();
        var result = xrplClient.accountTransactions(params);

        var list = new ArrayList<Transaction>();
        for (var r : result.transactions()) {
            var t = r.resultTransaction().transaction();
            // TODO: all trx are fetched -> filter earlier
            if (!period.isBetween(t.closeDateHuman().get().toLocalDateTime())) {
                continue;
            }

            if (t.transactionType() == TransactionType.PAYMENT) {
                // TODO: handle ImmutableIssuedCurrencyAmount
                var deliveredAmount = r.metadata().get().deliveredAmount().get();
                if (deliveredAmount instanceof XrpCurrencyAmount) {
                    list.add(toTransaction(t, (XrpCurrencyAmount) deliveredAmount));
                }
            }
        }

        return list.toArray(new Transaction[0]);
    }

    public boolean exists(Wallet wallet) {
        try {
            var xrplClient = new XrplClient(network.getUrl());
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            var result = xrplClient.accountInfo(requestParams);
            return result.accountData() != null;
        } catch (JsonRpcClientErrorException e) {
            LogManager.getLogger().error(e);
            return false;
        }
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        // TODO: handle IOUs
        // TODO: handle ImmutableIssuedCurrencyAmount
        var trx = new Transaction(ledger, deliveredAmount.value().longValue(), ledger.getNativeCcySymbol());
        trx.setId(t.hash().get().value());
        trx.setBooked(t.closeDateHuman().get().toLocalDateTime());
        trx.setSender(WalletConverter.from(t.account()));
        for (MemoWrapper mw : t.memos()) {
            var unwrappedMemo = PayloadConverter.fromMemo(Utils.hexToString(mw.memo().memoData().get()));
            for (var r : unwrappedMemo.structuredReferences()) {
                trx.addStructuredReference(r);
            }
            for (var ft : unwrappedMemo.freeTexts()) {
                trx.addMessage(ft);
            }
        }

        if (t.transactionType() == TransactionType.PAYMENT) {
            var p = (Payment) t;
            trx.setReceiver(WalletConverter.from(p.destination()));
            trx.setInvoiceId(p.invoiceId().isEmpty() ? "" : p.invoiceId().get().value());
        }

        return trx;
    }

    public void send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) throws Exception {
        // As explained on https://xrpl.org/send-xrp.html
        var xrplClient = new XrplClient(network.getUrl());

        var previousLastLedgerSequence = UnsignedInteger.ZERO;
        var accountSequenceOffset = UnsignedInteger.ZERO;

        for (var t : transactions) {
            var walletFactory = DefaultWalletFactory.getInstance();
            var sender = walletFactory.fromSeed(t.getSenderWallet().getSecret(), network.getType() != Network.Live);
            var receiver = Address.of(t.getReceiverWallet().getPublicKey());

            var amount = XrpCurrencyAmount.ofDrops(t.getAmountSmallestUnit());

            var memos = new ArrayList<MemoWrapper>();
            memos.add(Convert.toMemoWrapper(PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages())));

            // Get the latest validated ledger index
            LedgerIndex validatedLedger = xrplClient.ledger(LedgerRequestParams.builder().ledgerIndex(LedgerIndex.VALIDATED).build())
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

            // Workaround for https://github.com/XRPLF/xrpl4j/issues/84
            UnsignedInteger lastLedgerSequence = UnsignedInteger.valueOf(
                    validatedLedger.plus(UnsignedLong.valueOf(4)).unsignedLongValue().intValue()
            );

            if (previousLastLedgerSequence == UnsignedInteger.ZERO) {
                accountSequenceOffset = UnsignedInteger.ZERO;
            } else {
                accountSequenceOffset = accountSequenceOffset.plus(UnsignedInteger.ONE);
            }
            previousLastLedgerSequence = lastLedgerSequence;

            // TODO: implement invoiceNo from t.getInvoiceId() (maybe also use structuredReference as invoiceNo)
            var prepared = preparePayment(xrplClient, lastLedgerSequence, accountSequenceOffset, sender, receiver, amount, memos);

            // Idea: return prepared payment without signing to ensure this code never needs access to private key (option?)
            var signed = sign(prepared, sender);

            var prelimResult = xrplClient.submit(signed);
            if (!prelimResult.result().equalsIgnoreCase("tesSUCCESS")) {
                throw new LedgerException(String.format("Ledger submit failed with result %s %s", prelimResult.result(), prelimResult.engineResultMessage().get()));
            }

            t.setId(signed.hash().value());
        }
    }

    private Payment preparePayment(XrplClient xrplClient, UnsignedInteger lastLedgerSequence, UnsignedInteger accountSequenceOffset,
                                   org.xrpl.xrpl4j.wallet.Wallet sender, Address receiver, XrpCurrencyAmount amount, Iterable<? extends MemoWrapper> memos)
            throws JsonRpcClientErrorException {
        // Code from https://github.com/ripple/xrpl-dev-portal/blob/master/content/_code-samples/send-xrp/SendXrp.java

        // Prepare transaction --------------------------------------------------------
        // Look up your Account Info
        AccountInfoRequestParams requestParams = AccountInfoRequestParams.builder()
                .ledgerIndex(LedgerIndex.VALIDATED)
                .account(sender.classicAddress())
                .build();
        AccountInfoResult accountInfoResult = xrplClient.accountInfo(requestParams);
        UnsignedInteger sequence = accountInfoResult.accountData().sequence();
        sequence = sequence.plus(accountSequenceOffset);
        System.out.println("AccSequence: " + sequence);

        // Request current fee information from rippled
        FeeResult feeResult = xrplClient.fee();
        XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

        // Construct a Payment
        Payment payment = Payment.builder()
                .account(sender.classicAddress())
                .amount(amount)
                .addAllMemos(memos)
                // TODO: implement TAG
                .destination(receiver)
                .sequence(sequence)
                .fee(openLedgerFee)
                .signingPublicKey(sender.publicKey())
                .lastLedgerSequence(lastLedgerSequence)
                .build();

        return payment;
    }

    private SignedTransaction<Payment> sign(Payment prepared, org.xrpl.xrpl4j.wallet.Wallet sender) {
        // Code from https://github.com/ripple/xrpl-dev-portal/blob/master/content/_code-samples/send-xrp/SendXrp.java

        // Sign transaction -----------------------------------------------------------
        // Construct a SignatureService to sign the Payment
        PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(
                sender.privateKey().get()
        );
        SignatureService signatureService = new SingleKeySignatureService(privateKey);

        // Sign the Payment
        SignedTransaction<Payment> signedPayment = signatureService.sign(
                KeyMetadata.EMPTY,
                prepared
        );
        //System.out.println("Signed Payment: " + signedPayment.signedTransaction());
        return signedPayment;
    }
}
