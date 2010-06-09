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
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.util.TextWrapper;

/**
 * A small panel for editing the convergence parameters of a scan command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ConvergenceParametersPanel extends ScanCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -4254037720193218387L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ConvergenceParametersPanel.class.getName());
    
    /**
     * the scan command that this panel edits
     */
    private final ScanCommandBuilder scanCommand;
    
    /**
     * a handle for the spinner model that controls the max iterations parameter
     */
    private final SpinnerNumberModel maximumIterationsModel;
    
    /**
     * Constructor
     * @param scanCommand
     *          the scan command that this panel edits
     */
    public ConvergenceParametersPanel(ScanCommandBuilder scanCommand)
    {
        this.scanCommand = scanCommand;
        this.maximumIterationsModel = new SpinnerNumberModel(
                4000,
                0,
                Integer.MAX_VALUE,
                500);
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * Do the GUI initialization that isn't taken care of by the GUI builder
     * code
     */
    private void postGuiInit()
    {
        // update the spinner model to behave how we want it to
        this.maximumIterationsSpinner.setModel(this.maximumIterationsModel);
        this.maximumIterationsModel.addChangeListener(
                new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        ConvergenceParametersPanel.this.convergenceParametersUpdated();
                    }
                });
        this.convergenceToleranceTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        ConvergenceParametersPanel.this.convergenceParametersUpdated();
                    }

                    public void insertUpdate(DocumentEvent e)
                    {
                        ConvergenceParametersPanel.this.convergenceParametersUpdated();
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        ConvergenceParametersPanel.this.convergenceParametersUpdated();
                    }
                });
    }
    
    /**
     * Respond to a convergence updated event.
     */
    private void convergenceParametersUpdated()
    {
        if(this.useDefaultsCheckBox.isSelected())
        {
            this.scanCommand.setConvergenceTolerance(null);
            this.scanCommand.setMaximumNumberOfIterations(null);
        }
        else
        {
            try
            {
                double convergenceTolerance =
                    Double.parseDouble(this.convergenceToleranceTextField.getText().trim());
                this.scanCommand.setConvergenceTolerance(convergenceTolerance);
                this.fireCommandModified();
            }
            catch (NumberFormatException ex)
            {
                LOG.log(Level.FINE,
                        "convergence tolerance isn't formatted as a number (user error)",
                        ex);
            }

            this.scanCommand.setMaximumNumberOfIterations(
                    this.maximumIterationsModel.getNumber().intValue());
        }
        
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        // pass the enabled flag down to our contained components
        this.componentsUpdated();
    }
    
    /**
     * Refresh the enabled state of the components
     */
    private void componentsUpdated()
    {
        if(this.isEnabled())
        {
            this.useDefaultsCheckBox.setEnabled(true);
            
            boolean useDefaults = this.useDefaultsCheckBox.isSelected();
            this.convergenceToleranceLabel.setEnabled(!useDefaults);
            this.convergenceToleranceTextField.setEnabled(!useDefaults);
            this.maximumIterationsLabel.setEnabled(!useDefaults);
            this.maximumIterationsSpinner.setEnabled(!useDefaults);
        }
        else
        {
            this.useDefaultsCheckBox.setEnabled(false);
            this.convergenceToleranceLabel.setEnabled(false);
            this.convergenceToleranceTextField.setEnabled(false);
            this.maximumIterationsLabel.setEnabled(false);
            this.maximumIterationsSpinner.setEnabled(false);
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

        useDefaultsCheckBox = new javax.swing.JCheckBox();
        maximumIterationsLabel = new javax.swing.JLabel();
        maximumIterationsSpinner = new javax.swing.JSpinner();
        convergenceToleranceLabel = new javax.swing.JLabel();
        convergenceToleranceTextField = new javax.swing.JTextField();

        useDefaultsCheckBox.setSelected(true);
        useDefaultsCheckBox.setText("Use Default Convergence Parameters");
        useDefaultsCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                useDefaultsCheckBoxItemStateChanged(evt);
            }
        });

        maximumIterationsLabel.setText("Maximum Number of Iterations:");

        convergenceToleranceLabel.setText("Convergence Tolerance:");

        convergenceToleranceTextField.setText("1e-4");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(useDefaultsCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(maximumIterationsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(maximumIterationsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(convergenceToleranceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(convergenceToleranceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(useDefaultsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(maximumIterationsLabel)
                    .add(maximumIterationsSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(convergenceToleranceLabel)
                    .add(convergenceToleranceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void useDefaultsCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_useDefaultsCheckBoxItemStateChanged
        this.convergenceParametersUpdated();
        this.componentsUpdated();
    }//GEN-LAST:event_useDefaultsCheckBoxItemStateChanged
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel convergenceToleranceLabel;
    private javax.swing.JTextField convergenceToleranceTextField;
    private javax.swing.JLabel maximumIterationsLabel;
    private javax.swing.JSpinner maximumIterationsSpinner;
    private javax.swing.JCheckBox useDefaultsCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Validate the data in this panel.
     * @return
     *          true if the validation is successful
     */
    public boolean validateData()
    {
        String message = null;
        
        String toleranceText = this.convergenceToleranceTextField.getText().trim();
        if(this.isEnabled() && !this.useDefaultsCheckBox.isSelected())
        {
            if(toleranceText.length() == 0)
            {
                message =
                    "Tolerance cannot be empty unless the \"use " +
                    "defaults\" checkbox is selected";
            }
            else
            {
                try
                {
                    Double.parseDouble(toleranceText);
                }
                catch(NumberFormatException ex)
                {
                    message =
                        "Could not parse tolerance \"" + toleranceText +
                        "\" as a floating point number";
                }
            }
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
     * {@inheritDoc}
     */
    @Override
    protected ScanCommandBuilder getScanCommand()
    {
        return this.scanCommand;
    }

}
