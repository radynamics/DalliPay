package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class CamtConverter<T> {
    private Class<T> documentClass;

    public CamtConverter(Class<T> documentClass) {
        this.documentClass = documentClass;
    }

    public <T> ByteArrayOutputStream toXml(T document) throws JAXBException {
        var ctx = JAXBContext.newInstance(documentClass);
        var m = ctx.createMarshaller();
        var stream = new ByteArrayOutputStream();
        m.marshal(document, stream);
        return stream;
    }

    public <T> T toDocument(InputStream input) throws JAXBException {
        var ctx = JAXBContext.newInstance(documentClass);
        var m = ctx.createUnmarshaller();
        return (T) m.unmarshal(input);
    }
}
