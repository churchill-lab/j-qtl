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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.MarkerPairQtlBasketItem;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.SingleMarkerQtlBasketItem;
import org.jax.qtl.fit.FitPredictor;
import org.jax.qtl.fit.FitQtlCommand;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.ui.ImputationDialog;
import org.jax.r.RCommand;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.TextWrapper;

/**
 * Use this panel to edit fitqtl R commands
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitQtlPanel extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2891530987811064009L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            FitQtlPanel.class.getName());
    
    private volatile FitQtlCommand fitQtlCommand;
    
    private final JDialog parentDialog;
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent dialog
     */
    public FitQtlPanel(JDialog parentDialog)
    {
        this(parentDialog, null);
    }
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent dialog
     * @param selectedQtlBasket
     *          the QTL basket selection that this panel should start out
     *          with
     */
    public FitQtlPanel(
            JDialog parentDialog,
            QtlBasket selectedQtlBasket)
    {
        this.parentDialog = parentDialog;
        this.initComponents();
        
        this.postGuiInit(selectedQtlBasket);
    }
    
    /**
     * Do the GUI initialization that wasn't handled by the GUI builder
     * @param selectedQtlBasket
     *          the initial QTL basket
     */
    private void postGuiInit(QtlBasket selectedQtlBasket)
    {
        this.fitResultNameTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        FitQtlPanel.this.updateRCommand();
                    }

                    public void insertUpdate(DocumentEvent e)
                    {
                        FitQtlPanel.this.updateRCommand();
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        FitQtlPanel.this.updateRCommand();
                    }
                });
        
        DefaultTableModel modelInputsModel = new DefaultTableModel()
        {
            private static final long serialVersionUID = 5313156157630746144L;

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        modelInputsModel.setColumnIdentifiers(
                new String[] {"Input Type", "Input Identifier"});
        this.modelInputsTabel.setModel(modelInputsModel);
        this.modelInputsTabel.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.modelInputsTabel.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        FitQtlPanel.this.modelSelectionsChanged();
                    }
                });
        
        this.modelTermsList.setModel(new DefaultListModel());
        this.modelTermsList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        
        QtlProject activeProject =
            QtlProjectManager.getInstance().getActiveProject();
        
        // add crosses
        Cross[] crosses = activeProject.getDataModel().getCrosses();
        for(Cross cross: crosses)
        {
            this.crossComboBox.addItem(cross);
        }
        
        if(selectedQtlBasket != null)
        {
            this.setSelectedCross(selectedQtlBasket.getParentCross());
            this.setSelectedQtlBasket(selectedQtlBasket);
        }
        
        this.updateRCommand();
        this.modelSelectionsChanged();
    }
    
    /**
     * Getter for the current fit qtl command. Callers should make sure this
     * is valid 1st by calling {@link #validateData()}
     * @return
     *          the command
     */
    public FitQtlCommand getFitQtlCommand()
    {
        return this.fitQtlCommand;
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        FitQtlCommand fitQtlCommand = this.fitQtlCommand;
        if(fitQtlCommand == null)
        {
            return new RCommand[0];
        }
        else
        {
            return new RCommand[] {fitQtlCommand};
        }
    }
    
    /**
     * Convenience function for getting a handle on the table model
     * @return
     *          the table model
     */
    private DefaultTableModel getModelInputsTableModel()
    {
        return (DefaultTableModel)this.modelInputsTabel.getModel();
    }
    
    /**
     * Convenience function for getting a handle on the list model
     * @return
     *          the list model
     */
    private DefaultListModel getModelPredictorsListModel()
    {
        return (DefaultListModel)this.modelTermsList.getModel();
    }
    
    /**
     * Getter for the selected cross
     * @return
     *          the selected cross
     */
    private Cross getSelectedCross()
    {
        return (Cross)this.crossComboBox.getSelectedItem();
    }

    /**
     * Setter for the selected cross
     * @param selectedCross
     *          the selected cross
     */
    private void setSelectedCross(Cross selectedCross)
    {
        if(selectedCross != null && selectedCross != this.getSelectedCross())
        {
            this.crossComboBox.setSelectedItem(selectedCross);
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("ignoring null cross selection");
            }
        }
    }
    
    /**
     * Respond to a change in selected cross
     */
    private void selectedCrossChanged()
    {
        // clean up any old stuff that needs to go
        this.phenotypeComboBox.removeAllItems();
        this.qtlBasketComboBox.removeAllItems();
        
        // add in the new stuff
        Cross selectedCross = this.getSelectedCross();
        if(selectedCross != null)
        {
            String[] phenotypes =
                selectedCross.getPhenotypeData().getDataNames();
            for(String pheno: phenotypes)
            {
                this.phenotypeComboBox.addItem(pheno);
            }
            
            QtlBasket[] qtlBaskets = selectedCross.getQtlBaskets();
            for(QtlBasket currQtlBasket: qtlBaskets)
            {
                this.qtlBasketComboBox.addItem(currQtlBasket);
            }
        }
        
        this.updateRCommand();
    }

    /**
     * Respond to a change in QTL basket
     */
    private void selectedQtlBasketChanged()
    {
        // clean up any old stuff that needs to go
        this.getModelPredictorsListModel().removeAllElements();
        DefaultTableModel modelInputsTableModel =
            this.getModelInputsTableModel();
        modelInputsTableModel.setNumRows(0);
        
        Cross selectedCross = this.getSelectedCross();
        QtlBasket selectedQtlBasket = this.getSelectedQtlBasket();
        
        // add in the new stuff
        if(selectedCross != null && selectedQtlBasket != null)
        {
            String[] phenotypeNames =
                selectedCross.getPhenotypeData().getDataNames();
            List<QtlBasketItem> basketContents =
                selectedQtlBasket.getContents();
            
            for(QtlBasketItem qtlBasketItem: basketContents)
            {
                modelInputsTableModel.addRow(new QtlBasketItemCell[] {
                        new QtlBasketItemCell(qtlBasketItem, 0),
                        new QtlBasketItemCell(qtlBasketItem, 1)});
            }
            
            for(String currPhenotypeName: phenotypeNames)
            {
                modelInputsTableModel.addRow(new PhenotypeInputCell[] {
                        new PhenotypeInputCell(currPhenotypeName, 0),
                        new PhenotypeInputCell(currPhenotypeName, 1)});
            }
        }
        
        this.updateRCommand();
    }
    
    /**
     * Get the currently selected QTL basket
     * @return
     *          the selected basket
     */
    private QtlBasket getSelectedQtlBasket()
    {
        return (QtlBasket)this.qtlBasketComboBox.getSelectedItem();
    }

    /**
     * Set the selected QTL basket
     * @param selectedQtlBasket
     *          the selected QTL basket
     */
    private void setSelectedQtlBasket(QtlBasket selectedQtlBasket)
    {
        if(selectedQtlBasket != null)
        {
            this.qtlBasketComboBox.setSelectedItem(selectedQtlBasket);
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("ignoring null QTL basket selection");
            }
        }
    }
    
    /**
     * Respond to a change in model selection (either inputs tabel or terms)
     */
    private void modelSelectionsChanged()
    {
        this.addTermsButton.setEnabled(
                this.modelInputsTabel.getSelectedRowCount() > 0);
        this.addInteractiveTermButton.setEnabled(
                this.modelInputsTabel.getSelectedRowCount() > 1);
        this.appendInteractionsToTermButton.setEnabled(
                this.modelInputsTabel.getSelectedRowCount() > 0 &&
                this.modelTermsList.getSelectedIndex() != -1);
        this.removeTermButton.setEnabled(
                this.modelTermsList.getSelectedIndex() != -1);
    }
    
    /**
     * This function is called when we should update the {@link FitQtlCommand}
     * in response to a change in the GUI
     */
    private void updateRCommand()
    {
        FitQtlCommand newFitCommand = new FitQtlCommand(
                this.getSelectedCross(),
                this.getFitPredictors());
        newFitCommand.setPhenotypeToFit(
                this.getSelectedPhenotypeName());
        newFitCommand.setEstimateQtlEffects(
                this.estimateEffectsCheckbox.isSelected());
        newFitCommand.setPerformDropOneAnalysis(
                this.dropOneAnalysisCheckbox.isSelected());
        newFitCommand.setFitResultName(
                this.getFitResultName());
        
        this.fitQtlCommand = newFitCommand;
        
        this.fireCommandModified();
    }
    
    /**
     * Get the fit result name that's currently entered in the GUI
     * @return
     *          the result name that's entered
     */
    private String getFitResultName()
    {
        return this.fitResultNameTextField.getText().trim();
    }

    /**
     * Getter for the phenotype that is selected
     * @return
     *          the phenotype
     */
    private String getSelectedPhenotypeName()
    {
        return (String)this.phenotypeComboBox.getSelectedItem();
    }

    /**
     * Getter for the fit predictors that are currently entered in the GUI
     * @return
     *          the predictors
     */
    private List<FitPredictor> getFitPredictors()
    {
        ListModel modelTermsModel = this.modelTermsList.getModel();
        int numTerms = modelTermsModel.getSize();
        List<FitPredictor> newPredictors = new ArrayList<FitPredictor>(
                numTerms);
        for(int i = 0; i < numTerms; i++)
        {
            newPredictors.add((FitPredictor)modelTermsModel.getElementAt(i));
        }
        
        return newPredictors;
    }

    /**
     * A cell class for holding phenotype info
     */
    private class PhenotypeInputCell
    {
        private static final String INPUT_TYPE_NAME = "phenotype";
        
        private final String phenotypeName;
        
        private final int columnNumber;

        /**
         * Constructor
         * @param phenotypeName
         *          the phenotype name for this cell
         * @param columnNumber
         *          the column number of this cell
         */
        public PhenotypeInputCell(String phenotypeName, int columnNumber)
        {
            this.phenotypeName = phenotypeName;
            this.columnNumber = columnNumber;
        }
        
        /**
         * @return the phenotypeName
         */
        public String getPhenotypeName()
        {
            return this.phenotypeName;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if(this.columnNumber == 0)
            {
                return INPUT_TYPE_NAME;
            }
            else if(this.columnNumber == 1)
            {
                return this.phenotypeName;
            }
            else
            {
                throw new IllegalStateException(
                        "phenotype column number is not valid: " +
                        this.columnNumber);
            }
        }
    }
    
    private class QtlBasketItemCell
    {
        private final int columnNumber;

        private final QtlBasketItem basketItem;

        /**
         * Constructor
         * @param basketItem
         *          the item we're holding
         * @param columnNumber
         *          the column number for this cell
         */
        public QtlBasketItemCell(QtlBasketItem basketItem, int columnNumber)
        {
            this.basketItem = basketItem;
            this.columnNumber = columnNumber;
        }
        
        /**
         * @return the basketItem
         */
        public QtlBasketItem getBasketItem()
        {
            return this.basketItem;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if(this.columnNumber == 0)
            {
                if(this.basketItem instanceof SingleMarkerQtlBasketItem)
                {
                    return "marker";
                }
                else if(this.basketItem instanceof MarkerPairQtlBasketItem)
                {
                    return "marker interaction";
                }
                else
                {
                    throw new IllegalStateException(
                            "unknown type: " +
                            this.basketItem.getClass().getName());
                }
            }
            else if(this.columnNumber == 1)
            {
                if(this.basketItem instanceof SingleMarkerQtlBasketItem)
                {
                    SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                        (SingleMarkerQtlBasketItem)this.basketItem;
                    return singleMarkerQtlBasketItem.getMarker().toString();
                }
                else if(this.basketItem instanceof MarkerPairQtlBasketItem)
                {
                    MarkerPairQtlBasketItem markerPairQtlBasketItem =
                        (MarkerPairQtlBasketItem)this.basketItem;
                    return markerPairQtlBasketItem.getMarkerPair().toString();
                }
                else
                {
                    throw new IllegalStateException(
                            "unknown type: " +
                            this.basketItem.getClass().getName());
                }
            }
            else
            {
                throw new IllegalStateException(
                        "column number is not valid: " +
                        this.columnNumber);
            }
        }
    }
    
    /**
     * Validate all of the data that the user entered into the GUI
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        String message = null;
        String fitResultName = this.getFitResultName();
        if(fitResultName.length() == 0)
        {
            message =
                "Please enter a name for your fit QTL results or cancel";
        }
        else if(JRIUtilityFunctions.isTopLevelObject(
                this.fitQtlCommand.getFitResultAccessor(),
                RInterfaceFactory.getRInterfaceInstance()))
        {
            message =
                "The name \"" + fitResultName + "\" conflicts with " +
                "an existing data object. Please choose another name.";
        }
        else
        {
            message = RUtilities.getErrorMessageForReadableName(fitResultName);
            if(message == null)
            {
                List<FitPredictor> fitPredictors = this.getFitPredictors();
                boolean atLeastOneMarkerInPredictor = false;
                for(FitPredictor fitPredictor: fitPredictors)
                {
                    if(!fitPredictor.getInteractingMarkers().isEmpty())
                    {
                        atLeastOneMarkerInPredictor = true;
                        break;
                    }
                }
                
                if(!atLeastOneMarkerInPredictor)
                {
                    message =
                        "Please include at least one genetic marker as a " +
                        "fit term or cancel";
                }
            }
        }
        
        if(message == null)
        {
            return this.validateGenotypeProbabilitiesSimulated();
        }
        else
        {
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Data Validation Failed",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    /**
     * Validate that genotype probabilities were calculated "calc.genoprob"
     * @return
     *          true iff valid
     */
    private boolean validateGenotypeProbabilitiesSimulated()
    {
        final Cross cross = this.getSelectedCross(); 
        if(cross.getSimulateGenotypeWasUsed())
        {
            return true;
        }
        else
        {
            final String message =
                "The fitqtl(...) R method requires that " +
                "genotype probabilities be simulated. " +
                "Would you like to do this now?";
            int response = JOptionPane.showConfirmDialog(
                    FitQtlPanel.this.parentDialog,
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Genotype Probabilities Not Yet Simulated",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            boolean confirmed = response == JOptionPane.OK_OPTION;
            if(confirmed)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        ImputationDialog simulateGenotypeProbabilitiesDialog =
                            new ImputationDialog(
                                    FitQtlPanel.this.parentDialog,
                                    new Cross[] {cross},
                                    cross);
                        simulateGenotypeProbabilitiesDialog.setVisible(true);
                    }
                });
            }
            
            return false;
        }
    }
    
    /**
     * Create a single interactive predictor out of the selected
     * inputs
     */
    private void addSelectedInputsAsInteractivePredictor()
    {
        List<String> interactingPhenotypes = new ArrayList<String>();
        List<GeneticMarker> interactingMarkers = new ArrayList<GeneticMarker>();
        
        int[] selectedInputRows = this.modelInputsTabel.getSelectedRows();
        for(int currSelectedRowIndex: selectedInputRows)
        {
            Object selectedValue =
                this.modelInputsTabel.getValueAt(currSelectedRowIndex, 0);
            
            if(selectedValue instanceof PhenotypeInputCell)
            {
                PhenotypeInputCell selectedPhenoCell =
                    (PhenotypeInputCell)selectedValue;
                interactingPhenotypes.add(selectedPhenoCell.getPhenotypeName());
            }
            else
            {
                QtlBasketItemCell selectedQtlBasketItemCell =
                    (QtlBasketItemCell)selectedValue;
                QtlBasketItem item =
                    selectedQtlBasketItemCell.getBasketItem();
                if(item instanceof SingleMarkerQtlBasketItem)
                {
                    SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                        (SingleMarkerQtlBasketItem)item;
                    interactingMarkers.add(singleMarkerQtlBasketItem.getMarker());
                }
                else
                {
                    MarkerPairQtlBasketItem markerPairQtlBasketItem =
                        (MarkerPairQtlBasketItem)item;
                    interactingMarkers.add(
                            markerPairQtlBasketItem.getMarkerPair().getMarkerOne());
                    interactingMarkers.add(
                            markerPairQtlBasketItem.getMarkerPair().getMarkerTwo());
                }
            }
        }
        
        FitPredictor newTerm = new FitPredictor(
                interactingPhenotypes,
                interactingMarkers);
        DefaultListModel modelTermsModel =
            (DefaultListModel)this.modelTermsList.getModel();
        modelTermsModel.addElement(newTerm);
        
        this.updateRCommand();
    }
    
    /**
     * Create separate one-term predictors out of each selected input
     */
    private void addSelectedInputsAsPredictors()
    {
        DefaultListModel modelTermsModel =
            (DefaultListModel)this.modelTermsList.getModel();
        
        int[] selectedInputRows = this.modelInputsTabel.getSelectedRows();
        for(int currSelectedRowIndex: selectedInputRows)
        {
            List<String> interactingPhenotypes = new ArrayList<String>();
            List<GeneticMarker> interactingMarkers = new ArrayList<GeneticMarker>();
            
            Object selectedValue =
                this.modelInputsTabel.getValueAt(currSelectedRowIndex, 0);
            
            if(selectedValue instanceof PhenotypeInputCell)
            {
                PhenotypeInputCell selectedPhenoCell =
                    (PhenotypeInputCell)selectedValue;
                interactingPhenotypes.add(selectedPhenoCell.getPhenotypeName());
            }
            else
            {
                QtlBasketItemCell selectedQtlBasketItemCell =
                    (QtlBasketItemCell)selectedValue;
                QtlBasketItem item =
                    selectedQtlBasketItemCell.getBasketItem();
                if(item instanceof SingleMarkerQtlBasketItem)
                {
                    SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                        (SingleMarkerQtlBasketItem)item;
                    interactingMarkers.add(singleMarkerQtlBasketItem.getMarker());
                }
                else
                {
                    MarkerPairQtlBasketItem markerPairQtlBasketItem =
                        (MarkerPairQtlBasketItem)item;
                    interactingMarkers.add(
                            markerPairQtlBasketItem.getMarkerPair().getMarkerOne());
                    interactingMarkers.add(
                            markerPairQtlBasketItem.getMarkerPair().getMarkerTwo());
                }
            }
            
            FitPredictor newTerm = new FitPredictor(
                    interactingPhenotypes,
                    interactingMarkers);
            modelTermsModel.addElement(newTerm);
        }
        
        this.updateRCommand();
    }

    /**
     * Append selected input terms as interactions to the selected predictor
     */
    private void appendSelectedInputsToSelectedPredictor()
    {
        FitPredictor selectedPredictor =
            (FitPredictor)this.modelTermsList.getSelectedValue();
        if(selectedPredictor != null)
        {
            List<String> interactingPhenotypes =
                selectedPredictor.getInteractingPhenotypes();
            List<GeneticMarker> interactingMarkers =
                selectedPredictor.getInteractingMarkers();
            
            int[] selectedInputRows = this.modelInputsTabel.getSelectedRows();
            for(int currSelectedRowIndex: selectedInputRows)
            {
                Object selectedValue =
                    this.modelInputsTabel.getValueAt(currSelectedRowIndex, 0);
                
                if(selectedValue instanceof PhenotypeInputCell)
                {
                    PhenotypeInputCell selectedPhenoCell =
                        (PhenotypeInputCell)selectedValue;
                    interactingPhenotypes.add(selectedPhenoCell.getPhenotypeName());
                }
                else
                {
                    QtlBasketItemCell selectedQtlBasketItemCell =
                        (QtlBasketItemCell)selectedValue;
                    QtlBasketItem item =
                        selectedQtlBasketItemCell.getBasketItem();
                    if(item instanceof SingleMarkerQtlBasketItem)
                    {
                        SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                            (SingleMarkerQtlBasketItem)item;
                        interactingMarkers.add(singleMarkerQtlBasketItem.getMarker());
                    }
                    else
                    {
                        MarkerPairQtlBasketItem markerPairQtlBasketItem =
                            (MarkerPairQtlBasketItem)item;
                        interactingMarkers.add(
                                markerPairQtlBasketItem.getMarkerPair().getMarkerOne());
                        interactingMarkers.add(
                                markerPairQtlBasketItem.getMarkerPair().getMarkerTwo());
                    }
                }
            }
            
            this.repaint();
            this.updateRCommand();
        }
    }

    /**
     * Remove any selected predictors
     */
    private void removeSelectedPredictors()
    {
        DefaultListModel modelPredictorsListModel =
            this.getModelPredictorsListModel();
        int[] selectedPredictorRows = this.modelTermsList.getSelectedIndices();
        for(int i = selectedPredictorRows.length - 1; i >= 0 ; i--)
        {
            modelPredictorsListModel.remove(selectedPredictorRows[i]);
        }
        
        this.updateRCommand();
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
        phenotypeComboBox = new javax.swing.JComboBox();
        qtlBasketLabel = new javax.swing.JLabel();
        qtlBasketComboBox = new javax.swing.JComboBox();
        addTermsButton = new javax.swing.JButton();
        addInteractiveTermButton = new javax.swing.JButton();
        appendInteractionsToTermButton = new javax.swing.JButton();
        removeTermButton = new javax.swing.JButton();
        modelSplitPane = new javax.swing.JSplitPane();
        modelInputsPanel = new javax.swing.JPanel();
        modelInputsLabel = new javax.swing.JLabel();
        modelInputsScrollPane = new javax.swing.JScrollPane();
        modelInputsTabel = new javax.swing.JTable();
        modelTermsPanel = new javax.swing.JPanel();
        modelTermsLabel = new javax.swing.JLabel();
        modelTermsScrollPanel = new javax.swing.JScrollPane();
        modelTermsList = new javax.swing.JList();
        dropOneAnalysisCheckbox = new javax.swing.JCheckBox();
        estimateEffectsCheckbox = new javax.swing.JCheckBox();
        fitResultNameLabel = new javax.swing.JLabel();
        fitResultNameTextField = new javax.swing.JTextField();

        crossLabel.setText("Cross:");

        crossComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                crossComboBoxItemStateChanged(evt);
            }
        });

        phenotypeLabel.setText("Phenotype to Fit:");

        phenotypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                phenotypeComboBoxItemStateChanged(evt);
            }
        });

        qtlBasketLabel.setText("QTL Basket:");

        qtlBasketComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                qtlBasketComboBoxItemStateChanged(evt);
            }
        });

        addTermsButton.setText("Add Term(s)");
        addTermsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTermsButtonActionPerformed(evt);
            }
        });

        addInteractiveTermButton.setText("Add as Interactive Term");
        addInteractiveTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInteractiveTermButtonActionPerformed(evt);
            }
        });

        appendInteractionsToTermButton.setText("Append Interaction(s) to Term");
        appendInteractionsToTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendInteractionsToTermButtonActionPerformed(evt);
            }
        });

        removeTermButton.setText("Remove Term");
        removeTermButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTermButtonActionPerformed(evt);
            }
        });

        modelSplitPane.setResizeWeight(0.5);
        modelSplitPane.setMinimumSize(new java.awt.Dimension(13, 100));
        modelSplitPane.setPreferredSize(new java.awt.Dimension(13, 100));

        modelInputsLabel.setText("Model Inputs:");

        modelInputsScrollPane.setViewportView(modelInputsTabel);

        org.jdesktop.layout.GroupLayout modelInputsPanelLayout = new org.jdesktop.layout.GroupLayout(modelInputsPanel);
        modelInputsPanel.setLayout(modelInputsPanelLayout);
        modelInputsPanelLayout.setHorizontalGroup(
            modelInputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelInputsPanelLayout.createSequentialGroup()
                .add(modelInputsLabel)
                .addContainerGap(346, Short.MAX_VALUE))
            .add(modelInputsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
        );
        modelInputsPanelLayout.setVerticalGroup(
            modelInputsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelInputsPanelLayout.createSequentialGroup()
                .add(modelInputsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelInputsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
        );

        modelSplitPane.setLeftComponent(modelInputsPanel);

        modelTermsLabel.setText("Model Terms:");

        modelTermsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                modelTermsListValueChanged(evt);
            }
        });
        modelTermsScrollPanel.setViewportView(modelTermsList);

        org.jdesktop.layout.GroupLayout modelTermsPanelLayout = new org.jdesktop.layout.GroupLayout(modelTermsPanel);
        modelTermsPanel.setLayout(modelTermsPanelLayout);
        modelTermsPanelLayout.setHorizontalGroup(
            modelTermsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelTermsPanelLayout.createSequentialGroup()
                .add(modelTermsLabel)
                .addContainerGap(127, Short.MAX_VALUE))
            .add(modelTermsScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
        );
        modelTermsPanelLayout.setVerticalGroup(
            modelTermsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(modelTermsPanelLayout.createSequentialGroup()
                .add(modelTermsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelTermsScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
        );

        modelSplitPane.setRightComponent(modelTermsPanel);

        dropOneAnalysisCheckbox.setText("Perform Drop-One-Term Analysis");
        dropOneAnalysisCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dropOneAnalysisCheckboxItemStateChanged(evt);
            }
        });

        estimateEffectsCheckbox.setText("Estimate QTL Effects");
        estimateEffectsCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                estimateEffectsCheckboxItemStateChanged(evt);
            }
        });

        fitResultNameLabel.setText("Name Your Fit Analysis:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(estimateEffectsCheckbox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(phenotypeLabel)
                            .add(qtlBasketLabel)
                            .add(crossLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(phenotypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(qtlBasketComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(modelSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(addTermsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addInteractiveTermButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendInteractionsToTermButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeTermButton))
                    .add(dropOneAnalysisCheckbox)
                    .add(layout.createSequentialGroup()
                        .add(fitResultNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fitResultNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 102, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossLabel)
                    .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phenotypeLabel)
                    .add(phenotypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(qtlBasketLabel)
                    .add(qtlBasketComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(modelSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addTermsButton)
                    .add(addInteractiveTermButton)
                    .add(appendInteractionsToTermButton)
                    .add(removeTermButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dropOneAnalysisCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(estimateEffectsCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fitResultNameLabel)
                    .add(fitResultNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addTermsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTermsButtonActionPerformed
        this.addSelectedInputsAsPredictors();
    }//GEN-LAST:event_addTermsButtonActionPerformed

    private void addInteractiveTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInteractiveTermButtonActionPerformed
        this.addSelectedInputsAsInteractivePredictor();
    }//GEN-LAST:event_addInteractiveTermButtonActionPerformed

    private void appendInteractionsToTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendInteractionsToTermButtonActionPerformed
        this.appendSelectedInputsToSelectedPredictor();
    }//GEN-LAST:event_appendInteractionsToTermButtonActionPerformed

    private void removeTermButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTermButtonActionPerformed
        this.removeSelectedPredictors();
    }//GEN-LAST:event_removeTermButtonActionPerformed

    private void dropOneAnalysisCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dropOneAnalysisCheckboxItemStateChanged
        this.updateRCommand();
    }//GEN-LAST:event_dropOneAnalysisCheckboxItemStateChanged

    private void estimateEffectsCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_estimateEffectsCheckboxItemStateChanged
        this.updateRCommand();
    }//GEN-LAST:event_estimateEffectsCheckboxItemStateChanged

    private void crossComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crossComboBoxItemStateChanged
        this.selectedCrossChanged();
    }//GEN-LAST:event_crossComboBoxItemStateChanged

    private void phenotypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_phenotypeComboBoxItemStateChanged
        this.updateRCommand();
    }//GEN-LAST:event_phenotypeComboBoxItemStateChanged

    private void qtlBasketComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_qtlBasketComboBoxItemStateChanged
        this.selectedQtlBasketChanged();
    }//GEN-LAST:event_qtlBasketComboBoxItemStateChanged

    private void modelTermsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_modelTermsListValueChanged
        this.modelSelectionsChanged();
    }//GEN-LAST:event_modelTermsListValueChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addInteractiveTermButton;
    private javax.swing.JButton addTermsButton;
    private javax.swing.JButton appendInteractionsToTermButton;
    private javax.swing.JComboBox crossComboBox;
    private javax.swing.JLabel crossLabel;
    private javax.swing.JCheckBox dropOneAnalysisCheckbox;
    private javax.swing.JCheckBox estimateEffectsCheckbox;
    private javax.swing.JLabel fitResultNameLabel;
    private javax.swing.JTextField fitResultNameTextField;
    private javax.swing.JLabel modelInputsLabel;
    private javax.swing.JPanel modelInputsPanel;
    private javax.swing.JScrollPane modelInputsScrollPane;
    private javax.swing.JTable modelInputsTabel;
    private javax.swing.JSplitPane modelSplitPane;
    private javax.swing.JLabel modelTermsLabel;
    private javax.swing.JList modelTermsList;
    private javax.swing.JPanel modelTermsPanel;
    private javax.swing.JScrollPane modelTermsScrollPanel;
    private javax.swing.JComboBox phenotypeComboBox;
    private javax.swing.JLabel phenotypeLabel;
    private javax.swing.JComboBox qtlBasketComboBox;
    private javax.swing.JLabel qtlBasketLabel;
    private javax.swing.JButton removeTermButton;
    // End of variables declaration//GEN-END:variables
    
}
