package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.cryptoledger.Ledger;
import com.radynamics.dallipay.cryptoledger.Wallet;
import com.radynamics.dallipay.cryptoledger.WalletFormatter;
import com.radynamics.dallipay.cryptoledger.WalletInfoAggregator;
import com.radynamics.dallipay.exchange.Money;
import com.radynamics.dallipay.iso20022.Account;
import com.radynamics.dallipay.iso20022.Address;
import com.radynamics.dallipay.iso20022.PaymentFormatter;

import javax.swing.*;

public class WalletLabel extends JPanel {
    private final JLabel firstLine;
    private final JLabel secondLine;

    private Wallet wallet;
    private Ledger ledger;
    private Account account;
    private Address address;
    private WalletInfoAggregator walletInfoAggregator;
    private int infoLineCount = 1;

    public WalletLabel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        firstLine = new JLabel();
        add(firstLine);

        secondLine = Utils.formatSecondaryInfo(new JLabel());
        add(secondLine);
    }

    private void evaluate() {
        firstLine.setText(PaymentFormatter.singleLineText(account, address));

        var walletText = new WalletFormatter().format(wallet);
        if (wallet == null) {
            secondLine.setText(createText(walletText, null, null));
            return;
        }

        String balanceText = null;
        if (!wallet.getBalances().isEmpty() && ledger != null) {
            balanceText = MoneyFormatter.formatFiat(Money.sort(wallet.getBalances().all()));
        }

        var wi = walletInfoAggregator == null ? null : walletInfoAggregator.getNameOrDomain(wallet);
        String walletInfoText = WalletInfoFormatter.toText(wi).orElse(null);

        secondLine.setText(createText(walletText, balanceText, walletInfoText));
        WalletInfoFormatter.format(secondLine, wi);
    }

    private String createText(String walletText, String balanceText, String walletInfoText) {
        if (balanceText == null && walletInfoText == null) {
            return walletText;
        }

        if (infoLineCount == 1) {
            if (balanceText != null && walletInfoText == null) {
                return String.format("%s (%s)", walletText, balanceText);
            }
            if (balanceText == null && walletInfoText != null) {
                return String.format("%s (%s)", walletText, walletInfoText);
            }
            return String.format("%s (%s, %s)", walletText, balanceText, walletInfoText);
        }
        if (infoLineCount == 2) {
            if (balanceText != null && walletInfoText == null) {
                return String.format("<html><body>%s<br />%s</body></html>", walletText, balanceText);
            }
            if (balanceText == null && walletInfoText != null) {
                return String.format("<html><body>%s<br />%s</body></html>", walletText, balanceText);
            }
            return String.format("<html><body>%s<br />%s, %s</body></html>", walletText, balanceText, walletInfoText);
        }

        return walletText;
    }

    public WalletLabel setWallet(Wallet wallet) {
        this.wallet = wallet;
        evaluate();
        return this;
    }

    public WalletLabel setLedger(Ledger ledger) {
        this.ledger = ledger;
        evaluate();
        return this;
    }

    public WalletLabel setAccount(Account account) {
        this.account = account;
        evaluate();
        return this;
    }

    public WalletLabel setAddress(Address address) {
        this.address = address;
        evaluate();
        return this;
    }

    public WalletLabel setWalletInfoAggregator(WalletInfoAggregator walletInfoAggregator) {
        this.walletInfoAggregator = walletInfoAggregator;
        evaluate();
        return this;
    }

    public WalletLabel setInfoLineCount(int infoLineCount) {
        this.infoLineCount = infoLineCount;
        evaluate();
        return this;
    }
}
