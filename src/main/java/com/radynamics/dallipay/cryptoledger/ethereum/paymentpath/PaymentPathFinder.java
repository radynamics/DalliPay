package com.radynamics.dallipay.cryptoledger.ethereum.paymentpath;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.PaymentPath;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.CurrencyConverter;
import com.radynamics.dallipay.iso20022.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PaymentPathFinder implements com.radynamics.dallipay.cryptoledger.PaymentPathFinder {
    public PaymentPath[] find(CurrencyConverter currencyConverter, Payment p) {
        if (p == null) throw new IllegalArgumentException("Parameter 'p' cannot be null");

        var list = new ArrayList<PaymentPath>();

        list.addAll(List.of(knownTokensOf(p.getUserCcy().getCode(), p.getLedger())));

        return list.toArray(new PaymentPath[0]);
    }

    private Erc20Path[] knownTokensOf(String ccy, Ledger ledger) {
        if (Stream.of("USD", "USDT", "USDC").anyMatch(o -> o.equalsIgnoreCase(ccy))) {
            return new Erc20Path[]{
                    new Erc20Path(new Currency("USD", "USDT", ledger.createWallet("0xdAC17F958D2ee523a2206206994597C13D831ec7", null))),
                    new Erc20Path(new Currency("USD", "USDC", ledger.createWallet("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48", null))),
            };
        }

        if (Stream.of("CHF", "XCHF").anyMatch(o -> o.equalsIgnoreCase(ccy))) {
            return new Erc20Path[]{
                    new Erc20Path(new Currency("CHF", "XCHF", ledger.createWallet("0xb4272071ecadd69d933adcd19ca99fe80664fc08", null))),
            };
        }

        return new Erc20Path[0];
    }
}
