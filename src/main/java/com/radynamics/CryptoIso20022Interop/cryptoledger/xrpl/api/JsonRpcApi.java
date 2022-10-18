package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.radynamics.CryptoIso20022Interop.DateTimeRange;
import com.radynamics.CryptoIso20022Interop.cryptoledger.*;
import com.radynamics.CryptoIso20022Interop.cryptoledger.memo.PayloadConverter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Transaction;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.*;
import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
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
import org.xrpl.xrpl4j.crypto.signing.SignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.SingleKeySignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
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
    private final Cache<AccountRootObject> accountDataCache;

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        this.xrplClient = new XrplClient(network.getUrl());
        this.ledgerRangeConverter = new LedgerRangeConverter(xrplClient);
        this.accountDataCache = new Cache<>(network.getUrl().toString());
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

                    var deliveredAmount = r.metadata().get().deliveredAmount().get();
                    deliveredAmount.handle(xrpCurrencyAmount -> {
                        try {
                            tr.add(toTransaction(t, xrpCurrencyAmount));
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }, issuedCurrencyAmount -> {
                        try {
                            // The standard format for currency codes is a three-character string such as USD. (https://xrpl.org/currency-formats.html)
                            final int ccyCodeStandardFormatLength = 3;
                            // trim() needed, due value is always 20 bytes, filled with 0.
                            var ccyCode = issuedCurrencyAmount.currency().length() <= ccyCodeStandardFormatLength ? issuedCurrencyAmount.currency() : Utils.hexToString(issuedCurrencyAmount.currency()).trim();
                            var amt = BigDecimal.valueOf(Double.parseDouble(issuedCurrencyAmount.value()));

                            var issuer = ledger.createWallet(issuedCurrencyAmount.issuer().value(), "");
                            var ccy = new Currency(ccyCode, issuer);
                            // When the issuer field of the destination Amount field matches the Destination address, it is treated as a special case meaning "any issuer that the destination accepts." (https://xrpl.org/payment.html)
                            if (!issuer.getPublicKey().equals((p.destination().value())) || p.sendMax().isEmpty()) {
                                tr.add(toTransaction(t, amt, ccy));
                                return;
                            }

                            p.sendMax().get().handle(xrpCurrencyAmount -> {
                                        try {
                                            tr.add(toTransaction(t, amt, ccy));
                                        } catch (Exception e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    },
                                    issuedCurrencyAmountSendMax -> {
                                        var issuerSendMax = ledger.createWallet(issuedCurrencyAmountSendMax.issuer().value(), "");
                                        try {
                                            tr.add(toTransaction(t, amt, new Currency(ccyCode, issuerSendMax)));
                                        } catch (Exception e) {
                                            log.error(e.getMessage(), e);
                                        }
                                    });
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                }
            }

            if (pageCounter == 2 && tr.transactions().length == 0) {
                tr.setHasNoTransactions(true);
                return tr;
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
        accountDataCache.evictOutdated();
        var data = accountDataCache.get(wallet);
        if (data != null) {
            return data;
        }
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (accountDataCache.isPresent(wallet)) {
            return null;
        }
        try {
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            data = xrplClient.accountInfo(requestParams).accountData();
            accountDataCache.add(wallet, data);
            return data;
        } catch (JsonRpcClientErrorException e) {
            if (isAccountNotFound(e)) {
                accountDataCache.add(wallet, null);
            } else {
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
        if ("XRP".equalsIgnoreCase(ccy)) {
            var accountData = getAccountData(wallet);
            if (accountData == null) {
                return false;
            }
            return !accountData.flags().lsfDisallowXrp();
        }
        return false;
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        return toTransaction(t, deliveredAmount.toXrp(), new Currency(ledger.getNativeCcySymbol()));
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Transaction t, BigDecimal amt, Currency ccy) throws DecoderException, UnsupportedEncodingException {
        var trx = new Transaction(ledger, Money.of(amt.doubleValue(), ccy));
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

    public void refreshBalance(Wallet wallet, boolean useCache) {
        try {
            {
                if (!useCache) {
                    accountDataCache.evict(wallet);
                }
                var accountData = getAccountData(wallet);
                if (accountData != null) {
                    wallet.getBalances().set(Money.of(accountData.balance().toXrp().doubleValue(), new Currency(ledger.getNativeCcySymbol())));
                }
            }
            {
                var requestParams = AccountLinesRequestParams.builder().account(Address.of(wallet.getPublicKey())).build();
                var result = xrplClient.accountLines(requestParams);
                for (var line : result.lines()) {
                    wallet.getBalances().set(Money.of(Double.parseDouble(line.balance()), new Currency(line.currency(), ledger.createWallet(line.account().value(), ""))));
                }
            }
        } catch (JsonRpcClientErrorException e) {
            if (!isAccountNotFound(e)) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public String getAccountDomain(Wallet wallet) {
        var accountData = getAccountData(wallet);
        if (accountData == null) {
            return null;
        }
        var hex = accountData.domain().orElse(null);
        try {
            return hex == null ? null : Utils.hexToString(hex);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction[] transactions) throws Exception {
        var sendingWallets = PaymentUtils.distinctSendingWallets(transactions);
        // Process by sending wallet to keep sequence number handling simple (prevent terPRE_SEQ).
        for (var sendingWallet : sendingWallets) {
            var trxByWallet = PaymentUtils.fromSender(sendingWallet, transactions);
            var sequences = new ImmutablePair<>(UnsignedInteger.ZERO, UnsignedInteger.ZERO);
            for (var t : trxByWallet) {
                try {
                    sequences = send(t, sequences);
                    ((Transaction) t).refreshTransmission();
                } catch (Exception ex) {
                    ((Transaction) t).refreshTransmission(ex);
                }
            }
        }
    }

    private ImmutablePair<UnsignedInteger, UnsignedInteger> send(com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction t, ImmutablePair<UnsignedInteger, UnsignedInteger> sequences) throws Exception {
        var previousLastLedgerSequence = sequences.getLeft();
        var accountSequenceOffset = sequences.getRight();

        var walletFactory = DefaultWalletFactory.getInstance();
        var sender = walletFactory.fromSeed(t.getSenderWallet().getSecret(), network.isTestnet());
        if (!StringUtils.equals(sender.classicAddress().value(), t.getSenderWallet().getPublicKey())) {
            throw new LedgerException(String.format("Secret matches for sending wallet %s but expected was %s.", sender.classicAddress().value(), t.getSenderWallet().getPublicKey()));
        }
        var receiver = Address.of(t.getReceiverWallet().getPublicKey());

        var amount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(t.getAmount().getNumber().doubleValue()));

        var memos = new ArrayList<MemoWrapper>();
        var memoData = PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
        if (!StringUtils.isEmpty(memoData)) {
            memos.add(Convert.toMemoWrapper(memoData));
        }

        // Get the latest validated ledger index
        var validatedLedger = xrplClient.ledger(LedgerRequestParams.builder().ledgerIndex(LedgerIndex.VALIDATED).build())
                .ledgerIndex()
                .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

        // Workaround for https://github.com/XRPLF/xrpl4j/issues/84
        var lastLedgerSequence = UnsignedInteger.valueOf(validatedLedger.plus(UnsignedLong.valueOf(4)).unsignedLongValue().intValue());
        if (previousLastLedgerSequence == UnsignedInteger.ZERO) {
            accountSequenceOffset = UnsignedInteger.ZERO;
        } else {
            accountSequenceOffset = accountSequenceOffset.plus(UnsignedInteger.ONE);
        }
        previousLastLedgerSequence = lastLedgerSequence;

        var fee = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(t.getFee().getNumber().doubleValue()));

        // TODO: implement invoiceNo from t.getInvoiceId() (maybe also use structuredReference as invoiceNo)
        var prepared = preparePayment(lastLedgerSequence, accountSequenceOffset, sender, receiver, amount, fee, memos);

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
                                   org.xrpl.xrpl4j.wallet.Wallet sender, Address receiver, XrpCurrencyAmount amount,
                                   XrpCurrencyAmount fee, Iterable<? extends MemoWrapper> memos)
            throws JsonRpcClientErrorException {
        var requestParams = AccountInfoRequestParams.builder()
                .ledgerIndex(LedgerIndex.VALIDATED)
                .account(sender.classicAddress())
                .build();
        var accountInfoResult = xrplClient.accountInfo(requestParams);
        var sequence = accountInfoResult.accountData().sequence().plus(accountSequenceOffset);

        return Payment.builder()
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
    }

    private SignedTransaction<Payment> sign(Payment prepared, org.xrpl.xrpl4j.wallet.Wallet sender) {
        var privateKey = PrivateKey.fromBase16EncodedPrivateKey(sender.privateKey().get());
        var signatureService = new SingleKeySignatureService(privateKey);

        return signatureService.sign(KeyMetadata.EMPTY, prepared);
    }
}
