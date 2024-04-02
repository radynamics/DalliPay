package com.radynamics.dallipay.cryptoledger.xrpl.api;

import com.google.common.primitives.UnsignedInteger;
import com.radynamics.dallipay.cryptoledger.Block;
import com.radynamics.dallipay.iso20022.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.ImmutableMemo;
import org.xrpl.xrpl4j.model.transactions.ImmutableMemoWrapper;

import java.io.UnsupportedEncodingException;

public final class Convert {
    private final static Logger log = LogManager.getLogger(Convert.class);

    // The standard format for currency codes is a three-character string such as USD. (https://xrpl.org/currency-formats.html)
    private static final int ccyCodeStandardFormatLength = 3;

    public static ImmutableMemoWrapper toMemoWrapper(String value) {
        return ImmutableMemoWrapper.builder().memo(
                ImmutableMemo.builder()
                        .memoData(Utils.stringToHex(value))
                        .memoFormat(Utils.stringToHex("json"))
                        .build()
        ).build();
    }

    public static String toCurrencyCode(String currency) {
        try {
            if (currency.length() <= ccyCodeStandardFormatLength) {
                return currency;
            }

            // "LP Token Currency Codes" (https://xrpl.org/docs/concepts/tokens/decentralized-exchange/automated-market-makers/#lp-token-currency-codes)
            if (currency.startsWith("03")) {
                // Eg. "0348E1573E830D01581CD80DFE1E02A9FF55A34B" -> "LP 48E1573E830D01581CD80DFE1E02A9FF55A34B"
                return "LP %s".formatted(currency.substring(2));
            }

            // trim() needed, due value is always 20 bytes, filled with 0.
            return Utils.hexToString(currency).trim();
        } catch (DecoderException | UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return currency;
        }
    }

    public static String fromCurrencyCode(String code) {
        // value is always 20 bytes, filled with 0.
        return code.length() <= ccyCodeStandardFormatLength ? code : StringUtils.rightPad(Utils.stringToHex(code), 40, '0');
    }

    public static LedgerBlock toLedgerBlock(Block block) {
        if (block == null) throw new IllegalArgumentException("Parameter 'block' cannot be null");
        if (block == Block.VALIDATED) {
            return new LedgerBlock(LedgerSpecifier.VALIDATED.ledgerIndex().orElseThrow());
        }
        return new LedgerBlock(LedgerIndex.of(UnsignedInteger.valueOf(block.getId())));
    }
}
