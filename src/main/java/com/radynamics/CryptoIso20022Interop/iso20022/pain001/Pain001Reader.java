package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.schema.generated.AccountIdentification4ChoiceCH;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.schema.generated.CreditorReferenceInformation2;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.schema.generated.Document;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;

public class Pain001Reader {
    private final Ledger ledger;
    private TransformInstruction transformInstruction;
    private final CurrencyConverter ccyConverter;

    public Pain001Reader(Ledger ledger, TransformInstruction transformInstruction, CurrencyConverter ccyConverter) {
        this.ledger = ledger;
        this.transformInstruction = transformInstruction;
        this.ccyConverter = ccyConverter;
    }

    public Transaction[] read(InputStream pain001) throws Exception {
        var doc = fromXml(pain001);

        var list = new ArrayList<Transaction>();
        for (var pmtInf : doc.getCstmrCdtTrfInitn().getPmtInf()) {
            var sender = transformInstruction.getWallet(getAccount(pmtInf.getDbtrAcct().getId()));
            for (var cdtTrfTxInf : pmtInf.getCdtTrfTxInf()) {
                var receiver = transformInstruction.getWallet(getAccount(cdtTrfTxInf.getCdtrAcct().getId()));
                // TODO: use currency from meta data and support IOUs.
                var ccy = ledger.getNativeCcySymbol();
                var amountNativeCcy = ccyConverter.convert(cdtTrfTxInf.getAmt().getInstdAmt().getValue(), cdtTrfTxInf.getAmt().getInstdAmt().getCcy(), ccy);
                var amountSmallestUnit = ledger.convertToSmallestAmount(amountNativeCcy);

                var t = ledger.createTransaction(sender, receiver, amountSmallestUnit, ccy);

                var rmtInf = cdtTrfTxInf.getRmtInf();
                if (rmtInf != null) {
                    if (rmtInf.getStrd() != null && rmtInf.getStrd().getCdtrRefInf() != null) {
                        var cdtrRefInf = rmtInf.getStrd().getCdtrRefInf();
                        var typeText = getReferenceType(cdtrRefInf);
                        var reference = cdtrRefInf.getRef();
                        t.addStructuredReference(StructuredReferenceFactory.create(typeText, reference));

                        for (var addtlRmtInf : rmtInf.getStrd().getAddtlRmtInf()) {
                            t.addMessage(addtlRmtInf);
                        }
                    }

                    if (rmtInf.getUstrd() != null && rmtInf.getUstrd().length() > 0) {
                        t.addMessage(rmtInf.getUstrd());
                    }
                }

                list.add(t);
            }
        }

        return list.toArray(new Transaction[0]);
    }

    private ReferenceType getReferenceType(CreditorReferenceInformation2 cdtrRefInf) {
        var tp = cdtrRefInf.getTp();
        if (tp == null) {
            return StructuredReferenceFactory.detectType(cdtrRefInf.getRef());
        }

        var cdOrPrtry = tp.getCdOrPrtry();
        var typeText = cdOrPrtry.getCd() == null ? cdOrPrtry.getPrtry() : cdOrPrtry.getCd().value();
        return StructuredReferenceFactory.getType(typeText);
    }

    private String getAccount(AccountIdentification4ChoiceCH id) {
        // TODO: 2021-12-28 create specific types (new OtherAccount("010832052"), new IbanAccount(...))
        return id.getIBAN() != null ? id.getIBAN() : id.getOthr().getId();
    }

    private Document fromXml(InputStream input) throws JAXBException {
        var ctx = JAXBContext.newInstance(Document.class);
        var jaxbUnmarshaller = ctx.createUnmarshaller();
        return (Document) jaxbUnmarshaller.unmarshal(input);
    }
}
