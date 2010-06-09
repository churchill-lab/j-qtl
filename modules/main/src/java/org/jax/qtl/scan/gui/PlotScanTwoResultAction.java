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

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.qtl.ui.PlotScantwoDialog;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * An action for plotting 2D QTL scan results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PlotScanTwoResultAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2017248038558694204L;
    
    /**
     * the result for us to plot
     */
    private final ScanTwoResult resultsToPlot;

    /**
     * Constructor
     */
    public PlotScanTwoResultAction()
    {
        this(null);
    }
    
    /**
     * Constructor
     * @param resultsToPlot
     *          the results of the scan
     */
    public PlotScanTwoResultAction(
            ScanTwoResult resultsToPlot)
    {
        super("Plot Two QTL Scan Results ...");
        
        this.resultsToPlot = resultsToPlot;
        
        if(resultsToPlot == null)
        {
            // add a listener to the project so that we know when to refresh
            // our updated state
            QtlProjectManager projectManager = QtlProjectManager.getInstance();
            projectManager.addProjectChangeListener(this);
            this.projectChangeOccurred(projectManager);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        new PlotScantwoDialog(this.resultsToPlot);
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(final ProjectManager projectManager)
    {
        QtlProjectManager qtlProjectManager = (QtlProjectManager)projectManager;
        QtlDataModel dataModel =
            qtlProjectManager.getActiveProject().getDataModel();
        final boolean anyScantwos =
            ScanTwoResult.anyScanTwoResultsExist(dataModel);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                PlotScanTwoResultAction.this.setEnabled(anyScantwos);
            }
        });
    }
}
