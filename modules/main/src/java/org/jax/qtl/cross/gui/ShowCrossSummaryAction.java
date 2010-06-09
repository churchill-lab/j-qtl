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

package org.jax.qtl.cross.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossSummary;
import org.jax.qtl.cross.CrossSummaryCommand;
import org.jax.util.gui.desktoporganization.Desktop;

/**
 * The action class for showing a cross summary
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowCrossSummaryAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3400330136645214200L;
    
    private final Cross cross;
    
    /**
     * Constructor
     * @param cross
     *          the cross to summarize
     */
    public ShowCrossSummaryAction(Cross cross)
    {
        super("Show Cross Summary ...");
        this.cross = cross;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        CrossSummaryCommand crossSummaryCommand =
            new CrossSummaryCommand(this.cross);
        CrossSummary crossSummary = new CrossSummary(crossSummaryCommand);
        Desktop desktop = QTL.getInstance().getDesktop();
        desktop.createInternalFrame(
                new CrossSummaryPane(crossSummary),
                "Cross Summary",
                null,
                crossSummaryCommand.getCommandText());
    }
}
