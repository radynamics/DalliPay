package com.radynamics.dallipay.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {
    @Test
    public void fromMultilineText() {
        Assertions.assertEquals(0, Utils.fromMultilineText("").length);
        Assertions.assertEquals(0, Utils.fromMultilineText("\n").length);
        Assertions.assertEquals(1, Utils.fromMultilineText(" ").length);
        Assertions.assertEquals(1, Utils.fromMultilineText("a").length);
        Assertions.assertEquals(2, Utils.fromMultilineText("a\r\nb").length);
        Assertions.assertEquals(2, Utils.fromMultilineText("a\nb").length);
    }
}
