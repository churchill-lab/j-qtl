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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.util.Tools;
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
public class ImputationDialog extends JDialog implements Constants
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4262088650064267945L;
    
    private Cross selectedCross;
    private final Cross[] availableCrosses;
    private JTextField tfNumDraws, tfStepSize, tfGenoErrorRate, tfDistPastTerminal;
    private final static String NUM_DRAWS = "16.0", STEP = "2.0"; // default
    private JComboBox mapFunctionCombobox, stepWidthTypeCombobox;
    private Thread runRcommand;

    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param availableCrosses
     *          the crosses the user can choose from
     * @param selectedCross
     *          the initially selected cross
     */
    public ImputationDialog(
            java.awt.Frame parent,
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        super(QTL.getInstance().getApplicationFrame(), "Simulate Genotype Probabilities", true);
        this.availableCrosses = availableCrosses;
        this.selectedCross = selectedCross;
        setCurrentPane(makeContentPane());

        this.setLocation(this.getParent().getX() + 150, this.getParent().getY() + 150);
        this.setModal(true);
        this.pack();
    }
    
    /**
     * Constructor
     * @param parent
     *          the parent dialog for this dialog
     * @param availableCrosses
     *          the crosses the user can choose from
     * @param selectedCross
     *          the initially selected cross
     */
    public ImputationDialog(
            JDialog parent,
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        super(parent, "Simulate Genotype Probabilities", true);
        this.availableCrosses = availableCrosses;
        this.selectedCross = selectedCross;
        setCurrentPane(makeContentPane());

        this.setModal(true);
        this.pack();
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
                ImputationDialog.this.runRcommand = new Thread(new Runnable(){
                    public void run() {
                        // pass the selected cross and parameters to R to calculate the genotype probability
                        int draws = (int) Double.parseDouble(ImputationDialog.this.tfNumDraws.getText());
                        double step = Double.parseDouble(ImputationDialog.this.tfStepSize.getText());
                        double errorProb = Double.parseDouble(ImputationDialog.this.tfGenoErrorRate.getText());
                        double offend = Double.parseDouble(ImputationDialog.this.tfDistPastTerminal.getText());
                        String mapFunction = MAP_FUNCTION[ImputationDialog.this.mapFunctionCombobox.getSelectedIndex()];
                        String stepwidth = STEP_WIDTH[ImputationDialog.this.stepWidthTypeCombobox.getSelectedIndex()];

                        // use above seven lines of information to run imputation in R:
                        String mapFunc = mapFunction.toLowerCase().trim();
                        if (mapFunc.equals("carter-falconer"))
                            mapFunc = "c-f";
                        String rcmd = ImputationDialog.this.selectedCross.getAccessorExpressionString() + " <- sim.geno(" + ImputationDialog.this.selectedCross.getAccessorExpressionString() +
                            ", n.draws=" + draws + ", step=" + step + ", off.end=" + offend + ", error.prob=" +
                            errorProb + ", map.function=\"" + mapFunc + "\", stepwidth=\"" +
                            stepwidth.toLowerCase().trim() + "\")";
                        String comment = "Running imputation for cross: " + ImputationDialog.this.selectedCross.getAccessorExpressionString() + " ...";
                        
                        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                        synchronized(rInterface)
                        {
                            rInterface.insertComment(comment);
                            rInterface.evaluateCommandNoReturn(rcmd);
                        }

                        QtlProjectManager.getInstance().notifyActiveProjectModified();
                    }
                });
                ImputationDialog.this.runRcommand.start();
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
        CSH.setHelpIDString(helpButton, "Simulate_Genotype_Probabilities");
        helpButton.addActionListener(new CSH.DisplayHelpFromSource(hs, "javax.help.SecondaryWindow", null));

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(helpButton);

        return buttonPane;
    }

    private JPanel makeCrossSelectionPane() {
        final JComboBox crossListCombobox = new JComboBox(
                this.availableCrosses);
        int defaultHeight = (int)crossListCombobox.getPreferredSize().getHeight();
        crossListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        if(this.selectedCross == null)
        {
            this.selectedCross = (Cross)crossListCombobox.getSelectedItem();
        }
        else
        {
            crossListCombobox.setSelectedItem(this.selectedCross);
        }
        
        crossListCombobox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    ImputationDialog.this.selectedCross = (Cross)e.getItem();
                }
            }
        });

        JPanel result = new JPanel();
        result.add(crossListCombobox);
        result.setBorder(BorderFactory.createTitledBorder(" Choose cross "));

        return result;
    }

    private JPanel makeParameterSelectionPane() {
        JLabel numDrawsLabel = new JLabel(NUM_DRAWS_LABEL);
        JLabel stepSizeLabel = new JLabel(STEP_SIZE_LABEL);
        JLabel genoErrorRateLabel = new JLabel(GENO_ERROR_RATE_LABEL);
        JLabel distPastTerminalLabel = new JLabel(DIST_PAST_TERMINAL_LABEL);
        JLabel mapFuncLabel = new JLabel(MAP_FUNCTION_LABEL);
        JLabel stepWidthTypeLabel = new JLabel(STEP_WIDTH_TYPE_LABEL);

        this.tfNumDraws = new JTextField(NUM_DRAWS);
        this.tfStepSize = new JTextField(STEP);
        this.tfGenoErrorRate = new JTextField(ERROR_PROB_DEFAULT);
        this.tfDistPastTerminal = new JTextField(OFF_END_DEFAULT);

        this.mapFunctionCombobox = new JComboBox(MAP_FUNCTION);
        this.stepWidthTypeCombobox = new JComboBox(STEP_WIDTH);

        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        result.add(numDrawsLabel, Tools.setGbc(c, 1,1,1,1));
        result.add(this.tfNumDraws, Tools.setGbc(c, 2,1,1,1));
        result.add(stepSizeLabel, Tools.setGbc(c, 1,2,1,1));
        result.add(this.tfStepSize, Tools.setGbc(c, 2,2,1,1));
        result.add(genoErrorRateLabel, Tools.setGbc(c, 1,3,1,1));
        result.add(this.tfGenoErrorRate, Tools.setGbc(c, 2,3,1,1));
        result.add(distPastTerminalLabel, Tools.setGbc(c, 1,4,1,1));
        result.add(this.tfDistPastTerminal, Tools.setGbc(c, 2,4,1,1));
        result.add(mapFuncLabel, Tools.setGbc(c, 1,5,1,1));
        result.add(this.mapFunctionCombobox, Tools.setGbc(c, 2,5,1,1));
        result.add(stepWidthTypeLabel, Tools.setGbc(c, 1,6,1,1));
        result.add(this.stepWidthTypeCombobox, Tools.setGbc(c, 2,6,1,1));
        result.setBorder(BorderFactory.createTitledBorder(" Choose parameters "));
        return result;
    }
}
