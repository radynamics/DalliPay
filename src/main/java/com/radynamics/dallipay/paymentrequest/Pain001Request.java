package com.radynamics.dallipay.paymentrequest;

import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.NetworkInfo;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.iso20022.AccountFactory;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.pain001.Pain001Reader;
import com.radynamics.dallipay.transformation.AccountMappingSource;
import com.radynamics.dallipay.transformation.MemoryAccountMappingSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Pain001Request {
    private final String xml;
    private String networkId;
    private List<AccountWalletPair> accountWalletPairs;

    public Pain001Request(String xml) {
        this.xml = xml;
    }

    public InputStream xml() {
        return new ByteArrayInputStream(xml.getBytes());
    }

    public String networkId() {
        return this.networkId;
    }

    public void networkId(String networkId) {
        this.networkId = networkId;
    }

    public List<AccountWalletPair> accountWalletPairs() {
        return this.accountWalletPairs;
    }

    public void accountWalletPairs(List<AccountWalletPair> accountWalletPairs) {
        this.accountWalletPairs = accountWalletPairs;
    }

    public NetworkInfo networkInfo() {
        var ledgerId = ledgerId();
        if (ledgerId == null) {
            return null;
        }

        Ledger ledger = LedgerFactory.create(ledgerId);
        NetworkInfo livenet = null;
        NetworkInfo other = null;
        for (var n : ledger.getDefaultNetworkInfo()) {
            if (n.isLivenet()) {
                livenet = n;
            } else {
                other = n;
            }
        }

        return livenet != null ? livenet : other;
    }

    public LedgerId ledgerId() {
        if ("xrpl".equalsIgnoreCase(networkId)) {
            return LedgerId.Xrpl;
        } else if ("xahau".equalsIgnoreCase(networkId)) {
            return LedgerId.Xahau;
        } else if ("bitcoin".equalsIgnoreCase(networkId)) {
            return LedgerId.Bitcoin;
        }
        return null;
    }

    public AccountMappingSource createAccountMappingSource(Ledger ledger) throws Exception {
        var reader = new Pain001Reader(ledger);
        var payments = reader.read(xml());

        var mappingSource = new MemoryAccountMappingSource(true);
        for (var pair : accountWalletPairs()) {
            var mapping = new AccountMapping(ledger);
            mapping.setAccount(AccountFactory.create(pair.accountNo()));
            mapping.setWallet(ledger.createWallet(pair.walletPublicKey(), null));
            {
                var p = Arrays.stream(payments).filter(o -> o.getSenderAccount().getUnformatted().equals(pair.accountNo())).findFirst();
                p.ifPresent(payment -> mapping.setPartyId(Address.createPartyIdOrEmpty(payment.getSenderAddress())));
            }
            {
                var p = Arrays.stream(payments).filter(o -> o.getReceiverAccount().getUnformatted().equals(pair.accountNo())).findFirst();
                p.ifPresent(payment -> mapping.setPartyId(Address.createPartyIdOrEmpty(payment.getReceiverAddress())));
            }
            mappingSource.add(mapping);
        }
        return mappingSource;
    }
}
