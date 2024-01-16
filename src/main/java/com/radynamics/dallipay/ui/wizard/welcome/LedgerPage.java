package com.radynamics.dallipay.ui.wizard.welcome;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.ui.wizard.AbstractWizardPage;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class LedgerPage extends AbstractWizardPage {
    private final JComboBox<Ledger> cboLedger = new JComboBox<>();
    private final AbstractWizardPage nextPage = new NetworkPage();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public LedgerPage() {
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
        pnl.add(Utils.createImageWithText(res.getString("switchLedger"), "switchLedger.png"));

        add(Box.createRigidArea(new Dimension(0, 20)));
        add(Utils.createText(res.getString("defaultLedger")));

        add(cboLedger);
        cboLedger.setMaximumSize(new Dimension(200, cboLedger.getPreferredSize().height));
        cboLedger.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboLedger.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((Ledger) value).getDisplayText(), index, isSelected, cellHasFocus);
            }
        });
        cboLedger.removeAllItems();
        for (var ledger : LedgerFactory.all()) {
            cboLedger.addItem(ledger);
            if (ledger.getId().sameAs(LedgerId.Xrpl)) {
                cboLedger.setSelectedItem(ledger);
            }
        }
    }

    @Override
    protected AbstractWizardPage nextPage() {
        wizardController().ledgerId(((Ledger) cboLedger.getSelectedItem()).getId());
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
