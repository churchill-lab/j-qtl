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

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.scan.ScanResultFilter;
import org.jax.qtl.scan.ScanResultFilter.AbsoluteConfidenceFilter;
import org.jax.qtl.scan.ScanResultFilter.MarkerRelativeConfidenceFilter;

/**
 * A panel for editing scan result confidence values
 */
public class ScanResultConfidenceFilterEditorPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4042163952500958444L;
    
    private final ScanResultFilter scanResultFilter;
    
    private final ItemListener markerRelativeFilterItemListener =
        new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                ScanResultConfidenceFilterEditorPanel.this.markerRelativeFilterItemChanged();
            }
        };
    
    private final ChangeListener markerRelativeFilterSpinnerChangeListener =
        new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ScanResultConfidenceFilterEditorPanel.this.markerRelativeFilterSpinnerChanged();
            }
        };
    
    private final ItemListener absoluteFilterItemListener =
        new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                ScanResultConfidenceFilterEditorPanel.this.absoluteFilterItemChanged();
            }
        };
        
    private final ChangeListener absoluteFilterSpinnerChangeListener =
        new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ScanResultConfidenceFilterEditorPanel.this.absoluteFilterSpinnerChanged();
            }
        };

    private final boolean alphaValuesAreAvailable;

    /**
     * Constructor
     * @param scanResultFilter
     *          the filter
     * @param alphaValuesAreAvailable
     *          true iff we can filter on alpha values
     */
    public ScanResultConfidenceFilterEditorPanel(
            ScanResultFilter scanResultFilter,
            boolean alphaValuesAreAvailable)
    {
        this.scanResultFilter = scanResultFilter;
        this.alphaValuesAreAvailable = alphaValuesAreAvailable;
        this.initComponents();
        
        this.postGuiInit();
    }
    
    /**
     * Handle the GUI initialization that the GUI builder does not
     * take care of
     */
    private void postGuiInit()
    {
        this.absoluteConfidenceFilterValueSpinner.setModel(
                new SpinnerNumberModel());
        this.markerRelativeFilterSpinner.setModel(
                new SpinnerNumberModel());
        
        this.reinitializeAbsoluteGui();
        this.reinitializeMarkerRelativeGui();
    }
    
    private void reinitializeAbsoluteGui()
    {
        // stop listening for a bit
        this.absoluteConfidenceFilterComboBox.removeItemListener(
                this.absoluteFilterItemListener);
        
        // initialize absolute filters
        this.absoluteConfidenceFilterComboBox.addItem(
                AbsoluteConfidenceFilter.NO_ABSOLUTE_CONFIDENCE_FILTER);
        this.absoluteConfidenceFilterComboBox.addItem(
                AbsoluteConfidenceFilter.LOD_SCORE_FILTER);
        if(this.alphaValuesAreAvailable)
        {
            this.absoluteConfidenceFilterComboBox.addItem(
                    AbsoluteConfidenceFilter.ALPHA_VALUE_FILTER);
        }
        this.absoluteConfidenceFilterComboBox.setSelectedItem(
                this.scanResultFilter.getAbsoluteConfidenceFilterState());
        
        // start listening again
        this.absoluteConfidenceFilterComboBox.addItemListener(
                this.absoluteFilterItemListener);
        
        // fake change notifications in order to update the spinner values
        this.reinitializeAbsoluteSpinner();
    }
    
    /**
     * Reinitialize the "absolute" filter spinner component
     */
    private void reinitializeAbsoluteSpinner()
    {
        // stop listening for a bit
        this.absoluteConfidenceFilterValueSpinner.removeChangeListener(
                this.absoluteFilterSpinnerChangeListener);
        
        // update the spinners model & value
        AbsoluteConfidenceFilter absoluteFilterState =
            this.scanResultFilter.getAbsoluteConfidenceFilterState();
        switch(absoluteFilterState)
        {
            case NO_ABSOLUTE_CONFIDENCE_FILTER:
            {
                this.absoluteConfidenceFilterValueSpinner.setModel(
                        new SpinnerNumberModel());
                this.absoluteConfidenceFilterValueSpinner.setEnabled(false);
            }
            break;
            
            case LOD_SCORE_FILTER:
            {
                this.absoluteConfidenceFilterValueSpinner.setModel(
                        ScanResultConfidenceFilterEditorPanel.createLodThresholdSpinnerModel());
                this.absoluteConfidenceFilterValueSpinner.setEnabled(true);
            }
            break;
            
            case ALPHA_VALUE_FILTER:
            {
                this.absoluteConfidenceFilterValueSpinner.setModel(
                        ScanResultConfidenceFilterEditorPanel.createAlphaThresholdSpinnerModle());
                this.absoluteConfidenceFilterValueSpinner.setEnabled(true);
            }
            break;
            
            default:
            {
                throw new IllegalStateException(
                        "Unknown scan result filter type: " +
                        absoluteFilterState.name());
            }
        }
        
        this.getAbsoluteSpinnerModel().setValue(
                this.scanResultFilter.getAbsoluteConfidenceFilterValue());
        
        // start listening again
        this.absoluteConfidenceFilterValueSpinner.addChangeListener(
                this.absoluteFilterSpinnerChangeListener);
    }
    
    /**
     * reinitialize the "marker relative" filter spinner component
     */
    private void reinitializeMarkerRelativeSpinner()
    {
        // stop listening for a bit
        this.markerRelativeFilterSpinner.removeChangeListener(
                this.markerRelativeFilterSpinnerChangeListener);
        
        // update the spinners model & value
        MarkerRelativeConfidenceFilter markerRelativeFilterState =
            this.scanResultFilter.getMarkerRelativeConfidenceFilterState();
        switch(markerRelativeFilterState)
        {
            case LOCAL_MAXIMA_WITH_MINIMUM_SPACING_FILTER:
            {
                this.markerRelativeFilterSpinner.setModel(
                        ScanResultConfidenceFilterEditorPanel.createMinimumMarkerSpacingSpinnerModel());
                this.markerRelativeFilterSpinner.setEnabled(true);
            }
            break;
            
            default:
            {
                this.markerRelativeFilterSpinner.setModel(
                        new SpinnerNumberModel());
                this.markerRelativeFilterSpinner.setEnabled(false);
            }
            break;
        }
        
        this.getMarkerRelativeSpinnerModel().setValue(
                this.scanResultFilter.getMarkerRelativeConfidenceFilterValue());
        
        // start listening again
        this.markerRelativeFilterSpinner.addChangeListener(
                this.markerRelativeFilterSpinnerChangeListener);
    }
    
    /**
     * reinitialize "marker relative" GUI components
     */
    private void reinitializeMarkerRelativeGui()
    {
        // stop listening for a bit
        this.markerRelativeFilterComboBox.removeItemListener(
                this.markerRelativeFilterItemListener);
        
        // initialize marker relative filters
        this.markerRelativeFilterComboBox.addItem(
                MarkerRelativeConfidenceFilter.NO_MARKER_RELATIVE_CONFIDENCE_FILTER);
        this.markerRelativeFilterComboBox.addItem(
                MarkerRelativeConfidenceFilter.CHROMOSOME_MAXIMA_FILTER);
        this.markerRelativeFilterComboBox.setSelectedItem(
                this.scanResultFilter.getAbsoluteConfidenceFilterState());
        
        // start listening again
        this.markerRelativeFilterComboBox.addItemListener(
                this.markerRelativeFilterItemListener);
        
        // fake change notifications in order to update the spinner values
        this.reinitializeMarkerRelativeSpinner();
    }
    
    /**
     * the method is called when the absolute filter item state changes
     */
    private void absoluteFilterItemChanged()
    {
        this.scanResultFilter.setAbsoluteConfidenceFilterState(
                (AbsoluteConfidenceFilter)this.absoluteConfidenceFilterComboBox.getSelectedItem());
        this.reinitializeAbsoluteSpinner();
    }
    
    /**
     * the method is called when the marker relative filter item state changes
     */
    private void markerRelativeFilterItemChanged()
    {
        this.scanResultFilter.setMarkerRelativeConfidenceFilterState(
                (MarkerRelativeConfidenceFilter)this.markerRelativeFilterComboBox.getSelectedItem());
        this.reinitializeMarkerRelativeSpinner();
    }

    /**
     * this method is called when the marker relative spinner is changed
     */
    private void markerRelativeFilterSpinnerChanged()
    {
        if(this.markerRelativeFilterSpinner.isEnabled())
        {
            Number value = this.getMarkerRelativeSpinnerModel().getNumber();
            if(value != null)
            {
                this.scanResultFilter.setMarkerRelativeConfidenceFilterValue(
                        value.doubleValue());
            }
        }
    }
    
    /**
     * Convenience getter for the marker relative spinner number model
     * @return
     *          the model
     */
    private SpinnerNumberModel getMarkerRelativeSpinnerModel()
    {
        return (SpinnerNumberModel)this.markerRelativeFilterSpinner.getModel();
    }

    /**
     * Respond to a change in the filter spinner
     */
    private void absoluteFilterSpinnerChanged()
    {
        if(this.absoluteConfidenceFilterValueSpinner.isEnabled())
        {
            Number value = this.getAbsoluteSpinnerModel().getNumber();
            if(value != null)
            {
                this.scanResultFilter.setAbsoluteConfidenceFilterValue(
                        value.doubleValue());
            }
        }
    }
    
    /**
     * Convenience getter for the absolute spinner number model
     * @return
     *          the model
     */
    private SpinnerNumberModel getAbsoluteSpinnerModel()
    {
        return (SpinnerNumberModel)this.absoluteConfidenceFilterValueSpinner.getModel();
    }

    /**
     * Getter for the scan result filter
     * @return the scan result filter
     */
    public ScanResultFilter getScanResultFilter()
    {
        return this.scanResultFilter;
    }
    
    /**
     * Create a new model for the LOD threshold
     * @return
     *          the new model
     */
    private static SpinnerNumberModel createLodThresholdSpinnerModel()
    {
        return new SpinnerNumberModel(
                3.0,                // initial value
                0.0,                // min
                Double.MAX_VALUE,   // max
                0.5);               // step size
    }
    
    /**
     * create a new model for the alpha threshold
     * @return
     *          the new model
     */
    private static SpinnerNumberModel createAlphaThresholdSpinnerModle()
    {
        return new SpinnerNumberModel(
                0.1,    // initial value
                0.0,    // min
                1.0,    // max
                0.05);  // step size
    }
    
    /**
     * create a new model for the minimum spacing spinner
     * @return
     *          the new model
     */
    private static SpinnerNumberModel createMinimumMarkerSpacingSpinnerModel()
    {
        return new SpinnerNumberModel(
                0,                  // initial value
                0,                  // min
                100,                // max
                5);                 // step size
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

        markerRelativeFilterSpinner = new javax.swing.JSpinner();
        absoluteConfidenceFilterLabel = new javax.swing.JLabel();
        absoluteConfidenceFilterComboBox = new javax.swing.JComboBox();
        absoluteConfidenceFilterValueSpinner = new javax.swing.JSpinner();
        markerRelativeFilterLabel = new javax.swing.JLabel();
        markerRelativeFilterComboBox = new javax.swing.JComboBox();

        absoluteConfidenceFilterLabel.setText("Absolute Confidence Filter:");

        markerRelativeFilterLabel.setText("Relative Confidence Filter:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(absoluteConfidenceFilterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(absoluteConfidenceFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(absoluteConfidenceFilterValueSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(markerRelativeFilterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(markerRelativeFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(absoluteConfidenceFilterLabel)
                    .add(absoluteConfidenceFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(absoluteConfidenceFilterValueSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(markerRelativeFilterLabel)
                    .add(markerRelativeFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox absoluteConfidenceFilterComboBox;
    private javax.swing.JLabel absoluteConfidenceFilterLabel;
    private javax.swing.JSpinner absoluteConfidenceFilterValueSpinner;
    private javax.swing.JComboBox markerRelativeFilterComboBox;
    private javax.swing.JLabel markerRelativeFilterLabel;
    private javax.swing.JSpinner markerRelativeFilterSpinner;
    // End of variables declaration//GEN-END:variables
    
}
