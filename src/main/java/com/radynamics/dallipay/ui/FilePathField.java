package com.radynamics.dallipay.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FilePathField extends JPanel {
    private JTextField txt;

    private ArrayList<ChangedListener> listener = new ArrayList<>();
    private boolean validateExists;
    private File currentDirectory;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public FilePathField() {
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(52);
            txt.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, res.getString("placeholderText"));
            txt.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, createToolbar());
            txt.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    if (!validateExists) {
                        return true;
                    }

                    var text = ((JTextField) input).getText().trim();
                    var file = new File(text);
                    if (file.exists()) {
                        txt.putClientProperty("JComponent.outline", null);
                        return true;
                    } else {
                        txt.putClientProperty("JComponent.outline", "error");
                        return false;
                    }
                }
            });
            txt.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    var file = new File(txt.getText().trim());
                    if (file.exists()) {
                        currentDirectory = file.getParentFile();
                    }
                    raiseChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    // do nothing
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    // do nothing
                }
            });
            var c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            add(txt, c);
        }
    }

    private Object createToolbar() {
        var toolbar = new JToolBar();
        {
            var cmd = new JToggleButton(new FlatSVGIcon("svg/open.svg", 16, 16));
            toolbar.add(cmd);
            Utils.setRolloverIcon(cmd);
            cmd.setToolTipText(res.getString("browse"));
            cmd.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showFileChooser();
                }
            });
        }
        return toolbar;
    }

    private void showFileChooser() {
        var fc = new JFileChooser();
        var xmlFilter = new FileTypeFilter(".xml", res.getString("fileTypeText"));
        fc.addChoosableFileFilter(xmlFilter);
        fc.addChoosableFileFilter(new FileTypeFilter(".csv", res.getString("fileTypeTextCsv")));
        fc.setFileFilter(xmlFilter);
        if (getText().length() > 0) {
            fc.setCurrentDirectory(new File(getText()).getParentFile());
        } else if (currentDirectory != null) {
            fc.setCurrentDirectory(currentDirectory);
        }
        int option = fc.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setText(fc.getSelectedFile().getAbsolutePath());
    }

    public void addChangedListener(ChangedListener l) {
        listener.add(l);
    }

    private void raiseChanged() {
        for (var l : listener) {
            l.onChanged();
        }
    }

    public void setText(String value) {
        txt.setText(value);
        txt.getInputVerifier().verify(txt);
    }

    public String getText() {
        return txt.getText().trim();
    }

    public void setValidateExists(boolean validateExists) {
        this.validateExists = validateExists;
    }

    public File getCurrentDirectory() {
        return this.currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }
}
