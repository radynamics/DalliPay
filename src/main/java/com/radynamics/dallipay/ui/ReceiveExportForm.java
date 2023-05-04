package com.radynamics.dallipay.ui;

import com.radynamics.dallipay.iso20022.camt054.CamtFormat;
import com.radynamics.dallipay.iso20022.camt054.CamtFormatHelper;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReceiveExportForm extends JDialog {
    private SpringLayout panel1Layout;
    private JPanel pnlContent;
    private Component anchorComponentTopLeft;
    private boolean accepted;
    private FilePathField txtOutputFile;
    private JComboBox<CamtFormat> cboExportFormat;
    private final FormAcceptCloseHandler formAcceptCloseHandler = new FormAcceptCloseHandler(this);

    private static final Map<CamtFormat, String> formatMapping = new LinkedHashMap<>();

    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());

    public ReceiveExportForm() {
        formatMapping.put(CamtFormat.Camt05300108, "camt.053 Version 08");
        formatMapping.put(CamtFormat.Camt05400109, "camt.054 Version 09");
        formatMapping.put(CamtFormat.Camt05400104, "camt.054 Version 04");
        formatMapping.put(CamtFormat.Camt05400102, "camt.054 Version 02");

        setupUI();
    }

    private void setupUI() {
        setTitle(res.getString("title"));
        setIconImage(Utils.getProductIcon());

        formAcceptCloseHandler.configure();
        formAcceptCloseHandler.addFormActionListener(this::acceptDialog);

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
                lbl.setText(res.getString("desc"));
                lbl.setOpaque(true);
                pnl.add(lbl, BorderLayout.WEST);
            }
        }

        {
            int line = 0;
            int padValueCtrl = 110;
            {
                var lbl = new JLabel(res.getString("file"));
                anchorComponentTopLeft = lbl;
                panel1Layout.putConstraint(SpringLayout.WEST, lbl, 0, SpringLayout.WEST, pnlContent);
                panel1Layout.putConstraint(SpringLayout.NORTH, lbl, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                lbl.setOpaque(true);
                pnlContent.add(lbl);

                txtOutputFile = new FilePathField();
                txtOutputFile.setValidateExists(false);
                panel1Layout.putConstraint(SpringLayout.WEST, txtOutputFile, padValueCtrl, SpringLayout.WEST, anchorComponentTopLeft == null ? lbl : anchorComponentTopLeft);
                panel1Layout.putConstraint(SpringLayout.NORTH, txtOutputFile, getNorthPad(line), SpringLayout.NORTH, pnlContent);
                panel1Layout.putConstraint(SpringLayout.EAST, pnlContent, 0, SpringLayout.EAST, txtOutputFile);
                pnlContent.add(txtOutputFile);
                line++;
            }
            {
                var lbl = new JLabel(res.getString("format"));
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
                cmd.addActionListener(e -> formAcceptCloseHandler.accept());
                pnl.add(cmd);
            }
            {
                var cmd = new JButton(res.getString("cancel"));
                cmd.setPreferredSize(new Dimension(150, 35));
                cmd.addActionListener(e -> formAcceptCloseHandler.close());
                pnl.add(cmd);
            }
        }
    }

    private void acceptDialog() {
        setDialogAccepted(true);
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
