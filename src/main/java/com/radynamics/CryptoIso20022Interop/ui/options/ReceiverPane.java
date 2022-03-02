package com.radynamics.CryptoIso20022Interop.ui.options;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.CryptoIso20022Interop.ui.XrplPriceOracleEdit.XrplPriceOracleEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ReceiverPane extends JPanel {
    private final XrplPriceOracleEditor xrplPriceOracleEditor;
    private final XrplPriceOracleConfig xrplPriceOracleConfig = new XrplPriceOracleConfig();

    public ReceiverPane() {
        var pnlContent = this;
        pnlContent.setPreferredSize(new Dimension(1000, 400));
        var contentLayout = new SpringLayout();
        pnlContent.setLayout(contentLayout);

        {
            final int paddingWest = 120;
            Component anchorComponentTopLeft;
            {
                var lbl = new JLabel("XRPL price oracle:");
                anchorComponentTopLeft = lbl;
                contentLayout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
                contentLayout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(0), SpringLayout.NORTH, pnlContent);
                lbl.setOpaque(true);
                pnlContent.add(lbl);

                xrplPriceOracleEditor = new XrplPriceOracleEditor();
                contentLayout.putConstraint(SpringLayout.WEST, xrplPriceOracleEditor, paddingWest, SpringLayout.WEST, anchorComponentTopLeft);
                contentLayout.putConstraint(SpringLayout.NORTH, xrplPriceOracleEditor, getNorthPad(0), SpringLayout.NORTH, pnlContent);
                pnlContent.add(xrplPriceOracleEditor);
            }
        }
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        final var topOffset = 5;
        return line * lineHeight + topOffset;
    }

    public void save() throws Exception {
        xrplPriceOracleEditor.apply();
        xrplPriceOracleConfig.set(xrplPriceOracleEditor.issuedCurrencies());

        xrplPriceOracleConfig.save();
    }

    public void load() {
        xrplPriceOracleConfig.load();
        xrplPriceOracleEditor.load(Arrays.asList(xrplPriceOracleConfig.issuedCurrencies()));
    }
}
