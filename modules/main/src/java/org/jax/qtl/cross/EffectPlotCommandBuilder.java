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
import java.util.List;

import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;


/**
 * Builds an effect plot command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EffectPlotCommandBuilder
{
    private static final String EFFECT_PLOT_METHOD_NAME = "effectplot";
    
    private static final String CROSS_PARAMETER_NAME = "cross";
    
    private volatile Cross cross;
    
    private static final String PHENOTYPE_INDEX_PARAMETER_NAME = "pheno.col";
    
    private volatile int phenotypeIndex;
    
    private static final String FIRST_MARKER_PARAMETER_NAME = "mname1";
    
    private volatile GeneticMarker firstMarker;
    
    private static final String SECOND_MARKER_PARAMETER_NAME = "mname2";
    
    private volatile GeneticMarker secondMarker;
    
    private static final String DRAW_PLOT_PARAMETER_NAME = "draw";
    
    /**
     * Constructor
     */
    public EffectPlotCommandBuilder()
    {
    }
    
    /**
     * @return the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }
    
    /**
     * @param cross the cross to set
     */
    public void setCross(Cross cross)
    {
        this.cross = cross;
    }
    
    /**
     * Getter for the zero based phenotype index that we're doing an effect
     * plot for.
     * @return
     *          the phenotype index
     */
    public int getPhenotypeIndex()
    {
        return this.phenotypeIndex;
    }
    
    /**
     * Setter for the phenotype index.
     * @param phenotypeIndex
     *          the phenotype index to set
     */
    public void setPhenotypeIndex(int phenotypeIndex)
    {
        this.phenotypeIndex = phenotypeIndex;
    }
    
    /**
     * Getter for the 1st marker to plot
     * @return the firstMarker
     */
    public GeneticMarker getFirstMarker()
    {
        return this.firstMarker;
    }
    
    /**
     * Setter for the 1st marker to plot
     * @param firstMarker the firstMarker to set
     */
    public void setFirstMarker(GeneticMarker firstMarker)
    {
        this.firstMarker = firstMarker;
    }
    
    /**
     * Getter for the second marker (use null if this is just a single
     * plot)
     * @return
     *          the second marker
     */
    public GeneticMarker getSecondMarker()
    {
        return this.secondMarker;
    }
    
    /**
     * Setter for the second marker to plot effects for
     * @param secondMarker the secondMarker to set
     */
    public void setSecondMarker(GeneticMarker secondMarker)
    {
        this.secondMarker = secondMarker;
    }
    
    /**
     * Get the command
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        Cross cross = this.cross;
        if(cross != null)
        {
            parameters.add(new RCommandParameter(
                    CROSS_PARAMETER_NAME,
                    cross.getAccessorExpressionString()));
        }
        
        parameters.add(new RCommandParameter(
                PHENOTYPE_INDEX_PARAMETER_NAME,
                Integer.toString(this.getPhenotypeIndex() + 1)));
        
        GeneticMarker firstMarker = this.firstMarker;
        if(firstMarker != null)
        {
            parameters.add(new RCommandParameter(
                    FIRST_MARKER_PARAMETER_NAME,
                    RUtilities.javaStringToRString(firstMarker.getMarkerName())));
        }
        
        GeneticMarker secondMarker = this.secondMarker;
        if(secondMarker != null)
        {
            parameters.add(new RCommandParameter(
                    SECOND_MARKER_PARAMETER_NAME,
                    RUtilities.javaStringToRString(secondMarker.getMarkerName())));
        }
        
        parameters.add(new RCommandParameter(
                DRAW_PLOT_PARAMETER_NAME,
                RUtilities.javaBooleanToRBoolean(false)));
        
        return new RMethodInvocationCommand(
                EFFECT_PLOT_METHOD_NAME,
                parameters);
    }
}
