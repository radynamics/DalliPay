package com.radynamics.dallipay.cryptoledger.xrpl.signing.xumm;

import com.radynamics.dallipay.db.ConfigRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseStorage implements Storage {
    private final static Logger log = LogManager.getLogger(DatabaseStorage.class);

    @Override
    public String getAccessToken() {
        try (var repo = new ConfigRepo()) {
            return repo.getXummAccessToken();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setAccessToken(String value) {
        try (var repo = new ConfigRepo()) {
            repo.setXummAccessToken(value);
            repo.commit();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public int getLocalHttpServerPort() {
        try (var repo = new ConfigRepo()) {
            return repo.getXummLocalHttpServerPort().orElse(XummPkce.defaultPort);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return XummPkce.defaultPort;
        }
    }
}
