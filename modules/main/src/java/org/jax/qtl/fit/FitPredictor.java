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

package org.jax.qtl.fit;

import java.util.List;

import org.jax.qtl.cross.GeneticMarker;

/**
 * Class which holds info about a single fit predictor
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitPredictor
{
    private final List<String> interactingPhenotypes;
    
    private final List<GeneticMarker> interactingMarkers;

    /**
     * @param interactingPhenotypes
     * @param interactingMarkers
     */
    public FitPredictor(
            List<String> interactingPhenotypes,
            List<GeneticMarker> interactingMarkers)
    {
        this.interactingPhenotypes = interactingPhenotypes;
        this.interactingMarkers = interactingMarkers;
    }
    
    /**
     * Getter for the interacting markers
     * @return the interactingMarkers
     */
    public List<GeneticMarker> getInteractingMarkers()
    {
        return this.interactingMarkers;
    }
    
    /**
     * Getter for the interacting phenotypes
     * @return the interactingPhenotypes
     */
    public List<String> getInteractingPhenotypes()
    {
        return this.interactingPhenotypes;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuffer formulaBuffer = new StringBuffer();
        
        for(int j = 0; j < this.interactingMarkers.size(); j++)
        {
            GeneticMarker currInteractingMarker =
                this.interactingMarkers.get(j);
            formulaBuffer.append(currInteractingMarker.toString());
            
            // add a * for all but the last marker
            if(j < this.interactingMarkers.size() - 1)
            {
                formulaBuffer.append('*');
            }
        }
        
        if((!this.interactingMarkers.isEmpty()) &&
           (!this.interactingPhenotypes.isEmpty()))
        {
            formulaBuffer.append('*');
        }
        
        for(int j = 0; j < this.interactingPhenotypes.size(); j++)
        {
            String currInteractingPheno =
                this.interactingPhenotypes.get(j);
            formulaBuffer.append(currInteractingPheno);
            
            // add a * for all but the last pheno
            if(j < this.interactingPhenotypes.size() - 1)
            {
                formulaBuffer.append('*');
            }
        }
        
        return formulaBuffer.toString();
    }
}
