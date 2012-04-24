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
import java.util.logging.Logger;

import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.scan.ScanOneSummary.ScanOneSummaryRow;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * A builder class for scan one summaries
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneSummaryBuilder
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneSummaryBuilder.class.getName());
    
    /**
     * the number of columns before significance values
     */
    private static final int NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES = 2;
    
    private static final String TEMPORARY_SUMMARY_OBJECT_SUFFIX =
        ".temp_summary_scanone";
    
    private final ScanOneResult resultToSummarize;
    
    private final ConfidenceThresholdState confidenceThresholdType;
    
    private final double confidenceThresholdValue;
    
    private final String lodColumnName;

    /**
     * Constructor
     * @param resultToSummarize
     *          see {@link #getResultToSummarize()}
     * @param confidenceThresholdType
     *          see {@link #getConfidenceThresholdType()}
     * @param lodColumnName
     *          see {@link #getLodColumnName()}
     * @param confidenceThresholdValue
     *          see {@link #confidenceThresholdValue}
     */
    public ScanOneSummaryBuilder(
            ScanOneResult resultToSummarize,
            ConfidenceThresholdState confidenceThresholdType,
            String lodColumnName,
            double confidenceThresholdValue)
    {
        this.resultToSummarize = resultToSummarize;
        this.confidenceThresholdType = confidenceThresholdType;
        this.lodColumnName = lodColumnName;
        this.confidenceThresholdValue = confidenceThresholdValue;
    }
    
    /**
     * Getter for the lod column name
     * @return the lodColumnName
     */
    public String getLodColumnName()
    {
        return this.lodColumnName;
    }

    /**
     * Getter for the result to summarize
     * @return
     *          the result to summarize
     */
    public ScanOneResult getResultToSummarize()
    {
        return this.resultToSummarize;
    }

    /**
     * Getter for the confidence threshold type
     * @return
     *          the confidence threshold type
     */
    public ConfidenceThresholdState getConfidenceThresholdType()
    {
        return this.confidenceThresholdType;
    }

    /**
     * Getter for the confidence threshold value
     * @return
     *          the confidence threshold value
     */
    public double getConfidenceThresholdValue()
    {
        return this.confidenceThresholdValue;
    }
    
    /**
     * Create a summary given all of the properties that have been set on this
     * builder
     * @return
     *          the summary
     */
    public ScanOneSummary createSummary()
    {
        // figure out what the p-value setting & LOD column index are
        boolean showPValues =
            this.resultToSummarize.getPermutationsWereCalculated();
        int lodColumnIndexNoOffset = this.resultToSummarize.getLodColumnIndexWithoutColumnOffset(
                this.lodColumnName);
        
        List<RCommandParameter> parameters = this.createSummaryParameters(
                showPValues,
                lodColumnIndexNoOffset);
        
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
            ScanOneSummary summary = this.extractSummary(
                    temporarySummaryObject,
                    showPValues,
                    lodColumnIndexNoOffset);
            
            // clean up and return
            this.resultToSummarize.getRInterface().evaluateCommandNoReturn(new SilentRCommand(
                    "rm(" + temporarySummaryObject.getAccessorExpressionString() + ")"));
            
            return summary;
        }
    }
    
    /**
     * Extract the summary from the summary object
     * @param summaryObject
     *          the summary object
     * @param extractPValues
     *          if true we should extract the p-values too
     * @param lodColumnIndexNoOffset
     *          the LOD column index to use
     * @return
     *          the summary
     */
    private ScanOneSummary extractSummary(
            RObject summaryObject,
            boolean extractPValues,
            int lodColumnIndexNoOffset)
    {
        RInterface rInterface = summaryObject.getRInterface();
        
        String[] markerNames = JRIUtilityFunctions.getRowNames(summaryObject);
        
        if(markerNames != null && markerNames.length > 0)
        {
            // figure out what the LOD column index is
            int lodColumnIndexWithOffset = lodColumnIndexNoOffset;
            if(extractPValues)
            {
                lodColumnIndexWithOffset *= 2;
            }
            lodColumnIndexWithOffset += NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES;
            
            String lodScoreCommandString = RUtilities.columnIndexExpression(
                    summaryObject.getAccessorExpressionString(),
                    lodColumnIndexWithOffset);
            REXP lodScoreRExpression = rInterface.evaluateCommand(
                    new SilentRCommand(lodScoreCommandString));
            double[] lodScores = lodScoreRExpression.asDoubleArray();
            
            if(markerNames.length != lodScores.length)
            {
                throw new IllegalStateException(
                        "marker name length doesn't match the lod score length " +
                        "for the scanone summary");
            }
            
            double[] pValues = null;
            if(extractPValues)
            {
                String pValuesCommandString = RUtilities.columnIndexExpression(
                        summaryObject.getAccessorExpressionString(),
                        lodColumnIndexWithOffset + 1);
                REXP pValuesRExpression = rInterface.evaluateCommand(
                        new SilentRCommand(pValuesCommandString));
                pValues = pValuesRExpression.asDoubleArray();
                
                if(lodScores.length != pValues.length)
                {
                    throw new IllegalStateException(
                            "lod score length doesn't match the pvalue length for " +
                            "the scanone summary");
                }
            }
            
            Map<String, GeneticMarker> markerNameToMarkerMap =
                this.resultToSummarize.getMarkerNameToMarkerMap();
            ScanOneSummaryRow[] summaryRows =
                new ScanOneSummaryRow[markerNames.length];
            for(int currRow = 0; currRow < summaryRows.length; currRow++)
            {
                GeneticMarker marker = markerNameToMarkerMap.get(
                        markerNames[currRow]);
                summaryRows[currRow] = new ScanOneSummaryRow(
                        marker,
                        lodScores[currRow],
                        pValues == null ? 0.0 : pValues[currRow]);
            }
            
            return new ScanOneSummary(
                    summaryRows,
                    extractPValues);
        }
        else
        {
            return new ScanOneSummary(
                    new ScanOneSummaryRow[0],
                    extractPValues);
        }
    }

    /**
     * Create a list of summary parameters
     * @param showPValues
     *          if true we should show p-values in the table
     * @param lodColumnIndexNoOffset
     *          the index of the lod comment to summarize
     * @return
     *          the parameter list or null if we don't have enough info to
     *          come up with good parameters
     */
    private List<RCommandParameter> createSummaryParameters(
            boolean showPValues,
            int lodColumnIndexNoOffset)
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        parameters.add(new RCommandParameter(
                this.resultToSummarize.getAccessorExpressionString()));
        
        parameters.add(new RCommandParameter(
                "format",
                RUtilities.javaStringToRString("onepheno")));
        
        // add the LOD column parameter using a 1 based index (java is 0 based)
        parameters.add(new RCommandParameter(
                "lodcolumn",
                RUtilities.javaIntToRInt(lodColumnIndexNoOffset + 1)));
        
        switch(this.confidenceThresholdType)
        {
            case NO_THRESHOLD:
            {
                // nothing to do since there's no threshold
                break;
            }
            
            case ALPHA_THRESHOLD:
            {
                parameters.add(new RCommandParameter(
                        "alpha",
                        Double.toString(this.confidenceThresholdValue)));
                break;
            }
            
            case LOD_SCORE_THRESHOLD:
            {
                parameters.add(new RCommandParameter(
                        "threshold",
                        Double.toString(this.confidenceThresholdValue)));
                break;
            }
            
            default:
            {
                LOG.warning(
                        "unknown confidence threshold type: " +
                        this.confidenceThresholdType);
                break;
            }
        }
        
        if(showPValues)
        {
            parameters.add(new RCommandParameter(
                    "perms",
                    this.resultToSummarize.getPermutationsObjectAccessorString()));
            parameters.add(new RCommandParameter(
                    "pvalues",
                    RUtilities.javaBooleanToRBoolean(true)));
        }
        
        return parameters;
    }
}
