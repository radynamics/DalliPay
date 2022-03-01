package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.CryptoIso20022Interop.ui.XrplPriceOracleEdit.XrplPriceOracleEditor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class OptionsForm extends JPanel implements MainFormPane {
    private final ArrayList<ChangedListener> listener = new ArrayList<>();
    private final XrplPriceOracleEditor xrplPriceOracleEditor;
    private final XrplPriceOracleConfig xrplPriceOracleConfig = new XrplPriceOracleConfig();

    public OptionsForm() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var pnlContent = new JPanel();
        add(pnlContent, BorderLayout.CENTER);

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
        {
            var buttonPane = new JPanel();
            add(buttonPane, BorderLayout.PAGE_END);
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
            buttonPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            buttonPane.setPreferredSize(new Dimension(500, 45));
            buttonPane.add(Box.createHorizontalGlue());

            var buttonSize = new Dimension(150, 35);
            {
                var cmd = new JButton("Save");
                cmd.setMinimumSize(buttonSize);
                cmd.setPreferredSize(buttonSize);
                cmd.setMaximumSize(buttonSize);
                cmd.addActionListener(e -> save());
                buttonPane.add(cmd);
            }
        }
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        final var topOffset = 5;
        return line * lineHeight + topOffset;
    }

    private void save() {
        xrplPriceOracleEditor.apply();
        xrplPriceOracleConfig.set(xrplPriceOracleEditor.issuedCurrencies());
        try {
            xrplPriceOracleConfig.save();

            raiseChanged();
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public void load() {
        xrplPriceOracleConfig.load();
        xrplPriceOracleEditor.load(Arrays.asList(xrplPriceOracleConfig.issuedCurrencies()));
    }

    @Override
    public String getTitle() {
        return "Options";
    }

    public void addChangedListener(ChangedListener l) {
        listener.add(l);
    }

    private void raiseChanged() {
        for (var l : listener) {
            l.onChanged();
        }
    }
}
