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

import org.jax.util.ObjectUtil;

/**
 * Holds basic information about a marker
 */
public class GeneticMarker
{
    /**
     * @see #getMarkerName()
     */
    private final String markerName;
    
    /**
     * @see #getChromosomeName()
     */
    private final String chromosomeName;
    
    /**
     * @see #getMarkerPositionCentimorgans()
     */
    private final double markerPositionCentimorgans;
    
    /**
     * Constructor
     * @param markerName
     *          see {@link #getMarkerName()}
     * @param chromosomeName
     *          see {@link #getChromosomeName()}
     * @param markerPositionCentimorgans
     *          see {@link #getMarkerPositionCentimorgans()}
     */
    public GeneticMarker(String markerName, String chromosomeName, double markerPositionCentimorgans)
    {
        this.markerName = markerName;
        this.chromosomeName = chromosomeName;
        this.markerPositionCentimorgans = markerPositionCentimorgans;
    }
    
    /**
     * Getter for the name of this marker
     * @return
     *          the name
     */
    public String getMarkerName()
    {
        return this.markerName;
    }
    
    /**
     * Getter for the chromosome name.
     * @return
     *          the chromosome name
     */
    public String getChromosomeName()
    {
        return this.chromosomeName;
    }
    
    /**
     * Getter for the position of this marker in centimorgans (cM)
     * @return
     *          the position
     */
    public double getMarkerPositionCentimorgans()
    {
        return this.markerPositionCentimorgans;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof GeneticMarker)
        {
            GeneticMarker otherGeneticMarker = (GeneticMarker)obj;
            return
                    ObjectUtil.areEqual(
                            this.markerName,
                            otherGeneticMarker.markerName) &&
                    ObjectUtil.areEqual(
                            this.chromosomeName,
                            otherGeneticMarker.chromosomeName);
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
        return ObjectUtil.hashObject(this.markerName);
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
     * Enum used to specify how a string representation of a genetic marker
     * should be formatted.
     */
    public enum MarkerStringFormat
    {
        /**
         * For showing marker name and position
         */
        FULL_DESCRIPTION
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Marker Name and Position";
            }
        },
        
        /**
         * For showing only the marker name
         */
        NAME_ONLY
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Marker Name Only";
            }
        },
        
        /**
         * For showing only the marker position
         */
        POSITION_ONLY
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Position Only";
            }
        }
    }
    
    /**
     * A toString that takes a formatting enumeration
     * @param format
     *          the formating enumeration
     * @return
     *          the String representation
     */
    public String toString(MarkerStringFormat format)
    {
        switch(format)
        {
            case FULL_DESCRIPTION:
            {
                return this.getMarkerName() +
                       "(" + this.toString(MarkerStringFormat.POSITION_ONLY) + ")";
            }
            
            case NAME_ONLY:
            {
                return this.getMarkerName();
            }
            
            case POSITION_ONLY:
            {
                if(this.chromosomeName == null)
                {
                    return this.markerPositionCentimorgans + " cM";
                }
                else
                {
                    return
                    "Chr" + this.chromosomeName + "@" +
                    this.markerPositionCentimorgans + " cM";
                }
            }
            
            default:
            {
                throw new IllegalArgumentException(
                        "Unknown marker format type: " + format);
            }
        }
    }
}