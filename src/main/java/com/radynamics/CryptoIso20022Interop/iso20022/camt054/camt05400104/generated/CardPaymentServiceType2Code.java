//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CardPaymentServiceType2Code.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="CardPaymentServiceType2Code"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="AGGR"/&gt;
 *     &lt;enumeration value="DCCV"/&gt;
 *     &lt;enumeration value="GRTT"/&gt;
 *     &lt;enumeration value="INSP"/&gt;
 *     &lt;enumeration value="LOYT"/&gt;
 *     &lt;enumeration value="NRES"/&gt;
 *     &lt;enumeration value="PUCO"/&gt;
 *     &lt;enumeration value="RECP"/&gt;
 *     &lt;enumeration value="SOAF"/&gt;
 *     &lt;enumeration value="UNAF"/&gt;
 *     &lt;enumeration value="VCAU"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CardPaymentServiceType2Code")
@XmlEnum
public enum CardPaymentServiceType2Code {

    AGGR,
    DCCV,
    GRTT,
    INSP,
    LOYT,
    NRES,
    PUCO,
    RECP,
    SOAF,
    UNAF,
    VCAU;

    public String value() {
        return name();
    }

    public static CardPaymentServiceType2Code fromValue(String v) {
        return valueOf(v);
    }

}
