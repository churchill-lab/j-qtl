/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jax.qtl.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
/**
 * <p>Title: QTL data analysis</p>
 *
 * <p>Description: </p>
 *
 * <p>Company: The Jackson Laboratory</p>
 *
 * @author Lei Wu
 * @version 1.0
 */
@SuppressWarnings("all")
public class Tools
{
    public static GridBagConstraints setGbc(GridBagConstraints c, int gridx, int gridy, int gridwidth, int gridheight) {
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,5,3,5);

        c.gridx = gridx;
        c.gridy = gridy;
        c.gridwidth = gridwidth;
        c.gridheight = gridheight;
        return c;
    }

    public static GridBagConstraints setGbcWithComponent(GridBagConstraints gbc, Container mother, Component child, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        mother.add(child, gbc);
        return gbc;
    }

    // this may not be right, don't use it for now -- 2007.6.13
    public static int getTableHeight(JTable table, boolean hasHeader) {
        int head = 0;
        if (hasHeader) head = 1;
        return (table.getRowHeight() + table.getRowMargin())*(table.getRowCount()+head);
    }

    public static DefaultCellEditor getCheckboxCellEditor() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setForeground(Color.white);
        checkBox.setBackground(Color.white);
        checkBox.setHorizontalAlignment(JLabel.CENTER);
        return new DefaultCellEditor(checkBox);
    }
    
    public static Color[] makeColormap(int numColors) {
        Color[] colormap = new Color[numColors];
        int i, idx = 0;
        for (i = 0; i < 64; i++) {
            colormap[idx] = new Color(0, i * 4, 255);
            idx++;
        }
        for (i = 0; i < 64; i++) {
            colormap[idx] = new Color(0, 255, 255 - i * 4);
            idx++;
        }
        for (i = 0; i < 64; i++) {
            colormap[idx] = new Color(i * 4, 255, 0);
            idx++;
        }
        for (i = 0; i < 64; i++) {
            colormap[idx] = new Color(255, 255 - i * 4, 0);
            idx++;
        }
        return (colormap);
    }
}
