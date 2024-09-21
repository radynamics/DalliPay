package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.WalletSetupProcess;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.ui.WaitingForm;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

        var dlg = WaitingForm.create(null, res.getString("walletImportInProgress"));
        Future<WalletImportTaskResult> future = Executors.newCachedThreadPool().submit(() -> {
            try {
                if (frm.importWalletAddress() && !StringUtils.isEmpty(frm.walletAddress())) {
                    var walletName = frm.walletName().orElse(frm.walletAddress());
                    // Only import if not yet exists (prevent BitcoinRPCException "Database already exists").
                    if (ledger.walletImported(walletName)) {
                        return WalletImportTaskResult.ALREADY_IMPORTED;
                    }
                    ledger.importWallet(walletName, frm.historicTransactionSince(), ledger.createWallet(frm.walletAddress()));
                    return WalletImportTaskResult.IMPORTED;
                } else if (frm.importDevice() && frm.device() != null) {
                    var walletName = frm.walletName().orElse(frm.device().type());
                    // Only import if not yet exists (prevent BitcoinRPCException "Database already exists").
                    if (ledger.walletImported(walletName)) {
                        return WalletImportTaskResult.ALREADY_IMPORTED;
                    }
                    ledger.importWallet(walletName, frm.historicTransactionSince(), frm.device());
                    return WalletImportTaskResult.IMPORTED;
                } else {
                    return WalletImportTaskResult.NOT_IMPORTED;
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            } finally {
                dlg.setVisible(false);
            }
        });

        try {
            dlg.setVisible(true);
            var result = future.get();
            switch (result) {
                case IMPORTED ->
                        JOptionPane.showMessageDialog(parentComponent, res.getString("successfullyAdded"), res.getString("successfullyAddedTitle"), JOptionPane.INFORMATION_MESSAGE);
                case NOT_IMPORTED -> {
                }
                case ALREADY_IMPORTED ->
                        JOptionPane.showMessageDialog(parentComponent, res.getString("alreadyImported"), res.getString("alreadyImportedTitle"), JOptionPane.INFORMATION_MESSAGE);
                default -> throw new IllegalStateException("Unexpected value: " + result);
            }
        } catch (InterruptedException | ExecutionException e) {
            var errorJson = BitcoinCoreRpcClientExt.errorJson(e.getCause());
            if (errorJson.isPresent()) {
                final var ERR_RESCAN_ABORTED_BY_USER = -1;
                if (errorJson.get().getInt("code") == ERR_RESCAN_ABORTED_BY_USER) {
                    return;
                }
            }
            throw new RuntimeException(e);
        }
    }

    private enum WalletImportTaskResult {
        IMPORTED,
        NOT_IMPORTED,
        ALREADY_IMPORTED,
    }
}
