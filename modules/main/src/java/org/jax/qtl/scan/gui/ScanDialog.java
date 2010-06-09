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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.qtl.scan.ScanType;
import org.jax.r.gui.RCommandEditorAndPreviewPanel;

/**
 * Dialog used for editing a scanone or scantwo command.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanDialog extends javax.swing.JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -5134252851036554406L;
    
    private final RCommandEditorAndPreviewPanel editorAndPreviewPanel;
    private final ScanCommandBuilder scanCommand;
    private final AllScanPanels allScanPanels;
    
    private final ConcurrentLinkedQueue<ActionListener> actionListenerList;
    
    private static final String SCANONE_HELP_ID_STRING = "One_QTL_Genome_Scan";
    private static final String SCANTWO_HELP_ID_STRING = "Two_QTL_Genome_Scan";
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     * @param scanType
     *          the type of scan to perform
     * @param availableCrosses
     *          the list of available crosses
     * @param selectedCross
     *          the selected cross
     */
    public ScanDialog(
            java.awt.Frame parent,
            ScanType scanType,
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        super(parent, scanType.toString(), true);
        
        this.scanCommand = new ScanCommandBuilder();
        this.scanCommand.setScanType(scanType);
        this.allScanPanels = new AllScanPanels(
                this,
                this.scanCommand,
                availableCrosses,
                selectedCross);
        this.editorAndPreviewPanel = new RCommandEditorAndPreviewPanel(
                this.allScanPanels);
        this.actionListenerList = new ConcurrentLinkedQueue<ActionListener>();
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * take care of the gui initialization that wasn't done by the
     * GUI builder code
     */
    private void postGuiInit()
    {
        // initialize the help stuff
        HelpSet hs = QTL.getInstance().getMenubar().getHelpSet();
        CSH.setHelpIDString(
                this.helpButton,
                this.scanCommand.getScanType() == ScanType.SCANONE ?
                        SCANONE_HELP_ID_STRING :
                        SCANTWO_HELP_ID_STRING);
        this.helpButton.addActionListener(
                new CSH.DisplayHelpFromSource(
                        hs,
                        SecondaryWindow.class.getName(),
                        null));
    }

    /**
     * Getter for the scan command that this dialog edits
     * @return the scan command that this dialog edits
     */
    public ScanCommandBuilder getScanCommand()
    {
        return this.scanCommand;
    }

    /**
     * Add an action listener. The listener will get called if a scan command
     * is successfully completed.
     * @param actionListener
     *          the action listener to add
     */
    public void addActionListener(ActionListener actionListener)
    {
        this.actionListenerList.add(actionListener);
    }
    
    /**
     * Remove an action listener.
     * @param actionListener
     *          the action listener to remove
     */
    public void removeActionListener(ActionListener actionListener)
    {
        this.actionListenerList.remove(actionListener);
    }
    
    /**
     * Tell all of the action listeners that we've finished.
     */
    private void fireScanCompleted()
    {
        Iterator<ActionListener> actionIter = this.actionListenerList.iterator();
        ActionEvent actionEvent = new ActionEvent(
                this,
                ActionEvent.ACTION_LAST + 1,
                "commandEditingCompleted");
        while(actionIter.hasNext())
        {
            actionIter.next().actionPerformed(actionEvent);
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

        mainContentPanel = this.editorAndPreviewPanel;
        controlPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/back-16x16.png"))); // NOI18N
        backButton.setText("Back");
        backButton.setEnabled(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        controlPanel.add(backButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/forward-16x16.png"))); // NOI18N
        nextButton.setText("Next");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        controlPanel.add(nextButton);

        finishButton.setText("Finish");
        finishButton.setEnabled(false);
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });
        controlPanel.add(finishButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        controlPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        controlPanel.add(helpButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
            .add(mainContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(mainContentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        this.allScanPanels.back();
        this.updateControlButtons();
    }//GEN-LAST:event_backButtonActionPerformed

    /**
     * Update the [en/dis]abled status of the control buttons
     */
    private void updateControlButtons()
    {
        this.nextButton.setEnabled(this.allScanPanels.isNextValid());
        this.backButton.setEnabled(this.allScanPanels.isBackValid());
        this.finishButton.setEnabled(this.allScanPanels.isFinishValid());
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        this.allScanPanels.next();
        this.updateControlButtons();
    }//GEN-LAST:event_nextButtonActionPerformed

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishButtonActionPerformed
        if(this.allScanPanels.validateData())
        {
            this.dispose();
            
            // tell the users that we've finished the scan
            this.fireScanCompleted();
        }
    }//GEN-LAST:event_finishButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.dispose();
    }//GEN-LAST:event_formWindowClosing
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton finishButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel mainContentPanel;
    private javax.swing.JButton nextButton;
    // End of variables declaration//GEN-END:variables
    
}
