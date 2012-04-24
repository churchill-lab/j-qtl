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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.project.QtlDataModel;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * A Java wrapper around an R scan result object.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneResult extends ScanResult
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneResult.class.getName());
    
    /**
     * the number of columns before significance values
     */
    private static final int NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES = 2;
    
    /**
     * the R type that a {@link ScanOneResult} should have
     */
    public static final String SCANONE_RESULT_TYPE_STRING = "scanone";
    
    private static final String AUTOSOME_PERMUTATIONS_NAME = "A";
    
    private static final String X_CHROMOSOME_PERMUTATIONS_NAME = "X";

    private static final String PERMUTATION_RESULT_TYPE_STRING = "scanoneperm";
    
    private static final String SCANONE_CHROMOSOME_COLUMN_NAME = "chr";
    
    private static final String SCANONE_MARKER_POSITION_COLUMN_NAME = "pos";
    
    private static final String LIST_TYPE_STRING = "list";

//    private static final String X_CHROMOSOME_ATTRIBUTE_NAME = "xchr";
    
    private final RObject scanPermutationsRObject;
    
    /**
     * Construct a new scanone result
     * @param rInterface
     *          see {@link RObject#getRInterface()}
     * @param accessorExpressionString
     *          see {@link RObject#getAccessorExpressionString()}
     * @param parentCross
     *          see {@link #getParentCross()}
     */
    public ScanOneResult(
            RInterface rInterface,
            String accessorExpressionString,
            Cross parentCross)
    {
        super(rInterface, accessorExpressionString, parentCross);
        
        String accessorStringForPermResult =
            this.getAccessorExpressionString() +
            ScanCommandBuilder.PERMUTATION_IDENTIFIER_SUFFIX;
        this.scanPermutationsRObject = new RObject(
                this.getRInterface(),
                accessorStringForPermResult);
        
        this.checkRClass();
    }
    
    /**
     * issues a warning if the R class is not the expected type
     */
    private void checkRClass()
    {
        if(!JRIUtilityFunctions.inheritsRClass(this, SCANONE_RESULT_TYPE_STRING))
        {
            LOG.warning(
                    this.getAccessorExpressionString() +
                    " is not of the expected class: " +
                    SCANONE_RESULT_TYPE_STRING);
        }
    }
    
    /**
     * Get the accessor string for the permutations object. This function
     * returns a string whether or not the permutations were actually
     * calculated
     * @return
     *          the accessor string for the permutations object
     * @see #getPermutationsWereCalculated()
     */
    public String getPermutationsObjectAccessorString()
    {
        return this.scanPermutationsRObject.getAccessorExpressionString();
    }

    /**
     * Determines if permutations based info is available for this result
     * @return
     *          true iff permutations are available
     */
    public boolean getPermutationsWereCalculated()
    {
        if(JRIUtilityFunctions.isTopLevelObject(this.scanPermutationsRObject))
        {
            if(JRIUtilityFunctions.inheritsRClass(
                    this.scanPermutationsRObject,
                    PERMUTATION_RESULT_TYPE_STRING))
            {
                return true;
            }
            else
            {
                LOG.warning(
                        "R object \"" +
                        this.scanPermutationsRObject.getAccessorExpressionString() +
                        "\" exists, but isn't the expected type \"" +
                        PERMUTATION_RESULT_TYPE_STRING + "\"");
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Get the significance value column names. These will usually match the
     * phenotype names, but sometimes they're different.
     * @return
     *          the significance value column names
     */
    public String[] getSignificanceValueColumnNames()
    {
        String[] allColumnNames = JRIUtilityFunctions.getNames(this);
        if(allColumnNames == null ||
           allColumnNames.length <= NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES)
        {
            throw new IllegalStateException(
                    "failed to read significance value column names " +
            		"for: " + this.getAccessorExpressionString());
        }
        else
        {
            int numSignificanceValueColumns =
                allColumnNames.length - NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES;
            String[] sigColumnNames =
                new String[numSignificanceValueColumns];
            if(numSignificanceValueColumns == 1)
            {
                String scannedPhenotype = this.getScannedPhenotypeName();
                if(scannedPhenotype != null)
                {
                    sigColumnNames[0] = scannedPhenotype;
                }
                else
                {
                    sigColumnNames[0] =
                        allColumnNames[NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES];
                }
            }
            else
            {
                for(int i = 0; i < sigColumnNames.length; i++)
                {
                    sigColumnNames[i] =
                        allColumnNames[i + NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES];
                }
            }
            
            return sigColumnNames;
        }
    }
    
    /**
     * Determine if there are separate p-values for autosomes and the X
     * chromosome.
     * @return
     *          true iff the p-values are separate
     */
    public boolean getXChromosomePValuesAreSeparate()
    {
        String typeOfCommandString =
            "typeof(" +
            this.scanPermutationsRObject.getAccessorExpressionString() +
            ")";
        REXP typeOfPerm = this.getRInterface().evaluateCommand(
                new SilentRCommand(typeOfCommandString));
        if(LIST_TYPE_STRING.equals(typeOfPerm.asString()))
        {
            String[] permListNames = JRIUtilityFunctions.getNames(
                    this.scanPermutationsRObject);
            List<String> permListNamesList = Arrays.asList(permListNames);
            
            if(permListNamesList.size() == 2 &&
               permListNamesList.contains(AUTOSOME_PERMUTATIONS_NAME) &&
               permListNamesList.contains(X_CHROMOSOME_PERMUTATIONS_NAME))
            {
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(
                            this.scanPermutationsRObject.getAccessorExpressionString() +
                            " uses separate permutations for the X chromosome");
                }
                return true;
            }
            else
            {
                throw new IllegalStateException(
                        "failed to determine internal structure of: " +
                        this.scanPermutationsRObject.getAccessorExpressionString());
            }
        }
        else
        {
            if(LOG.isLoggable(Level.FINER))
            {
                LOG.finer(
                        this.scanPermutationsRObject.getAccessorExpressionString() +
                        " does not use separate permutations for the X chromosome");
            }
            return false;
        }
    }
    
    /**
     * Get the mapping from marker name to marker
     * @return
     *          the mapping
     */
    public Map<String, GeneticMarker> getMarkerNameToMarkerMap()
    {
        // TODO make me efficient over multiple calls
        List<ScanOneMarkerSignificanceValues> markerSignificanceValues =
            this.getMarkerSignificanceValues();
        Map<String, GeneticMarker> markerNameToMarkerMap =
            new HashMap<String, GeneticMarker>();
        
        for(ScanOneMarkerSignificanceValues currMarkerSigValue: markerSignificanceValues)
        {
            markerNameToMarkerMap.put(
                    currMarkerSigValue.getMarker().getMarkerName(),
                    currMarkerSigValue.getMarker());
        }
        
        return markerNameToMarkerMap;
    }
    
    /**
     * Get the marker significance values for the "default" LOD column name
     * @return
     *          the marker significance values
     */
    private List<ScanOneMarkerSignificanceValues> getMarkerSignificanceValues()
    {
        return this.getMarkerSignificanceValues(
                this.getSignificanceValueColumnNames()[0]);
    }

    /**
     * Getter for the marker significance values. These will be recalculated
     * each time this function is invoked.
     * @param lodColumnName
     *          the name of the lod column that we're looking to get
     *          significance values for
     * @return
     *          a list containing all of the significance values
     */
    public List<ScanOneMarkerSignificanceValues> getMarkerSignificanceValues(
            String lodColumnName)
    {
        // figure out what some of the index values are from the column names
        String[] columnNames = JRIUtilityFunctions.getColumnNames(this);
        List<String> columnNamesList = Arrays.asList(columnNames);
        int chromosomeIndex = columnNamesList.indexOf(SCANONE_CHROMOSOME_COLUMN_NAME);
        int markerPositionIndex = columnNamesList.indexOf(SCANONE_MARKER_POSITION_COLUMN_NAME);
        if(chromosomeIndex < 0 || chromosomeIndex >= NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES)
        {
            throw new IndexOutOfBoundsException(
                    "can't read LOD scores because of bad chromosome index: " +
                    chromosomeIndex);
        }
        else if(markerPositionIndex < 0 || markerPositionIndex >= NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES)
        {
            throw new IndexOutOfBoundsException(
                    "can't read LOD scores because of bad marker index: " +
                    markerPositionIndex);
        }
        
        // read in marker names
        String[] markerNames = JRIUtilityFunctions.getRowNames(this);
        
        // read in chromosome column
        String chromosomesCommandString = RUtilities.columnIndexExpression(
            this.getAccessorExpressionString(),
            chromosomeIndex);
        REXP chromosomesRExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(chromosomesCommandString));
        String[] chromosomeNames = JRIUtilityFunctions.extractStringArrayFromFactor(
                chromosomesRExpression);
        
        // read in marker positions column
        String markerPositionsCommandString = RUtilities.columnIndexExpression(
                this.getAccessorExpressionString(),
                markerPositionIndex);
        REXP markerPositionsRExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(markerPositionsCommandString));
        double[] markerPositions = markerPositionsRExpression.asDoubleArray();
        
        // read in the LOD columns
        int currLodColumnIndex = this.getLodColumnIndexWithColumnOffset(
                lodColumnName);
        if(currLodColumnIndex < NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES)
        {
            throw new IllegalArgumentException(
                    "unknown LOD column name: " + lodColumnName);
        }
        else
        {
            REXP currLodValuesExpression = this.getRInterface().evaluateCommand(
                    new SilentRCommand(RUtilities.columnIndexExpression(
                            this.getAccessorExpressionString(),
                            currLodColumnIndex)));
            double[] currLodValues = currLodValuesExpression.asDoubleArray();
            
            if(currLodValues.length != markerNames.length)
            {
                throw new IllegalStateException(
                        "Bad state: # of lod values isn't equal to # of markers");
            }
            // Aggregate all of the information into marker significance values
            List<ScanOneMarkerSignificanceValues> markerSignificanceValues =
                new ArrayList<ScanOneMarkerSignificanceValues>(markerNames.length);
            for(int i = 0; i < markerNames.length; i++)
            {
                ScanOneMarkerSignificanceValues currMarkerSignificanceValues =
                    new ScanOneMarkerSignificanceValues(
                            new GeneticMarker(
                                    markerNames[i],
                                    chromosomeNames[i],
                                    markerPositions[i]),
                            currLodValues[i]);
                markerSignificanceValues.add(currMarkerSignificanceValues);
            }
            
            return markerSignificanceValues;
        }
    }
    
    /**
     * This just takes the results of
     * {@link #getMarkerSignificanceValues(String)}
     * and organizes them by marker type.
     * @param lodColumnName
     *          the lod column name to get significance values for
     * @return
     *          all of the {@link ScanOneMarkerSignificanceValues} seperated
     *          by chromosome name
     */
    public List<List<ScanOneMarkerSignificanceValues>> getMarkerSignificanceValuesByChromosome(
            String lodColumnName)
    {
        List<List<ScanOneMarkerSignificanceValues>> markerSigsByChromosome =
            new ArrayList<List<ScanOneMarkerSignificanceValues>>();
        
        // extract the single list and break it up by chromosome
        {
            List<ScanOneMarkerSignificanceValues> markerSigList =
                this.getMarkerSignificanceValues(lodColumnName);
            ScanOneMarkerSignificanceValues prevSigValues = null;
            List<ScanOneMarkerSignificanceValues> currChromoSigList = null;
            for(ScanOneMarkerSignificanceValues currSigValues: markerSigList)
            {
                // start with a new list if the previous marker came from a
                // different chromosome
                if(prevSigValues != null)
                {
                    if(!currSigValues.getMarker().getChromosomeName().equals(
                       prevSigValues.getMarker().getChromosomeName()))
                    {
                        prevSigValues = null;
                    }
                }
                
                if(prevSigValues == null || currChromoSigList == null)
                {
                    currChromoSigList = new ArrayList<ScanOneMarkerSignificanceValues>();
                    markerSigsByChromosome.add(currChromoSigList);
                }
                
                currChromoSigList.add(currSigValues);
                
                prevSigValues = currSigValues;
            }
        }
        
        return markerSigsByChromosome;
    }
    
    /**
     * Get the names of the chromosomes that were scanned (in order)
     * @return
     *          the unique names
     */
    public String[] getScannedChromosomes()
    {
        String[] columnNames = JRIUtilityFunctions.getColumnNames(this);
        List<String> columnNamesList = Arrays.asList(columnNames);
        int chromosomeIndex = columnNamesList.indexOf(
                SCANONE_CHROMOSOME_COLUMN_NAME);
        if(chromosomeIndex < 0 || chromosomeIndex >= NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES)
        {
            throw new IndexOutOfBoundsException(
                    "bad chromosome index: " + chromosomeIndex);
        }
        
        // read in chromosome column
        String chromosomesCommandString = RUtilities.columnIndexExpression(
            this.getAccessorExpressionString(),
            chromosomeIndex);
        REXP chromosomesRExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(chromosomesCommandString));
        String[] chromosomeNames = JRIUtilityFunctions.extractStringArrayFromFactor(
                chromosomesRExpression);
        
        List<String> uniqueChromosomeNames = new ArrayList<String>();
        for(String currChromosomeName: chromosomeNames)
        {
            if(!uniqueChromosomeNames.contains(currChromosomeName))
            {
                uniqueChromosomeNames.add(currChromosomeName);
            }
        }
        
        return uniqueChromosomeNames.toArray(
                new String[uniqueChromosomeNames.size()]);
    }
    
    /**
     * Determine if any scanone results exist
     * @param dataModel
     *          the model we're checking
     * @return
     *          true if there are any scanone results
     */
    public static boolean anyScanoneResultsExist(QtlDataModel dataModel)
    {
        Cross[] crosses = dataModel.getCrosses();
        for(Cross cross: crosses)
        {
            if(!cross.getScanOneResults().isEmpty())
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate a threshold for a single alpha value
     * @param alphaValue
     *          the value
     * @param lodColumnName
     *          the name of the lod column to calculate
     * @return
     *          the threshold
     */
    public ScanOneThreshold calculateThreshold(double alphaValue, String lodColumnName)
    {
        return this.calculateThresholds(new double[] {alphaValue}, lodColumnName)[0];
    }
    
    /**
     * Calculate the thresholds for the given alpha values
     * @param alphaValues
     *          the alpha values
     * @param lodColumnName
     *          the name of the lod column to calculate
     * @return
     *          the thresholds, or null if we can't calculate a threshold
     *          list the same length as the alpha values
     */
    public ScanOneThreshold[] calculateThresholds(
            double[] alphaValues,
            String lodColumnName)
    {
        int lodIndexWithoutOffset = this.getLodColumnIndexWithoutColumnOffset(
                lodColumnName);
        if(lodIndexWithoutOffset == -1)
        {
            throw new IllegalArgumentException(
                    "unknown LOD column name: " + lodColumnName);
        }
        else
        {
            if(this.getPermutationsWereCalculated())
            {
                RCommandParameter objectParameter = new RCommandParameter(
                        this.scanPermutationsRObject.getAccessorExpressionString());
                RCommandParameter alphaParameter = new RCommandParameter(
                        RUtilities.doubleArrayToRVector(alphaValues));
                RMethodInvocationCommand summaryCommand = new RMethodInvocationCommand(
                        "summary",
                        new RCommandParameter[] {objectParameter, alphaParameter});
                String summaryCommandString = summaryCommand.getCommandText();
                
                if(this.getXChromosomePValuesAreSeparate())
                {
                    // since the permutations are separate we need to grab the
                    // "A" and "X" values by themselves
                    double[] autosomeLodThresholds =
                        this.extractLodThresholdsFromSummaryCommand(
                                summaryCommandString +
                                "$\"" + AUTOSOME_PERMUTATIONS_NAME + "\"",
                                lodIndexWithoutOffset);
                    double[] xLodThresholds =
                        this.extractLodThresholdsFromSummaryCommand(
                                summaryCommandString +
                                "$\"" + X_CHROMOSOME_PERMUTATIONS_NAME + "\"",
                                lodIndexWithoutOffset);
                    if(autosomeLodThresholds.length != xLodThresholds.length)
                    {
                        LOG.warning("autosome & x threshold lengths dont match");
                        return null;
                    }
                    else if(autosomeLodThresholds.length != alphaValues.length)
                    {
                        LOG.warning("threshold lengths don't match alpha length");
                        return null;
                    }
                    else
                    {
                        ScanOneThreshold[] thresholds =
                            new ScanOneThreshold[autosomeLodThresholds.length];
                        for(int i = 0; i < thresholds.length; i++)
                        {
                            thresholds[i] = new ScanOneThreshold(
                                    alphaValues[i],
                                    autosomeLodThresholds[i],
                                    xLodThresholds[i]);
                        }
                        return thresholds;
                    }
                }
                else
                {
                    double[] lodThresholds =
                        this.extractLodThresholdsFromSummaryCommand(
                                summaryCommandString,
                                lodIndexWithoutOffset);
                    if(lodThresholds.length != alphaValues.length)
                    {
                        LOG.warning("thresholds length doesn't match alpha length");
                        return null;
                    }
                    else
                    {
                        ScanOneThreshold[] thresholds =
                            new ScanOneThreshold[lodThresholds.length];
                        for(int i = 0; i < thresholds.length; i++)
                        {
                            thresholds[i] = new ScanOneThreshold(
                                    alphaValues[i],
                                    lodThresholds[i]);
                        }
                        return thresholds;
                    }
                }
            }
            else
            {
                LOG.warning(
                        "can't calculate thresholds for " +
                        this.getAccessorExpressionString() +
                        " since permutations have not been run");
                return null;
            }
        }
    }
    
    /**
     * Get the lod column index with the column offset (considering the
     * non-lod columns that come before)
     * @param lodColumnName
     *          the lod column name
     * @return
     *          the lod column index with offset or -1 if we can't find it
     */
    private int getLodColumnIndexWithColumnOffset(String lodColumnName)
    {
        int relativeLodColumnIndex = this.getLodColumnIndexWithoutColumnOffset(
                lodColumnName);
        
        if(relativeLodColumnIndex == -1)
        {
            return -1;
        }
        else
        {
            return relativeLodColumnIndex + NUM_COLUMNS_BEFOR_SIGNIFICANCE_VALUES;
        }
    }
    
    /**
     * The lod column index without an offset
     * @param lodColumnName
     *          the lod column name
     * @return
     *          the lod column index without any offset or -1 if we can't
     *          find it
     */
    public int getLodColumnIndexWithoutColumnOffset(String lodColumnName)
    {
        String[] lodColumnNames = this.getSignificanceValueColumnNames();
        return Arrays.asList(lodColumnNames).indexOf(lodColumnName);
    }
    
    /**
     * Get lod thresholds from the given summary command
     * @param summaryCommand
     *          the command
     * @return
     *          the thresholds
     */
    private double[] extractLodThresholdsFromSummaryCommand(
            String summaryCommand,
            int lodColumnIndex)
    {
        String extractLodVectorCommand = RUtilities.columnIndexExpression(
                summaryCommand,
                lodColumnIndex);
        REXP rExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(extractLodVectorCommand));
        return rExpression.asDoubleArray();
    }
}
