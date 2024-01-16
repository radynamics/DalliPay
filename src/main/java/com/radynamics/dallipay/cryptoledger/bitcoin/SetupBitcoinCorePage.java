package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.radynamics.dallipay.ui.wizard.AbstractWizardPage;
import com.radynamics.dallipay.ui.wizard.welcome.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class SetupBitcoinCorePage extends AbstractWizardPage {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public SetupBitcoinCorePage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Utils.createText(res.getString("section0")));
        add(Box.createRigidArea(new Dimension(0, 5)));

        add(Utils.createText(res.getString("bitcoinConf")));
        var lbl = new JTextArea(res.getString("bitcoinConfExample"));
        add(lbl);
        lbl.setBorder(BorderFactory.createEmptyBorder());
        lbl.setEditable(false);
        lbl.setLineWrap(true);
        lbl.setWrapStyleWord(true);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(Utils.createText(res.getString("section1")));
    }

    @Override
    protected AbstractWizardPage nextPage() {
        return null;
    }

    @Override
    protected boolean cancelAllowed() {
        return true;
    }

    @Override
    protected boolean previousAllowed() {
        return true;
    }

    @Override
    protected boolean nextAllowed() {
        return false;
    }

    @Override
    protected boolean finishAllowed() {
        return true;
    }
}
