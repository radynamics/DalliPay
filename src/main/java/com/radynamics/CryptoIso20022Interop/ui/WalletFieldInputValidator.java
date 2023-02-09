package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WalletFieldInputValidator implements InputControlValidator {
    private final static Logger log = LogManager.getLogger(WalletFieldInputValidator.class);
    private final Ledger ledger;

    public WalletFieldInputValidator(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    public Wallet getValidOrNull(Object value) {
        return getValidOrNull((String) value);
    }

    public Wallet getValidOrNull(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        var wallet = ledger.createWallet(text, null);
        var result = new WalletValidator(ledger).validateFormat(wallet);
        return result == null ? wallet : null;
    }

    @Override
    public String getValidExampleInput() {
        return "\"rn8A9923tgWJGSQEQLoYfU2qNsn9nWSUKk\"";
    }
}
