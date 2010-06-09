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

/**
 * A QTL basket item that holds a single marker
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SingleMarkerQtlBasketItem extends QtlBasketItem
{
    private final GeneticMarker marker;

    /**
     * Constructor
     * @param marker
     *          see {@link #getMarker()}
     * @param comment
     *          see {@link #getComment()}
     */
    public SingleMarkerQtlBasketItem(GeneticMarker marker, String comment)
    {
        super(comment);
        this.marker = marker;
    }
    
    /**
     * Getter for the marker that this basket item holds
     * @return
     *          the marker
     */
    public GeneticMarker getMarker()
    {
        return this.marker;
    }
}
