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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Cheque6-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Cheque6-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ChqTp" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ChequeType2Code" minOccurs="0"/&gt;
 *         &lt;element name="DlvryMtd" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ChequeDeliveryMethod1Choice" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cheque6-CH", propOrder = {
    "chqTp",
    "dlvryMtd"
})
public class Cheque6CH {

    @XmlElement(name = "ChqTp")
    @XmlSchemaType(name = "string")
    protected ChequeType2Code chqTp;
    @XmlElement(name = "DlvryMtd")
    protected ChequeDeliveryMethod1Choice dlvryMtd;

    /**
     * Ruft den Wert der chqTp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ChequeType2Code }
     *     
     */
    public ChequeType2Code getChqTp() {
        return chqTp;
    }

    /**
     * Legt den Wert der chqTp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ChequeType2Code }
     *     
     */
    public void setChqTp(ChequeType2Code value) {
        this.chqTp = value;
    }

    /**
     * Ruft den Wert der dlvryMtd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ChequeDeliveryMethod1Choice }
     *     
     */
    public ChequeDeliveryMethod1Choice getDlvryMtd() {
        return dlvryMtd;
    }

    /**
     * Legt den Wert der dlvryMtd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ChequeDeliveryMethod1Choice }
     *     
     */
    public void setDlvryMtd(ChequeDeliveryMethod1Choice value) {
        this.dlvryMtd = value;
    }

}
