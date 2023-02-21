package com.radynamics.dallipay.db;

import com.radynamics.dallipay.cryptoledger.LedgerFactory;
import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletValidator;
import com.radynamics.dallipay.iso20022.Account;
import org.apache.commons.lang3.StringUtils;

public class AccountMapping {
    private long id;
    private Account account;
    private Wallet wallet;
    private LedgerId ledgerId;

    public AccountMapping(LedgerId ledgerId) {
        this.ledgerId = ledgerId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public LedgerId getLedgerId() {
        return ledgerId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean allPresent() {
        return !accountMissing() && !walletMissing();
    }

    public boolean accountOrWalletMissing() {
        return accountMissing() || walletMissing();
    }

    private boolean accountMissing() {
        return getAccount() == null || StringUtils.isEmpty(getAccount().getUnformatted());
    }

    private boolean walletMissing() {
        return getWallet() == null || StringUtils.isEmpty(getWallet().getPublicKey());
    }

    public boolean isPersisted() {
        return getId() != 0;
    }

    public boolean bothSame() {
        return allPresent() && StringUtils.equals(getAccount().getUnformatted(), getWallet().getPublicKey());
    }

    public boolean isWalletPresentAndValid() {
        if (walletMissing()) {
            return false;
        }

        return WalletValidator.isValidFormat(LedgerFactory.create(wallet.getLedgerId()), wallet);
    }

    @Override
    public String toString() {
        return String.format("%s: %s -> %s", getId(),
                account == null ? "<null" : account.getUnformatted(),
                wallet == null ? "<null>" : wallet.getPublicKey());
    }
}
