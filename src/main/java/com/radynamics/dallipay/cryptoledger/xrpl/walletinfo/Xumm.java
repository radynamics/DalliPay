package com.radynamics.dallipay.cryptoledger.xrpl.walletinfo;

import com.radynamics.dallipay.cryptoledger.Cache;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletInfo;
import com.radynamics.dallipay.cryptoledger.WalletInfoProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

public class Xumm implements WalletInfoProvider {
    final static Logger log = LogManager.getLogger(WalletInfoProvider.class);
    private final Cache<WalletInfo[]> cache = new Cache<>("");

    @Override
    public WalletInfo[] list(Wallet wallet) throws WalletInfoLookupException {
        cache.evictOutdated();
        var data = cache.get(wallet);
        if (data != null) {
            return data;
        }
        // Contained without data means "wallet doesn't exist" (wasn't found previously)
        if (cache.isPresent(wallet)) {
            return new WalletInfo[0];
        }

        JSONObject result;
        try {
            result = load(wallet);
        } catch (IOException e) {
            if (isTooManyRequests(e)) {
                log.trace(e.getMessage(), e);
                return new WalletInfo[0];
            }
            throw new WalletInfoLookupException(e.getMessage(), e);
        } catch (WalletInfoLookupException e) {
            if (isTooManyRequests(e)) {
                log.trace(e.getMessage(), e);
                return new WalletInfo[0];
            }
            throw e;
        }
        if (result == null) {
            return new WalletInfo[0];
        }

        var kycApproved = false;
        var list = new ArrayList<WalletInfo>();

        {
            kycApproved = result.getBoolean("kycApproved");
            var wi = new WalletInfo(this, "KYC approved", kycApproved, 80);
            wi.setVerified(kycApproved);
            list.add(wi);
        }
        if (result.has("xummProfile")) {
            var xummProfile = result.getJSONObject("xummProfile");
            var accountAlias = get(xummProfile, "accountAlias").orElse(null);
            if (accountAlias != null) {
                var wi = new WalletInfo(this, "XUMM account alias", accountAlias, 50, InfoType.Name);
                wi.setVerified(kycApproved);
                list.add(wi);
            }
            var ownerAlias = get(xummProfile, "ownerAlias").orElse(null);
            if (ownerAlias != null) {
                var wi = new WalletInfo(this, "XUMM owner alias", ownerAlias, 60, InfoType.Name);
                wi.setVerified(kycApproved);
                list.add(wi);
            }
            var profileUrl = get(xummProfile, "profileUrl").orElse(null);
            if (profileUrl != null) {
                var wi = new WalletInfo(this, "XUMM profile", profileUrl, 50, InfoType.Url);
                wi.setVerified(kycApproved);
                list.add(wi);
            }
        }

        if (result.has("thirdPartyProfiles")) {
            var thirdPartyProfiles = result.getJSONArray("thirdPartyProfiles");
            for (var i = 0; i < thirdPartyProfiles.length(); i++) {
                var o = thirdPartyProfiles.getJSONObject(i);
                var accountAlias = get(o, "accountAlias").orElse(null);
                if (accountAlias != null) {
                    list.add(new WalletInfo(this, String.format("%s account alias", o.getString("source")), accountAlias, 40));
                }
            }
        }

        if (result.has("globalid")) {
            var globalid = result.getJSONObject("globalid");
            var profileUrl = get(globalid, "profileUrl").orElse(null);
            if (profileUrl != null) {
                list.add(new WalletInfo(this, "GlobaliD profile URL", profileUrl, 50, InfoType.Url));
            }
            if (!globalid.isNull("sufficientTrust")) {
                var wi = new WalletInfo(this, "GlobaliD sufficient trust", globalid.getBoolean("sufficientTrust"), 60);
                wi.setVerified(true);
                list.add(wi);
            }
        }

        var infos = list.toArray(new WalletInfo[0]);
        cache.add(wallet, infos);
        return infos;
    }

    private static Optional<String> get(JSONObject json, String param) {
        if (json.isNull(param)) {
            return Optional.empty();
        }
        var value = json.getString(param);
        return value.length() > 0 ? Optional.of(value) : Optional.empty();
    }

    private static boolean isTooManyRequests(Exception e) {
        // "Too may requests" can be thrown in own exception but also in openStream as IOException.
        final String TOO_MANY_REQUESTS = "429";
        return e.getMessage().contains(TOO_MANY_REQUESTS);
    }

    @Override
    public String getDisplayText() {
        return "Xumm";
    }

    @Override
    public InfoType[] supportedTypes() {
        return new InfoType[0];
    }

    private JSONObject load(Wallet wallet) throws IOException, WalletInfoLookupException {
        URL url = new URL(String.format("https://xumm.app/api/v1/platform/account-meta/%s", wallet.getPublicKey()));

        var conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new WalletInfoLookupException(String.format("Failed to get wallet info for %s from Xumm due HttpResponseCode %s", wallet.getPublicKey(), responseCode));
        }

        var responseString = "";
        var scanner = new Scanner(url.openStream(), StandardCharsets.UTF_8);
        while (scanner.hasNext()) {
            responseString += scanner.nextLine();
        }
        scanner.close();

        var result = new JSONObject(responseString);
        if (!result.has("account")) {
            if (result.has("error")) {
                throwException(result);
            }
            return null;
        }
        return result;
    }

    private void throwException(JSONObject result) throws WalletInfoLookupException {
        throw new WalletInfoLookupException(String.format("%s (Code %s)", result.get("message"), result.get("code")));
    }
}