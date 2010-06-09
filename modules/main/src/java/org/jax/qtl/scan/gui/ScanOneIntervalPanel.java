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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.gui.ScanOneInterval.IntervalType;
import org.jax.r.RCommand;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.gui.CheckableListTableModel;

/**
 * Panel for viewing and editing the interval settings
 */
public class ScanOneIntervalPanel extends JPanel
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneIntervalPanel.class.getName());
    
    /**
     * Every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6458058262545783908L;
    
    /**
     * The checkable table model used by 
     */
    private final CheckableListTableModel chromosomeTableModel;
    
    /**
     * The builder we use to create the interval R commands
     */
    private final ScanOneIntervalCommandBuilder scanOneIntervalCommandBuilder;
    
    /**
     * The spinner model to use for probability coverage
     */
    private final SpinnerNumberModel credibleIntervalSpinnerModel;
    
    /**
     * The spinner model to use for LOD drop
     */
    private final SpinnerNumberModel lodDropSpinnerModel;
    
    /**
     * Constructor
     * @param scanOneIntervalCommandBuilder
     *          the scan one command builder
     */
    public ScanOneIntervalPanel(
            ScanOneIntervalCommandBuilder scanOneIntervalCommandBuilder)
    {
        this.initComponents();
        
        this.scanOneIntervalCommandBuilder =
            scanOneIntervalCommandBuilder;
        this.chromosomeTableModel = new CheckableListTableModel(1);
        this.chromosomesTable.setModel(this.chromosomeTableModel);
        this.credibleIntervalSpinnerModel = new SpinnerNumberModel(
                0.0,
                0.0,
                1.0,
                0.01);
        this.credibleIntervalSpinner.setModel(
                this.credibleIntervalSpinnerModel);
        this.lodDropSpinnerModel = new SpinnerNumberModel(
                0.0,
                0.0,
                50.0,
                0.5);
        this.lodDropSpinner.setModel(this.lodDropSpinnerModel);
        
        this.postGuiInit();
    }
    
    /**
     * Respond to an update in the command
     */
    private void scanOneIntervalCommandWasUpdated()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ScanOneIntervalPanel.this.printIntervalsToTerminalButton.setEnabled(
                        ScanOneIntervalPanel.this.scanOneIntervalCommandBuilder.getCommands() != null);
            }
        });
    }
    
    /**
     * Respond to an update in probability coverage
     */
    private void credibleIntervalWasUpdated()
    {
        double probabilityCoverage =
            this.credibleIntervalSpinnerModel.getNumber().doubleValue();
        this.scanOneIntervalCommandBuilder.setProbabilityCoverage(
                probabilityCoverage);
    }

    /**
     * Respond to an update in the LOD drop value
     */
    private void lodDropWasUpdated()
    {
        double lodDrop =
            this.lodDropSpinnerModel.getNumber().doubleValue();
        this.scanOneIntervalCommandBuilder.setLodDrop(lodDrop);
    }
    
    /**
     * Respond to an update in the selected chromosomes.
     */
    private void chromosomesToScanWasUpdated()
    {
        List<String> selectedChromosomes = new ArrayList<String>();
        
        int rowCount = this.chromosomeTableModel.getRowCount();
        for(int row = 0; row < rowCount; row++)
        {
            Boolean isSelected =
                (Boolean)this.chromosomeTableModel.getValueAt(row, 0);
            if(isSelected.booleanValue())
            {
                String chromosomeName =
                    (String)this.chromosomeTableModel.getValueAt(row, 1);
                selectedChromosomes.add(chromosomeName);
            }
        }
        
        this.scanOneIntervalCommandBuilder.setChromosomeNames(
                selectedChromosomes.toArray(
                        new String[selectedChromosomes.size()]));
    }

    /**
     * Refresh the chromosome table contents.
     */
    private void postGuiInit()
    {
        this.chromosomeTableModel.setRowCount(0);
        
        ScanOneResult scanOneResult =
            this.scanOneIntervalCommandBuilder.getScanOneResult();
        
        // take care of the table
        List<String> selectedScannedChromosomeList;
        {
            String[] selectedScannedChromosomes =
                this.scanOneIntervalCommandBuilder.getChromosomeNames();
            if(selectedScannedChromosomes == null)
            {
                selectedScannedChromosomeList = Collections.emptyList();
            }
            else
            {
                selectedScannedChromosomeList = Arrays.asList(
                        selectedScannedChromosomes);
            }
        }
        
        if(scanOneResult != null)
        {
            String[] scannedChromosomes = scanOneResult.getScannedChromosomes();
            for(int i = 0; i < scannedChromosomes.length; i++)
            {
                if(selectedScannedChromosomeList.contains(scannedChromosomes[i]))
                {
                    this.chromosomeTableModel.addRow(new Object[] {
                            Boolean.TRUE,
                            scannedChromosomes[i]});
                }
                else
                {
                    this.chromosomeTableModel.addRow(new Object[] {
                            Boolean.FALSE,
                            scannedChromosomes[i]});
                }
            }
        }
        else
        {
            LOG.warning(
                    "chromosome table is empty because the scan results " +
                    "are null");
        }
        
        // update the probability coverage && lod drop
        this.credibleIntervalSpinnerModel.setValue(Double.valueOf(
                this.scanOneIntervalCommandBuilder.getProbabilityCoverage()));
        this.lodDropSpinnerModel.setValue(Double.valueOf(
                this.scanOneIntervalCommandBuilder.getLodDrop()));
        
        // remove the table header and resize the rows
        this.chromosomesTable.getTableHeader().setVisible(false);
        this.chromosomesTable.getTableHeader().setPreferredSize(
                new Dimension(0, 0));
        TableColumnModel chromosomesColumnModel =
            this.chromosomesTable.getColumnModel();
        chromosomesColumnModel.getColumn(0).setPreferredWidth(0);
        chromosomesColumnModel.getColumn(1).setPreferredWidth(1000);
        
        // do a refresh
        this.scanOneIntervalCommandWasUpdated();
        
        // add listeners
        this.scanOneIntervalCommandBuilder.addPropertyChangeListener(
                new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        ScanOneIntervalPanel.this.scanOneIntervalCommandWasUpdated();
                    }
                });
        this.chromosomeTableModel.addTableModelListener(
                new TableModelListener()
                {
                    public void tableChanged(TableModelEvent e)
                    {
                        if(e.getType() == TableModelEvent.UPDATE)
                        {
                            ScanOneIntervalPanel.this.chromosomesToScanWasUpdated();
                        }
                    }
                });
        this.credibleIntervalSpinnerModel.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        ScanOneIntervalPanel.this.credibleIntervalWasUpdated();
                    }
                });
        this.lodDropSpinnerModel.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        ScanOneIntervalPanel.this.lodDropWasUpdated();
                    }
                });
        
        this.refreshIntervalType();
    }
    
    /**
     * Getter for the command builder that this dialog is editing
     * @return
     *          the command builder
     */
    public ScanOneIntervalCommandBuilder getScanOneIntervalCommandBuilder()
    {
        return this.scanOneIntervalCommandBuilder;
    }
    
    /**
     * Update the GUI based on what the current interval type is in the
     * command builder.
     */
    private void refreshIntervalType()
    {
        IntervalType intervalType =
            this.scanOneIntervalCommandBuilder.getIntervalType();
        switch(intervalType)
        {
            case BAYESIAN_CREDIBLE:
            {
                this.credibleIntervalRadioButton.setSelected(true);
                this.credibleIntervalSpinner.setEnabled(true);
                this.lodDropSpinner.setEnabled(false);
            }
            break;
            
            case LOD_SUPPORT:
            {
                this.lodDropRadioButton.setSelected(true);
                this.lodDropSpinner.setEnabled(true);
                this.credibleIntervalSpinner.setEnabled(false);
            }
            break;
            
            default:
            {
                LOG.severe("unknown scanone interval type: " + intervalType);
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

        intervalMethodButonGroup = new javax.swing.ButtonGroup();
        chromosomesScrollPanel = new javax.swing.JScrollPane();
        chromosomesTable = new javax.swing.JTable();
        intervalMethodSelectionLabel = new javax.swing.JLabel();
        printIntervalsToTerminalButton = new javax.swing.JButton();
        chromosomesToScanLabel = new javax.swing.JLabel();
        toggleSelectAllChromosomesButton = new javax.swing.JButton();
        credibleIntervalRadioButton = new javax.swing.JRadioButton();
        lodDropRadioButton = new javax.swing.JRadioButton();
        lodDropSpinner = new javax.swing.JSpinner();
        credibleIntervalSpinner = new javax.swing.JSpinner();

        chromosomesScrollPanel.setMinimumSize(new java.awt.Dimension(50, 23));
        chromosomesScrollPanel.setViewportView(chromosomesTable);

        intervalMethodSelectionLabel.setText("Interval Calculation Method (Bayesian or LOD Drop):");

        printIntervalsToTerminalButton.setText("Print Interval(s) to Terminal");
        printIntervalsToTerminalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printIntervalsToTerminalButtonActionPerformed(evt);
            }
        });

        chromosomesToScanLabel.setText("Chromosomes to Calculate Interval On:");

        toggleSelectAllChromosomesButton.setText("Toggle Select All");
        toggleSelectAllChromosomesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSelectAllChromosomesButtonActionPerformed(evt);
            }
        });

        intervalMethodButonGroup.add(credibleIntervalRadioButton);
        credibleIntervalRadioButton.setText("Bayesian Credible Interval (0-1):");
        credibleIntervalRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                credibleIntervalRadioButtonItemStateChanged(evt);
            }
        });

        intervalMethodButonGroup.add(lodDropRadioButton);
        lodDropRadioButton.setText("LOD Score Drop Threshold:");
        lodDropRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lodDropRadioButtonItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chromosomesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                    .add(intervalMethodSelectionLabel)
                    .add(layout.createSequentialGroup()
                        .add(chromosomesToScanLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(toggleSelectAllChromosomesButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(credibleIntervalRadioButton)
                            .add(lodDropRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(lodDropSpinner)
                            .add(credibleIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(printIntervalsToTerminalButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(intervalMethodSelectionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(credibleIntervalRadioButton)
                    .add(credibleIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lodDropRadioButton)
                    .add(lodDropSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chromosomesToScanLabel)
                    .add(toggleSelectAllChromosomesButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chromosomesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(printIntervalsToTerminalButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void printIntervalsToTerminalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printIntervalsToTerminalButtonActionPerformed
        List<RCommand> intervalCommands =
                this.scanOneIntervalCommandBuilder.getCommands();
        if(intervalCommands != null) {
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            rInterface.insertComment(
                    "printing scanone interval values");
            for(RCommand currIntervalCommand: intervalCommands) {
                rInterface.evaluateCommandNoReturn(currIntervalCommand);
            }
        }
    }//GEN-LAST:event_printIntervalsToTerminalButtonActionPerformed

    private void credibleIntervalRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_credibleIntervalRadioButtonItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            this.scanOneIntervalCommandBuilder.setIntervalType(
                    IntervalType.BAYESIAN_CREDIBLE);
            this.refreshIntervalType();
        }
    }//GEN-LAST:event_credibleIntervalRadioButtonItemStateChanged

    private void lodDropRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lodDropRadioButtonItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            this.scanOneIntervalCommandBuilder.setIntervalType(
                    IntervalType.LOD_SUPPORT);
            this.refreshIntervalType();
        }
    }//GEN-LAST:event_lodDropRadioButtonItemStateChanged

    private void toggleSelectAllChromosomesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSelectAllChromosomesButtonActionPerformed
        this.chromosomeTableModel.toggleSelectAllCheckBoxes();
    }//GEN-LAST:event_toggleSelectAllChromosomesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane chromosomesScrollPanel;
    private javax.swing.JTable chromosomesTable;
    private javax.swing.JLabel chromosomesToScanLabel;
    private javax.swing.JRadioButton credibleIntervalRadioButton;
    private javax.swing.JSpinner credibleIntervalSpinner;
    private javax.swing.ButtonGroup intervalMethodButonGroup;
    private javax.swing.JLabel intervalMethodSelectionLabel;
    private javax.swing.JRadioButton lodDropRadioButton;
    private javax.swing.JSpinner lodDropSpinner;
    private javax.swing.JButton printIntervalsToTerminalButton;
    private javax.swing.JButton toggleSelectAllChromosomesButton;
    // End of variables declaration//GEN-END:variables
    
}
