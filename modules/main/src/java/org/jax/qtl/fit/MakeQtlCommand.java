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

import java.util.ArrayList;
import java.util.List;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * A convenience class for creating the makeqtl R command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MakeQtlCommand implements RCommand
{
    private static final String MAKE_QTL_FUNCTION_NAME = "makeqtl";
    
    private final Cross cross;
    
    private final List<GeneticMarker> markers;
    
    /**
     * Constructor
     * @param cross
     *          see {@link #getCross()}
     * @param markers
     *          see {@link #getMarkers()}
     */
    public MakeQtlCommand(
            Cross cross,
            List<GeneticMarker> markers)
    {
        this.cross = cross;
        this.markers = markers;
    }
    
    /**
     * @return the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }
    
    /**
     * @return the markers
     */
    public List<GeneticMarker> getMarkers()
    {
        return this.markers;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        List<RCommandParameter> makeQtlParameters =
            new ArrayList<RCommandParameter>();
        
        makeQtlParameters.add(new RCommandParameter(
                "cross",
                this.cross.getAccessorExpressionString()));
        
        String[] markerChromoNames = new String[this.markers.size()];
        double[] markerPositions = new double[this.markers.size()];
        for(int i = 0; i < markerChromoNames.length; i++)
        {
            GeneticMarker currMarker = this.markers.get(i);
            markerChromoNames[i] = currMarker.getChromosomeName();
            
            // marker positions
            markerPositions[i] = currMarker.getMarkerPositionCentimorgans();
        }
        
        makeQtlParameters.add(new RCommandParameter(
                "chr",
                RUtilities.stringArrayToRVector(markerChromoNames)));
        makeQtlParameters.add(new RCommandParameter(
                "pos",
                RUtilities.doubleArrayToRVector(markerPositions)));
        
        // TODO need to be smarter about this one
//        if(this.cross.getCalculateConditionalProbabilitiesWasUsed())
//        {
//            makeQtlParameters.add(new RCommandParameter(
//                    "what",
//                    "\"prob\""));
//        }
//        else if(this.cross.getSimulateGenotypeWasUsed())
//        {
//            makeQtlParameters.add(new RCommandParameter(
//                    "what",
//                    "\"draws\""));
//        }
        
        RMethodInvocationCommand methodInvocationCommand =
            new RMethodInvocationCommand(
                    MAKE_QTL_FUNCTION_NAME,
                    makeQtlParameters);
        return methodInvocationCommand.getCommandText();
    }
}
