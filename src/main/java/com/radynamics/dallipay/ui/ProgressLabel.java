package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.ui.paymentTable.Progress;

import javax.swing.*;

public class ProgressLabel extends JLabel {
    public void showLoading() {
        setText("Loading, please wait...");
    }

    public void hideLoading() {
        setText("");
    }

    public void update(Progress progress) {
        if (progress.isFinished()) {
            hideLoading();
        } else {
            setText(String.format("Loaded %s / %s...", progress.getCount(), progress.getTotal()));
        }
    }
}
