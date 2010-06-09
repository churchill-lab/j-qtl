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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.scan.PhenotypeDistribution;
import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.qtl.ui.CalcGenoprobDialog;
import org.jax.util.TextWrapper;
import org.jax.util.gui.CheckableListTableModel;

/**
 * A panel for editing the cross and chromosome parts of a {@link ScanCommandBuilder}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CrossAndPhenotypesScanPanel extends ScanCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8330114863082705178L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            CrossAndPhenotypesScanPanel.class.getName());
    
    /**
     * the command that this panel edits
     */
    private final ScanCommandBuilder scanCommand;
    
    private final JDialog parentDialog;

    private volatile Cross selectedCross;
    
    private volatile CheckableListTableModel phenotypesToScanTableModel;
    
    private final TableModelListener phenotypesToScanListener = new TableModelListener()
    {
        public void tableChanged(TableModelEvent e)
        {
            CrossAndPhenotypesScanPanel.this.phenotypeSelectionChanged();
        }
    };
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent dialog that we can create dialogs from
     * @param scanCommand
     *          the command that this panel will edit
     * @param availableCrosses
     *          the crosses that we can select from
     * @param selectedCross
     *          the default selection (null means "don't care")
     */
    public CrossAndPhenotypesScanPanel(
            JDialog parentDialog,
            ScanCommandBuilder scanCommand,
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        this.parentDialog = parentDialog;
        this.scanCommand = scanCommand;
        this.initComponents();
        this.postGuiInit(availableCrosses, selectedCross);
    }
    
    /**
     * respond to a change in the phenotype selection
     */
    private void phenotypeSelectionChanged()
    {
        List<Integer> indices = new ArrayList<Integer>();
        int numRows = this.phenotypesToScanTableModel.getRowCount();
        for(int row = 0; row < numRows; row++)
        {
            if(Boolean.TRUE.equals(this.phenotypesToScanTableModel.getValueAt(row, 0)))
            {
                indices.add(row);
            }
        }
        
        int[] indicesArray = new int[indices.size()];
        for(int i = 0; i < indicesArray.length; i++)
        {
            indicesArray[i] = indices.get(i);
        }
        this.setSelectedPhenotypeIndices(indicesArray);
    }

    /**
     * Handle the initialization that the GUI builder doesn't take care of
     * @param availableCrosses
     *          the crosses that the user can pick from
     * @param selectedCross
     *          the cross that is currently selected
     */
    private void postGuiInit(
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        this.phenotypesToScanTableModel = new CheckableListTableModel(
                new String[] {"Selected", "Phenotype"}, 1);
        
        this.setAvailableCrosses(availableCrosses);
        
        // fill in the phenotype distribution combo box
        PhenotypeDistribution[] supportedDistributions =
            this.scanCommand.getScanType().getSupportedPhenotypeDistributions();
        DefaultComboBoxModel phenoDistComboModel =
            (DefaultComboBoxModel)this.phenotypeDistributionComboBox.getModel();
        for(PhenotypeDistribution currPhenoDist: supportedDistributions)
        {
            phenoDistComboModel.addElement(currPhenoDist);
        }
        
        if(selectedCross != null)
        {
            this.crossComboBox.setSelectedItem(selectedCross);
            this.setSelectedCross(selectedCross);
        }
        else if(availableCrosses.length > 0)
        {
            this.crossComboBox.setSelectedItem(availableCrosses[0]);
            this.setSelectedCross(availableCrosses[0]);
        }
        
        this.phenotypesToScanTable.setModel(this.phenotypesToScanTableModel);
    }
    
    /**
     * Setter for the selected cross
     * @param selectedCross the selectedCross to set
     */
    private void setSelectedCross(Cross selectedCross)
    {
        if(selectedCross == null)
        {
            throw new NullPointerException(
                    "Null crosses are not allowed");
        }
        
        this.selectedCross = selectedCross;
        this.scanCommand.setCross(selectedCross);
        this.buildPhenotypeModel(selectedCross);
        this.fireCommandModified();
    }
    
    /**
     * Build the phenotype list model from the given cross
     * @param selectedCross
     *          the cross
     */
    private void buildPhenotypeModel(Cross selectedCross)
    {
        String[] phenotypeNames =
            selectedCross.getPhenotypeData().getDataNames();
        this.phenotypesToScanTableModel.removeTableModelListener(
                this.phenotypesToScanListener);
        this.phenotypesToScanTableModel.setRowCount(0);
        if(phenotypeNames.length > 0)
        {
            for(int i = 0; i < phenotypeNames.length; i++)
            {
                this.phenotypesToScanTableModel.addRow(
                        new Object[] {Boolean.FALSE, phenotypeNames[i]});
            }
            
            this.phenotypesToScanTableModel.addTableModelListener(
                    this.phenotypesToScanListener);
        }
        
        this.phenotypeSelectionChanged();
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

        crossLabel = new javax.swing.JLabel();
        crossComboBox = new javax.swing.JComboBox();
        phenotypeLabel = new javax.swing.JLabel();
        phenotypeDistributionLabel = new javax.swing.JLabel();
        phenotypeDistributionComboBox = new javax.swing.JComboBox();
        javax.swing.JScrollPane phenotypesToScanScrollPane = new javax.swing.JScrollPane();
        phenotypesToScanTable = new javax.swing.JTable();
        toggleSelectAllChromosomesButton = new javax.swing.JButton();

        crossLabel.setText("Cross:");

        crossComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                crossComboBoxItemStateChanged(evt);
            }
        });

        phenotypeLabel.setText("Phenotype(s) to Scan:");

        phenotypeDistributionLabel.setText("Phenotype Distribution:");

        phenotypeDistributionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                phenotypeDistributionComboBoxItemStateChanged(evt);
            }
        });

        phenotypesToScanScrollPane.setViewportView(phenotypesToScanTable);

        toggleSelectAllChromosomesButton.setText("Toggle Select All");
        toggleSelectAllChromosomesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSelectAllChromosomesButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phenotypesToScanScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(phenotypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(toggleSelectAllChromosomesButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(phenotypeDistributionLabel)
                            .add(crossLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(phenotypeDistributionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossLabel)
                    .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phenotypeDistributionLabel)
                    .add(phenotypeDistributionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phenotypeLabel)
                    .add(toggleSelectAllChromosomesButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(phenotypesToScanScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void setSelectedPhenotypeIndices(int[] selectedPhenotypeIndices)
    {
        this.scanCommand.setPhenotypeIndices(selectedPhenotypeIndices);
        this.fireCommandModified();
    }

    private void crossComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crossComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.setSelectedCross((Cross)this.crossComboBox.getSelectedItem());
        }
    }//GEN-LAST:event_crossComboBoxItemStateChanged

    private void phenotypeDistributionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_phenotypeDistributionComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.setSelectedPhenotypeDistribution(
                    (PhenotypeDistribution)this.phenotypeDistributionComboBox.getSelectedItem());
        }
    }//GEN-LAST:event_phenotypeDistributionComboBoxItemStateChanged

    private void toggleSelectAllChromosomesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSelectAllChromosomesButtonActionPerformed
        this.phenotypesToScanTableModel.toggleSelectAllCheckBoxes();
    }//GEN-LAST:event_toggleSelectAllChromosomesButtonActionPerformed
    
    
    
    /**
     * Setter for the phenotype distribution
     * @param phenotypeDistribution
     *          the new phenotype distribution value
     */
    private void setSelectedPhenotypeDistribution(
            PhenotypeDistribution phenotypeDistribution)
    {
        if(this.scanCommand.getPhenotypeDistribution() != phenotypeDistribution)
        {
            this.scanCommand.setPhenotypeDistribution(phenotypeDistribution);
            this.fireCommandModified();
        }
        
        this.phenotypeDistributionComboBox.setSelectedItem(
                phenotypeDistribution);
    }

    /**
     * Setter for the list of crosses that the user can select from
     * @param availableCrosses
     *          the new list of available crosses
     */
    private void setAvailableCrosses(Cross[] availableCrosses)
    {
        // update the combo box with the new crosses
        DefaultComboBoxModel crossComboModel =
            (DefaultComboBoxModel)this.crossComboBox.getModel();
        crossComboModel.removeAllElements();
        for(Cross currCross: availableCrosses)
        {
            crossComboModel.addElement(currCross);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox crossComboBox;
    private javax.swing.JLabel crossLabel;
    private javax.swing.JComboBox phenotypeDistributionComboBox;
    private javax.swing.JLabel phenotypeDistributionLabel;
    private javax.swing.JLabel phenotypeLabel;
    private javax.swing.JTable phenotypesToScanTable;
    private javax.swing.JButton toggleSelectAllChromosomesButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Validate the data contained in this panel. The user is alerted if
     * there is something wrong with the data format
     * @return
     *          true iff the validation succeeds
     */
    public boolean validateData()
    {
        String message = null;
        if(this.selectedCross == null)
        {
            message = "Please select a cross or cancel scan";
        }
        else
        {
            int[] selectedPhenotypes = this.scanCommand.getPhenotypeIndices();
            if(selectedPhenotypes == null || selectedPhenotypes.length == 0)
            {
                message =
                    "Please select at least one phenotype to scan " +
                    "before proceeding";
            }
        }
        
        if(message != null)
        {
            LOG.info("Validation failure: " + message);
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                            "Validation Failed",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else
        {
            PhenotypeDistribution phenotypeDistribution =
                this.scanCommand.getPhenotypeDistribution();
            if(phenotypeDistribution == PhenotypeDistribution.OTHER)
            {
                return this.validateGenotypeProbabilitiesCalculated(
                        phenotypeDistribution);
            }
            else
            {
                return true;
            }
        }
    }
    
    /**
     * Validate that genotype probabilities were simulated "sim.geno"
     * @param phenotypeDistribution
     *          the phenotype distribution we're requiring this for
     * @return
     *          true iff valid
     */
    private boolean validateGenotypeProbabilitiesCalculated(
            PhenotypeDistribution phenotypeDistribution)
    {
        final Cross cross = this.scanCommand.getCross();
        if(cross.getCalculateConditionalProbabilitiesWasUsed())
        {
            return true;
        }
        else
        {
            final String message =
                "The phenotype distribution that you have selected \"" +
                phenotypeDistribution.toString() + "\" requires that " +
                "genotype probabilities be calculated before " +
                "running the scan command. Would you like to do this now?";
            boolean confirmed = this.getConfirmation(
                    "Genotype Probabilities Not Yet Calculated",
                    message);
            if(confirmed)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        CrossAndPhenotypesScanPanel.this.calculateGenotypeProbabilities(
                                cross);
                    }
                });
            }
            
            return false;
        }
    }

    /**
     * Get confirmation from the user
     * @param title
     *          the message title
     * @param message
     *          the message
     * @return
     *          true iff the user says OK
     */
    private boolean getConfirmation(String title, String message)
    {
        int response = JOptionPane.showConfirmDialog(
                QTL.getInstance().getApplicationFrame(),
                TextWrapper.wrapText(
                        message,
                        TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.OK_OPTION;
    }

    /**
     * Run the calc genoprob dialog
     * @param cross
     *          the cross that we're calculating probabilities for
     */
    private void calculateGenotypeProbabilities(Cross cross)
    {
        CalcGenoprobDialog calculateGenotypeProbabilitiesDialog =
            new CalcGenoprobDialog(
                    this.parentDialog,
                    new Cross[] {cross},
                    cross);
        calculateGenotypeProbabilitiesDialog.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScanCommandBuilder getScanCommand()
    {
        return this.scanCommand;
    }

}
