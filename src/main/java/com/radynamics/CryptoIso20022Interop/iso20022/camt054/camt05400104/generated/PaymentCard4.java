//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PaymentCard4 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PaymentCard4"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PlainCardData" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}PlainCardData1" minOccurs="0"/&gt;
 *         &lt;element name="CardCtryCd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Exact3NumericText" minOccurs="0"/&gt;
 *         &lt;element name="CardBrnd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}GenericIdentification1" minOccurs="0"/&gt;
 *         &lt;element name="AddtlCardData" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max70Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentCard4", propOrder = {
    "plainCardData",
    "cardCtryCd",
    "cardBrnd",
    "addtlCardData"
})
public class PaymentCard4 {

    @XmlElement(name = "PlainCardData")
    protected PlainCardData1 plainCardData;
    @XmlElement(name = "CardCtryCd")
    protected String cardCtryCd;
    @XmlElement(name = "CardBrnd")
    protected GenericIdentification1 cardBrnd;
    @XmlElement(name = "AddtlCardData")
    protected String addtlCardData;

    /**
     * Ruft den Wert der plainCardData-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PlainCardData1 }
     *     
     */
    public PlainCardData1 getPlainCardData() {
        return plainCardData;
    }

    /**
     * Legt den Wert der plainCardData-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PlainCardData1 }
     *     
     */
    public void setPlainCardData(PlainCardData1 value) {
        this.plainCardData = value;
    }

    /**
     * Ruft den Wert der cardCtryCd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardCtryCd() {
        return cardCtryCd;
    }

    /**
     * Legt den Wert der cardCtryCd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardCtryCd(String value) {
        this.cardCtryCd = value;
    }

    /**
     * Ruft den Wert der cardBrnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GenericIdentification1 }
     *     
     */
    public GenericIdentification1 getCardBrnd() {
        return cardBrnd;
    }

    /**
     * Legt den Wert der cardBrnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericIdentification1 }
     *     
     */
    public void setCardBrnd(GenericIdentification1 value) {
        this.cardBrnd = value;
    }

    /**
     * Ruft den Wert der addtlCardData-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddtlCardData() {
        return addtlCardData;
    }

    /**
     * Legt den Wert der addtlCardData-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddtlCardData(String value) {
        this.addtlCardData = value;
    }

}
