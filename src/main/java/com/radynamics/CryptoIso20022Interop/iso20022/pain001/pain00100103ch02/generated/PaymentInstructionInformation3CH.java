//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.05.28 um 01:45:09 PM CEST 
//


package com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103ch02.generated;

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
 * <p>Java-Klasse für PaymentInstructionInformation3-CH complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PaymentInstructionInformation3-CH"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="PmtInfId" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}Max35Text-Swift"/&gt;
 *         &lt;element name="PmtMtd" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PaymentMethod3Code"/&gt;
 *         &lt;element name="BtchBookg" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}BatchBookingIndicator" minOccurs="0"/&gt;
 *         &lt;element name="NbOfTxs" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}Max15NumericText" minOccurs="0"/&gt;
 *         &lt;element name="CtrlSum" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}DecimalNumber" minOccurs="0"/&gt;
 *         &lt;element name="PmtTpInf" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PaymentTypeInformation19-CH" minOccurs="0"/&gt;
 *         &lt;element name="ReqdExctnDt" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ISODate"/&gt;
 *         &lt;element name="Dbtr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PartyIdentification32-CH"/&gt;
 *         &lt;element name="DbtrAcct" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}CashAccount16-CH_IdTpCcy"/&gt;
 *         &lt;element name="DbtrAgt" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}BranchAndFinancialInstitutionIdentification4-CH_BicOrClrId"/&gt;
 *         &lt;element name="UltmtDbtr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}PartyIdentification32-CH" minOccurs="0"/&gt;
 *         &lt;element name="ChrgBr" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}ChargeBearerType1Code" minOccurs="0"/&gt;
 *         &lt;element name="ChrgsAcct" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}CashAccount16-CH_IdAndCurrency" minOccurs="0"/&gt;
 *         &lt;element name="CdtTrfTxInf" type="{http://www.six-interbank-clearing.com/de/pain.001.001.03.ch.02.xsd}CreditTransferTransactionInformation10-CH" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PaymentInstructionInformation3-CH", propOrder = {
    "pmtInfId",
    "pmtMtd",
    "btchBookg",
    "nbOfTxs",
    "ctrlSum",
    "pmtTpInf",
    "reqdExctnDt",
    "dbtr",
    "dbtrAcct",
    "dbtrAgt",
    "ultmtDbtr",
    "chrgBr",
    "chrgsAcct",
    "cdtTrfTxInf"
})
public class PaymentInstructionInformation3CH {

    @XmlElement(name = "PmtInfId", required = true)
    protected String pmtInfId;
    @XmlElement(name = "PmtMtd", required = true)
    @XmlSchemaType(name = "string")
    protected PaymentMethod3Code pmtMtd;
    @XmlElement(name = "BtchBookg")
    protected Boolean btchBookg;
    @XmlElement(name = "NbOfTxs")
    protected String nbOfTxs;
    @XmlElement(name = "CtrlSum")
    protected BigDecimal ctrlSum;
    @XmlElement(name = "PmtTpInf")
    protected PaymentTypeInformation19CH pmtTpInf;
    @XmlElement(name = "ReqdExctnDt", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar reqdExctnDt;
    @XmlElement(name = "Dbtr", required = true)
    protected PartyIdentification32CH dbtr;
    @XmlElement(name = "DbtrAcct", required = true)
    protected CashAccount16CHIdTpCcy dbtrAcct;
    @XmlElement(name = "DbtrAgt", required = true)
    protected BranchAndFinancialInstitutionIdentification4CHBicOrClrId dbtrAgt;
    @XmlElement(name = "UltmtDbtr")
    protected PartyIdentification32CH ultmtDbtr;
    @XmlElement(name = "ChrgBr")
    @XmlSchemaType(name = "string")
    protected ChargeBearerType1Code chrgBr;
    @XmlElement(name = "ChrgsAcct")
    protected CashAccount16CHIdAndCurrency chrgsAcct;
    @XmlElement(name = "CdtTrfTxInf", required = true)
    protected List<CreditTransferTransactionInformation10CH> cdtTrfTxInf;

    /**
     * Ruft den Wert der pmtInfId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPmtInfId() {
        return pmtInfId;
    }

    /**
     * Legt den Wert der pmtInfId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPmtInfId(String value) {
        this.pmtInfId = value;
    }

    /**
     * Ruft den Wert der pmtMtd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PaymentMethod3Code }
     *     
     */
    public PaymentMethod3Code getPmtMtd() {
        return pmtMtd;
    }

    /**
     * Legt den Wert der pmtMtd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentMethod3Code }
     *     
     */
    public void setPmtMtd(PaymentMethod3Code value) {
        this.pmtMtd = value;
    }

    /**
     * Ruft den Wert der btchBookg-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBtchBookg() {
        return btchBookg;
    }

    /**
     * Legt den Wert der btchBookg-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBtchBookg(Boolean value) {
        this.btchBookg = value;
    }

    /**
     * Ruft den Wert der nbOfTxs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNbOfTxs() {
        return nbOfTxs;
    }

    /**
     * Legt den Wert der nbOfTxs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNbOfTxs(String value) {
        this.nbOfTxs = value;
    }

    /**
     * Ruft den Wert der ctrlSum-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getCtrlSum() {
        return ctrlSum;
    }

    /**
     * Legt den Wert der ctrlSum-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setCtrlSum(BigDecimal value) {
        this.ctrlSum = value;
    }

    /**
     * Ruft den Wert der pmtTpInf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PaymentTypeInformation19CH }
     *     
     */
    public PaymentTypeInformation19CH getPmtTpInf() {
        return pmtTpInf;
    }

    /**
     * Legt den Wert der pmtTpInf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentTypeInformation19CH }
     *     
     */
    public void setPmtTpInf(PaymentTypeInformation19CH value) {
        this.pmtTpInf = value;
    }

    /**
     * Ruft den Wert der reqdExctnDt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getReqdExctnDt() {
        return reqdExctnDt;
    }

    /**
     * Legt den Wert der reqdExctnDt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setReqdExctnDt(XMLGregorianCalendar value) {
        this.reqdExctnDt = value;
    }

    /**
     * Ruft den Wert der dbtr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PartyIdentification32CH }
     *     
     */
    public PartyIdentification32CH getDbtr() {
        return dbtr;
    }

    /**
     * Legt den Wert der dbtr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyIdentification32CH }
     *     
     */
    public void setDbtr(PartyIdentification32CH value) {
        this.dbtr = value;
    }

    /**
     * Ruft den Wert der dbtrAcct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount16CHIdTpCcy }
     *     
     */
    public CashAccount16CHIdTpCcy getDbtrAcct() {
        return dbtrAcct;
    }

    /**
     * Legt den Wert der dbtrAcct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount16CHIdTpCcy }
     *     
     */
    public void setDbtrAcct(CashAccount16CHIdTpCcy value) {
        this.dbtrAcct = value;
    }

    /**
     * Ruft den Wert der dbtrAgt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BranchAndFinancialInstitutionIdentification4CHBicOrClrId }
     *     
     */
    public BranchAndFinancialInstitutionIdentification4CHBicOrClrId getDbtrAgt() {
        return dbtrAgt;
    }

    /**
     * Legt den Wert der dbtrAgt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BranchAndFinancialInstitutionIdentification4CHBicOrClrId }
     *     
     */
    public void setDbtrAgt(BranchAndFinancialInstitutionIdentification4CHBicOrClrId value) {
        this.dbtrAgt = value;
    }

    /**
     * Ruft den Wert der ultmtDbtr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PartyIdentification32CH }
     *     
     */
    public PartyIdentification32CH getUltmtDbtr() {
        return ultmtDbtr;
    }

    /**
     * Legt den Wert der ultmtDbtr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PartyIdentification32CH }
     *     
     */
    public void setUltmtDbtr(PartyIdentification32CH value) {
        this.ultmtDbtr = value;
    }

    /**
     * Ruft den Wert der chrgBr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ChargeBearerType1Code }
     *     
     */
    public ChargeBearerType1Code getChrgBr() {
        return chrgBr;
    }

    /**
     * Legt den Wert der chrgBr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ChargeBearerType1Code }
     *     
     */
    public void setChrgBr(ChargeBearerType1Code value) {
        this.chrgBr = value;
    }

    /**
     * Ruft den Wert der chrgsAcct-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CashAccount16CHIdAndCurrency }
     *     
     */
    public CashAccount16CHIdAndCurrency getChrgsAcct() {
        return chrgsAcct;
    }

    /**
     * Legt den Wert der chrgsAcct-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CashAccount16CHIdAndCurrency }
     *     
     */
    public void setChrgsAcct(CashAccount16CHIdAndCurrency value) {
        this.chrgsAcct = value;
    }

    /**
     * Gets the value of the cdtTrfTxInf property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cdtTrfTxInf property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCdtTrfTxInf().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CreditTransferTransactionInformation10CH }
     * 
     * 
     */
    public List<CreditTransferTransactionInformation10CH> getCdtTrfTxInf() {
        if (cdtTrfTxInf == null) {
            cdtTrfTxInf = new ArrayList<CreditTransferTransactionInformation10CH>();
        }
        return this.cdtTrfTxInf;
    }

}
