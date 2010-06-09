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

package org.jax.qtl.scan;

import org.jax.qtl.cross.GeneticMarker;

/**
 * Holds significance information for a marker
 */
public class ScanOneMarkerSignificanceValues
{
    private final GeneticMarker marker;
    
    private final double lodScore;
    
    /**
     * Constructor
     * @param marker
     *          see {@link #getMarker()}
     * @param lodScore
     *          see {@link #getLodScore()}
     */
    public ScanOneMarkerSignificanceValues(
            GeneticMarker marker,
            double lodScore)
    {
        this.marker = marker;
        this.lodScore = lodScore;
    }
    
    /**
     * Getter for the marker
     * @return
     *          the marker
     */
    public GeneticMarker getMarker()
    {
        return this.marker;
    }
    
    /**
     * Get the LOD score
     * @return
     *          the LOD score
     */
    public double getLodScore()
    {
        return this.lodScore;
    }
}