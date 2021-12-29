package com.radynamics.CryptoIso20022Interop.iso20022;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

public class Utils {
    public static XMLGregorianCalendar toXmlDateTime(LocalDateTime value) throws DatatypeConfigurationException {
        var gcal = GregorianCalendar.from(ZonedDateTime.of(value, ZoneOffset.UTC));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    public static String hexToString(String hex) throws DecoderException, UnsupportedEncodingException {
        return new String(Hex.decodeHex(hex.toCharArray()), "UTF-8");
    }

    public static String stringToHex(String plain) {
        try {
            return new String(Hex.encodeHex(plain.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
