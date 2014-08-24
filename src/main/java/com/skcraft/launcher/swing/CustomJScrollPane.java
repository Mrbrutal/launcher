/*
 * Copyright (c) 2014 Mrbrutal. All rights reserved.
 * Do not modify or redistribute without written permission.
 *
 * @author Mrbrutal
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.Launcher;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomJScrollPane extends BasicScrollBarUI {

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(0xff1c1c1e));
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

        if(trackHighlight == DECREASE_HIGHLIGHT)        {
            paintDecreaseHighlight(g);
        }
        else if(trackHighlight == INCREASE_HIGHLIGHT)           {
            paintIncreaseHighlight(g);
        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }

        int w = thumbBounds.width;
        int h = thumbBounds.height;

        g.translate(thumbBounds.x, thumbBounds.y);

        g.setColor(new Color(0xff86493b));
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(0xff1c1c1e));
        g.drawLine(0, 0, 0, h);
        g.drawLine(w-1, 0, w-1, h);
        g.drawLine(0, 0, w, 0);
        g.drawLine(0, h-1, w, h-1);

        g.translate(-thumbBounds.x, -thumbBounds.y);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton btn = new JButton("");
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setPressedIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/up.png")));
        btn.setBorderPainted(false);
        btn.setIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/up.png")));
        btn.setRolloverIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/up.png")));
        btn.setRolloverEnabled(false);
        return btn;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton btn = new JButton("");
        btn.setPreferredSize(new Dimension(0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setFocusPainted(false);
        btn.setPressedIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/down.png")));
        btn.setBorderPainted(false);
        btn.setIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/down.png")));
        btn.setRolloverIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/down.png")));
        btn.setRolloverEnabled(false);
        return btn;
    }
}
