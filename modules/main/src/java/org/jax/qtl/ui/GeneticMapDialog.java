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
package org.jax.qtl.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.graph.GeneticMapPlot;
import org.jax.qtl.graph.OneDimensionPlotContainerPanel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;

/**
 * <p>Title: QTL data analysis</p>
 *
 * <p>Description: </p>
 *
 * <p>Company: The Jackson Laboratory</p>
 *
 * @author Lei Wu
 * @version 1.0
 */
public class GeneticMapDialog extends JDialog implements Constants {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6791440163516396570L;
    private int selectedCrossIndex;
    private final Cross[] crosses;

    /**
     * Constructor
     */
    public GeneticMapDialog() {
        super(QTL.getInstance().getApplicationFrame(), "Genectic Map", true);
        QtlProject project =
            QtlProjectManager.getInstance().getActiveProject();
        this.crosses = project.getDataModel().getCrosses();
        
        // TODO do the correct thing with the cross index
        this.selectedCrossIndex = 0;
        setCurrentPane(makeContentPane());

        this.setLocation(this.getParent().getX() + 150, this.getParent().getY() + 150);
        this.setModal(true);
        this.pack();

        setVisible(true);
    }

    // set the contentPane to the given content. This is used for updating the current window
    private void setCurrentPane(JPanel content) {
        getContentPane().removeAll();
        this.setContentPane(content);
        this.validate();
        this.pack();
    }

    private JPanel makeContentPane() {
        JPanel result = new JPanel(new BorderLayout());
        result.add(makeCrossSelectionPane());
        result.add(makeButtonPane(), BorderLayout.SOUTH);

        return result;
    }

    private JPanel makeButtonPane() {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
                // pass the selected cross to show the corresponding cross descriptive table
                Cross tempCross = GeneticMapDialog.this.crosses[GeneticMapDialog.this.selectedCrossIndex];
                String iframeid = "Cross" + tempCross.getAccessorExpressionString() + ".geneticmap";
                if (!QTL.getInstance().getDesktop().setIframeSelected(iframeid)) {
                    GeneticMapPlot temp = new GeneticMapPlot(tempCross);
                    
                    QTL.getInstance().getDesktop().createInternalFrame(
                            new OneDimensionPlotContainerPanel(temp),
                            "Genetic map for cross: " + tempCross.toString(),
                            null,
                            iframeid);
                }
                // print the ploting R command in log area
                String rcmd = "plot.map(" + tempCross.getAccessorExpressionString() + ")";
                String comment = "Plot genetic map for " + tempCross.getAccessorExpressionString() + " ...";
                RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                synchronized(rInterface)
                {
                    rInterface.insertComment(comment);
                    
                    // print the plot command as a comment since we're doing the
                    // real plotting in java
                    rInterface.insertComment(rcmd);
                }
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        JButton helpButton = new JButton("Help");
        HelpSet hs = QTL.getInstance().getMenubar().getHelpSet();
        CSH.setHelpIDString(helpButton, "analysis.geno.Display_Genetic_Map");
        helpButton.addActionListener(new CSH.DisplayHelpFromSource(hs, "javax.help.SecondaryWindow", null));

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(helpButton);

        return buttonPane;
    }

    private JPanel makeCrossSelectionPane() {
        final JComboBox crossListCombobox = new JComboBox(this.crosses);
        int defaultHeight = (int)crossListCombobox.getPreferredSize().getHeight();
        crossListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        crossListCombobox.setSelectedIndex(this.selectedCrossIndex);
        crossListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                GeneticMapDialog.this.selectedCrossIndex = crossListCombobox.getSelectedIndex();
            }
        });

        JPanel result = new JPanel();
        result.add(crossListCombobox);
        result.setBorder(BorderFactory.createTitledBorder(" Choose cross "));

        return result;
    }
}
