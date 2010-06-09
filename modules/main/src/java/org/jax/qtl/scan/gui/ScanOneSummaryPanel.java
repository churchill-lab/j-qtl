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
import javax.swing.JPopupMenu;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.action.OpenMgdUrlAction;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.SingleMarkerQtlBasketItem;
import org.jax.qtl.cross.GeneticMarker.MarkerStringFormat;
import org.jax.qtl.fit.gui.AddToQtlBasketDialog;
import org.jax.qtl.gui.ExportDataTableAction;
import org.jax.qtl.scan.ConfidenceThresholdState;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.ScanOneSummary;
import org.jax.qtl.scan.ScanOneSummaryBuilder;
import org.jax.qtl.scan.ScanOneSummary.ScanOneSummaryRow;
import org.jax.util.gui.CheckableListTableModel;
import org.jax.util.io.FormattedData;
import org.jax.util.io.JTableDataTable;

/**
 * A panel for displaying scan-one summary results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneSummaryPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6969434233989354386L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneSummaryPanel.class.getName());

    private static final String HELP_ID_STRING = "One_QTL_Genome_Scan_Summary";
    
    /**
     * Does a refilter whenever one of the spinners is changed
     */
    private final ChangeListener spinnerChangeListener =
        new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                ScanOneSummaryPanel.this.refreshSummaryTable();
            }
        };
    
    private volatile ScanOneSummary scanOneSummary;
    
    private final MouseListener tableMouseListener = new MouseAdapter()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(MouseEvent mouseEvent)
        {
            int row = ScanOneSummaryPanel.this.scanResultsTable.rowAtPoint(new Point(
                    mouseEvent.getX(),
                    mouseEvent.getY()));
            int column = ScanOneSummaryPanel.this.scanResultsTable.columnAtPoint(new Point(
                    mouseEvent.getX(),
                    mouseEvent.getY()));
            
            if(row >= 0 && column == 1)
            {
                ScanOneSummaryCell clickedCell =
                    (ScanOneSummaryCell)ScanOneSummaryPanel.this.scanResultsTable.getValueAt(
                            row,
                            column);
                GeneticMarker clickedMarker =
                    clickedCell.getScanOneSummaryRow().getMarker();
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(new OpenMgdUrlAction(clickedMarker));
                popupMenu.show(
                        ScanOneSummaryPanel.this.scanResultsTable,
                        mouseEvent.getX(),
                        mouseEvent.getY());
            }
        }
    };
    
    /**
     * The spinner model to use when we're using an alpha threshold
     */
    SpinnerNumberModel alphaSpinnerModel = new SpinnerNumberModel(
            0.1,    // initial value
            0.0,    // min
            1.0,    // max
            0.05);  // step size
    
    /**
     * The spinner model to use when we're using a LOD threshold
     */
    SpinnerNumberModel lodSpinnerModel = new SpinnerNumberModel(
            3.0,                // initial value
            0.0,                // min
            1000.0,             // max
            0.5);               // step size
    
    /**
     * Constructor
     * @param availableCrosses
     *          the crosses that we can select
     * @param selectedScanOneResult
     *          the scan to show
     */
    public ScanOneSummaryPanel(Cross[] availableCrosses, ScanOneResult selectedScanOneResult)
    {
        this.initComponents();
        
        this.postGuiInit(availableCrosses, selectedScanOneResult);
    }
    
    /**
     * take care of all of the initialization that isn't handled by the
     * GUI builder
     * @param availableCrosses
     *          the crosses that we should make available
     * @param selectedScanOneResult
     *          the scan one that we should select
     */
    private void postGuiInit(Cross[] availableCrosses, ScanOneResult selectedScanOneResult)
    {
        for(MarkerStringFormat currFormat: MarkerStringFormat.values())
        {
            this.markerFormatComboBox.addItem(currFormat);
        }
        
        this.scanResultsTable.setModel(new CheckableListTableModel(
                1));
        this.scanResultsTable.addMouseListener(this.tableMouseListener);
        
        this.thresholdComboBox.addItem(ConfidenceThresholdState.NO_THRESHOLD);
        this.thresholdComboBox.addItem(ConfidenceThresholdState.LOD_SCORE_THRESHOLD);
        
        // get all of the scan results
        List<ScanOneResult> allScanResults = new ArrayList<ScanOneResult>();
        for(Cross currCross: availableCrosses)
        {
            this.crossComboBox.addItem(currCross);
            allScanResults.addAll(currCross.getScanOneResults());
        }
        
        // initialize the scan result combo box
        DefaultComboBoxModel scansToSummarizeComboModel =
            (DefaultComboBoxModel)this.scanResultComboBox.getModel();
        scansToSummarizeComboModel.removeAllElements();
        for(ScanOneResult currScanOneResult: allScanResults)
        {
            scansToSummarizeComboModel.addElement(currScanOneResult);
        }
        
        if(selectedScanOneResult != null)
        {
            this.crossComboBox.setSelectedItem(
                    selectedScanOneResult.getParentCross());
            this.crossSelectionChanged();
            scansToSummarizeComboModel.setSelectedItem(selectedScanOneResult);
            this.rebuildSummaryTable();
        }
        
        this.lodSpinnerModel.addChangeListener(this.spinnerChangeListener);
        this.alphaSpinnerModel.addChangeListener(this.spinnerChangeListener);
        
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
     * Rebuild the scan one summary object
     */
    private void buildScanOneSummary()
    {
        ScanOneResult selectedResult = this.getSelectedScanOneResult();
        ConfidenceThresholdState selectedThreshold = this.getSelectedThreshold();
        String selectedLodColumnName = this.getSelectedLodColumnName();
        
        if(selectedResult != null && selectedThreshold != null && selectedLodColumnName != null)
        {
            double currentThresholdValue = this.getThresholdValue(
                    selectedThreshold);
            
            ScanOneSummaryBuilder scanOneSummaryBuilder = new ScanOneSummaryBuilder(
                    selectedResult,
                    selectedThreshold,
                    selectedLodColumnName,
                    currentThresholdValue);
            this.scanOneSummary = scanOneSummaryBuilder.createSummary();
        }
        else
        {
            this.scanOneSummary = null;
        }
    }
    
    private String getSelectedLodColumnName()
    {
        return (String)this.lodColumnComboBox.getSelectedItem();
    }

    /**
     * Rebuild the scan one summary table
     */
    private void rebuildSummaryTable()
    {
        CheckableListTableModel scanResultsTableModel =
            this.getScanResultsTableModel();
        scanResultsTableModel.setRowCount(0);
        
        this.buildScanOneSummary();
        
        String[] header = this.createHeaderFor(this.scanOneSummary);
        scanResultsTableModel.setColumnIdentifiers(header);
        Set<String> emptySet = Collections.emptySet();
        this.refreshSummaryTable(
                this.scanOneSummary,
                emptySet);
    }
    
    /**
     * Refresh the contents of the scanone summary table
     */
    private void refreshSummaryTable()
    {
        List<ScanOneSummaryRow> selectedRows =
            this.getScanOneSummaryRowsFromTable(true);
        Set<String> selectedMarkerNames = new HashSet<String>(
                selectedRows.size());
        for(ScanOneSummaryRow currSelectedRow: selectedRows)
        {
            selectedMarkerNames.add(
                    currSelectedRow.getMarker().getMarkerName());
        }
        
        this.buildScanOneSummary();
        this.refreshSummaryTable(
                this.scanOneSummary,
                selectedMarkerNames);
    }
    
    /**
     * Refresh the summary table contents
     * @param summary
     *          the summary to use
     */
    private void refreshSummaryTable(
            ScanOneSummary summary,
            Set<String> markersNamesToSelect)
    {
        CheckableListTableModel scanResultsTableModel =
            this.getScanResultsTableModel();
        scanResultsTableModel.setRowCount(0);
        
        if(summary != null)
        {
            ScanOneSummaryRow[] summaryRows = summary.getScanOneSummaryRows();
            for(int rowNumber = 0; rowNumber < summaryRows.length; rowNumber++)
            {
                boolean selected = markersNamesToSelect.contains(
                        summaryRows[rowNumber].getMarker().getMarkerName());
                Object[] tableRow = this.createRow(summary, selected, rowNumber);
                scanResultsTableModel.addRow(tableRow);
            }
        }
    }

    /**
     * Get the threshold value for the given threshold state
     * @param selectedThreshold
     *          the threshold state
     * @return
     *          the threshold value
     */
    private double getThresholdValue(ConfidenceThresholdState selectedThreshold)
    {
        switch(selectedThreshold)
        {
            case NO_THRESHOLD:
            {
                return 0.0;
            }
            
            case ALPHA_THRESHOLD:
            {
                return this.alphaSpinnerModel.getNumber().doubleValue();
            }
            
            case LOD_SCORE_THRESHOLD:
            {
                return this.lodSpinnerModel.getNumber().doubleValue();
            }
            
            default:
            {
                LOG.warning("unknown threshold type: " + selectedThreshold);
                return 0.0;
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
        return (ConfidenceThresholdState)this.thresholdComboBox.getSelectedItem();
    }

    /**
     * Get the selected scanone result
     * @return
     *          the selected result
     */
    private ScanOneResult getSelectedScanOneResult()
    {
        return (ScanOneResult)this.scanResultComboBox.getSelectedItem();
    }

    /**
     * Create a header for the given summary
     * @param scanOneSummary
     *          the summary that we're creating a header for
     * @return
     *          the header
     */
    private String[] createHeaderFor(ScanOneSummary scanOneSummary)
    {
        if(scanOneSummary == null)
        {
            return new String[] {"No Scan Results Selected"};
        }
        else if(scanOneSummary.getPValuesAreValid())
        {
            return new String[] {"Selected", "Marker", "LOD", "p-value"};
        }
        else
        {
            return new String[] {"Selected", "Marker", "LOD"};
        }
    }

    /**
     * Create a new table row
     * @param scanOneSummary
     *          the summary to use for the row
     * @param selected
     *          true iff the row should be selected
     * @param rowNumber
     *          the row number
     * @return
     *          the row object array
     */
    private Object[] createRow(
            ScanOneSummary scanOneSummary,
            boolean selected,
            int rowNumber)
    {
        final Object[] row;
        if(scanOneSummary.getPValuesAreValid())
        {
            row = new Object[4];
        }
        else
        {
            row = new Object[3];
        }
        
        row[0] = selected;
        for(int column = 1; column < row.length; column++)
        {
            row[column] = new ScanOneSummaryCell(
                    scanOneSummary,
                    rowNumber,
                    column);
        }
        
        return row;
    }
    
    /**
     * Wraps up significance values for presentation in a
     * {@link javax.swing.JTable}
     */
    private class ScanOneSummaryCell implements FormattedData
    {
        private final ScanOneSummary scanOneSummary;
        
        private final int columnNumber;

        private final int rowNumber;
        
        /**
         * Constructor
         * @param scanOneSummary
         *          the summary that we're showing
         * @param rowNumber
         *          the row number for this cell
         * @param columnNumber
         *          the column number for this cell
         */
        public ScanOneSummaryCell(
                ScanOneSummary scanOneSummary,
                int rowNumber,
                int columnNumber)
        {
            this.scanOneSummary = scanOneSummary;
            this.rowNumber = rowNumber;
            this.columnNumber = columnNumber;
        }
        
        /**
         * {@inheritDoc}
         */
        public String toUnformattedString()
        {
            ScanOneSummaryRow thisRow = this.getScanOneSummaryRow();
            switch(this.columnNumber)
            {
                case 1:
                {
                    return thisRow.getMarker().toString(
                                   ScanOneSummaryPanel.this.getSelectedMarkerFormat());
                }
                
                case 2:
                {
                    return Constants.THREE_DIGIT_FORMATTER.format(
                            thisRow.getLodScore());
                }
                
                case 3:
                {
                    return Constants.THREE_DIGIT_FORMATTER.format(
                            thisRow.getPValue());
                }
                
                default:
                {
                    return null;
                }
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public String toFormattedString()
        {
            ScanOneSummaryRow thisRow = this.getScanOneSummaryRow();
            switch(this.columnNumber)
            {
                case 1:
                {
                    return "<html><u><font color=\"blue\">" +
                           thisRow.getMarker().toString(
                                   ScanOneSummaryPanel.this.getSelectedMarkerFormat()) +
                           "</font></u></html>";
                }
                
                case 2:
                {
                    return Constants.THREE_DIGIT_FORMATTER.format(
                            thisRow.getLodScore());
                }
                
                case 3:
                {
                    return Constants.THREE_DIGIT_FORMATTER.format(
                            thisRow.getPValue());
                }
                
                default:
                {
                    return null;
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

        /**
         * The the summary row for this cell
         * @return
         *          the summary row
         */
        private ScanOneSummaryRow getScanOneSummaryRow()
        {
            return this.scanOneSummary.getScanOneSummaryRows()[this.rowNumber];
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
        int rowCount =
            summaryTableModel.getRowCount();
        for(int i = 0; i < rowCount; i++)
        {
            summaryTableModel.setValueAt(selectedStateBoolean, i, 0);
        }
    }
    
    /**
     * Get summary values from the table
     * @param selected
     *          determines whether we're getting the selected rows or
     *          the deselected rows
     * @return
     *          the list
     */
    private List<ScanOneSummaryRow> getScanOneSummaryRowsFromTable(
            boolean selected)
    {
        
        CheckableListTableModel summaryTableModel = this.getScanResultsTableModel();
        int totalRowCount =
            summaryTableModel.getRowCount();
        List<ScanOneSummaryRow> matchingRows =
            new ArrayList<ScanOneSummaryRow>(totalRowCount);
        for(int tableRow = 0; tableRow < totalRowCount; tableRow++)
        {
            boolean currSelected =
                (Boolean)summaryTableModel.getValueAt(tableRow, 0);
            if(currSelected == selected)
            {
                ScanOneSummaryRow currScanOneSummaryRow =
                    this.getScanOneSummaryRowAtTableRow(tableRow);
                matchingRows.add(currScanOneSummaryRow);
            }
        }
        
        return matchingRows;
    }
    
    /**
     * Get the scan one summary row at the given table row
     * @param tableRow
     *          the table row number
     * @return
     *          the scan one summary row
     */
    private ScanOneSummaryRow getScanOneSummaryRowAtTableRow(int tableRow)
    {
        ScanOneSummaryCell cell = (ScanOneSummaryCell)this.scanResultsTable.getValueAt(
                tableRow,
                1);
        return cell.getScanOneSummaryRow();
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
            Set<ScanOneResult> scanResults =
                selectedCross.getScanOneResults();
            for(ScanOneResult currScanOneResult: scanResults)
            {
                this.scanResultComboBox.addItem(currScanOneResult);
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
                this.thresholdSpinner.setModel(new SpinnerNumberModel());
                this.thresholdSpinner.setEnabled(false);
                break;
            }
            
            case LOD_SCORE_THRESHOLD:
            {
                this.thresholdSpinner.setModel(this.lodSpinnerModel);
                this.thresholdSpinner.setEnabled(true);
                break;
            }
            
            case ALPHA_THRESHOLD:
            {
                this.thresholdSpinner.setModel(this.alphaSpinnerModel);
                this.thresholdSpinner.setEnabled(true);
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
        
        this.refreshSummaryTable();
    }
    
    /**
     * Respond to a change in the selected scan result
     */
    private void selectedScanResultChanged()
    {
        ScanOneResult selectedScanResult = this.getSelectedScanOneResult();
        this.lodColumnComboBox.removeAllItems();
        if(selectedScanResult != null)
        {
            if(selectedScanResult.getPermutationsWereCalculated())
            {
                boolean alphaAlreadyInList = false;
                int itemCount = this.thresholdComboBox.getItemCount();
                for(int i = 0; i < itemCount; i++)
                {
                    if(this.thresholdComboBox.getItemAt(i) == ConfidenceThresholdState.ALPHA_THRESHOLD)
                    {
                        alphaAlreadyInList = true;
                        break;
                    }
                }
                
                if(!alphaAlreadyInList)
                {
                    this.thresholdComboBox.addItem(
                            ConfidenceThresholdState.ALPHA_THRESHOLD);
                }
            }
            else
            {
                this.thresholdComboBox.removeItem(
                        ConfidenceThresholdState.ALPHA_THRESHOLD);
            }
            
            String[] lodColumnNames = selectedScanResult.getSignificanceValueColumnNames();
            for(String currLodColumnName: lodColumnNames)
            {
                this.lodColumnComboBox.addItem(currLodColumnName);
            }
        }
    }
    
    private void selectedLodColumnNameChanged()
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
        lodColumnComboBox = new javax.swing.JComboBox();
        thresholdLabel = new javax.swing.JLabel();
        thresholdComboBox = new javax.swing.JComboBox();
        thresholdSpinner = new javax.swing.JSpinner();
        scanResultsLabel = new javax.swing.JLabel();
        scanResultsScrollPane = new javax.swing.JScrollPane();
        scanResultsTable = new javax.swing.JTable();
        toggleSelectAllButton = new javax.swing.JButton();
        addMarkersToBasketButton = new javax.swing.JButton();
        exportTableButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        markerFormatComboBox = new javax.swing.JComboBox();

        scanResultLabel.setText("One QTL Scan:");

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

        lodColumnComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                lodColumnComboBoxItemStateChanged(evt);
            }
        });

        thresholdLabel.setText("Threshold:");

        thresholdComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                thresholdComboBoxItemStateChanged(evt);
            }
        });

        scanResultsLabel.setText("Chromosome Peaks:");

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
                    .add(scanResultsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE)
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
                        .add(lodColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(thresholdLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(thresholdComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(thresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(scanResultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
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
                    .add(lodColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(thresholdLabel)
                    .add(thresholdComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(thresholdSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(scanResultsLabel)
                    .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scanResultsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
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
        List<ScanOneSummaryRow> selectedRows =
            this.getScanOneSummaryRowsFromTable(true);
        List<QtlBasketItem> qtlBasketItems = new ArrayList<QtlBasketItem>();
        for(ScanOneSummaryRow scanOneSummaryRow: selectedRows)
        {
            GeneticMarker marker = scanOneSummaryRow.getMarker();
            
            qtlBasketItems.add(new SingleMarkerQtlBasketItem(marker, ""));
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
        List<ScanOneSummaryRow> uncheckedMarkers =
            this.getScanOneSummaryRowsFromTable(false);
        this.setAllMarkersSelectionStateTo(!uncheckedMarkers.isEmpty());
    }//GEN-LAST:event_toggleSelectAllButtonActionPerformed

    private void crossComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crossComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.crossSelectionChanged();
        }
    }//GEN-LAST:event_crossComboBoxItemStateChanged

    private void thresholdComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_thresholdComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.confidenceThresholdStateChanged();
        }
    }//GEN-LAST:event_thresholdComboBoxItemStateChanged

    private void markerFormatComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_markerFormatComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.scanResultsTable.repaint();
        }
    }//GEN-LAST:event_markerFormatComboBoxItemStateChanged

    private void lodColumnComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lodColumnComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.selectedLodColumnNameChanged();
        }
    }//GEN-LAST:event_lodColumnComboBoxItemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkersToBasketButton;
    private javax.swing.JComboBox crossComboBox;
    private javax.swing.JButton exportTableButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JComboBox lodColumnComboBox;
    private javax.swing.JComboBox markerFormatComboBox;
    private javax.swing.JComboBox scanResultComboBox;
    private javax.swing.JLabel scanResultLabel;
    private javax.swing.JLabel scanResultsLabel;
    private javax.swing.JScrollPane scanResultsScrollPane;
    private javax.swing.JTable scanResultsTable;
    private javax.swing.JComboBox thresholdComboBox;
    private javax.swing.JLabel thresholdLabel;
    private javax.swing.JSpinner thresholdSpinner;
    private javax.swing.JButton toggleSelectAllButton;
    // End of variables declaration//GEN-END:variables
    
}
