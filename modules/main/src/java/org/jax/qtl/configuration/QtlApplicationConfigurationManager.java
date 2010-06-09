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

package org.jax.qtl.configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jax.qtl.jaxbgenerated.JQtlApplicationState;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.RApplicationConfiguration;
import org.jax.r.jaxbgenerated.RApplicationStateType;


/**
 * Class that takes care of managing the saving and loading of
 * the main application configuration
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlApplicationConfigurationManager extends RApplicationConfigurationManager
{
    /**
     * the default config file name to use
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = "j-qtl-config.xml";
    
    /**
     * the default file name to use for application state
     */
    private static final String DEFAULT_APPLICATION_STATE_FILE_NAME =
        "j-qtl-application-state.xml";
    
    /**
     * the default config zip resource location
     */
    private static final String DEFAULT_CONFIG_ZIP_RESOURCE = "/j-qtl-configuration.zip";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QtlApplicationConfigurationManager.class.getName());
    
    /**
     * our singleton instance
     */
    private static final QtlApplicationConfigurationManager instance;
    static
    {
        QtlApplicationConfigurationManager tempInstance = null;
        try
        {
            tempInstance = new QtlApplicationConfigurationManager(
                    JAXBContext.newInstance(
                            RApplicationConfiguration.class,
                            JQtlApplicationState.class));
        }
        catch(JAXBException ex)
        {
            LOG.log(Level.SEVERE,
                    "Failed to initialize JAXB");
        }
        finally
        {
            instance = tempInstance;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getApplicationStateFileName()
    {
        return DEFAULT_APPLICATION_STATE_FILE_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigurationFileName()
    {
        return DEFAULT_CONFIG_FILE_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigurationZipResourceName()
    {
        return DEFAULT_CONFIG_ZIP_RESOURCE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationStateType createNewApplicationState()
    {
        org.jax.qtl.jaxbgenerated.ObjectFactory objectFactory =
            new org.jax.qtl.jaxbgenerated.ObjectFactory();
        return objectFactory.createJQtlApplicationState();
    }
    
    /**
     * Getter for the application state. This is meant to hold "session"
     * information that allows us to restore the application to the state
     * that it was in before the user last closed.
     * @return
     *          the application state
     */
    @Override
    public JQtlApplicationState getApplicationState()
    {
        return (JQtlApplicationState)super.getApplicationState();
    }

    /**
     * get a default singleton instance of this class
     * @return
     *      the singleton or null if something bad and unexpected happens
     *      during initialization
     */
    public static QtlApplicationConfigurationManager getInstance()
    {
        return QtlApplicationConfigurationManager.instance;
    }
    
    /**
     * constructor. the singleton instance should be obtained through
     * {@link #getInstance()}
     */
    private QtlApplicationConfigurationManager(JAXBContext jaxbContext) throws JAXBException
    {
        super(jaxbContext);
    }
}
