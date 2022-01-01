//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.01.01 um 10:27:37 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TaxParty2 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TaxParty2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TaxId" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.03}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="RegnId" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.03}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="TaxTp" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.03}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="Authstn" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.03}TaxAuthorisation1" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxParty2", propOrder = {
    "taxId",
    "regnId",
    "taxTp",
    "authstn"
})
public class TaxParty2 {

    @XmlElement(name = "TaxId")
    protected String taxId;
    @XmlElement(name = "RegnId")
    protected String regnId;
    @XmlElement(name = "TaxTp")
    protected String taxTp;
    @XmlElement(name = "Authstn")
    protected TaxAuthorisation1 authstn;

    /**
     * Ruft den Wert der taxId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxId() {
        return taxId;
    }

    /**
     * Legt den Wert der taxId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxId(String value) {
        this.taxId = value;
    }

    /**
     * Ruft den Wert der regnId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegnId() {
        return regnId;
    }

    /**
     * Legt den Wert der regnId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegnId(String value) {
        this.regnId = value;
    }

    /**
     * Ruft den Wert der taxTp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxTp() {
        return taxTp;
    }

    /**
     * Legt den Wert der taxTp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxTp(String value) {
        this.taxTp = value;
    }

    /**
     * Ruft den Wert der authstn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TaxAuthorisation1 }
     *     
     */
    public TaxAuthorisation1 getAuthstn() {
        return authstn;
    }

    /**
     * Legt den Wert der authstn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxAuthorisation1 }
     *     
     */
    public void setAuthstn(TaxAuthorisation1 value) {
        this.authstn = value;
    }

}
