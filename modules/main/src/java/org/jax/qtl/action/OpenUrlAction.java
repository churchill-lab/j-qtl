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

package org.jax.qtl.action;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * Action for opening URLs
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class OpenUrlAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1957501593384571267L;

    private static final Logger LOG = Logger.getLogger(
            OpenUrlAction.class.getName());
    
    private final String url;

    /**
     * An action for opening the given URL
     * @param text
     *          the user readable text to use
     * @param url
     *          the URL to visit
     */
    public OpenUrlAction(
            String text,
            String url)
    {
        super(text,
              new ImageIcon(OpenUrlAction.class.getResource(
                      "/images/internet-16x16.png")));
        this.url = url;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            // launch a browser with the appropriate URL
            BrowserLauncher browserLauncher = new BrowserLauncher();
            browserLauncher.openURLinBrowser(this.url);
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to launch URL: " + this.url,
                    ex);
        }
    }
}
