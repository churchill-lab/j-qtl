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
package org.jax.qtl.ui;

import java.awt.Container;
import java.util.Vector;

import javax.swing.JTable;

import org.jax.qtl.util.CheckBoxTableCellRenderer;
import org.jax.qtl.util.ListSelectionTableModel;
import org.jax.qtl.util.Tools;

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
public class ListSelectionTable extends JTable{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -843352647932649400L;
    ThisListSelectionTableModel tableModel;
    Object[][] tableContents;
    String[] columnNames;
    Container parent;
    int totalTableWidth;
    final static int CHECKBOX_WIDTH = 30, TABLE_CELL_H_SPACE = 1, TABLE_CELL_V_SPACE = 1;
    boolean hasHeader = false;

    /**
     * Using an object array to create a table with the selection check box in front of each row.
     *
     * @param originalTableContent Object[] The 1-D object array
     * @param originalColumnName String The original array name
     * @param selectAll boolean If set all check boxes selected. If value is false, choose the first item
     * @param parent Container Pointer to it's parent Panel (this may not needed)
     */
    public ListSelectionTable(Object[] originalTableContent, String originalColumnName, boolean selectAll, Container parent) {
        int numRows = originalTableContent.length;
        Object[][] tableContent = new Object[numRows][1];
        for (int i = 0; i < numRows; i++) {
            tableContent[i][0] = originalTableContent[i];
        }
        String[] columnName = new String[1];
        columnName[0] = originalColumnName;
        makeSelectionTable(tableContent, columnName, selectAll, parent);
    }

    /**
     * Using an object 2-D array to create a table with the selection check box in front of each row.
     *
     * @param originalTableContents Object[][] The 2-D object arry
     * @param originalColumnNames String[] The original table header
     * @param selectAll boolean If set all check boxes selected. If value is false, choose the first item
     * @param parent Container Pointer to it's parent Panel (this may not needed)
     */
    public ListSelectionTable(Object[][] originalTableContents,
                              String[] originalColumnNames, boolean selectAll, Container parent) {
        makeSelectionTable(originalTableContents, originalColumnNames, selectAll, parent);
    }

    public void makeSelectionTable(Object[][] originalTableContents,
                                   String[] originalColumnNames, boolean selectAll, Container parent) {
        this.parent = parent;

        // add the first column(checkboxes) to the original data table to make it selectable.
        int columns = originalColumnNames.length + 1; // to add the first selection column
        int rows = originalTableContents.length + 1; // to add the first row with "All" as the option

        this.tableContents = new Object[rows][columns];
        for (int i = 0; i < rows; i++)
            this.tableContents[i][0] = new Boolean(selectAll);
        this.tableContents[0][1] = "All";
        // copy the old table contents
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < columns; c++) {
                this.tableContents[r][c] = originalTableContents[r - 1][c - 1];
            }
        }
        this.columnNames = new String[columns];
        this.columnNames[0] = "select";
        // copy the old column names
        for (int i = 1; i < columns; i++) {
            this.columnNames[i] = originalColumnNames[i - 1];
        }

        // creat the table model and add to this JTable
        this.tableModel = new ThisListSelectionTableModel(this.tableContents, this.columnNames, this);
        setModel(this.tableModel);

        // set this JTable's characters
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // set table renderer
        getColumnModel().getColumn(0).setCellRenderer(new CheckBoxTableCellRenderer());

        // set table editor
        getColumnModel().getColumn(0).setCellEditor(Tools.getCheckboxCellEditor());

        // set column width
        getColumnModel().getColumn(0).setPreferredWidth(CHECKBOX_WIDTH);
        int numColumns = getColumnCount();
//        int avaliableWidth = parent.getWidth();
//        int columnWidth = (avaliableWidth - 30 - 25)/(numColumns-1);
//        int columnWidth = 100;
        this.totalTableWidth = CHECKBOX_WIDTH + TABLE_CELL_H_SPACE;
        for (int i = 1; i < numColumns; i++) {
            int columnWidth = getColumnWidth(i);
            getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
            this.totalTableWidth += columnWidth + TABLE_CELL_H_SPACE;
        }

        if (!this.hasHeader)
            setTableHeader(null); // default is a table without header
        repaint();
    }

    public int getTableWidth() {
        return this.totalTableWidth;
    }

    public int getTableHeight() {
        return getRowCount() * (getRowHeight() + TABLE_CELL_V_SPACE) - 1;
    }

    private int getColumnWidth(int columnIndex) {
        int biggestWidth = 0;
        int numRows = this.tableContents.length;
        for (int i = 0; i < numRows; i++) {
            int columnWidth = this.tableContents[i][columnIndex].toString().length();
            if (biggestWidth < columnWidth) biggestWidth = columnWidth;
        }
        return biggestWidth * 10;
    }

    public void setDefaultSelection(int[] defaultSelections) {
        int numDefaultSelections = defaultSelections.length;
        if (numDefaultSelections > 0) {
            for (int i = 0; i < numDefaultSelections; i++) {
                this.tableContents[defaultSelections[i] + 1][0] = new Boolean(true);
                // if everyone is selected, set "All" as selected
                if (this.tableContents.length == numDefaultSelections + 1) {
                    this.tableContents[0][0] = new Boolean(true);
                }
            }
        }
    }

    public int[] getSelections() {
        int[] selections = null;
        int listSize = getRowCount();
        if (! ( (Boolean) this.tableContents[0][0]).booleanValue()) {
            Vector selectionsHolder = new Vector();
            for (int i = 1; i < listSize; i++) {
                boolean b = ( (Boolean) this.tableContents[i][0]).booleanValue();
                if (b)
                    selectionsHolder.add(new Integer(i - 1));
            }
            int numSelections = selectionsHolder.size();

            selections = new int[numSelections];
            for (int i = 0; i < numSelections; i++) {
                selections[i] = ( (Integer) selectionsHolder.elementAt(i)).intValue();
            }
        }
        else {
            selections = new int[listSize - 1];
            for (int i = 0; i < listSize - 1; i++) {
                selections[i] = i;
            }
        }
        return selections;
    }

    private void setHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        repaint();
    }

    /**
     * <p>Title: ListSelectionTableModel </p>
     *
     * <p>Description: Table model for building the selection list with the check box
     * as the first column.</p>
     *
     * <p>Copyright: Copyright (c) 2006</p>
     *
     * <p>Company: The Jackson Laboratory</p>
     *
     * @author Lei Wu
     * @version 1.0
     */
    class ThisListSelectionTableModel extends ListSelectionTableModel {
        /**
         * 
         */
        private static final long serialVersionUID = -8184190293009052248L;

        // constructor
        public ThisListSelectionTableModel(Object[][] tc, Object[] cn, JTable t) {
            super(tc, cn, t);
        }

        public void setValueAt(Object value, int r, int c) {
            // first line that is check/uncheck all
            // the rest lines changed at the same time
            if (r == 0) {
                Boolean b = (Boolean) value;
                // make all other lines the same as the first line
                int tableRows = this.tableContents.length;
                for (int i = 1; i < tableRows; i++) {
                    this.tableContents[i][c] = b;
                }
            }
            // not the first line
            else {
                boolean b = ( (Boolean) value).booleanValue();
                if (!b) {
                    this.tableContents[0][0] = new Boolean(false);
                }
                else {
                    boolean allSelected = true;
                    int tableRows = this.tableContents.length;
                    for (int i = 1; i < tableRows; i++) {
                        if ( (! ( (Boolean) (this.tableContents[i][0])).booleanValue()) && (i != r)) {
                            allSelected = false;
                            break;
                        }
                    }
                    if (allSelected)
                        this.tableContents[0][0] = new Boolean(true);
                    else
                        this.tableContents[0][0] = new Boolean(false);
                }
            }
            setValue(value, r, c);
        }
    }
}
