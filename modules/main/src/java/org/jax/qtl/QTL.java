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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.desktopOrganization.QtlMenubar;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.r.gui.ApplicationFrame;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.r.rintegration.VersionStringComparator;
import org.jax.util.TextWrapper;
import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.TypeSafeSystemProperties.OsFamily;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectManager;
import org.rosuda.JRI.REXP;

/**
 * The main application class for J/qtl
 * @see QtlLauncher
 * @author Lei Wu, Keith Sheppard
 */
public class QTL
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QTL.class.getName());
    private static final String MINIMUM_RQTL_VERSION = "1.08";
    private static QTL instance;
    
    private final Desktop desktop;
    private final QtlMenubar menubar;

    private final ApplicationFrame applicationFrame;
    
    /**
     * @see #getProjectTree()
     */
    private final QtlProjectTree projectTree;
    
    /**
     * Getter for the singleton QTL instance
     * @return
     *          the singleton instance
     */
    public static QTL getInstance()
    {
        // we have 2 if's so that we don't need to waste time synchronizing
        // unless it's needed
        if(QTL.instance == null)
        {
            synchronized(QTL.class)
            {
                if(QTL.instance == null)
                {
                    QTL.instance = new QTL();
                }
            }
        }
        return QTL.instance;
    }
    
    /**
     *  constructor for initial screen
     */
    private QTL()
    {
        this.desktop = new Desktop();
        this.menubar = new QtlMenubar(this.desktop.getWindowMenu());
        
        this.projectTree = new QtlProjectTree();
        this.projectTree.setProjectManager(QtlProjectManager.getInstance());
        
        this.applicationFrame = new ApplicationFrame(
                "J/qtl - GUI for R/qtl ",
                RInterfaceFactory.getRInterfaceInstance(),
                this.menubar,
                this.desktop,
                this.projectTree,
                QtlProjectManager.getInstance());
        
        this.showGui();
        this.initialCallsToR();
        
        final QtlProjectManager projectManager = QtlProjectManager.getInstance();
        projectManager.addPropertyChangeListener(
                ProjectManager.ACTIVE_PROJECT_PROPERTY_NAME,
                new PropertyChangeListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        QTL.this.getDesktop().closeAllWindows();
                    }
                });
        
        projectManager.addPropertyChangeListener(
                ProjectManager.ACTIVE_PROJECT_FILE_PROPERTY_NAME,
                new PropertyChangeListener()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        QtlApplicationConfigurationManager.getInstance().notifyActiveProjectFileChanged(
                                projectManager.getActiveProjectFile());
                    }
                });
        
        // Show tool tips immediately
        ToolTipManager.sharedInstance().setInitialDelay(0);
        // Keep the tool tip showing through the whole program
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }
    
    /**
     * Getter for the main application frame
     * @return
     *          the application frame
     */
    public ApplicationFrame getApplicationFrame()
    {
        return this.applicationFrame;
    }

    /**
     * Getter for the menu bar
     * @return
     *          the menu bar
     */
    public QtlMenubar getMenubar()
    {
        return this.menubar;
    }

    /**
     * Getter for the desktop
     * @return
     *          the desktop
     */
    public Desktop getDesktop()
    {
        return this.desktop;
    }

    /**
     * Getter for the project tree
     * @return
     *          the project tree
     */
    public QtlProjectTree getProjectTree()
    {
        return this.projectTree;
    }

    /**
     * Execute some initialization calls to R.
     */
    private void initialCallsToR()
    {
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        configurationManager.setSaveOnExit(true);
        Long rMemLimitMB =
            configurationManager.getApplicationConfiguration().getRMemoryLimitMegabytes();
        
        // intial calls to R
        final RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        if(rMemLimitMB != null &&
           TypeSafeSystemProperties.getOsFamily() == OsFamily.WINDOWS_OS_FAMILY)
        {
            String comment =
                "Setting R memory ceiling " +
                "(you can change this in the preferences panel)";
            rInterface.insertComment(comment);
            rInterface.evaluateCommandNoReturn(
                    "memory.limit(" + rMemLimitMB.intValue() + ")");
        }
        final String qtlVersionString = this.loadRQtlAndGetVersionString();
        
        // TODO need a way of making sure this stuff doesn't get into the script
        if(qtlVersionString == null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    String message =
                        "It appears that the required library R/qtl is not " +
                        "installed. Would you like me to try installing R/qtl via " +
                        "the \"install.packages(...)\" command?";
                    String[] wrappedMessage = TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT);
                    int response = JOptionPane.showConfirmDialog(
                            QTL.this.applicationFrame,
                            wrappedMessage,
                            "R/qtl Not Installed",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if(response == JOptionPane.YES_OPTION)
                    {
                        // this is long running so we shouldn't do it in the
                        // AWT thread
                        Thread installThread = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                // install then validate
                                QTL.this.installRQtl();
                                QTL.this.runFinalRQtlInstallCheck();
                            }
                        });
                        installThread.start();
                    }
                    else
                    {
                        QTL.this.getApplicationFrame().closeApplication();
                    }
                }
            });
        }
        else
        {
            if(!this.isRQtlVersionValid(qtlVersionString))
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        String message =
                            "The installed version of R/qtl appears to be older " +
                            "than the minimum required version (" +
                            MINIMUM_RQTL_VERSION + "). Would you like J/qtl to " +
                            "attempt to replace R/qtl " + qtlVersionString +
                            " with an updated version?";
                        String[] wrappedMessage = TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT);
                        int response = JOptionPane.showConfirmDialog(
                                QTL.this.applicationFrame,
                                wrappedMessage,
                                "Outdated R/qtl Version",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if(response == JOptionPane.YES_OPTION)
                        {
                            // this is long running so we shouldn't do it in the
                            // AWT thread
                            Thread installThread = new Thread(new Runnable()
                            {
                                public void run()
                                {
                                    // remove, install then validate
                                    rInterface.insertComment(
                                            "removing outdated r/QTL version");
                                    rInterface.evaluateCommandNoReturn(
                                            "detach(\"qtl\")");
                                    rInterface.evaluateCommand(
                                            "remove.packages(\"qtl\")");
                                    QTL.this.installRQtl();
                                    QTL.this.runFinalRQtlInstallCheck();
                                }
                            });
                            installThread.start();
                        }
                        else
                        {
                            QTL.this.getApplicationFrame().closeApplication();
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Check if the R/qtl install is valid and alert the user if it is not.
     * It is assumed at this point that we have already tried to repair the
     * installation and will not try again.
     */
    private void runFinalRQtlInstallCheck()
    {
        String qtlVersionString = this.loadRQtlAndGetVersionString();
        String errorMessage = null;
        if(qtlVersionString == null)
        {
            errorMessage = "Attempted installation of R/qtl failed.";
        }
        else
        {
            if(!this.isRQtlVersionValid(qtlVersionString))
            {
                errorMessage =
                    "Failed to install a recent enough version of R/qtl. " +
                    "The installed version is " + qtlVersionString +
                    " but J/qtl requires at least version " +
                    MINIMUM_RQTL_VERSION + ".";
            }
        }
        
        if(errorMessage != null)
        {
            // the "final" is so that we can pass this to the runnable using
            // closure
            final String finalErrorMessage = errorMessage;
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    String[] wrappedErrorMessage = TextWrapper.wrapText(
                            finalErrorMessage,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT);
                    JOptionPane.showMessageDialog(
                            QTL.this.applicationFrame,
                            wrappedErrorMessage,
                            "R/qtl Installation Failure",
                            JOptionPane.ERROR_MESSAGE);
                    QTL.this.getApplicationFrame().closeApplication();
                }
            });
            LOG.severe(errorMessage);
        }
    }

    /**
     * Determine if the given version is acceptable.
     * @param rQtlVersion
     *          the R/qtl version to check
     * @return
     *          true iff the version is good
     */
    private boolean isRQtlVersionValid(String rQtlVersion)
    {
        VersionStringComparator versionComparator =
            VersionStringComparator.getInstance();
        
        return versionComparator.compare(rQtlVersion, MINIMUM_RQTL_VERSION) >= 0;
    }
    
    /**
     * Install R/qtl using "install.packages"
     */
    private void installRQtl()
    {
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        rInterface.insertComment(
                "installing required library R/qtl");
        rInterface.evaluateCommandNoReturn(
                "install.packages(pkgs=\"qtl\", repos=\"http://cran.r-project.org\")");
    }
    
    /**
     * Load R/qtl and return the version string
     * @return
     *          the version string or null if the load fails
     */
    private String loadRQtlAndGetVersionString()
    {
        final String qtlVersionCommand = "qtlversion()";
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        rInterface.insertComment("load R/qtl library");
        rInterface.evaluateCommandNoReturn("library(qtl)");
        rInterface.insertComment("current R/qtl version");
        
        // this one is just so that the user can see the output. the
        // next one is for real
        rInterface.evaluateCommandNoReturn(
                qtlVersionCommand);
        REXP qtlVersionExpression = rInterface.evaluateCommand(
                new SilentRCommand(qtlVersionCommand));
        
        // validate the expression and return it as a string or null
        if(qtlVersionExpression == null)
        {
            return null;
        }
        else
        {
            String qtlVersionString = qtlVersionExpression.asString();
            if(qtlVersionString == null || qtlVersionString.contains("Error"))
            {
                return null;
            }
            else
            {
                return qtlVersionString;
            }
        }
    }

    /**
     * Create the GUI and show it.
     */
    private void showGui()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                QTL.this.applicationFrame.setVisible(true);
            }
        });
    }

    /**
     * The entry point for the J/QTL application.
     * @param args
     *      command line arguments
     */
    public static void main (String[] args)
    {
        // set the look and feel
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                        UIManager.setLookAndFeel("net.sourceforge.napkinlaf.NapkinLookAndFeel");
                    }
                    catch(Exception ex)
                    {
                        LOG.log(Level.WARNING, "failed to set system look-and-feel", ex);
                    }
                }
            });
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "caught exception trying to set look-and-feel",
                    ex);
        }

        QTL.writeSystemConfigurationToLog();
        QTL.getInstance();
    }

    private static int BYTES_PER_MEGABYTE = (1<<20);
    
    /**
     * Log some system configuration information
     */
    private static void writeSystemConfigurationToLog()
    {
        if(LOG.isLoggable(Level.FINE))
        {
            long maxMemoryBytes = Runtime.getRuntime().maxMemory();
            if(maxMemoryBytes % BYTES_PER_MEGABYTE == 0)
            {
                // memory allocation is divisible by megabytes
                LOG.fine(
                        "Maximum Java Memory: " +
                        (Runtime.getRuntime().maxMemory() / (BYTES_PER_MEGABYTE)) +
                        "MB");
            }
            else
            {
                LOG.fine(
                        "Maximum Java Memory: " +
                        Runtime.getRuntime().maxMemory() +
                        " bytes");
            }
            
            LOG.fine(
                    "java.library.path=" +
                    TypeSafeSystemProperties.getJavaLibraryPath());
            LOG.fine("Environment Variables:");
            for(Map.Entry<String, String> currEnvEntry: System.getenv().entrySet())
            {
                LOG.fine(currEnvEntry.getKey() + "=" + currEnvEntry.getValue());
            }
        }
    }
}
