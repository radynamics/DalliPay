package com.radynamics.CryptoIso20022Interop.exchange;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XummPriceOracle;
import org.apache.commons.lang3.NotImplementedException;

public final class HistoricExchangeRateSourceFactory {
    public static final HistoricExchangeRateSource create(Ledger ledger, String id) {
        switch (id.toLowerCase()) {
            case XummPriceOracle.ID:
                return new XummPriceOracle((com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.Ledger) ledger);
            default:
                throw new NotImplementedException(String.format("Source %s unknown.", id));
        }
    }
}
