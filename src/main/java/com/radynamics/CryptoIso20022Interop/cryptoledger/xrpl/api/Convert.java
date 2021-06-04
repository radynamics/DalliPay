package com.radynamics.CryptoIso20022Interop.cryptoledger.xrpl.api;

import com.radynamics.CryptoIso20022Interop.iso20022.Utils;
import org.xrpl.xrpl4j.model.transactions.ImmutableMemo;
import org.xrpl.xrpl4j.model.transactions.ImmutableMemoWrapper;

public final class Convert {
    public static ImmutableMemoWrapper toMemoWrapper(String value) {
        return ImmutableMemoWrapper.builder().memo(
                ImmutableMemo.builder()
                        .memoData(Utils.stringToHex(value))
                        .memoFormat(Utils.stringToHex("json"))
                        .build()
        ).build();
    }
}
