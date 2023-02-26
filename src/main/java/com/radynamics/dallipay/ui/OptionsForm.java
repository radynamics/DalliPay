package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.db.ConfigRepo;
import com.radynamics.dallipay.ui.options.GeneralPane;
import com.radynamics.dallipay.ui.options.ReceiverPane;
import com.radynamics.dallipay.ui.options.SenderPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class OptionsForm extends JPanel implements MainFormPane {
    private final GeneralPane generalPane;
    private final SenderPane senderPane;
    private final ReceiverPane receiverPane;
    private final ArrayList<ChangedListener> listener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Options");

    public OptionsForm(Ledger ledger) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var pnlContent = new JPanel();
        add(pnlContent, BorderLayout.CENTER);
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        var border = BorderFactory.createEmptyBorder(10, 0, 0, 0);
        {
            var tabbedPane = new JTabbedPane();
            pnlContent.add(tabbedPane);

            generalPane = new GeneralPane(ledger);
            tabbedPane.addTab(res.getString("general"), generalPane);
            generalPane.setBorder(border);
            senderPane = new SenderPane(ledger);
            tabbedPane.addTab(res.getString("send"), senderPane);
            senderPane.setBorder(border);
            receiverPane = new ReceiverPane();
            tabbedPane.addTab(res.getString("receive"), receiverPane);
            receiverPane.setBorder(border);
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
                var cmd = new JButton(res.getString("save"));
                cmd.setMinimumSize(buttonSize);
                cmd.setPreferredSize(buttonSize);
                cmd.setMaximumSize(buttonSize);
                cmd.addActionListener(e -> save());
                buttonPane.add(cmd);
            }
        }
    }

    private void save() {
        try (var repo = new ConfigRepo()) {
            generalPane.save(repo);
            senderPane.save(repo);
            receiverPane.save(repo);
            repo.commit();
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
            return;
        }

        raiseChanged();
        JOptionPane.showMessageDialog(this, res.getString("saveSuccess"), res.getString("saved"), JOptionPane.INFORMATION_MESSAGE);
    }

    public void load() {
        try (var repo = new ConfigRepo()) {
            generalPane.load(repo);
            senderPane.load(repo);
            receiverPane.load(repo);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    @Override
    public String getTitle() {
        return res.getString("options");
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
