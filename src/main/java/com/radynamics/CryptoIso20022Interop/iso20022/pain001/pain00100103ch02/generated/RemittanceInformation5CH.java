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
 * <p>Java-Klasse für RemittanceInformation5-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RemittanceInformation5-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Ustrd" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}Max140Text" minOccurs="0"/&gt;
 *         &lt;element name="Strd" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}StructuredRemittanceInformation7" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemittanceInformation5-CH", propOrder = {
    "ustrd",
    "strd"
})
public class RemittanceInformation5CH {

    @XmlElement(name = "Ustrd")
    protected String ustrd;
    @XmlElement(name = "Strd")
    protected StructuredRemittanceInformation7 strd;

    /**
     * Ruft den Wert der ustrd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUstrd() {
        return ustrd;
    }

    /**
     * Legt den Wert der ustrd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUstrd(String value) {
        this.ustrd = value;
    }

    /**
     * Ruft den Wert der strd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StructuredRemittanceInformation7 }
     *     
     */
    public StructuredRemittanceInformation7 getStrd() {
        return strd;
    }

    /**
     * Legt den Wert der strd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StructuredRemittanceInformation7 }
     *     
     */
    public void setStrd(StructuredRemittanceInformation7 value) {
        this.strd = value;
    }

}
