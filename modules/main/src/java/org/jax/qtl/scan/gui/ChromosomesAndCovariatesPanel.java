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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.SingleMarkerQtlBasketItem;
import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.util.TextWrapper;
import org.jax.util.gui.CanDisableContentsTableCellRenderer;
import org.jax.util.gui.CheckableListTableModel;

/**
 * A small panel for editing covariates.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ChromosomesAndCovariatesPanel extends ScanCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2159225430604268002L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ChromosomesAndCovariatesPanel.class.getName());

    private final ScanCommandBuilder scanCommand;
    
    private volatile Cross cross;

    private CheckableListTableModel covariatesTableModel;
    
    private static final int CHROMOSOME_NAME_COLUMN = 1;
    
    private static final int COVARIATE_NAME_COLUMN = 2;
    
    private static final int ADDITIVE_COLUMN = 0;
    
    private static final int INTERACTIVE_COLUMN = 1;
    
    private final TableModelListener covariatesTableListener = new TableModelListener()
    {
        public void tableChanged(TableModelEvent e)
        {
            ChromosomesAndCovariatesPanel.this.covariatesTableUpdated(e);
        }
    };
    
    private volatile CheckableListTableModel chromosomeTableModel;

    /**
     * Constructor
     * @param scanCommand
     *          the scan command whose covariate parameters this dialog edits
     */
    public ChromosomesAndCovariatesPanel(
            ScanCommandBuilder scanCommand)
    {
        this.scanCommand = scanCommand;
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * take care of the initialization that the GUI builder doesn't do
     */
    private void postGuiInit()
    {
        this.chromosomeTableModel =
            ChromosomesAndCovariatesPanel.initializeChromoTable(this.chromosomesTable);
        this.chromosomeTableModel.addTableModelListener(
                new TableModelListener()
                {
                    public void tableChanged(TableModelEvent e)
                    {
                        if(e.getType() == TableModelEvent.UPDATE)
                        {
                            ChromosomesAndCovariatesPanel.this.chromosomesToScanWasUpdated();
                        }
                    }
                });
        
        this.initializeCovariatesTable();
    }
    
    /**
     * Validate the data contained in this panel. The user is alerted if
     * there is something wrong with the data format
     * @return
     *          true iff the validation succeeds
     */
    public boolean validateData()
    {
        String message = null;
        String[] chromosomeNames = this.scanCommand.getChromosomeNames();
        if(chromosomeNames == null || chromosomeNames.length == 0)
        {
            message =
                "Please select at least one chromosome to " +
                "scan, or cancel the scan operation";
        }
        
        if(message != null)
        {
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
            return true;
        }
    }

    /**
     * Method for responding to a covariates update event
     * @param event
     *          the event
     */
    private void covariatesTableUpdated(TableModelEvent event)
    {
        // enforce additive selection when interactive selection happens
        if(event.getType() == TableModelEvent.UPDATE)
        {
            if(event.getFirstRow() == event.getLastRow() &&
               event.getFirstRow() != TableModelEvent.HEADER_ROW)
            {
                if(event.getColumn() == INTERACTIVE_COLUMN)
                {
                    Boolean interactiveValue =
                        (Boolean)this.covariatesTableModel.getValueAt(
                            event.getFirstRow(),
                            event.getColumn());
                    if(interactiveValue.booleanValue())
                    {
                        this.covariatesTableModel.setValueAt(
                                Boolean.TRUE,
                                event.getFirstRow(),
                                ADDITIVE_COLUMN);
                    }
                }
                else if(event.getColumn() == ADDITIVE_COLUMN)
                {
                    Boolean additiveValue =
                        (Boolean)this.covariatesTableModel.getValueAt(
                            event.getFirstRow(),
                            event.getColumn());
                    if(!additiveValue.booleanValue())
                    {
                        this.covariatesTableModel.setValueAt(
                                Boolean.FALSE,
                                event.getFirstRow(),
                                INTERACTIVE_COLUMN);
                    }
                }
            }
        }
        
        int rowCount = this.covariatesTableModel.getRowCount();
        List<String> additivePhenoCovariatesList = new ArrayList<String>();
        List<String> interactivePhenoCovariatesList = new ArrayList<String>();
        List<GeneticMarker> additiveGenoCovariatesList = new ArrayList<GeneticMarker>();
        List<GeneticMarker> interactiveGenoCovariatesList = new ArrayList<GeneticMarker>();
        
        for(int currRow = 0; currRow < rowCount; currRow++)
        {
            Boolean interactive =
                (Boolean)this.covariatesTableModel.getValueAt(currRow, INTERACTIVE_COLUMN);
            Boolean additive =
                (Boolean)this.covariatesTableModel.getValueAt(currRow, ADDITIVE_COLUMN);
            Object covariate =
                this.covariatesTableModel.getValueAt(currRow, COVARIATE_NAME_COLUMN);
            if(covariate instanceof String)
            {
                String phenotypeName = (String)covariate;
                if(interactive.booleanValue())
                {
                    interactivePhenoCovariatesList.add(phenotypeName);
                    additivePhenoCovariatesList.add(phenotypeName);
                }
                else if(additive.booleanValue())
                {
                    additivePhenoCovariatesList.add(phenotypeName);
                }
            }
            else
            {
                GeneticMarker markerCovariate = (GeneticMarker)covariate;
                if(interactive.booleanValue())
                {
                    interactiveGenoCovariatesList.add(markerCovariate);
                    additiveGenoCovariatesList.add(markerCovariate);
                }
                else if(additive.booleanValue())
                {
                    additiveGenoCovariatesList.add(markerCovariate);
                }
            }
        }
        
        String[] additivePhenoCovariates =
            additivePhenoCovariatesList.toArray(new String[additivePhenoCovariatesList.size()]);
        this.scanCommand.setAdditivePhenotypeCovariates(additivePhenoCovariates);
        
        String[] interactivePhenoCovariates =
            interactivePhenoCovariatesList.toArray(new String[interactivePhenoCovariatesList.size()]);
        this.scanCommand.setInteractivePhenotypeCovariates(interactivePhenoCovariates);
        
        this.scanCommand.setAdditiveGenotypeCovariates(additiveGenoCovariatesList);
        this.scanCommand.setInteractiveGenotypeCovariates(interactiveGenoCovariatesList);
        
        this.fireCommandModified();
    }
    
    private void initializeCovariatesTable()
    {
        this.covariatesTableModel =
            new CheckableListTableModel(2);
        this.covariatesTableModel.addTableModelListener(
                this.covariatesTableListener);
        this.covariatesTableModel.setColumnIdentifiers(
                new String[] {"Additive", "Interactive", "Covariate"});
        this.selectCovariatesTabel.setModel(this.covariatesTableModel);
        TableColumnModel chromoOrPhenoColumnModel =
            this.selectCovariatesTabel.getColumnModel();
        TableColumn addColumn = chromoOrPhenoColumnModel.getColumn(ADDITIVE_COLUMN);
        addColumn.setCellRenderer(new CanDisableContentsTableCellRenderer(
                addColumn.getCellRenderer()));
        TableColumn interactColumn = chromoOrPhenoColumnModel.getColumn(INTERACTIVE_COLUMN);
        interactColumn.setCellRenderer(new CanDisableContentsTableCellRenderer(
                interactColumn.getCellRenderer()));
        TableColumn nameColumn = chromoOrPhenoColumnModel.getColumn(COVARIATE_NAME_COLUMN);
        nameColumn.setCellRenderer(new CanDisableContentsTableCellRenderer(
                nameColumn.getCellRenderer()));
    }

    /**
     * Build the table that holds the chromosomes we can scan
     * @param crossToBuildFrom
     *          the cross for us to scan
     */
    private void buildChromosomeTable(Cross crossToBuildFrom)
    {
        // update the chromosome stuff
        List<CrossChromosome> chromosomes = crossToBuildFrom.getGenotypeData();
        if(chromosomes == null)
        {
            LOG.severe("unexpected condition: chromosomes are null");
            return;
        }
        else if(chromosomes.size() == 0)
        {
            LOG.severe("unexpected condition: chromosomes are empty");
            return;
        }
        
        CheckableListTableModel chromoTableModel =
            (CheckableListTableModel)this.chromosomesTable.getModel();
        chromoTableModel.setRowCount(0);
        for(CrossChromosome currChromosome: chromosomes)
        {
            chromoTableModel.addRow(new Object[] {
                    Boolean.FALSE,
                    currChromosome.getChromosomeName()});
        }
        
        this.chromosomesToScanWasUpdated();
    }

    /**
     * Respond to a change in chromosome-to-scan selection
     */
    private void chromosomesToScanWasUpdated()
    {
        // throw in selected chromosome names
        int chromosomeRows = this.chromosomesTable.getRowCount();
        List<String> chromosomeNameList = new ArrayList<String>(
                chromosomeRows);
        for(int currRow = 0; currRow < chromosomeRows; currRow++)
        {
            Boolean selected =
                (Boolean)this.chromosomesTable.getValueAt(currRow, 0);
            if(selected.booleanValue())
            {
                String chromosomeName = (String)this.chromosomeTableModel.getValueAt(
                        currRow,
                        CHROMOSOME_NAME_COLUMN);
                chromosomeNameList.add(chromosomeName);
            }
        }
        
        this.scanCommand.setChromosomeIndices(
                chromosomeNameList.toArray(new String[chromosomeNameList.size()]));
        this.fireCommandModified();
    }

    /**
     * A little utility method that is used to initialize the chromosome
     * table model
     * @param chromoTable
     *          the table that we're initializing
     * @return
     *          the table model
     */
    private static CheckableListTableModel initializeChromoTable(JTable chromoTable)
    {
        CheckableListTableModel chromoTableModel =
            new CheckableListTableModel(1);
        chromoTable.setModel(chromoTableModel);
        chromoTable.getTableHeader().setVisible(false);
        chromoTable.getTableHeader().setPreferredSize(
                new Dimension(0, 0));
        TableColumnModel chromoColumnModel =
            chromoTable.getColumnModel();
        TableColumn checkColumn = chromoColumnModel.getColumn(0);
        checkColumn.setPreferredWidth(0);
        checkColumn.setCellRenderer(new CanDisableContentsTableCellRenderer(
                checkColumn.getCellRenderer()));
        TableColumn labelColumn = chromoColumnModel.getColumn(1);
        labelColumn.setCellRenderer(new CanDisableContentsTableCellRenderer(
                labelColumn.getCellRenderer()));
        labelColumn.setPreferredWidth(1000);
        
        return chromoTableModel;
    }
    
    /**
     * Refresh the GUI by adding or removing covariates from the table
     * based on which ones are valid or not.
     */
    public void refreshGui()
    {
        if(this.cross != this.scanCommand.getCross())
        {
            // start over
            this.cross = this.scanCommand.getCross();
            
            // update chromosome table
            this.buildChromosomeTable(this.cross);
            this.chromosomeTableModel.setAllCheckBoxValuesTo(true);
            
            // now update covariates
            this.covariatesTableModel.setRowCount(0);
            this.qtlBasketComboBox.removeAllItems();
            if(this.cross != null)
            {
                QtlBasket[] baskets = this.cross.getQtlBaskets();
                if(baskets.length == 0)
                {
                    this.qtlBasketComboBox.addItem(
                            "No QTL Baskets for " + this.cross);
                }
                else
                {
                    for(QtlBasket qtlBasket: baskets)
                    {
                        this.qtlBasketComboBox.addItem(qtlBasket);
                    }
                }
            }
        }
        
        if(this.cross != null)
        {
            // update the table
            String[] allPhenotypeNames = this.cross.getPhenotypeData().getDataNames();
            int[] phenoIndicesWeAreAlreadyScanning =
                this.scanCommand.getPhenotypeIndices();
            List<String> phenotypeNamesToAdd = new ArrayList<String>(
                    Arrays.asList(allPhenotypeNames));
            
            // cull out the phenotype names that are in the scan phenos already
            // we have to count in reverse here or things will get messed up
            for(int i = phenoIndicesWeAreAlreadyScanning.length - 1;
                i >= 0;
                i--)
            {
                phenotypeNamesToAdd.remove(phenoIndicesWeAreAlreadyScanning[i]);
            }
            
            // subtract the rows that don't belong (again, go in reverse)
            int rowCount = this.covariatesTableModel.getRowCount();
            for(int currRow = rowCount - 1;
                currRow >= 0;
                currRow--)
            {
                Object tableCovariate = this.covariatesTableModel.getValueAt(
                        currRow,
                        COVARIATE_NAME_COLUMN);
                if(tableCovariate instanceof String)
                {
                    String tableCovariateString = (String)this.covariatesTableModel.getValueAt(
                            currRow,
                            COVARIATE_NAME_COLUMN);
                    boolean removeSuccessfull = phenotypeNamesToAdd.remove(
                            tableCovariateString);
                    if(!removeSuccessfull)
                    {
                        this.covariatesTableModel.removeRow(currRow);
                    }
                }
            }
            
            // add the rows that were not there
            for(String currPhenoName: phenotypeNamesToAdd)
            {
                this.covariatesTableModel.addRow(new Object[] {
                        Boolean.FALSE,
                        Boolean.FALSE,
                        currPhenoName});
            }
        }
    }
    
    /**
     * Respond to a change in the selected qtl basket
     */
    private void selectedQtlBasketChanged()
    {
        // clear out all the existing markers
        int numTableRows = this.covariatesTableModel.getRowCount();
        for(int rowIndex = numTableRows - 1; rowIndex >= 0; rowIndex--)
        {
            Object rowValue = this.covariatesTableModel.getValueAt(
                    rowIndex,
                    COVARIATE_NAME_COLUMN);
            if(rowValue instanceof GeneticMarker)
            {
                this.covariatesTableModel.removeRow(rowIndex);
            }
        }
        
        QtlBasket selectedQtlBasket = this.getSelectedQtlBasket();
        if(selectedQtlBasket != null && this.cross != null)
        {
            // create a set of all non-imputed markers
            Set<String> nonImputedMarkerNames = new HashSet<String>();
            List<CrossChromosome> allChromosomes = this.cross.getGenotypeData();
            for(CrossChromosome crossChromosome: allChromosomes)
            {
                for(GeneticMarker currMarker: crossChromosome.getAnyGeneticMap().getMarkerPositions())
                {
                    nonImputedMarkerNames.add(currMarker.getMarkerName());
                }
            }
            
            for(QtlBasketItem currItem: selectedQtlBasket.getContents())
            {
                if(currItem instanceof SingleMarkerQtlBasketItem)
                {
                    SingleMarkerQtlBasketItem currSingleMarkerItem =
                        (SingleMarkerQtlBasketItem)currItem;
                    GeneticMarker currMarker =
                        currSingleMarkerItem.getMarker();
                    if(nonImputedMarkerNames.contains(currMarker.getMarkerName()))
                    {
                        // since it's a non-imputed single marker it's OK
                        // to act as a covariate
                        LOG.fine("adding marker covariate: " + currMarker);
                        this.covariatesTableModel.addRow(new Object[] {
                            Boolean.FALSE,
                            Boolean.FALSE,
                            currMarker});
                    }
                    else
                    {
                        LOG.fine(
                                "skipping marker covariate because it looks " +
                                "like it's imputed: " + currMarker);
                    }
                }
                else
                {
                    LOG.fine(
                            "skipping QTL basket item as covariate because " +
                            "the type isn't right: " + currItem);
                }
            }
        }
    }
    
    private QtlBasket getSelectedQtlBasket()
    {
        Object selectedQtlItem = this.qtlBasketComboBox.getSelectedItem();
        if(selectedQtlItem instanceof QtlBasket)
        {
            return (QtlBasket)selectedQtlItem;
        }
        else
        {
            return null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        qtlBasketLabel = new javax.swing.JLabel();
        qtlBasketComboBox = new javax.swing.JComboBox();
        selectCovariatesLabel = new javax.swing.JLabel();
        selectCovariatesScrollPanel = new javax.swing.JScrollPane();
        selectCovariatesTabel = new javax.swing.JTable();
        chromosomesToScanLabel = new javax.swing.JLabel();
        toggleSelectAllChromosomesButton = new javax.swing.JButton();
        chromosomesScrollPanel = new javax.swing.JScrollPane();
        chromosomesTable = new javax.swing.JTable();

        qtlBasketLabel.setText("Include QTL's From:");

        qtlBasketComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                qtlBasketComboBoxItemStateChanged(evt);
            }
        });

        selectCovariatesLabel.setText("Select Additive and Interactive Covariates:");

        selectCovariatesScrollPanel.setMinimumSize(new java.awt.Dimension(23, 100));
        selectCovariatesScrollPanel.setViewportView(selectCovariatesTabel);

        chromosomesToScanLabel.setText("Chromosomes to Scan:");

        toggleSelectAllChromosomesButton.setText("Toggle Select All");
        toggleSelectAllChromosomesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSelectAllChromosomesButtonActionPerformed(evt);
            }
        });

        chromosomesScrollPanel.setMinimumSize(new java.awt.Dimension(50, 23));
        chromosomesScrollPanel.setViewportView(chromosomesTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(selectCovariatesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(qtlBasketLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(qtlBasketComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(selectCovariatesLabel))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chromosomesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(chromosomesToScanLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(toggleSelectAllChromosomesButton)))
                        .add(23, 23, 23))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(25, 25, 25)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chromosomesToScanLabel)
                    .add(toggleSelectAllChromosomesButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chromosomesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectCovariatesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(qtlBasketLabel)
                    .add(qtlBasketComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectCovariatesScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void qtlBasketComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_qtlBasketComboBoxItemStateChanged
        this.selectedQtlBasketChanged();
    }//GEN-LAST:event_qtlBasketComboBoxItemStateChanged

    private void toggleSelectAllChromosomesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSelectAllChromosomesButtonActionPerformed
        this.chromosomeTableModel.toggleSelectAllCheckBoxes();
    }//GEN-LAST:event_toggleSelectAllChromosomesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane chromosomesScrollPanel;
    private javax.swing.JTable chromosomesTable;
    private javax.swing.JLabel chromosomesToScanLabel;
    private javax.swing.JComboBox qtlBasketComboBox;
    private javax.swing.JLabel qtlBasketLabel;
    private javax.swing.JLabel selectCovariatesLabel;
    private javax.swing.JScrollPane selectCovariatesScrollPanel;
    private javax.swing.JTable selectCovariatesTabel;
    private javax.swing.JButton toggleSelectAllChromosomesButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Determines how many covariates are available for the user to select
     * @return
     *          the count
     */
    public int getCandidateCovariateCount()
    {
        return this.covariatesTableModel.getRowCount();
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
