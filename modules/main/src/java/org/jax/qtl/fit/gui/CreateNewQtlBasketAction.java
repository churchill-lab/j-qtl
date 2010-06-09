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
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Action class for creating a new QTL basket
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CreateNewQtlBasketAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1918084994270622328L;
    
    private final ProjectChangeListener projectChangeListener = new ProjectChangeListener()
    {
        public void projectChangeOccurred(ProjectManager projectManager)
        {
            CreateNewQtlBasketAction.this.projectChanged();
        }
    };

    /**
     * Constructor
     */
    public CreateNewQtlBasketAction()
    {
        super("Create a New QTL Basket ...");
        
        QtlProjectManager.getInstance().addProjectChangeListener(
                this.projectChangeListener);
        this.projectChanged();
    }

    /**
     * Deal with a change in the project
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
                CreateNewQtlBasketAction.this.setEnabled(crosses.length >= 1);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        
        Cross[] availableCrosses = activeProject.getDataModel().getCrosses();
        Cross selectedCross = QTL.getInstance().getProjectTree().getSelectedCross();
        
        CreateNewQtlBasketDialog dialog = new CreateNewQtlBasketDialog(
                QTL.getInstance().getApplicationFrame(),
                selectedCross,
                availableCrosses);
        dialog.setVisible(true);
    }
}
