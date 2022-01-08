package com.radynamics.CryptoIso20022Interop.iso20022.pain001;

import com.radynamics.CryptoIso20022Interop.cryptoledger.Ledger;
import com.radynamics.CryptoIso20022Interop.cryptoledger.Transaction;
import com.radynamics.CryptoIso20022Interop.exchange.CurrencyConverter;
import com.radynamics.CryptoIso20022Interop.iso20022.Account;
import com.radynamics.CryptoIso20022Interop.iso20022.IbanAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.OtherAccount;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.ReferenceType;
import com.radynamics.CryptoIso20022Interop.iso20022.creditorreference.StructuredReferenceFactory;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103.generated.AccountIdentification4Choice;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103.generated.CreditorReferenceInformation2;
import com.radynamics.CryptoIso20022Interop.iso20022.pain001.pain00100103.generated.Document;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
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
            var senderAccount = getAccount(pmtInf.getDbtrAcct().getId());
            var senderLedger = transformInstruction.getWalletOrNull(senderAccount);
            for (var cdtTrfTxInf : pmtInf.getCdtTrfTxInf()) {
                var receiverAccount = getAccount(cdtTrfTxInf.getCdtrAcct().getId());
                var receiverLedger = transformInstruction.getWalletOrNull(receiverAccount);
                // TODO: use currency from meta data and support IOUs.
                var ccy = ledger.getNativeCcySymbol();
                var amountNativeCcy = ccyConverter.convert(cdtTrfTxInf.getAmt().getInstdAmt().getValue(), cdtTrfTxInf.getAmt().getInstdAmt().getCcy(), ccy);
                var amountSmallestUnit = ledger.convertToSmallestAmount(amountNativeCcy);

                var t = ledger.createTransaction(senderLedger, receiverLedger, amountSmallestUnit, ccy);
                t.setSender(senderAccount);
                t.setReceiver(receiverAccount);

                var rmtInf = cdtTrfTxInf.getRmtInf();
                if (rmtInf != null) {
                    if (rmtInf.getStrd() != null) {
                        for (var strd : rmtInf.getStrd()) {
                            var cdtrRefInf = strd.getCdtrRefInf();
                            if (cdtrRefInf == null) {
                                continue;
                            }
                            var typeText = getReferenceType(cdtrRefInf);
                            var reference = cdtrRefInf.getRef();
                            t.addStructuredReference(StructuredReferenceFactory.create(typeText, reference));

                            for (var addtlRmtInf : strd.getAddtlRmtInf()) {
                                t.addMessage(addtlRmtInf);
                            }
                        }
                    }

                    if (rmtInf.getUstrd() != null) {
                        for (var ustrd : rmtInf.getUstrd()) {
                            t.addMessage(ustrd);
                        }
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

    private Account getAccount(AccountIdentification4Choice id) {
        return id.getIBAN() != null ? new IbanAccount(id.getIBAN()) : new OtherAccount(id.getOthr().getId());
    }

    private Document fromXml(InputStream input) throws JAXBException, XMLStreamException {
        // TODO: RST 2021-12-31 manually ensure input matches ISO version (ex "pain.001.001.03") without regional derived xsd.
        var xif = XMLInputFactory.newFactory();
        xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        var xsr = xif.createXMLStreamReader(input);

        var ctx = JAXBContext.newInstance(Document.class);
        var jaxbUnmarshaller = ctx.createUnmarshaller();
        return (Document) jaxbUnmarshaller.unmarshal(xsr);
    }
}
