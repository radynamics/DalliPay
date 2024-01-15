package com.radynamics.dallipay.ui.wizard;

import javax.swing.*;
import java.awt.*;

public interface Wizard {
    Container pageContainer();

    AbstractButton cancelButton();

    AbstractButton previousButton();

    AbstractButton nextButton();

    AbstractButton finishButton();

}
