package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import com.radynamics.dallipay.cryptoledger.signing.SigningException;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

public interface SigningMethod {
    WalletProcessPsbtResult signPsbt(BitcoinJSONRPCClient client, WalletCreateFundedPsbtResult funded) throws SigningException;

    boolean usesWalletPassPhrase();
}
