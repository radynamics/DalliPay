package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.DateTimeRange;
import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import com.radynamics.dallipay.cryptoledger.xrpl.Transaction;
import com.radynamics.dallipay.cryptoledger.xrpl.Wallet;
import com.radynamics.dallipay.cryptoledger.xrpl.*;
import com.radynamics.dallipay.cryptoledger.xrpl.signing.RpcSubmitter;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Utils;
import okhttp3.HttpUrl;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.client.accounts.*;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.serverinfo.ServerInfoResult;
import org.xrpl.xrpl4j.model.client.transactions.ImmutableTransactionRequestParams;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;
import org.xrpl.xrpl4j.model.transactions.*;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class JsonRpcApi implements TransactionSource {
    final static Logger log = LogManager.getLogger(JsonRpcApi.class);
    private final Ledger ledger;
    private final NetworkInfo network;
    private final XrplClient xrplClient;
    private final LedgerRangeConverter ledgerRangeConverter;
    private final Cache<AccountRootObject> accountDataCache;
    private final Cache<AccountLinesResult> accountTrustLineCache;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public JsonRpcApi(Ledger ledger, NetworkInfo network) {
        this.ledger = ledger;
        this.network = network;
        this.xrplClient = new XrplClient(network.getUrl());
        this.ledgerRangeConverter = new LedgerRangeConverter(xrplClient);
        this.accountDataCache = new Cache<>(network.getUrl().toString());
        this.accountTrustLineCache = new Cache<>(network.getUrl().toString());
    }

    @Override
    public TransactionResult listPaymentsSent(Wallet wallet, long sinceDaysAgo, int limit) throws Exception {
        var start = ledgerRangeConverter.estimatedDaysAgo(sinceDaysAgo);
        // Use endOfToday to ensure data until latest ledger is loaded.
        var end = Utils.endOfToday();
        var params = createAccountTransactionsRequestParams(wallet, start, end, null);
        return listPayments(params, limit, (Payment p) -> StringUtils.equals(p.account().value(), wallet.getPublicKey()));
    }

    @Override
    public TransactionResult listPaymentsReceived(Wallet wallet, DateTimeRange period) throws Exception {
        var params = createAccountTransactionsRequestParams(wallet, period, null);
        return listPayments(params, 200, (Payment p) -> StringUtils.equals(p.destination().value(), wallet.getPublicKey()));
    }

    private TransactionResult listPayments(ImmutableAccountTransactionsRequestParams.Builder params, int limit, Function<Payment, Boolean> include) throws Exception {
        var tr = new TransactionResult();
        loadTransactions(params, limit, tr, (org.xrpl.xrpl4j.model.transactions.Transaction t, CurrencyAmount deliveredAmount) -> {
            if (t.transactionType() == TransactionType.PAYMENT) {
                var p = (Payment) t;
                if (!include.apply(p)) {
                    return false;
                }

                tr.add(toTransaction(p, deliveredAmount));
                return true;
            }
            return false;
        });
        return tr;
    }

    private void loadTransactions(ImmutableAccountTransactionsRequestParams.Builder params, int limit, TransactionResult tr, BiFunction<org.xrpl.xrpl4j.model.transactions.Transaction, CurrencyAmount, Boolean> include) throws Exception {
        var pageCounter = 0;
        var maxPages = 10;
        var result = xrplClient.accountTransactions(params.build());
        while (tr.transactions().length < limit && pageCounter < maxPages && result.transactions().size() > 0) {
            for (var r : result.transactions()) {
                if (tr.transactions().length >= limit) {
                    tr.setHasMarker(true);
                    return;
                }

                if (!r.metadata().isPresent() || !r.metadata().get().transactionResult().equalsIgnoreCase("tesSUCCESS")) {
                    continue;
                }

                var deliveredAmount = r.metadata().get().deliveredAmount().orElse(XrpCurrencyAmount.ofDrops(0));
                if (!include.apply(r.resultTransaction().transaction(), deliveredAmount)) {
                    continue;
                }
            }

            if (pageCounter == 2 && tr.transactions().length == 0) {
                tr.setHasNoTransactions(true);
                return;
            }

            if (!result.marker().isPresent()) {
                tr.setHasMarker(false);
                return;
            }
            params.marker(result.marker().get());
            result = xrplClient.accountTransactions(params.build());
            pageCounter++;
        }

        tr.setHasMaxPageCounterReached(pageCounter >= maxPages);
    }

    private Transaction toTransaction(Payment p, CurrencyAmount deliveredAmount) {
        var future = new CompletableFuture<Transaction>();
        deliveredAmount.handle(xrpCurrencyAmount -> {
            try {
                future.complete(toTransaction(p, xrpCurrencyAmount));
            } catch (DecoderException | UnsupportedEncodingException e) {
                future.completeExceptionally(e);
            }
        }, issuedCurrencyAmount -> {
            try {
                future.complete(toTransaction(p, issuedCurrencyAmount));
            } catch (ExecutionException | InterruptedException | DecoderException | UnsupportedEncodingException e) {
                future.completeExceptionally(e);
            }
        });
        return future.join();
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Payment p, IssuedCurrencyAmount amount) throws ExecutionException, InterruptedException, DecoderException, UnsupportedEncodingException {
        var ccyCode = toCurrencyCode(amount.currency());
        var amt = BigDecimal.valueOf(Double.parseDouble(amount.value()));

        var issuer = ledger.createWallet(amount.issuer().value(), "");
        var ccy = new Currency(ccyCode, issuer);
        // When the issuer field of the destination Amount field matches the Destination address, it is treated as a special case meaning "any issuer that the destination accepts." (https://xrpl.org/payment.html)
        if (!issuer.getPublicKey().equals((p.destination().value())) || p.sendMax().isEmpty()) {
            return toTransaction(p, amt, ccy);
        }

        var future = new CompletableFuture<Transaction>();
        p.sendMax().get().handle(xrpCurrencyAmount -> {
                    try {
                        future.complete(toTransaction(p, amt, ccy));
                    } catch (DecoderException | UnsupportedEncodingException e) {
                        future.completeExceptionally(e);
                    }
                },
                issuedCurrencyAmountSendMax -> {
                    try {
                        var issuerSendMax = ledger.createWallet(issuedCurrencyAmountSendMax.issuer().value(), "");
                        future.complete(toTransaction(p, amt, new Currency(ccyCode, issuerSendMax)));
                    } catch (DecoderException | UnsupportedEncodingException e) {
                        future.completeExceptionally(e);
                    }
                });

        return future.join();
    }

    private static String toCurrencyCode(String currency) {
        try {
            // The standard format for currency codes is a three-character string such as USD. (https://xrpl.org/currency-formats.html)
            final int ccyCodeStandardFormatLength = 3;
            // trim() needed, due value is always 20 bytes, filled with 0.
            return currency.length() <= ccyCodeStandardFormatLength ? currency : Utils.hexToString(currency).trim();
        } catch (DecoderException | UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return currency;
        }
    }

    private ImmutableAccountTransactionsRequestParams.Builder createAccountTransactionsRequestParams(Wallet wallet, DateTimeRange period, Marker marker) throws JsonRpcClientErrorException, LedgerException {
        var start = ledgerRangeConverter.findOrNull(period.getStart());
        if (start == null) {
            throw new LedgerException(String.format(res.getString("ledgerNotFoundAt"), period.getStart()));
        }

        return createAccountTransactionsRequestParams(wallet, start, period.getEnd(), marker);
    }

    private ImmutableAccountTransactionsRequestParams.Builder createAccountTransactionsRequestParams(Wallet wallet, LedgerIndex start, ZonedDateTime end, Marker marker) throws JsonRpcClientErrorException, LedgerException {
        var b = AccountTransactionsRequestParams.builder()
                .account(Address.of(wallet.getPublicKey()))
                .ledgerIndexMinimum(LedgerIndexBound.of(start.unsignedIntegerValue().intValue()));

        if (end.isBefore(ZonedDateTime.now())) {
            var endLedger = ledgerRangeConverter.findOrNull(end);
            if (endLedger == null) {
                throw new LedgerException(String.format(res.getString("ledgerNotFoundAt"), end));
            }
            b.ledgerIndexMaximum(LedgerIndexBound.of(endLedger.unsignedIntegerValue().intValue()));
        }
        if (marker != null) {
            b.marker(marker);
        }
        return b;
    }

    public com.radynamics.dallipay.cryptoledger.Transaction getTransaction(String transactionId) {
        try {
            var params = ImmutableTransactionRequestParams.builder()
                    .transaction(Hash256.of(transactionId));
            var r = xrplClient.transaction(params.build(), org.xrpl.xrpl4j.model.transactions.Transaction.class);

            if (r.transaction().transactionType() != TransactionType.PAYMENT) {
                log.info("Transaction is not a payment, return null");
                return null;
            }
            var p = (Payment) r.transaction();
            var deliveredAmount = r.metadata().get().deliveredAmount().get();
            return toTransaction(p, deliveredAmount);
        } catch (JsonRpcClientErrorException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public com.radynamics.dallipay.cryptoledger.Transaction[] listTrustlineTransactions(Wallet wallet, DateTimeRange period, Wallet ccyIssuer, String ccy) throws Exception {
        var tr = new TransactionResult();
        var params = createAccountTransactionsRequestParams(wallet, period, null);
        final int limit = 200;
        loadTransactions(params, limit, tr, (org.xrpl.xrpl4j.model.transactions.Transaction t, CurrencyAmount deliveredAmount) -> {
            if (!(t instanceof ImmutableTrustSet)) {
                return false;
            }
            var trustSet = (ImmutableTrustSet) t;
            if (!trustSet.limitAmount().issuer().value().equals(ccyIssuer.getPublicKey()) || !trustSet.limitAmount().currency().equalsIgnoreCase(ccy)) {
                return false;
            }
            try {
                tr.add(toTransaction(t, XrpCurrencyAmount.ofDrops(0)));
            } catch (DecoderException | UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
            return true;
        });
        return tr.transactions();
    }

    public boolean exists(Wallet wallet) {
        return getAccountData(wallet) != null;
    }

    private AccountRootObject getAccountData(Wallet wallet) {
        accountDataCache.evictOutdated();
        var data = accountDataCache.get(wallet);
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (data != null || accountDataCache.isPresent(wallet)) {
            return data;
        }
        try {
            var requestParams = AccountInfoRequestParams.of(Address.of(wallet.getPublicKey()));
            data = xrplClient.accountInfo(requestParams).accountData();
            accountDataCache.add(wallet, data);
            return data;
        } catch (Exception e) {
            if (isAccountNotFound(e)) {
                accountDataCache.add(wallet, null);
            } else {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    private AccountLinesResult getAccountLines(Wallet wallet) {
        accountTrustLineCache.evictOutdated();
        var data = accountTrustLineCache.get(wallet);
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (data != null || accountTrustLineCache.isPresent(wallet)) {
            return data;
        }
        try {
            var requestParams = AccountLinesRequestParams.builder().account(Address.of(wallet.getPublicKey())).build();
            data = xrplClient.accountLines(requestParams);
            accountTrustLineCache.add(wallet, data);
            return data;
        } catch (JsonRpcClientErrorException e) {
            if (isAccountNotFound(e)) {
                accountTrustLineCache.add(wallet, null);
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

    public double getTransferFee(Wallet wallet) {
        var accountData = getAccountData(wallet);
        if (accountData == null) {
            return 0;
        }

        // "0%" fee (https://xrpl.org/transfer-fees.html)
        final UnsignedInteger zeroFee = UnsignedInteger.valueOf(1000000000);
        var transferRate = accountData.transferRate().orElse(zeroFee);
        // Return fee as value between 0.0 (0%) and 1.0 (100%)
        return transferRate.minus(zeroFee).doubleValue() / zeroFee.doubleValue();
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

    public boolean walletAcceptsXrp(Wallet wallet) {
        var accountData = getAccountData(wallet);
        if (accountData == null) {
            return false;
        }
        return !accountData.flags().lsfDisallowXrp();
    }

    private Transaction toTransaction(org.xrpl.xrpl4j.model.transactions.Payment p, XrpCurrencyAmount deliveredAmount) throws DecoderException, UnsupportedEncodingException {
        return toTransaction((org.xrpl.xrpl4j.model.transactions.Transaction) p, deliveredAmount);
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
            for (var ft : unwrappedMemo.freeTexts()) {
                trx.addMessage(ft);
            }
        }

        var l = new StructuredReferenceLookup(t);
        for (var r : l.find()) {
            trx.addStructuredReference(r);
        }

        if (t.transactionType() == TransactionType.PAYMENT) {
            var p = (Payment) t;
            trx.setReceiver(WalletConverter.from(p.destination()));
            if (p.destinationTag().isPresent()) {
                trx.setDestinationTag(p.destinationTag().get().toString());
            }
            trx.setInvoiceId(p.invoiceId().isEmpty() ? "" : p.invoiceId().get().value());
        }

        return trx;
    }

    public void refreshBalance(Wallet wallet, boolean useCache) {
        if (!useCache) {
            accountDataCache.evict(wallet);
            accountTrustLineCache.evict(wallet);
        }
        var accountData = getAccountData(wallet);
        if (accountData != null) {
            wallet.getBalances().set(Money.of(accountData.balance().toXrp().doubleValue(), new Currency(ledger.getNativeCcySymbol())));
        }

        for (var t : listTrustlines(wallet)) {
            wallet.getBalances().set(t.getBalance());
        }
    }

    public Trustline[] listTrustlines(Wallet wallet) {
        var list = new ArrayList<Trustline>();
        var result = getAccountLines(wallet);
        if (result == null) {
            return new Trustline[0];
        }
        for (var line : result.lines()) {
            var amt = Double.parseDouble(line.balance());
            var issuer = amt >= 0 ? ledger.createWallet(line.account().value(), "") : wallet;
            var ccy = new Currency(toCurrencyCode(line.currency()), issuer);
            ccy.setTransferFee(getTransferFee(WalletConverter.from(issuer)));
            var balance = Money.of(amt, ccy);
            var lmt = Double.parseDouble(line.limit());
            var limit = Money.of(lmt, ccy);

            list.add(new Trustline(wallet, balance, limit));
        }
        return list.toArray(new Trustline[0]);
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

    public TransactionSubmitter createTransactionSubmitter(PrivateKeyProvider privateKeyProvider) {
        return new RpcSubmitter(ledger, xrplClient, privateKeyProvider);
    }

    public com.radynamics.dallipay.cryptoledger.Wallet createRandomWallet(HttpUrl faucetUrl) {
        var walletFactory = DefaultWalletFactory.getInstance();
        var w = walletFactory.randomWallet(network.isTestnet());

        var faucetClient = FaucetClient.construct(faucetUrl);
        faucetClient.fundAccount(FundAccountRequest.of(w.wallet().classicAddress()));

        var wallet = ledger.createWallet(w.wallet().classicAddress().value(), w.seed());
        ledger.refreshBalance(wallet, false);
        return wallet;
    }

    public EndpointInfo getEndpointInfo(NetworkInfo networkInfo) {
        ServerInfoResult serverInfo;
        try {
            var c = new XrplClient(networkInfo.getUrl());
            serverInfo = c.serverInformation();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

        var future = new CompletableFuture<EndpointInfo>();
        serverInfo.info().handle(rippledServerInfo -> future.complete(EndpointInfo.builder()
                .networkInfo(networkInfo)
                .serverVersion(rippledServerInfo.buildVersion())
                .hostId(rippledServerInfo.hostId())
        ), clioServerInfo -> future.complete(EndpointInfo.builder()
                .networkInfo(networkInfo)
                .serverVersion(clioServerInfo.rippledVersion().orElse(clioServerInfo.clioVersion()))
        ), reportingModeServerInfo -> future.complete(EndpointInfo.builder()
                .networkInfo(networkInfo)
                .serverVersion(reportingModeServerInfo.buildVersion())
                .hostId(reportingModeServerInfo.hostId())
        ));
        return future.join();
    }
}
