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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.jaxbgenerated.JQtlApplicationState;
import org.jax.r.jaxbgenerated.FileType;

/**
 * A menu item for recent QTL projects
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class RecentQtlProjectsMenu extends JMenu
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3937157580041137960L;
    
    /**
     * Constructor
     */
    public RecentQtlProjectsMenu()
    {
        super("Load Recent Projects");
        
        final MenuSelectionManager menuSelectionManager = MenuSelectionManager.defaultManager();
        menuSelectionManager.addChangeListener(new ChangeListener()
        {
            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e)
            {
                MenuElement[] selectedPath = menuSelectionManager.getSelectedPath();
                
                if(selectedPath.length > 0 &&
                   selectedPath[selectedPath.length - 1] == RecentQtlProjectsMenu.this)
                {
                    RecentQtlProjectsMenu.this.refreshRecentQtlProjects();
                }
            }
        });
    }
    
    /**
     * Refresh the recent projects menu items
     */
    private void refreshRecentQtlProjects()
    {
        this.removeAll();
        
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        JQtlApplicationState applicationState =
            configurationManager.getApplicationState();
        
        FileType[] recentProjects =
            applicationState.getRecentProjectFile().toArray(new FileType[0]);
        if(recentProjects.length > 0)
        {
            // list the most recent projects
            for(int i = 0; i < recentProjects.length; i++)
            {
                this.add(new LoadRecentQtlProjectAction(
                        i,
                        new File(recentProjects[i].getFileName())));
            }
        }
        else
        {
            // there are no recent projects
            JMenuItem emptyMenuItem = new JMenuItem("No Recent Projects Found");
            emptyMenuItem.setEnabled(false);
            this.add(emptyMenuItem);
        }
    }
}
