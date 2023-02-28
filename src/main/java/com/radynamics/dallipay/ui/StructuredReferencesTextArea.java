package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReference;
import com.radynamics.dallipay.iso20022.creditorreference.StructuredReferenceFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class StructuredReferencesTextArea extends PlaceholderTextArea {
    private StructuredReference[] structuredReferences = new StructuredReference[0];

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public StructuredReferencesTextArea() {
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("structuredReferencesTextArea.placeholderText"));
    }

    private void refreshText() {
        setText(toText());
        setCaretPosition(0);
        Utils.patchTabBehavior(this);
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
