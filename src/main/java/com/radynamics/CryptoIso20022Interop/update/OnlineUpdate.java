package com.radynamics.CryptoIso20022Interop.update;

import com.radynamics.CryptoIso20022Interop.VersionController;
import com.vdurmont.semver4j.Semver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class OnlineUpdate {
    final static Logger log = LogManager.getLogger(OnlineUpdate.class);

    public static final CompletableFuture<UpdateInfo> search() {
        var f = new CompletableFuture<UpdateInfo>();

        Executors.newCachedThreadPool().submit(() -> {
            var latestUpdate = loadUpdateInfo();

            var vc = new VersionController();
            var current = new Semver(vc.getVersion());
            var latest = new Semver(latestUpdate.getVersion());

            if (latest.isGreaterThan(current)) {
                f.complete(latestUpdate);
            } else {
                f.complete(null);
            }
        });

        return f;
    }

    private static UpdateInfo loadUpdateInfo() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL("https://www.radynamics.com/CryptoIso20022Interop/releases/update.xml").openStream());

            var xPath = XPathFactory.newInstance().newXPath();
            var version = xPath.evaluate("/item/version", doc);
            var urlText = xPath.evaluate("/item/url", doc);
            return new UpdateInfo(version, URI.create(urlText));
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return null;
        }
    }
}
