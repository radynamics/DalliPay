package com.radynamics.dallipay.ui.wizard;

import com.radynamics.dallipay.ui.Utils;
import com.radynamics.dallipay.util.RequestFocusListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class WizardDialog extends JDialog implements Wizard {
    private static final Dimension defaultminimumSize = new Dimension(700, 500);

    private final JPanel topContainer = new JPanel(new GridLayout(1, 1));
    private final JLabel contentTitle = new JLabel();

    private final JPanel pageContainer = new JPanel(new GridLayout(1, 1));
    private final ResourceBundle res = ResourceBundle.getBundle("i18n." + this.getClass().getSimpleName());
    private final JButton cancelButton = new JButton(res.getString("cancel"));
    private final JButton previousButton = new JButton(res.getString("previous"));
    private final JButton nextButton = new JButton(res.getString("next"));
    private final JButton finishButton = new JButton(res.getString("finish"));

    public WizardDialog(Component parentComponent, String title) {
        super(SwingUtilities.getWindowAncestor(parentComponent), title, ModalityType.APPLICATION_MODAL);
        setupWizard();
    }

    public WizardDialog() {
        super();
        setupWizard();
    }

    private void setupWizard() {
        setIconImage(Utils.getProductIcon());
        setupComponents();
        layoutComponents();

        setMinimumSize(defaultminimumSize);

        // Center on screen
        var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPosition = (screenSize.width / 2) - (defaultminimumSize.width / 2);
        int yPosition = (screenSize.height / 2) - (defaultminimumSize.height / 2);
        setLocation(xPosition, yPosition);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setupComponents() {
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.X_AXIS));
        contentTitle.putClientProperty("FlatLaf.style", "font: 200%");
        topContainer.add(contentTitle);
        topContainer.add(Box.createHorizontalGlue());
        var img = new JLabel();
        img.setIcon(Utils.getScaled("img/productIcon.png", 64, 64));
        topContainer.add(img);

        cancelButton.addActionListener(e -> dispose());
        finishButton.addActionListener(e -> {
            dispose();
        });

        nextButton.addAncestorListener(new RequestFocusListener());

        cancelButton.setMnemonic(KeyEvent.VK_C);
        previousButton.setMnemonic(KeyEvent.VK_P);
        nextButton.setMnemonic(KeyEvent.VK_N);
        finishButton.setMnemonic(KeyEvent.VK_F);

        pageContainer.addContainerListener(new MinimumSizeAdjuster());
    }

    private void layoutComponents() {
        var layout = new GridBagLayout();
        layout.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0};
        layout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};
        layout.rowHeights = new int[]{0, 0, 0, 0};
        layout.columnWidths = new int[]{0, 0, 0, 0, 0};
        getContentPane().setLayout(layout);

        {
            var c = new GridBagConstraints();
            c.gridwidth = 5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(10, 20, 10, 10);
            getContentPane().add(topContainer, c);
        }
        {
            var c = new GridBagConstraints();
            c.gridwidth = 5;
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(10, 20, 5, 10);
            getContentPane().add(pageContainer, c);
        }
        {
            var c = new GridBagConstraints();
            c.gridwidth = 5;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 2;
            c.insets = new Insets(5, 20, 5, 10);
            getContentPane().add(new JSeparator(), c);
        }
        final var buttonInsets = new Insets(5, 5, 10, 0);
        {
            var c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = 3;
            c.insets = buttonInsets;
            getContentPane().add(cancelButton, c);
        }
        {
            var c = new GridBagConstraints();
            c.gridx = 2;
            c.gridy = 3;
            c.insets = buttonInsets;
            getContentPane().add(previousButton, c);
        }
        {
            var c = new GridBagConstraints();
            c.gridx = 3;
            c.gridy = 3;
            c.insets = buttonInsets;
            getContentPane().add(nextButton, c);
        }
        {
            var c = new GridBagConstraints();
            c.gridx = 4;
            c.gridy = 3;
            c.insets = new Insets(5, 5, 10, 10);
            getContentPane().add(finishButton, c);
        }
    }

    public String contentTitle() {
        return this.contentTitle.getText();
    }

    public void contentTitle(String contentTitle) {
        this.contentTitle.setText(contentTitle);
    }

    @Override
    public JPanel pageContainer() {
        return pageContainer;
    }

    @Override
    public AbstractButton cancelButton() {
        return cancelButton;
    }

    @Override
    public JButton previousButton() {
        return previousButton;
    }

    @Override
    public JButton nextButton() {
        return nextButton;
    }

    @Override
    public JButton finishButton() {
        return finishButton;
    }

    private class MinimumSizeAdjuster implements ContainerListener {
        @Override
        public void componentAdded(ContainerEvent e) {
            Dimension currentSize = getSize();
            Dimension preferredSize = getPreferredSize();

            Dimension newSize = new Dimension(currentSize);
            newSize.width = Math.max(currentSize.width, preferredSize.width);
            newSize.height = Math.max(currentSize.height, preferredSize.height);

            setMinimumSize(newSize);
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
        }
    }
}
