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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.fit.FitQtlResult;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Action class for showing fit results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowFitQtlResultsAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7058099015086390519L;

    private final FitQtlResult fitQtlResult;
    
    private final ProjectChangeListener projectChangeListener = new ProjectChangeListener()
    {
        public void projectChangeOccurred(ProjectManager projectManager)
        {
            ShowFitQtlResultsAction.this.projectChanged();
        }
    };
    
    /**
     * Constructor
     * @param fitQtlResult
     *          the fit qtl to show
     */
    public ShowFitQtlResultsAction(
            FitQtlResult fitQtlResult)
    {
        super("Show Fit Results ...");
        this.fitQtlResult = fitQtlResult;
        
        if(fitQtlResult == null)
        {
            QtlProjectManager.getInstance().addProjectChangeListener(
                    this.projectChangeListener);
            this.projectChanged();
        }
    }
    
    /**
     * Respond to a project change
     */
    private void projectChanged()
    {
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        final Cross[] crosses = activeProject.getDataModel().getCrosses();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                ShowFitQtlResultsAction.this.setEnabled(crosses.length >= 1);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        QtlProject project = QtlProjectManager.getInstance().getActiveProject();
        Cross[] availableCrosses =  project.getDataModel().getCrosses();
        FitQtlResultsPanel fitQtlResultsPanel = new FitQtlResultsPanel(
                availableCrosses,
                this.fitQtlResult);
        Desktop desktop = QTL.getInstance().getDesktop();
        desktop.createInternalFrame(
                fitQtlResultsPanel,
                "QTL Model Fitting Results",
                null,
                "fit-qtl-results");
    }
}
