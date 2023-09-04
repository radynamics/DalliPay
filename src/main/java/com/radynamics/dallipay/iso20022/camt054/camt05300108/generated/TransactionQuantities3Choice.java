//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.04 um 04:37:51 PM CEST 
//


package com.radynamics.dallipay.iso20022.camt054.camt05300108.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für TransactionQuantities3Choice complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TransactionQuantities3Choice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Qty" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.08}FinancialInstrumentQuantity1Choice"/&gt;
 *         &lt;element name="OrgnlAndCurFaceAmt" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.08}OriginalAndCurrentQuantities1"/&gt;
 *         &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.08}ProprietaryQuantity1"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionQuantities3Choice", propOrder = {
    "qty",
    "orgnlAndCurFaceAmt",
    "prtry"
})
public class TransactionQuantities3Choice {

    @XmlElement(name = "Qty")
    protected FinancialInstrumentQuantity1Choice qty;
    @XmlElement(name = "OrgnlAndCurFaceAmt")
    protected OriginalAndCurrentQuantities1 orgnlAndCurFaceAmt;
    @XmlElement(name = "Prtry")
    protected ProprietaryQuantity1 prtry;

    /**
     * Ruft den Wert der qty-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FinancialInstrumentQuantity1Choice }
     *     
     */
    public FinancialInstrumentQuantity1Choice getQty() {
        return qty;
    }

    /**
     * Legt den Wert der qty-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FinancialInstrumentQuantity1Choice }
     *     
     */
    public void setQty(FinancialInstrumentQuantity1Choice value) {
        this.qty = value;
    }

    /**
     * Ruft den Wert der orgnlAndCurFaceAmt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OriginalAndCurrentQuantities1 }
     *     
     */
    public OriginalAndCurrentQuantities1 getOrgnlAndCurFaceAmt() {
        return orgnlAndCurFaceAmt;
    }

    /**
     * Legt den Wert der orgnlAndCurFaceAmt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OriginalAndCurrentQuantities1 }
     *     
     */
    public void setOrgnlAndCurFaceAmt(OriginalAndCurrentQuantities1 value) {
        this.orgnlAndCurFaceAmt = value;
    }

    /**
     * Ruft den Wert der prtry-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProprietaryQuantity1 }
     *     
     */
    public ProprietaryQuantity1 getPrtry() {
        return prtry;
    }

    /**
     * Legt den Wert der prtry-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProprietaryQuantity1 }
     *     
     */
    public void setPrtry(ProprietaryQuantity1 value) {
        this.prtry = value;
    }

}
