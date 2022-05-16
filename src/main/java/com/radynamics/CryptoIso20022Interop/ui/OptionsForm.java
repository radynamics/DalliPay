package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.ui.options.GeneralPane;
import com.radynamics.CryptoIso20022Interop.ui.options.ReceiverPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OptionsForm extends JPanel implements MainFormPane {
    private final GeneralPane generalPane;
    private final ReceiverPane receiverPane;
    private final ArrayList<ChangedListener> listener = new ArrayList<>();

    public OptionsForm(Window owner) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        var pnlContent = new JPanel();
        add(pnlContent, BorderLayout.CENTER);
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        {
            var tabbedPane = new JTabbedPane();
            pnlContent.add(tabbedPane);

            generalPane = new GeneralPane(owner);
            tabbedPane.addTab("General", generalPane);
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
        try {
            generalPane.save();
            receiverPane.save();

            raiseChanged();
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            ExceptionDialog.show(this, e);
        }
    }

    public void load() {
        generalPane.load();
        receiverPane.load();
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
