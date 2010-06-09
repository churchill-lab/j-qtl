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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;

/**
 * Manager for getting a marker position
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MarkerPositionManager
{
    /**
     * this is the amount of the total graph space that we allocate to
     * providing some room between different chromosomes
     */
    private static final double CUMULATIVE_CHROMOSOME_SPACING = 0.1;

    private Map<GeneticMarker, Double> markerPositionMap;
    
    private Map<String, Double> chromosomeStartingPositionMap;
    
    private Map<String, Double> chromosomeStartingPositionInCentimorgansMap;
    
    private Map<String, Double> chromosomeExtentMap;
    
    private double graphDistancePerCentimorgan;
    
    /**
     * Constructor
     * @param geneticMaps
     *          the maps whose positions we're controlling
     */
    public MarkerPositionManager(
            GeneticMap[] geneticMaps)
    {
        List<List<GeneticMarker>> geneticMarkerLists =
            new ArrayList<List<GeneticMarker>>(geneticMaps.length);
        for(GeneticMap currGeneticMap: geneticMaps)
        {
            geneticMarkerLists.add(currGeneticMap.getMarkerPositions());
        }
        
        this.initializeData(geneticMarkerLists);
    }
    
    /**
     * Constructor
     * @param geneticMarkerLists
     *          the maps in list form whose positions we're controlling
     */
    public MarkerPositionManager(
            List<List<GeneticMarker>> geneticMarkerLists)
    {
        this.initializeData(geneticMarkerLists);
    }
    
    /**
     * Do all of the real calculations in order to figure out what the
     * marker and chromosome positions should be.
     * @param geneticMarkerLists
     *          the list of marker lists. each marker list should belong
     *          to a single list
     */
    private void initializeData(List<List<GeneticMarker>> geneticMarkerLists)
    {
        if(geneticMarkerLists.isEmpty())
        {
            throw new IllegalArgumentException(
                    "cannot create a layout without any genetic maps");
        }
        else
        {
            Map<GeneticMarker, Double> markerPositionMap =
                new HashMap<GeneticMarker, Double>();
            Map<String, Double> chromosomeStartingPositionMap =
                new HashMap<String, Double>();
            Map<String, Double> chromosomeStartingPositionInCentimorgansMap =
                new HashMap<String, Double>();
            Map<String, Double> chromosomeExtentMap =
                new HashMap<String, Double>();
            
            // do some initial calculations to figure in order to figure
            // out what the chromosome buffer and graph distance per
            // cM should be
            double cumulativeExtentInCentimorgans = 0.0;
            for(List<GeneticMarker> currMarkerList: geneticMarkerLists)
            {
                if(currMarkerList.isEmpty())
                {
                    throw new IllegalArgumentException(
                            "found an empty marker list");
                }
                else
                {
                    double chromosomeExtentInCm =
                        GeneticMap.getTotalExtentOfMarkerListInCentimorgans(
                                currMarkerList);
                    cumulativeExtentInCentimorgans += chromosomeExtentInCm;
                }
            }
            final double graphDistancePerCentimorgan =
                (1.0 - CUMULATIVE_CHROMOSOME_SPACING) /
                cumulativeExtentInCentimorgans;
            
            final double bufferBetweenChromosomes =
                (CUMULATIVE_CHROMOSOME_SPACING / 2.0) /
                geneticMarkerLists.size();
            
            // now that we know what the buffer and distance per cM should
            // be, we can do the real calculations
            double currChromoStartPosition = 0.0;
            for(List<GeneticMarker> currMarkerList: geneticMarkerLists)
            {
                GeneticMarker initialMarker =
                    currMarkerList.get(0);
                chromosomeStartingPositionInCentimorgansMap.put(
                        initialMarker.getChromosomeName(),
                        initialMarker.getMarkerPositionCentimorgans());
                chromosomeStartingPositionMap.put(
                        initialMarker.getChromosomeName(),
                        currChromoStartPosition);
                double chromosomeExtentInGraphUnits =
                    GeneticMap.getTotalExtentOfMarkerListInCentimorgans(currMarkerList) *
                    graphDistancePerCentimorgan;
                chromosomeExtentMap.put(
                        initialMarker.getChromosomeName(),
                        chromosomeExtentInGraphUnits +
                        (bufferBetweenChromosomes * 2.0));
                
                currChromoStartPosition += bufferBetweenChromosomes;
                for(GeneticMarker currMarker: currMarkerList)
                {
                    double currMarkerOffsetInCentimorgans =
                        currMarker.getMarkerPositionCentimorgans() -
                        initialMarker.getMarkerPositionCentimorgans();
                    double currMarkerGraphPosition =
                        currChromoStartPosition +
                        (currMarkerOffsetInCentimorgans * graphDistancePerCentimorgan);
                    markerPositionMap.put(
                            currMarker,
                            currMarkerGraphPosition);
                }
                currChromoStartPosition +=
                    chromosomeExtentInGraphUnits +
                    bufferBetweenChromosomes;
            }
            
            this.markerPositionMap = markerPositionMap;
            this.chromosomeStartingPositionMap = chromosomeStartingPositionMap;
            this.chromosomeStartingPositionInCentimorgansMap =
                chromosomeStartingPositionInCentimorgansMap;
            this.chromosomeExtentMap = chromosomeExtentMap;
            this.graphDistancePerCentimorgan = graphDistancePerCentimorgan;
        }
    }
    
    /**
     * Get the position for the given marker
     * @param marker
     *          the marker that we're getting the position for
     * @return
     *          the position which will range between 0 and 1
     */
    public Double getMarkerPositionInGraphUnits(GeneticMarker marker)
    {
        return this.markerPositionMap.get(marker);
    }
    
    /**
     * Get the graph position for the given chromosome at the given position
     * @param chromosomeName
     *          the name of the chromosome that we're looking for
     * @param positionInCentimorgans
     *          the position that we're looking for in the chromomosome in
     *          centimorgan units
     * @return
     *          the position in graph units
     */
    public double getPositionInGraphUnits(
            String chromosomeName,
            double positionInCentimorgans)
    {
        final double bufferBetweenChromosomes =
            (CUMULATIVE_CHROMOSOME_SPACING / 2.0) /
            this.chromosomeStartingPositionInCentimorgansMap.size();
        
        final double chromoStart = this.chromosomeStartingPositionMap.get(
                chromosomeName);
        final double offsetInCentimorgans =
            positionInCentimorgans -
            this.chromosomeStartingPositionInCentimorgansMap.get(chromosomeName);
        return
            chromoStart + bufferBetweenChromosomes +
            (offsetInCentimorgans * this.graphDistancePerCentimorgan);
    }
    
    /**
     * Get the starting position for the given chromosome
     * @param chromosomeName
     *          the name of the chromosome whose starting position we're
     *          asking for
     * @return
     *          the starting position
     */
    public double getChromosomeStartingPositionInGraphUnits(String chromosomeName)
    {
        return this.chromosomeStartingPositionMap.get(chromosomeName);
    }
    
    /**
     * Get the chromosome extent in graph units
     * @param chromosomeName
     *          the name of the chromosome whose extent we're asking for
     * @return
     *          the extent
     */
    public double getChromosomeExtentInGraphUnits(String chromosomeName)
    {
        return this.chromosomeExtentMap.get(chromosomeName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("positions:");
        for(Entry<GeneticMarker, Double> markerEntry: this.markerPositionMap.entrySet())
        {
            sb.append(" ");
            sb.append(markerEntry.getKey().toString());
            sb.append("=");
            sb.append(markerEntry.getValue());
        }
        
        return sb.toString();
    }
}
