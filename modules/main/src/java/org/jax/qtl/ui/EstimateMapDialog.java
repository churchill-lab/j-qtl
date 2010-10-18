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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.SexAwareGeneticMap;
import org.jax.qtl.graph.CompareGeneticMapPlot;
import org.jax.qtl.graph.OneDimensionPlotContainerPanel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.util.Tools;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.RObject;

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
public class EstimateMapDialog extends JDialog implements Constants {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2428987948707392732L;
    private int selectedCrossIndex;
    private JTextField tfGenoErrorRate, tfm, tfp, tfMaxit, tfTol;
    private JComboBox mapFunctionCombobox, sexSpCombobox;
    private Thread runRcommand;
    private final Cross[] crosses;

    /**
     * Constructor
     */
    public EstimateMapDialog() {
        super(QTL.getInstance().getApplicationFrame(), "Estimate Genetic Map", true);
        
        QtlProject project = QtlProjectManager.getInstance().getActiveProject();
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
        JPanel selectionPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        selectionPane.add(makeCrossSelectionPane(), Tools.setGbc(c, 1,1,1,1));
        selectionPane.add(makeParameterSelectionPane(), Tools.setGbc(c, 1,2,1,1));
        JPanel result = new JPanel(new BorderLayout());
        result.add(selectionPane, BorderLayout.CENTER);
        result.add(makeButtonPane(), BorderLayout.SOUTH);

        return result;
    }

    private JPanel makeButtonPane() {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
                EstimateMapDialog.this.runRcommand = new Thread(new Runnable(){
                    public void run() {
                        // run R command to get the estimated map
                        // map function
                        String mapFunction = MAP_FUNCTION[EstimateMapDialog.this.mapFunctionCombobox.getSelectedIndex()];
                        if (mapFunction.equals(MAP_FUNCTION[0]))
                        {
                            mapFunction = "";
                        }
                        else
                        {
                            String mapFunc = mapFunction.toLowerCase().trim();
                            if (mapFunc.equals("carter-falconer"))
                                mapFunc = "c-f";
                            mapFunction = ", map.function=\"" + mapFunc + "\"";
                        }
        
                        // m
                        String m = EstimateMapDialog.this.tfm.getText();
                        if (m.equals(ZERO)) m = "";
                        else m = ", m=" + m;
        
                        // p
                        String p = EstimateMapDialog.this.tfp.getText();
                        if (p.equals(ZERO)) p = "";
                        else p = ", p=" + p;
        
                        // maxit
                        String maxit = EstimateMapDialog.this.tfMaxit.getText();
                        if (maxit.equals(MAXIT_DEFAULT + "")) maxit = "";
                        else maxit = ", maxit=" + maxit;
        
                        // tol
                        String tol = EstimateMapDialog.this.tfTol.getText();
                        if (tol.equals(TOL_DEFAULT)) tol = "";
                        else tol = ", tol=" + tol;
        
                        // sex.sp
                        String sexSp = TRUE_FALSE[EstimateMapDialog.this.sexSpCombobox.getSelectedIndex()];
                        if (sexSp.equals(TRUE_FALSE[0])) sexSp = "";
                        else sexSp = ", sex.sp=FALSE";
        
//                        final CrossObj currentCross = (CrossObj) crossList.elementAt(selectedCrossIndex);
                        final Cross selectedCross =
                            EstimateMapDialog.this.crosses[EstimateMapDialog.this.selectedCrossIndex];
                        final String newMapName =
                            selectedCross.getAccessorExpressionString() + ".newmap";
                        String rcmd =
                            newMapName + " <- est.map(" +
                            selectedCross.getAccessorExpressionString() +
                            mapFunction + m + p + maxit + tol + sexSp + ")";
                        String comment =
                            "Re-estimate the genetic map for cross: " +
                            selectedCross.getAccessorExpressionString() + " ...";
                        
                        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                        rInterface.insertComment(comment);
                        rInterface.evaluateCommandNoReturn(rcmd);
                        
                        final List<SexAwareGeneticMap> estimatedGeneticMaps =
                            SexAwareGeneticMap.extractMaps(new RObject(
                                    rInterface,
                                    newMapName));
                        
                        // update UI on EDT
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String iframeid =
                                    "Cross" +
                                    selectedCross.getAccessorExpressionString() +
                                    ".estmap";
                                if (!QTL.getInstance().getDesktop().setIframeSelected(iframeid)) {
                                    CompareGeneticMapPlot temp = new CompareGeneticMapPlot(
                                            selectedCross,
                                            estimatedGeneticMaps);
        
                                    QTL.getInstance().getDesktop().createInternalFrame(
                                            new OneDimensionPlotContainerPanel(temp),
                                            "Estimated genetic map of cross: " + selectedCross.toString(),
                                            null,
                                            iframeid);
                                }
                                else {
                                    QTL.getInstance().getDesktop().getComponents();
                                }
        
                                // parameters for making "plot" R command
                                String plotCommandAsComment =
                                    "plot(" +
                                    selectedCross.getAccessorExpressionString() +
                                    ", " + newMapName + ")";
                                String comment =
                                    "Plot comparison of genetic map for cross: " +
                                    selectedCross.getAccessorExpressionString() + " ...";
                                RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                                rInterface.insertComment(comment);
                                rInterface.insertComment(plotCommandAsComment);
        
                            }
                                });
                    }
                });
                EstimateMapDialog.this.runRcommand.start();
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
        CSH.setHelpIDString(helpButton, "Display_and_Estimate_Genetic_Map");
        helpButton.addActionListener(new CSH.DisplayHelpFromSource(hs, "javax.help.SecondaryWindow", null));

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(helpButton);

        return buttonPane;
    }

    private JPanel makeCrossSelectionPane() {
        final JComboBox crossListCombobox = new JComboBox(
                this.crosses);
        int defaultHeight = (int)crossListCombobox.getPreferredSize().getHeight();
        crossListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        crossListCombobox.setSelectedIndex(this.selectedCrossIndex);
        crossListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                EstimateMapDialog.this.selectedCrossIndex = crossListCombobox.getSelectedIndex();
            }
        });

        JPanel result = new JPanel();
        result.add(crossListCombobox);
        result.setBorder(BorderFactory.createTitledBorder(" Choose cross "));

        return result;
    }

    private JPanel makeParameterSelectionPane() {
        JLabel genoErrorRateLabel = new JLabel(GENO_ERROR_RATE_LABEL);
        JLabel mapFuncLabel = new JLabel(MAP_FUNCTION_LABEL);
        JLabel mLabel = new JLabel(M_LABEL);
        JLabel pLabel = new JLabel(P_LABEL);
        JLabel maxitLable = new JLabel(MAXIT_LABEL);
        JLabel tolLabel = new JLabel(TOL_LABEL);
        JLabel setSpLabel = new JLabel(SEX_SP_LABEL);

        this.tfGenoErrorRate = new JTextField(ERROR_PROB_DEFAULT);
        this.tfm = new JTextField(ZERO);
        this.tfp = new JTextField(ZERO);
        this.tfMaxit = new JTextField(MAXIT_DEFAULT+"");
        this.tfTol = new JTextField(TOL_DEFAULT);

        this.mapFunctionCombobox = new JComboBox(MAP_FUNCTION);
        this.sexSpCombobox = new JComboBox(TRUE_FALSE);

        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int line = 1;
        result.add(genoErrorRateLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfGenoErrorRate, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(mapFuncLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.mapFunctionCombobox, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(mLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfm, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(pLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfp, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(maxitLable, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfMaxit, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(tolLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfTol, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(setSpLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.sexSpCombobox, Tools.setGbc(c, 2, line++, 1, 1));
        result.setBorder(BorderFactory.createTitledBorder(" Choose parameters "));
        return result;
    }
}
