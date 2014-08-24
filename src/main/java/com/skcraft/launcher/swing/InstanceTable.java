/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public class InstanceTable extends JTable {

    public InstanceTable() {
        setShowGrid(false);
        setRowHeight(Math.max(getRowHeight() + 4, 40));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(new Color(0xff1c1c1e));
        setForeground(new Color(0xff86493b));
        setFocusable(false);
        clearSelection();
        setBorder(BorderFactory.createEmptyBorder());
        setSelectionBackground(new Color(0xff29292d));
        this.getTableHeader().setDefaultRenderer(new CustomJTableRenderer());
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth(48);
            getColumnModel().getColumn(0).setMinWidth(48);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}
