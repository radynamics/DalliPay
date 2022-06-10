package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResult;
import com.radynamics.CryptoIso20022Interop.cryptoledger.transaction.ValidationResultUtils;

import javax.swing.*;
import java.awt.*;

public class ValidationResultDialog {
    public static void show(Component parentComponent, ValidationResult[] validations) {
        JOptionPane.showMessageDialog(parentComponent, toText(validations), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String toText(ValidationResult[] validations) {
        ValidationResultUtils.sortDescending(validations);

        var sb = new StringBuilder();
        for (var vr : validations) {
            sb.append(String.format("- [%s] %s\n", vr.getStatus().name(), vr.getMessage()));
        }
        return sb.toString();
    }

    /**
     * Shows warning and errors. Returns true if there were no errors or user accepts warnings.
     */
    public static boolean showErrorAndWarnings(Component parentComponent, ValidationResult[] validations) {
        if (ValidationResultUtils.fromError(validations).length > 0) {
            show(parentComponent, validations);
            return false;
        }

        var validationsToShow = ValidationResultUtils.fromWarning(validations);
        if (validationsToShow.length == 0) {
            return true;
        }

        var sb = new StringBuilder();
        sb.append("Do you want to continue?\n");
        sb.append(toText(validationsToShow));

        return 0 == JOptionPane.showConfirmDialog(parentComponent, sb.toString(), "Warnings", JOptionPane.YES_NO_CANCEL_OPTION);
    }
}
