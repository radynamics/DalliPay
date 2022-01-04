//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import javax.xml.bind.annotation.*;


/**
 * <p>Java-Klasse f�r Document complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Document"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="BkToCstmrDbtCdtNtfctn" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}BankToCustomerDebitCreditNotificationV04"/&gt;
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
    "bkToCstmrDbtCdtNtfctn"
})
@XmlRootElement(name="Document")
public class Document {

    @XmlElement(name = "BkToCstmrDbtCdtNtfctn", required = true)
    protected BankToCustomerDebitCreditNotificationV04 bkToCstmrDbtCdtNtfctn;

    /**
     * Ruft den Wert der bkToCstmrDbtCdtNtfctn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BankToCustomerDebitCreditNotificationV04 }
     *     
     */
    public BankToCustomerDebitCreditNotificationV04 getBkToCstmrDbtCdtNtfctn() {
        return bkToCstmrDbtCdtNtfctn;
    }

    /**
     * Legt den Wert der bkToCstmrDbtCdtNtfctn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BankToCustomerDebitCreditNotificationV04 }
     *     
     */
    public void setBkToCstmrDbtCdtNtfctn(BankToCustomerDebitCreditNotificationV04 value) {
        this.bkToCstmrDbtCdtNtfctn = value;
    }

}
