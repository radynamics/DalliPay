package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

public class SubmitterSelectionForm extends JDialog {
    private final Hashtable<JRadioButton, TransactionSubmitter> mapping = new Hashtable<>();
    private boolean accepted;

    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JPanel pnlContent;
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public SubmitterSelectionForm(TransactionSubmitter[] submitters, TransactionSubmitter selected) {
        if (submitters == null) throw new IllegalArgumentException("submitter 'selectedExchange' cannot be null");

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
        pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

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
                    var lbl = new JLabel(res.getString("desc"));
                    pnlLine.add(lbl);
                }
            }
        }

        {
            var sorted = new ArrayList<>(List.of(submitters));
            sorted.sort((o1, o2) -> Boolean.compare(o2.getInfo().isRecommended(), o1.getInfo().isRecommended()));
            for (var s : sorted) {
                create(s, selected);
            }
            if (buttonGroup.getButtonCount() > 0 && buttonGroup.getSelection() == null) {
                buttonGroup.setSelected(buttonGroup.getElements().nextElement().getModel(), true);
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

    private void create(TransactionSubmitter submitter, TransactionSubmitter selected) {
        var info = submitter.getInfo();

        var title = info.getTitle();
        if (info.isRecommended()) {
            title = String.format("%s (" + res.getString("recommended") + ")", title);
        }
        var rdo = new JRadioButton(title);

        var itemClickListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                rdo.setSelected(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };

        var pnl = new JPanel();
        var layout = new GridBagLayout();
        pnl.setLayout(layout);
        pnlContent.add(pnl);
        pnlContent.add(Box.createRigidArea(new Dimension(0, 10)));
        pnl.addMouseListener(itemClickListener);

        var c = new GridBagConstraints();
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        {
            c.gridx = 0;
            c.gridy = 0;
            pnl.add(rdo, c);

            mapping.put(rdo, submitter);
            buttonGroup.add(rdo);
            rdo.setSelected(selected == null ? info.isRecommended() : info.getTitle().equals(selected.getInfo().getTitle()));
            rdo.setOpaque(true);
            rdo.putClientProperty("FlatLaf.styleClass", "h3");
        }
        var border = BorderFactory.createEmptyBorder(0, 22, 0, 0);
        {
            if (info.getDetailUri() != null) {
                var lbl = Utils.createLinkLabel(pnl, info.getDetailUri().toString());
                c.gridx = 0;
                c.gridy = 1;
                pnl.add(lbl, c);

                lbl.setBorder(border);
                lbl.setOpaque(true);
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 1) {
                            Utils.openBrowser(pnl, info.getDetailUri());
                        }
                    }
                });
            }
        }
        {
            var lbl = Utils.formatLabel(new JTextArea(info.getDescription()));
            c.gridx = 0;
            c.gridy = 2;
            pnl.add(lbl, c);

            lbl.setColumns(40);
            lbl.setRows(3);
            lbl.setBorder(border);
            lbl.setOpaque(true);
            lbl.addMouseListener(itemClickListener);
        }
    }

    static TransactionSubmitter showDialog(Component parentComponent, Ledger ledger, TransactionSubmitter selected) {
        var submitterFactory = ledger.createTransactionSubmitterFactory();
        var s = selected == null ? submitterFactory.getSuggested(parentComponent) : selected;
        var frm = new SubmitterSelectionForm(submitterFactory.all(parentComponent), s);
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(520, 400);
        frm.setModal(true);
        frm.setLocationRelativeTo(parentComponent);
        frm.setVisible(true);
        if (!frm.isDialogAccepted()) {
            return null;
        }

        return frm.getSelected();
    }

    private void acceptDialog() {
        setDialogAccepted(true);
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }

    public TransactionSubmitter getSelected() {
        var it = buttonGroup.getElements().asIterator();
        while (it.hasNext()) {
            var rdo = (JRadioButton) it.next();
            if (rdo.isSelected()) {
                return mapping.get(rdo);
            }
        }
        return null;
    }
}
