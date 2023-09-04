package com.radynamics.dallipay.iso20022.pain001;

import com.google.common.io.Files;
import com.radynamics.dallipay.cryptoledger.Ledger;

import java.io.File;
import java.util.Locale;

public final class PaymentInstructionReaderFactory {
    public static PaymentInstructionReader create(Ledger ledger, File file) throws Exception {
        var ext = Files.getFileExtension(file.getName()).toLowerCase(Locale.ROOT);
        if (ext.equals("xml")) {
            return new Pain001Reader(ledger);
        }

        if (ext.equals("csv")) {
            var reader = new CsvReader(ledger);
            reader.setSkipFirstLine(true);
            reader.setSeparator(';');
            return reader;
        }

        if (ext.equals("aba")) {
            return new AbaReader(ledger);
        }

        throw new Exception("Could not determine format for the given file based on its file extension.");
    }

    public static boolean supportsExport(File file) {
        var ext = Files.getFileExtension(file.getName()).toLowerCase(Locale.ROOT);
        return ext.equals("xml");
    }
}
