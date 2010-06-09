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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.jax.analyticgraph.framework.Graph2DWithAxes;
import org.jax.qtl.QTL;

/**
 * Action for editing graph settings
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditSimpleGraphSettingsAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -8738610792606229417L;
    
    private final Graph2DWithAxes graph;
    
    /**
     * Constructor
     * @param graph
     *          the graph
     */
    public EditSimpleGraphSettingsAction(
            Graph2DWithAxes graph)
    {
        super("Edit Graph Settings ...",
              new ImageIcon(EditSimpleGraphSettingsAction.class.getResource(
                      "/images/action/edit-form-preferences-16x16.png")));
        this.graph = graph;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        SimpleGraphSettingsDialog simpleGraphSettingsDialog =
            new SimpleGraphSettingsDialog(
                    QTL.getInstance().getApplicationFrame(),
                    this.graph);
        simpleGraphSettingsDialog.setVisible(true);
    }
}
