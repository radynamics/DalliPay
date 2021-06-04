package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;

public final class MemoConverter {
    public static String toMemo(StructuredReference[] refs, String[] freeText) {
        final int VERSION = 1;

        var json = new JSONObject();
        json.put("v", VERSION);

        {
            var arr = new JSONArray();
            for (var r : refs) {
                arr.put(StructuredReferenceConverter.toMemo(r));
            }
            json.put("CdOrPrtry", arr);
        }
        {
            var arr = new JSONArray();
            for (var t : freeText) {
                arr.put(FreeTextConverter.toMemo(t));
            }
            json.put("ft", arr); // FreeText
        }

        return json.toString();
    }

    public static MemoData fromMemo(String text) {
        try {
            return parse(text);
        } catch (Exception e) {
            var allFreeText = new MemoData();
            allFreeText.add(text);
            return allFreeText;
        }
    }

    private static MemoData parse(String text) throws ParseException {
        if (text == null) {
            throw new ParseException("json text is null", 0);
        }

        JSONObject json = new JSONObject(text);

        // TODO: handle versioning
        var v = json.getInt("v");
        if (v <= 0) {
            return null;
        }

        // TODO: validate format
        var data = new MemoData();
        {
            var arr = json.getJSONArray("ft");
            for (int i = 0; i < arr.length(); i++) {
                data.add(arr.getString(i));
            }
        }
        {
            var arr = json.getJSONArray("CdOrPrtry");
            for (int i = 0; i < arr.length(); i++) {
                var obj = arr.getJSONObject(i);
                data.add(StructuredReferenceFactory.create(obj.getString("t"), obj.getString("v")));
            }
        }
        return data;
    }
}
