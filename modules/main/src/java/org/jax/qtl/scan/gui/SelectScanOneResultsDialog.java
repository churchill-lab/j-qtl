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

import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.util.concurrent.SettableFuture;

/**
 * A dialog for selecting scan one results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SelectScanOneResultsDialog extends SelectScanResultsDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1626399746902098676L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SelectScanOneResultsDialog.class.getName());
    
    private final SettableFuture<ScanOneResult> futureScanOneResult =
        new SettableFuture<ScanOneResult>();
    
    /**
     * Constructor
     * @param parent
     *          the parent frame to use
     * @param title
     *          the dialog title to use
     * @param selectedCross
     *          the initial cross selection
     * @param availableCrosses
     *          the crosses that are available to select from
     */
    public SelectScanOneResultsDialog(
            Frame parent,
            String title,
            Cross selectedCross,
            Cross[] availableCrosses)
    {
        super(parent, title, selectedCross, availableCrosses);
    }
    
    /**
     * Getter for the selected scan one result. This function blocks until the
     * user clicks ok or cancel. If they cancel then null is returned
     * @return
     *          the result
     */
    public ScanOneResult getSelectedScanOneResult()
    {
        try
        {
            return this.futureScanOneResult.get();
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to get future selected scan one result",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void ok()
    {
        ScanOneResult selectedScanResult =
            (ScanOneResult)this.getScanResultsList().getSelectedValue();
        try
        {
            this.futureScanOneResult.set(selectedScanResult);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to set future values on OK",
                    ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void cancel()
    {
        try
        {
            this.futureScanOneResult.set(null);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to set future values to null on cancel",
                    ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillListWithScanResults()
    {
        DefaultListModel listModel =
            (DefaultListModel)this.getScanResultsList().getModel();
        listModel.removeAllElements();
        Cross selectedCross = this.selectedCross;
        if(selectedCross != null)
        {
            for(ScanOneResult scanOneResult:
                selectedCross.getScanOneResults())
            {
                listModel.addElement(scanOneResult);
            }
        }
    }
}
