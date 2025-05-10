package com.radynamics.dallipay.db;

import com.radynamics.dallipay.cryptoledger.*;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.iso20022.camt054.*;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class ConfigRepo implements AutoCloseable {
    private final Connection conn;

    private ConfigRepo(Connection conn) {
        if (conn == null) throw new IllegalArgumentException("Parameter 'conn' cannot be null");
        this.conn = conn;
    }

    public ConfigRepo() {
        this(Database.connect());
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }

    public DateFormat getBookingDateFormat() throws Exception {
        return DateFormatHelper.toType(single("bookingFormat").orElse(DateFormatHelper.toKey(DateFormat.DateTime)));
    }

    public void setBookingDateFormat(DateFormat value) throws Exception {
        saveOrUpdate("bookingFormat", DateFormatHelper.toKey(value));
    }

    public DateFormat getValutaDateFormat() throws Exception {
        return DateFormatHelper.toType(single("valutaFormat").orElse(DateFormatHelper.toKey(DateFormat.DateTime)));
    }

    public void setValutaDateFormat(DateFormat value) throws Exception {
        saveOrUpdate("valutaFormat", DateFormatHelper.toKey(value));
    }

    public Optional<JSONObject> getXrplPriceOracleConfig(LedgerId ledgerId) throws Exception {
        var value = single(createLedgerSpecificKey(ledgerId, "priceOracleConfig"));
        return value.isPresent() ? Optional.of(new JSONObject(value.get())) : Optional.empty();
    }

    public void setXrplPriceOracleConfig(LedgerId ledgerId, JSONObject value) throws Exception {
        saveOrUpdate(createLedgerSpecificKey(ledgerId, "priceOracleConfig"), value.toString());
    }

    public Optional<String> getExchangeRateProvider() throws Exception {
        return single("exchangeRateProvider");
    }

    public void setExchangeRateProvider(ExchangeRateProvider value) throws Exception {
        saveOrUpdate("exchangeRateProvider", value.getId());
    }

    public Optional<String> getHistoricExchangeRateSource(Ledger ledger) throws Exception {
        return single(createLedgerSpecificKey(ledger, "historicExchangeRateSource"));
    }

    public void setHistoricExchangeRateSource(Ledger ledger, ExchangeRateProvider value) throws Exception {
        saveOrUpdate(createLedgerSpecificKey(ledger, "historicExchangeRateSource"), value.getId());
    }

    public String getTargetCcy(String defaultValue) throws Exception {
        return single("targetCcy").orElse(defaultValue);
    }

    public void setTargetCcy(String value) throws Exception {
        saveOrUpdate("targetCcy", value);
    }

    public StructuredReference getCreditorReferenceIfMissing() throws Exception {
        var value = single("creditorReferenceIfMissing").orElse("");
        return value.length() == 0 ? null : StructuredReferenceFactory.create(StructuredReferenceFactory.detectType(value), value);
    }

    public void setCreditorReferenceIfMissing(String value) throws Exception {
        saveOrUpdate("creditorReferenceIfMissing", value);
    }

    public File getDefaultInputDirectory() throws Exception {
        var value = single("inputDirectory").orElse("");
        return value.length() == 0 ? null : new File(value);
    }

    public void setDefaultInputDirectory(File value) throws Exception {
        saveOrUpdate("inputDirectory", value == null ? "" : value.getAbsolutePath());
    }

    public File getDefaultOutputDirectory() throws Exception {
        var value = single("outputDirectory").orElse("");
        return value.length() == 0 ? null : new File(value);
    }

    public void setDefaultOutputDirectory(File value) throws Exception {
        saveOrUpdate("outputDirectory", value == null ? "" : value.getAbsolutePath());
    }

    public HttpUrl getLastUsedRpcUrl(Ledger ledger) throws Exception {
        var value = single(createLedgerSpecificKey(ledger, "lastUsedRpcUrl")).orElse("");
        return value.length() == 0 ? null : HttpUrl.get(value);
    }

    public void setLastUsedRpcUrl(Ledger ledger, HttpUrl value) throws Exception {
        var key = createLedgerSpecificKey(ledger, "lastUsedRpcUrl");
        if (value == null) {
            delete(key);
        } else {
            saveOrUpdate(key, value.toString());
        }
    }

    public NetworkInfo[] getCustomSidechains(Ledger ledger) throws Exception {
        return getCustomSidechains(ledger.getId());
    }

    public NetworkInfo[] getCustomSidechains(LedgerId ledgerId) throws Exception {
        var value = single(createLedgerSpecificKey(ledgerId, "customSidechains")).orElse("");
        return value.length() == 0 ? new NetworkInfo[0] : NetworkInfoJsonSerializer.parse(new JSONArray(value));
    }

    public void setCustomSidechains(Ledger ledger, NetworkInfo[] entries) throws Exception {
        saveOrUpdate(createLedgerSpecificKey(ledger, "customSidechains"), NetworkInfoJsonSerializer.toJsonArray(entries).toString());
    }

    public LedgerId getLastUsedLedger() throws Exception {
        var value = single("lastUsedLedger").orElse("");
        return value.length() == 0 ? null : LedgerId.of(value);
    }

    public void saveLastUsedLedger(LedgerId value) throws Exception {
        saveOrUpdate("lastUsedLedger", value == null ? "" : value.textId());
    }

    public TransactionSubmitter getLastUsedSubmitter(Component parentComponent, Ledger ledger) throws Exception {
        var value = single(createLastUsedSubmitterKey(ledger)).orElse(null);
        if (value == null) {
            return null;
        }
        return ledger.createTransactionSubmitterFactory().create(value, parentComponent);
    }

    private static String createLastUsedSubmitterKey(Ledger ledger) {
        return createLedgerSpecificKey(ledger, "lastUsedSubmitter");
    }

    private static String createLedgerSpecificKey(Ledger ledger, String key) {
        return createLedgerSpecificKey(ledger.getId(), key);
    }

    private static String createLedgerSpecificKey(LedgerId ledgerId, String key) {
        return String.format("%s_%s", ledgerId, key);
    }

    public void setLastUsedSubmitter(TransactionSubmitter submitter) throws Exception {
        saveOrUpdate(createLastUsedSubmitterKey(submitter.getLedger()), submitter.getId());
    }

    public Wallet getDefaultSenderWallet(Ledger ledger) throws Exception {
        var value = single(createDefaultSenderWalletKey(ledger.getId())).orElse(null);
        return value == null ? null : ledger.createWallet(value, null);
    }

    public void setDefaultSenderWallet(LedgerId ledgerId, Wallet wallet) throws Exception {
        var key = createDefaultSenderWalletKey(ledgerId);
        if (wallet == null) {
            delete(key);
        } else {
            saveOrUpdate(key, wallet.getPublicKey());
        }
    }

    private static String createDefaultSenderWalletKey(LedgerId ledgerId) {
        return String.format("%s_defaultSenderWallet", ledgerId);
    }

    public CamtFormat getDefaultExportFormat() throws Exception {
        return CamtFormatHelper.toType(single("exportFormat").orElse(CamtFormatHelper.toKey(CamtFormatHelper.getDefault())));
    }

    public void setDefaultExportFormat(CamtFormat value) throws Exception {
        saveOrUpdate("exportFormat", CamtFormatHelper.toKey(value));
    }

    public LedgerCurrencyFormat getExportLedgerCurrencyFormat(Ledger ledger) throws Exception {
        var value = single(createLedgerSpecificKey(ledger.getId(), "exportLedgerCurrencyFormat"));
        var defaultValue = ledger.createLedgerCurrencyConverter(LedgerCurrencyFormat.Native).getDefaultTargetFormat();
        return value.isPresent() ? LedgerCurrencyFormatHelper.toType(value.get()) : defaultValue;
    }

    public void setExportLedgerCurrencyFormat(LedgerId ledgerId, LedgerCurrencyFormat value) throws Exception {
        saveOrUpdate(createLedgerSpecificKey(ledgerId, "exportLedgerCurrencyFormat"), LedgerCurrencyFormatHelper.toKey(value));
    }

    public Optional<String> getLookupProviderId(LedgerId ledgerId) throws Exception {
        return single(createLedgerSpecificKey(ledgerId, "lookupProviderId"));
    }

    public void setLookupProviderId(LedgerId ledgerId, String value) throws Exception {
        saveOrUpdate(createLedgerSpecificKey(ledgerId, "lookupProviderId"), value);
    }

    public String getXummAccessToken() throws Exception {
        return single("xummAccessToken").orElse(null);
    }

    public void setXummAccessToken(String value) throws Exception {
        var key = "xummAccessToken";
        if (StringUtils.isEmpty(value)) {
            delete(key);
        } else {
            saveOrUpdate(key, value);
        }
    }

    public Optional<Integer> getXummLocalHttpServerPort() throws Exception {
        return single("xummLocalHttpServerPort").map(Integer::parseInt);
    }

    public HttpUrl getFaucetUrl(Ledger ledger) throws Exception {
        var value = single(String.format("%s_faucetUrl", ledger.getId())).orElse("");
        return value.length() == 0 ? ledger.getDefaultFaucetUrl() : HttpUrl.get(value);
    }

    public Optional<String> getApiKeyXumm() throws Exception {
        return single("apiKeyXumm");
    }

    public void setApiKeyXumm(String value) throws Exception {
        saveOrDeleteIfEmpty("apiKeyXumm", value);
    }

    public Optional<HttpUrl> getCryptoPriceOracleUrl() throws Exception {
        var value = single("cryptoPriceOracleUrl").orElse("");
        return value.length() == 0 ? Optional.empty() : Optional.of(HttpUrl.get(value));
    }

    public Optional<String> getApiKeyCryptoPriceOracle() throws Exception {
        return single("apiKeyCryptoPriceOracle");
    }

    public void setApiKeyCryptoPriceOracle(String value) throws Exception {
        saveOrDeleteIfEmpty("apiKeyCryptoPriceOracle", value);
    }

    private void saveOrDeleteIfEmpty(String key, String value) throws Exception {
        if (StringUtils.isEmpty(value)) {
            delete(key);
        } else {
            saveOrUpdate(key, value);
        }
    }

    private Optional<String> single(String key) throws Exception {
        var ps = conn.prepareStatement("SELECT value FROM config WHERE key = ?");
        ps.setString(1, key);

        return Database.singleString(ps.executeQuery(), "value");
    }

    private void saveOrUpdate(String key, String value) throws Exception {
        String sql = "INSERT OR REPLACE INTO config (id, key, value) \n"
                + "	    VALUES ((SELECT id FROM config WHERE key = ?), ?, ?);";
        var ps = conn.prepareStatement(sql);
        ps.setString(1, key);
        ps.setString(2, key);
        ps.setString(3, value);

        ps.executeUpdate();
    }

    private void delete(String key) throws SQLException {
        String sql = "DELETE FROM config WHERE key = ?;";
        var ps = conn.prepareStatement(sql);
        ps.setString(1, key);

        ps.executeUpdate();
    }

    public void commit() throws SQLException {
        conn.commit();
    }
}
