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
import java.io.File;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.TextWrapper;

/**
 * An action for launching a recently opened QTL project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadRecentQtlProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 36996041493958443L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            LoadRecentQtlProjectAction.class.getName());
    
    private final File qtlProjectFile;

    /**
     * Constructor
     * @param index
     *          the file index (used for the label)
     * @param qtlProjectFile
     *          the project file
     */
    public LoadRecentQtlProjectAction(
            int index,
            File qtlProjectFile)
    {
        super((index + 1) + ". " + qtlProjectFile.getName());
        
        this.qtlProjectFile = qtlProjectFile;
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
                "The current project contains unsaved modifications. Loading " +
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
        
        if(!projectManager.loadActiveProject(this.qtlProjectFile))
        {
            // there was a problem... tell the user
            String message =
                "Failed to load selected J/qtl project file: " +
                this.qtlProjectFile.getAbsolutePath();
            LOG.info(message);
            
            JOptionPane.showMessageDialog(
                    QTL.getInstance().getApplicationFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Error Loading Project",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
