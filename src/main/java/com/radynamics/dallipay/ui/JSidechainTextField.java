package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JSidechainTextField extends JTextField {
    private final ArrayList<SidechainChangedListener> sidechainChangedListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public JSidechainTextField() {
        putClientProperty("JTextField.placeholderText", res.getString("addCustomSidechain"));
        putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);

        registerKeyboardAction(e -> onAccept(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void onAccept() {
        var value = getText();
        setText("");

        if (StringUtils.isEmpty(value)) {
            return;
        }

        HttpUrl httpUrl;
        try {
            httpUrl = HttpUrl.get(value);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, String.format(res.getString("urlParseFailed"), value), res.getString("connectionFailed"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        raiseSidechainChanged(NetworkInfo.create(httpUrl, value));
    }

    public void addChangedListener(SidechainChangedListener l) {
        sidechainChangedListener.add(l);
    }

    private void raiseSidechainChanged(NetworkInfo networkInfo) {
        for (var l : sidechainChangedListener) {
            l.onChanged(networkInfo);
        }
    }
}
