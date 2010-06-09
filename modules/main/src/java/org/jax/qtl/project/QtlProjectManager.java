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

package org.jax.qtl.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jax.qtl.jaxbgenerated.JQtlProjectMetadata;
import org.jax.r.CleanEnvironmentCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.ConfigurationUtilities;
import org.jax.util.io.FileChooserExtensionFilter;
import org.jax.util.io.FileUtilities;
import org.jax.util.project.Project;
import org.jax.util.project.ProjectManager;

/**
 * The QLT project manager
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlProjectManager extends ProjectManager
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QtlProjectManager.class.getName());
    
    /**
     * the file name that is used for project metadata
     */
    private static final String PROJECT_METADATA_FILENAME_1_0_0 =
        "project-metadata.xml";
    
    /**
     * the file name that is used for project metadata
     */
    private static final String PROJECT_METADATA_FILENAME_1_2_0 =
        "project-metadata-1.2.0.xml";
    
    /**
     * the file name that is used for R data
     */
    private static final String PROJECT_R_DATA_FILENAME =
        "qtl-data.RData";
    
    /**
     * XSLT document resource for transforming the old 1.0.0 project metadata
     * to the new 1.2.0 format
     */
    private static final String PROJECT_METADATA_1_0_0_TO_1_2_0_XSLT_RESOURCE =
        "/xml-transformation/jqtl-project-metadata_1.0.0_to_1.2.0.xslt";
    
    /**
     * the temporary directory name that we use for short-term storage of
     * project data (in the long term, project data is stored in a
     * zip file... usually with a .jqtl extension)
     */
    private static final String TEMP_PROJECT_DIR_NAME =
        "temp-proj";
    
    /**
     * the singleton instance of project manager
     */
    private static final QtlProjectManager instance = new QtlProjectManager();
    
    /**
     * the extension that we expect J/qtl project files to end with
     */
    public static final String JQTL_PROJECT_EXTENSION = "jqtl";
    
    /**
     * some user level text for the filter
     */
    public static final String FILTER_DESCRIPTION = "J/qtl Project (*.jqtl)";
    
    private static final FileChooserExtensionFilter QTL_PROJECT_FILE_FILTER =
        new FileChooserExtensionFilter(
                JQTL_PROJECT_EXTENSION,
                FILTER_DESCRIPTION);
    
    /**
     * Get the file filter for j/qtl projects
     * @return
     *          the file filter
     */
    @Override
    public FileFilter getProjectFileFilter()
    {
        return QTL_PROJECT_FILE_FILTER;
    }
    
    /**
     * Getter for the singleton instance of project manager
     * @return
     *          the singleton
     */
    public static QtlProjectManager getInstance()
    {
        return QtlProjectManager.instance;
    }
    
    /**
     * The R interface that we issue commands to
     */
    private final RInterface rInterface;
    
    /**
     * the jaxb context for marshalling and unmarshalling
     */
    private JAXBContext jaxbContext;
    
    /**
     * Private constructor. Use {@link #getInstance()} to get a handle
     * on the singleton instance of this class
     */
    private QtlProjectManager()
    {
        this.rInterface = RInterfaceFactory.getRInterfaceInstance();
        
        try
        {
            this.jaxbContext = JAXBContext.newInstance(
                    JQtlProjectMetadata.class);
        }
        catch(JAXBException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to initialize project manager",
                    ex);
        }
        
        this.createNewActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Project createNewActiveProject()
    {
        // clear the current r data
        this.rInterface.evaluateCommand(new SilentRCommand(
                "rm(list=ls())"));
        
        this.setActiveProjectFile(null);
        this.setActiveProjectModified(false);
        QtlProject newProject = new QtlProject(this.rInterface);
        this.setActiveProject(newProject);
        
        return newProject;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadActiveProject(File projectFile)
    {
        try
        {
            File tempProjDir = this.getCleanedTempProjectDir();
            if(tempProjDir == null)
            {
                return false;
            }
            else
            {
                try
                {
                    // expand project file to temp dir
                    ZipInputStream zipIn = new ZipInputStream(
                            new FileInputStream(projectFile));
                    FileUtilities.unzipToDirectory(
                            zipIn,
                            tempProjDir);
                    
                    // clear the current r data
                    this.rInterface.evaluateCommand(new SilentRCommand(
                            "rm(list=ls())"));
                    
                    // load the r data
                    File rDataFile = new File(tempProjDir, PROJECT_R_DATA_FILENAME);
                    this.rInterface.evaluateCommandNoReturn(new SilentRCommand(
                            new CleanEnvironmentCommand()));
                    String loadDataCommandString =
                        "load(" +
                        RUtilities.javaStringToRString(rDataFile.getAbsolutePath()) +
                        ")";
                    this.rInterface.evaluateCommand(new SilentRCommand(
                            loadDataCommandString));
                    
                    // load the meta data
                    InputStream configFileIn = this.getProjectMetadataInputStreamFromDir(
                            tempProjDir);
                    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
                    JQtlProjectMetadata jaxbProjectMetatata =
                        (JQtlProjectMetadata)unmarshaller.unmarshal(configFileIn);
                    
                    // create the project
                    QtlProject newProject = new QtlProject(
                            this.rInterface,
                            jaxbProjectMetatata);
                    
                    // update and notify
                    this.setActiveProjectFile(projectFile);
                    this.setActiveProjectModified(false);
                    this.setActiveProject(newProject);
                }
                finally
                {
                    // blow away the temp dir
                    FileUtilities.recursiveDelete(tempProjDir);
                }
                
                return true;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception loading project data",
                    ex);
            return false;
        }
    }

    private InputStream getProjectMetadataInputStreamFromDir(File projDir)
    throws IOException, TransformerFactoryConfigurationError, TransformerException
    {
        File projMetadataFile_1_2_0 = new File(
                projDir,
                PROJECT_METADATA_FILENAME_1_2_0);
        if(projMetadataFile_1_2_0.exists())
        {
            LOG.fine("Found 1.2.0 project metadata");
            return new FileInputStream(projMetadataFile_1_2_0);
        }
        else
        {
            LOG.fine("Transforming 1.0.0 project metadata");
            File projMetadataFile_1_0_0 = new File(
                    projDir,
                    PROJECT_METADATA_FILENAME_1_0_0);
            
            StreamSource xsltSource = new StreamSource(
                    QtlProjectManager.class.getResourceAsStream(
                            PROJECT_METADATA_1_0_0_TO_1_2_0_XSLT_RESOURCE));
            Transformer transformer =
                TransformerFactory.newInstance().newTransformer(xsltSource);
            
            ByteArrayOutputStream transformedOutput = new ByteArrayOutputStream();
            transformer.transform(
                    new StreamSource(projMetadataFile_1_0_0),
                    new StreamResult(transformedOutput));
            return new ByteArrayInputStream(transformedOutput.toByteArray());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveActiveProject(File projectFile)
    {
        try
        {
            File tempProjDir = this.getCleanedTempProjectDir();
            if(tempProjDir == null)
            {
                return false;
            }
            else
            {
                try
                {
                    // create temp r data file
                    File rDataFile = new File(tempProjDir, PROJECT_R_DATA_FILENAME);
                    String saveDataCommandString =
                        "save(list = ls(), file = " +
                        RUtilities.javaStringToRString(rDataFile.getAbsolutePath()) +
                        ")";
                    this.rInterface.evaluateCommand(
                            new SilentRCommand(saveDataCommandString));
                    
                    // create temp metadata file
                    FileOutputStream configFileOut = new FileOutputStream(
                            new File(tempProjDir, PROJECT_METADATA_FILENAME_1_2_0));
                    Marshaller marshaller = this.jaxbContext.createMarshaller();
                    marshaller.setProperty(
                            Marshaller.JAXB_FORMATTED_OUTPUT,
                            Boolean.TRUE);
                    marshaller.marshal(
                            this.getActiveProject().getMetadata(),
                            configFileOut);
                    configFileOut.close();
                    
                    // zip up the directory and save it to the file
                    ZipOutputStream zipOut = new ZipOutputStream(
                            new FileOutputStream(projectFile));
                    FileUtilities.compressDirectoryToZip(
                            tempProjDir,
                            zipOut);
                    zipOut.close();
                    
                    // update and notify
                    this.setActiveProjectFile(projectFile);
                    this.setActiveProjectModified(false);
                }
                finally
                {
                    // blow away the temp dir
                    FileUtilities.recursiveDelete(tempProjDir);
                }
                
                return true;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "caught exception saving project data",
                    ex);
            return false;
        }
    }
    
    /**
     * Get a clean version of the temporary project directory.
     * @return
     *          return the project directory
     */
    private File getCleanedTempProjectDir()
    {
        try
        {
            ConfigurationUtilities configurationUtilities =
                new ConfigurationUtilities();
            File configDir = configurationUtilities.getBaseDirectory();
            File tempProjDir = new File(configDir, TEMP_PROJECT_DIR_NAME);
            if(tempProjDir.exists())
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(
                            "Temporary project directory already exists: " +
                            tempProjDir);
                }
                
                if(!FileUtilities.recursiveDelete(tempProjDir))
                {
                    return null;
                }
            }
            
            if(tempProjDir.mkdir())
            {
                return tempProjDir;
            }
            else
            {
                LOG.warning(
                        "Failed to create temporary project directory");
                return null;
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to clean temporary project directory",
                    ex);
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QtlProject getActiveProject()
    {
        return (QtlProject)super.getActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProjectDataStructures()
    {
        this.getActiveProject().getDataModel().updateAll();
    }
}
