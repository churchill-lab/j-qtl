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

import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.action.OpenMgdUrlAction;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarkerPair;
import org.jax.qtl.cross.MarkerPairQtlBasketItem;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.GeneticMarker.MarkerStringFormat;
import org.jax.qtl.fit.gui.AddToQtlBasketDialog;
import org.jax.qtl.gui.ExportDataTableAction;
import org.jax.qtl.scan.ConfidenceThresholdState;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.qtl.scan.ScanTwoSummary;
import org.jax.qtl.scan.ScanTwoSummaryBuilder;
import org.jax.qtl.scan.ScanTwoSummary.ModelToOptimize;
import org.jax.qtl.scan.ScanTwoSummary.ScanTwoSummaryRow;
import org.jax.util.gui.CheckableListTableModel;
import org.jax.util.io.FormattedData;
import org.jax.util.io.JTableDataTable;

/**
 * A panel for displaying scan-one summary results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO document me better!
public class ScanTwoSummaryPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6969434233989354386L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanTwoSummaryPanel.class.getName());
    
    private static final String HELP_ID_STRING = "Two_QTL_Genome_Scan_Summary";
    
    /**
     * Does a refilter whenever one of the spinners is changed
     */
    private final ChangeListener thresholdSpinnerChangeListener =
        new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ScanTwoSummaryPanel.this.refreshSummaryTable();
            }
        };
    
    private final ItemListener thresholdCheckboxListener =
        new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if(ScanTwoSummaryPanel.this.getSelectedThreshold() ==
                   ConfidenceThresholdState.LOD_SCORE_THRESHOLD)
                {
                    ScanTwoSummaryPanel.this.refreshThresholdControlsEnabled();
                    ScanTwoSummaryPanel.this.refreshSummaryTable();
                }
            }
        };
    
    private volatile ScanTwoSummary scanTwoSummary;
    
    private final MouseListener tableMouseListener = new MouseAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent mouseEvent)
        {
            int row = ScanTwoSummaryPanel.this.scanResultsTable.rowAtPoint(new Point(
                    mouseEvent.getX(),
                    mouseEvent.getY()));
            int column = ScanTwoSummaryPanel.this.scanResultsTable.columnAtPoint(new Point(
                    mouseEvent.getX(),
                    mouseEvent.getY()));
            
            if(row >= 0 && column >= 0)
            {
                ScanTwoSummaryCell clickedCell =
                    (ScanTwoSummaryCell)ScanTwoSummaryPanel.this.scanResultsTable.getValueAt(
                            row,
                            column);
                GeneticMarkerPair clickedMarkerPair =
                    clickedCell.getMarkerPairForCell();
                
                if(clickedMarkerPair != null)
                {
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(new OpenMgdUrlAction(
                            clickedMarkerPair.getMarkerOne()));
                    popupMenu.add(new OpenMgdUrlAction(
                            clickedMarkerPair.getMarkerTwo()));
                    popupMenu.show(
                            ScanTwoSummaryPanel.this.scanResultsTable,
                            mouseEvent.getX(),
                            mouseEvent.getY());
                }
            }
        }
    };
    
    SpinnerNumberModel fullAlphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    SpinnerNumberModel fullLodSpinnerModel = new SpinnerNumberModel(
            3.0,    // initial value
            0.0,    // min
            1000.0, // max
            0.5);   // step size
    
    SpinnerNumberModel fullVsOneAlphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    SpinnerNumberModel fullVsOneLodSpinnerModel = new SpinnerNumberModel(
            3.0,    // initial value
            0.0,    // min
            1000.0, // max
            0.5);   // step size
    
    SpinnerNumberModel intAlphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    SpinnerNumberModel intLodSpinnerModel = new SpinnerNumberModel(
            3.0,    // initial value
            0.0,    // min
            1000.0, // max
            0.5);   // step size
    
    SpinnerNumberModel addAlphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    SpinnerNumberModel addLodSpinnerModel = new SpinnerNumberModel(
            3.0,    // initial value
            0.0,    // min
            1000.0, // max
            0.5);   // step size
    
    SpinnerNumberModel addVsOneAlphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    SpinnerNumberModel addVsOneLodSpinnerModel = new SpinnerNumberModel(
            3.0,    // initial value
            0.0,    // min
            1000.0, // max
            0.5);   // step size
    
    /**
     * Constructor
     * @param availableCrosses
     *          the crosses that we can select
     * @param selectedScanTwoResult
     *          the scan to show
     */
    public ScanTwoSummaryPanel(Cross[] availableCrosses, ScanTwoResult selectedScanTwoResult)
    {
        this.initComponents();
        
        this.postGuiInit(availableCrosses, selectedScanTwoResult);
    }
    
    /**
     * take care of all of the initialization that isn't handled by the
     * GUI builder
     * @param availableCrosses
     *          the crosses that we should make available
     * @param selectedScanTwoResult
     *          the scan one that we should select
     */
    private void postGuiInit(Cross[] availableCrosses, ScanTwoResult selectedScanTwoResult)
    {
        for(MarkerStringFormat currFormat: MarkerStringFormat.values())
        {
            this.markerFormatComboBox.addItem(currFormat);
        }
        
        this.scanResultsTable.setModel(new CheckableListTableModel(
                1));
        this.scanResultsTable.addMouseListener(this.tableMouseListener);
        
        this.thresholdTypeComboBox.addItem(ConfidenceThresholdState.NO_THRESHOLD);
        this.thresholdTypeComboBox.addItem(ConfidenceThresholdState.LOD_SCORE_THRESHOLD);
        
        // get all of the scan results
        List<ScanTwoResult> allScanResults = new ArrayList<ScanTwoResult>();
        for(Cross currCross: availableCrosses)
        {
            this.crossComboBox.addItem(currCross);
            allScanResults.addAll(currCross.getScanTwoResults());
        }
        
        // initialize the scan result combo box
        DefaultComboBoxModel scansToSummarizeComboModel =
            (DefaultComboBoxModel)this.scanResultComboBox.getModel();
        scansToSummarizeComboModel.removeAllElements();
        for(ScanTwoResult currScanTwoResult: allScanResults)
        {
            scansToSummarizeComboModel.addElement(currScanTwoResult);
        }
        
        if(selectedScanTwoResult != null)
        {
            this.crossComboBox.setSelectedItem(
                    selectedScanTwoResult.getParentCross());
            this.crossSelectionChanged();
            scansToSummarizeComboModel.setSelectedItem(selectedScanTwoResult);
            this.rebuildSummaryTable();
        }
        
        for(ModelToOptimize modelToOptimize: ModelToOptimize.values())
        {
            this.modelToOptimizeComboBox.addItem(modelToOptimize);
        }
        
        this.fullAlphaSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.fullLodSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.fullVsOneAlphaSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.fullVsOneLodSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.intAlphaSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.intLodSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.addAlphaSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.addLodSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.addVsOneAlphaSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        this.addVsOneLodSpinnerModel.addChangeListener(this.thresholdSpinnerChangeListener);
        
        this.fullThresholdCheckbox.addItemListener(this.thresholdCheckboxListener);
        this.fullVsOneCheckbox.addItemListener(this.thresholdCheckboxListener);
        this.interactionCheckbox.addItemListener(this.thresholdCheckboxListener);
        this.additiveCheckBox.addItemListener(this.thresholdCheckboxListener);
        this.addVsOneCheckbox.addItemListener(this.thresholdCheckboxListener);
        
        // create the export table action
        this.exportTableButton.setAction(new ExportDataTableAction(
                new JTableDataTable(this.scanResultsTable)));
        
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
     * Rebuild the scan two summary object
     */
    private void buildScanTwoSummary()
    {
        ScanTwoResult selectedResult = this.getSelectedScanTwoResult();
        ConfidenceThresholdState selectedThreshold = this.getSelectedThreshold();
        ModelToOptimize modelToOptimize = this.getSelectedModelToOptimize();
        
        if(selectedResult != null && selectedThreshold != null && modelToOptimize != null)
        {
            double[] currentThresholdValues = this.getThresholdValues(
                    selectedThreshold);
            
            ScanTwoSummaryBuilder scanTwoSummaryBuilder = new ScanTwoSummaryBuilder(
                    selectedResult,
                    selectedThreshold,
                    currentThresholdValues,
                    modelToOptimize,
                    this.scanPhenotypeComboBox.getSelectedIndex(),
                    this.getShowPValues());
            this.scanTwoSummary = scanTwoSummaryBuilder.createSummary();
        }
        else
        {
            this.scanTwoSummary = null;
        }
    }

    private boolean getShowPValues()
    {
        ScanTwoResult selectedResult = this.getSelectedScanTwoResult();
        ModelToOptimize modelToOptimize = this.getSelectedModelToOptimize();
        if(selectedResult != null && modelToOptimize != null)
        {
            return modelToOptimize == ModelToOptimize.BEST &&
                   selectedResult.getPermutationsWereCalculated();
        }
        else
        {
            return false;
        }
    }

    private ModelToOptimize getSelectedModelToOptimize()
    {
        return (ModelToOptimize)this.modelToOptimizeComboBox.getSelectedItem();
    }

    /**
     * Rebuild the scan one summary table
     */
    private void rebuildSummaryTable()
    {
        this.buildScanTwoSummary();
        
        this.scanResultsTable.setModel(this.createTableModel());
        Set<GeneticMarkerPair> emptySet = Collections.emptySet();
        this.refreshSummaryTable(
                this.scanTwoSummary,
                emptySet);
    }
    
    /**
     * Refresh the contents of the scantwo summary table
     */
    private void refreshSummaryTable()
    {
        this.buildScanTwoSummary();
        this.refreshSummaryTable(
                this.scanTwoSummary,
                this.getMarkerPairs(true));
    }
    
    /**
     * Refresh the summary table contents
     * @param summary
     *          the summary to use
     */
    private void refreshSummaryTable(
            ScanTwoSummary summary,
            Set<GeneticMarkerPair> markerPairs)
    {
        CheckableListTableModel scanResultsTableModel =
            this.getScanResultsTableModel();
        scanResultsTableModel.setRowCount(0);
        int[] selectableRows = scanResultsTableModel.getCheckableColumnIndices();
        
        if(summary != null)
        {
            ScanTwoSummaryRow[] summaryRows = summary.getScanTwoSummaryRows();
            for(int rowNumber = 0; rowNumber < summaryRows.length; rowNumber++)
            {
                if(selectableRows.length == 1)
                {
                    boolean selected = markerPairs.contains(
                            summaryRows[rowNumber].getMarkerPair());
                    Object[] tableRow = this.createRow(
                            summary,
                            selected,
                            rowNumber);
                    scanResultsTableModel.addRow(tableRow);
                }
                else if(selectableRows.length == 2)
                {
                    boolean fullSelected = markerPairs.contains(
                            summaryRows[rowNumber].getFullMarkerPair());
                    boolean additiveSelected = markerPairs.contains(
                            summaryRows[rowNumber].getAdditiveMarkerPair());
                    Object[] tableRow = this.createRow(
                            summary,
                            fullSelected,
                            additiveSelected,
                            rowNumber);
                    scanResultsTableModel.addRow(tableRow);
                }
                else
                {
                    throw new IllegalStateException(
                            "bad table model. # selectable columns = " +
                            selectableRows.length);
                }
            }
        }
    }

    /**
     * Get the threshold values for the given threshold state
     * @param selectedThreshold
     *          the threshold state
     * @return
     *          the threshold values
     */
    private double[] getThresholdValues(ConfidenceThresholdState selectedThreshold)
    {
        switch(selectedThreshold)
        {
            case NO_THRESHOLD:
            {
                return null;
            }
            
            case ALPHA_THRESHOLD:
            {
                double[] confidenceThresholdValues = new double[5];
                confidenceThresholdValues[0] =
                    this.fullAlphaSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[1] =
                    this.fullVsOneAlphaSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[2] =
                    this.intAlphaSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[3] =
                    this.addAlphaSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[4] =
                    this.addVsOneAlphaSpinnerModel.getNumber().doubleValue();
                return confidenceThresholdValues;
            }
            
            case LOD_SCORE_THRESHOLD:
            {
                double[] confidenceThresholdValues = new double[5];
                confidenceThresholdValues[0] =
                    this.fullThresholdCheckbox.isSelected() ?
                    Double.POSITIVE_INFINITY :
                    this.fullLodSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[1] =
                    this.fullVsOneCheckbox.isSelected() ?
                    Double.POSITIVE_INFINITY :
                    this.fullVsOneLodSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[2] =
                    this.interactionCheckbox.isSelected() ?
                    Double.POSITIVE_INFINITY :
                    this.intLodSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[3] =
                    this.additiveCheckBox.isSelected() ?
                    Double.POSITIVE_INFINITY :
                    this.addLodSpinnerModel.getNumber().doubleValue();
                confidenceThresholdValues[4] =
                    this.addVsOneCheckbox.isSelected() ?
                    Double.POSITIVE_INFINITY :
                    this.addVsOneLodSpinnerModel.getNumber().doubleValue();
                return confidenceThresholdValues;
            }
            
            default:
            {
                LOG.warning("unknown threshold type: " + selectedThreshold);
                return null;
            }
        }
    }

    /**
     * Get the selected threshold state
     * @return
     *          the selected state
     */
    private ConfidenceThresholdState getSelectedThreshold()
    {
        return (ConfidenceThresholdState)this.thresholdTypeComboBox.getSelectedItem();
    }

    /**
     * Get the selected scantwo result
     * @return
     *          the selected result
     */
    private ScanTwoResult getSelectedScanTwoResult()
    {
        return (ScanTwoResult)this.scanResultComboBox.getSelectedItem();
    }

    private CheckableListTableModel createTableModel()
    {
        ScanTwoResult selectedScanResult = this.getSelectedScanTwoResult();
        ModelToOptimize modelToOptimize = this.getSelectedModelToOptimize();
        if(selectedScanResult == null)
        {
            String[] header = new String[] {"No Scan Results Selected"};
            return new CheckableListTableModel(header, 1);
        }
        else if(modelToOptimize == ModelToOptimize.BEST)
        {
            boolean showPValues = this.getShowPValues();
            if(showPValues)
            {
                String[] header = new String[] {
                        "Selected",     "1st Marker Pair",
                        "Full",         "p-value",
                        "Full vs. One", "p-value",
                        "Int.",         "p-value",
                        "Selected",     "2nd Marker Pair",
                        "Add.",         "p-value",
                        "Add. vs. One", "p-value"};
                return new CheckableListTableModel(
                        header,
                        new int[] {0, 8});
            }
            else
            {
                String[] header = new String[] {
                        "Selected",     "1st Marker Pair",
                        "Full",
                        "Full vs. One",
                        "Int.",
                        "Selected",     "2nd Marker Pair",
                        "Add.",
                        "Add. vs. One"};
                return new CheckableListTableModel(
                        header,
                        new int[] {0, 5});
            }
        }
        else
        {
            String[] header = new String[] {
                    "Selected",     "1st Marker Pair",
                    "Full",
                    "Full vs. One",
                    "Int.",
                    "Add.",
                    "Add. vs. One"};
            return new CheckableListTableModel(header, 1);
        }
    }

    /**
     * Create a new table row
     * @param scanTwoSummary
     *          the summary to use for the row
     * @param fullSelected
     *          true iff the full column should be selected
     * @param rowNumber
     *          the row number
     * @return
     *          the row object array
     */
    private Object[] createRow(
            ScanTwoSummary scanTwoSummary,
            boolean fullSelected,
            boolean additiveSelected,
            int rowNumber)
    {
        // TODO add comments for each cell column
        ModelToOptimize modelToOptimize = this.getSelectedModelToOptimize();
        if(modelToOptimize == ModelToOptimize.BEST)
        {
            boolean showPValues = this.getShowPValues();
            if(showPValues)
            {
                return new Object[] {
                        fullSelected,
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                1),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                2),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                3),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                4),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                5),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                6),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                7),
                        additiveSelected,
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                9),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                10),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                11),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                12),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                13)};
            }
            else
            {
                return new Object[] {
                        fullSelected,
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                1),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                2),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                3),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                4),
                        additiveSelected,
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                6),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                7),
                        new ScanTwoSummaryCell(
                                scanTwoSummary,
                                rowNumber,
                                8)};
            }
        }
        else
        {
            return new Object[] {
                    fullSelected,
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            1),
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            2),
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            3),
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            4),
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            5),
                    new ScanTwoSummaryCell(
                            scanTwoSummary,
                            rowNumber,
                            6)};
//                            6),
//                    new ScanTwoSummaryCell(
//                            scanTwoSummary,
//                            rowNumber,
//                            7)};
        }
    }
    
    /**
     * Create a new table row
     * @param scanTwoSummary
     *          the summary to use for the row
     * @param selected
     *          true iff the row should be selected
     * @param rowNumber
     *          the row number
     * @return
     *          the row object array
     */
    private Object[] createRow(
            ScanTwoSummary scanTwoSummary,
            boolean selected,
            int rowNumber)
    {
        return this.createRow(
                scanTwoSummary,
                selected,
                false,
                rowNumber);
    }
    
    /**
     * Wraps up significance values for presentation in a
     * {@link javax.swing.JTable}
     */
    private class ScanTwoSummaryCell implements FormattedData
    {
        private final ScanTwoSummary scanTwoSummary;
        
        private final int columnNumber;

        private final int rowNumber;
        
        /**
         * Constructor
         * @param scanTwoSummary
         *          the summary that we're showing
         * @param rowNumber
         *          the row number for this cell
         * @param columnNumber
         *          the column number for this cell
         */
        public ScanTwoSummaryCell(
                ScanTwoSummary scanTwoSummary,
                int rowNumber,
                int columnNumber)
        {
            this.scanTwoSummary = scanTwoSummary;
            this.rowNumber = rowNumber;
            this.columnNumber = columnNumber;
        }
        
        /**
         * Determines if we should show a hyperlink for this cell
         * @return
         *          true iff we should show a hyperlink
         */
        public GeneticMarkerPair getMarkerPairForCell()
        {
            ScanTwoSummaryRow summaryRow =
                this.scanTwoSummary.getScanTwoSummaryRows()[this.rowNumber];
            ModelToOptimize modelToOptimize = ScanTwoSummaryPanel.this.getSelectedModelToOptimize();
            if(modelToOptimize == ModelToOptimize.BEST)
            {
                boolean showPValues = ScanTwoSummaryPanel.this.getShowPValues();
                if(showPValues)
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return summaryRow.getFullMarkerPair();
                        }
                        
                        case 9:
                        {
                            return summaryRow.getAdditiveMarkerPair();
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
                else
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return summaryRow.getFullMarkerPair();
                        }
                        
                        case 6:
                        {
                            return summaryRow.getAdditiveMarkerPair();
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
            }
            else
            {
                switch(this.columnNumber)
                {
                    case 1:
                    {
                        return summaryRow.getMarkerPair();
                    }
                    
//                    case 5:
//                    {
//                        return summaryRow.getAdditiveMarkerPair();
//                    }
                    
                    default:
                    {
                        return null;
                    }
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public String toFormattedString()
        {
            ScanTwoSummaryRow summaryRow =
                this.scanTwoSummary.getScanTwoSummaryRows()[this.rowNumber];
            ModelToOptimize modelToOptimize = ScanTwoSummaryPanel.this.getSelectedModelToOptimize();
            if(modelToOptimize == ModelToOptimize.BEST)
            {
                boolean showPValues = ScanTwoSummaryPanel.this.getShowPValues();
                if(showPValues)
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return this.toHyperlinkString(summaryRow.getFullMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
                        }
                        
                        case 2:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullLodScore());
                        }
                        
                        case 3:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullPValue());
                        }
                        
                        case 4:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOneLodScore());
                        }
                        
                        case 5:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOnePValue());
                        }
                        
                        case 6:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractiveLodScore());
                        }
                        
                        case 7:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractivePValue());
                        }
                        
                        case 9:
                        {
                            return this.toHyperlinkString(summaryRow.getAdditiveMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
                        }
                        
                        case 10:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveLodScore());
                        }
                        
                        case 11:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditivePValue());
                        }
                        
                        case 12:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOneLodScore());
                        }
                        
                        case 13:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOnePValue());
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
                else
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return this.toHyperlinkString(summaryRow.getFullMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
                        }
                        
                        case 2:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullLodScore());
                        }
                        
                        case 3:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOneLodScore());
                        }
                        
                        case 4:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractiveLodScore());
                        }
                        
                        case 6:
                        {
                            return this.toHyperlinkString(summaryRow.getAdditiveMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
                        }
                        
                        case 7:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveLodScore());
                        }
                        
                        case 8:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOneLodScore());
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
            }
            else
            {
                switch(this.columnNumber)
                {
                    case 1:
                    {
                        return this.toHyperlinkString(summaryRow.getFullMarkerPair().toString(
                                ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
                    }
                    
                    case 2:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getFullLodScore());
                    }
                    
                    case 3:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getFullVsOneLodScore());
                    }
                    
                    case 4:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getInteractiveLodScore());
                    }
                    
//                    case 5:
//                    {
//                        return this.toHyperlinkString(summaryRow.getAdditiveMarkerPair().toString(
//                                ScanTwoSummaryPanel.this.getSelectedMarkerFormat()));
//                    }
                    
//                    case 6:
                    case 5:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getAdditiveLodScore());
                    }
                    
//                    case 7:
                    case 6:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getAdditiveVsOneLodScore());
                    }
                    
                    default:
                    {
                        return null;
                    }
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public String toUnformattedString()
        {
            ScanTwoSummaryRow summaryRow =
                this.scanTwoSummary.getScanTwoSummaryRows()[this.rowNumber];
            ModelToOptimize modelToOptimize = ScanTwoSummaryPanel.this.getSelectedModelToOptimize();
            if(modelToOptimize == ModelToOptimize.BEST)
            {
                boolean showPValues = ScanTwoSummaryPanel.this.getShowPValues();
                if(showPValues)
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return summaryRow.getFullMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
                        }
                        
                        case 2:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullLodScore());
                        }
                        
                        case 3:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullPValue());
                        }
                        
                        case 4:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOneLodScore());
                        }
                        
                        case 5:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOnePValue());
                        }
                        
                        case 6:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractiveLodScore());
                        }
                        
                        case 7:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractivePValue());
                        }
                        
                        case 9:
                        {
                            return summaryRow.getAdditiveMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
                        }
                        
                        case 10:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveLodScore());
                        }
                        
                        case 11:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditivePValue());
                        }
                        
                        case 12:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOneLodScore());
                        }
                        
                        case 13:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOnePValue());
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
                else
                {
                    switch(this.columnNumber)
                    {
                        case 1:
                        {
                            return summaryRow.getFullMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
                        }
                        
                        case 2:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullLodScore());
                        }
                        
                        case 3:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getFullVsOneLodScore());
                        }
                        
                        case 4:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getInteractiveLodScore());
                        }
                        
                        case 6:
                        {
                            return summaryRow.getAdditiveMarkerPair().toString(
                                    ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
                        }
                        
                        case 7:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveLodScore());
                        }
                        
                        case 8:
                        {
                            return Constants.THREE_DIGIT_FORMATTER.format(
                                    summaryRow.getAdditiveVsOneLodScore());
                        }
                        
                        default:
                        {
                            return null;
                        }
                    }
                }
            }
            else
            {
                switch(this.columnNumber)
                {
                    case 1:
                    {
                        return summaryRow.getFullMarkerPair().toString(
                                ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
                    }
                    
                    case 2:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getFullLodScore());
                    }
                    
                    case 3:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getFullVsOneLodScore());
                    }
                    
                    case 4:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getInteractiveLodScore());
                    }
                    
//                    case 5:
//                    {
//                        return summaryRow.getAdditiveMarkerPair().toString(
//                                ScanTwoSummaryPanel.this.getSelectedMarkerFormat());
//                    }
                    
//                    case 6:
                    case 5:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getAdditiveLodScore());
                    }
                    
//                    case 7:
                    case 6:
                    {
                        return Constants.THREE_DIGIT_FORMATTER.format(
                                summaryRow.getAdditiveVsOneLodScore());
                    }
                    
                    default:
                    {
                        return null;
                    }
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.toFormattedString();
        }
        
        private String toHyperlinkString(String plainString)
        {
            return "<html><u><font color=\"blue\">" +
                   plainString +
                   "</font></u></html>";
        }

        /**
         * The the summary row for this cell
         * @return
         *          the summary row
         */
        private ScanTwoSummaryRow getScanTwoSummaryRow()
        {
            return this.scanTwoSummary.getScanTwoSummaryRows()[this.rowNumber];
        }
    }
    
    /**
     * A helper method that provides a typecast version of the table model
     * @return
     *          the table model
     */
    private CheckableListTableModel getScanResultsTableModel()
    {
        return (CheckableListTableModel)this.scanResultsTable.getModel();
    }

    /**
     * Set all selection states to the given value
     * @param selectedState
     *          the selection state value to use for all rows
     */
    private void setAllMarkersSelectionStateTo(boolean selectedState)
    {
        Boolean selectedStateBoolean = Boolean.valueOf(selectedState);
        CheckableListTableModel summaryTableModel = this.getScanResultsTableModel();
        int[] checkableColumnIndecies = summaryTableModel.getCheckableColumnIndices();
        int rowCount =
            summaryTableModel.getRowCount();
        for(int rowIndex = 0; rowIndex < rowCount; rowIndex++)
        {
            for(int checkableColumnIndex: checkableColumnIndecies)
            {
                summaryTableModel.setValueAt(
                        selectedStateBoolean,
                        rowIndex,
                        checkableColumnIndex);
            }
        }
    }
    
    private Set<GeneticMarkerPair> getMarkerPairs(boolean selected)
    {
        HashSet<GeneticMarkerPair> markerPairs =
            new HashSet<GeneticMarkerPair>();
        
        CheckableListTableModel tableModel = this.getScanResultsTableModel();
        int[] selectionColumns = tableModel.getCheckableColumnIndices();
        if(selectionColumns.length == 1)
        {
            int numRows = tableModel.getRowCount();
            for(int rowIndex = 0; rowIndex < numRows; rowIndex++)
            {
                Boolean selectionCell = (Boolean)tableModel.getValueAt(
                        rowIndex,
                        selectionColumns[0]);
                
                if(selectionCell.booleanValue() == selected)
                {
                    ScanTwoSummaryRow summaryRow =
                        this.getScanTwoSummaryRowAtTableRow(rowIndex);
                    markerPairs.add(summaryRow.getMarkerPair());
                }
            }
        }
        else if(selectionColumns.length == 2)
        {
            int numRows = tableModel.getRowCount();
            for(int rowIndex = 0; rowIndex < numRows; rowIndex++)
            {
                Boolean fullSelectionCell = (Boolean)tableModel.getValueAt(
                        rowIndex,
                        selectionColumns[0]);
                if(fullSelectionCell.booleanValue() == selected)
                {
                    ScanTwoSummaryRow summaryRow =
                        this.getScanTwoSummaryRowAtTableRow(rowIndex);
                    markerPairs.add(summaryRow.getFullMarkerPair());
                }
                
                Boolean addSelectionCell = (Boolean)tableModel.getValueAt(
                        rowIndex,
                        selectionColumns[1]);
                if(addSelectionCell.booleanValue() == selected)
                {
                    ScanTwoSummaryRow summaryRow =
                        this.getScanTwoSummaryRowAtTableRow(rowIndex);
                    markerPairs.add(summaryRow.getAdditiveMarkerPair());
                }
            }
        }
        else
        {
            throw new IllegalStateException(
                    "Unexpected number of checkable columns: " +
                    selectionColumns.length);
        }
        
        return markerPairs;
    }
    
    /**
     * Get the scan one summary row at the given table row
     * @param tableRow
     *          the table row number
     * @return
     *          the scan one summary row
     */
    private ScanTwoSummaryRow getScanTwoSummaryRowAtTableRow(int tableRow)
    {
        ScanTwoSummaryCell cell = (ScanTwoSummaryCell)this.scanResultsTable.getValueAt(
                tableRow,
                1);
        return cell.getScanTwoSummaryRow();
    }
    
    /**
     * Respond to a change in the selected cross
     */
    private void crossSelectionChanged()
    {
        Cross selectedCross = this.getSelectedCross();
        this.scanResultComboBox.removeAllItems();
        if(selectedCross != null)
        {
            Set<ScanTwoResult> scanResults =
                selectedCross.getScanTwoResults();
            for(ScanTwoResult currScanTwoResult: scanResults)
            {
                this.scanResultComboBox.addItem(currScanTwoResult);
            }
        }
    }

    /**
     * Getter for the selected cross
     * @return
     *          the selected cross
     */
    private Cross getSelectedCross()
    {
        return (Cross)this.crossComboBox.getSelectedItem();
    }
    
    /**
     * Respond to a change in confidence threshold
     */
    private void confidenceThresholdStateChanged()
    {
        ConfidenceThresholdState selectedConfidenceThresholdState =
            this.getSelectedThreshold();
        switch(selectedConfidenceThresholdState)
        {
            case NO_THRESHOLD:
            {
                this.fullThresholdSpinner.setModel(new SpinnerNumberModel());
                this.fullVsOneSpinner.setModel(new SpinnerNumberModel());
                this.interactionSpinner.setModel(new SpinnerNumberModel());
                this.additiveSpinner.setModel(new SpinnerNumberModel());
                this.addVsOneSpinner.setModel(new SpinnerNumberModel());
                
                break;
            }
            
            case LOD_SCORE_THRESHOLD:
            {
                this.fullThresholdSpinner.setModel(this.fullLodSpinnerModel);
                this.fullVsOneSpinner.setModel(this.fullVsOneLodSpinnerModel);
                this.interactionSpinner.setModel(this.intLodSpinnerModel);
                this.additiveSpinner.setModel(this.addLodSpinnerModel);
                this.addVsOneSpinner.setModel(this.addVsOneLodSpinnerModel);
                
                break;
            }
            
            case ALPHA_THRESHOLD:
            {
                this.fullThresholdSpinner.setModel(this.fullAlphaSpinnerModel);
                this.fullVsOneSpinner.setModel(this.fullVsOneAlphaSpinnerModel);
                this.interactionSpinner.setModel(this.intAlphaSpinnerModel);
                this.additiveSpinner.setModel(this.addAlphaSpinnerModel);
                this.addVsOneSpinner.setModel(this.addVsOneAlphaSpinnerModel);
                
                break;
            }
            
            default:
            {
                LOG.warning(
                        "unknown threshold state: " +
                        selectedConfidenceThresholdState);
                break;
            }
        }
        
        this.refreshThresholdControlsEnabled();
        this.refreshSummaryTable();
    }
    
    private void refreshThresholdControlsEnabled()
    {
        // refresh all 5 threshold component groups
        this.refreshThresholdGroupEnabled(
                this.fullThresholdLabel,
                this.fullThresholdSpinner,
                this.fullThresholdCheckbox);
        this.refreshThresholdGroupEnabled(
                this.fullVsOneLabel,
                this.fullVsOneSpinner,
                this.fullVsOneCheckbox);
        this.refreshThresholdGroupEnabled(
                this.interactionLabel,
                this.interactionSpinner,
                this.interactionCheckbox);
        this.refreshThresholdGroupEnabled(
                this.additiveLabel,
                this.additiveSpinner,
                this.additiveCheckBox);
        this.refreshThresholdGroupEnabled(
                this.addVsOneLabel,
                this.addVsOneSpinner,
                this.addVsOneCheckbox);
    }
    
    /**
     * Refresh the given threshold widgets
     * @param thresholdLabel
     *          the label
     * @param thresholdSpinner
     *          the spinner
     * @param thresholdInfinityCheckbox
     *          the infinity checkbox
     */
    private void refreshThresholdGroupEnabled(
            JLabel thresholdLabel,
            JSpinner thresholdSpinner,
            JCheckBox thresholdInfinityCheckbox)
    {
        ConfidenceThresholdState selectedConfidenceThresholdState =
            this.getSelectedThreshold();
        boolean alphaThresholdSelected =
            selectedConfidenceThresholdState == ConfidenceThresholdState.ALPHA_THRESHOLD;
        boolean lodThresholdSelected =
            selectedConfidenceThresholdState == ConfidenceThresholdState.LOD_SCORE_THRESHOLD;
        
        thresholdLabel.setEnabled(alphaThresholdSelected || lodThresholdSelected);
        thresholdSpinner.setEnabled(
                alphaThresholdSelected ||
                (lodThresholdSelected && !thresholdInfinityCheckbox.isSelected()));
        thresholdInfinityCheckbox.setEnabled(
                lodThresholdSelected);
        if(alphaThresholdSelected)
        {
            thresholdInfinityCheckbox.setSelected(false);
        }
    }
    
    /**
     * Respond to a change in the selected scan result
     */
    private void selectedScanResultChanged()
    {
        ScanTwoResult selectedScanResult = this.getSelectedScanTwoResult();
        if(selectedScanResult != null)
        {
            if(selectedScanResult.getPermutationsWereCalculated())
            {
                boolean alphaAlreadyInList = false;
                int itemCount = this.thresholdTypeComboBox.getItemCount();
                for(int i = 0; i < itemCount; i++)
                {
                    if(this.thresholdTypeComboBox.getItemAt(i) == ConfidenceThresholdState.ALPHA_THRESHOLD)
                    {
                        alphaAlreadyInList = true;
                        break;
                    }
                }
                
                if(!alphaAlreadyInList)
                {
                    this.thresholdTypeComboBox.addItem(
                            ConfidenceThresholdState.ALPHA_THRESHOLD);
                }
            }
            else
            {
                this.thresholdTypeComboBox.removeItem(
                        ConfidenceThresholdState.ALPHA_THRESHOLD);
            }
        }
        
        this.updatePhenotypeList();
    }
    
    private void updatePhenotypeList()
    {
        this.scanPhenotypeComboBox.removeAllItems();
        
        ScanTwoResult selectedScanResult = this.getSelectedScanTwoResult();
        if(selectedScanResult != null)
        {
            String[] scannedPhenotypes = selectedScanResult.getScannedPhenotypeNames();
            for(String phenotypeName: scannedPhenotypes)
            {
                this.scanPhenotypeComboBox.addItem(phenotypeName);
            }
        }
    }
    
    private void selectedPhenotypeChanged()
    {
        this.rebuildSummaryTable();
    }

    /**
     * Getter for the currently selected marker format
     * @return
     *          the selected format
     */
    private MarkerStringFormat getSelectedMarkerFormat()
    {
        return (MarkerStringFormat)this.markerFormatComboBox.getSelectedItem();
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

        scanResultLabel = new javax.swing.JLabel();
        crossComboBox = new javax.swing.JComboBox();
        scanResultComboBox = new javax.swing.JComboBox();
        scanPhenotypeComboBox = new javax.swing.JComboBox();
        modelToOptimizeLabel = new javax.swing.JLabel();
        modelToOptimizeComboBox = new javax.swing.JComboBox();
        thresholdTypeLabel = new javax.swing.JLabel();
        thresholdTypeComboBox = new javax.swing.JComboBox();
        scanResultsLabel = new javax.swing.JLabel();
        scanResultsScrollPane = new javax.swing.JScrollPane();
        scanResultsTable = new javax.swing.JTable();
        toggleSelectAllButton = new javax.swing.JButton();
        addMarkersToBasketButton = new javax.swing.JButton();
        fullThresholdLabel = new javax.swing.JLabel();
        fullThresholdCheckbox = new javax.swing.JCheckBox();
        fullThresholdSpinner = new javax.swing.JSpinner();
        fullVsOneLabel = new javax.swing.JLabel();
        fullVsOneCheckbox = new javax.swing.JCheckBox();
        fullVsOneSpinner = new javax.swing.JSpinner();
        interactionLabel = new javax.swing.JLabel();
        interactionCheckbox = new javax.swing.JCheckBox();
        interactionSpinner = new javax.swing.JSpinner();
        additiveLabel = new javax.swing.JLabel();
        additiveCheckBox = new javax.swing.JCheckBox();
        additiveSpinner = new javax.swing.JSpinner();
        addVsOneLabel = new javax.swing.JLabel();
        addVsOneCheckbox = new javax.swing.JCheckBox();
        addVsOneSpinner = new javax.swing.JSpinner();
        exportTableButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        markerFormatComboBox = new javax.swing.JComboBox();

        scanResultLabel.setText("Two QTL Scan:");

        crossComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                crossComboBoxItemStateChanged(evt);
            }
        });

        scanResultComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scanResultComboBoxItemStateChanged(evt);
            }
        });

        scanPhenotypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scanPhenotypeComboBoxItemStateChanged(evt);
            }
        });

        modelToOptimizeLabel.setText("Model to Optimize:");

        modelToOptimizeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                modelToOptimizeComboBoxItemStateChanged(evt);
            }
        });

        thresholdTypeLabel.setText("Threshold Type:");

        thresholdTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                thresholdTypeComboBoxItemStateChanged(evt);
            }
        });

        scanResultsLabel.setText("Chromosome Pair Peaks:");

        scanResultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        scanResultsScrollPane.setViewportView(scanResultsTable);

        toggleSelectAllButton.setText("Toggle Select All Displayed");
        toggleSelectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleSelectAllButtonActionPerformed(evt);
            }
        });

        addMarkersToBasketButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/add-16x16.png"))); // NOI18N
        addMarkersToBasketButton.setText("Add Selected Markers to QTL Basket...");
        addMarkersToBasketButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMarkersToBasketButtonActionPerformed(evt);
            }
        });

        fullThresholdLabel.setText("Full:");

        fullThresholdCheckbox.setText("Infinity");

        fullVsOneLabel.setText("Conditional-Interactive (Full vs. One):");

        fullVsOneCheckbox.setText("Infinity");

        interactionLabel.setText("Interaction (Int.):");

        interactionCheckbox.setText("Infinity");

        additiveLabel.setText("Additive (Add.):");

        additiveCheckBox.setText("Infinity");

        addVsOneLabel.setText("Conditional-Additive (Add. vs. One):");

        addVsOneCheckbox.setText("Infinity");

        exportTableButton.setText("Export Table ...");

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help ...");

        markerFormatComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                markerFormatComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scanResultsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(toggleSelectAllButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addMarkersToBasketButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportTableButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(helpButton))
                    .add(layout.createSequentialGroup()
                        .add(scanResultLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scanResultComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scanPhenotypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(thresholdTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(thresholdTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fullThresholdLabel)
                            .add(fullVsOneLabel)
                            .add(interactionLabel)
                            .add(additiveLabel)
                            .add(addVsOneLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fullVsOneSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fullThresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(interactionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(additiveSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(addVsOneSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(addVsOneCheckbox)
                            .add(fullVsOneCheckbox)
                            .add(fullThresholdCheckbox)
                            .add(interactionCheckbox)
                            .add(additiveCheckBox)))
                    .add(layout.createSequentialGroup()
                        .add(scanResultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(modelToOptimizeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(modelToOptimizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(scanResultLabel)
                    .add(crossComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(scanResultComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(scanPhenotypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modelToOptimizeLabel)
                    .add(modelToOptimizeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(thresholdTypeLabel)
                    .add(thresholdTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fullThresholdLabel)
                    .add(fullThresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fullThresholdCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fullVsOneLabel)
                    .add(fullVsOneSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fullVsOneCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(interactionLabel)
                    .add(interactionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(interactionCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additiveLabel)
                    .add(additiveSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(additiveCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addVsOneLabel)
                    .add(addVsOneSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addVsOneCheckbox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(scanResultsLabel)
                    .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scanResultsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(toggleSelectAllButton)
                    .add(addMarkersToBasketButton)
                    .add(exportTableButton)
                    .add(helpButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMarkersToBasketButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMarkersToBasketButtonActionPerformed
        Set<GeneticMarkerPair> selectedMarkers = this.getMarkerPairs(true);
        List<QtlBasketItem> qtlBasketItems = new ArrayList<QtlBasketItem>();
        for(GeneticMarkerPair currMarkerPair: selectedMarkers)
        {
            qtlBasketItems.add(new MarkerPairQtlBasketItem(
                    currMarkerPair,
                    ""));
        }
        
        final AddToQtlBasketDialog addToBasketDialog = new AddToQtlBasketDialog(
                QTL.getInstance().getApplicationFrame(),
                this.getSelectedCross(),
                qtlBasketItems);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                addToBasketDialog.setVisible(true);
            }
        });
    }//GEN-LAST:event_addMarkersToBasketButtonActionPerformed

    private void scanResultComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scanResultComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.selectedScanResultChanged();
        }
        else if(this.scanResultComboBox.getItemCount() == 0)
        {
            this.selectedScanResultChanged();
        }
    }//GEN-LAST:event_scanResultComboBoxItemStateChanged

    private void toggleSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleSelectAllButtonActionPerformed
        // if anything is unchecked, we check them all, else if everything
        // is checked we uncheck them all
        Set<GeneticMarkerPair> uncheckedMarkers =
            this.getMarkerPairs(false);
        this.setAllMarkersSelectionStateTo(!uncheckedMarkers.isEmpty());
    }//GEN-LAST:event_toggleSelectAllButtonActionPerformed

    private void crossComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crossComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.crossSelectionChanged();
        }
    }//GEN-LAST:event_crossComboBoxItemStateChanged

    private void thresholdTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_thresholdTypeComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.confidenceThresholdStateChanged();
        }
}//GEN-LAST:event_thresholdTypeComboBoxItemStateChanged

    private void modelToOptimizeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_modelToOptimizeComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.rebuildSummaryTable();
        }
    }//GEN-LAST:event_modelToOptimizeComboBoxItemStateChanged

    private void markerFormatComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_markerFormatComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.scanResultsTable.repaint();
        }
    }//GEN-LAST:event_markerFormatComboBoxItemStateChanged

    private void scanPhenotypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scanPhenotypeComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.selectedPhenotypeChanged();
        }
        else if(this.scanPhenotypeComboBox.getItemCount() == 0)
        {
            this.selectedPhenotypeChanged();
        }
    }//GEN-LAST:event_scanPhenotypeComboBoxItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkersToBasketButton;
    private javax.swing.JCheckBox addVsOneCheckbox;
    private javax.swing.JLabel addVsOneLabel;
    private javax.swing.JSpinner addVsOneSpinner;
    private javax.swing.JCheckBox additiveCheckBox;
    private javax.swing.JLabel additiveLabel;
    private javax.swing.JSpinner additiveSpinner;
    private javax.swing.JComboBox crossComboBox;
    private javax.swing.JButton exportTableButton;
    private javax.swing.JCheckBox fullThresholdCheckbox;
    private javax.swing.JLabel fullThresholdLabel;
    private javax.swing.JSpinner fullThresholdSpinner;
    private javax.swing.JCheckBox fullVsOneCheckbox;
    private javax.swing.JLabel fullVsOneLabel;
    private javax.swing.JSpinner fullVsOneSpinner;
    private javax.swing.JButton helpButton;
    private javax.swing.JCheckBox interactionCheckbox;
    private javax.swing.JLabel interactionLabel;
    private javax.swing.JSpinner interactionSpinner;
    private javax.swing.JComboBox markerFormatComboBox;
    private javax.swing.JComboBox modelToOptimizeComboBox;
    private javax.swing.JLabel modelToOptimizeLabel;
    private javax.swing.JComboBox scanPhenotypeComboBox;
    private javax.swing.JComboBox scanResultComboBox;
    private javax.swing.JLabel scanResultLabel;
    private javax.swing.JLabel scanResultsLabel;
    private javax.swing.JScrollPane scanResultsScrollPane;
    private javax.swing.JTable scanResultsTable;
    private javax.swing.JComboBox thresholdTypeComboBox;
    private javax.swing.JLabel thresholdTypeLabel;
    private javax.swing.JButton toggleSelectAllButton;
    // End of variables declaration//GEN-END:variables
    
}
