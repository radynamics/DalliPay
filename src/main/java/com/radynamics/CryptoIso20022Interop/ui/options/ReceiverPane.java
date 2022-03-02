package com.radynamics.CryptoIso20022Interop.ui.options;

import com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import com.radynamics.CryptoIso20022Interop.ui.options.XrplPriceOracleEdit.XrplPriceOracleEditor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ReceiverPane extends JPanel {
    final static Logger log = LogManager.getLogger(ReceiverPane.class);

    private final XrplPriceOracleEditor xrplPriceOracleEditor = new XrplPriceOracleEditor();
    private final XrplPriceOracleConfig xrplPriceOracleConfig = new XrplPriceOracleConfig();
    private final SpringLayout contentLayout;
    private final JComboBox<DateFormat> cboBookingFormat;
    private final JComboBox<DateFormat> cboValutaFormat;

    public ReceiverPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        {
            final var topOffset = 5;
            var top = topOffset;
            {
                addRowLabel(top, "XRPL price oracle:");
                addRowContent(top, xrplPriceOracleEditor);
                top += 140;
            }
            {
                addRowLabel(top, "Booking date format:");
                cboBookingFormat = createCboDateFormat();
                addRowContent(top, cboBookingFormat);
                top += 30;
            }
            {
                addRowLabel(top, "Valuta date format:");
                cboValutaFormat = createCboDateFormat();
                addRowContent(top, cboValutaFormat);
                top += 30;
            }
        }
    }

    private void addRowContent(int top, Component component) {
        final int paddingWest = 120;
        contentLayout.putConstraint(SpringLayout.WEST, component, paddingWest, SpringLayout.WEST, this);
        contentLayout.putConstraint(SpringLayout.NORTH, component, top, SpringLayout.NORTH, this);
        add(component);
    }

    private void addRowLabel(int top, String text) {
        var lbl = new JLabel(text);
        contentLayout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, this);
        contentLayout.putConstraint(SpringLayout.NORTH, lbl, top, SpringLayout.NORTH, this);
        lbl.setOpaque(true);
        add(lbl);
    }

    private JComboBox<DateFormat> createCboDateFormat() {
        var cbo = new JComboBox<DateFormat>();
        cbo.addItem(DateFormat.DateTime);
        cbo.addItem(DateFormat.Date);
        return cbo;
    }

    public void save() throws Exception {
        xrplPriceOracleEditor.apply();
        xrplPriceOracleConfig.set(xrplPriceOracleEditor.issuedCurrencies());

        try (var repo = new ConfigRepo()) {
            xrplPriceOracleConfig.save(repo);
            repo.setBookingDateFormat((DateFormat) cboBookingFormat.getSelectedItem());
            repo.setValutaDateFormat((DateFormat) cboValutaFormat.getSelectedItem());

            repo.commit();
        }
    }

    public void load() {
        try (var repo = new ConfigRepo()) {
            xrplPriceOracleConfig.load(repo);
            xrplPriceOracleEditor.load(Arrays.asList(xrplPriceOracleConfig.issuedCurrencies()));

            cboBookingFormat.setSelectedItem(repo.getBookingDateFormat());
            cboValutaFormat.setSelectedItem(repo.getValutaDateFormat());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
