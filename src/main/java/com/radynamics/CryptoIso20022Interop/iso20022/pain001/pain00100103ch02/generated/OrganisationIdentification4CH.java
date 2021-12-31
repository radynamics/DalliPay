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
 * <p>Java-Klasse für OrganisationIdentification4-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="OrganisationIdentification4-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BICOrBEI" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}AnyBICIdentifier" minOccurs="0"/&gt;
 *         &lt;element name="Othr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}GenericOrganisationIdentification1" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrganisationIdentification4-CH", propOrder = {
    "bicOrBEI",
    "othr"
})
public class OrganisationIdentification4CH {

    @XmlElement(name = "BICOrBEI")
    protected String bicOrBEI;
    @XmlElement(name = "Othr")
    protected GenericOrganisationIdentification1 othr;

    /**
     * Ruft den Wert der bicOrBEI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBICOrBEI() {
        return bicOrBEI;
    }

    /**
     * Legt den Wert der bicOrBEI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBICOrBEI(String value) {
        this.bicOrBEI = value;
    }

    /**
     * Ruft den Wert der othr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GenericOrganisationIdentification1 }
     *     
     */
    public GenericOrganisationIdentification1 getOthr() {
        return othr;
    }

    /**
     * Legt den Wert der othr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericOrganisationIdentification1 }
     *     
     */
    public void setOthr(GenericOrganisationIdentification1 value) {
        this.othr = value;
    }

}
