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
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für ReferredDocumentInformation3 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ReferredDocumentInformation3"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Tp" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ReferredDocumentType2" minOccurs="0"/&gt;
 *         &lt;element name="Nb" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="RltdDt" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ISODate" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferredDocumentInformation3", propOrder = {
    "tp",
    "nb",
    "rltdDt"
})
public class ReferredDocumentInformation3 {

    @XmlElement(name = "Tp")
    protected ReferredDocumentType2 tp;
    @XmlElement(name = "Nb")
    protected String nb;
    @XmlElement(name = "RltdDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar rltdDt;

    /**
     * Ruft den Wert der tp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReferredDocumentType2 }
     *     
     */
    public ReferredDocumentType2 getTp() {
        return tp;
    }

    /**
     * Legt den Wert der tp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferredDocumentType2 }
     *     
     */
    public void setTp(ReferredDocumentType2 value) {
        this.tp = value;
    }

    /**
     * Ruft den Wert der nb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNb() {
        return nb;
    }

    /**
     * Legt den Wert der nb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNb(String value) {
        this.nb = value;
    }

    /**
     * Ruft den Wert der rltdDt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRltdDt() {
        return rltdDt;
    }

    /**
     * Legt den Wert der rltdDt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRltdDt(XMLGregorianCalendar value) {
        this.rltdDt = value;
    }

}
