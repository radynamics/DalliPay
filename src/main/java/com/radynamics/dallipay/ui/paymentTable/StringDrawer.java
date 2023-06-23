package com.radynamics.dallipay.ui.paymentTable;

import com.radynamics.dallipay.ui.Consts;
import com.radynamics.dallipay.ui.Utils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class StringDrawer {
    public static void draw(Graphics g, String text, int areaWidth, int areaHeight) {
        if (StringUtils.isEmpty(text)) {
            return;
        }

        var font = new Font(g.getFont().getFontName(), Font.PLAIN, 30);
        g.setFont(font);
        g.setColor(Consts.ColorSmallInfo);

        int lineHeight = g.getFontMetrics().getHeight();
        var lines = Utils.fromMultilineText(text);
        for (var i = 0; i < lines.length; i++) {
            int lineWidth = g.getFontMetrics().stringWidth(lines[i]);
            g.drawString(lines[i], areaWidth / 2 - lineWidth / 2, areaHeight / 2 + (i * lineHeight));
        }
    }
}
