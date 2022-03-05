package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerId;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.db.AccountMapping;
import com.radynamics.CryptoIso20022Interop.db.AccountMappingRepo;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbAccountMappingSource implements AccountMappingSource {
    final static Logger log = LogManager.getLogger(DbAccountMappingSource.class);

    private final LedgerId ledgerId;

    public DbAccountMappingSource(LedgerId ledgerId) {
        this.ledgerId = ledgerId;
    }

    @Override
    public Wallet getWalletOrNull(Account account) {
        try (var repo = new AccountMappingRepo()) {
            var found = repo.list(ledgerId, account);
            if (found.length == 0) {
                return null;
            }
            return found[0].getWallet();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Account getAccountOrNull(Wallet wallet) {
        try (var repo = new AccountMappingRepo()) {
            var found = repo.list(ledgerId, wallet);
            return found.length == 0 ? null : found[0].getAccount();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void add(AccountMapping mapping) {
        try (var repo = new AccountMappingRepo()) {
            repo.saveOrUpdate(mapping);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
