package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.exchange.ExchangeRateProvider;

import javax.swing.*;
import java.awt.*;

public class ExchangeRateProviderCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof ExchangeRateProvider) {
            value = ((ExchangeRateProvider) value).getDisplayText();
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
