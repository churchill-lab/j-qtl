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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectManager;

/**
 * Action for saveing the qtl project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveQtlProjectAction extends AbstractSaveQtlProjectAction
{
    /**
     * every {@link java.io.Serializable} has one of these
     */
    private static final long serialVersionUID = -4435938386490315099L;

    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveQtlProjectAction.class.getName());
    
    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Save Project";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/save-16x16.png";
    
    /**
     * Constructor
     */
    public SaveQtlProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(SaveQtlProjectAction.class.getResource(
                          ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void performSave()
    {
        SaveQtlProjectAction.saveProject();
    }
    
    /**
     * Save the project
     */
    private static void saveProject()
    {
        ProjectManager projectManager = QtlProjectManager.getInstance();
        File activeProjFile = projectManager.getActiveProjectFile();
        
        if(activeProjFile == null)
        {
            // since we don't have an active file this is the same as a
            // "save as"
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "calling save as since we don't have an existing " +
                        "file name for the project");
            }
            SaveQtlProjectAsAction.saveProjectAs();
        }
        else
        {
            // try to save to the project file
            if(!projectManager.saveActiveProject(activeProjFile))
            {
                // there was a problem... tell the user
                String message =
                    "Failed to save to selected J/qtl project file: " +
                    activeProjFile.getAbsolutePath();
                LOG.info(message);
                
                JOptionPane.showMessageDialog(
                        QTL.getInstance().getApplicationFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Error Saving Project",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
