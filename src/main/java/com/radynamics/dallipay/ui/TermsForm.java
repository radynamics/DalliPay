package com.radynamics.dallipay.ui;

import javax.swing.*;
import java.util.ResourceBundle;

public class TermsForm {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public boolean show() {
        var textArea = new JTextArea(res.getString("terms"));
        textArea.setColumns(60);
        textArea.setRows(25);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);

        var frm = Utils.createDummyForm();

        Object[] options = {res.getString("accept"), res.getString("decline"), res.getString("cancel")};
        var result = JOptionPane.showOptionDialog(null, new JScrollPane(textArea), res.getString("title"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
        frm.dispose();
        return JOptionPane.YES_OPTION == result;
    }
}
