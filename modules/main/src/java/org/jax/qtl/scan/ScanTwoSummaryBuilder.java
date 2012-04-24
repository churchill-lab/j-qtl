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

package org.jax.qtl.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.GeneticMarkerPair;
import org.jax.qtl.scan.ScanTwoSummary.ModelToOptimize;
import org.jax.qtl.scan.ScanTwoSummary.ScanTwoSummaryRow;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;

/**
 * Class for building scan two summary objects
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanTwoSummaryBuilder
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanTwoSummaryBuilder.class.getName());
    
    private static final String TEMPORARY_SUMMARY_OBJECT_SUFFIX =
        ".temp_summary_scantwo";
    
    private final ScanTwoResult resultToSummarize;
    
    private final ConfidenceThresholdState confidenceThresholdState;
    
    private final double[] confidenceThresholdValues;
    
    private final ModelToOptimize modelToOptimize;
    
    private final boolean calculatePValues;
    
    private final int phenotypeIndex;
    
    /**
     * Constructor
     * @param resultToSummarize
     *          the scantwo result that we're summarizing
     * @param confidenceThresholdState
     *          the confidence threshold state to use in the summary
     * @param confidenceThresholdValues
     *          the confidence threshold values to use
     * @param modelToOptimize
     *          the kind of model we want to optimize in our summary
     * @param phenotypeIndex
     *          the phenotype index to scan
     * @param calculatePValues
     *          if true we should try to calculate p-values for the summary
     */
    public ScanTwoSummaryBuilder(
            ScanTwoResult resultToSummarize,
            ConfidenceThresholdState confidenceThresholdState,
            double[] confidenceThresholdValues,
            ModelToOptimize modelToOptimize,
            int phenotypeIndex,
            boolean calculatePValues)
    {
        this.resultToSummarize = resultToSummarize;
        this.confidenceThresholdState = confidenceThresholdState;
        this.confidenceThresholdValues = confidenceThresholdValues;
        this.modelToOptimize = modelToOptimize;
        this.phenotypeIndex = phenotypeIndex;
        this.calculatePValues = calculatePValues;
    }
    
    /**
     * Create a summary using the parameters that were passed into this
     * builder
     * @return
     *          the summary
     */
    public ScanTwoSummary createSummary()
    {
        boolean permutationsWereCalculated =
            this.resultToSummarize.getPermutationsWereCalculated();
        List<RCommandParameter> parameters = this.createSummaryParameters(
                permutationsWereCalculated);
        
        RMethodInvocationCommand summaryMethod = new RMethodInvocationCommand(
                "summary",
                parameters);
        RObject temporarySummaryObject = new RObject(
                this.resultToSummarize.getRInterface(),
                this.resultToSummarize.getAccessorExpressionString() +
                TEMPORARY_SUMMARY_OBJECT_SUFFIX);
        RAssignmentCommand tempAssignmentCommand = new RAssignmentCommand(
                temporarySummaryObject.getAccessorExpressionString(),
                summaryMethod.getCommandText());
        
        synchronized(ScanOneSummaryBuilder.class)
        {
            // run the command to create a temporary summary object
            this.resultToSummarize.getRInterface().evaluateCommandNoReturn(
                    new SilentRCommand(tempAssignmentCommand));
            ScanTwoSummary summary = this.extractSummary(
                    temporarySummaryObject,
                    this.calculatePValues && permutationsWereCalculated);
            
            // clean up and return
            this.resultToSummarize.getRInterface().evaluateCommandNoReturn(new SilentRCommand(
                    "rm(" + temporarySummaryObject.getAccessorExpressionString() + ")"));
            
            return summary;
        }
    }

    private ScanTwoSummary extractSummary(
            RObject summaryObject,
            boolean extractPValues)
    {
        try
        {
            if(JRIUtilityFunctions.getNumberOfRows(summaryObject) == 0)
            {
                // return an empty result
                return new ScanTwoSummary(
                        this.modelToOptimize,
                        new ScanTwoSummary.ScanTwoSummaryRow[0]);
            }
            else
            {
                Map<String, List<GeneticMarker>> markersByChromosome =
                    this.resultToSummarize.getChromosomeNameToMarkerMap();
                
                int columnCursor = 0;
                String[] firstChromosomeNames = JRIUtilityFunctions.getColumnFactors(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                String[] secondChromosomeNames = JRIUtilityFunctions.getColumnFactors(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                GeneticMarkerPair[] firstMarkerPairs;
                {
                    double[] firstPosInCm = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                    
                    double[] secondPosInCm = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                    
                    firstMarkerPairs = this.extractMarkerPairs(
                            markersByChromosome,
                            firstChromosomeNames,
                            secondChromosomeNames,
                            firstPosInCm,
                            secondPosInCm);
                }
                
                double[] fullLod = JRIUtilityFunctions.getColumnDoubles(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                double[] fullPValue;
                if(extractPValues)
                {
                    fullPValue = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                }
                else
                {
                    fullPValue = new double[fullLod.length];
                }
                
                double[] fullVsOneLod = JRIUtilityFunctions.getColumnDoubles(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                double[] fullVsOnePValue;
                if(extractPValues)
                {
                    fullVsOnePValue = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                }
                else
                {
                    fullVsOnePValue = new double[fullVsOneLod.length];
                }
                
                double[] interactiveLod = JRIUtilityFunctions.getColumnDoubles(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                double[] interactivePValue;
                if(extractPValues)
                {
                    interactivePValue = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                }
                else
                {
                    interactivePValue = new double[interactiveLod.length];
                }
                
                GeneticMarkerPair[] secondMarkerPairs;
                if(this.modelToOptimize == ModelToOptimize.BEST)
                {
                    double[] firstPosInCm = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                    
                    double[] secondPosInCm = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                    
                    secondMarkerPairs = this.extractMarkerPairs(
                            markersByChromosome,
                            firstChromosomeNames,
                            secondChromosomeNames,
                            firstPosInCm,
                            secondPosInCm);
                }
                else
                {
                    secondMarkerPairs = firstMarkerPairs;
                }
                
                double[] additiveLod = JRIUtilityFunctions.getColumnDoubles(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                double[] additivePValue;
                if(extractPValues)
                {
                    additivePValue = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                }
                else
                {
                    additivePValue = new double[additiveLod.length];
                }
                
                double[] additiveVsOneLod = JRIUtilityFunctions.getColumnDoubles(
                        summaryObject,
                        columnCursor);
                columnCursor++;
                
                double[] additiveVsOnePValue = new double[additiveVsOneLod.length];
                if(extractPValues)
                {
                    additiveVsOnePValue = JRIUtilityFunctions.getColumnDoubles(
                            summaryObject,
                            columnCursor);
                    columnCursor++;
                }
                else
                {
                    additiveVsOnePValue = new double[additiveVsOneLod.length];
                }
                
                // now that we've collected all of the data that we need, we need
                // to fill out the scantwo summary object
                ScanTwoSummaryRow[] scanTwoSummaryRows =
                    new ScanTwoSummaryRow[firstMarkerPairs.length];
                for(int rowIndex = 0; rowIndex < scanTwoSummaryRows.length; rowIndex++)
                {
                    ScanTwoSummaryRow currRow = new ScanTwoSummaryRow();
                    currRow.setFullMarkerPair(firstMarkerPairs[rowIndex]);
                    currRow.setFullLodScore(fullLod[rowIndex]);
                    currRow.setFullPValue(fullPValue[rowIndex]);
                    currRow.setFullVsOneLodScore(fullVsOneLod[rowIndex]);
                    currRow.setFullVsOnePValue(fullVsOnePValue[rowIndex]);
                    currRow.setInteractiveLodScore(interactiveLod[rowIndex]);
                    currRow.setInteractivePValue(interactivePValue[rowIndex]);
                    currRow.setAdditiveMarkerPair(secondMarkerPairs[rowIndex]);
                    currRow.setAdditiveLodScore(additiveLod[rowIndex]);
                    currRow.setAdditivePValue(additivePValue[rowIndex]);
                    currRow.setAdditiveVsOneLodScore(additiveVsOneLod[rowIndex]);
                    currRow.setAdditiveVsOnePValue(additiveVsOnePValue[rowIndex]);
                    
                    scanTwoSummaryRows[rowIndex] = currRow;
                }
                
                return new ScanTwoSummary(
                        this.modelToOptimize,
                        scanTwoSummaryRows);
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Caught an exception trying to extract a scantwo summary.",
                    ex);
            return new ScanTwoSummary(
                    this.modelToOptimize,
                    new ScanTwoSummary.ScanTwoSummaryRow[0]);
        }
    }

    private GeneticMarkerPair[] extractMarkerPairs(
            Map<String, List<GeneticMarker>> markersByChromosome,
            String[] firstChromosomeNames,
            String[] secondChromosomeNames,
            double[] firsMarkerPositions,
            double[] secondMarkerPositions)
    {
        GeneticMarkerPair[] markerPairs =
            new GeneticMarkerPair[firstChromosomeNames.length];
        for(int i = 0; i < firstChromosomeNames.length; i++)
        {
            List<GeneticMarker> firstMarkerList =
                markersByChromosome.get(firstChromosomeNames[i]);
            List<GeneticMarker> secondMarkerList =
                markersByChromosome.get(secondChromosomeNames[i]);
            
            GeneticMarker marker1 = this.getClosestMarker(
                    firstMarkerList,
                    firsMarkerPositions[i]);
            GeneticMarker marker2 = this.getClosestMarker(
                    secondMarkerList,
                    secondMarkerPositions[i]);
            markerPairs[i] = new GeneticMarkerPair(marker1, marker2);
        }
        return markerPairs;
    }

    private GeneticMarker getClosestMarker(
            List<GeneticMarker> markerList,
            double markerPositionInCentimorgans)
    {
        GeneticMarker closestMarker = null;
        
        for(GeneticMarker currMarker: markerList)
        {
            if(closestMarker == null)
            {
                closestMarker = currMarker;
            }
            else
            {
                double closestDistance = Math.abs(
                        markerPositionInCentimorgans -
                        closestMarker.getMarkerPositionCentimorgans());
                double newDistance = Math.abs(
                        markerPositionInCentimorgans -
                        currMarker.getMarkerPositionCentimorgans());
                
                if(newDistance < closestDistance)
                {
                    closestMarker = currMarker;
                }
            }
        }
        
        return closestMarker;
    }

    /**
     * Create the summary parameters
     * @param permutationsWereCalculated
     *          if true, we have a valid permutations object for the scantwo
     * @return
     *          the parameter list
     */
    private List<RCommandParameter> createSummaryParameters(
            boolean permutationsWereCalculated)
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        parameters.add(new RCommandParameter(
                this.resultToSummarize.getAccessorExpressionString()));
        
        switch(this.modelToOptimize)
        {
            case BEST:
            {
                parameters.add(new RCommandParameter(
                        "what",
                        RUtilities.javaStringToRString("best")));
                break;
            }
            
            case FULL:
            {
                parameters.add(new RCommandParameter(
                        "what",
                        RUtilities.javaStringToRString("full")));
                break;
            }
            
            case ADDITIVE:
            {
                parameters.add(new RCommandParameter(
                        "what",
                        RUtilities.javaStringToRString("add")));
                break;
            }
            
            case INTERACTIVE:
            {
                parameters.add(new RCommandParameter(
                        "what",
                        RUtilities.javaStringToRString("int")));
                break;
            }
        }
        
        if(this.confidenceThresholdValues != null)
        {
            String rConfidenceThresholdValues =
                RUtilities.doubleArrayToRVector(this.confidenceThresholdValues);
            switch(this.confidenceThresholdState)
            {
                case NO_THRESHOLD:
                {
                    // nothing to do since there's no threshold
                    break;
                }
                
                case ALPHA_THRESHOLD:
                {
                    if(permutationsWereCalculated)
                    {
                        parameters.add(new RCommandParameter(
                                "alphas",
                                rConfidenceThresholdValues));
                    }
                    else
                    {
                        LOG.warning(
                                "cannot use alpha thresholds since " +
                                "permutations were not calculated");
                    }
                    break;
                }
                
                case LOD_SCORE_THRESHOLD:
                {
                    parameters.add(new RCommandParameter(
                            "thresholds",
                            rConfidenceThresholdValues));
                    break;
                }
                
                default:
                {
                    LOG.warning(
                            "unknown confidence threshold type: " +
                            this.confidenceThresholdState);
                    break;
                }
            }
        }
        
        if(permutationsWereCalculated)
        {
            parameters.add(new RCommandParameter(
                    "perms",
                    this.resultToSummarize.getPermutationsObjectAccessorString()));
            if(this.calculatePValues)
            {
                parameters.add(new RCommandParameter(
                        "pvalues",
                        RUtilities.javaBooleanToRBoolean(true)));
            }
        }
        
        if(this.phenotypeIndex >= 0)
        {
            parameters.add(new RCommandParameter(
                    "lodcolumn",
                    Integer.toString(this.phenotypeIndex + 1)));
        }
        
        return parameters;
    }
}
