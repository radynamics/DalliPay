package com.radynamics.dallipay.ui.wizard;

import com.radynamics.dallipay.cryptoledger.LedgerId;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class WizardController {
    private final Wizard wizard;
    private LedgerId ledgerId;
    private final Stack<AbstractWizardPage> pageHistory = new Stack<AbstractWizardPage>();
    private AbstractWizardPage currentPage = null;

    public WizardController(Wizard wizard) {
        if (wizard == null) throw new IllegalArgumentException("Parameter 'wizard' cannot be null");
        this.wizard = wizard;
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        wizard.nextButton().addActionListener(new NextPageListener());
        wizard.previousButton().addActionListener(new PreviousPageListener());
    }

    public boolean showNextPage(AbstractWizardPage nextPage) {
        if (nextPage == null) {
            // Next page is null. Updating buttons and ignoring request.
            updateButtons();
            return false;
        }
        if (currentPage != null) {
            pageHistory.push(currentPage);
        }
        setPage(nextPage);
        return true;
    }

    public boolean showPreviousPage() {
        AbstractWizardPage previousPage;
        try {
            previousPage = pageHistory.pop();
        } catch (EmptyStackException e) {
            // Previous page is null. Updating buttons and ignoring request.
            updateButtons();
            return false;
        }
        setPage(previousPage);
        return true;
    }

    private void setPage(AbstractWizardPage newPage) {
        var pageContainer = wizard.pageContainer();
        if (currentPage != null) {
            pageContainer.remove(currentPage);
        }
        currentPage = newPage;
        currentPage.wizardController(this);
        pageContainer.add(currentPage);
        pageContainer.validate();
        pageContainer.repaint();
        updateButtons();
    }

    public void startWizard(AbstractWizardPage startPage) {
        if (startPage == null) throw new IllegalArgumentException("Parameter 'startPage' cannot be null");
        if (currentPage != null) {
            wizard.pageContainer().remove(currentPage);
            pageHistory.clear();
            currentPage = null;
        }
        showNextPage(startPage);
    }

    public void updateButtons() {
        var cancelButton = wizard.cancelButton();
        if (cancelButton != null) {
            cancelButton.setEnabled(currentPage.cancelAllowed());
        }
        var previousButton = wizard.previousButton();
        if (previousButton != null) {
            previousButton.setEnabled(currentPage.previousAllowed() && !pageHistory.isEmpty());
        }
        var nextButton = wizard.nextButton();
        if (nextButton != null) {
            nextButton.setEnabled(currentPage.nextAllowed() && (currentPage.nextPage() != null));
        }
        var finishButton = wizard.finishButton();
        if (finishButton != null) {
            finishButton.setEnabled(currentPage.finishAllowed());
        }
    }

    public AbstractWizardPage currentPage() {
        return currentPage;
    }

    public List<AbstractWizardPage> pageHistoryList() {
        return new ArrayList<>(pageHistory);
    }

    public LedgerId ledgerId() {
        return ledgerId;
    }

    public void ledgerId(LedgerId ledgerId) {
        this.ledgerId = ledgerId;
    }

    private class NextPageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showNextPage(currentPage.nextPage());
        }
    }

    private class PreviousPageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showPreviousPage();
        }
    }
}
