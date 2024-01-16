package com.radynamics.dallipay.ui.wizard.welcome;

import com.radynamics.dallipay.ui.wizard.AbstractWizardPage;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class StartPage extends AbstractWizardPage {
    private final AbstractWizardPage nextPage = new LedgerPage();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public StartPage() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Utils.createText(res.getString("section0")));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(Utils.createText(res.getString("section1")));
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(Utils.createText(res.getString("section2")));
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
