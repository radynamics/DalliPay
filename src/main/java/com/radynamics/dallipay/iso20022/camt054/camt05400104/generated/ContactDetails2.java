//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.dallipay.iso20022.camt054.camt05400104.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ContactDetails2 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ContactDetails2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="NmPrfx" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}NamePrefix1Code" minOccurs="0"/&gt;
 *         &lt;element name="Nm" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max140Text" minOccurs="0"/&gt;
 *         &lt;element name="PhneNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}PhoneNumber" minOccurs="0"/&gt;
 *         &lt;element name="MobNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}PhoneNumber" minOccurs="0"/&gt;
 *         &lt;element name="FaxNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}PhoneNumber" minOccurs="0"/&gt;
 *         &lt;element name="EmailAdr" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max2048Text" minOccurs="0"/&gt;
 *         &lt;element name="Othr" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactDetails2", propOrder = {
    "nmPrfx",
    "nm",
    "phneNb",
    "mobNb",
    "faxNb",
    "emailAdr",
    "othr"
})
public class ContactDetails2 {

    @XmlElement(name = "NmPrfx")
    @XmlSchemaType(name = "string")
    protected NamePrefix1Code nmPrfx;
    @XmlElement(name = "Nm")
    protected String nm;
    @XmlElement(name = "PhneNb")
    protected String phneNb;
    @XmlElement(name = "MobNb")
    protected String mobNb;
    @XmlElement(name = "FaxNb")
    protected String faxNb;
    @XmlElement(name = "EmailAdr")
    protected String emailAdr;
    @XmlElement(name = "Othr")
    protected String othr;

    /**
     * Ruft den Wert der nmPrfx-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NamePrefix1Code }
     *     
     */
    public NamePrefix1Code getNmPrfx() {
        return nmPrfx;
    }

    /**
     * Legt den Wert der nmPrfx-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NamePrefix1Code }
     *     
     */
    public void setNmPrfx(NamePrefix1Code value) {
        this.nmPrfx = value;
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
     * Ruft den Wert der phneNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhneNb() {
        return phneNb;
    }

    /**
     * Legt den Wert der phneNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhneNb(String value) {
        this.phneNb = value;
    }

    /**
     * Ruft den Wert der mobNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobNb() {
        return mobNb;
    }

    /**
     * Legt den Wert der mobNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobNb(String value) {
        this.mobNb = value;
    }

    /**
     * Ruft den Wert der faxNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaxNb() {
        return faxNb;
    }

    /**
     * Legt den Wert der faxNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaxNb(String value) {
        this.faxNb = value;
    }

    /**
     * Ruft den Wert der emailAdr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAdr() {
        return emailAdr;
    }

    /**
     * Legt den Wert der emailAdr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAdr(String value) {
        this.emailAdr = value;
    }

    /**
     * Ruft den Wert der othr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOthr() {
        return othr;
    }

    /**
     * Legt den Wert der othr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOthr(String value) {
        this.othr = value;
    }

}
