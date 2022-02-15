package com.radynamics.CryptoIso20022Interop.cryptoledger;

import com.radynamics.CryptoIso20022Interop.MoneyFormatter;
import com.radynamics.CryptoIso20022Interop.iso20022.Payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PaymentUtils {
    public static final ArrayList<Wallet> distinctSendingWallets(Payment[] payments) {
        var list = new ArrayList<Wallet>();
        for (var p : payments) {
            var existing = list.stream().anyMatch(w -> WalletCompare.isSame(p.getSenderWallet(), w));
            if (!existing) {
                list.add(p.getSenderWallet());
            }
        }
        return list;
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

    public static long sumSmallestLedgerUnit(Collection<Payment> payments) {
        long sum = 0;
        for (var p : payments) {
            sum += p.getLedgerAmountSmallestUnit();
        }
        return sum;
    }

    public static String sumString(ArrayList<Payment> payments) {
        var sb = new StringBuilder();
        var i = 0;
        var sums = sum(payments);
        for (var sum : sums.entrySet()) {
            sb.append(MoneyFormatter.formatFiat(BigDecimal.valueOf(sum.getValue()), sum.getKey()));
            if (i + 1 < sums.size()) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    public static Map<String, Double> sum(ArrayList<Payment> payments) {
        var map = new HashMap<String, Double>();
        for (var p : payments) {
            if (!map.containsKey(p.getFiatCcy())) {
                map.put(p.getFiatCcy(), Double.valueOf(0));
            }
            map.put(p.getFiatCcy(), map.get(p.getFiatCcy()) + p.getAmount());
        }
        return map;
    }
}
