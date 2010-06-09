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

package org.jax.qtl.scan.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.jax.qtl.QTL;

/**
 * Action for editing settings for a scanone graph
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditScanOneGraphSettingsAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -8738610792606229417L;
    
    private final ScanOneGraph scanOneGraph;
    
    /**
     * Constructor
     * @param scanOneGraph
     *          the scan one graph
     */
    public EditScanOneGraphSettingsAction(
            ScanOneGraph scanOneGraph)
    {
        super("Edit Graph Settings ...",
              new ImageIcon(EditScanOneGraphSettingsAction.class.getResource(
                      "/images/action/edit-form-preferences-16x16.png")));
        this.scanOneGraph = scanOneGraph;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        ScanOneSettingsDialog scanOneSettingsDialog =
            new ScanOneSettingsDialog(
                    QTL.getInstance().getApplicationFrame(),
                    this.scanOneGraph);
        scanOneSettingsDialog.setVisible(true);
    }
}
