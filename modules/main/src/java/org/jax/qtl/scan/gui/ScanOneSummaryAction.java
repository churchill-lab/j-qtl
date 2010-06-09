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
import org.jax.qtl.scan.ScanOneResult;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * The action for displaying a scanone summary
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneSummaryAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7521049728179234755L;
    
    /**
     * the results that this scan summary should summarize
     */
    private final ScanOneResult resultToSummarize;

    /**
     * Constructor
     */
    public ScanOneSummaryAction()
    {
        this(null);
    }
    
    /**
     * Constructor
     * @param resultToSummarize
     *          the result to summarize. if null then we use the selected
     *          result from the {@link QtlProjectTree}
     */
    public ScanOneSummaryAction(
            ScanOneResult resultToSummarize)
    {
        super("Show One QTL Scan Results ...");
        
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
        ScanOneResult resultToSummarize = this.resultToSummarize;
        if(resultToSummarize == null)
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            Cross selectedCross = projectTree.getSelectedCross();
            
            QtlDataModel dataModel =
                QtlProjectManager.getInstance().getActiveProject().getDataModel();
            Cross[] availableCrosses = dataModel.getCrosses(); 
            
            final SelectScanOneResultsDialog selectScanResultsDialog =
                new SelectScanOneResultsDialog(
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
                    final ScanOneResult selectedScanOneResult =
                        selectScanResultsDialog.getSelectedScanOneResult();
                    if(selectedScanOneResult != null)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                ScanOneSummaryAction.showSummary(
                                        selectedScanOneResult);
                            }
                        });
                    }
                }
            };
            waitForResultsSelectionThread.start();
        }
        else
        {
            ScanOneSummaryAction.showSummary(resultToSummarize);
        }
    }
    
    /**
     * Show a summary for the given result
     * @param resultToSummarize
     *          the result to summarize
     */
    private static void showSummary(
            ScanOneResult resultToSummarize)
    {
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        
        Desktop desktop = QTL.getInstance().getDesktop();
        ScanOneSummaryPanel summaryPanel =
            new ScanOneSummaryPanel(
                    activeProject.getDataModel().getCrosses(),
                    resultToSummarize);
        desktop.createInternalFrame(
                summaryPanel,
                "Scan One Summary",
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
        final boolean anyScanoneResults =
            ScanOneResult.anyScanoneResultsExist(dataModel);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ScanOneSummaryAction.this.setEnabled(anyScanoneResults);
            }
        });
    }
}
