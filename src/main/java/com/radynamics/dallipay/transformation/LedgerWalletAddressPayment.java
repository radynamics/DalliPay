package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Payment;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class LedgerWalletAddressPayment {
    private final Ledger ledger;

    public LedgerWalletAddressPayment(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public static boolean matches(Ledger ledger, String text) {
        if (text == null) {
            return false;
        }

        return ledger.createWalletAddressResolver().resolve(text) != null;
    }

    private static String reduce(String text) {
        var lines = text.split("\\r?\\n");
        for (var i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
        }

        // remove leading/trailing empty lines
        var content = Arrays.stream(lines).filter(o -> !StringUtils.isBlank(o)).toArray(String[]::new);
        return content.length == 0 ? "" : String.join(System.lineSeparator(), content);
    }

    public Payment createOrNull(String text) {
        if (!matches(ledger, text)) {
            return null;
        }

        var reduced = reduce(text);

        var payment = new Payment(ledger.createTransaction());
        payment.setAmount(Money.zero(new Currency(ledger.getNativeCcySymbol())));
        var addressInfo = ledger.createWalletAddressResolver().resolve(reduced);
        payment.setReceiverWallet(addressInfo.getWallet());
        payment.setDestinationTag(addressInfo.getDestinationTag());
        payment.setOrigin(Origin.Manual);

        return payment;
    }
}
