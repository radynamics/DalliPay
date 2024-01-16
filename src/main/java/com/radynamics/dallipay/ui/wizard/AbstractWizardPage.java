package com.radynamics.dallipay.ui.wizard;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractWizardPage extends JPanel {
    private WizardController wizardController;

    public AbstractWizardPage(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public AbstractWizardPage(LayoutManager layout) {
        super(layout);
    }

    public AbstractWizardPage(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public AbstractWizardPage() {
        super();
    }

    protected WizardController wizardController() {
        return wizardController;
    }

    void wizardController(WizardController wizardController) {
        this.wizardController = wizardController;
    }

    public void updateButtons() {
        wizardController.updateButtons();
    }

    protected abstract AbstractWizardPage nextPage();

    protected abstract boolean cancelAllowed();

    protected abstract boolean previousAllowed();

    protected abstract boolean nextAllowed();

    protected abstract boolean finishAllowed();
}
