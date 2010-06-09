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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.graph.OneDimensionPlotContainerPanel;
import org.jax.qtl.graph.ScantwoPlot;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.qtl.util.Tools;
import org.jax.r.RUtilities;
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
public class PlotScantwoDialog extends JDialog implements Constants {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3864323983920317584L;
    private int selectedCrossIndex, selectedScantwoIndex = 0, selectedPhenoIndex = 0; // default indexes
    private int DEFAULT_UPPER_INDEX = 4, DEFAULT_LOWER_INDEX = 0;
    private int selectedUpperIndex = this.DEFAULT_UPPER_INDEX, selectedLowerIndex = this.DEFAULT_LOWER_INDEX, selectedColorMapIndex = 0;
    private int[] selectedChrIndexes = {0};
    private boolean plotColorScale = true;
//    private ListSelectionTable tbChr;
//    private boolean newChrTable = true;
    private final Cross[] crosses;

    /**
     * Constructor
     * @param resultsToPlot
     *          the results we need to plot
     */
    public PlotScantwoDialog(ScanTwoResult resultsToPlot)
    {
        super(QTL.getInstance().getApplicationFrame(), "Plot Two QTL Genome Scan Result", true);
        QtlDataModel dataModel =
            QtlProjectManager.getInstance().getActiveProject().getDataModel();
        this.crosses =  dataModel.getCrosses();
        
        // TODO do the right thing with the cross index
        this.selectedCrossIndex = 0;
        setCurrentPane(makeContentPane(resultsToPlot));

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

    private JPanel makeContentPane(ScanTwoResult resultsToPlot) {
        JPanel result = new JPanel(new BorderLayout());
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        contents.add(makeCrossSelectionPane(resultsToPlot), Tools.setGbc(c,1,1,1,1));
        contents.add(makeScanResultSelectionPane(resultsToPlot), Tools.setGbc(c,1,2,1,1));
        contents.add(makePhenoSelectionPane(), Tools.setGbc(c, 1,3,1,1));
//        contents.add(makeChromosomeSelectionPane(), Tools.setGbc(c, 2,1,1,3));
        contents.add(makeParamtersPane(), Tools.setGbc(c, 2,1,1,3));
        result.add(contents, BorderLayout.CENTER);
        result.add(makeButtonPane(), BorderLayout.SOUTH);

        return result;
    }
    
    private JPanel makePhenoSelectionPane() {
        Cross selectedCross = this.crosses[this.selectedCrossIndex];
        Set<ScanTwoResult> selectedScanTwoResultsSet = selectedCross.getScanTwoResults();
        ScanTwoResult[] selectedScanTwoResults = selectedScanTwoResultsSet.toArray(
                new ScanTwoResult[selectedScanTwoResultsSet.size()]);
        Arrays.sort(selectedScanTwoResults);
        JComboBox tempPhenoListCombobox;
        if(selectedScanTwoResults.length >= 1) {
            ScanTwoResult selectedScanTwo = selectedScanTwoResults[this.selectedScantwoIndex];
            
            tempPhenoListCombobox = new JComboBox(
                    selectedScanTwo.getScannedPhenotypeNames());
        }
        else
            tempPhenoListCombobox = new JComboBox(new String[]{NO_SCANTWO_MESSAGE});
        final JComboBox phenoListCombobox = tempPhenoListCombobox;
        int defaultHeight = (int)phenoListCombobox.getPreferredSize().getHeight();
        phenoListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        phenoListCombobox.setSelectedIndex(this.selectedPhenoIndex);
        phenoListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PlotScantwoDialog.this.selectedPhenoIndex = phenoListCombobox.getSelectedIndex();
                setCurrentPane(makeContentPane(null));
            }
        });

        JPanel result = new JPanel();
        result.add(phenoListCombobox);

        result.setBorder(BorderFactory.createTitledBorder(" Choose scanned trait "));
        return result;
    }
    
    // TODO should we add chromosome selection back
    /*
    private JPanel makeChromosomeSelectionPane() {
//        CrossObj currentCross = (CrossObj)crossList.elementAt(selectedCrossIndex);
        Cross selectedCross = this.crosses[this.selectedCrossIndex];
//        ScantwoObj selectedScantwo = (ScantwoObj)currentCross.getScantwoVector().elementAt(selectedScantwoIndex);
        Set<ScanTwoResult> selectedScanTwoResultsSet = selectedCross.getScanTwoResults();
        ScanTwoResult[] selectedScanTwoResults = selectedScanTwoResultsSet.toArray(
                new ScanTwoResult[selectedScanTwoResultsSet.size()]);
        Arrays.sort(selectedScanTwoResults);
        ScanTwoResult selectedScanTwo =
            selectedScanTwoResults[this.selectedScantwoIndex];

        if (newChrTable) {
            tbChr = new ListSelectionTable(selectedScantwo.getChromosomeNames(), "Chromosomes", true, this);
            selectedChrIndexes = tbChr.getSelections();
            newChrTable = false;
        }
        tbChr.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                selectedChrIndexes = tbChr.getSelections();
                if (selectedChrIndexes.length == 0) selectedChrIndexes = new int[] {0};
                tbChr.setDefaultSelection(selectedChrIndexes);
                setCurrentPane(makeContentPane());
            }
        });

        JPanel result = new JPanel();
        result.add(tbChr);

        result.setBorder(BorderFactory.createTitledBorder(" Chr "));
        return result;

    }
    */

    private JPanel makeParamtersPane() {
        JLabel upperLabel = new JLabel(UPPER_LOD_LABEL);
        JLabel lowerLabel = new JLabel(LOWER_LOD_LABEL);
//        JLabel colorMapLable = new JLabel(COLOR_MAP_LABEL);

        final JComboBox cbboxUpper = new JComboBox(LOD_TYPE);
        cbboxUpper.setSelectedIndex(this.selectedUpperIndex);
        cbboxUpper.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PlotScantwoDialog.this.selectedUpperIndex = cbboxUpper.getSelectedIndex();
            }
        });

        final JComboBox cbboxLower = new JComboBox(LOD_TYPE);
        cbboxLower.setSelectedIndex(this.selectedLowerIndex);
        cbboxLower.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PlotScantwoDialog.this.selectedLowerIndex = cbboxLower.getSelectedIndex();
            }
        });

        final JCheckBox ckbColorScale = new JCheckBox(ADD_COLOR_MAP_SCALE);
        ckbColorScale.setSelected(this.plotColorScale);
        ckbColorScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                PlotScantwoDialog.this.plotColorScale = ckbColorScale.isSelected();
            }
        });


        final JComboBox cbboxColorMap = new JComboBox(COLOR_MAP_LIST);
        cbboxColorMap.setSelectedIndex(this.selectedColorMapIndex);
        cbboxColorMap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PlotScantwoDialog.this.selectedColorMapIndex = cbboxColorMap.getSelectedIndex();
            }
        });


        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int line = 1;
        result.add(upperLabel, Tools.setGbc(c, 1,line,2,1));
        result.add(cbboxUpper, Tools.setGbc(c, 2,line++,2,1));
        result.add(lowerLabel, Tools.setGbc(c, 1,line,1,1));
        result.add(cbboxLower, Tools.setGbc(c, 2,line++,1,1));
//        result.add(colorMapLable, Tools.setGbc(c, 1,line,1,1));
//        result.add(cbboxColorMap, Tools.setGbc(c, 2,line++,1,1));
        result.add(ckbColorScale, Tools.setGbc(c, 1,line++,2,1));

        result.setBorder(BorderFactory.createTitledBorder(" Choose Parameters "));
        return result;
    }

    private JPanel makeScanResultSelectionPane(ScanTwoResult resultsToPlot) {
        Cross selectedCross = this.crosses[this.selectedCrossIndex];
        Set<ScanTwoResult> availableScanTwosSet =
            selectedCross.getScanTwoResults();
        JComboBox tempScantwoListCombobox;
        if(!availableScanTwosSet.isEmpty()) {
            tempScantwoListCombobox = new JComboBox();
            ScanTwoResult[] availableScanTwos = availableScanTwosSet.toArray(
                    new ScanTwoResult[availableScanTwosSet.size()]);
            Arrays.sort(availableScanTwos);
            for(ScanTwoResult currScanTwo: availableScanTwos)
            {
                tempScantwoListCombobox.addItem(currScanTwo.toString());
            }
            
            if(resultsToPlot != null)
            {
                this.selectedScantwoIndex = 0;
                for(int i = 0; i < availableScanTwos.length; i++)
                {
                    if(availableScanTwos[i].equals(resultsToPlot))
                    {
                        tempScantwoListCombobox.setSelectedIndex(i);
                        this.selectedScantwoIndex = i;
                        break;
                    }
                }
            }
        }
        else
        {
            tempScantwoListCombobox = new JComboBox(new String[] {NO_SCANTWO_MESSAGE});
            this.selectedScantwoIndex = 0;
        }
        final JComboBox scantwoListCombobox = tempScantwoListCombobox;
        int defaultHeight = (int)scantwoListCombobox.getPreferredSize().getHeight();
        scantwoListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        scantwoListCombobox.setSelectedIndex(this.selectedScantwoIndex);
        scantwoListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int oldScantwoIndex = PlotScantwoDialog.this.selectedScantwoIndex;
                PlotScantwoDialog.this.selectedScantwoIndex = scantwoListCombobox.getSelectedIndex();
                if (oldScantwoIndex != PlotScantwoDialog.this.selectedScantwoIndex) {
                    PlotScantwoDialog.this.selectedPhenoIndex = 0;
//                    PlotScantwoDialog.this.newChrTable = true;
                    setCurrentPane(makeContentPane(null));
                }
            }
        });

        JPanel result = new JPanel();
        result.add(scantwoListCombobox);

        result.setBorder(BorderFactory.createTitledBorder(" Choose pair scan result "));
        return result;
    }

    private JPanel makeButtonPane() {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
                // plot the scantwo result
                Cross selectedCross = PlotScantwoDialog.this.crosses[PlotScantwoDialog.this.selectedCrossIndex];
                Set<ScanTwoResult> availableScanTwosSet =
                    selectedCross.getScanTwoResults();

                if(!availableScanTwosSet.isEmpty()) {
                    ScanTwoResult[] availableScanTwos =
                        availableScanTwosSet.toArray(
                                new ScanTwoResult[availableScanTwosSet.size()]);
                    Arrays.sort(availableScanTwos);
                    
                    ScanTwoResult selectedScanTwo = availableScanTwos[PlotScantwoDialog.this.selectedScantwoIndex];
                    
                    // TODO remove this if we add chromosome slection back
                    int numChromosomes = selectedScanTwo.getChromosomeCount();
                    PlotScantwoDialog.this.selectedChrIndexes = new int[numChromosomes];
                    for(int i = 0; i < numChromosomes; i++)
                    {
                        PlotScantwoDialog.this.selectedChrIndexes[i] = i;
                    }
                    
                    List<CrossChromosome> chromosomes = selectedCross.getGenotypeData();
                    GeneticMap[] geneticMaps = new GeneticMap[chromosomes.size()];
                    for(int i = 0; i < geneticMaps.length; i++)
                    {
                        geneticMaps[i] = chromosomes.get(i).getAnyGeneticMap();
                    }
                    ScantwoPlot temp = new ScantwoPlot(
                            selectedScanTwo,
                            geneticMaps,
                            PlotScantwoDialog.this.selectedPhenoIndex,
                            PlotScantwoDialog.this.selectedUpperIndex,
                            PlotScantwoDialog.this.selectedLowerIndex,
                            PlotScantwoDialog.this.selectedChrIndexes,
                            PlotScantwoDialog.this.plotColorScale);

                    String title = "Plot pair scan result for: " + selectedScanTwo.toString();
                    QTL.getInstance().getDesktop().createInternalFrame(
                            new OneDimensionPlotContainerPanel(temp),
                            title,
                            null,
                            selectedScanTwo.getAccessorExpressionString());
                    
                    // parameters for making R command
                    String lodcolumn = "";
                    if (PlotScantwoDialog.this.selectedPhenoIndex > 0)
                        lodcolumn = ", lodcolumn=" + (PlotScantwoDialog.this.selectedPhenoIndex + 1);
                    String upper = "";
                    if (PlotScantwoDialog.this.selectedUpperIndex != PlotScantwoDialog.this.DEFAULT_UPPER_INDEX)
                        upper = ", upper=" +
                        RUtilities.javaStringToRString(LOD_TYPE[PlotScantwoDialog.this.selectedUpperIndex]);
                    String lower = "";
                    if (PlotScantwoDialog.this.selectedLowerIndex != PlotScantwoDialog.this.DEFAULT_LOWER_INDEX)
                        lower = ", lower=" +
                        RUtilities.javaStringToRString(LOD_TYPE[PlotScantwoDialog.this.selectedLowerIndex]);
                    String rcmd = "plot(" + selectedScanTwo.getAccessorExpressionString() + lodcolumn + upper + lower + ")";
                    String comment = "Plot pair scan result ...";
                    
                    RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
                    synchronized(rInterface)
                    {
                        rInterface.insertComment(comment);
                        rInterface.insertComment(rcmd);
                    }
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
        CSH.setHelpIDString(helpButton, "Two_QTL_Scan_Plot");
        helpButton.addActionListener(new CSH.DisplayHelpFromSource(hs, "javax.help.SecondaryWindow", null));

        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.add(helpButton);

        return buttonPane;
    }

    private JPanel makeCrossSelectionPane(ScanTwoResult resultsToPlot) {
        // find out all crosses that has scantwo result
        final JComboBox crossListCombobox = new JComboBox();
        for(Cross currCross: this.crosses)
        {
            crossListCombobox.addItem(currCross.toString());
        }
        
        if(resultsToPlot != null)
        {
            for(int i = 0; i < this.crosses.length; i++)
            {
                if(this.crosses[i].getScanTwoResults().contains(resultsToPlot))
                {
                    crossListCombobox.setSelectedIndex(i);
                }
            }
        }
        
        int defaultHeight = (int)crossListCombobox.getPreferredSize().getHeight();
        crossListCombobox.setPreferredSize(new Dimension(CROSS_COMBOBOX_WIDTH,defaultHeight));
        crossListCombobox.setSelectedIndex(this.selectedCrossIndex);
        crossListCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int oldCrossIndex = PlotScantwoDialog.this.selectedCrossIndex;
                PlotScantwoDialog.this.selectedCrossIndex = crossListCombobox.getSelectedIndex();
                if (oldCrossIndex != PlotScantwoDialog.this.selectedCrossIndex) {
                    PlotScantwoDialog.this.selectedScantwoIndex = 0;
                    PlotScantwoDialog.this.selectedPhenoIndex = 0;
//                    PlotScantwoDialog.this.newChrTable = true;
                    setCurrentPane(makeContentPane(null));
                }
            }
        });

        JPanel result = new JPanel();
        result.add(crossListCombobox);
        result.setBorder(BorderFactory.createTitledBorder(" Choose cross "));

        return result;
    }
}
