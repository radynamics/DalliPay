package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Transaction;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.BitcoinCoreRpcClientExt;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.MultiWalletJsonRpcApi;
import com.radynamics.dallipay.cryptoledger.memo.PayloadConverter;
import com.radynamics.dallipay.cryptoledger.signing.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BitcoinCoreRpcSubmitter implements TransactionSubmitter {
    private final static Logger log = LogManager.getLogger(BitcoinCoreRpcSubmitter.class);
    private final Ledger ledger;
    private final PrivateKeyProvider privateKeyProvider;
    private final MultiWalletJsonRpcApi openedWallets;
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public final static String Id = "bitcoinCoreRpcSubmitter";

    public BitcoinCoreRpcSubmitter(Ledger ledger, PrivateKeyProvider privateKeyProvider, MultiWalletJsonRpcApi openedWallets) {
        this.ledger = ledger;
        this.privateKeyProvider = privateKeyProvider;
        this.openedWallets = openedWallets;

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("bitcoinCoreRpcSubmitter.title"));
        info.setDescription(res.getString("bitcoinCoreRpcSubmitter.desc"));
        info.setDetailUri(URI.create("https://bitcoin.org/en/wallets/desktop/windows/bitcoincore"));
        info.setIcon(new FlatSVGIcon("img/bitcoincore.svg", 58, 58));
        info.setNotRecommended(true);
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public Ledger getLedger() {
        return ledger;
    }

    @Override
    public void submit(com.radynamics.dallipay.cryptoledger.Transaction[] transactions) {
        for (var trx : transactions) {
            var t = (com.radynamics.dallipay.cryptoledger.generic.Transaction) trx;

            var client = openedWallets.client(t.getSenderWallet()).orElseThrow();
            // Necessary if wallet is encrypted.
            client.walletPassPhrase(privateKeyProvider.get(t.getSenderWallet().getPublicKey()), Duration.ofSeconds(5).toMillis());

            try {
                submit(client, t);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                t.refreshTransmission(e);
                raiseFailure(t);
            }
        }
    }

    private void submit(BitcoinJSONRPCClient client, com.radynamics.dallipay.cryptoledger.generic.Transaction t) throws SigningException {
        var amount = BigDecimal.valueOf(t.getAmount().getNumber().doubleValue());
        var comment = PayloadConverter.toMemo(t.getStructuredReferences(), t.getMessages());
        var commentBytes = StringUtils.isEmpty(comment) ? null : comment.getBytes(StandardCharsets.UTF_8);

        var outputs = new ArrayList<BitcoindRpcClient.TxOutput>();
        outputs.add(new BitcoindRpcClient.BasicTxOutput(t.getReceiverWallet().getPublicKey(), amount, commentBytes));
        var ext = new BitcoinCoreRpcClientExt(client);
        var walletCreateFundedPsbtResult = ext.walletCreateFundedPsbt(outputs);

        var signed = ext.walletProcessPsbt(walletCreateFundedPsbtResult.psbt());
        if (!signed.complete()) {
            throw new SigningException("walletProcessPsbt failed");
        }
        var finalized = ext.finalizePsbt(signed);
        if (!finalized.complete()) {
            throw new SigningException("finalizePsbt failed");
        }

        var transactionHash = client.sendRawTransaction(finalized.hex());
        if (StringUtils.isEmpty(transactionHash)) {
            t.refreshTransmission(new SigningException("Didn't get a txHash after sending transaction."));
            raiseFailure(t);
            return;
        }

        t.setId(transactionHash);
        t.setBooked(ZonedDateTime.now());

        // Onchain verification using Verifier is skipped as it would take too long.
        t.refreshTransmission();
        raiseSuccess(t);
    }

    @Override
    public PrivateKeyProvider getPrivateKeyProvider() {
        return privateKeyProvider;
    }

    @Override
    public TransactionSubmitterInfo getInfo() {
        return info;
    }

    @Override
    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    @Override
    public boolean supportIssuedTokens() {
        return false;
    }

    @Override
    public boolean supportsPathFinding() {
        return false;
    }

    @Override
    public void deleteSettings() {
    }

    private void raiseSuccess(Transaction t) {
        for (var l : stateListener) {
            l.onSuccess(t);
        }
    }

    private void raiseFailure(Transaction t) {
        for (var l : stateListener) {
            l.onFailure(t);
        }
    }
}
