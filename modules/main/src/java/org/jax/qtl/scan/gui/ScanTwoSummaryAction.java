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

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Action for showing a scan two summary panel
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanTwoSummaryAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7578158688248408735L;
    
    /**
     * the scan results that we should summarize
     */
    private final ScanTwoResult resultToSummarize;

    /**
     * Constructor. Since no results are specified in this constructor, the
     * user will be asked to select a scan two result before we show the
     * summary
     */
    public ScanTwoSummaryAction()
    {
        this(null);
    }
    
    /**
     * Constructor
     * @param resultToSummarize 
     *          the result that this action should summarize
     */
    public ScanTwoSummaryAction(ScanTwoResult resultToSummarize)
    {
        super("Show Two QTL Scan Results ...");
        this.resultToSummarize = resultToSummarize;
        
        if(resultToSummarize == null)
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
        if(this.resultToSummarize != null)
        {
            ScanTwoSummaryAction.showSummary(this.resultToSummarize);
        }
        else
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            Cross selectedCross = projectTree.getSelectedCross();
            
            QtlDataModel dataModel =
                QtlProjectManager.getInstance().getActiveProject().getDataModel();
            Cross[] availableCrosses = dataModel.getCrosses(); 
            
            final SelectScanTwoResultsDialog selectScanResultsDialog =
                new SelectScanTwoResultsDialog(
                        QTL.getInstance().getApplicationFrame(),
                        "Select Scan Results to Display",
                        selectedCross,
                        availableCrosses);
            selectScanResultsDialog.setVisible(true);
            Thread waitForResultsSelectionThread = new Thread()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void run()
                {
                    final ScanTwoResult selectedScanTwoResult =
                        selectScanResultsDialog.getSelectedScanTwoResult();
                    if(selectedScanTwoResult != null)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                ScanTwoSummaryAction.showSummary(
                                        selectedScanTwoResult);
                            }
                        });
                    }
                }
            };
            waitForResultsSelectionThread.start();
        }
    }

    /**
     * Show the given scan results
     * @param resultToSummarize
     *          the results to show
     */
    private static void showSummary(ScanTwoResult resultToSummarize)
    {
        Desktop desktop = QTL.getInstance().getDesktop();
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        QtlDataModel dataModel = activeProject.getDataModel();
        
        ScanTwoSummaryPanel summaryPanel =
            new ScanTwoSummaryPanel(
                    dataModel.getCrosses(),
                    resultToSummarize);
        desktop.createInternalFrame(
                summaryPanel,
                "Scan Two Summary",
                null,
                resultToSummarize.getAccessorExpressionString());
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(final ProjectManager projectManager)
    {
        QtlProjectManager qtlProjectManager = (QtlProjectManager)projectManager;
        QtlDataModel dataModel =
            qtlProjectManager.getActiveProject().getDataModel();
        final boolean anyScantwoResults =
            ScanTwoResult.anyScanTwoResultsExist(dataModel);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ScanTwoSummaryAction.this.setEnabled(anyScantwoResults);
            }
        });
    }
}
