/*
 * Copyright (c) 2014 Mrbrutal. All rights reserved.
 * Do not modify or redistribute without written permission.
 *
 * @author Mrbrutal
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.Launcher;

import javax.swing.*;
import java.awt.*;

public class CustomJLabel extends JLabel {

    public String name;

    public CustomJLabel(String text) {
        super();
        this.name = text;
        if(text.equals("x")) {
            ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + this.name + "_default.png"));
            this.setIcon(icon);
        }
        else if(text.equals("max") || text.equals("min")) {
            ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + this.name + "_default.png"));
            this.setIcon(icon);
        }
        else {
            ImageIcon icon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/button_default_" + this.name + ".png"));
            this.setIcon(icon);
        }

        setForeground(Color.white);
    }

//    @Override
//    public void paint(Graphics g) {
//        super.paint(g);
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.drawString(translate, 10, this.getHeight()/2+2);
//    }
}
