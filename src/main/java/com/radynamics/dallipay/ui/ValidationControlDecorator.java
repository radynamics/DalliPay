package com.radynamics.dallipay.ui;

import javax.swing.*;
import java.util.ResourceBundle;

public class ValidationControlDecorator {
    private final JComponent ctrl;
    private final InputControlValidator validator;

    private final ResourceBundle res = ResourceBundle.getBundle("i18n.Various");

    public ValidationControlDecorator(JComponent ctrl, InputControlValidator validator) {
        if (ctrl == null) throw new IllegalArgumentException("Parameter 'ctrl' cannot be null");
        if (validator == null) throw new IllegalArgumentException("Parameter 'validator' cannot be null");
        this.ctrl = ctrl;
        this.validator = validator;
    }

    private void showInvalid() {
        ctrl.putClientProperty("JComponent.outline", "error");
        ctrl.setToolTipText(String.format(res.getString("inputInvalid"), validator.getValidExampleInput()));
    }

    private void showValid() {
        ctrl.putClientProperty("JComponent.outline", null);
        ctrl.setToolTipText(null);
    }

    public void update(Object value) {
        if (!validator.isValid(value)) {
            showInvalid();
            return;
        }
        showValid();
    }
}
