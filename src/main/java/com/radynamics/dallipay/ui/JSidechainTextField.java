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

        var networkInfo = createNetworkInfo(value);
        if (networkInfo == null) {
            return;
        }

        raiseSidechainCreated(networkInfo);
        raiseSidechainChanged(networkInfo);
    }

    private NetworkInfo createNetworkInfo(String value) {
        HttpUrl url = null;
        var displayName = value;
        try {
            url = HttpUrl.get(value);
            displayName = NetworkInfo.createDisplayName(url);
        } catch (Exception ex) {
        }

        return NetworkInfoEdit.show(this, url, displayName);
    }

    public void addChangedListener(SidechainChangedListener l) {
        sidechainChangedListener.add(l);
    }

    private void raiseSidechainChanged(NetworkInfo networkInfo) {
        for (var l : sidechainChangedListener) {
            l.onChanged(networkInfo);
        }
    }

    private void raiseSidechainCreated(NetworkInfo networkInfo) {
        for (var l : sidechainChangedListener) {
            l.onCreated(networkInfo);
        }
    }
}
