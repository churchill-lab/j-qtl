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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;

/**
 * A convenience class for creating a fitqtl R command.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitQtlCommand implements RCommand
{
    private static final String FIT_FUNCTION_NAME = "fitqtl";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            FitQtlCommand.class.getName());
    
    private final List<FitPredictor> fitPredictors;
    
    private final Cross cross;
    
    private volatile String fitResultName = null;
    
    private volatile String phenotypeToFit = null;
    
    private volatile boolean performDropOneAnalysis = false;
    
    private volatile boolean estimateQtlEffects = false;
    
    /**
     * Constructor
     * @param cross
     *          see {@link #getCross()}
     * @param fitPredictors
     *          see {@link #getFitPredictors()}
     */
    public FitQtlCommand(Cross cross, List<FitPredictor> fitPredictors)
    {
        this.cross = cross;
        this.fitPredictors = fitPredictors;
    }

    /**
     * Getter for the result name. We use this plus the cross name to come
     * up with an identifier for the fit results
     * @return
     *          the result name
     */
    public String getFitResultName()
    {
        return this.fitResultName;
    }

    /**
     * Setter for the result name
     * @param fitResultName
     *          the result name
     */
    public void setFitResultName(String fitResultName)
    {
        this.fitResultName = fitResultName;
    }

    /**
     * Getter for the phenotype that the fit function should fit
     * @return
     *          the phenotype
     */
    public String getPhenotypeToFit()
    {
        return this.phenotypeToFit;
    }

    /**
     * Setter for the phenotype the fit function should fit
     * @param phenotypeToFit
     *          the phenotype that we should fit
     */
    public void setPhenotypeToFit(String phenotypeToFit)
    {
        this.phenotypeToFit = phenotypeToFit;
    }

    /**
     * Getter for determining if we should perform "drop-one-term" analysis
     * @return
     *          true if we should do "drop-one-term"
     */
    public boolean getPerformDropOneAnalysis()
    {
        return this.performDropOneAnalysis;
    }

    /**
     * Setter for whether or not we should do drop-one-term
     * @param performDropOneAnalysis
     *          determines if we should do "drop-one-term" analysis or not
     */
    public void setPerformDropOneAnalysis(boolean performDropOneAnalysis)
    {
        this.performDropOneAnalysis = performDropOneAnalysis;
    }

    /**
     * Getter for determining if we should estimate QTL effects or not
     * @return
     *          true iff we should estimate QTL effects
     */
    public boolean getEstimateQtlEffects()
    {
        return this.estimateQtlEffects;
    }

    /**
     * Setter for determining if we should estimate QTL effects or not
     * @param estimateQtlEffects
     *          if true we should estimate QTL effects
     */
    public void setEstimateQtlEffects(boolean estimateQtlEffects)
    {
        this.estimateQtlEffects = estimateQtlEffects;
    }

    /**
     * Get the list of predictors that we're using for the fit function
     * @return
     *          the predictors
     */
    public List<FitPredictor> getFitPredictors()
    {
        return this.fitPredictors;
    }

    /**
     * Get the cross that we're doing a fit for
     * @return
     *          the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }

    /**
     * {@inheritDoc}
     */
    public String getCommandText()
    {
        List<RCommandParameter> fitParameters =
            new ArrayList<RCommandParameter>();
        
        Cross cross = this.cross;
        if(cross != null)
        {
            fitParameters.add(new RCommandParameter(
                    "cross",
                    cross.getAccessorExpressionString()));
        }
        
        String phenotypeToFit = this.phenotypeToFit;
        if(phenotypeToFit != null)
        {
            fitParameters.add(new RCommandParameter(
                    "pheno.col",
                    RUtilities.javaStringToRString(phenotypeToFit)));
        }
        
        List<FitPredictor> fitPredictors =
            new ArrayList<FitPredictor>(this.fitPredictors);
        if(!fitPredictors.isEmpty() && cross != null)
        {
            List<GeneticMarker> markerList =
                new ArrayList<GeneticMarker>();
            for(FitPredictor fitPredictor: fitPredictors)
            {
                for(GeneticMarker interactingMarker: fitPredictor.getInteractingMarkers())
                {
                    if(!markerList.contains(interactingMarker))
                    {
                        markerList.add(interactingMarker);
                    }
                }
            }
            
            if(!markerList.isEmpty())
            {
                MakeQtlCommand makeQtl = new MakeQtlCommand(
                        cross,
                        markerList);
                fitParameters.add(new RCommandParameter(
                        "qtl",
                        makeQtl.getCommandText()));
            }
            
            StringBuffer formulaBuffer = new StringBuffer("y~");
            for(int i = 0; i < fitPredictors.size(); i++)
            {
                FitPredictor currFitPredictor = fitPredictors.get(i);
                
                List<GeneticMarker> currInteractingMarkers =
                    currFitPredictor.getInteractingMarkers();
                for(int j = 0; j < currInteractingMarkers.size(); j++)
                {
                    GeneticMarker currInteractingMarker =
                        currInteractingMarkers.get(j);
                    int markerIndex =
                        markerList.indexOf(currInteractingMarker);
                    if(markerIndex == -1)
                    {
                        throw new IndexOutOfBoundsException(
                                "marker index is out of bounds");
                    }
                    
                    formulaBuffer.append("Q" + (markerIndex + 1));
                    
                    // add a * for all but the last marker
                    if(j < currInteractingMarkers.size() - 1)
                    {
                        formulaBuffer.append('*');
                    }
                }
                
                // if we have both interacting markers and interacting
                // phenotypes, then we need to combine the two with
                // a '*'
                List<String> currInteractingPhenos =
                    currFitPredictor.getInteractingPhenotypes();
                if((!currInteractingMarkers.isEmpty()) &&
                   (!currInteractingPhenos.isEmpty()))
                {
                    formulaBuffer.append('*');
                }
                
                for(int j = 0; j < currInteractingPhenos.size(); j++)
                {
                    String currInteractingPheno =
                        currInteractingPhenos.get(j);
                    formulaBuffer.append(currInteractingPheno);
                    
                    // add a * for all but the last pheno
                    if(j < currInteractingPhenos.size() - 1)
                    {
                        formulaBuffer.append('*');
                    }
                }
                
                // connect seperate predictors using a '+'
                if(i < fitPredictors.size() - 1)
                {
                    formulaBuffer.append('+');
                }
            }
            fitParameters.add(new RCommandParameter(
                    "formula",
                    formulaBuffer.toString()));
        }
        
        if(cross != null)
        {
            fitParameters.add(new RCommandParameter(
                    "covar",
                    cross.getAccessorExpressionString() + "$pheno"));
        }
        
        fitParameters.add(new RCommandParameter(
                "dropone",
                RUtilities.javaBooleanToRBoolean(
                        this.getPerformDropOneAnalysis())));
        
        fitParameters.add(new RCommandParameter(
                "get.ests",
                RUtilities.javaBooleanToRBoolean(
                        this.getEstimateQtlEffects())));
        
        RMethodInvocationCommand methodInvocationCommand =
            new RMethodInvocationCommand(
                    FIT_FUNCTION_NAME,
                    fitParameters);
        
        String fitResultAccessor = this.getFitResultAccessor();
        if(fitResultAccessor == null || fitResultAccessor.length() == 0)
        {
            return methodInvocationCommand.getCommandText();
        }
        else
        {
            RAssignmentCommand assignmentCommand = new RAssignmentCommand(
                    fitResultAccessor,
                    methodInvocationCommand.getCommandText());
            return assignmentCommand.getCommandText();
        }
    }
    
    /**
     * Get the accessor string for the result or null if the result was not
     * assigned to an R variable
     * @return
     *          the accessor
     */
    public String getFitResultAccessor()
    {
        Cross cross = this.getCross();
        String fitResultName = this.getFitResultName();
        if(cross != null && fitResultName != null)
        {
            try
            {
                return
                    cross.getAccessorExpressionString() + "." +
                    RUtilities.fromReadableNameToRIdentifier(
                        fitResultName);
            }
            catch(RSyntaxException ex)
            {
                LOG.log(Level.FINE,
                        "ignoring fit result name because of bad syntax",
                        ex);
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
