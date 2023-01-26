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
        var sb = new StringBuilder();
        for (var ref : structuredReferences) {
            sb.append(String.format("%s%s", ref.getUnformatted(), System.lineSeparator()));
        }
        return Utils.removeEndingLineSeparator(sb.toString());
    }

    private StructuredReference[] fromText() {
        var words = getText().split("\\r?\\n");
        for (var i = 0; i < words.length; i++) {
            words[i] = StringUtils.deleteWhitespace(words[i]);
        }

        var refs = new ArrayList<StructuredReference>();
        for (var word : words) {
            if (word.length() == 0) {
                continue;
            }

            var type = StructuredReferenceFactory.detectType(word);
            refs.add(StructuredReferenceFactory.create(type, word));
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
