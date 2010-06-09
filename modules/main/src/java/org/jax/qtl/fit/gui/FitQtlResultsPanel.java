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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.fit.AnovaTable;
import org.jax.qtl.fit.FitQtlResult;
import org.jax.qtl.gui.ExportDataTableAction;

/**
 * A panel to use for displaying fit results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitQtlResultsPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6994446301556766851L;
    
    private static final String HELP_ID_STRING = "Fit_Results";
    
    /**
     * Constructor
     * @param availableCrosses
     *          the available crosses
     * @param selectedFitResult
     *          the selected fit results
     */
    public FitQtlResultsPanel(
            Cross[] availableCrosses,
            FitQtlResult selectedFitResult)
    {
        this.initComponents();
        
        for(Cross cross: availableCrosses)
        {
            this.crossSelectionComboBox.addItem(cross);
        }
        
        if(selectedFitResult != null)
        {
            this.crossSelectionComboBox.setSelectedItem(
                    selectedFitResult.getParentCross());
            this.selectedCrossChanged();
            this.fitResultsSelectionComboBox.setSelectedItem(selectedFitResult);
            this.selectedFitResultChanged();
        }
        else
        {
            this.selectedCrossChanged();
        }
        
        // start listening to events
        this.crossSelectionComboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                FitQtlResultsPanel.this.selectedCrossChanged();
            }
        });
        this.fitResultsSelectionComboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                FitQtlResultsPanel.this.selectedFitResultChanged();
            }
        });
        
        // initialize the help stuff
        HelpSet hs = QTL.getInstance().getMenubar().getHelpSet();
        CSH.setHelpIDString(
                this.helpButton,
                HELP_ID_STRING);
        this.helpButton.addActionListener(
                new CSH.DisplayHelpFromSource(
                        hs,
                        SecondaryWindow.class.getName(),
                        null));
    }
    
    /**
     * respond to a change in the selected cross
     */
    private void selectedCrossChanged()
    {
        this.fitResultsSelectionComboBox.removeAllItems();
        
        Cross selectedCross = this.getSelectedCross();
        if(selectedCross != null)
        {
            for(FitQtlResult fitResult: selectedCross.getFitQtlResults())
            {
                this.fitResultsSelectionComboBox.addItem(fitResult);
            }
        }
        
        this.selectedFitResultChanged();
    }
    
    /**
     * Getter for the selected cross
     * @return
     *          the selected cross
     */
    private Cross getSelectedCross()
    {
        return (Cross)this.crossSelectionComboBox.getSelectedItem();
    }
    
    /**
     * Getter for the selected fit
     * @return
     *          the selected fit
     */
    private FitQtlResult getSelectedFitResult()
    {
        return (FitQtlResult)this.fitResultsSelectionComboBox.getSelectedItem();
    }

    /**
     * respond to an update in the selected fit result
     */
    private void selectedFitResultChanged()
    {
        this.fitResultsTabbedPane.removeAll();
        
        FitQtlResult selectedFitResult = this.getSelectedFitResult();
        if(selectedFitResult != null)
        {
            AnovaTablePanel fullResultsPanel = new AnovaTablePanel(
                    selectedFitResult.getFullResults());
            this.fitResultsTabbedPane.addTab(
                    "Full Results",
                    fullResultsPanel);
            
            AnovaTable dropOneTermResults =
                selectedFitResult.getDropOneTermResults();
            if(dropOneTermResults != null)
            {
                AnovaTablePanel dropOneTermResultsPanel = new AnovaTablePanel(
                        dropOneTermResults);
                this.fitResultsTabbedPane.addTab(
                        "Drop-One-Term Results",
                        dropOneTermResultsPanel);
            }
        }
    }
    
    /**
     * respond to a change in the selected tab
     */
    private void selectedTabChanged()
    {
        Component selectedPanel = this.fitResultsTabbedPane.getSelectedComponent();
        if(selectedPanel instanceof AnovaTablePanel)
        {
            AnovaTablePanel selectedAnovaPanel = (AnovaTablePanel)selectedPanel;
            this.exportTableButton.setAction(new ExportDataTableAction(
                    selectedAnovaPanel.getDataTable()));
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

        crossSelectionLabel = new javax.swing.JLabel();
        crossSelectionComboBox = new javax.swing.JComboBox();
        fitResultsSelectionLabel = new javax.swing.JLabel();
        fitResultsSelectionComboBox = new javax.swing.JComboBox();
        fitResultsTabbedPane = new javax.swing.JTabbedPane();
        exportTableButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        crossSelectionLabel.setText("Cross:");

        fitResultsSelectionLabel.setText("Fit Results:");

        fitResultsTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fitResultsTabbedPaneStateChanged(evt);
            }
        });

        exportTableButton.setText("Export Table ...");

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help ...");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fitResultsTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fitResultsSelectionLabel)
                            .add(crossSelectionLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(crossSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fitResultsSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(exportTableButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(helpButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossSelectionLabel)
                    .add(crossSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fitResultsSelectionLabel)
                    .add(fitResultsSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fitResultsTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(exportTableButton)
                    .add(helpButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fitResultsTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fitResultsTabbedPaneStateChanged
        this.selectedTabChanged();
    }//GEN-LAST:event_fitResultsTabbedPaneStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox crossSelectionComboBox;
    private javax.swing.JLabel crossSelectionLabel;
    private javax.swing.JButton exportTableButton;
    private javax.swing.JComboBox fitResultsSelectionComboBox;
    private javax.swing.JLabel fitResultsSelectionLabel;
    private javax.swing.JTabbedPane fitResultsTabbedPane;
    private javax.swing.JButton helpButton;
    // End of variables declaration//GEN-END:variables
    
}
