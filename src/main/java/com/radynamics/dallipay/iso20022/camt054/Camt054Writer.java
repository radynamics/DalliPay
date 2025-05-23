package com.radynamics.dallipay.iso20022.camt054;

import com.radynamics.dallipay.iso20022.Payment;
import com.radynamics.dallipay.transformation.TransformInstruction;

public interface Camt054Writer {
    Object createDocument(Payment[] transactions, ReportBalances reportBalances) throws Exception;

    TransformInstruction getTransformInstruction();

    CamtFormat getExportFormat();

    LedgerCurrencyFormat getExportLedgerCurrencyFormat();
}
