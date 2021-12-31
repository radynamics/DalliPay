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
 * <p>Java-Klasse für AmountType3Choice complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AmountType3Choice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="InstdAmt" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ActiveOrHistoricCurrencyAndAmount"/&gt;
 *           &lt;element name="EqvtAmt" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}EquivalentAmount2"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AmountType3Choice", propOrder = {
    "instdAmt",
    "eqvtAmt"
})
public class AmountType3Choice {

    @XmlElement(name = "InstdAmt")
    protected ActiveOrHistoricCurrencyAndAmount instdAmt;
    @XmlElement(name = "EqvtAmt")
    protected EquivalentAmount2 eqvtAmt;

    /**
     * Ruft den Wert der instdAmt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActiveOrHistoricCurrencyAndAmount }
     *     
     */
    public ActiveOrHistoricCurrencyAndAmount getInstdAmt() {
        return instdAmt;
    }

    /**
     * Legt den Wert der instdAmt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActiveOrHistoricCurrencyAndAmount }
     *     
     */
    public void setInstdAmt(ActiveOrHistoricCurrencyAndAmount value) {
        this.instdAmt = value;
    }

    /**
     * Ruft den Wert der eqvtAmt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EquivalentAmount2 }
     *     
     */
    public EquivalentAmount2 getEqvtAmt() {
        return eqvtAmt;
    }

    /**
     * Legt den Wert der eqvtAmt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EquivalentAmount2 }
     *     
     */
    public void setEqvtAmt(EquivalentAmount2 value) {
        this.eqvtAmt = value;
    }

}
