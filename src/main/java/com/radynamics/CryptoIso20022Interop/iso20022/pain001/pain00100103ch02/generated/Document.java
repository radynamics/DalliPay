//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.05.28 um 01:45:09 PM CEST 
//


package com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103ch02.generated;

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
 *         &lt;element name="CstmrCdtTrfInitn" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}CustomerCreditTransferInitiationV03-CH"/&gt;
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
    "cstmrCdtTrfInitn"
})
@XmlRootElement(name="Document")
public class Document {

    @XmlElement(name = "CstmrCdtTrfInitn", required = true)
    protected CustomerCreditTransferInitiationV03CH cstmrCdtTrfInitn;

    /**
     * Ruft den Wert der cstmrCdtTrfInitn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CustomerCreditTransferInitiationV03CH }
     *     
     */
    public CustomerCreditTransferInitiationV03CH getCstmrCdtTrfInitn() {
        return cstmrCdtTrfInitn;
    }

    /**
     * Legt den Wert der cstmrCdtTrfInitn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomerCreditTransferInitiationV03CH }
     *     
     */
    public void setCstmrCdtTrfInitn(CustomerCreditTransferInitiationV03CH value) {
        this.cstmrCdtTrfInitn = value;
    }

}
