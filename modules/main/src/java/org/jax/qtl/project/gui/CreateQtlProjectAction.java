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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.action.EditPreferencesAction;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.TextWrapper;
import org.jax.util.project.gui.CreateOrRenameProjectDialog;

/**
 * Action for creating a new project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CreateQtlProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have this
     */
    private static final long serialVersionUID = -4827036834223607203L;

    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Create New Project...";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/create-project-16x16.png";
    
    /**
     * Constructor
     */
    public CreateQtlProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(EditPreferencesAction.class.getResource(
                        ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // prompt the user if they're about to lose unsaved changes
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        if(projectManager.isActiveProjectModified())
        {
            String message =
                "The current project contains unsaved modifications. Creating " +
                "a new project will cause these modifications to be lost. " +
                "Would you like to continue without saving?";
            int response = JOptionPane.showConfirmDialog(
                    QTL.getInstance().getApplicationFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Unsaved Project Modifications",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        CreateOrRenameProjectDialog dialog = new CreateOrRenameProjectDialog(
                QTL.getInstance().getApplicationFrame(),
                projectManager);
        dialog.setVisible(true);
    }
}
