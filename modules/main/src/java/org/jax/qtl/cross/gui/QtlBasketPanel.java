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

import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.MarkerPairQtlBasketItem;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.SingleMarkerQtlBasketItem;
import org.jax.qtl.cross.GeneticMarker.MarkerStringFormat;
import org.jax.qtl.gui.ExportDataTableAction;
import org.jax.util.TextWrapper;
import org.jax.util.io.JTableDataTable;

/**
 * A panel for viewing and editing QTL basket info
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlBasketPanel extends javax.swing.JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1265801937556731429L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QtlBasketPanel.class.getName());
    
    private static final String HELP_ID_STRING = "Edit_QTL_Basket";
    
    private QtlBasket qtlBasket;
    
    private final List<QtlBasketItem> selectedQtlBasketItems;
    
    private final ListSelectionListener qtlTableSelectionListener =
        new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                QtlBasketPanel.this.qtlTableSelectionChanged();
            }
        };
    
    private final DocumentListener qtlCommentDocumentListener =
        new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                this.anyChange();
            }

            public void insertUpdate(DocumentEvent e)
            {
                this.anyChange();
            }

            public void removeUpdate(DocumentEvent e)
            {
                this.anyChange();
            }
            
            private void anyChange()
            {
                QtlBasketPanel.this.qtlCommentChanged();
            }
        };
    
    private final PropertyChangeListener qtlBasketPropertyListener =
        new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                QtlBasketPanel.this.refreshQtlTableContents();
            }
        };
    
    /**
     * Constructor
     * @param qtlBasket
     *          the qtl basket that we're displaying
     */
    public QtlBasketPanel(QtlBasket qtlBasket)
    {
        this.qtlBasket = qtlBasket;
        this.qtlBasket.addPropertyChangeListener(
                this.qtlBasketPropertyListener);
        this.selectedQtlBasketItems = new ArrayList<QtlBasketItem>();
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Take care of the GUI initialization that wasn't handled by the
     * GUI builder's code
     */
    private void postGuiInit()
    {
        for(MarkerStringFormat currFormat: MarkerStringFormat.values())
        {
            this.markerFormatComboBox.addItem(currFormat);
        }
        
        String[] tableHeader = new String[] {
                "QTL Type",
                "QTL Name",
                "Comments"};
        DefaultTableModel qtlTableModel =
            new DefaultTableModel(tableHeader, 0)
            {
                /**
                 * for serialization
                 */
                private static final long serialVersionUID = 3691035618365541184L;

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };
        
        this.qtlTable.setModel(qtlTableModel);
        
        this.refreshQtlTableContents();
        
        this.exportTableButton.setAction(new ExportDataTableAction(
                new JTableDataTable(this.qtlTable)));
        
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
     * Respond to a change in the qtl comment
     */
    protected void qtlCommentChanged()
    {
        if(this.selectedQtlBasketItems.size() == 1)
        {
            this.selectedQtlBasketItems.get(0).setComment(
                    this.qtlCommentEditorTextArea.getText());
            this.qtlTable.repaint();
        }
        else
        {
            LOG.warning(
                    "QTL comment changed when it should not have been " +
                    "able to. Selected QTL candidates = " +
                    this.selectedQtlBasketItems.size());
        }
    }
    
    /**
     * Respond to a change in table row selection
     */
    protected void qtlTableSelectionChanged()
    {
        int[] selectedRows = this.qtlTable.getSelectedRows();
        QtlBasketItem[] contents =
            this.qtlBasket.getContents().toArray(new QtlBasketItem[0]);
        
        this.selectedQtlBasketItems.clear();
        for(int selectedRow: selectedRows)
        {
            this.selectedQtlBasketItems.add(contents[selectedRow]);
        }
        
        this.removeQtlButton.setEnabled(!this.selectedQtlBasketItems.isEmpty());
        
        this.refreshQtlCommentTextArea();
    }

    /**
     * This method makes sure that the comment area is up to date with
     * the selected QTLs
     */
    private void refreshQtlCommentTextArea()
    {
        this.qtlCommentEditorTextArea.getDocument().removeDocumentListener(
                this.qtlCommentDocumentListener);
        
        if(this.selectedQtlBasketItems.isEmpty())
        {
            this.qtlCommentEditorTextArea.setEnabled(false);
            this.qtlCommentLabel.setEnabled(false);
            this.qtlCommentEditorTextArea.setText(
                    "Select a QTL candidate to edit its comment");
        }
        else if(this.selectedQtlBasketItems.size() == 1)
        {
            this.qtlCommentEditorTextArea.setEnabled(true);
            this.qtlCommentLabel.setEnabled(true);
            this.qtlCommentEditorTextArea.setText(
                    this.selectedQtlBasketItems.get(0).getComment());
        }
        else
        {
            this.qtlCommentEditorTextArea.setEnabled(false);
            this.qtlCommentLabel.setEnabled(false);
            this.qtlCommentEditorTextArea.setText(
                    "Cannot edit multiple QTL candidate comments simultaneously");
        }
        
        this.qtlCommentEditorTextArea.getDocument().addDocumentListener(
                this.qtlCommentDocumentListener);
    }

    /**
     * convenience function for getting a narrowed cast of the qtl table
     * model
     * @return
     *          the model
     */
    private DefaultTableModel getQtlTableModel()
    {
        return (DefaultTableModel)this.qtlTable.getModel();
    }

    /**
     * Update the table contents to reflect whats in the QTL basket that
     * we're showing
     */
    private void refreshQtlTableContents()
    {
        this.qtlTable.getSelectionModel().removeListSelectionListener(
                this.qtlTableSelectionListener);
        
        DefaultTableModel qtlTableModel = this.getQtlTableModel();
        qtlTableModel.setRowCount(0);
        QtlBasketItem[] contents =
            this.qtlBasket.getContents().toArray(new QtlBasketItem[0]);
        
        for(QtlBasketItem qtlBasketItem: contents)
        {
            qtlTableModel.addRow(this.createQtlTableRowFor(qtlBasketItem));
        }
        
        this.qtlTable.getSelectionModel().addListSelectionListener(
                this.qtlTableSelectionListener);
        this.qtlTableSelectionChanged();
    }
    
    /**
     * Create a new row based on the given basket item
     * @param item
     *          the item to create a new row for
     * @return
     *          the array of cells for the row
     */
    private Object[] createQtlTableRowFor(QtlBasketItem item)
    {
        return new Object[] {
                new QtlBasketItemCell(item, 0),
                new QtlBasketItemCell(item, 1),
                new QtlBasketItemCell(item, 2)};
    }
    
    /**
     * A table cell for a qtl basket item
     */
    private class QtlBasketItemCell
    {
        private final int columnNumber;

        private final QtlBasketItem basketItem;

        /**
         * Constructor
         * @param basketItem
         *          the item we're holding
         * @param columnNumber
         *          the column number for this cell
         */
        public QtlBasketItemCell(QtlBasketItem basketItem, int columnNumber)
        {
            this.basketItem = basketItem;
            this.columnNumber = columnNumber;
        }
        
        /**
         * @return the basketItem
         */
        public QtlBasketItem getBasketItem()
        {
            return this.basketItem;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if(this.columnNumber == 0)
            {
                if(this.basketItem instanceof SingleMarkerQtlBasketItem)
                {
                    return "marker";
                }
                else if(this.basketItem instanceof MarkerPairQtlBasketItem)
                {
                    return "marker interaction";
                }
                else
                {
                    throw new IllegalStateException(
                            "unknown type: " +
                            this.basketItem.getClass().getName());
                }
            }
            else if(this.columnNumber == 1)
            {
                if(this.basketItem instanceof SingleMarkerQtlBasketItem)
                {
                    SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                        (SingleMarkerQtlBasketItem)this.basketItem;
                    return singleMarkerQtlBasketItem.getMarker().toString(
                            QtlBasketPanel.this.getSelectedMarkerFormat());
                }
                else if(this.basketItem instanceof MarkerPairQtlBasketItem)
                {
                    MarkerPairQtlBasketItem markerPairQtlBasketItem =
                        (MarkerPairQtlBasketItem)this.basketItem;
                    return markerPairQtlBasketItem.getMarkerPair().toString(
                            QtlBasketPanel.this.getSelectedMarkerFormat());
                }
                else
                {
                    throw new IllegalStateException(
                            "unknown type: " +
                            this.basketItem.getClass().getName());
                }
            }
            else if(this.columnNumber == 2)
            {
                return this.basketItem.getComment();
            }
            else
            {
                throw new IllegalStateException(
                        "column number is not valid: " +
                        this.columnNumber);
            }
        }
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
    
    private void addQtlToBasket()
    {
        List<CrossChromosome> genoData = this.qtlBasket.getParentCross().getGenotypeData();
        List<GeneticMarker> markerList = new ArrayList<GeneticMarker>();
        for(CrossChromosome currChromosome: genoData)
        {
            markerList.addAll(currChromosome.getAnyGeneticMap().getMarkerPositions());
        }
        GeneticMarker[] markerArray = markerList.toArray(
                new GeneticMarker[markerList.size()]);
        
        GeneticMarker selectedMarker = (GeneticMarker)JOptionPane.showInputDialog(
                this,
                TextWrapper.wrapText(
                        "Select a marker to add to the QTL basket or cancel:",
                        TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                "Add Marker to QTL Basket",
                JOptionPane.PLAIN_MESSAGE,
                null,
                markerArray,
                markerArray[0]);

        if(selectedMarker != null)
        {
            this.qtlBasket.getContents().add(new SingleMarkerQtlBasketItem(
                    selectedMarker, ""));
            this.qtlBasket.notifyContentsChanged();
        }
        else
        {
            LOG.info("Add QLT canceled");
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

        closeButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        qtlTable = new javax.swing.JTable();
        addQtlButton = new javax.swing.JButton();
        removeQtlButton = new javax.swing.JButton();
        exportTableButton = new javax.swing.JButton();
        markerFormatComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        qtlCommentLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        qtlCommentEditorTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        helpButton = new javax.swing.JButton();

        closeButton.setText("Close");

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.7);
        jSplitPane1.setOneTouchExpandable(true);

        jLabel2.setText("Candidate QTLs:");

        jScrollPane2.setViewportView(qtlTable);

        addQtlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/add-16x16.png"))); // NOI18N
        addQtlButton.setText("Add QTL ...");
        addQtlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addQtlButtonActionPerformed(evt);
            }
        });

        removeQtlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/remove-16x16.png"))); // NOI18N
        removeQtlButton.setText("Remove Selected QTL(s)");
        removeQtlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeQtlButtonActionPerformed(evt);
            }
        });

        exportTableButton.setText("Export Table ...");

        markerFormatComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                markerFormatComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(addQtlButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeQtlButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportTableButton))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(markerFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(addQtlButton)
                    .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(removeQtlButton)
                        .add(exportTableButton))))
        );

        jSplitPane1.setTopComponent(jPanel2);

        qtlCommentLabel.setText("QTL Comment Editor:");

        qtlCommentEditorTextArea.setColumns(20);
        qtlCommentEditorTextArea.setRows(5);
        jScrollPane1.setViewportView(qtlCommentEditorTextArea);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 672, Short.MAX_VALUE)
                    .add(qtlCommentLabel))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(qtlCommentLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel3);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help ...");
        jPanel1.add(helpButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addQtlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addQtlButtonActionPerformed
        this.addQtlToBasket();
    }//GEN-LAST:event_addQtlButtonActionPerformed

    private void removeQtlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeQtlButtonActionPerformed
        this.qtlBasket.getContents().removeAll(this.selectedQtlBasketItems);
        this.qtlBasket.notifyContentsChanged();
    }//GEN-LAST:event_removeQtlButtonActionPerformed

    private void markerFormatComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_markerFormatComboBoxItemStateChanged
        if(evt.getStateChange() == ItemEvent.SELECTED)
        {
            this.qtlTable.repaint();
        }
    }//GEN-LAST:event_markerFormatComboBoxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addQtlButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton exportTableButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox markerFormatComboBox;
    private javax.swing.JTextArea qtlCommentEditorTextArea;
    private javax.swing.JLabel qtlCommentLabel;
    private javax.swing.JTable qtlTable;
    private javax.swing.JButton removeQtlButton;
    // End of variables declaration//GEN-END:variables
    
}
