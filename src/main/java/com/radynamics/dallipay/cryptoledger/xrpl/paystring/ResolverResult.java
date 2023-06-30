package com.radynamics.dallipay.cryptoledger.xrpl.paystring;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.Ledger;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.util.Objects;

public class ResolverResult {
    private String payId;
    private Address[] addresses = new Address[0];

    public ResolverResult(String payId) {
        this.payId = payId;
    }

    public WalletAddressInfo createXrplWalletAddressInfo(Ledger ledger) {
        if (addresses.length == 0) {
            return null;
        }

        for (var a : addresses) {
            if ("xrpl".equalsIgnoreCase(a.getPaymentNetwork())) {
                var addressText = a.getDetails().getAddress();
                var addressCodec = AddressCodec.getInstance();
                if (addressCodec.isValidXAddress(XAddress.of(addressText))) {
                    var classicAddress = addressCodec.xAddressToClassicAddress(XAddress.of(addressText));
                    var addressInfo = new WalletAddressInfo(ledger.createWallet(classicAddress.classicAddress().value(), null));
                    addressInfo.setDestinationTag(Objects.equals(classicAddress.tag(), UnsignedInteger.ZERO) ? null : classicAddress.tag().toString());
                    return addressInfo;
                }
                var addressInfo = new WalletAddressInfo(ledger.createWallet(addressText, null));
                addressInfo.setDestinationTag(a.getDetails().getTag());
            }
        }
        return null;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return "payId: %s, addresses: %s".formatted(payId, addresses.length);
    }
}
