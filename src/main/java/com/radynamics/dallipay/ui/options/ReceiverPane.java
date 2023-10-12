package com.radynamics.dallipay.ui.options;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.xrpl.XrplPriceOracleConfig;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.iso20022.camt054.DateFormat;
import com.radynamics.dallipay.ui.options.XrplPriceOracleEdit.XrplPriceOracleEditor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ReceiverPane extends JPanel {
    final static Logger log = LogManager.getLogger(ReceiverPane.class);

    private final XrplPriceOracleEditor xrplPriceOracleEditor = new XrplPriceOracleEditor();
    private XrplPriceOracleConfig xrplPriceOracleConfig;
    private final SpringLayout contentLayout;
    private final JComboBox<DateFormat> cboBookingFormat;
    private final JComboBox<DateFormat> cboValutaFormat;
    private final JTextField txtCreditorReference;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public ReceiverPane() {
        setPreferredSize(new Dimension(1000, 400));
        contentLayout = new SpringLayout();
        setLayout(contentLayout);

        var builder = new RowContentBuilder(this, contentLayout);
        {
            final var topOffset = 5;
            var top = topOffset;
            {
                builder.addRowLabel(top, res.getString("xrplPriceOracle"));
                builder.addRowContent(top, xrplPriceOracleEditor);
                top += 140;
            }
            {
                var lbl = builder.addRowLabel(top, res.getString("export"));
                lbl.putClientProperty("FlatLaf.styleClass", "h3");
                top += 40;
            }
            {
                builder.addRowLabel(top, res.getString("bookingFormat"));
                cboBookingFormat = createCboDateFormat();
                builder.addRowContent(top, cboBookingFormat);
                top += 30;
            }
            {
                builder.addRowLabel(top, res.getString("valutaFormat"));
                cboValutaFormat = createCboDateFormat();
                builder.addRowContent(top, cboValutaFormat);
                top += 30;
            }
            {
                builder.addRowLabel(top, res.getString("refNoIfEmpty"));
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

    public void init(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        xrplPriceOracleEditor.init(ledger);
        xrplPriceOracleConfig = new XrplPriceOracleConfig(ledger.getId());
    }
}