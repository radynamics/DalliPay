package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class WalletInfoFormatter {
    public static void format(JLabel lbl, WalletInfo wi) {
        format(lbl, wi, lbl.getForeground());
    }

    public static void format(JLabel lbl, WalletInfo wi, Color foregroundColor) {
        if (wi == null) {
            lbl.setForeground(foregroundColor);
            lbl.setToolTipText("");
        } else {
            lbl.setForeground(wi.getVerified() ? foregroundColor : Consts.ColorWarning);
            lbl.setToolTipText(wi.getVerified() ? "" : String.format("%s not verified", wi.getText()));
        }
    }

    public static Optional<String> toText(WalletInfo wi) {
        return wi == null ? Optional.empty() : Optional.of(com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoFormatter.format(wi));
    }
}
