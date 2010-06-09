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

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.cross.SimulateCrossCommandBuilder;
import org.jax.qtl.cross.SimulateMapCommandBuilder;
import org.jax.qtl.cross.Cross.CrossSubType;
import org.jax.qtl.cross.SimulateCrossCommandBuilder.MapFunction;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorPanel;

/**
 * The 1st sim cross panel
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimulateCrossPanelOne extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6956336179249044949L;
    
    private final SimulateMapCommandBuilder simulateMapCommandBuilder;
    
    private final SimulateCrossCommandBuilder simulateCrossCommandBuilder;
    
    private final SpinnerNumberModel numIndividualsSpinnerModel =
        new SpinnerNumberModel(
                100,                // value
                0,                  // min
                Integer.MAX_VALUE,  // max
                1);                 // step
    
    private final SpinnerNumberModel genoErrorRateSpinnerModel =
        new SpinnerNumberModel(
                0.0,
                0.0,
                1.0,
                0.1);
    
    private final SpinnerNumberModel missingGenoRateSpinnerModel =
        new SpinnerNumberModel(
                0.0,
                0.0,
                1.0,
                0.1);
    
    private final SpinnerNumberModel partiallyInformativeRateSpinnerModel =
        new SpinnerNumberModel(
                0.0,
                0.0,
                1.0,
                0.1);
    
    private final SpinnerNumberModel probabilityOfNoInterferenceSpinnerModel =
        new SpinnerNumberModel(
                0.0,
                0.0,
                1.0,
                0.1);
    
    private final SpinnerNumberModel interferenceParameterSpinnerModel =
        new SpinnerNumberModel(
                0.0,
                0.0,
                Integer.MAX_VALUE,
                0.1);
    
    private final ChangeListener updateOnAnyChangeListener =
        new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                SimulateCrossPanelOne.this.updateCommand();
            }
        };
    
    private final ItemListener updateOnAnyItemChangeListener =
        new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    SimulateCrossPanelOne.this.updateCommand();
                }
            }
        };
    
    /**
     * Constructor
     * @param simulateMapCommandBuilder
     *          this map command builder
     * @param simulateCrossCommandBuilder
     *          the cross command builder
     */
    public SimulateCrossPanelOne(
            SimulateMapCommandBuilder simulateMapCommandBuilder,
            SimulateCrossCommandBuilder simulateCrossCommandBuilder)
    {
        this.simulateMapCommandBuilder = simulateMapCommandBuilder;
        this.simulateCrossCommandBuilder = simulateCrossCommandBuilder;
        this.initComponents();
        
        this.postGuiInit();
    }
    
    /**
     * Take care of the GUI initialization that wasn't handled by the GUI
     * builder
     */
    private void postGuiInit()
    {
        this.numIndividualsSpinner.setModel(
                this.numIndividualsSpinnerModel);
        this.numIndividualsSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getNumIndividuals());
        this.numIndividualsSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        this.genoErrorRateSpinner.setModel(
                this.genoErrorRateSpinnerModel);
        this.genoErrorRateSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getGenotypingErrorRate());
        this.genoErrorRateSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        this.missingGenoRateSpinner.setModel(
                this.missingGenoRateSpinnerModel);
        this.missingGenoRateSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getMissingGenotypeRate());
        this.missingGenoRateSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        this.partiallyInformativeRateSpinner.setModel(
                this.partiallyInformativeRateSpinnerModel);
        this.partiallyInformativeRateSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getPartiallyInformativeRate());
        this.partiallyInformativeRateSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        this.probabilityOfNoInterferenceSpinner.setModel(
                this.probabilityOfNoInterferenceSpinnerModel);
        this.probabilityOfNoInterferenceSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getProbabilityOfNoInterference());
        this.probabilityOfNoInterferenceSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        this.interferenceParameterSpinner.setModel(
                this.interferenceParameterSpinnerModel);
        this.interferenceParameterSpinnerModel.setValue(
                this.simulateCrossCommandBuilder.getInterferenceParameter());
        this.interferenceParameterSpinnerModel.addChangeListener(
                this.updateOnAnyChangeListener);
        
        for(CrossSubType crossType: CrossSubType.values())
        {
            this.crossTypeComboBox.addItem(crossType);
        }
        this.crossTypeComboBox.setSelectedItem(
                this.simulateCrossCommandBuilder.getCrossType());
        this.crossTypeComboBox.addItemListener(
                this.updateOnAnyItemChangeListener);
        
        for(MapFunction mapFunction: MapFunction.values())
        {
            this.mapFunctionComboBox.addItem(mapFunction);
        }
        this.mapFunctionComboBox.setSelectedItem(
                this.simulateCrossCommandBuilder.getMapFunction());
        this.mapFunctionComboBox.addItemListener(
                this.updateOnAnyItemChangeListener);
    }

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.simulateCrossCommandBuilder.getCommand()};
    }

    /**
     * Validate the data in this panel
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        // the user can't enter invalid data
        return true;
    }
    
    /**
     * Update the cross command
     */
    private void updateCommand()
    {
        this.simulateCrossCommandBuilder.setNumIndividuals(
                this.numIndividualsSpinnerModel.getNumber().intValue());
        this.simulateCrossCommandBuilder.setGenotypingErrorRate(
                this.genoErrorRateSpinnerModel.getNumber().doubleValue());
        this.simulateCrossCommandBuilder.setMissingGenotypeRate(
                this.missingGenoRateSpinnerModel.getNumber().doubleValue());
        this.simulateCrossCommandBuilder.setPartiallyInformativeRate(
                this.partiallyInformativeRateSpinnerModel.getNumber().doubleValue());
        this.simulateCrossCommandBuilder.setProbabilityOfNoInterference(
                this.probabilityOfNoInterferenceSpinnerModel.getNumber().doubleValue());
        this.simulateCrossCommandBuilder.setInterferenceParameter(
                this.interferenceParameterSpinnerModel.getNumber().doubleValue());
        
        CrossSubType crossType =
            (CrossSubType)this.crossTypeComboBox.getSelectedItem();
        this.simulateCrossCommandBuilder.setCrossType(crossType);
        this.simulateCrossCommandBuilder.setMapFunction(
                (MapFunction)this.mapFunctionComboBox.getSelectedItem());
        
        this.simulateMapCommandBuilder.setCreateSexSpecificGeneticMaps(
                crossType == CrossSubType.FOUR_WAY);
        
        this.fireCommandModified();
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

        javax.swing.JLabel numIndividualsLabel = new javax.swing.JLabel();
        numIndividualsSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel crossTypeLabel = new javax.swing.JLabel();
        crossTypeComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel mapFunctionLabel = new javax.swing.JLabel();
        mapFunctionComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel genoErrorRateLabel = new javax.swing.JLabel();
        genoErrorRateSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel missingGenoRateLabel = new javax.swing.JLabel();
        missingGenoRateSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel partiallyInformativeRateLabel = new javax.swing.JLabel();
        partiallyInformativeRateSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel probabilityOfNoInterferenceLabel = new javax.swing.JLabel();
        probabilityOfNoInterferenceSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel interferenceParameterLabel = new javax.swing.JLabel();
        interferenceParameterSpinner = new javax.swing.JSpinner();

        numIndividualsLabel.setText("Number of Individuals:");

        crossTypeLabel.setText("Cross Type:");

        mapFunctionLabel.setText("Map Function:");

        genoErrorRateLabel.setText("Genotyping Error Rate (0-1):");

        missingGenoRateLabel.setText("Missing Genotype Rate (0-1):");

        partiallyInformativeRateLabel.setText("Partially Informative Rate (0-1):");

        probabilityOfNoInterferenceLabel.setText("Probability of No Interference (0-1):");

        interferenceParameterLabel.setText("Interference Parameter:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(numIndividualsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(numIndividualsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(crossTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(crossTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mapFunctionLabel)
                        .add(5, 5, 5)
                        .add(mapFunctionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(probabilityOfNoInterferenceLabel)
                            .add(partiallyInformativeRateLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(partiallyInformativeRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(genoErrorRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(missingGenoRateLabel))
                                    .add(layout.createSequentialGroup()
                                        .add(probabilityOfNoInterferenceSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(interferenceParameterLabel)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(interferenceParameterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(missingGenoRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                    .add(genoErrorRateLabel))
                .addContainerGap(85, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(numIndividualsLabel)
                    .add(numIndividualsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossTypeLabel)
                    .add(crossTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(mapFunctionLabel)
                    .add(mapFunctionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(genoErrorRateLabel)
                    .add(genoErrorRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(missingGenoRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(missingGenoRateLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(partiallyInformativeRateLabel)
                    .add(partiallyInformativeRateSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(probabilityOfNoInterferenceLabel)
                    .add(probabilityOfNoInterferenceSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(interferenceParameterLabel)
                    .add(interferenceParameterSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox crossTypeComboBox;
    private javax.swing.JSpinner genoErrorRateSpinner;
    private javax.swing.JSpinner interferenceParameterSpinner;
    private javax.swing.JComboBox mapFunctionComboBox;
    private javax.swing.JSpinner missingGenoRateSpinner;
    private javax.swing.JSpinner numIndividualsSpinner;
    private javax.swing.JSpinner partiallyInformativeRateSpinner;
    private javax.swing.JSpinner probabilityOfNoInterferenceSpinner;
    // End of variables declaration//GEN-END:variables
    
}
