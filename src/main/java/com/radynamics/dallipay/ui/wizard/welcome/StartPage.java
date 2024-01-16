package com.radynamics.dallipay.ui.wizard.welcome;

import com.radynamics.dallipay.ui.wizard.AbstractWizardPage;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class StartPage extends AbstractWizardPage {
    private final AbstractWizardPage nextPage = new NetworkPage();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public StartPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Utils.createText(res.getString("section0")));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(Utils.createText(res.getString("section1")));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(Utils.createText(res.getString("section2")));

        add(Box.createRigidArea(new Dimension(0, 20)));

        var pnl = new JPanel();
        add(pnl);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.add(Utils.createImageWithText(res.getString("mainnet"), "mainnet.png"));
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));
        pnl.add(Utils.createImageWithText(res.getString("testnet"), "testnet.png"));
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));
        pnl.add(Utils.createImageWithText(res.getString("offline"), "offline.png"));
    }

    @Override
    protected AbstractWizardPage nextPage() {
        return nextPage;
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
        return true;
    }

    @Override
    protected boolean finishAllowed() {
        return false;
    }
}
