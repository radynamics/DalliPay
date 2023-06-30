package com.radynamics.dallipay.cryptoledger.xrpl;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressInfo;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.ClassicAddress;
import org.xrpl.xrpl4j.model.transactions.XAddress;

public class WalletAddressResolver implements com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver {
    private final Ledger ledger;

    public WalletAddressResolver(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
    }

    @Override
    public WalletAddressInfo resolve(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }

        if (ledger.isValidPublicKey(value)) {
            return new WalletAddressInfo(ledger.createWallet(value, null));
        }

        if (isValidXAddress(value)) {
            var classicAddress = toClassicAddress(value);
            var addressInfo = new WalletAddressInfo(ledger.createWallet(classicAddress.classicAddress().value(), null));
            addressInfo.setDestinationTag(classicAddress.tag() == UnsignedInteger.ZERO ? null : classicAddress.tag().toString());
            return addressInfo;
        }

        return null;
    }

    private static boolean isValidXAddress(String value) {
        return AddressCodec.getInstance().isValidXAddress(XAddress.of(value));
    }

    private static ClassicAddress toClassicAddress(String value) {
        return AddressCodec.getInstance().xAddressToClassicAddress(XAddress.of(value));
    }
}
