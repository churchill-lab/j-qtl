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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.jaxbgenerated.JQtlApplicationState;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.util.TextWrapper;

/**
 * An action to save the current J/qtl 
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveQtlProjectAsAction extends AbstractSaveQtlProjectAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5132711529579577251L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveQtlProjectAsAction.class.getName());
    
    private static final String ACTION_NAME = "Save Project As...";
    
    /**
     * Constructor
     */
    public SaveQtlProjectAsAction()
    {
        super(ACTION_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void performSave()
    {
        SaveQtlProjectAsAction.saveProjectAs();
    }
    
    /**
     * Do the save as...
     */
    public static void saveProjectAs()
    {
        // try to be smart about the file dialogs starting file
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        File activeProjFile = projectManager.getActiveProjectFile();
        
        if(activeProjFile == null)
        {
            QtlApplicationConfigurationManager configurationManager =
                QtlApplicationConfigurationManager.getInstance();
            JQtlApplicationState applicationState =
                configurationManager.getApplicationState();
            
            FileType[] recentProjects =
                    applicationState.getRecentProjectFile().toArray(
                            new FileType[0]);
            if(recentProjects.length > 0)
            {
                activeProjFile = new File(recentProjects[0].getFileName()).getParentFile();
            }
        }
        
        // select the project file to save
        JFileChooser fileChooser = new JFileChooser(activeProjFile);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Save J/qtl Project");
        fileChooser.setDialogTitle("Save J/qtl Project");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileFilter(
                projectManager.getProjectFileFilter());
        int response = fileChooser.showSaveDialog(QTL.getInstance().getApplicationFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the J/qtl project extension if there isn't one
            // already
            String dotJqtl = "." + QtlProjectManager.JQTL_PROJECT_EXTENSION;
            if(!selectedFile.exists() && !selectedFile.getName().toLowerCase().endsWith(dotJqtl))
            {
                String newFileName =
                    selectedFile.getName() + "." +
                    QtlProjectManager.JQTL_PROJECT_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            if(selectedFile.exists() && !selectedFile.equals(activeProjFile))
            {
                // ask the user if they're sure they want to overwrite
                String message =
                    "Saving the current J/qtl project to " +
                    selectedFile.getAbsolutePath() + " will overwrite an " +
                    " existing file. Would you like to continue anyway?";
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(message);
                }
                
                int overwriteResponse = JOptionPane.showConfirmDialog(
                        QTL.getInstance().getApplicationFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Overwriting Existing File",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(overwriteResponse != JOptionPane.OK_OPTION)
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("overwrite canceled");
                    }
                    return;
                }
            }
            
            if(!projectManager.saveActiveProject(selectedFile))
            {
                // there was a problem... tell the user
                String message =
                    "Failed to save to selected J/qtl project file: " +
                    selectedFile.getAbsolutePath();
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
