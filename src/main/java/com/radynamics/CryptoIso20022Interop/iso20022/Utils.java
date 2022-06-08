package com.radynamics.CryptoIso20022Interop.iso20022;

import com.radynamics.CryptoIso20022Interop.DateTimeConvert;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

public class Utils {
    public static XMLGregorianCalendar toXmlDateTime(ZonedDateTime value) throws DatatypeConfigurationException {
        var gcal = GregorianCalendar.from(DateTimeConvert.toUserTimeZone(value));
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

    public static boolean bothPresent(Object first, Object second) {
        return first != null && second != null;
    }

    public static boolean bothNull(Object first, Object second) {
        return first == null && second == null;
    }

    public static ZonedDateTime endOfToday() {
        return endOfDay(ZonedDateTime.now());
    }

    public static ZonedDateTime endOfDay(ZonedDateTime dt) {
        return dt.with(LocalTime.of(23, 59, 59));
    }
}
