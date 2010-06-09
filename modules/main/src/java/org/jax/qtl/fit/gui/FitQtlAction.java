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
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.fit.FitQtlCommand;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Perform a "fitqtl"
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitQtlAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4886831184621793808L;

    private final QtlBasket qtlBasket;
    
    private final ActionListener fitApprovedListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            FitQtlDialog fitDialog = (FitQtlDialog)e.getSource();
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            FitQtlCommand fitCommand = fitDialog.getFitQtlCommand();
            rInterface.evaluateCommand(fitCommand);
            QtlProjectManager.getInstance().notifyActiveProjectModified();
        }
    };

    private final ProjectChangeListener projectChangeListener = new ProjectChangeListener()
    {
        public void projectChangeOccurred(ProjectManager projectManager)
        {
            FitQtlAction.this.projectChanged();
        }
    };
    
    /**
     * Constructor
     * @param qtlBasket
     *          the qtl basket to use for fir
     */
    public FitQtlAction(QtlBasket qtlBasket)
    {
        super("Fit QTL ...");
        this.qtlBasket = qtlBasket;
        
        if(qtlBasket == null)
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
                FitQtlAction.this.setEnabled(crosses.length >= 1);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        QtlBasket qtlBasket = this.qtlBasket;
        if(qtlBasket == null)
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            qtlBasket = projectTree.getSelectedQtlBasket();
        }
        
        final FitQtlDialog fitQtlDialog =
            new FitQtlDialog(QTL.getInstance().getApplicationFrame(), qtlBasket);
        fitQtlDialog.pack();
        fitQtlDialog.addActionListener(this.fitApprovedListener);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fitQtlDialog.setVisible(true);
            }
        });
    }
}
