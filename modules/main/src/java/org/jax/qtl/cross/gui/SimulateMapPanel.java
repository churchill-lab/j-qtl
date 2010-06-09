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

package org.jax.qtl.cross.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.qtl.cross.SimulateMapCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.util.TextWrapper;

/**
 * Panel for editing a simulate map command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimulateMapPanel extends RCommandEditorPanel
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SimulateMapPanel.class.getName());
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2161949438215349056L;
    
    private DefaultTableModel chromosomeTableModel;
    
    private final SimulateMapCommandBuilder simulateMapCommandBuilder;
    
    private final SpinnerNumberModel selectedChromosomeLengthModel =
        new SpinnerNumberModel(
                100.0,  // value
                0.0,    // min
                100.0,  // max
                1.0);   // step
    
    private final SpinnerNumberModel markersPerChromosomeModel =
        new SpinnerNumberModel(
                1,                  // value
                1,                  // min
                Integer.MAX_VALUE,  // max
                1);                 // step
    
    /**
     * Constructor
     * @param simulateMapCommandBuilder
     *          command builder that this panel edits
     */
    public SimulateMapPanel(SimulateMapCommandBuilder simulateMapCommandBuilder)
    {
        this.simulateMapCommandBuilder = simulateMapCommandBuilder;
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * 
     */
    private void postGuiInit()
    {
        this.markersPerChromosomeSpinner.setModel(
                this.markersPerChromosomeModel);
        this.markersPerChromosomeSpinner.setValue(
                this.simulateMapCommandBuilder.getMarkersPerChromosome());
        this.markersPerChromosomeModel.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        SimulateMapPanel.this.updateCommand();
                    }
                });
        
        ItemListener updateOnItemChangeListener = new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                SimulateMapPanel.this.updateCommand();
            }
        };
        
        this.includeTelomeresCheckBox.setSelected(
                this.simulateMapCommandBuilder.getIncludeTelomereMarkers());
        this.includeTelomeresCheckBox.addItemListener(
                updateOnItemChangeListener);
        
        this.useEqualMarkerSpacingCheckBox.setSelected(
                this.simulateMapCommandBuilder.getUseEqualMarkerSpacing());
        this.useEqualMarkerSpacingCheckBox.addItemListener(
                updateOnItemChangeListener);
        
        this.includeXChromosomeCheckBox.setSelected(
                this.simulateMapCommandBuilder.getIncludeXChromosome());
        this.includeXChromosomeCheckBox.addItemListener(
                new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        SimulateMapPanel.this.updateCommand();
                        SimulateMapPanel.this.chromosomeTable.repaint();
                    }
                });
        
        this.selectedChromosomesLenthSpinner.setModel(
                this.selectedChromosomeLengthModel);
        this.selectedChromosomesLenthSpinner.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        SimulateMapPanel.this.updateSelectedChromosomeLength();
                        SimulateMapPanel.this.chromosomeTable.repaint();
                    }
                });
        
        this.chromosomeTableModel = new DefaultTableModel()
        {
            /**
             * every serializable is supposed to have one of these
             */
            private static final long serialVersionUID = -4195503319407081829L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        
        this.chromosomeTableModel.setColumnIdentifiers(new String[] {
                "Chromosome",
                "Length (cM)"});
        this.chromosomeTable.setModel(this.chromosomeTableModel);
        this.rebuildChromosomeTable();
        this.chromosomeTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        SimulateMapPanel.this.chromosomeSelectionChanged();
                    }
                });
        this.chromosomeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        
        this.chromosomeSelectionChanged();
    }

    private void rebuildChromosomeTable()
    {
        this.chromosomeTableModel.setRowCount(0);
        double[] chromoLengths = this.simulateMapCommandBuilder.getChromosomeLengths();
        
        for(int chromoLenIndex = 0; chromoLenIndex < chromoLengths.length; chromoLenIndex++)
        {
            this.chromosomeTableModel.addRow(new Object[] {
                    new ChromosomeTableCell(chromoLenIndex, 0),
                    new ChromosomeTableCell(chromoLenIndex, 1)});
        }
    }
    
    /**
     * update the length of the selected chromosome
     */
    private void updateSelectedChromosomeLength()
    {
        double[] chromoLengths =
            this.simulateMapCommandBuilder.getChromosomeLengths().clone();
        
        int selectedIndex = this.chromosomeTable.getSelectedRow();
        if(selectedIndex >= 0 && selectedIndex < chromoLengths.length)
        {
            chromoLengths[selectedIndex] =
                this.selectedChromosomeLengthModel.getNumber().doubleValue();
            LOG.fine("set chromosome length to: " + chromoLengths[selectedIndex]);
            this.simulateMapCommandBuilder.setChromosomeLengths(chromoLengths);
            this.fireCommandModified();
        }
    }
    
    private void chromosomeSelectionChanged()
    {
        double[] chromoLengths =
            this.simulateMapCommandBuilder.getChromosomeLengths();
        
        int selectedIndex = this.chromosomeTable.getSelectedRow();
        if(selectedIndex >= 0 && selectedIndex < chromoLengths.length)
        {
            this.selectedChromosomeLengthModel.setValue(
                    chromoLengths[selectedIndex]);
            this.removeSelectedChromosomesButton.setEnabled(true);
            this.selectedChromosomesLenthLabel.setEnabled(true);
            this.selectedChromosomesLenthSpinner.setEnabled(true);
        }
        else
        {
            this.removeSelectedChromosomesButton.setEnabled(false);
            this.selectedChromosomesLenthLabel.setEnabled(false);
            this.selectedChromosomesLenthSpinner.setEnabled(false);
        }
    }
    
    private void updateCommand()
    {
        this.simulateMapCommandBuilder.setMarkersPerChromosome(
                this.markersPerChromosomeModel.getNumber().intValue());
        this.simulateMapCommandBuilder.setIncludeTelomereMarkers(
                this.includeTelomeresCheckBox.isSelected());
        this.simulateMapCommandBuilder.setUseEqualMarkerSpacing(
                this.useEqualMarkerSpacingCheckBox.isSelected());
        this.simulateMapCommandBuilder.setIncludeXChromosome(
                this.includeXChromosomeCheckBox.isSelected());
        this.fireCommandModified();
    }
    
    private void addChromosome()
    {
        this.insertRow(
                this.simulateMapCommandBuilder.getChromosomeLengths().length);
    }
    
    private void removeSelectedChromosome()
    {
        double[] chromoLengths =
            this.simulateMapCommandBuilder.getChromosomeLengths();
        
        int selectedIndex = this.chromosomeTable.getSelectedRow();
        if(selectedIndex >= 0 && selectedIndex < chromoLengths.length)
        {
            double[] newChromoLengths = new double[chromoLengths.length - 1];
            
            for(int i = 0; i < chromoLengths.length; i++)
            {
                if(i < selectedIndex)
                {
                    newChromoLengths[i] = chromoLengths[i];
                }
                else if(i > selectedIndex)
                {
                    newChromoLengths[i - 1] = chromoLengths[i];
                }
            }
            
            this.simulateMapCommandBuilder.setChromosomeLengths(newChromoLengths);
            this.rebuildChromosomeTable();
            this.fireCommandModified();
        }
    }
    
    private void insertRow(int rowNumber)
    {
        double[] chromoLengths =
            this.simulateMapCommandBuilder.getChromosomeLengths();
        
        double[] newChromoLengths = new double[chromoLengths.length + 1];
        for(int i = 0; i <= chromoLengths.length; i++)
        {
            if(i < rowNumber)
            {
                newChromoLengths[i] = chromoLengths[i];
            }
            else if(i == rowNumber)
            {
                newChromoLengths[i] = 100;
            }
            else
            {
                newChromoLengths[i] = chromoLengths[i - 1];
            }
        }
        this.simulateMapCommandBuilder.setChromosomeLengths(newChromoLengths);
        this.rebuildChromosomeTable();
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.simulateMapCommandBuilder.getCommand()};
    }
    
    /**
     * The chromosome table cell
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    private class ChromosomeTableCell
    {
        private final int row;
        
        private final int column;

        /**
         * Constructor
         * @param row
         *          the table row for this cell
         * @param column
         *          the table column for this cell
         */
        public ChromosomeTableCell(int row, int column)
        {
            super();
            this.row = row;
            this.column = column;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            double[] chromoLengths =
                SimulateMapPanel.this.simulateMapCommandBuilder.getChromosomeLengths();
            
            if(this.row < 0 || this.row >= chromoLengths.length)
            {
                LOG.severe("chromosome row out of bounds: " + this.row);
                return "chromosome row out of bounds";
            }
            else if(this.column == 0)
            {
                if(this.row == chromoLengths.length - 1 &&
                   SimulateMapPanel.this.includeXChromosomeCheckBox.isSelected())
                {
                    return "X";
                }
                else
                {
                    return Integer.toString(this.row + 1);
                }
            }
            else if(this.column == 1)
            {
                return Double.toString(chromoLengths[this.row]);
            }
            else
            {
                LOG.severe("chromosome column out of bounds: " + this.column);
                return "chromosome column out of bounds";
            }
        }
    }

    /**
     * Validate the data in this panel
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        String validationErrorMessage = null;

        if(this.simulateMapCommandBuilder.getChromosomeLengths().length == 0)
        {
            validationErrorMessage =
                "Chromosome list cannot be empty. Please add at least one " +
                "chromosome or cancel.";
        }

        if(validationErrorMessage != null)
        {
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            validationErrorMessage,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                            "Validation Failed",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else
        {
            return true;
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

        javax.swing.JLabel markersPerChromosomeLabel = new javax.swing.JLabel();
        markersPerChromosomeSpinner = new javax.swing.JSpinner();
        includeTelomeresCheckBox = new javax.swing.JCheckBox();
        useEqualMarkerSpacingCheckBox = new javax.swing.JCheckBox();
        includeXChromosomeCheckBox = new javax.swing.JCheckBox();
        selectedChromosomesLenthLabel = new javax.swing.JLabel();
        selectedChromosomesLenthSpinner = new javax.swing.JSpinner();
        addChromosomeButton = new javax.swing.JButton();
        removeSelectedChromosomesButton = new javax.swing.JButton();
        javax.swing.JScrollPane chromosomesScrollPane = new javax.swing.JScrollPane();
        chromosomeTable = new javax.swing.JTable();

        markersPerChromosomeLabel.setText("Markers Per Chromosome:");

        includeTelomeresCheckBox.setText("Include Telomere Markers");

        useEqualMarkerSpacingCheckBox.setText("Use Equal Marker Spacing");

        includeXChromosomeCheckBox.setText("Include X Chromosome");

        selectedChromosomesLenthLabel.setText("Selected Chromosome Length (cM):");

        addChromosomeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/add-16x16.png"))); // NOI18N
        addChromosomeButton.setText("New Chromosome");
        addChromosomeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChromosomeButtonActionPerformed(evt);
            }
        });

        removeSelectedChromosomesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/remove-16x16.png"))); // NOI18N
        removeSelectedChromosomesButton.setText("Remove Selected");
        removeSelectedChromosomesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedChromosomesButtonActionPerformed(evt);
            }
        });

        chromosomeTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        chromosomesScrollPane.setViewportView(chromosomeTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chromosomesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(markersPerChromosomeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(markersPerChromosomeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(includeTelomeresCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useEqualMarkerSpacingCheckBox))
                    .add(layout.createSequentialGroup()
                        .add(selectedChromosomesLenthLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(selectedChromosomesLenthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(addChromosomeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeSelectedChromosomesButton))
                    .add(includeXChromosomeCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(markersPerChromosomeLabel)
                    .add(markersPerChromosomeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(includeTelomeresCheckBox)
                    .add(useEqualMarkerSpacingCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(includeXChromosomeCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedChromosomesLenthLabel)
                    .add(selectedChromosomesLenthSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chromosomesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addChromosomeButton)
                    .add(removeSelectedChromosomesButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addChromosomeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChromosomeButtonActionPerformed
        this.addChromosome();
}//GEN-LAST:event_addChromosomeButtonActionPerformed

    private void removeSelectedChromosomesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedChromosomesButtonActionPerformed
        this.removeSelectedChromosome();
}//GEN-LAST:event_removeSelectedChromosomesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addChromosomeButton;
    private javax.swing.JTable chromosomeTable;
    private javax.swing.JCheckBox includeTelomeresCheckBox;
    private javax.swing.JCheckBox includeXChromosomeCheckBox;
    private javax.swing.JSpinner markersPerChromosomeSpinner;
    private javax.swing.JButton removeSelectedChromosomesButton;
    private javax.swing.JLabel selectedChromosomesLenthLabel;
    private javax.swing.JSpinner selectedChromosomesLenthSpinner;
    private javax.swing.JCheckBox useEqualMarkerSpacingCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
