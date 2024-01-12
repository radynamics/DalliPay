package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import okhttp3.HttpUrl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JSidechainTextField extends JTextField {
    private final Frame owner;
    private Ledger ledger;
    private final ArrayList<SidechainChangedListener> sidechainChangedListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public JSidechainTextField(Frame owner, Ledger ledger) {
        this.owner = owner;
        this.ledger = ledger;
        putClientProperty("JTextField.placeholderText", res.getString("addConnection"));
        putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, createToolbar());

        registerKeyboardAction(e -> onAccept(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JToolBar createToolbar() {
        var toolbar = new JToolBar();
        {
            var cmd = new JToggleButton(new FlatSVGIcon("svg/arrowRightCircle.svg", 16, 16));
            toolbar.add(cmd);
            Utils.setRolloverIcon(cmd);
            cmd.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onAccept();
                }
            });
        }

        return toolbar;
    }

    private void onAccept() {
        var value = getText();
        setText("");

        var networkInfo = createNetworkInfo(value);
        if (networkInfo == null) {
            return;
        }

        raiseSidechainCreated(networkInfo);
    }

    private NetworkInfo createNetworkInfo(String value) {
        HttpUrl url = null;
        var displayName = value;
        try {
            url = HttpUrl.get(value);
            displayName = NetworkInfo.createDisplayName(url);
        } catch (Exception ex) {
        }

        return NetworkInfoEdit.show(owner, this, ledger, url, displayName);
    }

    public void addChangedListener(SidechainChangedListener l) {
        sidechainChangedListener.add(l);
    }

    private void raiseSidechainCreated(NetworkInfo networkInfo) {
        for (var l : sidechainChangedListener) {
            l.onCreated(networkInfo);
        }
    }
}
