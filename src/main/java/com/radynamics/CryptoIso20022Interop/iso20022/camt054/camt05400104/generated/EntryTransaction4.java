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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EntryTransaction4 complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EntryTransaction4"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Refs" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionReferences3" minOccurs="0"/&gt;
 *         &lt;element name="Amt" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}ActiveOrHistoricCurrencyAndAmount"/&gt;
 *         &lt;element name="CdtDbtInd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CreditDebitCode"/&gt;
 *         &lt;element name="AmtDtls" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}AmountAndCurrencyExchange3" minOccurs="0"/&gt;
 *         &lt;element name="Avlbty" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CashBalanceAvailability2" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="BkTxCd" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}BankTransactionCodeStructure4" minOccurs="0"/&gt;
 *         &lt;element name="Chrgs" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Charges4" minOccurs="0"/&gt;
 *         &lt;element name="Intrst" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionInterest3" minOccurs="0"/&gt;
 *         &lt;element name="RltdPties" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionParties3" minOccurs="0"/&gt;
 *         &lt;element name="RltdAgts" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionAgents3" minOccurs="0"/&gt;
 *         &lt;element name="Purp" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Purpose2Choice" minOccurs="0"/&gt;
 *         &lt;element name="RltdRmtInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}RemittanceLocation2" maxOccurs="10" minOccurs="0"/&gt;
 *         &lt;element name="RmtInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}RemittanceInformation7" minOccurs="0"/&gt;
 *         &lt;element name="RltdDts" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionDates2" minOccurs="0"/&gt;
 *         &lt;element name="RltdPric" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionPrice3Choice" minOccurs="0"/&gt;
 *         &lt;element name="RltdQties" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TransactionQuantities2Choice" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="FinInstrmId" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}SecurityIdentification14" minOccurs="0"/&gt;
 *         &lt;element name="Tax" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}TaxInformation3" minOccurs="0"/&gt;
 *         &lt;element name="RtrInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}PaymentReturnReason2" minOccurs="0"/&gt;
 *         &lt;element name="CorpActn" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CorporateAction9" minOccurs="0"/&gt;
 *         &lt;element name="SfkpgAcct" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}SecuritiesAccount13" minOccurs="0"/&gt;
 *         &lt;element name="CshDpst" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CashDeposit1" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="CardTx" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}CardTransaction1" minOccurs="0"/&gt;
 *         &lt;element name="AddtlTxInf" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}Max500Text" minOccurs="0"/&gt;
 *         &lt;element name="SplmtryData" type="{urn:iso:std:iso:20022:tech:xsd:camt.054.001.04}SupplementaryData1" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntryTransaction4", propOrder = {
    "refs",
    "amt",
    "cdtDbtInd",
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
    "cshDpst",
    "cardTx",
    "addtlTxInf",
    "splmtryData"
})
public class EntryTransaction4 {

    @XmlElement(name = "Refs")
    protected TransactionReferences3 refs;
    @XmlElement(name = "Amt", required = true)
    protected ActiveOrHistoricCurrencyAndAmount amt;
    @XmlElement(name = "CdtDbtInd", required = true)
    @XmlSchemaType(name = "string")
    protected CreditDebitCode cdtDbtInd;
    @XmlElement(name = "AmtDtls")
    protected AmountAndCurrencyExchange3 amtDtls;
    @XmlElement(name = "Avlbty")
    protected List<CashBalanceAvailability2> avlbty;
    @XmlElement(name = "BkTxCd")
    protected BankTransactionCodeStructure4 bkTxCd;
    @XmlElement(name = "Chrgs")
    protected Charges4 chrgs;
    @XmlElement(name = "Intrst")
    protected TransactionInterest3 intrst;
    @XmlElement(name = "RltdPties")
    protected TransactionParties3 rltdPties;
    @XmlElement(name = "RltdAgts")
    protected TransactionAgents3 rltdAgts;
    @XmlElement(name = "Purp")
    protected Purpose2Choice purp;
    @XmlElement(name = "RltdRmtInf")
    protected List<RemittanceLocation2> rltdRmtInf;
    @XmlElement(name = "RmtInf")
    protected RemittanceInformation7 rmtInf;
    @XmlElement(name = "RltdDts")
    protected TransactionDates2 rltdDts;
    @XmlElement(name = "RltdPric")
    protected TransactionPrice3Choice rltdPric;
    @XmlElement(name = "RltdQties")
    protected List<TransactionQuantities2Choice> rltdQties;
    @XmlElement(name = "FinInstrmId")
    protected SecurityIdentification14 finInstrmId;
    @XmlElement(name = "Tax")
    protected TaxInformation3 tax;
    @XmlElement(name = "RtrInf")
    protected PaymentReturnReason2 rtrInf;
    @XmlElement(name = "CorpActn")
    protected CorporateAction9 corpActn;
    @XmlElement(name = "SfkpgAcct")
    protected SecuritiesAccount13 sfkpgAcct;
    @XmlElement(name = "CshDpst")
    protected List<CashDeposit1> cshDpst;
    @XmlElement(name = "CardTx")
    protected CardTransaction1 cardTx;
    @XmlElement(name = "AddtlTxInf")
    protected String addtlTxInf;
    @XmlElement(name = "SplmtryData")
    protected List<SupplementaryData1> splmtryData;

    /**
     * Ruft den Wert der refs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionReferences3 }
     *     
     */
    public TransactionReferences3 getRefs() {
        return refs;
    }

    /**
     * Legt den Wert der refs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionReferences3 }
     *     
     */
    public void setRefs(TransactionReferences3 value) {
        this.refs = value;
    }

    /**
     * Ruft den Wert der amt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ActiveOrHistoricCurrencyAndAmount }
     *     
     */
    public ActiveOrHistoricCurrencyAndAmount getAmt() {
        return amt;
    }

    /**
     * Legt den Wert der amt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ActiveOrHistoricCurrencyAndAmount }
     *     
     */
    public void setAmt(ActiveOrHistoricCurrencyAndAmount value) {
        this.amt = value;
    }

    /**
     * Ruft den Wert der cdtDbtInd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CreditDebitCode }
     *     
     */
    public CreditDebitCode getCdtDbtInd() {
        return cdtDbtInd;
    }

    /**
     * Legt den Wert der cdtDbtInd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CreditDebitCode }
     *     
     */
    public void setCdtDbtInd(CreditDebitCode value) {
        this.cdtDbtInd = value;
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
     * Ruft den Wert der chrgs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Charges4 }
     *     
     */
    public Charges4 getChrgs() {
        return chrgs;
    }

    /**
     * Legt den Wert der chrgs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Charges4 }
     *     
     */
    public void setChrgs(Charges4 value) {
        this.chrgs = value;
    }

    /**
     * Ruft den Wert der intrst-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionInterest3 }
     *     
     */
    public TransactionInterest3 getIntrst() {
        return intrst;
    }

    /**
     * Legt den Wert der intrst-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionInterest3 }
     *     
     */
    public void setIntrst(TransactionInterest3 value) {
        this.intrst = value;
    }

    /**
     * Ruft den Wert der rltdPties-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionParties3 }
     *     
     */
    public TransactionParties3 getRltdPties() {
        return rltdPties;
    }

    /**
     * Legt den Wert der rltdPties-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionParties3 }
     *     
     */
    public void setRltdPties(TransactionParties3 value) {
        this.rltdPties = value;
    }

    /**
     * Ruft den Wert der rltdAgts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransactionAgents3 }
     *     
     */
    public TransactionAgents3 getRltdAgts() {
        return rltdAgts;
    }

    /**
     * Legt den Wert der rltdAgts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionAgents3 }
     *     
     */
    public void setRltdAgts(TransactionAgents3 value) {
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
     *     {@link RemittanceInformation7 }
     *     
     */
    public RemittanceInformation7 getRmtInf() {
        return rmtInf;
    }

    /**
     * Legt den Wert der rmtInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceInformation7 }
     *     
     */
    public void setRmtInf(RemittanceInformation7 value) {
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
     *     {@link TransactionPrice3Choice }
     *     
     */
    public TransactionPrice3Choice getRltdPric() {
        return rltdPric;
    }

    /**
     * Legt den Wert der rltdPric-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionPrice3Choice }
     *     
     */
    public void setRltdPric(TransactionPrice3Choice value) {
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
     * {@link TransactionQuantities2Choice }
     * 
     * 
     */
    public List<TransactionQuantities2Choice> getRltdQties() {
        if (rltdQties == null) {
            rltdQties = new ArrayList<TransactionQuantities2Choice>();
        }
        return this.rltdQties;
    }

    /**
     * Ruft den Wert der finInstrmId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SecurityIdentification14 }
     *     
     */
    public SecurityIdentification14 getFinInstrmId() {
        return finInstrmId;
    }

    /**
     * Legt den Wert der finInstrmId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityIdentification14 }
     *     
     */
    public void setFinInstrmId(SecurityIdentification14 value) {
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
     *     {@link PaymentReturnReason2 }
     *     
     */
    public PaymentReturnReason2 getRtrInf() {
        return rtrInf;
    }

    /**
     * Legt den Wert der rtrInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentReturnReason2 }
     *     
     */
    public void setRtrInf(PaymentReturnReason2 value) {
        this.rtrInf = value;
    }

    /**
     * Ruft den Wert der corpActn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CorporateAction9 }
     *     
     */
    public CorporateAction9 getCorpActn() {
        return corpActn;
    }

    /**
     * Legt den Wert der corpActn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CorporateAction9 }
     *     
     */
    public void setCorpActn(CorporateAction9 value) {
        this.corpActn = value;
    }

    /**
     * Ruft den Wert der sfkpgAcct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SecuritiesAccount13 }
     *     
     */
    public SecuritiesAccount13 getSfkpgAcct() {
        return sfkpgAcct;
    }

    /**
     * Legt den Wert der sfkpgAcct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SecuritiesAccount13 }
     *     
     */
    public void setSfkpgAcct(SecuritiesAccount13 value) {
        this.sfkpgAcct = value;
    }

    /**
     * Gets the value of the cshDpst property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cshDpst property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCshDpst().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CashDeposit1 }
     * 
     * 
     */
    public List<CashDeposit1> getCshDpst() {
        if (cshDpst == null) {
            cshDpst = new ArrayList<CashDeposit1>();
        }
        return this.cshDpst;
    }

    /**
     * Ruft den Wert der cardTx-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CardTransaction1 }
     *     
     */
    public CardTransaction1 getCardTx() {
        return cardTx;
    }

    /**
     * Legt den Wert der cardTx-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CardTransaction1 }
     *     
     */
    public void setCardTx(CardTransaction1 value) {
        this.cardTx = value;
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

    /**
     * Gets the value of the splmtryData property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the splmtryData property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSplmtryData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupplementaryData1 }
     * 
     * 
     */
    public List<SupplementaryData1> getSplmtryData() {
        if (splmtryData == null) {
            splmtryData = new ArrayList<SupplementaryData1>();
        }
        return this.splmtryData;
    }

}
