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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.graph.OneDimensionPlotContainerPanel;
import org.jax.qtl.graph.RfPlot;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.util.Tools;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

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
public class RfPlotDialog extends JDialog implements Constants {
    /**
     * 
     */
    private static final long serialVersionUID = -824385139238584485L;

    private int selectedCrossIndex, selectedMaxit = MAXIT_DEFAULT;
    
    private JTextField tfMaxit, tfTol;
    private String selectedTol = TOL_DEFAULT;
    private int[] selectedChrIndexes = new int[] {0};
    private Thread runRcommand;
    private boolean newChrTable = true;
    private ListSelectionTable tbChr;

    private Cross[] crosses;

    /**
     * Constructor
     */
    public RfPlotDialog() {
        super(QTL.getInstance().getApplicationFrame(), "Recombination Fraction Plot", true);
        QtlProject activeProject =
            QtlProjectManager.getInstance().getActiveProject();
        this.crosses = activeProject.getDataModel().getCrosses();
        
        // TODO do the right thing with cross indexes
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
        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int line = 1;
        result.add(makeCrossSelectionPane(), Tools.setGbc(c, 1,line++,1,1));
        result.add(makeParameterSelectionPane(), Tools.setGbc(c, 1,line++,1,1));
        result.add(makeChromosomeSelectionPane(), Tools.setGbc(c, 1,line++,1,1));
        result.add(makeButtonPane(), Tools.setGbc(c, 1,line++,1,1));

        return result;
    }

    private JPanel makeButtonPane() {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();

                RfPlotDialog.this.runRcommand = new Thread(new Runnable() {
                    public void run() {
                        // set current cross in QTL
                        // run R command to get the estimated map
                        // maxit
                        String maxit = RfPlotDialog.this.tfMaxit.getText();
                        if (maxit.equals(MAXIT_DEFAULT + "")) maxit = "";
                        else maxit = ", " + maxit;

                        // tol
                        String tol = RfPlotDialog.this.tfTol.getText();
                        if (tol.equals(TOL_DEFAULT)) tol = "";
                        else tol = ", " + tol;

                        final Cross selectedCross =
                            RfPlotDialog.this.crosses[RfPlotDialog.this.selectedCrossIndex];
                        String rcmd =
                            selectedCross.getAccessorExpressionString() +
                            " <- est.rf(" +
                            selectedCross.getAccessorExpressionString() +
                            maxit +
                            tol + ")";

                        String comment =
                            "Estimate the sex-averaged recombination fraction for cross: " +
                            selectedCross.getAccessorExpressionString() + " ...";
                        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                        synchronized(rInterface)
                        {
                            rInterface.insertComment(comment);
                            rInterface.evaluateCommandNoReturn(rcmd);
                        }

                        // get the est.rf result from R for drawing RF plot
                        final double[][] rf;
                        synchronized(rInterface)
                        {
                            String rcmdForGetRf = 
                                selectedCross.getAccessorExpressionString() +
                                "$rf";
                            REXP rfExpression = rInterface.evaluateCommand(
                                    new SilentRCommand(rcmdForGetRf));
                            rf = rfExpression.asDoubleMatrix();
                        }
                        
                        // update UI on EDT
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                String iframeid =
                                    "Cross" +
                                    selectedCross.getAccessorExpressionString() +
                                    ".rf";
                                RfPlot temp = new RfPlot(selectedCross, rf, RfPlotDialog.this.selectedChrIndexes);

                                QTL.getInstance().getDesktop().createInternalFrame(
                                        new OneDimensionPlotContainerPanel(temp),
                                        "RF Plot for cross: " + selectedCross.getAccessorExpressionString(),
                                        null,
                                        iframeid);

                                // parameters for making R command
                                String rcmd = "plot.rf(" + selectedCross.getAccessorExpressionString() + ")";
                                String comment =
                                    "Plot recombination fraction plot for cross: " +
                                    selectedCross.getAccessorExpressionString() + " ...";
                                
                                RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                                synchronized(rInterface)
                                {
                                    rInterface.insertComment(comment);
                                    
                                    // insert a comment in place of the command
                                    // since we're taking care of the plot in
                                    // java
                                    rInterface.insertComment(rcmd);
                                }
                            }
                        });
                    }
                });
                RfPlotDialog.this.runRcommand.start();
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
        CSH.setHelpIDString(helpButton, "RF_Plot");
        helpButton.addActionListener(new CSH.DisplayHelpFromSource(hs, "javax.help.SecondaryWindow", null));

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(helpButton);

        return buttonPane;
    }

    private JPanel makeCrossSelectionPane() {
        final JComboBox crossListCombobox = new JComboBox(this.crosses);
        int defaultHeight = (int) crossListCombobox.getPreferredSize().getHeight();
        crossListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH, defaultHeight));
        crossListCombobox.setSelectedIndex(this.selectedCrossIndex);
        crossListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int oldCrossIndex = RfPlotDialog.this.selectedCrossIndex;
                RfPlotDialog.this.selectedCrossIndex = crossListCombobox.getSelectedIndex();
                if (oldCrossIndex != RfPlotDialog.this.selectedCrossIndex) {
                    RfPlotDialog.this.newChrTable = true;
                    setCurrentPane(makeContentPane());
                }
            }
        });

        JPanel result = new JPanel();
        result.add(crossListCombobox);
        result.setBorder(BorderFactory.createTitledBorder(" Choose cross "));

        return result;
    }

    private JPanel makeParameterSelectionPane() {
        JLabel maxitLable = new JLabel(MAXIT_LABEL);
        JLabel tolLabel = new JLabel(TOL_LABEL);

        this.tfMaxit = new JTextField(this.selectedMaxit + "");
        this.tfMaxit.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                try {
                    RfPlotDialog.this.selectedMaxit = Integer.parseInt(RfPlotDialog.this.tfMaxit.getText());
                }
                catch (NumberFormatException ee) {
                }
            }

            public void insertUpdate(DocumentEvent e) {
                try {
                    RfPlotDialog.this.selectedMaxit = Integer.parseInt(RfPlotDialog.this.tfMaxit.getText());
                }
                catch (NumberFormatException ee) {
                }
            }

            public void removeUpdate(DocumentEvent e) {
                try {
                    RfPlotDialog.this.selectedMaxit = Integer.parseInt(RfPlotDialog.this.tfMaxit.getText());
                }
                catch (NumberFormatException ee) {
                }
            }
        });

        this.tfMaxit.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, (int) this.tfMaxit.getPreferredSize().getHeight()));

        this.tfTol = new JTextField(this.selectedTol);
        this.tfTol.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {}

            public void insertUpdate(DocumentEvent e) {
                try {
                    RfPlotDialog.this.selectedTol = RfPlotDialog.this.tfTol.getText();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            public void removeUpdate(DocumentEvent e) {}
        });

        this.tfTol.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, (int) this.tfTol.getPreferredSize().getHeight()));

        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int line = 1;
        result.add(maxitLable, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfMaxit, Tools.setGbc(c, 2, line++, 1, 1));
        result.add(tolLabel, Tools.setGbc(c, 1, line, 1, 1));
        result.add(this.tfTol, Tools.setGbc(c, 2, line++, 1, 1));

        result.setBorder(BorderFactory.createTitledBorder(" Parameters for estimate recombination fraction "));
        return result;
    }

    private JPanel makeChromosomeSelectionPane() {
        final Cross selectedCross =
            RfPlotDialog.this.crosses[this.selectedCrossIndex];

        if (this.newChrTable) {
            List<CrossChromosome> genoData = selectedCross.getGenotypeData();
            String[] chromosomeNames = new String[genoData.size()];
            for(int i = 0; i < chromosomeNames.length; i++)
            {
                chromosomeNames[i] = genoData.get(i).getChromosomeName();
            }
            
            this.tbChr = new ListSelectionTable(chromosomeNames, "Chromosomes", true, this);
            this.selectedChrIndexes = this.tbChr.getSelections();
            this.newChrTable = false;
        }
        this.tbChr.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                RfPlotDialog.this.selectedChrIndexes = RfPlotDialog.this.tbChr.getSelections();
                if (RfPlotDialog.this.selectedChrIndexes.length == 0) RfPlotDialog.this.selectedChrIndexes = new int[] {0};
                RfPlotDialog.this.tbChr.setDefaultSelection(RfPlotDialog.this.selectedChrIndexes);
                setCurrentPane(makeContentPane());
            }
        });

        JScrollPane holder = new JScrollPane(this.tbChr);
        holder.setPreferredSize(new Dimension( (int) holder.getPreferredSize().getWidth() / 2, (int) holder.getPreferredSize().getHeight() / 4));

        JPanel result = new JPanel();
        result.add(holder);

        result.setBorder(BorderFactory.createTitledBorder(" Choose chromosome to plot recombination fraction "));
        return result;

    }
}
