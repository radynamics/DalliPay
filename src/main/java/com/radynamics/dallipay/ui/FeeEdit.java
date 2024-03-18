package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.FeeRefresher;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.AmountFormatter;
import com.radynamics.dallipay.iso20022.ExchangeRateFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.ResourceBundle;

public class FeeEdit {
    private final ExchangeRateProvider exchangeRateProvider;
    private Currency displayCcy;
    private Money customFeeSuggestion;

    private final Component parentComponent;
    private final JPanel pnl;
    private final JRadioButton rdoLow;
    private final JRadioButton rdoMedium;
    private final JRadioButton rdoHigh;
    private final JRadioButton rdoCustom;
    private final MoneyTextField txtCustomAmount;
    private final JLabel lblCustomAmountInFiat = new JLabel();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public FeeEdit(Component parentComponent, Ledger ledger, ExchangeRateProvider exchangeRateProvider, FeeRefresher feeRefresher) {
        this.parentComponent = parentComponent;
        this.exchangeRateProvider = exchangeRateProvider;

        pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setMinimumSize(new Dimension(250, 130));
        pnl.setMaximumSize(new Dimension(250, 130));
        pnl.setPreferredSize(new Dimension(250, 130));

        txtCustomAmount = new MoneyTextField(ledger);
        txtCustomAmount.placeholderText(res.getString("custom.placeholderText").formatted(ledger.getNativeCcySymbol()));
        txtCustomAmount.addChangedListener(this::onCustomAmountChanged);

        var group = new ButtonGroup();
        rdoLow = createRdo(res.getString("fees.low"));
        group.add(rdoLow);
        rdoMedium = createRdo(res.getString("fees.medium"));
        group.add(rdoMedium);
        rdoHigh = createRdo(res.getString("fees.high"));
        group.add(rdoHigh);
        rdoCustom = createRdo(res.getString("fees.custom"));
        if (!StringUtils.isEmpty(ledger.transactionFeeUnitText())) {
            rdoCustom.setText("%s (%s)".formatted(rdoCustom.getText(), ledger.transactionFeeUnitText()));
        }
        group.add(rdoCustom);

        rdoLow.setSelected(feeRefresher.allLow());
        rdoMedium.setSelected(feeRefresher.allMedium());
        rdoHigh.setSelected(feeRefresher.allHigh());
        rdoCustom.setSelected(feeRefresher.custom());

        pnl.add(createFeeRow(rdoLow));
        pnl.add(createFeeRow(rdoMedium));
        pnl.add(createFeeRow(rdoHigh));
        pnl.add(createFeeRow(rdoCustom));

        {
            var pnlCustomAmount = new JPanel();
            pnl.add(pnlCustomAmount);

            var layout = new SpringLayout();
            pnlCustomAmount.setLayout(layout);

            pnlCustomAmount.add(txtCustomAmount);
            layout.putConstraint(SpringLayout.WEST, txtCustomAmount, 20, SpringLayout.WEST, pnlCustomAmount);
            txtCustomAmount.setMaximumSize(new Dimension(100, 21));

            pnlCustomAmount.add(lblCustomAmountInFiat);
            layout.putConstraint(SpringLayout.WEST, lblCustomAmountInFiat, 10, SpringLayout.EAST, txtCustomAmount);
        }
    }

    private void onCustomAmountChanged() {
        var displayCcy = this.displayCcy == null ? new Currency("USD") : this.displayCcy;
        var pair = new CurrencyPair(txtCustomAmount.getAmount().getCcy(), displayCcy);
        var rate = Arrays.stream(exchangeRateProvider.latestRates()).filter(o -> o.getPair().sameAs(pair)).findFirst();
        if (rate.isPresent()) {
            var cc = new CurrencyConverter(exchangeRateProvider.latestRates());
            var amtInDisplayCcy = Money.of(cc.convert(txtCustomAmount.getAmount(), displayCcy), displayCcy);
            lblCustomAmountInFiat.setText("~%s".formatted(AmountFormatter.formatAmtWithCcyFiat(amtInDisplayCcy)));
            lblCustomAmountInFiat.setToolTipText(ExchangeRateFormatter.format(rate.get()));
        } else {
            lblCustomAmountInFiat.setText("");
        }
    }

    public int showDialog() {
        var optionPane = new JOptionPane();
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        return JOptionPane.showOptionDialog(parentComponent, pnl, res.getString("feePerTrx"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"OK", res.getString("cancel")}, "OK");
    }

    private JRadioButton createRdo(String title) {
        var rdo = new JRadioButton(title);
        rdo.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            var isCustomSelected = rdoCustom.equals(e.getItem());
            txtCustomAmount.setEditable(isCustomSelected);
            txtCustomAmount.setEnabled(isCustomSelected);

            if (isCustomSelected) {
                if (customFeeSuggestion != null && txtCustomAmount.getAmount() == null) {
                    txtCustomAmount.setAmount(customFeeSuggestion);
                    onCustomAmountChanged();
                }
            }
        });
        return rdo;
    }

    private Component createFeeRow(JRadioButton rdo) {
        var pnl = new JPanel();
        var layout = new SpringLayout();
        pnl.setLayout(layout);

        pnl.add(rdo);
        layout.putConstraint(SpringLayout.WEST, rdo, 0, SpringLayout.WEST, pnl);

        return pnl;
    }

    public boolean isLowSelected() {
        return rdoLow.isSelected();
    }

    public boolean isMediumSelected() {
        return rdoMedium.isSelected();
    }

    public boolean isHighSelected() {
        return rdoHigh.isSelected();
    }

    public boolean isCustomSelected() {
        return rdoCustom.isSelected();
    }

    public Money customAmount() {
        return txtCustomAmount.getAmount();
    }

    public void customFeeSuggestion(Money value) {
        customFeeSuggestion = value;
    }

    public void displayCcy(Currency currency) {
        this.displayCcy = currency;
    }
}
