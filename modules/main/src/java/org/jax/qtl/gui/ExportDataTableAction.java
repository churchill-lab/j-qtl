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

package org.jax.qtl.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jax.qtl.QTL;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.jaxbgenerated.JQtlApplicationState;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jaxbgenerated.ObjectFactory;
import org.jax.util.TextWrapper;
import org.jax.util.io.CommaSeparatedDataTableWriter;
import org.jax.util.io.DataTable;

/**
 * Action for exporting a data table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportDataTableAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8471226090112842389L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ExportDataTableAction.class.getName());
    
    private final DataTable tableToExport;
    
    /**
     * Constructor that uses a default action text
     * @param tableToExport
     *          the table that this action exports
     */
    public ExportDataTableAction(DataTable tableToExport)
    {
        this(tableToExport, "Export Table ...");
    }

    /**
     * Constructor that uses the given action text
     * @param tableToExport
     *          the data table that we're exporting
     * @param actionText 
     *          the text to use for this export action
     */
    public ExportDataTableAction(DataTable tableToExport, String actionText)
    {
        super(actionText,
              new ImageIcon(ExportDataTableAction.class.getResource(
                      "/images/action/export-table-16x16.png")));
        this.tableToExport = tableToExport;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // use the remembered starting dir
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        JQtlApplicationState applicationState =
            configurationManager.getApplicationState();
        FileType rememberedJaxbTableDir =
            applicationState.getRecentTableExportDirectory();
        File rememberedTableDir = null;
        if(rememberedJaxbTableDir != null && rememberedJaxbTableDir.getFileName() != null)
        {
            rememberedTableDir = new File(rememberedJaxbTableDir.getFileName());
        }
        
        // select the file to save
        JFileChooser fileChooser = new JFileChooser(rememberedTableDir);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Export");
        fileChooser.setDialogTitle("Export Table to Comma-Separated File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                CommaSeparatedFileFilter.getInstance());
        fileChooser.setFileFilter(
                CommaSeparatedFileFilter.getInstance());
        int response = fileChooser.showSaveDialog(
                QTL.getInstance().getApplicationFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the extension if there isn't one
            // already
            if(!CommaSeparatedFileFilter.getInstance().accept(selectedFile))
            {
                String newFileName =
                    selectedFile.getName() + "." +
                    CommaSeparatedFileFilter.CSV_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            if(selectedFile.exists())
            {
                // ask the user if they're sure they want to overwrite
                String message =
                    "Exporting the table to " +
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
            
            CommaSeparatedDataTableWriter commaSeparatedDataTableWriter =
                new CommaSeparatedDataTableWriter();
            try
            {
                commaSeparatedDataTableWriter.writeTable(
                        this.tableToExport,
                        selectedFile);
                
                File parentDir = selectedFile.getParentFile();
                if(parentDir != null)
                {
                    // update the "recent table directory"
                    ObjectFactory objectFactory = new ObjectFactory();
                    FileType latestJaxbTableDir = objectFactory.createFileType();
                    latestJaxbTableDir.setFileName(
                            parentDir.getAbsolutePath());
                    applicationState.setRecentTableExportDirectory(
                            latestJaxbTableDir);
                }
            }
            catch(IOException ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to export data table to file: " + selectedFile,
                        ex);
            }
        }
    }
}
