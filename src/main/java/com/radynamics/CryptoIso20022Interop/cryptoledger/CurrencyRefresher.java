package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.exchange.Currency;
import com.radynamics.CryptoIso20022Interop.exchange.Money;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.util.Arrays;

public class CurrencyRefresher {
    public void refresh(Payment p) {
        var senderValid = WalletValidator.isValidFormat(p.getLedger(), p.getSenderWallet());
        var receiverValid = WalletValidator.isValidFormat(p.getLedger(), p.getReceiverWallet());

        // Use ledger native currency if information about sender/receiver is missing
        if (!senderValid || !receiverValid) {
            setAmountLedgerUnit(p);
            return;
        }

        var available = Arrays.stream(p.getSenderWallet().getBalances().all()).map(Money::getCcy).toArray(Currency[]::new);
        Currency ccyAnyIssuer = findSameCode(available, p.getUserCcy());
        // Use ledger native currency if expected currency code isn't available
        if (ccyAnyIssuer == null) {
            setAmountLedgerUnit(p);
            return;
        }

        p.setAmount(Money.of(p.getAmount(), ccyAnyIssuer));
    }

    private void setAmountLedgerUnit(Payment p) {
        p.setExchangeRate(null);
        p.setAmount(Money.of(p.getAmount(), p.getUserCcy().withoutIssuer()));
    }

    private static Currency findSameCode(Currency[] available, Currency ccy) {
        for (var o : available) {
            if (o.sameCode(ccy)) {
                return o;
            }
        }

        return null;
    }
}
