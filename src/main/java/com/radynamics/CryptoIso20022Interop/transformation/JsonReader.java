package com.radynamics.CryptoIso20022Interop.transformation;

import com.radynamics.CryptoIso20022Interop.cryptoledger.LedgerFactory;
import com.radynamics.CryptoIso20022Interop.exchange.ExchangeFactory;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.DateFormat;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonReader {
    public TransformInstruction read(InputStream input) {
        var bufferedReader = new BufferedReader(new InputStreamReader(input));
        var tokener = new JSONTokener(bufferedReader);
        var json = new JSONObject(tokener);

        // TODO: validate format
        var ledger = LedgerFactory.create(json.getString("ledger"));
        var ti = new TransformInstruction(ledger);
        ti.setExchange(ExchangeFactory.create(json.getString("exchange")));
        // If set to another currency than ledger's native currency, amounts are converted using rates provided by exchange.
        ti.setTargetCcy(json.getString("targetCcy"));

        ti.setBookingDateFormat(parseDateFormat(json.getString("bookingDateFormat")));
        ti.setValutaDateFormat(parseDateFormat(json.getString("valutaDateFormat")));

        var arr = json.getJSONArray("accountMapping");
        for (int i = 0; i < arr.length(); i++) {
            var obj = arr.getJSONObject(i);
            var account = obj.has("iban") ? new IbanAccount(obj.getString("iban")) : new OtherAccount(obj.getString("other"));
            ti.add(new AccountMapping(account, obj.getString("ledgerWallet")));
        }

        return ti;
    }

    private DateFormat parseDateFormat(String value) {
        if (StringUtils.equalsIgnoreCase(value, "datetime")) {
            return DateFormat.DateTime;
        }
        if (StringUtils.equalsIgnoreCase(value, "date")) {
            return DateFormat.Date;
        }
        throw new NotImplementedException(String.format("dateFormat %s unknown.", value));
    }
}
