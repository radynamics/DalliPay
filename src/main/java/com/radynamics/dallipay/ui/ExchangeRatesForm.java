package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.exchange.ExchangeRate;
import com.radynamics.dallipay.exchange.ExchangeRateProvider;
import com.radynamics.dallipay.exchange.ExchangeRateProviderFactory;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ExchangeRatesForm extends JDialog {
    private final Ledger ledger;
    private ExchangeRateProvider selectedExchange;
    private final ExchangeRate[] rates;
    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private final ArrayList<JTextField> txts = new ArrayList<>();
    private boolean accepted;
    private ZonedDateTime pointInTime;
    private JComboBox<ExchangeRateProvider> cboExchange;
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public ExchangeRatesForm(Ledger ledger, ExchangeRateProvider selectedExchange, ExchangeRate[] rates, ZonedDateTime pointInTime) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        if (selectedExchange == null) throw new IllegalArgumentException("Parameter 'selectedExchange' cannot be null");
        if (rates == null) throw new IllegalArgumentException("Parameter 'rates' cannot be null");
        if (pointInTime == null) throw new IllegalArgumentException("Parameter 'pointInTime' cannot be null");
        this.ledger = ledger;
        this.selectedExchange = selectedExchange;
        this.rates = rates;
        this.pointInTime = pointInTime;

        setupUI();
    }

    private void setupUI() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(this::acceptDialog);

        var pnlMain = new JPanel();
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlMain);

        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        var innerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        JPanel panel0 = new JPanel();
        panel0.setBorder(innerBorder);
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
        var panel1 = new JPanel();
        panel1.setBorder(innerBorder);
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel1Layout = new SpringLayout();
        pnlContent = new JPanel();
        pnlContent.setLayout(panel1Layout);
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlContent.setPreferredSize(new Dimension(100, 100));
        var sp = new JScrollPane(pnlContent);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel1.add(sp);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));

        {
            var pnl = new JPanel();
            pnl.setLayout(new BorderLayout());
            panel0.add(pnl);
            {
                var lbl = new JLabel();
                lbl.setText(getTitle());
                lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.NORTH);
            }
            {
                var pnlLine = new JPanel();
                pnlLine.setLayout(new BoxLayout(pnlLine, BoxLayout.X_AXIS));
                pnl.add(pnlLine, BorderLayout.WEST);
                {
                    var lbl = Utils.createLinkLabel(pnlMain, res.getString("refresh"));
                    lbl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                refreshRates();
                            }
                        }
                    });
                    pnlLine.add(lbl);
                }
                {
                    var lbl = new JLabel(" " + res.getString("fxRateFrom") + " ");
                    pnlLine.add(lbl);
                }
                {
                    cboExchange = new JComboBox<>();
                    setAllowChangeExchange(false);
                    cboExchange.setRenderer(new ExchangeRateProviderCellRenderer());
                    var selectedFound = false;
                    for (var exchange : ExchangeRateProviderFactory.allExchanges(ledger)) {
                        cboExchange.addItem(exchange);
                        if (exchange.getId().equals(selectedExchange.getId())) {
                            cboExchange.setSelectedItem(exchange);
                            selectedFound = true;
                        }
                    }
                    // Historic exchange rate providers aren't added.
                    if (!selectedFound) {
                        cboExchange.addItem(selectedExchange);
                        cboExchange.setSelectedItem(selectedExchange);
                    }
                    cboExchange.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            onSelectedExchangeChanged((ExchangeRateProvider) e.getItem());
                        }
                    });
                    pnlLine.add(cboExchange);
                }
                {
                    var lbl = new JLabel(" " + res.getString("ifPossible"));
                    pnlLine.add(lbl);
                }
            }
        }

        {
            int row = 0;
            for (var rate : rates) {
                var created = createRow(row++, rate);
                anchorComponentTopLeft = anchorComponentTopLeft == null ? created : anchorComponentTopLeft;
            }
        }
        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                var cmd = new JButton("OK");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmd);
            }
            {
                var cmd = new JButton(res.getString("cancel"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmd);
            }
        }
    }

    private void acceptDialog() {
        apply();
        setDialogAccepted(true);
    }

    private void onSelectedExchangeChanged(ExchangeRateProvider exchange) {
        selectedExchange = exchange;
        refreshRates();
    }

    private Component createRow(int row, ExchangeRate rate) {
        var lbl = new JLabel(String.format("%s:", rate.getPair().getDisplayText()));
        panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
        panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        lbl.setOpaque(true);
        pnlContent.add(lbl);

        var txt = new JTextField(String.valueOf(rate.getRate()));
        txt.setPreferredSize(new Dimension(160, 24));
        txt.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                var isValid = false;
                try {
                    getTxtValue(((JTextField) input));
                    isValid = true;
                } catch (NumberFormatException ignored) {
                }
                if (isValid) {
                    txt.putClientProperty("JComponent.outline", null);
                    return true;
                } else {
                    txt.putClientProperty("JComponent.outline", "error");
                    return false;
                }
            }
        });
        panel1Layout.putConstraint(SpringLayout.WEST, txt, 50, SpringLayout.EAST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
        panel1Layout.putConstraint(SpringLayout.NORTH, txt, getNorthPad(row), SpringLayout.NORTH, pnlContent);
        pnlContent.add(txt);
        txts.add(txt);

        return lbl;
    }

    private void refreshRates() {
        selectedExchange.load();
        for (var i = 0; i < rates.length; i++) {
            var pair = rates[i].getPair();
            var exchangeRates = new ExchangeRate[0];
            if (selectedExchange.supportsRateAt()) {
                var rate = selectedExchange.rateAt(pair, pointInTime);
                if (rate != null) {
                    exchangeRates = new ExchangeRate[]{rate};
                }
            } else {
                exchangeRates = selectedExchange.latestRates();
            }
            var r = ExchangeRate.getOrNull(exchangeRates, pair);
            if (r != null) {
                txts.get(i).setText(String.valueOf(r.getRate()));
            }
        }
    }

    private void apply() {
        for (var i = 0; i < rates.length; i++) {
            rates[i].setRate(getTxtValue(txts.get(i)));
            rates[i].setPointInTime(ZonedDateTime.now());
        }
    }

    private static Double getTxtValue(JTextField txt) {
        return StringUtils.isAllEmpty(txt.getText()) ? ExchangeRate.UndefinedRate : Double.parseDouble(txt.getText());
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }

    public ExchangeRateProvider getSelectedExchange() {
        return selectedExchange;
    }

    public void setAllowChangeExchange(boolean allowed) {
        cboExchange.setEnabled(allowed);
    }
}
