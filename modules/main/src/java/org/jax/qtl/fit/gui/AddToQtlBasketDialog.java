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

package org.jax.qtl.fit.gui;

import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.TextWrapper;

/**
 * A dialog that allows you to choose which qtl basket a selection of markers
 * gets added to.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AddToQtlBasketDialog extends javax.swing.JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4566959480624529075L;
    
    /**
     * The cross that we're adding the QTLs to
     */
    private final Cross cross;
    
    /**
     * the contents to add to the basket
     */
    private final List<QtlBasketItem> contentsToAdd;
    
    /**
     * Constructor
     * @param parent
     *          the frame that owns this dialog
     * @param cross
     *          the cross we're adding to
     * @param contentsToAdd
     *          the contents to add to the basket
     */
    public AddToQtlBasketDialog(
            java.awt.Frame parent,
            Cross cross,
            List<QtlBasketItem> contentsToAdd)
    {
        super(parent, "Add to QTL Basket", true);
        
        this.cross = cross;
        this.contentsToAdd = contentsToAdd;
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Do initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        DefaultComboBoxModel addToExistingModel =
            (DefaultComboBoxModel)this.addToExistingComboBox.getModel();
        Map<String, QtlBasket> existingQtlBasketMap =
            this.cross.getQtlBasketMap();
        if(existingQtlBasketMap.isEmpty())
        {
            this.addToExistingRadioButton.setEnabled(false);
            this.addToNewRadioButton.setSelected(true);
            
            addToExistingModel.addElement("No Existing QTL Baskets");
        }
        else
        {
            QtlBasket[] existingBaskets = this.cross.getQtlBaskets();
            for(QtlBasket currBasket: existingBaskets)
            {
                addToExistingModel.addElement(currBasket);
            }
            
            this.addToExistingRadioButton.setSelected(true);
        }
        
        this.existingVsNewChanged();
    }

    /**
     * Respond to an update in whether the user is selecting a new
     * or updated qtl basket
     */
    private void existingVsNewChanged()
    {
        this.addToExistingComboBox.setEnabled(
                this.addToExistingRadioButton.isSelected());
        this.addToNewTextField.setEnabled(
                this.addToNewRadioButton.isSelected());
    }
    
    /**
     * Perform the add action paying attention to what options the user
     * has selected in the dialog
     * @return true iff the add succeeds
     */
    private boolean performAddToQtlBasket()
    {
        boolean success;
        
        if(this.addToExistingRadioButton.isSelected())
        {
            QtlBasket selectedBasket =
                (QtlBasket)this.addToExistingComboBox.getSelectedItem();
            selectedBasket.getContents().addAll(this.contentsToAdd);
            selectedBasket.notifyContentsChanged();
            
            success = true;
        }
        else
        {
            String newBasketReadableName = this.addToNewTextField.getText().trim();
            String message = null;
            if(newBasketReadableName.length() == 0)
            {
                message =
                    "Basket ID cannot be empty";
            }
            else if(this.cross.getQtlBasketMap().containsKey(newBasketReadableName))
            {
                message =
                    "Basket name \"" + newBasketReadableName +
                    "\" conflicts with an existing name";
            }
            else
            {
                QtlBasket newBasket = new QtlBasket(
                        this.cross,
                        newBasketReadableName);
                newBasket.getContents().addAll(this.contentsToAdd);
                this.cross.getQtlBasketMap().put(
                        newBasketReadableName,
                        newBasket);
            }
            
            if(message == null)
            {
                success = true;
            }
            else
            {
                JOptionPane.showMessageDialog(
                        this,
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Create QTL Basket Failed",
                        JOptionPane.WARNING_MESSAGE);
                success = false;
            }
        }
        
        if(success)
        {
            QtlProjectManager.getInstance().notifyActiveProjectModified();
        }
        
        return success;
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

        existingOrNewButtonGroup = new javax.swing.ButtonGroup();
        addToExistingRadioButton = new javax.swing.JRadioButton();
        addToExistingComboBox = new javax.swing.JComboBox();
        addToNewRadioButton = new javax.swing.JRadioButton();
        addToNewTextField = new javax.swing.JTextField();
        commandButtonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        existingOrNewButtonGroup.add(addToExistingRadioButton);
        addToExistingRadioButton.setText("Add to Existing QTL Basket");
        addToExistingRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                addToExistingRadioButtonItemStateChanged(evt);
            }
        });

        existingOrNewButtonGroup.add(addToNewRadioButton);
        addToNewRadioButton.setText("Add to New QTL Basket");
        addToNewRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                addToNewRadioButtonItemStateChanged(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        commandButtonPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        commandButtonPanel.add(cancelButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addToExistingRadioButton)
                    .add(addToNewRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addToExistingComboBox, 0, 164, Short.MAX_VALUE)
                    .add(addToNewTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
                .addContainerGap())
            .add(commandButtonPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addToExistingRadioButton)
                    .add(addToExistingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addToNewRadioButton)
                    .add(addToNewTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(commandButtonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addToExistingRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_addToExistingRadioButtonItemStateChanged
        this.existingVsNewChanged();
    }//GEN-LAST:event_addToExistingRadioButtonItemStateChanged

    private void addToNewRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_addToNewRadioButtonItemStateChanged
        this.existingVsNewChanged();
    }//GEN-LAST:event_addToNewRadioButtonItemStateChanged

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if(this.performAddToQtlBasket())
        {
            this.dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox addToExistingComboBox;
    private javax.swing.JRadioButton addToExistingRadioButton;
    private javax.swing.JRadioButton addToNewRadioButton;
    private javax.swing.JTextField addToNewTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel commandButtonPanel;
    private javax.swing.ButtonGroup existingOrNewButtonGroup;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
    
}
