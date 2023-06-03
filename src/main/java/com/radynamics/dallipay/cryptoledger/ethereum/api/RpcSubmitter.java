package com.radynamics.dallipay.cryptoledger.ethereum.api;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerException;
import com.radynamics.dallipay.cryptoledger.PaymentUtils;
import com.radynamics.dallipay.cryptoledger.signing.PrivateKeyProvider;
import com.radynamics.dallipay.cryptoledger.signing.TransactionStateListener;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitter;
import com.radynamics.dallipay.cryptoledger.signing.TransactionSubmitterInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RpcSubmitter implements TransactionSubmitter {
    private final static Logger log = LogManager.getLogger(com.radynamics.dallipay.cryptoledger.xrpl.signing.RpcSubmitter.class);
    private final Ledger ledger;
    private final Web3j web3;
    private final PrivateKeyProvider privateKeyProvider;
    private final TransactionSubmitterInfo info;
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.TransactionSubmitter");

    public final static String Id = "rpcSubmitter";

    public RpcSubmitter(Ledger ledger, Web3j web3, PrivateKeyProvider privateKeyProvider) {
        this.ledger = ledger;
        this.web3 = web3;
        this.privateKeyProvider = privateKeyProvider;

        info = new TransactionSubmitterInfo();
        info.setTitle(res.getString("rpc.title"));
        info.setDescription(res.getString("rpc.desc"));
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
        var sendingWallets = PaymentUtils.distinctSendingWallets(transactions);
        // Process by sending wallet to keep sequence number handling simple (prevent terPRE_SEQ).
        for (var sendingWallet : sendingWallets) {
            var trxByWallet = PaymentUtils.fromSender(sendingWallet, transactions);
            for (var trx : trxByWallet) {
                // TODO: don't cast into xrpl.Transaction
                var t = (Transaction) trx;
                try {
                    submit2(t);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    t.refreshTransmission(e);
                    raiseFailure(t);
                }
            }
        }
    }

    private void submit2(Transaction t) throws Exception {
        var nonce = web3.ethGetTransactionCount(t.getSenderWallet().getPublicKey(), DefaultBlockParameterName.LATEST).sendAsync().get();

        final byte GOERLI = 5;
        // TODO hardcoded values
        var gasPrice = Convert.toWei(BigDecimal.valueOf(3), Convert.Unit.GWEI).toBigInteger();
        var gasLimit = BigInteger.valueOf(30000);
        var chainId = ledger.getNetwork().isLivenet() ? ChainId.MAINNET : GOERLI;
        String data = "";
        var signedData = signTransaction(
                nonce.getTransactionCount(),
                gasPrice,
                gasLimit,
                t.getReceiverWallet().getPublicKey(),
                Convert.toWei(BigDecimal.valueOf(t.getAmount().getNumber().doubleValue()), Convert.Unit.ETHER).toBigInteger(),
                data,
                chainId,
                this.privateKeyProvider.get(t.getSenderWallet().getPublicKey()));

        var result = web3.ethSendRawTransaction(signedData).sendAsync().get();
        if (result.hasError()) {
            throw new LedgerException(String.format("Ledger submit failed with result %s.", result.getError().getMessage()));
        }

        t.setId(result.getTransactionHash());
        t.setBooked(ZonedDateTime.now());
        t.refreshTransmission();
        raiseSuccess(t);
    }

    private static String signTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data, byte chainId, String privateKey) {
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        var ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        var credentials = Credentials.create(ecKeyPair);

        byte[] signedMessage;
        var rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        if (chainId > ChainId.NONE) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        return Numeric.toHexString(signedMessage);
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
    public boolean supportsPathFinding() {
        return false;
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
