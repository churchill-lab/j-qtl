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
import java.util.Arrays;
import java.util.List;

import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;


/**
 * A class for simulating a genotype map
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimulateMapCommandBuilder
{
    private volatile int markersPerChromosome = 10;
    
    private volatile boolean includeTelomereMarkers = false;
    
    private volatile boolean useEqualMarkerSpacing = false;
    
    private volatile boolean createSexSpecificGeneticMaps = false;
    
    private volatile boolean includeXChromosome = true;
    
    private volatile double[] chromosomeLengths;
    
    /**
     * Constructor
     */
    public SimulateMapCommandBuilder()
    {
        this.chromosomeLengths = new double[2];
        Arrays.fill(this.chromosomeLengths, 100.0);
    }

    /**
     * Getter for the markers per chromosome property
     * @return
     *          the markers per chromosome
     */
    public int getMarkersPerChromosome()
    {
        return this.markersPerChromosome;
    }

    /**
     * Setter for the markers per chromosome property
     * @param markersPerChromosome
     *          the markers per chromosome
     */
    public void setMarkersPerChromosome(int markersPerChromosome)
    {
        this.markersPerChromosome = markersPerChromosome;
    }

    /**
     * Getter that determines if this command should include telomere
     * markers
     * @return
     *          true if we should include telomere markers
     */
    public boolean getIncludeTelomereMarkers()
    {
        return this.includeTelomereMarkers;
    }

    /**
     * Setter for wether or not we should include telomere markers
     * @param includeTelomereMarkers
     *          true if we should include telomere marks
     */
    public void setIncludeTelomereMarkers(boolean includeTelomereMarkers)
    {
        this.includeTelomereMarkers = includeTelomereMarkers;
    }

    /**
     * Getter for determining if we should use equal spacing
     * @return
     *          true if we should include equal spacing
     */
    public boolean getUseEqualMarkerSpacing()
    {
        return this.useEqualMarkerSpacing;
    }

    /**
     * Setter for determining if we should use equal marker spacing
     * @param useEqualMarkerSpacing
     *          value for determining if we use equal spacing or not
     */
    public void setUseEqualMarkerSpacing(boolean useEqualMarkerSpacing)
    {
        this.useEqualMarkerSpacing = useEqualMarkerSpacing;
    }

    /**
     * Getter for determining if we should create sex specific genetic maps
     * @return
     *          getter for determining if sex specific maps are created
     */
    public boolean getCreateSexSpecificGeneticMaps()
    {
        return this.createSexSpecificGeneticMaps;
    }

    /**
     * Setter for determining if we should create sex specific genetic maps
     * @param createSexSpecificGeneticMaps
     *          value for determining if we should create sex specific
     *          maps or not
     */
    public void setCreateSexSpecificGeneticMaps(boolean createSexSpecificGeneticMaps)
    {
        this.createSexSpecificGeneticMaps = createSexSpecificGeneticMaps;
    }

    /**
     * Getter for determining if we should include an x chromosome
     * @return
     *          true iff we should include an x chromosome
     */
    public boolean getIncludeXChromosome()
    {
        return this.includeXChromosome;
    }

    /**
     * Setter for determining if we should include an X chromosome
     * @param includeXChromosome
     *          the include x chromosome value
     */
    public void setIncludeXChromosome(boolean includeXChromosome)
    {
        this.includeXChromosome = includeXChromosome;
    }

    /**
     * Getter for the chromosome lengths
     * @return
     *          the chromosome lengths
     */
    public double[] getChromosomeLengths()
    {
        return this.chromosomeLengths;
    }

    /**
     * Setter for the chromosome lengths
     * @param chromosomeLengths
     *          the chromosome lengths
     */
    public void setChromosomeLengths(double[] chromosomeLengths)
    {
        this.chromosomeLengths = chromosomeLengths;
    }
    
    /**
     * Getter for the current sim.map command
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        RMethodInvocationCommand simMapMethod = new RMethodInvocationCommand(
                "sim.map",
                this.getCommandParameters());
        return simMapMethod;
    }

    /**
     * Getter for the command parameters
     * @return
     *          the parameters
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        parameters.add(new RCommandParameter(
                "len",
                RUtilities.doubleArrayToRVector(this.chromosomeLengths)));
        parameters.add(new RCommandParameter(
                "n.mar",
                Integer.toString(this.markersPerChromosome)));
        parameters.add(new RCommandParameter(
                "anchor.tel",
                RUtilities.javaBooleanToRBoolean(this.includeTelomereMarkers)));
        parameters.add(new RCommandParameter(
                "include.x",
                RUtilities.javaBooleanToRBoolean(this.includeXChromosome)));
        parameters.add(new RCommandParameter(
                "sex.sp",
                RUtilities.javaBooleanToRBoolean(this.createSexSpecificGeneticMaps)));
        parameters.add(new RCommandParameter(
                "eq.spacing",
                RUtilities.javaBooleanToRBoolean(this.useEqualMarkerSpacing)));
        
        return parameters;
    }
}
