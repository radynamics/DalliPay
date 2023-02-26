package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.ui.paymentTable.Progress;

import javax.swing.*;
import java.util.ResourceBundle;

public class ProgressLabel extends JLabel {
    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public void showLoading() {
        setText(res.getString("loadingPleaseWait"));
    }

    public void hideLoading() {
        setText("");
    }

    public void update(Progress progress) {
        if (progress.isFinished()) {
            hideLoading();
        } else {
            setText(String.format(res.getString("loadingX"), progress.getCount(), progress.getTotal()));
        }
    }
}
