package com.radynamics.dallipay.cryptoledger.xrpl;

import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.generic.Wallet;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.exchange.CurrencyPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class XrplPriceOracleConfig {
    final static Logger log = LogManager.getLogger(XrplPriceOracleConfig.class);
    private final HashSet<IssuedCurrency> issuedCurrencies = new HashSet<>();
    private final LedgerId ledgerId;

    private final static ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public final static String AsReceived = "asReceived";
    public final static String AsReceivedText = res.getString("asReceived");

    public XrplPriceOracleConfig(LedgerId ledgerId) {
        if (ledgerId == null) throw new IllegalArgumentException("Parameter 'ledgerId' cannot be null");
        this.ledgerId = ledgerId;
    }

    public void load(ConfigRepo repo) throws Exception {
        issuedCurrencies.clear();
        var json = repo.getXrplPriceOracleConfig(ledgerId);
        if (json.isPresent()) {
            issuedCurrencies.addAll(Arrays.asList(fromJson(json.get())));
        }
    }

    public void save() throws Exception {
        try (var repo = new ConfigRepo()) {
            save(repo);
            repo.commit();
        } catch (Exception e) {
            throw e;
        }
    }

    public void save(ConfigRepo repo) throws Exception {
        repo.setXrplPriceOracleConfig(ledgerId, toJson(issuedCurrencies));
    }

    private JSONObject toJson(Collection<IssuedCurrency> ccys) {
        var o = new JSONObject();

        o.put("version", 1);
        var ccyPairs = new JSONArray();
        o.put("ccyPairs", ccyPairs);
        for (var ccy : ccys) {
            var k = new JSONObject();
            ccyPairs.put(k);
            k.put("first", ccy.getPair().getFirstCode());
            k.put("second", ccy.getPair().getSecondCode());
            k.put("issuer", ccy.getIssuer().getPublicKey());
            k.put("receiver", ccy.getReceiver().getPublicKey());
        }

        return o;
    }

    private IssuedCurrency[] fromJson(JSONObject json) {
        var list = new ArrayList<IssuedCurrency>();

        var ccyPairs = json.getJSONArray("ccyPairs");
        for (var i = 0; i < ccyPairs.length(); i++) {
            var k = ccyPairs.getJSONObject(i);
            var pair = new CurrencyPair(k.getString("first"), k.getString("second"));
            list.add(new IssuedCurrency(pair, new Wallet(LedgerId.Xrpl, k.getString("issuer")), new Wallet(LedgerId.Xrpl, k.getString("receiver"))));
        }

        return list.toArray(new IssuedCurrency[0]);
    }

    public IssuedCurrency[] issuedCurrencies() {
        return issuedCurrencies.toArray(new IssuedCurrency[0]);
    }

    public void set(List<IssuedCurrency> issuedCurrencies) {
        this.issuedCurrencies.clear();
        this.issuedCurrencies.addAll(issuedCurrencies);
    }
}
