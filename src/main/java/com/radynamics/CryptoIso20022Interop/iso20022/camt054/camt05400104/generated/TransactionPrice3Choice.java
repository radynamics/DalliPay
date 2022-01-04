//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TransactionPrice3Choice complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TransactionPrice3Choice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="DealPric" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Price2"/&gt;
 *         &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ProprietaryPrice2" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionPrice3Choice", propOrder = {
    "dealPric",
    "prtry"
})
public class TransactionPrice3Choice {

    @XmlElement(name = "DealPric")
    protected Price2 dealPric;
    @XmlElement(name = "Prtry")
    protected List<ProprietaryPrice2> prtry;

    /**
     * Ruft den Wert der dealPric-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Price2 }
     *     
     */
    public Price2 getDealPric() {
        return dealPric;
    }

    /**
     * Legt den Wert der dealPric-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Price2 }
     *     
     */
    public void setDealPric(Price2 value) {
        this.dealPric = value;
    }

    /**
     * Gets the value of the prtry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the prtry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrtry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProprietaryPrice2 }
     * 
     * 
     */
    public List<ProprietaryPrice2> getPrtry() {
        if (prtry == null) {
            prtry = new ArrayList<ProprietaryPrice2>();
        }
        return this.prtry;
    }

}
