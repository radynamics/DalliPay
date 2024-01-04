package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.radynamics.dallipay.cryptoledger.bitcoin.hwi.Hwi;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

public class HwiSigning implements SigningMethod {
    private final Hwi hwi = new Hwi();

    @Override
    public WalletProcessPsbtResult signPsbt(BitcoinJSONRPCClient client, WalletCreateFundedPsbtResult funded) throws SigningException {
        return hwi.signPsbt(funded);
    }

    @Override
    public boolean usesWalletPassPhrase() {
        return false;
    }
}
