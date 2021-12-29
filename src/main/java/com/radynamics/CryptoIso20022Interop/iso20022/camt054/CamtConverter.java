package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.iso20022.camt054.schema.generated.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;

public final class CamtConverter {
    public static ByteArrayOutputStream toXml(Document document) throws JAXBException {
        var ctx = JAXBContext.newInstance(Document.class);
        var m = ctx.createMarshaller();
        var stream = new ByteArrayOutputStream();
        m.marshal(document, stream);
        return stream;
    }
}
