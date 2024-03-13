package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.FeeRefresher;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class FeeEdit {
    private final JPanel pnl;
    private final JRadioButton rdoLow;
    private final JRadioButton rdoMedium;
    private final JRadioButton rdoHigh;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public FeeEdit(FeeRefresher feeRefresher) {
        pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setMinimumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnl.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));

        var group = new ButtonGroup();
        rdoLow = new JRadioButton(res.getString("fees.low"));
        group.add(rdoLow);
        rdoMedium = new JRadioButton(res.getString("fees.medium"));
        group.add(rdoMedium);
        rdoHigh = new JRadioButton(res.getString("fees.high"));
        group.add(rdoHigh);

        rdoLow.setSelected(feeRefresher.allLow());
        rdoMedium.setSelected(feeRefresher.allMedium());
        rdoHigh.setSelected(feeRefresher.allHigh());

        pnl.add(createFeeRow(rdoLow));
        pnl.add(createFeeRow(rdoMedium));
        pnl.add(createFeeRow(rdoHigh));
    }

    public int showDialog(Component parentComponent) {
        var optionPane = new JOptionPane();
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        return JOptionPane.showOptionDialog(parentComponent, pnl, res.getString("feePerTrx"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"OK", res.getString("cancel")}, "OK");
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
}
