package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.WalletSetupProcess;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class BitcoinCoreWalletImport implements WalletSetupProcess {
    private final Component parentComponent;
    private final Ledger ledger;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public BitcoinCoreWalletImport(Component parentComponent, Ledger ledger) {
        this.parentComponent = parentComponent;
        this.ledger = ledger;
    }

    @Override
    public void start() {
        var frm = new BitcoinCoreWalletImportForm();
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setSize(500, 400);
        frm.setModal(true);
        frm.setLocationRelativeTo(parentComponent);
        frm.setVisible(true);

        if (!frm.isDialogAccepted()) {
            return;
        }

        try {
            if (frm.importWalletAddress() && !StringUtils.isEmpty(frm.walletAddress())) {
                ledger.importWallet(ledger.createWallet(frm.walletAddress()), frm.historicTransactionSince());
            } else if (frm.importDevice() && frm.device() != null) {
                ledger.importWallet(frm.device(), frm.historicTransactionSince());
            } else {
                return;
            }
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        JOptionPane.showMessageDialog(parentComponent, res.getString("successfullyAdded"), res.getString("successfullyAddedTitle"), JOptionPane.INFORMATION_MESSAGE);
    }
}
