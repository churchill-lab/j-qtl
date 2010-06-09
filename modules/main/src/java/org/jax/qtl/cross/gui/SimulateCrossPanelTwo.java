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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.qtl.cross.SimulateCrossCommandBuilder;
import org.jax.qtl.cross.SimulateMapCommandBuilder;
import org.jax.qtl.cross.SimulateCrossCommandBuilder.SimulatedQtl;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.TextWrapper;

/**
 * The 2nd sim cross panel
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimulateCrossPanelTwo extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -116189527421901875L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SimulateCrossPanelTwo.class.getName());
    
    private final SimulateCrossCommandBuilder simulateCrossCommandBuilder;
    
    private final SimulateMapCommandBuilder simulateMapCommandBuilder;
    
    private final ItemListener selectedQtlChromosomeItemListener = new ItemListener()
    {
        public void itemStateChanged(ItemEvent e)
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                SimulateCrossPanelTwo.this.updateSelectedQtl();
            }
        }
    };
    
    private final ChangeListener selectedQtlChangeListener = new ChangeListener()
    {
        public void stateChanged(ChangeEvent e)
        {
            SimulateCrossPanelTwo.this.updateSelectedQtl();
        }
    };
    
    private final SpinnerNumberModel qtlPositionSpinnerModel =
        new SpinnerNumberModel(
                0.0,    // value
                0.0,    // min
                100.0,  // max
                1.0);   // step
    
    private final SpinnerNumberModel qtlEffect1SpinnerModel =
        new SpinnerNumberModel();
    
    private final SpinnerNumberModel qtlEffect2SpinnerModel =
        new SpinnerNumberModel();
    
    private final SpinnerNumberModel qtlEffect3SpinnerModel =
        new SpinnerNumberModel();
    
    private final DefaultTableModel qtlsTableModel = new DefaultTableModel()
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = 5387415854671697500L;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    };
    
    /**
     * Constructor
     * @param simulateCrossCommandBuilder
     *          the simulate cross command
     * @param simulateMapCommandBuilder
     *          the simulate map command
     */
    public SimulateCrossPanelTwo(SimulateMapCommandBuilder simulateMapCommandBuilder, SimulateCrossCommandBuilder simulateCrossCommandBuilder)
    {
        this.simulateCrossCommandBuilder = simulateCrossCommandBuilder;
        this.simulateMapCommandBuilder = simulateMapCommandBuilder;
        this.initComponents();
        
        this.postGuiInit();
    }

    /**
     * Take care of any of the GUI initialization that isn't handled by
     * the GUI builder
     */
    private void postGuiInit()
    {
        this.simulatedCrossNameTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        SimulateCrossPanelTwo.this.updateSimulatedCrossName();
                    }
                    
                    public void insertUpdate(DocumentEvent e)
                    {
                        SimulateCrossPanelTwo.this.updateSimulatedCrossName();
                    }
                    
                    public void removeUpdate(DocumentEvent e)
                    {
                        SimulateCrossPanelTwo.this.updateSimulatedCrossName();
                    }
                });
        
        this.qtlsTable.setModel(this.qtlsTableModel);
        this.qtlsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        
        this.qtlsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        SimulateCrossPanelTwo.this.qtlSelectionChanged();
                    }
                });
        
        this.qtlPositionSpinner.setModel(this.qtlPositionSpinnerModel);
        this.qtlEffect1Spinner.setModel(this.qtlEffect1SpinnerModel);
        this.qtlEffect2Spinner.setModel(this.qtlEffect2SpinnerModel);
        this.qtlEffect3Spinner.setModel(this.qtlEffect3SpinnerModel);
        
        this.refreshGui();
    }

    /**
     * Refresh the simulate cross GUI
     */
    public void refreshGui()
    {
        switch(this.simulateCrossCommandBuilder.getCrossType())
        {
            case BACK_CROSS:
            {
                this.qtlEffect1Label.setText(
                        "Homozygote Effect - Heterozygote Effect:");
                
                this.qtlEffect2Label.setVisible(false);
                this.qtlEffect2Spinner.setVisible(false);
                this.qtlEffect3Label.setVisible(false);
                this.qtlEffect3Spinner.setVisible(false);
                this.qtlEffect4Label.setVisible(false);
                
                this.qtlsTableModel.setColumnIdentifiers(new String[] {
                        "Chromosome",
                        "Position (cM)",
                        "Effect"});
            }
            break;
            
            case F2:
            {
                this.qtlEffect1Label.setText(
                        "Additive Effect:");
                this.qtlEffect2Label.setText(
                        "Dominance Deviation:");
                
                this.qtlEffect2Label.setVisible(true);
                this.qtlEffect2Spinner.setVisible(true);
                this.qtlEffect3Label.setVisible(false);
                this.qtlEffect3Spinner.setVisible(false);
                this.qtlEffect4Label.setVisible(false);
                
                this.qtlsTableModel.setColumnIdentifiers(new String[] {
                        "Chromosome",
                        "Position (cM)",
                        "Additive Effect",
                        "Dominance Deviation"});
            }
            break;
            
            case FOUR_WAY:
            {
                this.qtlEffect1Label.setText(
                        "AC Effect:");
                this.qtlEffect2Label.setText(
                        "BC Effect:");
                this.qtlEffect3Label.setText(
                        "AD Effect:");
                this.qtlEffect4Label.setText(
                        "BD Effect: 0");
                
                this.qtlEffect2Label.setVisible(true);
                this.qtlEffect2Spinner.setVisible(true);
                this.qtlEffect3Label.setVisible(true);
                this.qtlEffect3Spinner.setVisible(true);
                this.qtlEffect4Label.setVisible(true);
                
                this.qtlsTableModel.setColumnIdentifiers(new String[] {
                        "Chromosome",
                        "Position (cM)",
                        "AC Effect",
                        "BC Effect",
                        "AD Effect"});
            }
            break;
            
            default:
            {
                LOG.warning(
                        "Unknown cross type: " +
                        this.simulateCrossCommandBuilder.getCrossType());
            }
            break;
        }
        
        this.listenForSelectedQtlChanges(false);
        
        this.qtlChromosomeComboBox.removeAllItems();
        int chromosomeCount =
            this.simulateMapCommandBuilder.getChromosomeLengths().length;
        for(int i = 0; i < chromosomeCount; i++)
        {
            this.qtlChromosomeComboBox.addItem(
                    this.chromosomeNumberToChromosomeString(i + 1));
        }
        
        this.listenForSelectedQtlChanges(true);
        
        this.qtlsTableModel.setRowCount(0);
        
        for(SimulatedQtl simulatedQtl: this.simulateCrossCommandBuilder.getSimulatedQtls())
        {
            SimulatedQtlCell[] qtlRow = new SimulatedQtlCell[] {
                    new SimulatedQtlCell(simulatedQtl, 0),
                    new SimulatedQtlCell(simulatedQtl, 1),
                    new SimulatedQtlCell(simulatedQtl, 2),
                    new SimulatedQtlCell(simulatedQtl, 3),
                    new SimulatedQtlCell(simulatedQtl, 4)};
            this.qtlsTableModel.addRow(qtlRow);
        }
        
        this.qtlSelectionChanged();
    }

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.simulateCrossCommandBuilder.getCommand()};
    }
    
    /**
     * Update the cross identifier to match what the user typed into the name
     * field
     */
    private void updateSimulatedCrossName()
    {
        try
        {
            this.simulateCrossCommandBuilder.setCrossName(
                    RUtilities.fromReadableNameToRIdentifier(
                            this.simulatedCrossNameTextField.getText().trim()));
            this.fireCommandModified();
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.FINE,
                    "can't convert cross name to identifier: " +
                    this.simulatedCrossNameTextField.getText().trim(),
                    ex);
        }
    }
    
    private String chromosomeNumberToChromosomeString(int chromosomeNumber)
    {
        int chromosomeCount = this.simulateMapCommandBuilder.getChromosomeLengths().length;
        boolean xIncluded = this.simulateMapCommandBuilder.getIncludeXChromosome();
        
        if(chromosomeNumber == chromosomeCount && xIncluded)
        {
            return "X";
        }
        else
        {
            return Integer.toString(chromosomeNumber);
        }
    }
    
    /**
     * Update the selected qtl value
     */
    private void updateSelectedQtl()
    {
        SimulatedQtl selectedQtl = this.getSelectedQtl();
        if(selectedQtl != null)
        {
            selectedQtl.setChromosomeNumber(
                    this.qtlChromosomeComboBox.getSelectedIndex() + 1);
            selectedQtl.setPositionInCentimorgans(
                    this.qtlPositionSpinnerModel.getNumber().doubleValue());
            selectedQtl.setEffectOne(
                    this.qtlEffect1SpinnerModel.getNumber().doubleValue());
            selectedQtl.setEffectTwo(
                    this.qtlEffect2SpinnerModel.getNumber().doubleValue());
            selectedQtl.setEffectThree(
                    this.qtlEffect3SpinnerModel.getNumber().doubleValue());
            
            this.qtlsTable.repaint();
            
            this.fireCommandModified();
        }
    }
    
    /**
     * Getter for the selected qtl
     * @return
     *          the selected qtl or null if no qtls are selected
     */
    private SimulatedQtl getSelectedQtl()
    {
        int selectedRow = this.qtlsTable.getSelectedRow();
        if(selectedRow == -1)
        {
            return null;
        }
        else
        {
            SimulatedQtlCell simulatedQtlCell =
                (SimulatedQtlCell)this.qtlsTable.getValueAt(selectedRow, 0);
            return simulatedQtlCell.getSimulatedQtl();
        }
    }
    
    /**
     * Respond
     */
    private void qtlSelectionChanged()
    {
        this.listenForSelectedQtlChanges(false);
        
        SimulatedQtl selectedQtl = this.getSelectedQtl();
        if(selectedQtl != null)
        {
            this.qtlChromosomeComboBox.setSelectedItem(
                    selectedQtl.getChromosomeNumber());
            this.qtlPositionSpinnerModel.setValue(
                    selectedQtl.getPositionInCentimorgans());
            this.qtlEffect1SpinnerModel.setValue(
                    selectedQtl.getEffectOne());
            this.qtlEffect2SpinnerModel.setValue(
                    selectedQtl.getEffectTwo());
            this.qtlEffect3SpinnerModel.setValue(
                    selectedQtl.getEffectThree());
        }
        
        this.qtlChromosomeLabel.setEnabled(selectedQtl != null);
        this.qtlChromosomeComboBox.setEnabled(selectedQtl != null);
        this.qtlPositionLabel.setEnabled(selectedQtl != null);
        this.qtlPositionSpinner.setEnabled(selectedQtl != null);
        this.qtlEffectsLabel.setEnabled(selectedQtl != null);
        this.qtlEffect1Label.setEnabled(selectedQtl != null);
        this.qtlEffect1Spinner.setEnabled(selectedQtl != null);
        this.qtlEffect2Label.setEnabled(selectedQtl != null);
        this.qtlEffect2Spinner.setEnabled(selectedQtl != null);
        this.qtlEffect3Label.setEnabled(selectedQtl != null);
        this.qtlEffect3Spinner.setEnabled(selectedQtl != null);
        this.qtlEffect4Label.setEnabled(selectedQtl != null);
        this.removeQtlButton.setEnabled(selectedQtl != null);
        
        this.listenForSelectedQtlChanges(true);
    }
    
    /**
     * Either start or stop listening for changes to the QTL
     * @param listen
     *          start listening if true, stop listening otherwise
     */
    private void listenForSelectedQtlChanges(boolean listen)
    {
        if(listen)
        {
            this.qtlChromosomeComboBox.addItemListener(
                    this.selectedQtlChromosomeItemListener);
            this.qtlPositionSpinnerModel.addChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect1SpinnerModel.addChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect2SpinnerModel.addChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect3SpinnerModel.addChangeListener(
                    this.selectedQtlChangeListener);
        }
        else
        {
            this.qtlChromosomeComboBox.removeItemListener(
                    this.selectedQtlChromosomeItemListener);
            this.qtlPositionSpinnerModel.removeChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect1SpinnerModel.removeChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect2SpinnerModel.removeChangeListener(
                    this.selectedQtlChangeListener);
            this.qtlEffect3SpinnerModel.removeChangeListener(
                    this.selectedQtlChangeListener);
        }
    }
    
    /**
     * Validate the data in this panel
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        String readableCrossName =
            this.simulatedCrossNameTextField.getText().trim();
        String validationErrorMessage =
            RUtilities.getErrorMessageForReadableName(
                    readableCrossName);

        if(validationErrorMessage == null)
        {
            if(readableCrossName.length() == 0)
            {
                validationErrorMessage =
                    "The cross name cannot be empty. See help for " +
                    "more detailed information.";
            }
            else if(JRIUtilityFunctions.isTopLevelObject(
                    this.simulateCrossCommandBuilder.getCrossName(),
                    RInterfaceFactory.getRInterfaceInstance()))
            {
                validationErrorMessage =
                    "The name \"" + readableCrossName + "\" conflicts with " +
                    "an existing data object. Please choose another name.";
            }
            else
            {
                // validate the QTLs
                double[] chromosomeLengths =
                    this.simulateMapCommandBuilder.getChromosomeLengths();
                SimulatedQtl[] qtls =
                    this.simulateCrossCommandBuilder.getSimulatedQtls();
                for(int qtlIndex = 0; qtlIndex < qtls.length; qtlIndex++)
                {
                    int chromosomeNumber = qtls[qtlIndex].getChromosomeNumber();
                    if(chromosomeNumber > chromosomeLengths.length)
                    {
                        validationErrorMessage =
                            "The QTL in row " + (qtlIndex + 1) + " of the table " +
                            "has an invalid chromosome (" +
                            this.chromosomeNumberToChromosomeString(chromosomeNumber) +
                            "). Please either correct this or cancel.";
                        break;
                    }
                    else
                    {
                        double qtlPosition = qtls[qtlIndex].getPositionInCentimorgans();
                        if(qtlPosition > chromosomeLengths[chromosomeNumber - 1])
                        {
                            validationErrorMessage =
                                "The QTL in row " + (qtlIndex + 1) + " of the table " +
                                "has an invalid QTL position (" +
                                qtlPosition + " cM). The maximum value that " +
                                "can be used for chromosome " +
                                this.chromosomeNumberToChromosomeString(chromosomeNumber) +
                                " is " + chromosomeLengths[chromosomeNumber - 1] +
                                " cM. Please either correct this or cancel.";
                            break;
                        }
                    }
                }
            }
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
     * Table cell class for qtls
     */
    private class SimulatedQtlCell
    {
        private final SimulatedQtl simulatedQtl;
        
        private final int column;

        /**
         * Constructor
         * @param simulatedQtl
         *          the simulated qtl
         * @param column
         *          the column
         */
        public SimulatedQtlCell(SimulatedQtl simulatedQtl, int column)
        {
            this.simulatedQtl = simulatedQtl;
            this.column = column;
        }
        
        /**
         * @return the simulatedQtl
         */
        public SimulatedQtl getSimulatedQtl()
        {
            return this.simulatedQtl;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if(this.column == 0)
            {
                return SimulateCrossPanelTwo.this.chromosomeNumberToChromosomeString(
                        this.simulatedQtl.getChromosomeNumber());
            }
            else if(this.column == 1)
            {
                return Double.toString(
                        this.simulatedQtl.getPositionInCentimorgans());
            }
            else if(this.column == 2)
            {
                return Double.toString(
                        this.simulatedQtl.getEffectOne());
            }
            else if(this.column == 3)
            {
                return Double.toString(
                        this.simulatedQtl.getEffectTwo());
            }
            else if(this.column == 4)
            {
                return Double.toString(
                        this.simulatedQtl.getEffectThree());
            }
            else
            {
                LOG.severe("bad qtl column: " + this.column);
                return null;
            }
        }
    }
    
    private void createQtl()
    {
        SimulatedQtl simulatedQtl = new SimulatedQtl();
        
        SimulatedQtl[] currentQtls =
            this.simulateCrossCommandBuilder.getSimulatedQtls();
        SimulatedQtl[] newQtls = new SimulatedQtl[currentQtls.length + 1];
        for(int i = 0; i < currentQtls.length; i++)
        {
            newQtls[i] = currentQtls[i];
        }
        newQtls[newQtls.length - 1] = simulatedQtl;
        this.simulateCrossCommandBuilder.setSimulatedQtls(newQtls);
        
        SimulatedQtlCell[] qtlRow = new SimulatedQtlCell[] {
                new SimulatedQtlCell(simulatedQtl, 0),
                new SimulatedQtlCell(simulatedQtl, 1),
                new SimulatedQtlCell(simulatedQtl, 2),
                new SimulatedQtlCell(simulatedQtl, 3),
                new SimulatedQtlCell(simulatedQtl, 4)};
        this.qtlsTableModel.addRow(qtlRow);
        
        this.qtlsTable.getSelectionModel().setSelectionInterval(
                newQtls.length - 1,
                newQtls.length - 1);
        
        this.fireCommandModified();
    }
    
    private void removeQtl()
    {
        int selectedIndex = this.qtlsTable.getSelectedRow();
        
        if(selectedIndex != -1)
        {
            SimulatedQtl[] currentQtls =
                this.simulateCrossCommandBuilder.getSimulatedQtls();
            SimulatedQtl[] newQtls = new SimulatedQtl[currentQtls.length - 1];
            for(int i = 0; i < currentQtls.length; i++)
            {
                if(i < selectedIndex)
                {
                    newQtls[i] = currentQtls[i];
                }
                else if(i > selectedIndex)
                {
                    newQtls[i - 1] = currentQtls[i];
                }
            }
            this.simulateCrossCommandBuilder.setSimulatedQtls(newQtls);
            
            this.qtlsTableModel.removeRow(selectedIndex);
            
            this.fireCommandModified();
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

        javax.swing.JLabel simulatedCrossNameLabel = new javax.swing.JLabel();
        simulatedCrossNameTextField = new javax.swing.JTextField();
        qtlChromosomeLabel = new javax.swing.JLabel();
        qtlChromosomeComboBox = new javax.swing.JComboBox();
        qtlPositionLabel = new javax.swing.JLabel();
        qtlPositionSpinner = new javax.swing.JSpinner();
        qtlEffectsLabel = new javax.swing.JLabel();
        qtlEffect1Label = new javax.swing.JLabel();
        qtlEffect1Spinner = new javax.swing.JSpinner();
        qtlEffect2Label = new javax.swing.JLabel();
        qtlEffect2Spinner = new javax.swing.JSpinner();
        qtlEffect3Label = new javax.swing.JLabel();
        qtlEffect3Spinner = new javax.swing.JSpinner();
        qtlEffect4Label = new javax.swing.JLabel();
        javax.swing.JScrollPane qtlsScrollPane = new javax.swing.JScrollPane();
        qtlsTable = new javax.swing.JTable();
        newQtlButton = new javax.swing.JButton();
        removeQtlButton = new javax.swing.JButton();

        simulatedCrossNameLabel.setText("Simulated Cross Name:");

        qtlChromosomeLabel.setText("QTL Chromosome:");

        qtlPositionLabel.setText("QTL Position (cM):");

        qtlEffectsLabel.setText("QTL Effects:");

        qtlEffect1Label.setText("Effect 1 Label:");

        qtlEffect2Label.setText("Effect 2 Label:");

        qtlEffect3Label.setText("Effect 3 Label:");

        qtlEffect4Label.setText("Effect 4 Label:");

        qtlsScrollPane.setViewportView(qtlsTable);

        newQtlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/add-16x16.png"))); // NOI18N
        newQtlButton.setText("New QTL");
        newQtlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newQtlButtonActionPerformed(evt);
            }
        });

        removeQtlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/remove-16x16.png"))); // NOI18N
        removeQtlButton.setText("Remove QTL");
        removeQtlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeQtlButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(qtlsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(newQtlButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeQtlButton))
                    .add(layout.createSequentialGroup()
                        .add(simulatedCrossNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(simulatedCrossNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(qtlChromosomeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlChromosomeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlPositionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlPositionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(qtlEffect1Label)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect1Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect2Label)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect2Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect3Label)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect3Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(qtlEffect4Label))
                    .add(qtlEffectsLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(simulatedCrossNameLabel)
                    .add(simulatedCrossNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(qtlChromosomeLabel)
                    .add(qtlChromosomeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qtlPositionLabel)
                    .add(qtlPositionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(qtlEffectsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(qtlEffect1Label)
                    .add(qtlEffect1Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qtlEffect2Label)
                    .add(qtlEffect2Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qtlEffect3Label)
                    .add(qtlEffect3Spinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qtlEffect4Label))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(qtlsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(newQtlButton)
                    .add(removeQtlButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newQtlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newQtlButtonActionPerformed
        this.createQtl();
    }//GEN-LAST:event_newQtlButtonActionPerformed

    private void removeQtlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeQtlButtonActionPerformed
        this.removeQtl();
    }//GEN-LAST:event_removeQtlButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton newQtlButton;
    private javax.swing.JComboBox qtlChromosomeComboBox;
    private javax.swing.JLabel qtlChromosomeLabel;
    private javax.swing.JLabel qtlEffect1Label;
    private javax.swing.JSpinner qtlEffect1Spinner;
    private javax.swing.JLabel qtlEffect2Label;
    private javax.swing.JSpinner qtlEffect2Spinner;
    private javax.swing.JLabel qtlEffect3Label;
    private javax.swing.JSpinner qtlEffect3Spinner;
    private javax.swing.JLabel qtlEffect4Label;
    private javax.swing.JLabel qtlEffectsLabel;
    private javax.swing.JLabel qtlPositionLabel;
    private javax.swing.JSpinner qtlPositionSpinner;
    private javax.swing.JTable qtlsTable;
    private javax.swing.JButton removeQtlButton;
    private javax.swing.JTextField simulatedCrossNameTextField;
    // End of variables declaration//GEN-END:variables
    
}
