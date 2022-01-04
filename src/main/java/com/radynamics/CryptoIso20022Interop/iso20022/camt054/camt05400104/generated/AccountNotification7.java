//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.02.21 um 09:19:55 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für AccountNotification7 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="AccountNotification7"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max35Text"/&gt;
 *         &lt;element name="NtfctnPgntn" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Pagination" minOccurs="0"/&gt;
 *         &lt;element name="ElctrncSeqNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Number" minOccurs="0"/&gt;
 *         &lt;element name="LglSeqNb" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Number" minOccurs="0"/&gt;
 *         &lt;element name="CreDtTm" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ISODateTime"/&gt;
 *         &lt;element name="FrToDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}DateTimePeriodDetails" minOccurs="0"/&gt;
 *         &lt;element name="CpyDplctInd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CopyDuplicate1Code" minOccurs="0"/&gt;
 *         &lt;element name="RptgSrc" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ReportingSource1Choice" minOccurs="0"/&gt;
 *         &lt;element name="Acct" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CashAccount25"/&gt;
 *         &lt;element name="RltdAcct" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CashAccount24" minOccurs="0"/&gt;
 *         &lt;element name="Intrst" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}AccountInterest2" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TxsSummry" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TotalTransactions4" minOccurs="0"/&gt;
 *         &lt;element name="Ntry" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ReportEntry4" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="AddtlNtfctnInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max500Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountNotification7", propOrder = {
    "id",
    "ntfctnPgntn",
    "elctrncSeqNb",
    "lglSeqNb",
    "creDtTm",
    "frToDt",
    "cpyDplctInd",
    "rptgSrc",
    "acct",
    "rltdAcct",
    "intrst",
    "txsSummry",
    "ntry",
    "addtlNtfctnInf"
})
public class AccountNotification7 {

    @XmlElement(name = "Id", required = true)
    protected String id;
    @XmlElement(name = "NtfctnPgntn")
    protected Pagination ntfctnPgntn;
    @XmlElement(name = "ElctrncSeqNb")
    protected BigDecimal elctrncSeqNb;
    @XmlElement(name = "LglSeqNb")
    protected BigDecimal lglSeqNb;
    @XmlElement(name = "CreDtTm", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creDtTm;
    @XmlElement(name = "FrToDt")
    protected DateTimePeriodDetails frToDt;
    @XmlElement(name = "CpyDplctInd")
    @XmlSchemaType(name = "string")
    protected CopyDuplicate1Code cpyDplctInd;
    @XmlElement(name = "RptgSrc")
    protected ReportingSource1Choice rptgSrc;
    @XmlElement(name = "Acct", required = true)
    protected CashAccount25 acct;
    @XmlElement(name = "RltdAcct")
    protected CashAccount24 rltdAcct;
    @XmlElement(name = "Intrst")
    protected List<AccountInterest2> intrst;
    @XmlElement(name = "TxsSummry")
    protected TotalTransactions4 txsSummry;
    @XmlElement(name = "Ntry")
    protected List<ReportEntry4> ntry;
    @XmlElement(name = "AddtlNtfctnInf")
    protected String addtlNtfctnInf;

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der ntfctnPgntn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Pagination }
     *     
     */
    public Pagination getNtfctnPgntn() {
        return ntfctnPgntn;
    }

    /**
     * Legt den Wert der ntfctnPgntn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Pagination }
     *     
     */
    public void setNtfctnPgntn(Pagination value) {
        this.ntfctnPgntn = value;
    }

    /**
     * Ruft den Wert der elctrncSeqNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getElctrncSeqNb() {
        return elctrncSeqNb;
    }

    /**
     * Legt den Wert der elctrncSeqNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setElctrncSeqNb(BigDecimal value) {
        this.elctrncSeqNb = value;
    }

    /**
     * Ruft den Wert der lglSeqNb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLglSeqNb() {
        return lglSeqNb;
    }

    /**
     * Legt den Wert der lglSeqNb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLglSeqNb(BigDecimal value) {
        this.lglSeqNb = value;
    }

    /**
     * Ruft den Wert der creDtTm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreDtTm() {
        return creDtTm;
    }

    /**
     * Legt den Wert der creDtTm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreDtTm(XMLGregorianCalendar value) {
        this.creDtTm = value;
    }

    /**
     * Ruft den Wert der frToDt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DateTimePeriodDetails }
     *     
     */
    public DateTimePeriodDetails getFrToDt() {
        return frToDt;
    }

    /**
     * Legt den Wert der frToDt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DateTimePeriodDetails }
     *     
     */
    public void setFrToDt(DateTimePeriodDetails value) {
        this.frToDt = value;
    }

    /**
     * Ruft den Wert der cpyDplctInd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CopyDuplicate1Code }
     *     
     */
    public CopyDuplicate1Code getCpyDplctInd() {
        return cpyDplctInd;
    }

    /**
     * Legt den Wert der cpyDplctInd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CopyDuplicate1Code }
     *     
     */
    public void setCpyDplctInd(CopyDuplicate1Code value) {
        this.cpyDplctInd = value;
    }

    /**
     * Ruft den Wert der rptgSrc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReportingSource1Choice }
     *     
     */
    public ReportingSource1Choice getRptgSrc() {
        return rptgSrc;
    }

    /**
     * Legt den Wert der rptgSrc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportingSource1Choice }
     *     
     */
    public void setRptgSrc(ReportingSource1Choice value) {
        this.rptgSrc = value;
    }

    /**
     * Ruft den Wert der acct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount25 }
     *     
     */
    public CashAccount25 getAcct() {
        return acct;
    }

    /**
     * Legt den Wert der acct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount25 }
     *     
     */
    public void setAcct(CashAccount25 value) {
        this.acct = value;
    }

    /**
     * Ruft den Wert der rltdAcct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount24 }
     *     
     */
    public CashAccount24 getRltdAcct() {
        return rltdAcct;
    }

    /**
     * Legt den Wert der rltdAcct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount24 }
     *     
     */
    public void setRltdAcct(CashAccount24 value) {
        this.rltdAcct = value;
    }

    /**
     * Gets the value of the intrst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the intrst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIntrst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountInterest2 }
     * 
     * 
     */
    public List<AccountInterest2> getIntrst() {
        if (intrst == null) {
            intrst = new ArrayList<AccountInterest2>();
        }
        return this.intrst;
    }

    /**
     * Ruft den Wert der txsSummry-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TotalTransactions4 }
     *     
     */
    public TotalTransactions4 getTxsSummry() {
        return txsSummry;
    }

    /**
     * Legt den Wert der txsSummry-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TotalTransactions4 }
     *     
     */
    public void setTxsSummry(TotalTransactions4 value) {
        this.txsSummry = value;
    }

    /**
     * Gets the value of the ntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNtry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReportEntry4 }
     * 
     * 
     */
    public List<ReportEntry4> getNtry() {
        if (ntry == null) {
            ntry = new ArrayList<ReportEntry4>();
        }
        return this.ntry;
    }

    /**
     * Ruft den Wert der addtlNtfctnInf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddtlNtfctnInf() {
        return addtlNtfctnInf;
    }

    /**
     * Legt den Wert der addtlNtfctnInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddtlNtfctnInf(String value) {
        this.addtlNtfctnInf = value;
    }

}
