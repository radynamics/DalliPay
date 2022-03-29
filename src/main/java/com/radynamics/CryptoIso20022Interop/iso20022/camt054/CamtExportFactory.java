package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import com.radynamics.CryptoIso20022Interop.VersionController;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.Camt05400102Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.Camt05400104Writer;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.Camt05400109Writer;
import com.radynamics.CryptoIso20022Interop.transformation.TransformInstruction;

public class CamtExportFactory {
    public static final CamtExport create(CamtFormat format, TransformInstruction transformInstruction, VersionController versionController) {
        var export = new CamtExport();
        if (format == Camt05400102Writer.ExportFormat) {
            export.setWriter(new Camt05400102Writer(transformInstruction.getLedger(), transformInstruction, versionController.getVersion()));
            export.setConverter(new CamtConverter(com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400102.generated.Document.class));
        } else if (format == Camt05400104Writer.ExportFormat) {
            export.setWriter(new Camt05400104Writer(transformInstruction.getLedger(), transformInstruction, versionController.getVersion()));
            export.setConverter(new CamtConverter(com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400104.generated.Document.class));
        } else if (format == Camt05400109Writer.ExportFormat) {
            export.setWriter(new Camt05400109Writer(transformInstruction.getLedger(), transformInstruction, versionController.getVersion()));
            export.setConverter(new CamtConverter(com.radynamics.CryptoIso20022Interop.iso20022.camt054.camt05400109.generated.Document.class));
        } else {
            throw new IllegalStateException("Unexpected value: " + format);
        }
        return export;
    }
}
