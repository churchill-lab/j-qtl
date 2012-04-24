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

package org.jax.qtl.cross;

import org.jax.r.RCommand;

/**
 * Command for summarizing crosses
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CrossSummaryCommand implements RCommand
{
    private final Cross cross;
    
    /**
     * Constructor
     * @param cross
     *          the cross to summarize
     */
    public CrossSummaryCommand(Cross cross)
    {
        this.cross = cross;
    }
    
    /**
     * Getter for the cross
     * @return the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        return "summary(" + this.cross.getAccessorExpressionString() + ")";
    }
}
