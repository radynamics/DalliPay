package com.radynamics.dallipay.cryptoledger.bitcoin;

import com.radynamics.dallipay.ui.wizard.AbstractWizardPage;
import com.radynamics.dallipay.ui.wizard.welcome.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class SetupBitcoinCoreWalletPage extends AbstractWizardPage {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public SetupBitcoinCoreWalletPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Utils.createText(res.getString("section0")));
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
