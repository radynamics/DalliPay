package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerException;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Network;
import com.radynamics.CryptoIso20022Interop.cryptoledger.NetworkInfo;
import com.radynamics.CryptoIso20022Interop.cryptoledger.TransactionResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.memo.PayloadConverter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.*;
import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.transactions.*;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;

public class JsonRpcApi implements TransactionSource {
    final static Logger log = LogManager.getLogger(JsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private final XrplClient xrplClient;
    private final LedgerRangeConverter ledgerRangeConverter;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        this.xrplClient = new XrplClient(network.getUrl());
        this.ledgerRangeConverter = new LedgerRangeConverter(xrplClient);
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, ZonedDateTime since, int limit) throws Exception {
        // Use endOfToday to ensure data until latest ledger is loaded.
        var period = DateTimeRange.of(since, Utils.endOfToday());
        var params = createAccountTransactionsRequestParams(wallet, period, null);
        return listPayments(params, period, limit, (Payment p) -> StringUtils.equals(p.account().value(), wallet.getPublicKey()));
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        var params = createAccountTransactionsRequestParams(wallet, period, null);
        return listPayments(params, period, 200, (Payment p) -> StringUtils.equals(p.destination().value(), wallet.getPublicKey()));
    }

    private TransactionResult listPayments(ImmutableAccountTransactionsRequestParams.Builder params, DateTimeRange period, int limit, Function<Payment, Boolean> include) throws Exception {
        var tr = new TransactionResult();
        var pageCounter = 0;
        var maxPages = 10;
        var result = xrplClient.accountTransactions(params.build());
        while (tr.transactions().length < limit && pageCounter < maxPages && result.transactions().size() > 0) {
            for (var r : result.transactions()) {
                if (tr.transactions().length >= limit) {
                    tr.setHasMarker(true);
                    return tr;
                }

                if (!r.metadata().isPresent() || !r.metadata().get().transactionResult().equalsIgnoreCase("tesSUCCESS")) {
                    continue;
                }

                var t = r.resultTransaction().transaction();
                if (!period.isBetween(t.closeDateHuman().get())) {
                    continue;
                }

                if (t.transactionType() == TransactionType.PAYMENT) {
                    var p = (Payment) t;
                    if (!include.apply(p)) {
                        continue;
                    }

                    // TODO: handle ImmutableIssuedCurrencyAmount
                    var deliveredAmount = r.metadata().get().deliveredAmount().get();
                    if (deliveredAmount instanceof XrpCurrencyAmount) {
                        tr.add(toTransaction(t, (XrpCurrencyAmount) deliveredAmount));
                    }
                }
            }

            if (!result.marker().isPresent()) {
                tr.setHasMarker(false);
                return tr;
            }
            params.marker(result.marker().get());
            result = xrplClient.accountTransactions(params.build());
            pageCounter++;
        }

        tr.setHasMaxPageCounterReached(pageCounter >= maxPages);
        return tr;
    }

    private ImmutableAccountTransactionsRequestParams.Builder createAccountTransactionsRequestParams(Wallet wallet, DateTimeRange period, Marker marker) throws JsonRpcClientErrorException, LedgerException {
        var start = ledgerRangeConverter.findOrNull(period.getStart());
        if (start == null) {
            throw new LedgerException(String.format("Could not find ledger at %s", period.getStart()));
        }

        var b = AccountTransactionsRequestParams.builder()
                .account(Address.of(wallet.getPublicKey()))
                .ledgerIndexMinimum(LedgerIndexBound.of(start.unsignedIntegerValue().intValue()));

        if (period.getEnd().isBefore(ZonedDateTime.now())) {
            var end = ledgerRangeConverter.findOrNull(period.getEnd());
            if (end == null) {
                throw new LedgerException(String.format("Could not find ledger at %s", period.getEnd()));
            }
            b.ledgerIndexMaximum(LedgerIndexBound.of(end.unsignedIntegerValue().intValue()));
        }
        if (marker != null) {
            b.marker(marker);
        }
        return b;
    }

    public Transaction[] listTrustlineTransactions(Wallet wallet, DateTimeRange period, Wallet ccyIssuer, String ccy) throws Exception {
        var params = createAccountTransactionsRequestParams(wallet, period, null);
        var result = xrplClient.accountTransactions(params.build());

        var list = new ArrayList<Transaction>();
        for (var r : result.transactions()) {
            var t = r.resultTransaction().transaction();
            if (!(t instanceof ImmutableTrustSet)) {
                continue;
            }
            var trustSet = (ImmutableTrustSet) t;
            if (!trustSet.limitAmount().issuer().value().equals(ccyIssuer.getPublicKey()) || !trustSet.limitAmount().currency().equalsIgnoreCase(ccy)) {
                continue;
            }
            var deliveredAmount = r.metadata().get().deliveredAmount().orElse(XrpCurrencyAmount.ofDrops(0));
            list.add(toTransaction(t, (XrpCurrencyAmount) deliveredAmount));
        }

        return list.toArray(new Transaction[0]);
    }

    public boolean exists(Wallet wallet) {
        return getAccountData(wallet) != null;
    }

    private AccountRootObject getAccountData(Wallet wallet) {
        try {
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            var result = xrplClient.accountInfo(requestParams);
            return result.accountData();
        } catch (JsonRpcClientErrorException e) {
            if (!isAccountNotFound(e)) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    private boolean isAccountNotFound(Exception e) {
        return e.getMessage().equals("Account not found.");
    }

    public FeeInfo latestFee() {
        try {
            var fee = xrplClient.fee();
            var drops = fee.drops();
            var queuePercentage = fee.currentQueueSize().longValue() / fee.maxQueueSize().orElse(UnsignedInteger.ONE).longValue();
            return new FeeInfo(drops.minimumFee().value().longValue(), drops.openLedgerFee().value().longValue(), drops.medianFee().value().longValue(), queuePercentage);
        } catch (JsonRpcClientErrorException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean requiresDestinationTag(Wallet wallet) {
        var accountData = getAccountData(wallet);
        return accountData != null && accountData.flags().lsfRequireDestTag();
    }

    public boolean isBlackholed(Wallet wallet) {
        var accountData = getAccountData(wallet);
        if (accountData == null) {
            return false;
        }

        var blackholed = new HashSet<>(Arrays.asList("rrrrrrrrrrrrrrrrrrrrrhoLvTp", "rrrrrrrrrrrrrrrrrrrrBZbvji"));
        return accountData.regularKey().isPresent()
                && accountData.flags().lsfDisableMaster() && blackholed.contains(accountData.regularKey().get().value());
    }

    public boolean walletAccepts(Wallet wallet, String ccy) {
        try {
            if ("XRP".equalsIgnoreCase(ccy)) {
                var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
                var result = xrplClient.accountInfo(requestParams);
                return !result.accountData().flags().lsfDisallowXrp();
            }
            return false;
        } catch (JsonRpcClientErrorException e) {
            if (!isAccountNotFound(e)) {
                log.error(e.getMessage(), e);
            }
            return false;
        }
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        // TODO: handle IOUs
        // TODO: handle ImmutableIssuedCurrencyAmount
        var trx = new Transaction(ledger, deliveredAmount.toXrp().doubleValue(), ledger.getNativeCcySymbol());
        trx.setId(t.hash().get().value());
        trx.setBooked(t.closeDateHuman().get());
        trx.setSender(WalletConverter.from(t.account()));
        for (MemoWrapper mw : t.memos()) {
            if (!mw.memo().memoData().isPresent()) {
                continue;
            }
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

    public void refreshBalance(Wallet wallet) {
        try {
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            var result = xrplClient.accountInfo(requestParams);
            wallet.setLedgerBalance(result.accountData().balance().value());
        } catch (JsonRpcClientErrorException e) {
            if (!isAccountNotFound(e)) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public String getAccountDomain(Wallet wallet) {
        try {
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            var result = xrplClient.accountInfo(requestParams);
            var hex = result.accountData().domain().orElse(null);
            return hex == null ? null : Utils.hexToString(hex);
        } catch (Exception e) {
            if (!isAccountNotFound(e)) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    public void send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) throws Exception {
        var sequences = new ImmutablePair<>(UnsignedInteger.ZERO, UnsignedInteger.ZERO);
        for (var t : transactions) {
            try {
                sequences = send(t, sequences);
                ((Transaction) t).refreshTransmission();
            } catch (Exception ex) {
                ((Transaction) t).refreshTransmission(ex);
            }
        }
    }

    private ImmutablePair<UnsignedInteger, UnsignedInteger> send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, ImmutablePair<UnsignedInteger, UnsignedInteger> sequences) throws Exception {
        var previousLastLedgerSequence = sequences.getLeft();
        var accountSequenceOffset = sequences.getRight();

        var walletFactory = DefaultWalletFactory.getInstance();
        var sender = walletFactory.fromSeed(t.getSenderWallet().getSecret(), network.getType() != Network.Live);
        if (!StringUtils.equals(sender.classicAddress().value(), t.getSenderWallet().getPublicKey())) {
            throw new LedgerException(String.format("Secret matches for sending wallet %s but expected was %s.", sender.classicAddress().value(), t.getSenderWallet().getPublicKey()));
        }
        var receiver = Address.of(t.getReceiverWallet().getPublicKey());

        var amount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(t.getAmountLedgerUnit()));

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

        var fee = XrpCurrencyAmount.ofDrops(t.getFeeSmallestUnit());

        // TODO: implement invoiceNo from t.getInvoiceId() (maybe also use structuredReference as invoiceNo)
        var prepared = preparePayment(lastLedgerSequence, accountSequenceOffset, sender, receiver, amount, fee, memos);

        // Idea: return prepared payment without signing to ensure this code never needs access to private key (option?)
        var signed = sign(prepared, sender);

        var prelimResult = xrplClient.submit(signed);
        if (!prelimResult.result().equalsIgnoreCase("tesSUCCESS")) {
            throw new LedgerException(String.format("Ledger submit failed with result %s %s", prelimResult.result(), prelimResult.engineResultMessage().get()));
        }

        t.setId(signed.hash().value());
        t.setBooked(ZonedDateTime.now());

        return new ImmutablePair<>(previousLastLedgerSequence, accountSequenceOffset);
    }

    private Payment preparePayment(UnsignedInteger lastLedgerSequence, UnsignedInteger accountSequenceOffset,
                                   org.xrpl.xrpl4j.wallet.Wallet sender, Address receiver, XrpCurrencyAmount amount, XrpCurrencyAmount fee, Iterable<? extends MemoWrapper> memos)
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

        // Construct a Payment
        Payment payment = Payment.builder()
                .account(sender.classicAddress())
                .amount(amount)
                .addAllMemos(memos)
                // TODO: implement TAG
                .destination(receiver)
                .sequence(sequence)
                .fee(fee)
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
