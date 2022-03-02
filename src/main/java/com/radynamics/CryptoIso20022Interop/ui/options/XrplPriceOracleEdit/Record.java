package com.radynamics.CryptoIso20022Interop.ui.options.XrplPriceOracleEdit;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.IssuedCurrency;
import org.apache.commons.lang3.StringUtils;

public class Record {
    public String first;
    public String second;
    public String issuer;
    public String receiver;

    public Record() {
    }

    public Record(IssuedCurrency o) {
        this();
        first = o.getPair().getFirst();
        second = o.getPair().getSecond();
        issuer = o.getIssuer().getPublicKey();
        receiver = o.getReceiver().getPublicKey();
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(first) && StringUtils.isEmpty(second) && StringUtils.isEmpty(issuer) && StringUtils.isEmpty(receiver);
    }

    @Override
    public String toString() {
        return String.format("%s/%s, %s, %s", first, second, issuer, receiver);
    }
}
