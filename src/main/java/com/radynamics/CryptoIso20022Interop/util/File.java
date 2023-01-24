package com.radynamics.CryptoIso20022Interop.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class File {
    public static java.io.File createWithTimeSuffix(java.io.File basedOn) {
        var df = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        return create(basedOn, df.format(new Date()));
    }

    public static java.io.File create(java.io.File basedOn, String suffix) {
        if (suffix == null || suffix.length() == 0) {
            return basedOn;
        }

        // "abc.xml" -> "abc-suffix.xml"
        var indexExtension = basedOn.getName().lastIndexOf(".");
        var fileNameWithoutExtension = indexExtension <= 0 ? basedOn.getName() : basedOn.getName().substring(0, indexExtension);
        // ".abc" -> ".abc-suffix"
        var extension = indexExtension <= 0 ? "" : basedOn.getName().substring(indexExtension);

        var newFileName = String.format("%s-%s%s", fileNameWithoutExtension, suffix, extension);
        if (basedOn.getParentFile() == null) {
            return new java.io.File(newFileName);
        }
        return new java.io.File(basedOn.getParentFile().getAbsolutePath() + java.io.File.separator + newFileName);
    }
}
