package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.camt054.schema.generated.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class CamtConverter {
    public static ByteArrayOutputStream toXml(Document document) throws JAXBException {
        var ctx = JAXBContext.newInstance(Document.class);
        var m = ctx.createMarshaller();
        var stream = new ByteArrayOutputStream();
        m.marshal(document, stream);
        return stream;
    }

    public static Document toDocument(InputStream input) throws JAXBException {
        var ctx = JAXBContext.newInstance(Document.class);
        var m = ctx.createUnmarshaller();
        JAXBElement<Document> rootElement = (JAXBElement<Document>) m.unmarshal(input);
        return rootElement.getValue();
    }
}
