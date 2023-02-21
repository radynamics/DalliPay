package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.Money;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class MoneyTextFieldInputValidator implements InputControlValidator {
    private final static Logger log = LogManager.getLogger(MoneyTextFieldInputValidator.class);

    @Override
    public boolean isValid(Object value) {
        return getValidOrNull((String) value) != null;
    }

    public Money getValidOrNull(String text) {
        if (text == null) {
            return null;
        }

        // Eg: "10.50 USD"
        var words = text.split(" ");
        for (var i = 0; i < words.length; i++) {
            words[i] = StringUtils.deleteWhitespace(words[i]);
        }
        if (words.length != 2 || words[0].length() == 0 || words[1].length() == 0) {
            return null;
        }

        var nf = NumberFormat.getInstance(Locale.getDefault());
        // Eg: "1'000.50 USD"
        double amt = 0;
        try {
            amt = nf.parse(words[0]).doubleValue();
        } catch (ParseException e) {
            log.info(e.getMessage(), e);
            return null;
        }
        var ccy = new Currency(words[1].toUpperCase());
        return Money.of(amt, ccy);
    }

    @Override
    public String getValidExampleInput() {
        return "\"10 USD\" or \"5.33 EUR\"";
    }
}
