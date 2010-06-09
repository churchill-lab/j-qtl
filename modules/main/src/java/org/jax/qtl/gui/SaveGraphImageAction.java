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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
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
import org.jax.util.io.PngFileFilter;

/**
 * This action saves a panel to an image file
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveGraphImageAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3484323141461581352L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveGraphImageAction.class.getName());
    
    private final Component componentToSaveAsImage;

    /**
     * Constructor
     * @param componentToSaveAsImage
     *          the component we're going to save as an image
     */
    public SaveGraphImageAction(Component componentToSaveAsImage)
    {
        super("Save Graph as Image ...",
              new ImageIcon(SaveGraphImageAction.class.getResource(
                      "/images/action/export-image-16x16.png")));
        this.componentToSaveAsImage = componentToSaveAsImage; 
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
        FileType rememberedJaxbImageDir =
            applicationState.getRecentImageExportDirectory();
        File rememberedImageDir = null;
        if(rememberedJaxbImageDir != null && rememberedJaxbImageDir.getFileName() != null)
        {
            rememberedImageDir = new File(rememberedJaxbImageDir.getFileName());
        }
        
        // select the image file to save
        JFileChooser fileChooser = new JFileChooser(rememberedImageDir);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Save Graph");
        fileChooser.setDialogTitle("Save Graph as Image");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                PngFileFilter.getInstance());
        fileChooser.setFileFilter(
                PngFileFilter.getInstance());
        int response = fileChooser.showSaveDialog(
                QTL.getInstance().getApplicationFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the extension if there isn't one
            // already
            if(!PngFileFilter.getInstance().accept(selectedFile))
            {
                String newFileName =
                    selectedFile.getName() + "." +
                    PngFileFilter.PNG_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            if(selectedFile.exists())
            {
                // ask the user if they're sure they want to overwrite
                String message =
                    "Exporting the graph image to " +
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
            
            Dimension componentSize = this.componentToSaveAsImage.getSize();
            BufferedImage bufferedImage = new BufferedImage(
                    componentSize.width,
                    componentSize.height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = bufferedImage.createGraphics();
            this.componentToSaveAsImage.paint(graphics);
            try
            {
                ImageIO.write(
                        bufferedImage,
                        "png",
                        selectedFile);
                
                File parentDir = selectedFile.getParentFile();
                if(parentDir != null)
                {
                    // update the "recent image directory"
                    ObjectFactory objectFactory = new ObjectFactory();
                    FileType latestJaxbImageDir = objectFactory.createFileType();
                    latestJaxbImageDir.setFileName(
                            parentDir.getAbsolutePath());
                    applicationState.setRecentImageExportDirectory(
                            latestJaxbImageDir);
                }
            }
            catch(IOException ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to save graph image",
                        ex);
            }
        }

    }
}
