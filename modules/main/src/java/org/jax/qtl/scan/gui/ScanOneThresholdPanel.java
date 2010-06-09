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

package org.jax.qtl.scan.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.qtl.Constants;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.ScanOneThreshold;

/**
 * A panel for editing thresholds
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneThresholdPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2795048577522996703L;
    
    private static final double DEFAULT_THRESHOLD_ALPHA = 0.05;
    
    private final ScanOneGraph scanOneGraph;
    
    private final ScanOneResult scanOneResult;
    
    private final boolean xPermutationsAreSeparate;
    
    private DefaultTableModel thresholdTableModel;

    private SpinnerNumberModel alphaThresholdSpinnerModel;
    
    /**
     * Constructor
     * @param scanOneGraph
     *          the graph that we're building thresholds for
     */
    public ScanOneThresholdPanel(ScanOneGraph scanOneGraph)
    {
        this.scanOneGraph = scanOneGraph;
        this.scanOneResult = scanOneGraph.getScanOneResult();
        this.xPermutationsAreSeparate =
            this.scanOneResult.getXChromosomePValuesAreSeparate();
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Take care of the GUI initialization that isn't handled by the
     * GUI builder
     */
    private void postGuiInit()
    {
        final String[] thresholdTableHeader;
        if(this.xPermutationsAreSeparate)
        {
            thresholdTableHeader = new String[] {
                    "Alpha",
                    "Autosome LOD Score",
                    "X Chromosome LOD Score"
            };
        }
        else
        {
            thresholdTableHeader = new String[] {
                    "Alpha",
                    "LOD Score"
            };
        }
        
        this.alphaThresholdSpinnerModel = new SpinnerNumberModel(
                0.1,    // initial value
                0.0,    // min
                1.0,    // max
                0.05);  // step size
        this.alphaValueSpinner.setModel(this.alphaThresholdSpinnerModel);
        this.alphaValueSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ScanOneThresholdPanel.this.thresholdAlphaChanged();
            }
        });
        
        this.showThresholdLabelCheckBox.setSelected(
                this.scanOneGraph.getShowThresholdLabel());
        this.showThresholdLabelCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                ScanOneThresholdPanel.this.scanOneGraph.setShowThresholdLabel(
                        e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        
        this.thresholdTableModel = new DefaultTableModel(
                thresholdTableHeader,
                0);
        this.thresholdListTable.setModel(this.thresholdTableModel);
        this.thresholdListTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        this.thresholdListTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        ScanOneThresholdPanel.this.rowSelectionChanged();
                    }
                });
        ScanOneThreshold[] thresholds = this.scanOneGraph.getThresholdsToRender();
        if(thresholds != null && thresholds.length > 0)
        {
            for(int i = 0; i < thresholds.length; i++)
            {
                ThresholdTableCell[] row = this.createRow(thresholds[i]);
                this.thresholdTableModel.addRow(row);
            }
            this.thresholdListTable.getSelectionModel().setSelectionInterval(
                    0,
                    0);
        }
        
        this.rowSelectionChanged();
    }
    
    /**
     * respond to a change in row selection
     */
    private void rowSelectionChanged()
    {
        ScanOneThreshold selectedThreshold = this.getSelectedThreshold();
        if(selectedThreshold != null)
        {
            this.alphaThresholdSpinnerModel.setValue(
                    Double.valueOf(selectedThreshold.getAlphaThreshold()));
            this.alphaValueLabel.setEnabled(true);
            this.alphaValueSpinner.setEnabled(true);
            this.removeThresholdButton.setEnabled(true);
        }
        else
        {
            this.alphaValueLabel.setEnabled(false);
            this.alphaValueSpinner.setEnabled(false);
            this.removeThresholdButton.setEnabled(false);
        }
    }

    /**
     * Get the selected threshold value
     * @return
     *          the selected value or null if no value is selected
     */
    private ScanOneThreshold getSelectedThreshold()
    {
        int selectedRow = this.thresholdListTable.getSelectedRow();
        if(selectedRow >= 0)
        {
            ThresholdTableCell cell =
                (ThresholdTableCell)this.thresholdTableModel.getValueAt(
                        selectedRow,
                        0);
            return cell.getThreshold();
        }
        else
        {
            return null;
        }
    }

    /**
     * update the selected row using the current alpha value
     */
    private void updateSelectedRow()
    {
        int selectedRow = this.thresholdListTable.getSelectedRow();
        if(selectedRow >= 0)
        {
            ScanOneThreshold thresholdUpdate = this.scanOneResult.calculateThreshold(
                    this.alphaThresholdSpinnerModel.getNumber().doubleValue(),
                    this.scanOneGraph.getLodColumnName());
            int columnCount = this.thresholdTableModel.getColumnCount();
            for(int column = 0; column < columnCount; column++)
            {
                ThresholdTableCell cell =
                    (ThresholdTableCell)this.thresholdTableModel.getValueAt(
                            selectedRow,
                            column);
                cell.setThreshold(thresholdUpdate);
            }
        }
        this.thresholdListTable.repaint();
    }
    
    /**
     * Create a new table row from the given threshold value
     * @param threshold
     *          the threshold to create a row for
     * @return
     *          the row
     */
    private ThresholdTableCell[] createRow(ScanOneThreshold threshold)
    {
        final ThresholdTableCell[] row = new ThresholdTableCell[
            threshold.getXChromosomePValuesAreSeparate() ? 3 : 2];
        for(int i = 0; i < row.length; i++)
        {
            row[i] = new ThresholdTableCell(threshold, i);
        }
        return row;
    }
    
    /**
     * a private cell class for the threshold table
     */
    private static class ThresholdTableCell
    {
        private volatile ScanOneThreshold threshold;
        
        private final int tableColumn;

        /**
         * Constructor
         * @param threshold
         *          the threshold
         * @param tableColumn
         *          the cells column in the table
         */
        public ThresholdTableCell(ScanOneThreshold threshold, int tableColumn)
        {
            this.threshold = threshold;
            this.tableColumn = tableColumn;
        }
        
        /**
         * @return the tableColumn
         */
        public int getTableColumn()
        {
            return this.tableColumn;
        }
        
        /**
         * @return the threshold
         */
        public ScanOneThreshold getThreshold()
        {
            return this.threshold;
        }
        
        /**
         * @param threshold the threshold to set
         */
        public void setThreshold(ScanOneThreshold threshold)
        {
            this.threshold = threshold;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            switch(this.tableColumn)
            {
                case 0: return Constants.THREE_DIGIT_FORMATTER.format(
                        this.threshold.getAlphaThreshold());
                case 1: return Constants.THREE_DIGIT_FORMATTER.format(
                        this.threshold.getAutosomeLodValue());
                case 2: return Constants.THREE_DIGIT_FORMATTER.format(
                        this.threshold.getXChromosomeLodValue());
                default: return null;
            }
        }
    }
    
    /**
     * Respond to a change in the spinners alpha value
     */
    private void thresholdAlphaChanged()
    {
        this.updateSelectedRow();
        this.sendThresholdsToGraph();
    }

    /**
     * Remove rows that are currently selected
     */
    private void removeSelectedThresholds()
    {
        int[] selectedRows = this.thresholdListTable.getSelectedRows();
        Arrays.sort(selectedRows);
        
        for(int rowIndex = selectedRows.length - 1; rowIndex >= 0; rowIndex--)
        {
            this.thresholdTableModel.removeRow(selectedRows[rowIndex]);
        }
        this.sendThresholdsToGraph();
    }
    
    /**
     * Write all of the thresholds currently in the table to a graph
     */
    private void sendThresholdsToGraph()
    {
        int rowCount = this.thresholdTableModel.getRowCount();
        ScanOneThreshold[] thresholds = new ScanOneThreshold[rowCount];
        for(int row = 0; row < rowCount; row++)
        {
            ThresholdTableCell currCell =
                (ThresholdTableCell)this.thresholdTableModel.getValueAt(row, 0);
            thresholds[row] = currCell.getThreshold();
        }
        
        this.scanOneGraph.setThresholdsToRender(thresholds);
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

        alphaValueLabel = new javax.swing.JLabel();
        alphaValueSpinner = new javax.swing.JSpinner();
        showThresholdLabelCheckBox = new javax.swing.JCheckBox();
        thresholdListScrollPanel = new javax.swing.JScrollPane();
        thresholdListTable = new javax.swing.JTable();
        addThresholdButton = new javax.swing.JButton();
        removeThresholdButton = new javax.swing.JButton();

        alphaValueLabel.setText("Alpha Threshold:");

        showThresholdLabelCheckBox.setText("Show Threshold Label");

        thresholdListScrollPanel.setViewportView(thresholdListTable);

        addThresholdButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/add-16x16.png"))); // NOI18N
        addThresholdButton.setText("New Threshold");
        addThresholdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addThresholdButtonActionPerformed(evt);
            }
        });

        removeThresholdButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/remove-16x16.png"))); // NOI18N
        removeThresholdButton.setText("Remove Selected");
        removeThresholdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeThresholdButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(thresholdListScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(alphaValueLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(alphaValueSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showThresholdLabelCheckBox))
                    .add(layout.createSequentialGroup()
                        .add(addThresholdButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeThresholdButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(alphaValueLabel)
                    .add(alphaValueSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(showThresholdLabelCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(thresholdListScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addThresholdButton)
                    .add(removeThresholdButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addThresholdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addThresholdButtonActionPerformed
        ScanOneThreshold newThreshold =
            this.scanOneResult.calculateThreshold(
                    DEFAULT_THRESHOLD_ALPHA,
                    this.scanOneGraph.getLodColumnName());
        ThresholdTableCell[] newRow = this.createRow(newThreshold);
        this.thresholdTableModel.addRow(newRow);
        int rowCount = this.thresholdTableModel.getRowCount();
        this.thresholdListTable.getSelectionModel().setSelectionInterval(
                rowCount - 1,
                rowCount - 1);
        this.sendThresholdsToGraph();
    }//GEN-LAST:event_addThresholdButtonActionPerformed

    private void removeThresholdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeThresholdButtonActionPerformed
        this.removeSelectedThresholds();
    }//GEN-LAST:event_removeThresholdButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addThresholdButton;
    private javax.swing.JLabel alphaValueLabel;
    private javax.swing.JSpinner alphaValueSpinner;
    private javax.swing.JButton removeThresholdButton;
    private javax.swing.JCheckBox showThresholdLabelCheckBox;
    private javax.swing.JScrollPane thresholdListScrollPanel;
    private javax.swing.JTable thresholdListTable;
    // End of variables declaration//GEN-END:variables
    
}
