package com.radynamics.dallipay.cryptoledger;

import com.radynamics.dallipay.MoneyFormatter;
import com.radynamics.dallipay.exchange.Currency;
import com.radynamics.dallipay.exchange.*;
import com.radynamics.dallipay.iso20022.Payment;

import java.math.BigDecimal;
import java.util.*;

public class PaymentUtils {
    public static ArrayList<Wallet> distinctSendingWallets(Payment[] payments) {
        return distinctSendingWallets(allTransactions(payments));
    }

    public static ArrayList<Wallet> distinctSendingWallets(Transaction[] payments) {
        var list = new ArrayList<Wallet>();
        for (var p : payments) {
            var existing = list.stream().anyMatch(w -> WalletCompare.isSame(p.getSenderWallet(), w));
            if (!existing) {
                list.add(p.getSenderWallet());
            }
        }
        return list;
    }

    private static Transaction[] allTransactions(Payment[] payments) {
        return Arrays.stream(payments).map(Payment::getTransaction).toArray(Transaction[]::new);
    }

    public static Ledger[] distinctLedgers(Payment[] payments) {
        var list = new ArrayList<Ledger>();
        for (var p : payments) {
            var existing = list.stream().anyMatch(l -> p.getLedger().getId().textId().equals(l.getId().textId()));
            if (!existing) {
                list.add(p.getLedger());
            }
        }
        return list.toArray(list.toArray(new Ledger[0]));
    }

    public static Optional<Ledger> getLedger(Wallet w, Payment[] payments) {
        for (var p : payments) {
            var isSameLedger = p.getLedger().getId().textId().equals(w.getLedgerId().textId());
            if (isSameLedger && WalletCompare.isSame(p.getSenderWallet(), w) || WalletCompare.isSame(p.getReceiverWallet(), w)) {
                return Optional.of(p.getLedger());
            }
        }
        return Optional.empty();
    }

    public static ArrayList<Payment> fromSender(Wallet w, Payment[] payments) {
        var list = new ArrayList<Payment>();
        for (var p : payments) {
            if (WalletCompare.isSame(p.getSenderWallet(), w)) {
                list.add(p);
            }
        }
        return list;
    }

    public static ArrayList<Transaction> fromSender(Wallet w, Transaction[] payments) {
        var list = new ArrayList<Transaction>();
        for (var p : payments) {
            if (WalletCompare.isSame(p.getSenderWallet(), w)) {
                list.add(p);
            }
        }
        return list;
    }

    public static MoneySums sumLedgerUnit(Collection<Payment> payments) {
        var sum = new MoneySums();
        for (var p : payments) {
            sum.plus(p.getAmountTransaction());
            for (var fee : p.getFees()) {
                sum.plus(fee.getAmount());
            }
        }
        return sum;
    }

    public static String sumString(ArrayList<Payment> payments, boolean ignoreIssuer) {
        return MoneyFormatter.formatFiat(Money.sort(sum(payments, ignoreIssuer)));
    }

    public static Money[] sum(ArrayList<Payment> payments, boolean ignoreIssuer) {
        var sums = new MoneySums();
        for (var p : payments) {
            sums.plus(Money.of(p.getAmount(), ignoreIssuer ? p.getUserCcy().withoutIssuer() : p.getUserCcy()));
        }

        return sums.sum();
    }

    public static String totalFeesText(Payment[] payments, ExchangeRateProvider provider) {
        if (payments.length == 0) {
            return "";
        }

        var sb = new StringBuilder();
        var i = 0;
        var fees = totalFees(payments);
        Double fiatSum = (double) 0;
        var fiatCcy = payments[0].getUserCcy();
        for (var ccy : fees.currencies()) {
            var amt = fees.sum(ccy);

            var r = ExchangeRate.getOrNull(provider.latestRates(), new CurrencyPair(ccy, fiatCcy));
            fiatSum = r == null || fiatSum == null ? null : fiatSum + amt.getNumber().doubleValue() * r.getRate();

            sb.append(MoneyFormatter.formatLedger(payments[0].getLedger().getNativeCcyNumberFormat(), amt));
            if (i + 1 < fees.currencies().length) {
                sb.append(", ");
            }
            i++;
        }

        return fiatSum == null
                ? sb.toString()
                : String.format("%s (%s)", MoneyFormatter.formatFiat(BigDecimal.valueOf(fiatSum), fiatCcy.getCode()), sb);
    }

    private static MoneySums totalFees(Payment[] payments) {
        var sum = new MoneySums();
        for (var p : payments) {
            for (var fee : p.getFees()) {
                sum.plus(fee.getAmount());
            }
        }
        return sum;
    }

    public static Currency mostUsedUserCcy(Payment[] payments) {
        if (payments.length == 0) {
            return null;
        }

        var usage = new HashMap<Currency, Integer>();
        for (var p : payments) {
            var ccy = p.getUserCcy();
            var count = usage.getOrDefault(ccy, 0);
            usage.put(ccy, count + 1);
        }

        var candidate = usage.entrySet().stream().findFirst().orElseThrow();
        for (var pair : usage.entrySet()) {
            candidate = candidate.getValue() > pair.getValue() ? candidate : pair;
        }
        return candidate.getKey();
    }
}
