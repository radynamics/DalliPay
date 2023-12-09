package com.radynamics.dallipay.ui;

import okhttp3.HttpUrl;
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

    @Test
    public void hideCredentials() {
        Assertions.assertEquals("http://www.abc.com/", Utils.hideCredentials(HttpUrl.get("http://www.abc.com/")).toString());
        Assertions.assertEquals("https://www.abc.com/", Utils.hideCredentials(HttpUrl.get("https://www.abc.com/")).toString());
        Assertions.assertEquals("http://u**:***@localhost/", Utils.hideCredentials(HttpUrl.get("http://user:pass@localhost/")).toString());
        Assertions.assertEquals("http://u**:***@localhost:8332/", Utils.hideCredentials(HttpUrl.get("http://user:pass@localhost:8332/")).toString());
    }
}
