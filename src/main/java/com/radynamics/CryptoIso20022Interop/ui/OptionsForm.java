package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.db.ConfigRepo;
import com.radynamics.CryptoIso20022Interop.ui.options.GeneralPane;
import com.radynamics.CryptoIso20022Interop.ui.options.ReceiverPane;
import com.radynamics.CryptoIso20022Interop.ui.options.SenderPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OptionsForm extends JPanel implements MainFormPane {
    private final GeneralPane generalPane;
    private final SenderPane senderPane;
    private final ReceiverPane receiverPane;
    private final ArrayList<ChangedListener> listener = new ArrayList<>();

    public OptionsForm(Ledger ledger) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var pnlContent = new JPanel();
        add(pnlContent, BorderLayout.CENTER);
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        {
            var tabbedPane = new JTabbedPane();
            pnlContent.add(tabbedPane);

            generalPane = new GeneralPane();
            tabbedPane.addTab("General", generalPane);
            senderPane = new SenderPane(ledger);
            tabbedPane.addTab("Send", senderPane);
            receiverPane = new ReceiverPane();
            tabbedPane.addTab("Receive", receiverPane);
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
        JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
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
