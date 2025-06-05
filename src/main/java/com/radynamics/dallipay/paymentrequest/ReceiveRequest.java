package com.radynamics.dallipay.paymentrequest;

import com.radynamics.dallipay.cryptoledger.LedgerId;
import com.radynamics.dallipay.iso20022.camt054.CamtFormat;

import java.time.LocalDateTime;
import java.util.List;

public class ReceiveRequest {
    private String applicationName;
    private String wallet;
    private String format;
    private LocalDateTime from;
    private LocalDateTime to;
    private LedgerId ledgerId;
    private List<AccountWalletPair> accountWalletPairs;
    private String camtXml;
    private boolean aborted;


    public String applicationName() {
        return this.applicationName;
    }

    public void applicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String wallet() {
        return wallet;
    }

    public void wallet(String wallet) {
        this.wallet = wallet;
    }

    public String format() {
        return format;
    }

    public void format(String format) {
        this.format = format;
    }

    public LocalDateTime from() {
        return from;
    }

    public void from(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime to() {
        return this.to;
    }

    public void to(LocalDateTime to) {
        this.to = to;
    }

    public LedgerId ledgerId() {
        return this.ledgerId;
    }

    public void ledgerId(LedgerId ledgerId) {
        this.ledgerId = ledgerId;
    }

    public void accountWalletPairs(List<AccountWalletPair> accountWalletPairs) {
        this.accountWalletPairs = accountWalletPairs;
    }

    public String camtXml() {
        return this.camtXml;
    }

    public void camtXml(String camtXml) {
        this.camtXml = camtXml;
    }

    public boolean aborted() {
        return this.aborted;
    }

    public void aborted(boolean aborted) {
        this.aborted = aborted;
    }

    public CamtFormat camtFormat() {
        return switch (format) {
            case "camt053" -> CamtFormat.Camt05300108;
            case "camt054" -> CamtFormat.Camt05400109;
            default -> throw new IllegalStateException("Unexpected value: " + format);
        };
    }
}
