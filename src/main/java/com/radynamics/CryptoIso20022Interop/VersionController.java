package com.radynamics.CryptoIso20022Interop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class VersionController {
    final static Logger log = LogManager.getLogger(VersionController.class);

    private final String version;

    public VersionController() {
        var is = getClass().getClassLoader().getResourceAsStream("version.properties");
        if (is == null) {
            // When running unit tests, no jar is built, so we load a copy of the file that we saved during build.gradle.
            // Possibly this also is the case during debugging, therefore we save in bin/main instead of bin/test.
            try {
                is = new FileInputStream("bin/main/version.properties");
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        var properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            log.error("Could not load classpath:/version.properties", e);
        }

        version = properties.getProperty("version", "version-tag-not-found");
    }

    public String getVersion() {
        return version;
    }
}
