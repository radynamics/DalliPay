package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WalletFieldInputValidator implements InputControlValidator {
    private final static Logger log = LogManager.getLogger(WalletFieldInputValidator.class);
    private final Ledger ledger;
    private final boolean allowGeneralTerm;

    public WalletFieldInputValidator(Ledger ledger, boolean allowGeneralTerm) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        this.allowGeneralTerm = allowGeneralTerm;
    }

    @Override
    public boolean isValid(Object value) {
        var text = (String) value;
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        return allowGeneralTerm
                ? ledger.createWalletInput(text).valid()
                : getValidOrNull(text) != null;
    }

    public Wallet getValidOrNull(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        return ledger.createWalletInput(text).wallet();
    }

    @Override
    public String getValidExampleInput() {
        return "\"rn8A9923tgWJGSQEQLoYfU2qNsn9nWSUKk\"";
    }
}
