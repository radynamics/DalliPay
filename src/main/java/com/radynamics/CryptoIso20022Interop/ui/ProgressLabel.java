package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.ui.paymentTable.Progress;

import javax.swing.*;

public class ProgressLabel extends JLabel {
    public void showLoading() {
        setText("Loading, please wait...");
    }

    public void update(Progress progress) {
        if (progress.isFinished()) {
            setText("");
        } else {
            setText(String.format("Loaded %s / %s...", progress.getCount(), progress.getTotal()));
        }
    }
}
