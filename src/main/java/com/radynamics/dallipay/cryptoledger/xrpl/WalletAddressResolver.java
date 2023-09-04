package com.radynamics.dallipay.cryptoledger.xrpl;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.Cache;
import com.radynamics.dallipay.cryptoledger.generic.WalletAddressInfo;
import com.radynamics.dallipay.cryptoledger.xrpl.paystring.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.ClassicAddress;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.time.Duration;

public class WalletAddressResolver implements com.radynamics.dallipay.cryptoledger.generic.WalletAddressResolver {
    private final static Logger log = LogManager.getLogger(WalletAddressResolver.class);
    private final Ledger ledger;
    private final Cache<ResolverResult> payStringCache;

    public WalletAddressResolver(Ledger ledger) {
        if (ledger == null) throw new IllegalArgumentException("Parameter 'ledger' cannot be null");
        this.ledger = ledger;
        payStringCache = new Cache<>(ledger.getNetwork().getUrl().toString(), Duration.ofMinutes(5));
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

        if (PayString.matches(value)) {
            return createByPayString(PayString.create(value));
        }

        return null;
    }

    private static boolean isValidXAddress(String value) {
        return AddressCodec.getInstance().isValidXAddress(XAddress.of(value));
    }

    private static ClassicAddress toClassicAddress(String value) {
        return AddressCodec.getInstance().xAddressToClassicAddress(XAddress.of(value));
    }

    private synchronized WalletAddressInfo createByPayString(PayString payString) {
        payStringCache.evictOutdated();
        var key = new PayStringKey(payString);
        var data = payStringCache.get(key);
        // Contained without data means "doesn't exist" (wasn't found previously)
        if (data != null || payStringCache.isPresent(key)) {
            return data == null ? null : data.createXrplWalletAddressInfo(ledger);
        }
        try {
            data = new Resolver().discover(payString);
            payStringCache.add(key, data);
            return data == null ? null : data.createXrplWalletAddressInfo(ledger);
        } catch (PayStringException e) {
            payStringCache.add(key, null);
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
