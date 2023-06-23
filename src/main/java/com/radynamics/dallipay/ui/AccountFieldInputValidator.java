package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.AccountFactory;
import org.apache.commons.lang3.StringUtils;

public class AccountFieldInputValidator implements InputControlValidator {
    @Override
    public boolean isValid(Object value) {
        var text = (String) value;
        return StringUtils.isEmpty(text) || getValidOrNull(text) != null;
    }

    public Account getValidOrNull(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        return AccountFactory.create(text);
    }

    @Override
    public String getValidExampleInput() {
        return "\"CH56 0483 5012 3456 7800 9\"";
    }
}
