package com.radynamics.CryptoIso20022Interop.ui;

import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtFormat;
import com.radynamics.CryptoIso20022Interop.iso20022.camt054.CamtFormatHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReceiveExportForm extends JDialog {
    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private boolean accepted;
    private FilePathField txtOutputFile;
    private JComboBox<CamtFormat> cboExportFormat;

    private static final Map<CamtFormat, String> formatMapping = new LinkedHashMap<>();

    public ReceiveExportForm() {
        formatMapping.put(CamtFormat.Camt05400109, "camt.054 Version 09");
        formatMapping.put(CamtFormat.Camt05400104, "camt.054 Version 04");
        formatMapping.put(CamtFormat.Camt05400102, "camt.054 Version 02");

        setupUI();
    }

    private void setupUI() {
        setTitle("Export");
        setIconImage(Utils.getProductIcon());

        var cancelDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
        getRootPane().registerKeyboardAction(cancelDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        var acceptDialog = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        };
        getRootPane().registerKeyboardAction(acceptDialog, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        var pnlMain = new JPanel();
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlMain);

        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));

        var innerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        JPanel panel0 = new JPanel();
        panel0.setBorder(innerBorder);
        panel0.setLayout(new BoxLayout(panel0, BoxLayout.X_AXIS));
        var panel1 = new JPanel();
        panel1.setBorder(innerBorder);
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel1Layout = new SpringLayout();
        pnlContent = new JPanel();
        pnlContent.setLayout(panel1Layout);
        JPanel panel3 = new JPanel();
        var panel3Layout = new SpringLayout();
        panel3.setLayout(panel3Layout);

        pnlContent.setPreferredSize(new Dimension(100, 100));
        var sp = new JScrollPane(pnlContent);
        sp.setBorder(BorderFactory.createEmptyBorder());
        panel1.add(sp);

        pnlMain.add(panel0);
        pnlMain.add(panel1);
        pnlMain.add(panel3);

        panel0.setMinimumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel0.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        panel3.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel3.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));

        {
            var pnl = new JPanel();
            pnl.setLayout(new BorderLayout());
            panel0.add(pnl);
            {
                var lbl = new JLabel();
                lbl.setText(getTitle());
                lbl.putClientProperty("FlatLaf.style", "font: 200% $semibold.font");
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.NORTH);
            }
            {
                var lbl = new JLabel();
                lbl.setText("Export received payments.");
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.WEST);
            }
        }

        {
            int line = 0;
            int padValueCtrl = 110;
            {
                var lbl = new JLabel("File:");
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                lbl.setOpaque(true);
                pnlContent.add(lbl);

                txtOutputFile = new FilePathField(this);
                txtOutputFile.setValidateExists(false);
                panel1Layout.putConstraint(SpringLayout.WEST, txtOutputFile, padValueCtrl, SpringLayout.WEST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, txtOutputFile, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                panel1Layout.putConstraint(SpringLayout.EAST, pnlContent, 0, SpringLayout.EAST, txtOutputFile);
                pnlContent.add(txtOutputFile);
                line++;
            }
            {
                var lbl = new JLabel("Payment format:");
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                lbl.setOpaque(true);
                pnlContent.add(lbl);

                cboExportFormat = new JComboBox<>();
                cboExportFormat.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, formatMapping.get(value), index, isSelected, cellHasFocus);
                    }
                });
                for (var item : formatMapping.entrySet()) {
                    cboExportFormat.addItem(item.getKey());
                }
                cboExportFormat.setSelectedItem(CamtFormatHelper.getDefault());
                panel1Layout.putConstraint(SpringLayout.WEST, cboExportFormat, padValueCtrl, SpringLayout.WEST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, cboExportFormat, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                pnlContent.add(cboExportFormat);
                line++;
            }
        }
        {
            var pnl = new JPanel();
            panel3Layout.putConstraint(SpringLayout.EAST, pnl, 0, SpringLayout.EAST, panel3);
            panel3Layout.putConstraint(SpringLayout.SOUTH, pnl, 0, SpringLayout.SOUTH, panel3);
            panel3.add(pnl);
            {
                var cmd = new JButton("OK");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> onOk());
                pnl.add(cmd);
            }
            {
                var cmd = new JButton("Cancel");
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> close());
                pnl.add(cmd);
            }
        }
    }

    private void onOk() {
        setDialogAccepted(true);
        close();
    }

    private void close() {
        dispose();
    }

    private static int getNorthPad(int line) {
        final var lineHeight = 30;
        return line * lineHeight;
    }

    private void setDialogAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDialogAccepted() {
        return accepted;
    }

    public void setOutputFile(String targetFileName) {
        txtOutputFile.setText(targetFileName);
    }

    public String getOutputFile() {
        return txtOutputFile.getText();
    }

    public CamtFormat getExportFormat() {
        return (CamtFormat) cboExportFormat.getSelectedItem();
    }

    public void setExportFormat(CamtFormat exportFormat) {
        cboExportFormat.setSelectedItem(exportFormat);
    }
}
