package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Wallet;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletFormatter;
import com.radynamics.CryptoIso20022Interop.cryptoledger.WalletInfoAggregator;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.AccountFormatter;
import com.radynamics.CryptoIso20022Interop.iso20022.Address;
import com.radynamics.CryptoIso20022Interop.iso20022.AddressFormatter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class WalletLabel extends JPanel {
    private final JLabel firstLine;
    private final JLabel secondLine;

    private Wallet wallet;
    private Ledger ledger;
    private Account account;
    private Address address;
    private WalletInfoAggregator walletInfoAggregator;

    public WalletLabel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        firstLine = new JLabel();
        add(firstLine);

        secondLine = Utils.formatSecondaryInfo(new JLabel());
        add(secondLine);
    }

    private void evaluate() {
        firstLine.setText(getFirstLineText());

        var secondLineText = new WalletFormatter().format(wallet);
        if (wallet == null) {
            secondLine.setText(secondLineText);
            return;
        }

        var amtSmallestUnit = wallet.getLedgerBalanceSmallestUnit();
        if (amtSmallestUnit != null && ledger != null) {
            var balance = ledger.convertToNativeCcyAmount(amtSmallestUnit.longValue());
            secondLineText = String.format("%s (%s)", secondLineText, MoneyFormatter.formatLedger(balance, ledger.getNativeCcySymbol()));
        }

        if (walletInfoAggregator != null) {
            var wi = walletInfoAggregator.getMostImportant(wallet);
            secondLineText = wi == null ? secondLineText : String.format("%s (%s %s)", secondLineText, wi.getText(), wi.getValue());
        }

        secondLine.setText(secondLineText);
    }

    private String getFirstLineText() {
        var sb = new StringBuilder();

        if (address != null) {
            sb.append(AddressFormatter.formatSingleLine(address));
        }

        if (account == null && sb.length() > 0) {
            return sb.toString();
        }

        var accountText = account == null || StringUtils.isEmpty(account.getUnformatted())
                ? "Missing Account"
                : AccountFormatter.format(account);
        var template = sb.length() == 0 ? "%s" : " (%s)";
        sb.append(String.format(template, accountText));

        return sb.toString();
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
}
