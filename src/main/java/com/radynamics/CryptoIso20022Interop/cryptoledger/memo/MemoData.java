package com.radynamics.CryptoIso20022Interop.cryptoledger.memo;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;

import java.util.ArrayList;

public class MemoData {
    private ArrayList<StructuredReference> structuredReferences = new ArrayList<>();
    private ArrayList<String> freeTexts = new ArrayList<>();

    public void add(StructuredReference value) {
        structuredReferences.add(value);
    }

    public void add(String value) {
        freeTexts.add(value);
    }

    public StructuredReference[] structuredReferences() {
        return structuredReferences.toArray(new StructuredReference[0]);
    }

    public String[] freeTexts() {
        return freeTexts.toArray(new String[0]);
    }
}
