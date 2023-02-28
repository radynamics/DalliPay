package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LookupProviderException;
import com.radynamics.dallipay.cryptoledger.LookupProviderFactory;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.exchange.Money;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

public abstract class MoneyControl<T extends JComponent> extends JPanel {
    protected final T ctrl;
    private JToggleButton detailButton;
    private Money value;
    private final Ledger ledger;
    private boolean issuerVisible = true;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public MoneyControl(Ledger ledger, T ctrl) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (ctrl == null) throw new IllegalArgumentException("Parameter 'ctrl' cannot be null");
        this.ledger = ledger;
        this.ctrl = ctrl;
        ctrl.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, createToolbar());
        setupUI();
    }

    private JToolBar createToolbar() {
        var toolbar = new JToolBar();
        {
            detailButton = new JToggleButton(new FlatSVGIcon("svg/search.svg", 16, 16));
            toolbar.add(detailButton);
            Utils.setRolloverIcon(detailButton);
            detailButton.setVisible(false);
            detailButton.setToolTipText(res.getString("showIssuer"));
            detailButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (hasAmountIssuer() && e.getClickCount() == 1) {
                        lookup(value.getCcy().getIssuer());
                    }
                }
            });
        }

        return toolbar;
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        {
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(ctrl, c);
        }
    }

    private void lookup(Wallet wallet) {
        try {
            var lp = LookupProviderFactory.createWalletLookupProvider(ledger);
            lp.open(wallet);
        } catch (LookupProviderException ex) {
            ExceptionDialog.show(this, ex);
        }
    }

    public void setAmount(Money value) {
        this.value = value;
        format();
    }

    public Money getAmount() {
        return value;
    }

    public void setIssuerVisible(boolean issuerVisible) {
        this.issuerVisible = issuerVisible;
        format();
    }

    private boolean hasAmountIssuer() {
        return value != null && value.getCcy().getIssuer() != null;
    }

    private void format() {
        refreshIssuerLinkVisibility();
        refreshText(value);
        var ccyFormatter = new CurrencyFormatter(ledger.getInfoProvider());
        ccyFormatter.format(ctrl, value.getCcy());

        var toolTip = new StringBuilder();
        toolTip.append(MoneyFormatter.formatLedger(value));
        if (ctrl.getToolTipText().length() > 0) {
            toolTip.append(System.lineSeparator());
            toolTip.append(ctrl.getToolTipText());
        }
        ctrl.setToolTipText(toolTip.toString());
    }

    private void refreshIssuerLinkVisibility() {
        detailButton.setVisible(issuerVisible && hasAmountIssuer());
    }

    protected abstract void refreshText(Money value);

    protected Ledger getLedger() {
        return ledger;
    }
}
