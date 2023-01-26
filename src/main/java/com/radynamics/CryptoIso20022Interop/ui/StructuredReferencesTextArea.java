package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReference;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;

public class StructuredReferencesTextArea extends JTextArea {
    private StructuredReference[] structuredReferences = new StructuredReference[0];

    private void refreshText() {
        setText(toText());
        setCaretPosition(0);
    }

    private String toText() {
        var lines = new ArrayList<String>();
        for (var ref : structuredReferences) {
            lines.add(ref.getUnformatted());
        }
        return Utils.toMultilineText(lines.toArray(new String[0]));
    }

    private StructuredReference[] fromText() {
        var refs = new ArrayList<StructuredReference>();
        for (var word : Utils.fromMultilineText(getText())) {
            var unformattedRef = StringUtils.deleteWhitespace(word);
            if (unformattedRef.length() == 0) {
                continue;
            }

            var type = StructuredReferenceFactory.detectType(unformattedRef);
            refs.add(StructuredReferenceFactory.create(type, unformattedRef));
        }

        return refs.toArray(new StructuredReference[0]);
    }

    public void setValue(StructuredReference[] structuredReferences) {
        this.structuredReferences = structuredReferences;
        refreshText();
    }

    public StructuredReference[] getValue() {
        this.structuredReferences = fromText();
        return this.structuredReferences;
    }
}
