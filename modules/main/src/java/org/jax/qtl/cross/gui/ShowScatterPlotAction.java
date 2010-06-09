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

package org.jax.qtl.cross.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Action for showing a scatter plot
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowScatterPlotAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5160639711320145900L;

    /**
     * the selected cross
     */
    private final Cross selectedCross;
    
    /**
     * Constructor for creating a scatter plot action without any specific cross.
     */
    public ShowScatterPlotAction()
    {
        this(null);
    }
    
    /**
     * Create a scatter plot action for the given cross.
     * @param selectedCross
     *          the cross
     */
    public ShowScatterPlotAction(Cross selectedCross)
    {
        super("Scatter Plot ...");
        
        this.selectedCross = selectedCross;
        
        if(selectedCross == null)
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
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        QtlProject activeProject =
                            QtlProjectManager.getInstance().getActiveProject();
                        QtlDataModel dataModel =
                            activeProject.getDataModel();
                        Cross[] crosses = dataModel.getCrosses();
                        
                        Cross selectedCross = ShowScatterPlotAction.this.selectedCross;
                        if(selectedCross == null)
                        {
                            QtlProjectTree projectTree =
                                QTL.getInstance().getProjectTree();
                            selectedCross = projectTree.getSelectedCross();
                        }
                        
                        ShowScatterPlotDialog showScatterPlotDialog =
                            new ShowScatterPlotDialog(
                                    QTL.getInstance().getApplicationFrame(),
                                    selectedCross,
                                    crosses);
                        showScatterPlotDialog.setVisible(true);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(final ProjectManager projectManager)
    {
        // we shouldn't enable this unless we have some active crosses
        QtlProjectManager qtlProjectManager = (QtlProjectManager)projectManager;
        QtlDataModel dataModel =
            qtlProjectManager.getActiveProject().getDataModel();
        final boolean anyCrosses = !dataModel.getCrossMap().isEmpty();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ShowScatterPlotAction.this.setEnabled(anyCrosses);
            }
        });
    }
}
