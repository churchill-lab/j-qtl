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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.project.QtlDataModel;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.datastructure.SequenceUtilities;
import org.rosuda.JRI.REXP;

/**
 * Holds the scantwo results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanTwoResult extends ScanResult implements Comparable<ScanTwoResult>
{
    private static final Logger LOG = Logger.getLogger(
            ScanTwoResult.class.getName());
    
    /**
     * Enum for specifying one of the significance values this scan result
     * holds.
     */
    public enum ScanTwoSignificanceValueType
    {
        /**
         * the full lod score
         */
        FULL_LOD
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Full Model";
            }
        },
        
        /**
         * the additive lod score
         */
        ADDITIVE_LOD
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Additive Model";
            }
        },
        
        /**
         * full vs. additive
         */
        FULL_VERSUS_ADDITIVE_LOD
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Full vs. Additive";
            }
        },
        
        /**
         * full vs. scanone
         */
        FULL_VERSUS_SCANONE_LOD
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Full vs. One QTL";
            }
        },
        
        /**
         * additive vs. scanone
         */
        ADDITIVE_VERSUS_SCANONE_LOD
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Additive vs. One QTL";
            }
        }
    }
    
    /**
     * A pairing of marker index values
     */
    public static class MarkerIndexPair implements Comparable<MarkerIndexPair>
    {
        private final int lesserMarkerIndex;
        
        private final int greaterMarkerIndex;

        /**
         * Constructor. The given lesser index should always be greater
         * than the given greater index.
         * @param lesserMarkerIndex
         *          the lesser index
         * @param greaterMarkerIndex
         *          the greater index
         * @throws IllegalArgumentException
         *          if lesserMarkerIndex >= greaterMarkerIndex
         */
        public MarkerIndexPair(int lesserMarkerIndex, int greaterMarkerIndex)
        throws IllegalArgumentException
        {
            if(lesserMarkerIndex >= greaterMarkerIndex)
            {
                throw new IllegalArgumentException(
                        "the given lesser index (" + lesserMarkerIndex +
                        ") is larger than the given greater index (" +
                        greaterMarkerIndex + ")");
            }
            
            this.lesserMarkerIndex = lesserMarkerIndex;
            this.greaterMarkerIndex = greaterMarkerIndex;
        }
        
        /**
         * Getter for the lesser index
         * @return the lesserMarkerIndex
         */
        public int getLesserMarkerIndex()
        {
            return this.lesserMarkerIndex;
        }
        
        /**
         * Getter for the greater index
         * @return the greaterMarkerIndex
         */
        public int getGreaterMarkerIndex()
        {
            return this.greaterMarkerIndex;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object otherPairObject)
        {
            if(otherPairObject instanceof MarkerIndexPair)
            {
                MarkerIndexPair otherPair = (MarkerIndexPair)otherPairObject;
                return this.lesserMarkerIndex == otherPair.lesserMarkerIndex &&
                       this.greaterMarkerIndex == otherPair.greaterMarkerIndex;
            }
            else
            {
                return false;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            int hash = this.lesserMarkerIndex << 16;
            hash = hash | this.greaterMarkerIndex;
            
            return hash;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(MarkerIndexPair otherPair)
        {
            // major sort on the lesser index
            int lesserDifference =
                this.lesserMarkerIndex - otherPair.lesserMarkerIndex;
            if(lesserDifference != 0)
            {
                return lesserDifference;
            }
            else
            {
                // minor sort on the greater index
                return this.greaterMarkerIndex - otherPair.greaterMarkerIndex;
            }
        }
    }
    
    /**
     * the R type that a {@link ScanTwoResult} should have
     */
    public static final String SCANTWO_RESULT_TYPE_STRING = "scantwo";
    
    private static final String PERMUTATION_RESULT_TYPE_STRING = "scantwoperm";
    
    private static final String LOD_COMPONENT_SUFFIX_STRING = "$lod";
    
    private static final String MAP_COMPONENT_SUFFIX_STRING = "$map";
    
    private static final String SCANONE_X_COMPONENT_SUFFIX_STRING = "$scanoneX";
    
    private final String lodComponentAccessor;
    
    private final String scanoneXComponentAccessor;
    
    private final String mapComponentAccessor;
    
    private final Map<String, double[][]> lodScoreMatrixCache =
        new HashMap<String, double[][]>();
    
    private final Map<String, double[]> scanOneXCache =
        new HashMap<String, double[]>();
    
    private final Map<Integer, double[]> phenotypeIndexToMaxScanonesCache =
        new HashMap<Integer, double[]>();
    
    private List<ScanTwoGeneticMarker> geneticMarkersCache = null;
    
    private String[] scannedChromosomeNamesCache = null;
    
    private final RObject scanPermutationsRObject;
    
    private final boolean singlePhenotypeScanned;

    private int[] markerChromosomeIndices;
    
    /**
     * Constructor
     * @param rInterface
     *          the r interface
     * @param accessorExpressionString 
     *          the accessor expression
     * @param parentCross
     *          the cross that owns this scan result
     */
    public ScanTwoResult(
            RInterface rInterface,
            String accessorExpressionString,
            Cross parentCross)
    {
        super(rInterface, accessorExpressionString, parentCross);
        
        this.lodComponentAccessor =
            accessorExpressionString +
            LOD_COMPONENT_SUFFIX_STRING;
        this.scanoneXComponentAccessor =
            accessorExpressionString +
            SCANONE_X_COMPONENT_SUFFIX_STRING;
        this.mapComponentAccessor =
            accessorExpressionString +
            MAP_COMPONENT_SUFFIX_STRING;
        
        String accessorStringForPermResult =
            this.getAccessorExpressionString() +
            ScanCommandBuilder.PERMUTATION_IDENTIFIER_SUFFIX;
        this.scanPermutationsRObject = new RObject(
                this.getRInterface(),
                accessorStringForPermResult);
        
        this.singlePhenotypeScanned =
            this.getScannedPhenotypeIndices().length == 1;
    }
    
    /**
     * This is just a convenience function for using the value type to get to
     * the score that we're interested in instead of calling the function
     * directly
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the marker index pair that we're getting a value for
     * @param valueType
     *          the type of value that we're looking for
     * @return
     *          the value
     */
    public double getSignificanceValue(
            int scannedPhenotypeIndex,
            MarkerIndexPair markerIndexPair,
            ScanTwoSignificanceValueType valueType)
    {
        switch(valueType)
        {
            case ADDITIVE_LOD:
            {
                return this.getAdditiveLod(scannedPhenotypeIndex, markerIndexPair);
            }
            
            case ADDITIVE_VERSUS_SCANONE_LOD:
            {
                return this.getAdditiveVersusScanOneLod(scannedPhenotypeIndex, markerIndexPair);
            }
            
            case FULL_LOD:
            {
                return this.getFullLod(scannedPhenotypeIndex, markerIndexPair);
            }
            
            case FULL_VERSUS_ADDITIVE_LOD:
            {
                return this.getFullVersusAdditiveLod(scannedPhenotypeIndex, markerIndexPair);
            }
            
            case FULL_VERSUS_SCANONE_LOD:
            {
                return this.getFullVersusScanOneLod(scannedPhenotypeIndex, markerIndexPair);
            }
            
            default:
            {
                throw new IllegalStateException(
                        "unknown value type: " + valueType.name());
            }
        }
    }
    
    /**
     * Get the full lod score for the given markers
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the full lod score
     */
    public double getFullLod(int scannedPhenotypeIndex, MarkerIndexPair markerIndexPair)
    {
        // the full model is in the "lower triangle" of the matrix, so
        // the row need to be bigger than the column
        final int row = markerIndexPair.getGreaterMarkerIndex();
        final int column = markerIndexPair.getLesserMarkerIndex();
        
        return this.getLodScoreMatrix(scannedPhenotypeIndex)[row][column];
    }
    
    /**
     * Get the additive lod score for the given markers
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the additive lod score
     */
    public double getAdditiveLod(int scannedPhenotypeIndex, MarkerIndexPair markerIndexPair)
    {
            // the additive model is in the "upper triangle" of the matrix, so
            // the row need to be smaller than the column
            int row = markerIndexPair.getLesserMarkerIndex();
            int column = markerIndexPair.getGreaterMarkerIndex();
            
            return this.getLodScoreMatrix(scannedPhenotypeIndex)[row][column];
    }
    
    /**
     * Get full sore vs. additive score for the given markers
     * (AKA: interaction)
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the full vs. additive score
     */
    public double getFullVersusAdditiveLod(int scannedPhenotypeIndex, MarkerIndexPair markerIndexPair)
    {
        return this.getFullLod(scannedPhenotypeIndex, markerIndexPair) -
               this.getAdditiveLod(scannedPhenotypeIndex, markerIndexPair);
    }
    
    /**
     * Get full sore vs. scanone score for the given markers
     * (AKA conditional-interactive)
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the full vs. scanone score
     */
    public double getFullVersusScanOneLod(int scannedPhenotypeIndex, MarkerIndexPair markerIndexPair)
    {
        double lod =
            this.getFullLod(scannedPhenotypeIndex, markerIndexPair) -
            this.getMaxScanOneLod(scannedPhenotypeIndex, markerIndexPair);
        
        // floor the lod value at 0
        return lod < 0.0 ? 0.0 : lod;
    }
    
    /**
     * Get the additive vs. scanone score
     * (AKA conditional-additive)
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the additive vs. scanone score
     */
    public double getAdditiveVersusScanOneLod(int scannedPhenotypeIndex, MarkerIndexPair markerIndexPair)
    {
        double lod =
            this.getAdditiveLod(scannedPhenotypeIndex, markerIndexPair) -
            this.getMaxScanOneLod(scannedPhenotypeIndex, markerIndexPair);
        
        // floor the lod value at 0
        return lod < 0.0 ? 0.0 : lod;
    }
    
    /**
     * Get the scanone lod score for the given marker index
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndex
     *          the marker index to get the scanone score for
     * @return
     *          the scanone lod
     */
    public double getScanOneLod(int scannedPhenotypeIndex, int markerIndex)
    {
        // the scanone values that ignore covariates live along the X
        // diagonal
        return this.getLodScoreMatrix(scannedPhenotypeIndex)[markerIndex][markerIndex];
    }
    
    /**
     * Get the scanone score that considers sex and/or cross direction
     * covariates
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @param markerIndex
     *          the marker index
     * @return
     *          the scanone LOD which accounts for covariates
     */
    public double getScanOneXLod(int scannedPhenotypeIndex, int markerIndex)
    {
        return this.getScanOneX(scannedPhenotypeIndex)[markerIndex];
    }
    
    /**
     * Get the "max" scanone. Depending on the chromosome they come from, we
     * may use either {@link #getScanOneLod(int, int)} or {@link #getScanOneXLod(int, int)}
     * @param scannedPhenotypeIndex
     *          the phenotype
     * @param markerIndexPair
     *          the index pair
     * @return
     *          the max LOD from either marker
     */
    private double getMaxScanOneLod(
            int scannedPhenotypeIndex,
            MarkerIndexPair markerIndexPair)
    {
        int lesserIndex = markerIndexPair.getLesserMarkerIndex();
        int greaterIndex = markerIndexPair.getGreaterMarkerIndex();
        
        int[] markerChromosomeIndices = this.getMarkerChromosomeIndices();
        
        double[] maxScanones = this.getMaxScanoneLodPerChromosome(scannedPhenotypeIndex);
        return Math.max(
                maxScanones[markerChromosomeIndices[lesserIndex]],
                maxScanones[markerChromosomeIndices[greaterIndex]]);
    }
    
    /**
     * Getter for the chromosome indices for each marker
     * @return
     *          the chromosome indices
     */
    private synchronized int[] getMarkerChromosomeIndices()
    {
        if(this.markerChromosomeIndices == null)
        {
            List<List<ScanTwoGeneticMarker>> geneticMarkersPerChromosome =
                this.getGeneticMarkersPerChromosome();
            int chromoCount = geneticMarkersPerChromosome.size();
            List<Integer> markerChromosomeIndicesList = new ArrayList<Integer>();
            
            for(int chromoIndex = 0; chromoIndex < chromoCount; chromoIndex++)
            {
                List<ScanTwoGeneticMarker> chromoMarkers =
                    geneticMarkersPerChromosome.get(chromoIndex);
                int markerCount = chromoMarkers.size();
                for(int markerIndex = 0; markerIndex < markerCount; markerIndex++)
                {
                    markerChromosomeIndicesList.add(chromoIndex);
                }
            }
            
            this.markerChromosomeIndices = SequenceUtilities.toIntArray(
                    markerChromosomeIndicesList);
        }
        
        return this.markerChromosomeIndices;
    }

    /**
     * Getter for the max scanone LOD values which will be one per chromosome
     * @param scannedPhenotypeIndex
     *          the phenotype index
     * @return
     *          the LOD scores
     */
    private synchronized double[] getMaxScanoneLodPerChromosome(
            int scannedPhenotypeIndex)
    {
        double[] maxScanoneLodPerChromosome =
            this.phenotypeIndexToMaxScanonesCache.get(scannedPhenotypeIndex);
        
        if(maxScanoneLodPerChromosome == null)
        {
            double[] scanOneXLods = null;
            
            List<List<ScanTwoGeneticMarker>> geneticMarkersPerChromosome =
                this.getGeneticMarkersPerChromosome();
            int chromoCount = geneticMarkersPerChromosome.size();
            maxScanoneLodPerChromosome = new double[chromoCount];
            
            int markerIndex = 0;
            for(int chromoIndex = 0; chromoIndex < chromoCount; chromoIndex++)
            {
                for(ScanTwoGeneticMarker currMarker: geneticMarkersPerChromosome.get(chromoIndex))
                {
                    double scanoneLod;
                    if(currMarker.isXChromosome())
                    {
                        if(scanOneXLods == null)
                        {
                            scanOneXLods = this.getScanOneX(scannedPhenotypeIndex);
                        }
                        
                        scanoneLod = scanOneXLods[markerIndex];
                    }
                    else
                    {
                        scanoneLod = this.getScanOneLod(scannedPhenotypeIndex, markerIndex);
                    }
                    
                    if(scanOneXLods == null)
                    {
                        scanOneXLods = this.getScanOneX(scannedPhenotypeIndex);
                    }
                    
                    if(scanoneLod > maxScanoneLodPerChromosome[chromoIndex])
                    {
                        maxScanoneLodPerChromosome[chromoIndex] = scanoneLod;
                    }
                    
                    markerIndex++;
                }
            }
            
            this.phenotypeIndexToMaxScanonesCache.put(
                    scannedPhenotypeIndex,
                    maxScanoneLodPerChromosome);
        }
        
        return maxScanoneLodPerChromosome;
    }
    
    /**
     * Getter for the lod score matrix at the given index
     * @param scannedPhenotypeIndex
     *          the index
     * @return
     *          the matrix
     */
    public synchronized double[][] getLodScoreMatrix(int scannedPhenotypeIndex)
    {
        if(this.singlePhenotypeScanned)
        {
            return this.getLodScoreMatrix(this.lodComponentAccessor);
        }
        else
        {
            return this.getLodScoreMatrix(
                    this.lodComponentAccessor + "[ , , " + (scannedPhenotypeIndex + 1) + "]");
        }
    }
    
//    /**
//     * Getter for the lod score matrix. The outer array is for rows and the
//     * inner array is for columns.
//     * @return
//     *          the matrix
//     */
//    public synchronized double[][] getLodScoreMatrix()
//    {
//        return this.getLodScoreMatrix(this.lodComponentAccessor);
//    }
    
    /**
     * Get the LOD score matrix
     * @param lodMatrixAccessor
     *          the R accessor string for the matrix
     * @return
     *          the matrix
     */
    private synchronized double[][] getLodScoreMatrix(String lodMatrixAccessor)
    {
        if(this.lodScoreMatrixCache.containsKey(lodMatrixAccessor))
        {
            return this.lodScoreMatrixCache.get(lodMatrixAccessor);
        }
        else
        {
            RObject lodComponent = new RObject(
                    this.getRInterface(),
                    lodMatrixAccessor);
            int numRows = JRIUtilityFunctions.getNumberOfRows(lodComponent);
            int numCols = JRIUtilityFunctions.getNumberOfColumns(lodComponent);
            
            double[][] lodScoreMatrix;
            if(numRows == 0)
            {
                LOG.warning("scantwo LOD scores are empty");
                lodScoreMatrix = new double[0][];
                this.lodScoreMatrixCache.put(
                        lodComponent.getAccessorExpressionString(),
                        lodScoreMatrix);
            }
            else if(numRows != numCols)
            {
                LOG.severe(
                        "scantwo rows are different than scantwo columns: cols=" +
                		numCols + ", rows=" + numRows);
                lodScoreMatrix = new double[0][];
                this.lodScoreMatrixCache.put(
                        lodComponent.getAccessorExpressionString(),
                        lodScoreMatrix);
            }
            else
            {
                lodScoreMatrix = new double[numRows][];
                for(int currRow = 0; currRow < numRows; currRow++)
                {
                    String currRowAccessor = RUtilities.rowIndexExpression(
                            lodMatrixAccessor,
                            currRow);
                    REXP currRowExpression =
                        this.getRInterface().evaluateCommand(
                                new SilentRCommand(currRowAccessor));
                    lodScoreMatrix[currRow] =
                        currRowExpression.asDoubleArray();
                }
                
                this.lodScoreMatrixCache.put(
                        lodComponent.getAccessorExpressionString(),
                        lodScoreMatrix);
            }
            
            return lodScoreMatrix;
        }
    }
    
    /**
     * Get the scanonex data for the given phenotype index
     * @param scannedPhenotypeIndex
     *          the phenotype index to use. this index represents the order
     *          that the phenotype is given in the scan, this is probably
     *          different from the index the phenotype has in the cross
     * @return
     *          the scanoneX lod array
     */
    public synchronized double[] getScanOneX(int scannedPhenotypeIndex)
    {
        if(this.singlePhenotypeScanned)
        {
            return this.getScanOneX(this.scanoneXComponentAccessor);
        }
        else
        {
            return this.getScanOneX(
                    this.scanoneXComponentAccessor + "[ , " + (scannedPhenotypeIndex + 1) + "]");
        }
    }
    
    /**
     * Getter for the scanone X values
     * @return
     *          the scanone X values
     */
    // TODO documentation says:
    // The final component is a version of the results of scanone including sex
    // and/or cross direction as additive covariates, which is needed for a
    // proper calculation of conditional LOD scores. 
    private synchronized double[] getScanOneX(String scanoneXArrayAccessor)
    {
        if(this.scanOneXCache.containsKey(scanoneXArrayAccessor))
        {
            return this.scanOneXCache.get(scanoneXArrayAccessor);
        }
        else
        {
            String scanOneXValuesAccessor = RUtilities.columnIndexExpression(
                    scanoneXArrayAccessor,
                    0);
            REXP scanoneValuesExpression = this.getRInterface().evaluateCommand(
                    new SilentRCommand(scanOneXValuesAccessor));
            double[] scanOneXArray = scanoneValuesExpression.asDoubleArray();
            this.scanOneXCache.put(scanoneXArrayAccessor, scanOneXArray);
            
            return scanOneXArray;
        }
    }
    
    /**
     * Get the genetic markers seperated into per-chromosome lists
     * @return
     *          the markers
     */
    public List<List<ScanTwoGeneticMarker>> getGeneticMarkersPerChromosome()
    {
        List<ScanTwoGeneticMarker> allMarkers = this.getGeneticMarkers();
        List<List<ScanTwoGeneticMarker>> markersPerChromosome =
            new ArrayList<List<ScanTwoGeneticMarker>>();
        Map<String, List<ScanTwoGeneticMarker>> markerListCache =
            new HashMap<String, List<ScanTwoGeneticMarker>>();
        
        for(ScanTwoGeneticMarker geneticMarker: allMarkers)
        {
            List<ScanTwoGeneticMarker> currMarkerList =
                markerListCache.get(geneticMarker.getChromosomeName());
            if(currMarkerList == null)
            {
                currMarkerList = new ArrayList<ScanTwoGeneticMarker>();
                markerListCache.put(
                        geneticMarker.getChromosomeName(),
                        currMarkerList);
                markersPerChromosome.add(currMarkerList);
            }
            
            currMarkerList.add(geneticMarker);
        }
        
        return markersPerChromosome;
    }
    
    /**
     * Organizes the markers returned by {@link #getGeneticMarkers()} into
     * a mapping where chromosome name is the key
     * @return
     *          the mapping
     */
    public Map<String, List<GeneticMarker>> getChromosomeNameToMarkerMap()
    {
        Map<String, List<GeneticMarker>> chromoNameToMarkerMap =
            new HashMap<String, List<GeneticMarker>>();
        List<ScanTwoGeneticMarker> markerList = this.getGeneticMarkers();
        
        for(ScanTwoGeneticMarker currMarker: markerList)
        {
            List<GeneticMarker> chromoMarkerList = chromoNameToMarkerMap.get(
                    currMarker.getChromosomeName());
            if(chromoMarkerList == null)
            {
                chromoMarkerList = new ArrayList<GeneticMarker>();
                chromoNameToMarkerMap.put(
                        currMarker.getChromosomeName(),
                        chromoMarkerList);
            }
            
            chromoMarkerList.add(currMarker);
        }
        
        return chromoNameToMarkerMap;
    }
    
    /**
     * Get the 1D marker count
     * @return
     *          the marker count
     */
    public int getMarkerCount()
    {
        return this.getGeneticMarkers().size();
    }
    
    /**
     * Getter for the genetic markers
     * @return
     *          the markers
     */
    public synchronized List<ScanTwoGeneticMarker> getGeneticMarkers()
    {
        if(this.geneticMarkersCache == null)
        {
            String[] markerNames =
                JRIUtilityFunctions.getRowNames(new RObject(
                        this.getRInterface(),
                        this.mapComponentAccessor));
            
            String markerPositionsAccessor =
                this.mapComponentAccessor + "$pos";
            REXP markerPositionsExpression = this.getRInterface().evaluateCommand(
                    new SilentRCommand(markerPositionsAccessor));
            double[] markerPositions =
                markerPositionsExpression.asDoubleArray();
            
            String isXChromosomeAccessor =
                this.mapComponentAccessor + "$xchr";
            REXP isXChromosomeExpression = this.getRInterface().evaluateCommand(
                    new SilentRCommand(isXChromosomeAccessor));
            boolean[] isXChromosomeValues =
                JRIUtilityFunctions.extractBooleanValues(isXChromosomeExpression);
            
            String[] chromosomeNames = this.getMarkerChromosomeNames();
            
            if(markerNames.length != markerPositions.length ||
               markerPositions.length != chromosomeNames.length)
            {
                LOG.severe("marker value arrays have different lengths");
                this.geneticMarkersCache = Collections.emptyList();
            }
            else
            {
                List<ScanTwoGeneticMarker> markers = new ArrayList<ScanTwoGeneticMarker>(
                        markerNames.length);
                
                for(int i = 0; i < markerNames.length; i++)
                {
                    ScanTwoGeneticMarker currGeneticMarker = new ScanTwoGeneticMarker(
                            markerNames[i],
                            chromosomeNames[i],
                            markerPositions[i],
                            isXChromosomeValues[i]);
                    markers.add(currGeneticMarker);
                }
                
                this.geneticMarkersCache = markers;
            }
        }
        
        return this.geneticMarkersCache;
    }
    
    /**
     * Getter for the marker chromosome names
     * @return
     *          the marker chromosome names
     */
    private String[] getMarkerChromosomeNames()
    {
        String markerChromosomesAccessor =
            this.mapComponentAccessor + "$chr";
        REXP markerChromosomeExpression = this.getRInterface().evaluateCommand(
                new SilentRCommand(markerChromosomesAccessor));
        return JRIUtilityFunctions.extractStringArrayFromFactor(
                markerChromosomeExpression);
    }
    
    /**
     * Getter for the unique chromosome name
     * @return
     *          the unique chromosome names
     */
    public synchronized String[] getScannedChromosomeNames()
    {
        if(this.scannedChromosomeNamesCache == null)
        {
            ArrayList<String> chromosomeNames = new ArrayList<String>();
            for(String currChromoName: this.getMarkerChromosomeNames())
            {
                if(!chromosomeNames.contains(currChromoName))
                {
                    chromosomeNames.add(currChromoName);
                }
            }
            
            this.scannedChromosomeNamesCache =
                chromosomeNames.toArray(new String[chromosomeNames.size()]);
        }
        
        return this.scannedChromosomeNamesCache;
    }
    
    /**
     * Class for holding the typical marker information plus an X chromosome
     * indicator from the scantwo results
     */
    public class ScanTwoGeneticMarker extends GeneticMarker
    {
        private final boolean isXChromosome;

        /**
         * Constructor
         * @param markerName
         *          the name of the marker
         * @param chromosomeName
         *          the chromosome name
         * @param markerPositionCentimorgans
         *          the marker position
         * @param isXChromosome
         *          specifies whether or not this is for the x chromosome
         */
        public ScanTwoGeneticMarker(
                String markerName,
                String chromosomeName,
                double markerPositionCentimorgans,
                boolean isXChromosome)
        {
            super(markerName, chromosomeName, markerPositionCentimorgans);
            
            this.isXChromosome = isXChromosome;
        }
        
        /**
         * Getter for determining if this is the x chromosome or not
         * @return
         *          true iff this marker is on the X chromosome
         */
        public boolean isXChromosome()
        {
            return this.isXChromosome;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(ScanTwoResult otherScanTwoResults)
    {
        return this.getAccessorExpressionString().compareTo(
                otherScanTwoResults.getAccessorExpressionString());
    }

    /**
     * Determine if there are any scantwo results in the given data model
     * @param dataModel
     *          the data model to check for scantwo results
     * @return
     *          true iff there are any scantwo results
     */
    public static boolean anyScanTwoResultsExist(QtlDataModel dataModel)
    {
        Cross[] crosses = dataModel.getCrosses();
        for(Cross cross: crosses)
        {
            if(!cross.getScanTwoResults().isEmpty())
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get the number of chromosomes represented in this scantwo result
     * @return
     *          the number of chromosomes
     */
    public int getChromosomeCount()
    {
        return this.getGeneticMarkersPerChromosome().size();
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
}
