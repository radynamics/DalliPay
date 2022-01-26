package com.radynamics.CryptoIso20022Interop.iso20022.camt054;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class AmountRounderTest {
    @Test
    public void round() {
        Assert.assertEquals("100", AmountRounder.round(100, 0).toPlainString());
        Assert.assertEquals("100.00", AmountRounder.round(100, 2).toPlainString());
        Assert.assertEquals("100", AmountRounder.round(100.1, 0).toPlainString());
        Assert.assertEquals("101", AmountRounder.round(100.5, 0).toPlainString());
        Assert.assertEquals("101", AmountRounder.round(100.9, 0).toPlainString());
        Assert.assertEquals("100.10", AmountRounder.round(100.1, 2).toPlainString());
        Assert.assertEquals("100.12", AmountRounder.round(100.12, 2).toPlainString());
        Assert.assertEquals("100.12", AmountRounder.round(100.123, 2).toPlainString());
        Assert.assertEquals("100.13", AmountRounder.round(100.127, 2).toPlainString());
    }
}
