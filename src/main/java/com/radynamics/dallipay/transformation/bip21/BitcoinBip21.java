package com.radynamics.dallipay.transformation.bip21;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.transaction.Origin;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.Payment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BitcoinBip21 {
    private final static Logger log = LogManager.getLogger(BitcoinBip21.class);
    private final Ledger ledger;

    public BitcoinBip21(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public static boolean matches(String text) {
        return BitcoinPaymentURI.parse(text) != null;
    }

    public Payment createOrNull(String text) {
        if (!matches(text)) {
            return null;
        }

        var bip21 = BitcoinPaymentURI.parse(text);

        var payment = new Payment(ledger.createTransaction());
        payment.setAmount(Money.of(bip21.getAmount() == null ? 0 : bip21.getAmount(), new Currency(ledger.getNativeCcySymbol())));
        payment.setReceiverWallet(ledger.createWallet(bip21.getAddress(), null));
        payment.setOrigin(Origin.Manual);

        if (bip21.getLabel() != null) {
            payment.setReceiverAddress(new Address(bip21.getLabel()));
        }

        if (bip21.getMessage() != null) {
            payment.addMessage(bip21.getMessage());
        }

        return payment;
    }
}
