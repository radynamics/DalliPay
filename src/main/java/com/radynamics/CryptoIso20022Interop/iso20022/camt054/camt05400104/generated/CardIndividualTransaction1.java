//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für CardIndividualTransaction1 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CardIndividualTransaction1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AddtlSvc" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CardPaymentServiceType2Code" minOccurs="0"/&gt;
 *         &lt;element name="TxCtgy" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ExternalCardTransactionCategory1Code" minOccurs="0"/&gt;
 *         &lt;element name="SaleRcncltnId" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="SaleRefNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="SeqNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text" minOccurs="0"/&gt;
 *         &lt;element name="TxId" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionIdentifier1" minOccurs="0"/&gt;
 *         &lt;element name="Pdct" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Product2" minOccurs="0"/&gt;
 *         &lt;element name="VldtnDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ISODate" minOccurs="0"/&gt;
 *         &lt;element name="VldtnSeqNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardIndividualTransaction1", propOrder = {
    "addtlSvc",
    "txCtgy",
    "saleRcncltnId",
    "saleRefNb",
    "seqNb",
    "txId",
    "pdct",
    "vldtnDt",
    "vldtnSeqNb"
})
public class CardIndividualTransaction1 {

    @XmlElement(name = "AddtlSvc")
    @XmlSchemaType(name = "string")
    protected CardPaymentServiceType2Code addtlSvc;
    @XmlElement(name = "TxCtgy")
    protected String txCtgy;
    @XmlElement(name = "SaleRcncltnId")
    protected String saleRcncltnId;
    @XmlElement(name = "SaleRefNb")
    protected String saleRefNb;
    @XmlElement(name = "SeqNb")
    protected String seqNb;
    @XmlElement(name = "TxId")
    protected TransactionIdentifier1 txId;
    @XmlElement(name = "Pdct")
    protected Product2 pdct;
    @XmlElement(name = "VldtnDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar vldtnDt;
    @XmlElement(name = "VldtnSeqNb")
    protected String vldtnSeqNb;

    /**
     * Ruft den Wert der addtlSvc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CardPaymentServiceType2Code }
     *     
     */
    public CardPaymentServiceType2Code getAddtlSvc() {
        return addtlSvc;
    }

    /**
     * Legt den Wert der addtlSvc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CardPaymentServiceType2Code }
     *     
     */
    public void setAddtlSvc(CardPaymentServiceType2Code value) {
        this.addtlSvc = value;
    }

    /**
     * Ruft den Wert der txCtgy-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTxCtgy() {
        return txCtgy;
    }

    /**
     * Legt den Wert der txCtgy-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTxCtgy(String value) {
        this.txCtgy = value;
    }

    /**
     * Ruft den Wert der saleRcncltnId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaleRcncltnId() {
        return saleRcncltnId;
    }

    /**
     * Legt den Wert der saleRcncltnId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaleRcncltnId(String value) {
        this.saleRcncltnId = value;
    }

    /**
     * Ruft den Wert der saleRefNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaleRefNb() {
        return saleRefNb;
    }

    /**
     * Legt den Wert der saleRefNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaleRefNb(String value) {
        this.saleRefNb = value;
    }

    /**
     * Ruft den Wert der seqNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeqNb() {
        return seqNb;
    }

    /**
     * Legt den Wert der seqNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeqNb(String value) {
        this.seqNb = value;
    }

    /**
     * Ruft den Wert der txId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionIdentifier1 }
     *     
     */
    public TransactionIdentifier1 getTxId() {
        return txId;
    }

    /**
     * Legt den Wert der txId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionIdentifier1 }
     *     
     */
    public void setTxId(TransactionIdentifier1 value) {
        this.txId = value;
    }

    /**
     * Ruft den Wert der pdct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Product2 }
     *     
     */
    public Product2 getPdct() {
        return pdct;
    }

    /**
     * Legt den Wert der pdct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Product2 }
     *     
     */
    public void setPdct(Product2 value) {
        this.pdct = value;
    }

    /**
     * Ruft den Wert der vldtnDt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getVldtnDt() {
        return vldtnDt;
    }

    /**
     * Legt den Wert der vldtnDt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setVldtnDt(XMLGregorianCalendar value) {
        this.vldtnDt = value;
    }

    /**
     * Ruft den Wert der vldtnSeqNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVldtnSeqNb() {
        return vldtnSeqNb;
    }

    /**
     * Legt den Wert der vldtnSeqNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVldtnSeqNb(String value) {
        this.vldtnSeqNb = value;
    }

}
