package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Hwi;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

public class HwiSigning implements SigningMethod {
    private final Hwi hwi = Hwi.get();
    private final boolean supportsPayload;

    public HwiSigning(boolean supportsPayload) {
        this.supportsPayload = supportsPayload;
    }

    @Override
    public WalletProcessPsbtResult signPsbt(BitcoinJSONRPCClient client, WalletCreateFundedPsbtResult funded) throws SigningException {
        hwi.chain(client.getBlockChainInfo().chain());
        return hwi.signPsbt(funded);
    }

    @Override
    public boolean usesWalletPassPhrase() {
        return false;
    }

    @Override
    public boolean supportsPayload() {
        return supportsPayload;
    }
}
