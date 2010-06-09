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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisDescription;
import org.jax.analyticgraph.graph.Tick;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;

/**
 * A special axis that can be used for plotting marker values.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MarkerAxisDescription extends AxisDescription
{
    private final Set<GeneticMarker> originalMarkers;
    
    private final List<List<GeneticMarker>> allMarkers;
    
    private final MarkerPositionManager positionManager;
    
    /**
     * Constructor
     * @param graphCoordinateConverter
     *          the graph coordinate converter to use
     * @param axisType
     *          the axis type
     * @param axisName
     *          the name to use for this axis
     * @param geneticMaps
     *          the genetic maps for this axis
     * @param allMarkers
     *          includes both imputed and original markers
     */
    public MarkerAxisDescription(
            GraphCoordinateConverter graphCoordinateConverter,
            AxisType axisType,
            String axisName,
            GeneticMap[] geneticMaps,
            List<List<GeneticMarker>> allMarkers)
    {
        super(graphCoordinateConverter, axisType);
        this.setAxisName(axisName);
        this.allMarkers = allMarkers;
        this.originalMarkers = new HashSet<GeneticMarker>();
        
        Set<GeneticMarker> allMarkersSet = new HashSet<GeneticMarker>();
        
        for(List<GeneticMarker> currMarkerList: allMarkers)
        {
            allMarkersSet.addAll(currMarkerList);
        }
        
        // throw everything from the genetic maps into the original markers
        // set
        this.positionManager = new MarkerPositionManager(allMarkers);
        for(GeneticMap currGeneticMap: geneticMaps)
        {
            this.originalMarkers.addAll(currGeneticMap.getMarkerPositions());
        }
        
        // if it wasn't in "allMarkers" we need to remove it from "original"
        this.originalMarkers.retainAll(allMarkersSet);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tick> getTicks()
    {
        List<Tick> ticks = new ArrayList<Tick>();
        for(List<GeneticMarker> currMarkers: this.allMarkers)
        {
            GeneticMarker firstMarker = currMarkers.get(0);
            double currChromoStartPosition =
                this.positionManager.getChromosomeStartingPositionInGraphUnits(
                        firstMarker.getChromosomeName());
            double currChromoExtent =
                this.positionManager.getChromosomeExtentInGraphUnits(
                        firstMarker.getChromosomeName());
            double currChromoTickPosition =
                currChromoStartPosition +
                (currChromoExtent / 2.0);
            
            // Add a tick per chromosome
            ticks.add(new Tick(
                    currChromoTickPosition,
                    0,
                    firstMarker.getChromosomeName()));
            
            for(GeneticMarker geneticMarker: currMarkers)
            {
                double currMarkerTickPosition =
                    this.positionManager.getMarkerPositionInGraphUnits(
                            geneticMarker);
                if(this.originalMarkers.contains(geneticMarker))
                {
                    // it's original... make it bigger
                    ticks.add(new Tick(
                            currMarkerTickPosition,
                            Tick.DEFAULT_MAJOR_TICK_SIZE_PIXELS,
                            null,
                            false,
                            false));
                }
                else
                {
                    // it's imputed... make it smaller
                    ticks.add(new Tick(
                            currMarkerTickPosition,
                            Tick.DEFAULT_MAJOR_TICK_SIZE_PIXELS / 2,
                            null,
                            false,
                            false));
                }
            }
        }
        
        return ticks;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowAxisScaling()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowAxisTranslation()
    {
        return false;
    }
}
