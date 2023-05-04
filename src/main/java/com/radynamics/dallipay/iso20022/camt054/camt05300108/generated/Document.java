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
 * <p>Java-Klasse für Document complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Document"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BkToCstmrStmt" type="{urn:iso:std:iso:20022:tech:xsd:camt.053.001.08}BankToCustomerStatementV08"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Document", propOrder = {
    "bkToCstmrStmt"
})
public class Document {

    @XmlElement(name = "BkToCstmrStmt", required = true)
    protected BankToCustomerStatementV08 bkToCstmrStmt;

    /**
     * Ruft den Wert der bkToCstmrStmt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BankToCustomerStatementV08 }
     *     
     */
    public BankToCustomerStatementV08 getBkToCstmrStmt() {
        return bkToCstmrStmt;
    }

    /**
     * Legt den Wert der bkToCstmrStmt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BankToCustomerStatementV08 }
     *     
     */
    public void setBkToCstmrStmt(BankToCustomerStatementV08 value) {
        this.bkToCstmrStmt = value;
    }

}
