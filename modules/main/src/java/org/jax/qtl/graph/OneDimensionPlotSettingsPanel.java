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

package org.jax.qtl.graph;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A panel for editing the visual settings of a graph.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class OneDimensionPlotSettingsPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} should have one of these
     */
    private static final long serialVersionUID = 1457262190704139854L;
    
    private final OneDimensionPlot oneDimensionPlot;
    
    /**
     * Constructor
     * @param oneDimensionPlot
     *          the graph w/ axes that we're editing
     */
    public OneDimensionPlotSettingsPanel(OneDimensionPlot oneDimensionPlot)
    {
        this.oneDimensionPlot = oneDimensionPlot;
        
        this.initComponents();
        
        String graphTitle = this.oneDimensionPlot.getTitle();
        String graphXLabel = this.oneDimensionPlot.getXLabel();
        String graphYLabel = this.oneDimensionPlot.getYLabel();
        
        if(graphTitle != null)
        {
            this.graphTitleTextField.setText(graphTitle);
        }
        if(graphXLabel != null)
        {
            this.xAxisTextField.setText(graphXLabel);
        }
        if(graphYLabel != null)
        {
            this.yAxisTextField.setText(graphYLabel);
        }
        
        this.graphTitleTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.graphTitleChanged();
            }

            public void insertUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.graphTitleChanged();
            }

            public void removeUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.graphTitleChanged();
            }
        });
        
        this.xAxisTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.xAxisLabelChanged();
            }

            public void insertUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.xAxisLabelChanged();
            }

            public void removeUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.xAxisLabelChanged();
            }
        });
        
        this.yAxisTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.yAxisLabelChanged();
            }

            public void insertUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.yAxisLabelChanged();
            }

            public void removeUpdate(DocumentEvent e)
            {
                OneDimensionPlotSettingsPanel.this.yAxisLabelChanged();
            }
        });
    }

    /**
     * notification that the y axis label changed
     */
    private void yAxisLabelChanged()
    {
        this.oneDimensionPlot.setYlabel(this.yAxisTextField.getText());
    }

    /**
     * notification that the x axis label changed
     */
    private void xAxisLabelChanged()
    {
        this.oneDimensionPlot.setXlabel(this.xAxisTextField.getText());
    }

    /**
     * notification that the graph title changed
     */
    private void graphTitleChanged()
    {
        this.oneDimensionPlot.setTitle(this.graphTitleTextField.getText());
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
        java.awt.GridBagConstraints gridBagConstraints;

        graphTitleLabel = new javax.swing.JLabel();
        graphTitleTextField = new javax.swing.JTextField();
        xAxisLabel = new javax.swing.JLabel();
        xAxisTextField = new javax.swing.JTextField();
        yAxisLabel = new javax.swing.JLabel();
        yAxisTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        graphTitleLabel.setText("Graph Title:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(graphTitleLabel, gridBagConstraints);

        graphTitleTextField.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(graphTitleTextField, gridBagConstraints);

        xAxisLabel.setText("X Axis Label:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(xAxisLabel, gridBagConstraints);

        xAxisTextField.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(xAxisTextField, gridBagConstraints);

        yAxisLabel.setText("Y Axis Label:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(yAxisLabel, gridBagConstraints);

        yAxisTextField.setPreferredSize(new java.awt.Dimension(200, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(yAxisTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel graphTitleLabel;
    private javax.swing.JTextField graphTitleTextField;
    private javax.swing.JLabel xAxisLabel;
    private javax.swing.JTextField xAxisTextField;
    private javax.swing.JLabel yAxisLabel;
    private javax.swing.JTextField yAxisTextField;
    // End of variables declaration//GEN-END:variables
    
}
