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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorListener;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.TextWrapper;

/**
 * A panel for editing the permutations and result name of a
 * {@link ScanCommandBuilder}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PermutationsAndResultScanPanel extends ScanCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5936966692016099862L;

    /**
     * the logger
     */
    private static final Logger LOG = Logger.getLogger(
            PermutationsAndResultScanPanel.class.getName());
    
    private final ScanPermutationsPanel scanPermutationsPanel;
    
    private final ScanCommandBuilder scanCommand;
    
    /**
     * Constructor
     * @param scanCommand
     *          the scan command that this panel edits
     */
    public PermutationsAndResultScanPanel(ScanCommandBuilder scanCommand)
    {
        this.scanCommand = scanCommand;
        this.scanPermutationsPanel = new ScanPermutationsPanel(scanCommand);
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addRCommandEditorListener(RCommandEditorListener editorListener)
    {
        super.addRCommandEditorListener(editorListener);
        this.scanPermutationsPanel.addRCommandEditorListener(editorListener);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        super.removeRCommandEditorListener(editorListener);
        this.scanPermutationsPanel.removeRCommandEditorListener(editorListener);
    }
    
    /**
     * Take care of the initialization that needs to happen after the
     * GUI builder code has run.
     */
    private void postGuiInit()
    {
        this.scanResultTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        PermutationsAndResultScanPanel.this.scanResultNameChanged();
                    }

                    public void insertUpdate(DocumentEvent e)
                    {
                        PermutationsAndResultScanPanel.this.scanResultNameChanged();
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        PermutationsAndResultScanPanel.this.scanResultNameChanged();
                    }
                });
    }

    /**
     * Respond to a change in the scan result name
     */
    private void scanResultNameChanged()
    {
        try
        {
            // construct the scan result text field from the readable name
            String scanResultIdentifier = RUtilities.fromReadableNameToRIdentifier(
                    this.scanResultTextField.getText().trim());
            if(scanResultIdentifier.length() > 0)
            {
                // append the cross name to the identifier
                scanResultIdentifier =
                    this.scanCommand.getCross().getAccessorExpressionString() + "." +
                    scanResultIdentifier;
            }
            
            this.scanCommand.setScanResultName(
                    scanResultIdentifier);
            this.fireCommandModified();
        }
        catch(RSyntaxException ex)
        {
            this.scanCommand.setScanResultName(null);
            this.fireCommandModified();
            LOG.log(Level.FINE,
                    "user error: bad syntax for scan result",
                    ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scanResultLabel = new javax.swing.JLabel();
        scanResultTextField = new javax.swing.JTextField();
        permutationsPanelDownCast = this.scanPermutationsPanel;

        scanResultLabel.setText("Name Your Scan Result:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(permutationsPanelDownCast, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(scanResultLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scanResultTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(scanResultLabel)
                    .add(scanResultTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(permutationsPanelDownCast, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel permutationsPanelDownCast;
    private javax.swing.JLabel scanResultLabel;
    private javax.swing.JTextField scanResultTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * Validate the data contained in this panel. The user is alerted if
     * there is something wrong with the data format
     * @return
     *          true iff the validation succeeds
     */
    public boolean validateData()
    {
        String message = null;
        
        String scanResultIdentifier = this.scanCommand.getScanResultName();
        String readableScanResultName =
            this.scanResultTextField.getText().trim();
        
        if(scanResultIdentifier == null || scanResultIdentifier.length() == 0)
        {
            if(this.scanResultTextField.getText().length() == 0)
            {
                message = "Scan result name cannot be empty";
            }
            else
            {
                message = RUtilities.getErrorMessageForReadableName(
                        readableScanResultName);
                if(message == null)
                {
                    message = "Cannot parse scan result name";
                }
            }
        }
        else if(JRIUtilityFunctions.isTopLevelObject(
                scanResultIdentifier,
                RInterfaceFactory.getRInterfaceInstance()))
        {
            message =
                "The name \"" + readableScanResultName + "\" conflicts with " +
                "an existing data object. Please choose another name.";
        }
        
        if(message != null)
        {
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                            "Validation Failed",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * This function tells this panel that a component other than itself
     * has edited the {@link ScanCommandBuilder} and that we need to refresh
     * our graphics to reflect those changes.
     */
    public void refreshGui()
    {
        String scanResultName = this.scanCommand.getScanResultName();
        String crossNamePlusDot =
            this.scanCommand.getCross().getAccessorExpressionString() + ".";
        if(scanResultName != null && scanResultName.startsWith(crossNamePlusDot))
        {
            scanResultName = scanResultName.substring(crossNamePlusDot.length());
            this.scanResultTextField.setText(RUtilities.fromRIdentifierToReadableName(
                    scanResultName));
        }
        else
        {
            this.scanResultTextField.setText("");
        }
        
        this.scanPermutationsPanel.refreshGui();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScanCommandBuilder getScanCommand()
    {
        return this.scanCommand;
    }
}
