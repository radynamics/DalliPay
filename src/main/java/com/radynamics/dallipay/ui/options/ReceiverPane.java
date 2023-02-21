package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.iso20022.camt054.DateFormat;
import com.radynamics.dallipay.ui.options.XrplPriceOracleEdit.XrplPriceOracleEditor;
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
    private final JTextField txtCreditorReference;

    public ReceiverPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, "XRPL price oracle:");
                builder.addRowContent(top, xrplPriceOracleEditor);
                top += 140;
            }
            {
                var lbl = builder.addRowLabel(top, "Export");
                lbl.putClientProperty("FlatLaf.styleClass", "h3");
                top += 40;
            }
            {
                builder.addRowLabel(top, "Booking date format:");
                cboBookingFormat = createCboDateFormat();
                builder.addRowContent(top, cboBookingFormat);
                top += 30;
            }
            {
                builder.addRowLabel(top, "Valuta date format:");
                cboValutaFormat = createCboDateFormat();
                builder.addRowContent(top, cboValutaFormat);
                top += 30;
            }
            {
                builder.addRowLabel(top, "Creditor reference if empty:");
                txtCreditorReference = new JTextField();
                txtCreditorReference.setPreferredSize(new Dimension(160, 24));
                builder.addRowContent(top, txtCreditorReference);
                top += 30;
            }
        }
    }

    private JComboBox<DateFormat> createCboDateFormat() {
        var cbo = new JComboBox<DateFormat>();
        cbo.addItem(DateFormat.DateTime);
        cbo.addItem(DateFormat.Date);
        return cbo;
    }

    public void save(ConfigRepo repo) throws Exception {
        xrplPriceOracleEditor.apply();
        xrplPriceOracleConfig.set(xrplPriceOracleEditor.issuedCurrencies());

        xrplPriceOracleConfig.save(repo);
        repo.setBookingDateFormat((DateFormat) cboBookingFormat.getSelectedItem());
        repo.setValutaDateFormat((DateFormat) cboValutaFormat.getSelectedItem());
        repo.setCreditorReferenceIfMissing(txtCreditorReference.getText());
    }

    public void load(ConfigRepo repo) throws Exception {
        xrplPriceOracleConfig.load(repo);
        xrplPriceOracleEditor.load(Arrays.asList(xrplPriceOracleConfig.issuedCurrencies()));

        cboBookingFormat.setSelectedItem(repo.getBookingDateFormat());
        cboValutaFormat.setSelectedItem(repo.getValutaDateFormat());
        var referenceNo = repo.getCreditorReferenceIfMissing();
        txtCreditorReference.setText(referenceNo == null ? "" : referenceNo.getUnformatted());
    }
}
