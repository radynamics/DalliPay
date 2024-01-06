package com.radynamics.dallipay.cryptoledger.bitcoin.api;

import com.radynamics.dallipay.cryptoledger.WalletSetupProcess;
import com.radynamics.dallipay.cryptoledger.bitcoin.Ledger;
import com.radynamics.dallipay.ui.WaitingForm;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;

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
        Future<Boolean> future = Executors.newCachedThreadPool().submit(() -> {
            try {
                if (frm.importWalletAddress() && !StringUtils.isEmpty(frm.walletAddress())) {
                    ledger.importWallet(ledger.createWallet(frm.walletAddress()), frm.historicTransactionSince());
                    return true;
                } else if (frm.importDevice() && frm.device() != null) {
                    ledger.importWallet(frm.device(), frm.historicTransactionSince());
                    return true;
                } else {
                    return false;
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            } finally {
                dlg.setVisible(false);
            }
        });

        try {
            dlg.setVisible(true);
            if (!future.get()) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof BitcoinRPCException) {
                final var rpcEx = (BitcoinRPCException) e.getCause();
                final var resultJson = new JSONObject(rpcEx.getResponse());
                final var ERR_RESCAN_ABORTED_BY_USER = -1;
                if (resultJson.getJSONObject("error").getInt("code") == ERR_RESCAN_ABORTED_BY_USER) {
                    return;
                }
            }
            throw new RuntimeException(e);
        }

        JOptionPane.showMessageDialog(parentComponent, res.getString("successfullyAdded"), res.getString("successfullyAddedTitle"), JOptionPane.INFORMATION_MESSAGE);
    }
}
