package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.ui.paymentTable.Progress;

import javax.swing.*;
import java.util.ResourceBundle;

public class ProgressLabel extends JLabel {
    private boolean isLoading;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public void showLoading() {
        isLoading = true;
        setText(res.getString("loadingPleaseWait"));
    }

    public void hideLoading() {
        isLoading = false;
        setText("");
    }

    public void update(Progress progress) {
        isLoading = !progress.isFinished();
        if (progress.isFinished()) {
            hideLoading();
        } else {
            setText(String.format(res.getString("loadingX"), progress.getCount(), progress.getTotal()));
        }
    }

    public boolean isLoading() {
        return isLoading;
    }
}
