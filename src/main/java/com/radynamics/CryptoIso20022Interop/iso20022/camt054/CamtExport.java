package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

public class CamtExport {
    private Camt054Writer writer;
    private CamtConverter converter;

    public Camt054Writer getWriter() {
        return writer;
    }

    public void setWriter(Camt054Writer writer) {
        this.writer = writer;
    }

    public CamtConverter getConverter() {
        return converter;
    }

    public void setConverter(CamtConverter converter) {
        this.converter = converter;
    }
}
