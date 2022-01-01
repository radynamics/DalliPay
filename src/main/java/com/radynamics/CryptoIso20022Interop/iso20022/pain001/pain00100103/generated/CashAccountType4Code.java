//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.01.01 um 10:27:37 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CashAccountType4Code.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="CashAccountType4Code"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CASH"/&gt;
 *     &lt;enumeration value="CHAR"/&gt;
 *     &lt;enumeration value="COMM"/&gt;
 *     &lt;enumeration value="TAXE"/&gt;
 *     &lt;enumeration value="CISH"/&gt;
 *     &lt;enumeration value="TRAS"/&gt;
 *     &lt;enumeration value="SACC"/&gt;
 *     &lt;enumeration value="CACC"/&gt;
 *     &lt;enumeration value="SVGS"/&gt;
 *     &lt;enumeration value="ONDP"/&gt;
 *     &lt;enumeration value="MGLD"/&gt;
 *     &lt;enumeration value="NREX"/&gt;
 *     &lt;enumeration value="MOMA"/&gt;
 *     &lt;enumeration value="LOAN"/&gt;
 *     &lt;enumeration value="SLRY"/&gt;
 *     &lt;enumeration value="ODFT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CashAccountType4Code")
@XmlEnum
public enum CashAccountType4Code {

    CASH,
    CHAR,
    COMM,
    TAXE,
    CISH,
    TRAS,
    SACC,
    CACC,
    SVGS,
    ONDP,
    MGLD,
    NREX,
    MOMA,
    LOAN,
    SLRY,
    ODFT;

    public String value() {
        return name();
    }

    public static CashAccountType4Code fromValue(String v) {
        return valueOf(v);
    }

}
