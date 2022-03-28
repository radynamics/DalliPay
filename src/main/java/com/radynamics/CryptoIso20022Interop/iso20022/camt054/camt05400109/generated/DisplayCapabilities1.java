//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.03.28 um 11:20:19 AM CEST 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für DisplayCapabilities1 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DisplayCapabilities1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DispTp" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}UserInterface2Code"/&gt;
 *         &lt;element name="NbOfLines" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max3NumericText"/&gt;
 *         &lt;element name="LineWidth" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max3NumericText"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DisplayCapabilities1", propOrder = {
    "dispTp",
    "nbOfLines",
    "lineWidth"
})
public class DisplayCapabilities1 {

    @XmlElement(name = "DispTp", required = true)
    @XmlSchemaType(name = "string")
    protected UserInterface2Code dispTp;
    @XmlElement(name = "NbOfLines", required = true)
    protected String nbOfLines;
    @XmlElement(name = "LineWidth", required = true)
    protected String lineWidth;

    /**
     * Ruft den Wert der dispTp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserInterface2Code }
     *     
     */
    public UserInterface2Code getDispTp() {
        return dispTp;
    }

    /**
     * Legt den Wert der dispTp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInterface2Code }
     *     
     */
    public void setDispTp(UserInterface2Code value) {
        this.dispTp = value;
    }

    /**
     * Ruft den Wert der nbOfLines-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNbOfLines() {
        return nbOfLines;
    }

    /**
     * Legt den Wert der nbOfLines-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNbOfLines(String value) {
        this.nbOfLines = value;
    }

    /**
     * Ruft den Wert der lineWidth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineWidth() {
        return lineWidth;
    }

    /**
     * Legt den Wert der lineWidth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineWidth(String value) {
        this.lineWidth = value;
    }

}
