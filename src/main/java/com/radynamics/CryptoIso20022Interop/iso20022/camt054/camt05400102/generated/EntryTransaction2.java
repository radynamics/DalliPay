//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.02.10 um 09:17:05 AM CET 
//


package com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EntryTransaction2 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EntryTransaction2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Refs" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionReferences2" minOccurs="0"/&gt;
 *         &lt;element name="AmtDtls" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}AmountAndCurrencyExchange3" minOccurs="0"/&gt;
 *         &lt;element name="Avlbty" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}CashBalanceAvailability2" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="BkTxCd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}BankTransactionCodeStructure4" minOccurs="0"/&gt;
 *         &lt;element name="Chrgs" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}ChargesInformation6" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Intrst" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionInterest2" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="RltdPties" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionParty2" minOccurs="0"/&gt;
 *         &lt;element name="RltdAgts" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionAgents2" minOccurs="0"/&gt;
 *         &lt;element name="Purp" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}Purpose2Choice" minOccurs="0"/&gt;
 *         &lt;element name="RltdRmtInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}RemittanceLocation2" maxOccurs="10" minOccurs="0"/&gt;
 *         &lt;element name="RmtInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}RemittanceInformation5" minOccurs="0"/&gt;
 *         &lt;element name="RltdDts" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionDates2" minOccurs="0"/&gt;
 *         &lt;element name="RltdPric" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionPrice2Choice" minOccurs="0"/&gt;
 *         &lt;element name="RltdQties" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TransactionQuantities1Choice" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="FinInstrmId" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}SecurityIdentification4Choice" minOccurs="0"/&gt;
 *         &lt;element name="Tax" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}TaxInformation3" minOccurs="0"/&gt;
 *         &lt;element name="RtrInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}ReturnReasonInformation10" minOccurs="0"/&gt;
 *         &lt;element name="CorpActn" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}CorporateAction1" minOccurs="0"/&gt;
 *         &lt;element name="SfkpgAcct" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}CashAccount16" minOccurs="0"/&gt;
 *         &lt;element name="AddtlTxInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.02}Max500Text" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntryTransaction2", propOrder = {
    "refs",
    "amtDtls",
    "avlbty",
    "bkTxCd",
    "chrgs",
    "intrst",
    "rltdPties",
    "rltdAgts",
    "purp",
    "rltdRmtInf",
    "rmtInf",
    "rltdDts",
    "rltdPric",
    "rltdQties",
    "finInstrmId",
    "tax",
    "rtrInf",
    "corpActn",
    "sfkpgAcct",
    "addtlTxInf"
})
public class EntryTransaction2 {

    @XmlElement(name = "Refs")
    protected TransactionReferences2 refs;
    @XmlElement(name = "AmtDtls")
    protected AmountAndCurrencyExchange3 amtDtls;
    @XmlElement(name = "Avlbty")
    protected List<CashBalanceAvailability2> avlbty;
    @XmlElement(name = "BkTxCd")
    protected BankTransactionCodeStructure4 bkTxCd;
    @XmlElement(name = "Chrgs")
    protected List<ChargesInformation6> chrgs;
    @XmlElement(name = "Intrst")
    protected List<TransactionInterest2> intrst;
    @XmlElement(name = "RltdPties")
    protected TransactionParty2 rltdPties;
    @XmlElement(name = "RltdAgts")
    protected TransactionAgents2 rltdAgts;
    @XmlElement(name = "Purp")
    protected Purpose2Choice purp;
    @XmlElement(name = "RltdRmtInf")
    protected List<RemittanceLocation2> rltdRmtInf;
    @XmlElement(name = "RmtInf")
    protected RemittanceInformation5 rmtInf;
    @XmlElement(name = "RltdDts")
    protected TransactionDates2 rltdDts;
    @XmlElement(name = "RltdPric")
    protected TransactionPrice2Choice rltdPric;
    @XmlElement(name = "RltdQties")
    protected List<TransactionQuantities1Choice> rltdQties;
    @XmlElement(name = "FinInstrmId")
    protected SecurityIdentification4Choice finInstrmId;
    @XmlElement(name = "Tax")
    protected TaxInformation3 tax;
    @XmlElement(name = "RtrInf")
    protected ReturnReasonInformation10 rtrInf;
    @XmlElement(name = "CorpActn")
    protected CorporateAction1 corpActn;
    @XmlElement(name = "SfkpgAcct")
    protected CashAccount16 sfkpgAcct;
    @XmlElement(name = "AddtlTxInf")
    protected String addtlTxInf;

    /**
     * Ruft den Wert der refs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionReferences2 }
     *     
     */
    public TransactionReferences2 getRefs() {
        return refs;
    }

    /**
     * Legt den Wert der refs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionReferences2 }
     *     
     */
    public void setRefs(TransactionReferences2 value) {
        this.refs = value;
    }

    /**
     * Ruft den Wert der amtDtls-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AmountAndCurrencyExchange3 }
     *     
     */
    public AmountAndCurrencyExchange3 getAmtDtls() {
        return amtDtls;
    }

    /**
     * Legt den Wert der amtDtls-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AmountAndCurrencyExchange3 }
     *     
     */
    public void setAmtDtls(AmountAndCurrencyExchange3 value) {
        this.amtDtls = value;
    }

    /**
     * Gets the value of the avlbty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the avlbty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAvlbty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CashBalanceAvailability2 }
     * 
     * 
     */
    public List<CashBalanceAvailability2> getAvlbty() {
        if (avlbty == null) {
            avlbty = new ArrayList<CashBalanceAvailability2>();
        }
        return this.avlbty;
    }

    /**
     * Ruft den Wert der bkTxCd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BankTransactionCodeStructure4 }
     *     
     */
    public BankTransactionCodeStructure4 getBkTxCd() {
        return bkTxCd;
    }

    /**
     * Legt den Wert der bkTxCd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BankTransactionCodeStructure4 }
     *     
     */
    public void setBkTxCd(BankTransactionCodeStructure4 value) {
        this.bkTxCd = value;
    }

    /**
     * Gets the value of the chrgs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chrgs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChrgs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChargesInformation6 }
     * 
     * 
     */
    public List<ChargesInformation6> getChrgs() {
        if (chrgs == null) {
            chrgs = new ArrayList<ChargesInformation6>();
        }
        return this.chrgs;
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
     * {@link TransactionInterest2 }
     * 
     * 
     */
    public List<TransactionInterest2> getIntrst() {
        if (intrst == null) {
            intrst = new ArrayList<TransactionInterest2>();
        }
        return this.intrst;
    }

    /**
     * Ruft den Wert der rltdPties-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionParty2 }
     *     
     */
    public TransactionParty2 getRltdPties() {
        return rltdPties;
    }

    /**
     * Legt den Wert der rltdPties-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionParty2 }
     *     
     */
    public void setRltdPties(TransactionParty2 value) {
        this.rltdPties = value;
    }

    /**
     * Ruft den Wert der rltdAgts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionAgents2 }
     *     
     */
    public TransactionAgents2 getRltdAgts() {
        return rltdAgts;
    }

    /**
     * Legt den Wert der rltdAgts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionAgents2 }
     *     
     */
    public void setRltdAgts(TransactionAgents2 value) {
        this.rltdAgts = value;
    }

    /**
     * Ruft den Wert der purp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Purpose2Choice }
     *     
     */
    public Purpose2Choice getPurp() {
        return purp;
    }

    /**
     * Legt den Wert der purp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Purpose2Choice }
     *     
     */
    public void setPurp(Purpose2Choice value) {
        this.purp = value;
    }

    /**
     * Gets the value of the rltdRmtInf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rltdRmtInf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRltdRmtInf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemittanceLocation2 }
     * 
     * 
     */
    public List<RemittanceLocation2> getRltdRmtInf() {
        if (rltdRmtInf == null) {
            rltdRmtInf = new ArrayList<RemittanceLocation2>();
        }
        return this.rltdRmtInf;
    }

    /**
     * Ruft den Wert der rmtInf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RemittanceInformation5 }
     *     
     */
    public RemittanceInformation5 getRmtInf() {
        return rmtInf;
    }

    /**
     * Legt den Wert der rmtInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceInformation5 }
     *     
     */
    public void setRmtInf(RemittanceInformation5 value) {
        this.rmtInf = value;
    }

    /**
     * Ruft den Wert der rltdDts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionDates2 }
     *     
     */
    public TransactionDates2 getRltdDts() {
        return rltdDts;
    }

    /**
     * Legt den Wert der rltdDts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionDates2 }
     *     
     */
    public void setRltdDts(TransactionDates2 value) {
        this.rltdDts = value;
    }

    /**
     * Ruft den Wert der rltdPric-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionPrice2Choice }
     *     
     */
    public TransactionPrice2Choice getRltdPric() {
        return rltdPric;
    }

    /**
     * Legt den Wert der rltdPric-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionPrice2Choice }
     *     
     */
    public void setRltdPric(TransactionPrice2Choice value) {
        this.rltdPric = value;
    }

    /**
     * Gets the value of the rltdQties property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rltdQties property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRltdQties().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransactionQuantities1Choice }
     * 
     * 
     */
    public List<TransactionQuantities1Choice> getRltdQties() {
        if (rltdQties == null) {
            rltdQties = new ArrayList<TransactionQuantities1Choice>();
        }
        return this.rltdQties;
    }

    /**
     * Ruft den Wert der finInstrmId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SecurityIdentification4Choice }
     *     
     */
    public SecurityIdentification4Choice getFinInstrmId() {
        return finInstrmId;
    }

    /**
     * Legt den Wert der finInstrmId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityIdentification4Choice }
     *     
     */
    public void setFinInstrmId(SecurityIdentification4Choice value) {
        this.finInstrmId = value;
    }

    /**
     * Ruft den Wert der tax-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TaxInformation3 }
     *     
     */
    public TaxInformation3 getTax() {
        return tax;
    }

    /**
     * Legt den Wert der tax-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TaxInformation3 }
     *     
     */
    public void setTax(TaxInformation3 value) {
        this.tax = value;
    }

    /**
     * Ruft den Wert der rtrInf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReturnReasonInformation10 }
     *     
     */
    public ReturnReasonInformation10 getRtrInf() {
        return rtrInf;
    }

    /**
     * Legt den Wert der rtrInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnReasonInformation10 }
     *     
     */
    public void setRtrInf(ReturnReasonInformation10 value) {
        this.rtrInf = value;
    }

    /**
     * Ruft den Wert der corpActn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CorporateAction1 }
     *     
     */
    public CorporateAction1 getCorpActn() {
        return corpActn;
    }

    /**
     * Legt den Wert der corpActn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CorporateAction1 }
     *     
     */
    public void setCorpActn(CorporateAction1 value) {
        this.corpActn = value;
    }

    /**
     * Ruft den Wert der sfkpgAcct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount16 }
     *     
     */
    public CashAccount16 getSfkpgAcct() {
        return sfkpgAcct;
    }

    /**
     * Legt den Wert der sfkpgAcct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount16 }
     *     
     */
    public void setSfkpgAcct(CashAccount16 value) {
        this.sfkpgAcct = value;
    }

    /**
     * Ruft den Wert der addtlTxInf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddtlTxInf() {
        return addtlTxInf;
    }

    /**
     * Legt den Wert der addtlTxInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddtlTxInf(String value) {
        this.addtlTxInf = value;
    }

}
