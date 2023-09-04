package com.radynamics.dallipay.iso20022.camt054;

public class CamtFormatEntry {
    private final CamtFormat camtFormat;
    private final String key;
    private final String displayText;

    public CamtFormatEntry(CamtFormat camtFormat, String key, String displayText) {
        this.camtFormat = camtFormat;
        this.key = key;
        this.displayText = displayText;
    }

    public CamtFormat getCamtFormat() {
        return camtFormat;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayText() {
        return displayText;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", camtFormat, displayText);
    }
}
