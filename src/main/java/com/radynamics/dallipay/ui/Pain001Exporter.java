package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.cryptoledger.transaction.TransmissionState;
import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.iso20022.pain001.Pain001Xml;

import javax.xml.transform.TransformerException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Pain001Exporter {
    private final ArrayList<Payment> payments;
    private Pain001Xml pain001;
    private int countBefore;
    private final ArrayList<Payment> failed = new ArrayList<>();

    public Pain001Exporter(ArrayList<Payment> payments) {
        this.payments = payments;
    }

    public void read(InputStream inputStream) throws Exception {
        pain001 = Pain001Xml.read(inputStream);
        countBefore = pain001.countCdtTrfTxInf();
    }

    public List<Payment> sentPayments() {
        return payments.stream().filter(o -> o.getTransmission() == TransmissionState.Success).collect(Collectors.toList());
    }

    public int getCountBefore() {
        return countBefore;
    }

    public ArrayList<Payment> getFailed() {
        return failed;
    }

    public int writeTo(OutputStream outputStream) throws TransformerException {
        for (var p : sentPayments()) {
            if (pain001.isRemovable(p)) {
                pain001.remove(p);
            } else {
                failed.add(p);
            }
        }

        pain001.writeTo(outputStream);
        return pain001.countCdtTrfTxInf();
    }
}
