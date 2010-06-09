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

import org.jax.qtl.cross.GeneticMarker;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class OpenMgdUrlAction extends OpenUrlAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2110430120885699268L;

    /**
     * Constructor
     * @param marker
     *          the marker
     */
    public OpenMgdUrlAction(GeneticMarker marker)
    {
        super(  "Visit MGD for Chr" + marker.getChromosomeName() +
                "@" + marker.getMarkerPositionCentimorgans() + " cM",
                "http://www.informatics.jax.org/searches/linkmap.cgi?" +
                "chromosome=" + marker.getChromosomeName() + "&midpoint=" +
                marker.getMarkerPositionCentimorgans() +
                "&cmrange=1.0&dsegments=1&syntenics=0");

    }
}
