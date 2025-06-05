package com.radynamics.dallipay.ui;

import java.io.ByteArrayOutputStream;

public interface ReceiveListener {
    void onExported(ByteArrayOutputStream camtXml);
}
