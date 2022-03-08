package com.radynamics.CryptoIso20022Interop.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

public class FilePathField extends JPanel {
    private JTextField txt;

    private ArrayList<ChangedListener> listener = new ArrayList<>();
    private Window owner;
    private boolean validateExists;
    private File currentDirectory;

    public FilePathField(Window owner) {
        this.owner = owner;
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        {
            txt = new JTextField();
            txt.setColumns(50);
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
        {
            var lbl = Utils.createLinkLabel(owner, "browse...");
            lbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showFileChooser();
                }
            });

            var c = new GridBagConstraints();
            c.insets = new Insets(0, 5, 0, 5);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 0.0;
            c.weighty = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            add(lbl, c);
        }
    }

    private void showFileChooser() {
        var fc = new JFileChooser();
        var xmlFilter = new FileTypeFilter(".xml", "ISO 20022 Payment files");
        fc.addChoosableFileFilter(xmlFilter);
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
