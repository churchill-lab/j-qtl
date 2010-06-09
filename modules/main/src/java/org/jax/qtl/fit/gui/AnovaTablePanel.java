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

package org.jax.qtl.fit.gui;

import javax.swing.table.DefaultTableModel;

import org.jax.qtl.Constants;
import org.jax.qtl.fit.AnovaTable;
import org.jax.util.io.DataTable;
import org.jax.util.io.JTableDataTable;

/**
 * A panel for showing the results of an ANOVA
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AnovaTablePanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3758401727819655100L;
    
    private final AnovaTable anovaTable;

    private final DataTable dataTable;
    
    /**
     * Constructor
     * @param anovaTable
     *          the ANOVA table that we want to show
     */
    public AnovaTablePanel(AnovaTable anovaTable)
    {
        this.anovaTable = anovaTable;
        this.initComponents();
        this.postGuiInit();
        this.dataTable = new JTableDataTable(this.anovaJTable);
    }
    
    /**
     * Gets the data table for this anova table
     * @return
     *          the data table
     */
    public DataTable getDataTable()
    {
        return this.dataTable;
    }
    
    /**
     * Take care of the initialization that isn't handled by the GUI
     * builder
     */
    private void postGuiInit()
    {
        String[] columnNames = this.anovaTable.getColumnNames();
        String[] jColumnNames = new String[columnNames.length + 1];
        jColumnNames[0] = "";
        for(int i = 0; i < columnNames.length; i++)
        {
            jColumnNames[i + 1] = columnNames[i];
        }
        
        DefaultTableModel tableModel = new DefaultTableModel(
                jColumnNames,
                0)
        {
            /**
             * every {@link java.io.Serializable} is supposed to have one of these
             */
            private static final long serialVersionUID = 4657569023376066825L;

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        
        double[][] matrixData = this.anovaTable.getMatrixData();
        for(int rowIndex = 0; rowIndex < matrixData.length; rowIndex++)
        {
            AnovaTableCell[] jMatrixRow = new AnovaTableCell[jColumnNames.length];
            for(int columnIndex = 0; columnIndex < jMatrixRow.length; columnIndex++)
            {
                jMatrixRow[columnIndex] = new AnovaTableCell(rowIndex, columnIndex);
            }
            
            tableModel.addRow(jMatrixRow);
        }
        this.anovaJTable.setModel(tableModel);
    }
    
    /**
     * An internal class for rendering ANOVA table cells
     */
    public class AnovaTableCell
    {
        private final int row;
        
        private final int column;

        /**
         * Create a new cell for the JTable using a row and column number
         * @param row
         *          the row of this cell
         * @param column
         *          the column of this cell
         */
        public AnovaTableCell(int row, int column)
        {
            this.row = row;
            this.column = column;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            // the 1st column gets special treatment since
            // it corresponds to the row name from R/qtl
            if(this.column == 0)
            {
                return AnovaTablePanel.this.anovaTable.getRowNames()[this.row];
            }
            else
            {
                double[][] matrixValues =
                    AnovaTablePanel.this.anovaTable.getMatrixData();
                double cellValue = matrixValues[this.row][this.column - 1];
                if(Double.isNaN(cellValue))
                {
                    return "";
                }
                else
                {
                    return Constants.THREE_DIGIT_FORMATTER.format(cellValue);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        anovaTableScrollPanel = new javax.swing.JScrollPane();
        anovaJTable = new javax.swing.JTable();

        anovaTableScrollPanel.setViewportView(anovaJTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, anovaTableScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(anovaTableScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable anovaJTable;
    private javax.swing.JScrollPane anovaTableScrollPanel;
    // End of variables declaration//GEN-END:variables
    
}
