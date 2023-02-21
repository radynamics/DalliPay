package com.radynamics.dallipay.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileTest {
    @Test
    public void createWithTimeSuffix() {
        Assertions.assertEquals(".abc", File.create(new java.io.File(".abc"), null).getName());
        Assertions.assertEquals(".abc", File.create(new java.io.File(".abc"), "").getName());
        Assertions.assertEquals(".abc-123", File.create(new java.io.File(".abc"), "123").getName());
        Assertions.assertEquals(".a-123", File.create(new java.io.File(".a"), "123").getName());
        Assertions.assertEquals(".ab-123.c", File.create(new java.io.File(".ab.c"), "123").getName());

        Assertions.assertEquals("test.abc", File.create(new java.io.File("test.abc"), "").getName());
        Assertions.assertEquals("test-test.abc", File.create(new java.io.File("test.abc"), "test").getName());

        Assertions.assertEquals("c:\\test-test.abc", File.create(new java.io.File("c:\\test.abc"), "test").getAbsolutePath());
        Assertions.assertEquals("c:\\te.st\\test-test.abc", File.create(new java.io.File("c:\\te.st\\test.abc"), "test").getAbsolutePath());
    }
}
