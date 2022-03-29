package com.radynamics.CryptoIso20022Interop.db;

import com.radynamics.CryptoIso20022Interop.exchange.Coinbase;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeRateProvider;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtFormatHelper;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormatHelper;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.json.JSONObject;

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

    public JSONObject getXrplPriceOracleConfig() throws Exception {
        return new JSONObject(single("xrplPriceOracleConfig").orElseThrow());
    }

    public void setXrplPriceOracleConfig(JSONObject value) throws Exception {
        saveOrUpdate("xrplPriceOracleConfig", value.toString());
    }

    public String getExchangeRateProvider() throws Exception {
        return single("exchangeRateProvider").orElse(Coinbase.ID);
    }

    public void setExchangeRateProvider(ExchangeRateProvider value) throws Exception {
        saveOrUpdate("exchangeRateProvider", value.getId());
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

    public CamtFormat getDefaultExportFormat() throws Exception {
        return CamtFormatHelper.toType(single("exportFormat").orElse(CamtFormatHelper.toKey(CamtFormatHelper.getDefault())));
    }

    public void setDefaultExportFormat(CamtFormat value) throws Exception {
        saveOrUpdate("exportFormat", CamtFormatHelper.toKey(value));
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

    public void commit() throws SQLException {
        conn.commit();
    }
}
