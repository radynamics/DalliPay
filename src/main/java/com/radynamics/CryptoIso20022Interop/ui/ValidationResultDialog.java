package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;

import javax.swing.*;
import java.awt.*;

public class ValidationResultDialog {
    public static void show(Component parentComponent, ValidationResult[] validations) {
        var sb = new StringBuilder();
        for (var vr : validations) {
            sb.append(String.format("- [%s] %s\n", vr.getStatus().name(), vr.getMessage()));
        }

        JOptionPane.showMessageDialog(parentComponent, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
