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

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
public class ListSelectionTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = -9067440351625517426L;
    public Object[][] tableContents;
    Object[] columnNames;
    protected JTable table;
    protected Vector vcTableContents;
    Vector vcColumnNames;
    final static int ARRAY_TC_ARRAY_CN = 0, VC_TC_VC_CN = 1, VC_TC_ARRAY_CN = 2, ARRAY_TC_VC_CN = 3;
    int dataType = ARRAY_TC_ARRAY_CN;

// constructor
    public ListSelectionTableModel(Object[][] tc, Object[] cn, JTable t) {
        this.tableContents = tc;
        this.columnNames = cn;
        this.table = t;
        this.dataType = ARRAY_TC_ARRAY_CN;
    }

    public ListSelectionTableModel(Vector tc, Vector cn, JTable t) {
        this.vcTableContents = tc;
        this.vcColumnNames = cn;
        this.table = t;
        this.dataType = VC_TC_VC_CN;
    }

    public ListSelectionTableModel(Vector tc, Object[] cn, JTable t) {
        this.vcTableContents = tc;
        this.columnNames = cn;
        this.table = t;
        this.dataType = VC_TC_ARRAY_CN;
    }

    public ListSelectionTableModel(Object[][] tc, Vector cn, JTable t) {
        this.tableContents = tc;
        this.vcColumnNames = cn;
        this.table = t;
        this.dataType = ARRAY_TC_VC_CN;
    }

    public int getRowCount() {
        int length = 0;
        switch (this.dataType) {
            case ARRAY_TC_ARRAY_CN:
            case ARRAY_TC_VC_CN:
                length = this.tableContents.length;
                break;
            case VC_TC_VC_CN:
            case VC_TC_ARRAY_CN:
                length = this.vcTableContents.size();
                break;
        }
        return length;
    }

    public int getColumnCount() {
        int count = 0;
        switch (this.dataType) {
            case ARRAY_TC_ARRAY_CN:
            case VC_TC_ARRAY_CN:
                count = this.columnNames.length;
                break;
            case VC_TC_VC_CN:
            case ARRAY_TC_VC_CN:
                count = this.vcColumnNames.size();
                break;
        }
        return count;
    }

    public String getColumnName(int col) {
        String colName = "";
        switch (this.dataType) {
            case ARRAY_TC_ARRAY_CN:
            case VC_TC_ARRAY_CN:
                colName = (String)this.columnNames[col];
                break;
            case VC_TC_VC_CN:
            case ARRAY_TC_VC_CN:
                colName = (String)this.vcColumnNames.elementAt(col);
                break;
        }
        return colName;
    }

    public Object getValueAt(int r, int c) {
        Object value = null;
        switch (this.dataType) {
            case ARRAY_TC_ARRAY_CN:
            case ARRAY_TC_VC_CN:
                value = this.tableContents[r][c];
                break;
            case VC_TC_VC_CN:
            case VC_TC_ARRAY_CN:
                value = ((Object[])this.vcTableContents.elementAt(r))[c];
                break;
        }
        return value;
    }

    public void setValueAt(Object value, int r, int c) {
        setValue(value, r, c);
    }

    protected void setValue(Object value, int r, int c) {
        switch (this.dataType) {
            case ARRAY_TC_ARRAY_CN:
            case ARRAY_TC_VC_CN:
                this.tableContents[r][c] = value;
                break;
            case VC_TC_VC_CN:
            case VC_TC_ARRAY_CN:
                ((Object[])this.vcTableContents.elementAt(r))[c] = value;
                break;
        }
        fireTableCellUpdated(r, c);
        this.table.repaint();
    }

    public boolean isCellEditable(int r, int c) {
        return c == 0;
    }
}
