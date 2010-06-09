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

import java.awt.CardLayout;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.scan.PhenotypeDistribution;
import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.qtl.scan.ScanType;
import org.jax.r.gui.RCommandEditorListener;
import org.jax.util.TextWrapper;

/**
 * The all scan panel breaks the scan functionality across a couple of panels
 * that we can toggle between using the {@link AllScanPanels#next()}
 * and {@link AllScanPanels#back()} functions. There's also a
 * {@link AllScanPanels#validateData()} function that should be used to make
 * sure that the data is valid before we "commit" the scan command.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AllScanPanels extends ScanCommandEditorPanel
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            AllScanPanels.class.getName());
    
    /**
     * every {@link java.io.Serializable} is supposed to have this
     */
    private static final long serialVersionUID = -1286763458908518025L;

    /**
     * Enumeration for the different possible inner panels that this 
     * "all" panel can show
     */
    private static enum InnerPanel
    {
        /**
         * 
         */
        CROSS_AND_CHROMOSOME,
        
        /**
         * 
         */
        COVARIATES,
        
        /**
         * 
         */
        SCAN_METHOD_AND_PARAMETERS,
        
        /**
         * 
         */
        PERMUTATIONS_AND_RESULTS
    }
    
    private volatile InnerPanel currentInnerPanel;

    private final ScanCommandBuilder scanCommand;
    private final CrossAndPhenotypesScanPanel crossAndPhenotypesScanPanel;
    private final ChromosomesAndCovariatesPanel chromosomesAndCovariatesPanel;
    private final ScanMethodAndParametersScanPanel scanMethodAndParametersScanPanel;
    private final PermutationsAndResultScanPanel permutationsAndResultScanPanel;
    
    /**
     * Constructor
     * @param parentDialog
     *          the parent frame we can use to show dialogs
     * @param scanCommand
     *          the scan command that we're editing
     * @param availableCrosses
     *          the crosses that we can select from
     * @param selectedCross
     *          the default selection
     */
    public AllScanPanels(
            JDialog parentDialog,
            ScanCommandBuilder scanCommand,
            Cross[] availableCrosses,
            Cross selectedCross)
    {
        this.scanCommand = scanCommand;
        this.crossAndPhenotypesScanPanel = new CrossAndPhenotypesScanPanel(
                parentDialog,
                scanCommand,
                availableCrosses,
                selectedCross);
        this.chromosomesAndCovariatesPanel = new ChromosomesAndCovariatesPanel(
                scanCommand);
        this.scanMethodAndParametersScanPanel = new ScanMethodAndParametersScanPanel(
                parentDialog,
                scanCommand);
        this.permutationsAndResultScanPanel = new PermutationsAndResultScanPanel(
                scanCommand);
        
        this.initComponents();
        this.postGuiInit();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addRCommandEditorListener(RCommandEditorListener editorListener)
    {
        this.crossAndPhenotypesScanPanel.addRCommandEditorListener(editorListener);
        this.chromosomesAndCovariatesPanel.addRCommandEditorListener(editorListener);
        this.scanMethodAndParametersScanPanel.addRCommandEditorListener(editorListener);
        this.permutationsAndResultScanPanel.addRCommandEditorListener(editorListener);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRCommandEditorListener(
            RCommandEditorListener editorListener)
    {
        this.crossAndPhenotypesScanPanel.removeRCommandEditorListener(editorListener);
        this.chromosomesAndCovariatesPanel.removeRCommandEditorListener(editorListener);
        this.scanMethodAndParametersScanPanel.removeRCommandEditorListener(editorListener);
        this.permutationsAndResultScanPanel.removeRCommandEditorListener(editorListener);
    }
    
    /**
     * Handle the initialization that isn't taken care of by the GUI
     * builder
     */
    private void postGuiInit()
    {
        // do some of the post GUI initialization
        this.add(
                this.crossAndPhenotypesScanPanel,
                InnerPanel.CROSS_AND_CHROMOSOME.name());
        this.add(
                this.chromosomesAndCovariatesPanel,
                InnerPanel.COVARIATES.name());
        this.add(
                this.scanMethodAndParametersScanPanel,
                InnerPanel.SCAN_METHOD_AND_PARAMETERS.name());
        this.add(
                this.permutationsAndResultScanPanel,
                InnerPanel.PERMUTATIONS_AND_RESULTS.name());
        
        this.setCurrentInnerPanel(InnerPanel.CROSS_AND_CHROMOSOME);
    }
    
    /**
     * Change the current inner panel to the given value.
     * @param currentInnerPanel
     *          the new value for the current inner panel
     */
    private void setCurrentInnerPanel(InnerPanel currentInnerPanel)
    {
        this.currentInnerPanel = currentInnerPanel;
        this.getLayout().show(this, currentInnerPanel.name());
    }
    
    /**
     * go to the next panel
     * @see #next()
     */
    public void next()
    {
        switch(this.currentInnerPanel)
        {
            case CROSS_AND_CHROMOSOME:
            {
                if(this.crossAndPhenotypesScanPanel.validateData())
                {
                    this.chromosomesAndCovariatesPanel.refreshGui();
                    this.setCurrentInnerPanel(InnerPanel.COVARIATES);
                }
            }
            break;
            
            case COVARIATES:
            {
                if(this.chromosomesAndCovariatesPanel.validateData())
                {
                    this.scanMethodAndParametersScanPanel.refreshGui();
                    this.setCurrentInnerPanel(InnerPanel.SCAN_METHOD_AND_PARAMETERS);
                    if(this.scanCommand.getPhenotypeDistribution() == PhenotypeDistribution.OTHER &&
                       this.scanCommand.getScanType() == ScanType.SCANONE)
                    {
                        // skip over scan method for "other" distribution
                        this.next();
                    }
                }
            }
            break;
            
            case SCAN_METHOD_AND_PARAMETERS:
            {
                if(this.scanMethodAndParametersScanPanel.validateData())
                {
                    this.permutationsAndResultScanPanel.refreshGui();
                    this.setCurrentInnerPanel(InnerPanel.PERMUTATIONS_AND_RESULTS);
                }
            }
            break;
            
            case PERMUTATIONS_AND_RESULTS:
            {
                throw new IllegalStateException(
                        "cannot call next when the permutations and results " +
                        "panel is active");
            }
            
            default:
            {
                // we should never reach this
                throw new IllegalStateException(
                        "unrecognized inner panel type: " +
                        this.currentInnerPanel.name());
            }
        }
    }
    
    /**
     * Determine if it is valid to invoke {@link #next()}
     * @return
     *          true iff it is valid
     */
    public boolean isNextValid()
    {
        return this.currentInnerPanel != InnerPanel.PERMUTATIONS_AND_RESULTS;
    }
    
    /**
     * Determine if it is valid to invoke {@link #back()}
     * @return
     *          true iff it is valid
     */
    public boolean isBackValid()
    {
        return this.currentInnerPanel != InnerPanel.CROSS_AND_CHROMOSOME;
    }
    
    /**
     * Determine if we're at the last panel and a "finish" is valid.
     * @return
     *          true iff it is valid
     */
    public boolean isFinishValid()
    {
        return this.currentInnerPanel == InnerPanel.PERMUTATIONS_AND_RESULTS;
    }
    
    /**
     * go back one panel
     * @see #isBackValid()
     */
    public void back()
    {
        switch(this.currentInnerPanel)
        {
            case CROSS_AND_CHROMOSOME:
            {
                throw new IllegalStateException(
                        "cannot call back when the cross & chromosome " +
                        "panel is active");
            }
            
            case COVARIATES:
            {
                this.setCurrentInnerPanel(InnerPanel.CROSS_AND_CHROMOSOME);
            }
            break;
            
            case SCAN_METHOD_AND_PARAMETERS:
            {
                this.setCurrentInnerPanel(InnerPanel.COVARIATES);
            }
            break;
            
            case PERMUTATIONS_AND_RESULTS:
            {
                this.setCurrentInnerPanel(InnerPanel.SCAN_METHOD_AND_PARAMETERS);
                if(this.scanCommand.getPhenotypeDistribution() == PhenotypeDistribution.OTHER &&
                   this.scanCommand.getScanType() == ScanType.SCANONE)
                {
                    // skip over scan method for "other" distribution
                    this.back();
                }
            }
            break;
            
            default:
            {
                // we should never reach this
                throw new IllegalStateException(
                        "unrecognized inner panel type: " +
                        this.currentInnerPanel.name());
            }
        }
    }
    
    /**
     * start the help for the current panel
     */
    public void help()
    {
        // TODO fill in help
    }
    
    /**
     * This function just casts the result of
     * {@link javax.swing.JPanel#getLayout()} to a
     * {@link java.awt.CardLayout}.
     * @return
     *          the {@link java.awt.CardLayout}
     */
    @Override
    public CardLayout getLayout()
    {
        return (CardLayout)super.getLayout();
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

        setLayout(new java.awt.CardLayout());
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Validate the data contained in the panels. The user is alerted if
     * there is something wrong with the data format
     * @return
     *          true iff the validation succeeds
     */
    public boolean validateData()
    {
        // first go through a "GUI" level validation. this should catch every
        // problem, but just in case it does not, we should to a final
        // command level validation since this is the final validation that
        // this command will go through
        boolean guiValid =
            this.crossAndPhenotypesScanPanel.validateData() &&
            this.scanMethodAndParametersScanPanel.validateData() &&
            this.permutationsAndResultScanPanel.validateData();
        if(!guiValid)
        {
            return false;
        }
        else
        {
            String message = this.scanCommand.getInvalidAssignmentCommandMessage();
            if(message != null)
            {
                LOG.severe(
                        "received a command error message that we were " +
                		"not able to catch during GUI validation: " +
                		message);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScanCommandBuilder getScanCommand()
    {
        return this.scanCommand;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
