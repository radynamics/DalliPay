//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.05.28 um 01:45:09 PM CEST 
//


package com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103ch02.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AccountIdentification4Choice-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AccountIdentification4Choice-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="IBAN" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}IBAN2007Identifier"/&gt;
 *           &lt;element name="Othr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}GenericAccountIdentification1-CH"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountIdentification4Choice-CH", propOrder = {
    "iban",
    "othr"
})
public class AccountIdentification4ChoiceCH {

    @XmlElement(name = "IBAN")
    protected String iban;
    @XmlElement(name = "Othr")
    protected GenericAccountIdentification1CH othr;

    /**
     * Ruft den Wert der iban-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIBAN() {
        return iban;
    }

    /**
     * Legt den Wert der iban-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIBAN(String value) {
        this.iban = value;
    }

    /**
     * Ruft den Wert der othr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GenericAccountIdentification1CH }
     *     
     */
    public GenericAccountIdentification1CH getOthr() {
        return othr;
    }

    /**
     * Legt den Wert der othr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericAccountIdentification1CH }
     *     
     */
    public void setOthr(GenericAccountIdentification1CH value) {
        this.othr = value;
    }

}
