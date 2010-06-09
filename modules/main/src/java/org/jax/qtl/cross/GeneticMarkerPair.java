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

import org.jax.qtl.cross.GeneticMarker.MarkerStringFormat;
import org.jax.util.ObjectUtil;

/**
 * A pairing of two genetic markers.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GeneticMarkerPair
{
    private final GeneticMarker markerOne;
    
    private final GeneticMarker markerTwo;

    /**
     * Constructor
     * @param markerOne
     *          the 1st marker
     * @param markerTwo
     *          the 2nd marker
     */
    public GeneticMarkerPair(GeneticMarker markerOne, GeneticMarker markerTwo)
    {
        this.markerOne = markerOne;
        this.markerTwo = markerTwo;
    }
    
    /**
     * Getter for the 1st marker
     * @return
     *          the marker
     */
    public GeneticMarker getMarkerOne()
    {
        return this.markerOne;
    }
    
    /**
     * Getter for the 2nd marker
     * @return
     *          the marker
     */
    public GeneticMarker getMarkerTwo()
    {
        return this.markerTwo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherPairObject)
    {
        if(otherPairObject instanceof GeneticMarkerPair)
        {
            GeneticMarkerPair otherPair = (GeneticMarkerPair)otherPairObject;
            return ObjectUtil.areEqual(this.markerOne, otherPair.markerOne) &&
                   ObjectUtil.areEqual(this.markerTwo, otherPair.markerTwo);
        }
        else
        {
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ObjectUtil.hashObject(this.markerOne) +
               ObjectUtil.hashObject(this.markerTwo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.toString(MarkerStringFormat.FULL_DESCRIPTION);
    }

    /**
     * A to string that takes a marker formatting argument
     * @param format
     *          the marker format to use
     * @return
     *          the string
     */
    public String toString(MarkerStringFormat format)
    {
        return this.getMarkerOne().toString(format) + ':' +
               this.getMarkerTwo().toString(format);
    }
}
