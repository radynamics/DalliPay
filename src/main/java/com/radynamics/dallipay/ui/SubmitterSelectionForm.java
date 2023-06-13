package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

public class SubmitterSelectionForm extends JDialog {
    private final Hashtable<JComponent, TransactionSubmitter> mapping = new Hashtable<>();
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
        var contentLayout = new WrapLayout();
        contentLayout.setAlignment(FlowLayout.LEADING);
        pnlContent.setLayout(contentLayout);
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
            sorted.sort((o1, o2) -> Integer.compare(o2.getInfo().getOrder(), o1.getInfo().getOrder()));
            for (var s : sorted) {
                create(s, selected != null ? selected : sorted.get(0));
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
        if (info.isNotRecommended()) {
            title = String.format("%s (" + res.getString("notRecommended") + ")", title);
        }

        var size = new Dimension(150, 100);
        var borderOffset = 50;
        var cmd = new JToggleButton("<html><center style='width: %spx'>%s</center></html>".formatted(size.width - borderOffset, title));
        cmd.setIcon(info.getIcon());
        cmd.setToolTipText(createToolTipText(submitter));
        cmd.putClientProperty("JButton.buttonType", "toolBarButton");
        // Text below image
        cmd.setVerticalTextPosition(JButton.BOTTOM);
        cmd.setHorizontalTextPosition(JButton.CENTER);
        cmd.setPreferredSize(size);
        cmd.setMinimumSize(size);
        cmd.setMaximumSize(size);
        pnlContent.add(cmd);

        cmd.setSelected(selected != null && info.getTitle().equals(selected.getInfo().getTitle()));

        mapping.put(cmd, submitter);
        buttonGroup.add(cmd);
    }

    private String createToolTipText(TransactionSubmitter submitter) {
        var sb = new StringBuilder();
        sb.append("<html><p width=500>");
        var info = submitter.getInfo();
        sb.append(info.getDescription());

        sb.append("<br>");
        sb.append("<br>");
        sb.append(res.getString("supports"));
        sb.append("<br>");
        sb.append(String.format("- [%s] %s<br>", toYesNo(submitter.supportIssuedTokens()), res.getString("issuedTokens")));
        sb.append(String.format("- [%s] %s<br>", toYesNo(submitter.supportsPathFinding()), res.getString("pathfinding")));

        if (info.getDetailUri() != null) {
            sb.append("<br>");
            sb.append(info.getDetailUri());
        }

        sb.append("</p></html>");
        return sb.toString();
    }

    private String toYesNo(boolean value) {
        return value ? res.getString("yes") : res.getString("no");
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
            var obj = (JToggleButton) it.next();
            if (obj.isSelected()) {
                return mapping.get(obj);
            }
        }
        return null;
    }
}
