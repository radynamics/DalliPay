package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;

public final class PayloadConverter {
    private static final int VERSION = 1;

    public static String toMemo(StructuredReference[] refs, String[] freeText) {
        if (refs == null) throw new IllegalArgumentException("Parameter 'refs' cannot be null");
        if (freeText == null) throw new IllegalArgumentException("Parameter 'freeText' cannot be null");

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
        } catch (ParseException e) {
            return null;
        } catch (Exception e) {
            var allFreeText = new MemoData();
            allFreeText.add(text);
            return allFreeText;
        }
    }

    private static MemoData parse(String text) throws ParseException {
        if (text == null) throw new ParseException("json text is null", 0);

        JSONObject json = new JSONObject(text);

        var v = json.getInt("v");
        if (v != VERSION) {
            return null;
        }

        var data = new MemoData();
        if (json.has("ft")) {
            var arr = json.getJSONArray("ft");
            for (int i = 0; i < arr.length(); i++) {
                data.add(arr.getString(i));
            }
        }
        if (json.has("CdOrPrtry")) {
            var arr = json.getJSONArray("CdOrPrtry");
            for (int i = 0; i < arr.length(); i++) {
                var obj = arr.getJSONObject(i);
                if (!obj.isNull("v") && obj.getString("v").length() > 0) {
                    data.add(StructuredReferenceFactory.create(StructuredReferenceFactory.getType(obj.getString("t")), obj.getString("v").replace(" ", "")));
                }
            }
        }
        return data;
    }
}
