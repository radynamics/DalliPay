//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.03.28 um 11:20:19 AM CEST 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für SequenceRange1Choice complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="SequenceRange1Choice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="FrSeq" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max35Text"/&gt;
 *         &lt;element name="ToSeq" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max35Text"/&gt;
 *         &lt;element name="FrToSeq" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}SequenceRange1" maxOccurs="unbounded"/&gt;
 *         &lt;element name="EQSeq" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max35Text" maxOccurs="unbounded"/&gt;
 *         &lt;element name="NEQSeq" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.09}Max35Text" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SequenceRange1Choice", propOrder = {
    "frSeq",
    "toSeq",
    "frToSeq",
    "eqSeq",
    "neqSeq"
})
public class SequenceRange1Choice {

    @XmlElement(name = "FrSeq")
    protected String frSeq;
    @XmlElement(name = "ToSeq")
    protected String toSeq;
    @XmlElement(name = "FrToSeq")
    protected List<SequenceRange1> frToSeq;
    @XmlElement(name = "EQSeq")
    protected List<String> eqSeq;
    @XmlElement(name = "NEQSeq")
    protected List<String> neqSeq;

    /**
     * Ruft den Wert der frSeq-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFrSeq() {
        return frSeq;
    }

    /**
     * Legt den Wert der frSeq-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrSeq(String value) {
        this.frSeq = value;
    }

    /**
     * Ruft den Wert der toSeq-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToSeq() {
        return toSeq;
    }

    /**
     * Legt den Wert der toSeq-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToSeq(String value) {
        this.toSeq = value;
    }

    /**
     * Gets the value of the frToSeq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the frToSeq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFrToSeq().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SequenceRange1 }
     * 
     * 
     */
    public List<SequenceRange1> getFrToSeq() {
        if (frToSeq == null) {
            frToSeq = new ArrayList<SequenceRange1>();
        }
        return this.frToSeq;
    }

    /**
     * Gets the value of the eqSeq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eqSeq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEQSeq().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getEQSeq() {
        if (eqSeq == null) {
            eqSeq = new ArrayList<String>();
        }
        return this.eqSeq;
    }

    /**
     * Gets the value of the neqSeq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the neqSeq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNEQSeq().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNEQSeq() {
        if (neqSeq == null) {
            neqSeq = new ArrayList<String>();
        }
        return this.neqSeq;
    }

}
