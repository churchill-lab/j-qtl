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

package org.jax.qtl.project.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.TextWrapper;
import org.jax.util.project.gui.CreateOrRenameProjectDialog;

/**
 * base class for save and save as
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class AbstractSaveQtlProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1339574408449022572L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            AbstractSaveQtlProjectAction.class.getName());
    
    /**
     * Same as {@link AbstractAction#AbstractAction(String, Icon)}
     * @param name
     *          the name
     * @param icon
     *          the icon
     */
    public AbstractSaveQtlProjectAction(String name, Icon icon)
    {
        super(name, icon);
    }

    /**
     * Same as {@link AbstractAction#AbstractAction(String)}
     * @param name
     *          the action name
     */
    public AbstractSaveQtlProjectAction(String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        if(activeProject.getName() == null)
        {
            // call save only after the project has been named
            CreateOrRenameProjectDialog projectRenameDialog =
                new CreateOrRenameProjectDialog(
                        QTL.getInstance().getApplicationFrame(),
                        projectManager,
                        activeProject);
            projectRenameDialog.addWindowListener(
                    new WindowAdapter()
                    {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void windowClosed(WindowEvent e)
                        {
                            AbstractSaveQtlProjectAction.this.renameDialogClosed();
                        }
                    });
            projectRenameDialog.pack();
            projectRenameDialog.setVisible(true);
        }
        else
        {
            // since the project is already named, we're OK to call save
            // project directly
            this.performSave();
        }
    }
    
    /**
     * respond to a rename dialog close event
     */
    private void renameDialogClosed()
    {
        // before we perform save, we should see if the user canceled the
        // rename
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        QtlProject activeProject = projectManager.getActiveProject();
        if(activeProject.getName() == null)
        {
            // they canceled
            String message =
                "Cannot save a J/qtl project without a project name";
            LOG.info(message);
            
            JOptionPane.showMessageDialog(
                    QTL.getInstance().getApplicationFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "No Project Name",
                    JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            this.performSave();
        }
    }

    /**
     * Do a save or a save as...
     */
    protected abstract void performSave();
}
