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
 * <p>Java-Klasse für FinancialInstitutionIdentification7-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="FinancialInstitutionIdentification7-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BIC" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}BICIdentifier" minOccurs="0"/&gt;
 *         &lt;element name="ClrSysMmbId" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ClearingSystemMemberIdentification2" minOccurs="0"/&gt;
 *         &lt;element name="Nm" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}Max70Text" minOccurs="0"/&gt;
 *         &lt;element name="PstlAdr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PostalAddress6-CH" minOccurs="0"/&gt;
 *         &lt;element name="Othr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}GenericFinancialIdentification1-CH" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FinancialInstitutionIdentification7-CH", propOrder = {
    "bic",
    "clrSysMmbId",
    "nm",
    "pstlAdr",
    "othr"
})
public class FinancialInstitutionIdentification7CH {

    @XmlElement(name = "BIC")
    protected String bic;
    @XmlElement(name = "ClrSysMmbId")
    protected ClearingSystemMemberIdentification2 clrSysMmbId;
    @XmlElement(name = "Nm")
    protected String nm;
    @XmlElement(name = "PstlAdr")
    protected PostalAddress6CH pstlAdr;
    @XmlElement(name = "Othr")
    protected GenericFinancialIdentification1CH othr;

    /**
     * Ruft den Wert der bic-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBIC() {
        return bic;
    }

    /**
     * Legt den Wert der bic-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBIC(String value) {
        this.bic = value;
    }

    /**
     * Ruft den Wert der clrSysMmbId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ClearingSystemMemberIdentification2 }
     *     
     */
    public ClearingSystemMemberIdentification2 getClrSysMmbId() {
        return clrSysMmbId;
    }

    /**
     * Legt den Wert der clrSysMmbId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ClearingSystemMemberIdentification2 }
     *     
     */
    public void setClrSysMmbId(ClearingSystemMemberIdentification2 value) {
        this.clrSysMmbId = value;
    }

    /**
     * Ruft den Wert der nm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNm() {
        return nm;
    }

    /**
     * Legt den Wert der nm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNm(String value) {
        this.nm = value;
    }

    /**
     * Ruft den Wert der pstlAdr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PostalAddress6CH }
     *     
     */
    public PostalAddress6CH getPstlAdr() {
        return pstlAdr;
    }

    /**
     * Legt den Wert der pstlAdr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalAddress6CH }
     *     
     */
    public void setPstlAdr(PostalAddress6CH value) {
        this.pstlAdr = value;
    }

    /**
     * Ruft den Wert der othr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GenericFinancialIdentification1CH }
     *     
     */
    public GenericFinancialIdentification1CH getOthr() {
        return othr;
    }

    /**
     * Legt den Wert der othr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericFinancialIdentification1CH }
     *     
     */
    public void setOthr(GenericFinancialIdentification1CH value) {
        this.othr = value;
    }

}
