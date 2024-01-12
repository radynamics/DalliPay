package com.radynamics.dallipay.ui;

import javax.swing.*;
import java.awt.*;

public class GeneralDialog extends JDialog {
    private final JButton cmdOk;
    private final JButton cmdCancel;
    private boolean accepted;

    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    public GeneralDialog(Frame owner, String title, GeneralDialogContent content) {
        super(owner, title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(new FormActionListener() {
            @Override
            public void onAccept() {
                if (!content.validateInput()) {
                    return;
                }
                accepted(true);
            }

            @Override
            public void onCancel() {
            }
        });

        var l = new SpringLayout();
        setLayout(l);

        var view = content.view();
        add(view);
        view.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        l.putConstraint(SpringLayout.WEST, view, 0, SpringLayout.WEST, getContentPane());
        l.putConstraint(SpringLayout.NORTH, view, 0, SpringLayout.NORTH, getContentPane());
        l.putConstraint(SpringLayout.EAST, view, 0, SpringLayout.EAST, getContentPane());
        l.putConstraint(SpringLayout.SOUTH, view, -60, SpringLayout.SOUTH, getContentPane());

        {
            var pnl = new JPanel();
            add(pnl);
            l.putConstraint(SpringLayout.EAST, pnl, -5, SpringLayout.EAST, getContentPane());
            l.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, getContentPane());
            {
                cmdOk = new JButton("OK");
                cmdOk.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmdOk);
            }
            {
                cmdCancel = new JButton("Cancel");
                cmdCancel.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmdCancel);
            }
        }
        largeOkCancel();
    }

    public void smallOkCancel() {
        okCancelSize(new Dimension(100, 25));
    }

    public void largeOkCancel() {
        okCancelSize(new Dimension(150, 35));
    }

    private void okCancelSize(Dimension d) {
        cmdOk.setPreferredSize(d);
        cmdCancel.setPreferredSize(d);
    }

    public boolean accepted() {
        return accepted;
    }

    private void accepted(boolean accepted) {
        this.accepted = accepted;
    }
}
