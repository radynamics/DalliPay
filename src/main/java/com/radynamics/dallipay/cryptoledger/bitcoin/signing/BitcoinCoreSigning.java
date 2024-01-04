package com.radynamics.dallipay.cryptoledger.bitcoin.signing;

import com.radynamics.dallipay.cryptoledger.bitcoin.api.BitcoinCoreRpcClientExt;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletCreateFundedPsbtResult;
import com.radynamics.dallipay.cryptoledger.bitcoin.api.WalletProcessPsbtResult;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

public class BitcoinCoreSigning implements SigningMethod {
    @Override
    public WalletProcessPsbtResult signPsbt(BitcoinJSONRPCClient client, WalletCreateFundedPsbtResult funded) {
        var ext = new BitcoinCoreRpcClientExt(client);
        return ext.walletProcessPsbt(funded.psbt());
    }

    public boolean usesWalletPassPhrase() {
        return true;
    }
}
