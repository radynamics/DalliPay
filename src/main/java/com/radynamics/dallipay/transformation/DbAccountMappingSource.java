package com.radynamics.dallipay.transformation;

import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.db.AccountMapping;
import com.radynamics.dallipay.db.AccountMappingRepo;
import com.radynamics.dallipay.iso20022.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class DbAccountMappingSource implements AccountMappingSource {
    final static Logger log = LogManager.getLogger(DbAccountMappingSource.class);

    private final LedgerId ledgerId;
    private AccountMappingRepo repo;

    public DbAccountMappingSource(LedgerId ledgerId) {
        this.ledgerId = ledgerId;
    }

    @Override
    public Wallet getWalletOrNull(Account account) throws AccountMappingSourceException {
        assertOpen();
        try {
            var found = repo.list(ledgerId, account);
            if (found.length == 0) {
                return null;
            }
            return found[0].getWallet();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void assertClosed() throws AccountMappingSourceException {
        if (repo != null) {
            throw new AccountMappingSourceException("Source not closed.");
        }
    }

    private void assertOpen() throws AccountMappingSourceException {
        if (repo == null) {
            throw new AccountMappingSourceException("Source not opened.");
        }
    }

    @Override
    public Account getAccountOrNull(Wallet wallet) throws AccountMappingSourceException {
        assertOpen();
        try {
            var found = repo.list(ledgerId, wallet);
            return found.length == 0 ? null : found[0].getAccount();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void add(AccountMapping mapping) throws AccountMappingSourceException {
        assertOpen();
        try {
            repo.saveOrUpdate(mapping);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void open() throws AccountMappingSourceException {
        assertClosed();
        repo = new AccountMappingRepo();
    }

    @Override
    public void close() throws AccountMappingSourceException {
        assertOpen();
        try {
            repo.close();
            repo = null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
