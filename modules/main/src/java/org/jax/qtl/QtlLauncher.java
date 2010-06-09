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

package org.jax.qtl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.rintegration.RLauncher;

/**
 * The QTL launcher fills in some of the application-specific launch
 * functionality that isn't found in {@link RLauncher}.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlLauncher extends RLauncher
{
    /**
     * our logger
     */
    private static final Logger LOG =
        Logger.getLogger(QtlLauncher.class.getName());
    
    /**
     * the resource location of the zip file that contains all of the jars
     * that are used in J/qtl's classpath.
     */
    private static final String CLASSPATH_ZIP_FILE_RESOURCE =
        "/j-qtl-classpath-bundle.zip";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getApplicationMainClass()
    {
        return QTL.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClasspathZipFileResourcePath()
    {
        return CLASSPATH_ZIP_FILE_RESOURCE;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            QtlLauncher launcher = new QtlLauncher();
            launcher.launchApplication();
            System.exit(0);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Caught exception trying to launch application",
                    ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationConfigurationManager getApplicationConfigurationManager()
    {
        return QtlApplicationConfigurationManager.getInstance();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadableApplicationName()
    {
        return "J/qtl";
    }
}
