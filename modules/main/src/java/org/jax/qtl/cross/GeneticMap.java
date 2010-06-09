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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Holds genetic map information
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class GeneticMap
{
    /**
     * Comparator for sorting markers on based on thier positions
     * @see GeneticMarker#getMarkerPositionCentimorgans()
     */
    public static final Comparator<GeneticMarker> positionComparator =
        new Comparator<GeneticMarker>()
        {
            /**
             * {@inheritDoc}
             */
            public int compare(GeneticMarker position1, GeneticMarker position2)
            {
                return Double.compare(
                        position1.getMarkerPositionCentimorgans(),
                        position2.getMarkerPositionCentimorgans());
            }
        };
    
    /**
     * Map type...
     */
    public static enum MapType
    {
        /**
         * Doesn't care about sex
         */
        SEX_AGNOSTIC,
        
        /**
         * specific to males
         */
        MALE,
        
        /**
         * specific to females
         */
        FEMALE
    }
    
    private final MapType mapType;
    
    private final RObject backinRMap;

    private final String chromosomeName;
    
    /**
     * Construct a new map
     * @param chromosomeName
     *          the name of the chromosome that this map is for
     * @param mapType
     *          the type of map to construct
     * @param backingRMap
     *          the R "map" object backing this map
     */
    public GeneticMap(
            String chromosomeName,
            MapType mapType,
            RObject backingRMap)
    {
        this.chromosomeName = chromosomeName;
        this.mapType = mapType;
        this.backinRMap = backingRMap;
    }
    
    /**
     * Getter for the chromosome name
     * @return
     *          the chromosome name
     */
    public String getChromosomeName()
    {
        return this.chromosomeName;
    }
    
    /**
     * Get a list of all of the positions in this map
     * @return
     *          the list of marker positions
     */
    public List<GeneticMarker> getMarkerPositions()
    {
        switch(this.mapType)
        {
            case SEX_AGNOSTIC:
            {
                return this.getMarkerPositionsFrom(this.backinRMap);
            }

            case MALE:
            {
                RObject backingMaleMap = new RObject(
                        this.backinRMap.getRInterface(),
                        this.backinRMap.getAccessorExpressionString() + "[2,]");
                return this.getMarkerPositionsFrom(backingMaleMap);
            }

            case FEMALE:
            {
                RObject backingMaleMap = new RObject(
                        this.backinRMap.getRInterface(),
                        this.backinRMap.getAccessorExpressionString() + "[1,]");
                return this.getMarkerPositionsFrom(backingMaleMap);
            }

            default:
            {
                throw new IllegalArgumentException("unknown map type");
            }
        }
    }

    /**
     * Get marker positions using the given r object as the genetic map
     * @param backingRMap
     *          the r object for the genetic map
     */
    private List<GeneticMarker> getMarkerPositionsFrom(RObject backingRMap)
    {
        String[] names = JRIUtilityFunctions.getNames(backingRMap);
        SilentRCommand silentCommand = new SilentRCommand(
                backingRMap.getAccessorExpressionString());
        REXP markerPositionsExpression = backingRMap.getRInterface().evaluateCommand(
                silentCommand);
        double[] markerPositionsArray = markerPositionsExpression.asDoubleArray();
        
        // the sizes should be the same
        if(names.length == markerPositionsArray.length)
        {
            List<GeneticMarker> markerPositions =
                new ArrayList<GeneticMarker>(names.length);
            for(int i = 0; i < markerPositionsArray.length; i++)
            {
                markerPositions.add(new GeneticMarker(
                        names[i],
                        this.chromosomeName,
                        markerPositionsArray[i]));
            }
            
            return markerPositions;
        }
        else
        {
            throw new IllegalStateException(
                    "the number of positions don't match the number " +
                    "of names: " + names.length + " " +
                    markerPositionsArray.length);
        }
    }
    
    /**
     * Get the total extent of the given list in centimorgans
     * @param markerPositions
     *          the list of marker positions. it is assumed that these all
     *          come from the same chromosome
     * @return
     *          the total extent in centimorgans
     */
    public static double getTotalExtentOfMarkerListInCentimorgans(List<GeneticMarker> markerPositions)
    {
        if(markerPositions.size() > 1)
        {
//            double maxMarkerPosition =
//                markerPositions.get(0).getMarkerPositionCentimorgans();
//            double minMarkerPosition =
//                markerPositions.get(0).getMarkerPositionCentimorgans();
//            
//            for(GeneticMarker markerPosition: markerPositions)
//            {
//                double currMarkerPosition =
//                    markerPosition.getMarkerPositionCentimorgans();
//                if(currMarkerPosition > maxMarkerPosition)
//                {
//                    maxMarkerPosition = currMarkerPosition;
//                }
//                else if(currMarkerPosition < minMarkerPosition)
//                {
//                    minMarkerPosition = currMarkerPosition;
//                }
//            }
//            
//            return maxMarkerPosition - minMarkerPosition;
            // TOOD make sure this is a good assumption
            // assuming marker positions are given in order
            GeneticMarker initialMarker = markerPositions.get(0);
            GeneticMarker finalMarker = markerPositions.get(
                    markerPositions.size() - 1);
            return
                    finalMarker.getMarkerPositionCentimorgans() -
                    initialMarker.getMarkerPositionCentimorgans();
        }
        else
        {
            return 0.0;
        }
    }
}
