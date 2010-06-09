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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.jax.analyticgraph.data.NamedDataMatrix;
import org.jax.analyticgraph.data.ReorderedNamedDataMatrix;
import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisRenderingGraph;
import org.jax.analyticgraph.graph.scatterplot.ScatterPlot;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.gui.SimpleGraphContainerPanel;


/**
 * Dialog for choosing which phenotypes to show in a scatterplot.
 */
public class ShowScatterPlotDialog extends javax.swing.JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3852079975247381225L;
    
    private volatile Cross selectedCross;
    
    private final Cross[] availableCrosses;
    
    private static final Logger LOG = Logger.getLogger(
            ShowScatterPlotDialog.class.getName());
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param selectedCross
     *          the selected cross
     * @param availableCrosses
     *          the available crosses
     */
    public ShowScatterPlotDialog(
            java.awt.Frame parent,
            Cross selectedCross,
            Cross[] availableCrosses)
    {
        super(parent, true);
        
        this.selectedCross = selectedCross;
        this.availableCrosses = availableCrosses;
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * take care of any GUI initialization that isn't handled by the
     * GUI builder
     */
    private void postGuiInit()
    {
        this.xPhenotypeList.setModel(
                new DefaultListModel());
        this.xPhenotypeList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        this.yPhenotypeList.setModel(
                new DefaultListModel());
        this.yPhenotypeList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        
        Cross selectedCross = this.selectedCross;
        for(Cross currCross: this.availableCrosses)
        {
            this.crossComboBox.addItem(currCross);
        }
        
        if(selectedCross != null)
        {
            this.crossComboBox.setSelectedItem(selectedCross);
        }
    }

    /**
     * Rebuild the phenotype lists using the selected cross
     */
    private void rebuildPhenotypeLists()
    {
        Cross selectedCross = this.selectedCross;
        if(selectedCross != null)
        {
            ShowScatterPlotDialog.fillListWithPhenotypes(
                    selectedCross,
                    this.xPhenotypeList);
            ShowScatterPlotDialog.fillListWithPhenotypes(
                    selectedCross,
                    this.yPhenotypeList);
            
            if(this.xPhenotypeList.getModel().getSize() >= 2 &&
               this.yPhenotypeList.getModel().getSize() >= 2)
            {
                this.xPhenotypeList.setSelectedIndex(0);
                this.yPhenotypeList.setSelectedIndex(1);
            }
        }
    }
    
    /**
     * Fill in the given list with phenotype name strings from the given cross
     * @param selectedCross
     *          the source of the phenotype
     * @param phenotypeList
     *          the list
     */
    private static void fillListWithPhenotypes(
            Cross selectedCross,
            JList phenotypeList)
    {
        DefaultListModel listModel = (DefaultListModel)phenotypeList.getModel();
        listModel.removeAllElements();
        for(String phenotypeName:
            selectedCross.getPhenotypeData().getDataNames())
        {
            listModel.addElement(phenotypeName);
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

        actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        crossLabel = new javax.swing.JLabel();
        crossComboBox = new javax.swing.JComboBox();
        xPhenotypeLabel = new javax.swing.JLabel();
        xPhenotypeScrollPane = new javax.swing.JScrollPane();
        xPhenotypeList = new javax.swing.JList();
        yPhenotypeLabel = new javax.swing.JLabel();
        yPhenotypeScrollPane = new javax.swing.JScrollPane();
        yPhenotypeList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        actionPanel.add(cancelButton);

        crossLabel.setText("Cross:");

        crossComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                crossComboBoxItemStateChanged(evt);
            }
        });

        xPhenotypeLabel.setText("X Axis Phenotype:");

        xPhenotypeScrollPane.setViewportView(xPhenotypeList);

        yPhenotypeLabel.setText("Y Axis Phenotype:");

        yPhenotypeScrollPane.setViewportView(yPhenotypeList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(crossLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(289, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(xPhenotypeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(xPhenotypeLabel)
                        .add(74, 74, 74)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(yPhenotypeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, yPhenotypeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
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
                    .add(xPhenotypeLabel)
                    .add(yPhenotypeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(yPhenotypeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                    .add(xPhenotypeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        Cross selectedCross = this.selectedCross;
        int selectedPhenoIndex1 = this.xPhenotypeList.getSelectedIndex();
        int selectedPhenoIndex2 = this.yPhenotypeList.getSelectedIndex();
        if(selectedCross != null && selectedPhenoIndex1 != -1 && selectedPhenoIndex2 != -1)
        {
            NamedDataMatrix<Number> allPhenoData = selectedCross.getPhenotypeData();
            NamedDataMatrix<Number> selectedPhenoData =
                new ReorderedNamedDataMatrix<Number>(
                        allPhenoData,
                        new int[] {selectedPhenoIndex1, selectedPhenoIndex2});
            
            GraphCoordinateConverter coordinateConverter = new SimpleGraphCoordinateConverter(
                    0.0, 0.0,
                    1.0, 1.0);
            ScatterPlot scatterPlotInterior = new ScatterPlot(coordinateConverter);
            scatterPlotInterior.plotData(selectedPhenoData);
    
            SimpleGraphCoordinateConverter coordinateConverter2 = new SimpleGraphCoordinateConverter(
                    0.0, 0.0,
                    1.0, 1.0);
    
            AxisRenderingGraph scatterPlotWithAxes =
                new AxisRenderingGraph(coordinateConverter2);
            scatterPlotWithAxes.setInteriorGraph(scatterPlotInterior);
    
            org.jax.analyticgraph.framework.Graph2DComponent graphComponent =
                new org.jax.analyticgraph.framework.Graph2DComponent();
            graphComponent.setBackground(Color.WHITE);
            graphComponent.addGraph2D(scatterPlotWithAxes);
            graphComponent.setPreferredSize(new Dimension(400, 400));
            
            SimpleGraphContainerPanel simpleGraphContainerPanel =
                new SimpleGraphContainerPanel(
                        graphComponent,
                        scatterPlotInterior);
            
            QTL.getInstance().getDesktop().createInternalFrame(
                    simpleGraphContainerPanel,
                    "Scatter Plot: x = " +
                    this.xPhenotypeList.getSelectedValue().toString() +
                    ", y = " +
                    this.yPhenotypeList.getSelectedValue().toString() +
                    " (" + selectedCross.toString() +")",
                    null,
                    this.xPhenotypeList.getSelectedValue().toString() +
                    "x" +
                    this.yPhenotypeList.getSelectedValue().toString());
        }
        else
        {
            LOG.severe(
                    "failed to show scatter plot: " +
                    "pheno 1 index: " + selectedPhenoIndex1 +
                    " pheno 2 index: " + selectedPhenoIndex2 +
                    " cross: " + selectedCross);
        }
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void crossComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crossComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.selectedCross = (Cross)evt.getItem();
            this.rebuildPhenotypeLists();
        }
    }//GEN-LAST:event_crossComboBoxItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox crossComboBox;
    private javax.swing.JLabel crossLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel xPhenotypeLabel;
    private javax.swing.JList xPhenotypeList;
    private javax.swing.JScrollPane xPhenotypeScrollPane;
    private javax.swing.JLabel yPhenotypeLabel;
    private javax.swing.JList yPhenotypeList;
    private javax.swing.JScrollPane yPhenotypeScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
