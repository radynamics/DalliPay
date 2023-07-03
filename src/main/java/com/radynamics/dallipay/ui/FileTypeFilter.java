package com.radynamics.dallipay.ui;

import org.apache.commons.lang3.StringUtils;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileTypeFilter extends FileFilter {
    private final String extension;
    private final String description;

    public FileTypeFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        return StringUtils.endsWithIgnoreCase(file.getName(), extension);
    }

    public String getDescription() {
        return String.format("%s (*%s)", description, extension);
    }
}
