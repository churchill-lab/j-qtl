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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.ui.PreferencesDialog;

/**
 * The action for editing preferences
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditPreferencesAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3347641962865352198L;
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/edit-preferences-16x16.png";
    
    /**
     * Constructor
     */
    public EditPreferencesAction()
    {
        super("Edit Preferences...",
              new ImageIcon(EditPreferencesAction.class.getResource(
                      ICON_RESOURCE_LOCATION)));
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        final PreferencesDialog preferencesDialog =
            new PreferencesDialog(QTL.getInstance().getApplicationFrame());
        preferencesDialog.pack();
        
        SwingUtilities.invokeLater(
            new Runnable()
            {
                public void run()
                {
                    preferencesDialog.setVisible(true);
                }
            });
    }
}
