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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.analyticgraph.data.NamedCategoricalData;
import org.jax.analyticgraph.data.NamedData;
import org.jax.analyticgraph.data.NamedDataMatrix;
import org.jax.analyticgraph.data.NamedIntegerData;
import org.jax.analyticgraph.data.NamedRealData;
import org.jax.analyticgraph.data.SimpleSelectableNamedDataMatrix;
import org.jax.qtl.fit.FitQtlResult;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.r.RCommand;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.ObjectUtil;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;

/**
 * Represents an R cross object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class Cross extends RObject
{
    /**
     * Our logger
     */
    private static final Logger LOG = Logger.getLogger(Cross.class.getName());
    
    /**
     * the R type that crosses should carry
     */
    public static final String TYPE_STRING = "cross";
    
    /**
     * Possible cross sub-types in R/qtl
     */
    public static enum CrossSubType
    {
        /**
         * an F2 cross
         */
        F2
        {
            private final String[] markerCategoricalValues = new String[] {
                    "AA",       // 1
                    "AB",       // 2
                    "BB",       // 3
                    "Not BB",   // 4
                    "Not AA"};  // 5
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getTypeString()
            {
                return "f2";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getMarkerDataCategoricalValues()
            {
                return this.markerCategoricalValues;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Intercross";
            }
        },
        
        /**
         * a back-cross
         */
        BACK_CROSS
        {
            private final String[] markerCategoricalValues = new String[] {
                    "AA",       // 1
                    "AB"};      // 2
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getTypeString()
            {
                return "bc";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getMarkerDataCategoricalValues()
            {
                return this.markerCategoricalValues;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Backcross";
            }
        },
        
        /**
         * a 4-way cross
         */
        FOUR_WAY
        {
            private final String[] markerCategoricalValues = new String[] {
                    "AC",           // 1
                    "BC",           // 2
                    "AD",           // 3
                    "BD",           // 4
                    "A, AC or AD",  // 5
                    "B, BC or BD",  // 6
                    "C, AC or BC",  // 7
                    "D, AD or BD",  // 8
                    "AC or BD",     // 9
                    "AD or BC"};    // 10
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getTypeString()
            {
                return "4way";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getMarkerDataCategoricalValues()
            {
                return this.markerCategoricalValues;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Four-Way Cross";
            }
        };
        
        /**
         * Get the R type string for this sub-type
         * @return
         *          the type string
         */
        public abstract String getTypeString();
        
        /**
         * Get the marker data values for this sub-type. Please note that the
         * R values for marker data are from 1 to n, but these strings need
         * to be indexed from 0 to n-1
         * @return
         *          the ordered array of category strings
         */
        public abstract String[] getMarkerDataCategoricalValues();
    }
    
    /**
     * local copy of the phenotype data for this cross
     */
    private NamedDataMatrix<Number> phenotypeData;
    
    /**
     * @see #getGenotypeData()
     */
    private List<CrossChromosome> genotypeData;
    
    /**
     * @see #getQtlBasketMap()
     */
    private Map<String, QtlBasket> qtlBasketMap;
    
    /**
     * @see #getCrossSubType()
     */
    private CrossSubType crossSubType;
    
    private final Set<ScanOneResult> scanOneResults;
    
    private final Set<ScanTwoResult> scanTwoResults;
    
    private final Set<FitQtlResult> fitQtlResults;
    
    /**
     * for dealing with bean events
     */
    private final PropertyChangeSupport propertyChangeSupport;
    
    /**
     * the phenotype sub-component of any cross
     */
    private static final String PHENO_COMPONENT = "$pheno";
    
    /**
     * These are phenotypes that we just assume are categorical even
     * if the R type is not categorical. See R documentation for
     * read.cross(...) for more information
     */
    public static enum AssumedCategoricalPhenotype
    {
        /**
         * "pgm"
         */
        PATERNAL_GRANDMOTHER
        {
            private final String[] categoryNames = new String[] {
                "(?x?)x(AxB)",
                "(?x?)x(BxA)"};
            
            private final String columnHeader = "pgm";
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getCategoryNames()
            {
                return this.categoryNames;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getColumnHeader()
            {
                return this.columnHeader;
            }
        },
        
        /**
         * "sex"
         */
        SEX
        {
            private final String[] categoryNames = new String[] {
                "female",
                "male"};
            
            private final String columnHeader = "sex";
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String[] getCategoryNames()
            {
                return this.categoryNames;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getColumnHeader()
            {
                return this.columnHeader;
            }
        };
        
        /**
         * The column header that is used for this phenotype
         * @return
         *          the column header
         */
        public abstract String getColumnHeader();
        
        /**
         * Getter for the category names used for this enum
         * @see NamedCategoricalData#getCategoryNames()
         * @return
         *          the category names
         */
        public abstract String[] getCategoryNames();
        
        /**
         * Get the categorical phenotype with the given header
         * @param columnHeader
         *          the header
         * @return
         *          the matching phenotype or null if there are no matches
         */
        public static AssumedCategoricalPhenotype getCategoricalPhenotypeWithHeader(String columnHeader)
        {
            for(AssumedCategoricalPhenotype currPheno: AssumedCategoricalPhenotype.values())
            {
                if(currPheno.getColumnHeader().equalsIgnoreCase(columnHeader))
                {
                    return currPheno;
                }
            }
            
            return null;
        }
    }
    
    /**
     * the property string we use for phenotype data when we fire change
     * events
     */
    public static final String PHENOTYPE_DATA_PROPERTY_NAME = "phenotypeData";
    
    /**
     * the property string we use for genotype data when we fire change
     * events
     */
    public static final String GENOTYPE_DATA_PROPERTY_NAME = "genotypeData";
    
    /**
     * Constructor for an R backed cross object
     * @param accessorExpressionString
     *          the identifier string for this cross in R
     * @param rInterface
     *          the R interface that has all of the real data for this cross
     */
    public Cross(
            RInterface rInterface,
            String accessorExpressionString)
    {
        super(rInterface, accessorExpressionString);
        
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.qtlBasketMap = Collections.synchronizedMap(
                new HashMap<String, QtlBasket>());
        this.scanOneResults = Collections.synchronizedSet(
                new HashSet<ScanOneResult>());
        this.scanTwoResults = Collections.synchronizedSet(
                new HashSet<ScanTwoResult>());
        this.fitQtlResults = Collections.synchronizedSet(
                new HashSet<FitQtlResult>());
        
        this.updateAll();
    }

    /**
     * Add a property listener
     * @param listener
     *          the listener
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     * @see #GENOTYPE_DATA_PROPERTY_NAME
     * @see #PHENOTYPE_DATA_PROPERTY_NAME
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Add a property listener
     * @param propertyName
     *          the property to listen to
     * @param listener
     *          the listener
     * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
     * @see #GENOTYPE_DATA_PROPERTY_NAME
     * @see #PHENOTYPE_DATA_PROPERTY_NAME
     */
    public void addPropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(
                propertyName,
                listener);
    }
    
    /**
     * Remove a property listener
     * @param listener
     *          the listener
     * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     * @see #GENOTYPE_DATA_PROPERTY_NAME
     * @see #PHENOTYPE_DATA_PROPERTY_NAME
     */
    public void removePropertyChangeListener(
            PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Remove a property listener
     * @param propertyName
     *          the property to stop listening to
     * @param listener
     *          the listener
     * @see PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
     * @see #GENOTYPE_DATA_PROPERTY_NAME
     * @see #PHENOTYPE_DATA_PROPERTY_NAME
     */
    public void removePropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(
                propertyName,
                listener);
    }

    /**
     * Call this method when all state data for this cross should be updated.
     */
    private void updateAll()
    {
        // update the cross sub-type
        for(CrossSubType currSubType: CrossSubType.values())
        {
            if(JRIUtilityFunctions.inheritsRClass(
                    this,
                    currSubType.getTypeString()))
            {
                this.crossSubType = currSubType;
                break;
            }
        }
        
        this.updatePhenotypeData();
        this.updateGenotypeData();
    }

    /**
     * Update the genotype data
     */
    private void updateGenotypeData()
    {
        String chromosomeNamesCommandString =
            "names(" + this.getAccessorExpressionString() +
            CrossChromosome.GENO_COMPONENT + ")";
        REXP chromosomeNamesRexp = this.getRInterface().evaluateCommand(new SilentRCommand(
                chromosomeNamesCommandString));
        String[] chromosomeNames = chromosomeNamesRexp.asStringArray();
        List<CrossChromosome> genotypeData =
            new ArrayList<CrossChromosome>();
        for(String chromosomeName: chromosomeNames)
        {
            genotypeData.add(new CrossChromosome(
                    this,
                    chromosomeName));
        }
        this.setGenotypeData(genotypeData);
    }

    /**
     * Set the genotype data 
     * @param genotypeData
     *          the new genotype data
     */
    private void setGenotypeData(List<CrossChromosome> genotypeData)
    {
        List<CrossChromosome> oldGenotypeData = this.genotypeData;
        this.genotypeData = genotypeData;
        this.propertyChangeSupport.firePropertyChange(
                GENOTYPE_DATA_PROPERTY_NAME,
                oldGenotypeData,
                genotypeData);
    }
    
    /**
     * Get the index of the chromosome with the given name or -1 if we
     * can't find the index
     * @param chromosomeName
     *          the chromosome name that we're looking for
     * @return
     *          the index or -1 if we can't find it
     */
    public int getIndexOfChromosomeNamed(String chromosomeName)
    {
        List<CrossChromosome> genoData = this.getGenotypeData();
        for(int i = 0; i < genoData.size(); i++)
        {
            CrossChromosome currCrossChromosome = genoData.get(i);
            if(currCrossChromosome.getChromosomeName().equals(chromosomeName))
            {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Getter for the genotype data
     * @return
     *          the genotype data
     */
    public List<CrossChromosome> getGenotypeData()
    {
        return this.genotypeData;
    }
    
    /**
     * Getter for the phenotype data
     * @return
     *          the phenotype data
     */
    public NamedDataMatrix<Number> getPhenotypeData()
    {
        return this.phenotypeData;
    }
    
    /**
     * Get the QTL baskets
     * @return
     *          the QTL baskets
     */
    public Map<String, QtlBasket> getQtlBasketMap()
    {
        return this.qtlBasketMap;
    }
    
    /**
     * Getter for the qtl baskets
     * @see #getQtlBasketMap()
     * @return
     *          the QTL baskets
     */
    public QtlBasket[] getQtlBaskets()
    {
        synchronized(this.qtlBasketMap)
        {
            QtlBasket[] baskets = new QtlBasket[this.qtlBasketMap.size()];
            return this.qtlBasketMap.values().toArray(baskets);
        }
    }

    /**
     * Update the phenotype data
     */
    public void updatePhenotypeData()
    {
        NamedDataMatrix<Number> newPhenoData = Cross.getPhenotypeDataForCross(
                this.getRInterface(),
                this.getAccessorExpressionString());
        if(!ObjectUtil.areEqual(this.phenotypeData, newPhenoData))
        {
            NamedDataMatrix<Number> oldPhenoData = this.phenotypeData;
            this.phenotypeData = newPhenoData;
            this.propertyChangeSupport.firePropertyChange(
                    PHENOTYPE_DATA_PROPERTY_NAME,
                    oldPhenoData,
                    newPhenoData);
        }
    }

    /**
     * Getter for determining if "calc.genoprob" was run on this cross
     * @return
     *          true if it was run
     */
    public boolean getCalculateConditionalProbabilitiesWasUsed()
    {
        return Cross.methodWasUsed(
                this.getRInterface(),
                this.getAccessorExpressionString(),
                GenotypeProbabilityMethod.CALCULATE_CONDITIONAL_PROBABILITIES);
    }
    
    /**
     * Getter for determining if "sim.geno" was run on this cross
     * @return
     *          true if it was run
     */
    public boolean getSimulateGenotypeWasUsed()
    {
        return Cross.methodWasUsed(
                this.getRInterface(),
                this.getAccessorExpressionString(),
                GenotypeProbabilityMethod.SIMULATE_GENOTYPE);
    }
    
    /**
     * Determine if the given method was used on the given cross.
     * @param rInterface
     *          the R interface to use
     * @param crossIdentifier
     *          the identifier for the cross we're asking about
     * @param method
     *          the method that we're testing for
     * @return 
     *          true iff the method was used
     */
    public static boolean methodWasUsed(
            RInterface rInterface,
            String crossIdentifier,
            GenotypeProbabilityMethod method)
    {
        RCommand genoSubComponentsCommand =
            Cross.getGenotypeSubComponentsCommand(
                    crossIdentifier,
                    0);
        REXP resultExpression = rInterface.evaluateCommand(genoSubComponentsCommand);
        String[] genoSubComponents =
            resultExpression.asStringArray();
        for(String currGenoSubComponent: genoSubComponents)
        {
            if(currGenoSubComponent.equals(method.getGenoSubComponentName()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the command string to fetch all genotype subcomponent names
     * @param crossIdentifier
     *          the cross
     * @param chromosomeNumber
     *          the chromosome that we want the names for (0 based index)
     * @return
     *          the command needed to get the names
     */
    private static RCommand getGenotypeSubComponentsCommand(
            String crossIdentifier,
            int chromosomeNumber)
    {
        return new SilentRCommand(
                "names(" + crossIdentifier + CrossChromosome.GENO_COMPONENT +
                "[[" + (chromosomeNumber + 1) + "]])");
    }
    
    /**
     * For the given cross in R, get the phenotype data as a matrix
     * @param rInterface
     *          the R interface to use
     * @param crossIdentifier
     *          the identifier for the cross that we're interested in
     * @return
     *          the phenotype data matrix
     */
    public static NamedDataMatrix<Number> getPhenotypeDataForCross(
            RInterface rInterface,
            String crossIdentifier)
    {
//        // get all information after read.cross() to create the cross
//        // pheno data
//        REXP x = rInterface.evaluateCommand("names(" + crossName + "$pheno)");
//        String[] phenoNames = rInterface.evaluateCommand("names(" + crossName + "$pheno)").
//            asStringArray();
//        int nphe = phenoNames.length;
//
//        for (int i = 0; i < nphe; i++) {
//            rcmd = crossName + "$pheno$" + phenoNames[i];
//            
//            // TODO make sure both conditions are unit tested
//            if (rInterface.evaluateCommand(rcmd).getType() == REXP.XT_ARRAY_DOUBLE) {
//                double[] phenoDouble = rInterface.evaluateCommand(rcmd).asDoubleArray();
//                pheno = convertPheno(phenoDouble);
//            }
//            else if (rInterface.evaluateCommand(rcmd).getType() == REXP.XT_FACTOR) {
//                RFactor phenoFactor = rInterface.evaluateCommand(rcmd).asFactor();
//                pheno = Tools.convertPheno(phenoFactor);
//            }
//            boolean isFactor = rInterface.evaluateCommand("is.factor(" + rcmd + ")").asBool().
//                isTRUE();
//            if (pd.getPhenoName().equalsIgnoreCase("sex") || pd.getPhenoName().equalsIgnoreCase("pgm"))
//                 isFactor = true;
//            pd.setFactor(isFactor);
//            cross.addPhenoData(pd);
//        }
        String phenotypeCommand = crossIdentifier + PHENO_COMPONENT;
        REXP phenotypesNamesExpression =
            rInterface.evaluateCommand(
                    new SilentRCommand("names(" + phenotypeCommand + ")"));
        String[] phenotypeNames = phenotypesNamesExpression.asStringArray();
        List<NamedData<Number>> namedPhenotypeData =
            new ArrayList<NamedData<Number>>(phenotypeNames.length);
        for(int i = 0; i < phenotypeNames.length; i++)
        {
            String currPhenotypeCommand =
                phenotypeCommand + "$" + phenotypeNames[i];
            REXP currPhenotypeExpression =
                rInterface.evaluateCommand(
                        new SilentRCommand(currPhenotypeCommand));
            
            // TODO: need to deal with pgm and sex special cases
            // TODO test all of these conditions
            if(currPhenotypeExpression.getType() == REXP.XT_FACTOR)
            {
                // TODO this should probably be a separate function
                // TODO are there any R N/A values that we need to deal
                //      with here
                RFactor phenoFactor = currPhenotypeExpression.asFactor();
                int numFactors = phenoFactor.size();
                
                List<String> categoryNamesList = new ArrayList<String>();
                Integer[] categoryData = new Integer[numFactors];
                for(int j = 0; j < numFactors; j++)
                {
                    String currPhenoStrVal;
                    
                    // This try/catch is a workaround for a JRI problem
                    // reading NA factors
                    try
                    {
                        currPhenoStrVal = phenoFactor.at(j);
                    }
                    catch (IndexOutOfBoundsException ex)
                    {
                        LOG.log(Level.FINE,
                                "Caught index out of bounds. Assuming N/A phenotype");
                        currPhenoStrVal = null;
                    }
                    
                    int currPhenoCatVal = categoryNamesList.indexOf(
                            currPhenoStrVal);
                    if(currPhenoCatVal == -1)
                    {
                        currPhenoCatVal = categoryNamesList.size();
                        categoryNamesList.add(currPhenoStrVal);
                    }
                    
                    categoryData[j] = Integer.valueOf(currPhenoCatVal);
                }
                
                NamedCategoricalData currPhenotypeData =
                    new NamedCategoricalData(
                            phenotypeNames[i],
                            categoryData,
                            categoryNamesList.toArray(
                                    new String[categoryNamesList.size()]));
                namedPhenotypeData.add(currPhenotypeData);
            }
            else
            {
                AssumedCategoricalPhenotype matchingAssumedCategoricalPheno =
                    AssumedCategoricalPhenotype.getCategoricalPhenotypeWithHeader(
                            phenotypeNames[i]);
                if(matchingAssumedCategoricalPheno != null)
                {
                    NamedCategoricalData currPhenotypeData =
                        new NamedCategoricalData(
                                phenotypeNames[i],
                                JRIUtilityFunctions.extractIntegerValues(
                                        currPhenotypeExpression),
                                matchingAssumedCategoricalPheno.getCategoryNames());
                    namedPhenotypeData.add(currPhenotypeData);
                }
                else if(currPhenotypeExpression.getType() == REXP.XT_ARRAY_DOUBLE)
                {
                    Double[] currPhenotypeValues;
                    {
                        double[] currPhenotypePrimitiveValues =
                            currPhenotypeExpression.asDoubleArray();
                        currPhenotypeValues =
                            new Double[currPhenotypePrimitiveValues.length];
                        for(int j = 0; j < currPhenotypePrimitiveValues.length; j++)
                        {
                            if(Double.isNaN(currPhenotypePrimitiveValues[j]))
                            {
                                currPhenotypeValues[j] = null;
                            }
                            else
                            {
                                currPhenotypeValues[j] =
                                    Double.valueOf(currPhenotypePrimitiveValues[j]);
                            }
                        }
                    }
                    
                    NamedRealData currPhenotypeData = new NamedRealData(
                            phenotypeNames[i],
                            currPhenotypeValues);
                    namedPhenotypeData.add(currPhenotypeData);
                }
                else if(currPhenotypeExpression.getType() == REXP.XT_ARRAY_INT)
                {
                    int[] currPhenotypeValues =
                        currPhenotypeExpression.asIntArray();
                    NamedIntegerData currPhenotypeData = new NamedIntegerData(
                            phenotypeNames[i],
                            currPhenotypeValues);
                    namedPhenotypeData.add(currPhenotypeData);
                }
                else
                {
                    // don't know what to do with this one
                    // TODO should probably throw an exception if this happens
                    LOG.severe(
                            "don't know how to translate expression of type: " +
                            currPhenotypeExpression.getType());
                }
            }
        }
        
        return new SimpleSelectableNamedDataMatrix<Number>(
                namedPhenotypeData);
    }

    /**
     * Getter for the cross sub-type
     * @return
     *          the cross sub-type
     */
    public CrossSubType getCrossSubType()
    {
        return this.crossSubType;
    }
    
    /**
     * Gets the number of individuals in this cross
     * @return
     *          the number of individuals
     */
    public int getNumberOfIndividuals()
    {
        String commandString =
            "nind(" + this.getAccessorExpressionString() + ")";
        SilentRCommand silentCommand = new SilentRCommand(commandString);
        return this.getRInterface().evaluateCommand(silentCommand).asInt();
    }
    
    /**
     * Get the number of chromosomes for this cross
     * @return
     *          the number or chromosomes
     */
    public int getNumberOfChromosomes()
    {
        String commandString =
            "nchr(" + this.getAccessorExpressionString() + ")";
        SilentRCommand silentCommand = new SilentRCommand(commandString);
        return this.getRInterface().evaluateCommand(silentCommand).asInt();
    }
    
    /**
     * Get the number of phenotypes for this cross
     * @return
     *          the number of phenotypes
     */
    public int getNumberOfPhenotypes()
    {
        String commandString =
            "nphe(" + this.getAccessorExpressionString() + ")";
        SilentRCommand silentCommand = new SilentRCommand(commandString);
        return this.getRInterface().evaluateCommand(silentCommand).asInt();
    }
    
    /**
     * Get the number of markers for this cross
     * @return
     *          the number of markers
     */
    public int[] getNumberOfMarkers()
    {
        String commandString =
            "nmar(" + this.getAccessorExpressionString() + ")";
        SilentRCommand silentCommand = new SilentRCommand(commandString);
        return this.getRInterface().evaluateCommand(silentCommand).asIntArray();
    }
    
    /**
     * Calculate error LOD values
     * @see #getErrorLodsExist()
     * @see CrossChromosome#getMarkerErrorLods()
     */
    public void calculateErrorLods()
    {
        String errorLodComment =
            "calculating error LOD values";
        String errorLodCommand =
            this.getAccessorExpressionString() +
            " <- calc.errorlod(" +
            this.getAccessorExpressionString() + ")";
        
        this.getRInterface().insertComment(errorLodComment);
        this.getRInterface().evaluateCommandNoReturn(errorLodCommand);
    }
    
    /**
     * Get all scan one results that belong to this cross
     * @return
     *          the scan results
     */
    public Set<ScanOneResult> getScanOneResults()
    {
        // grab all scanone results
        List<RObject> allScanoneResultRObjects = JRIUtilityFunctions.getTopLevelObjectsOfType(
                this.getRInterface(),
                ScanOneResult.SCANONE_RESULT_TYPE_STRING);
        
        // filter out any scanone results that don't belong to this cross
        this.removeObjectsNotOwnedByThis(allScanoneResultRObjects);
        Set<ScanOneResult> matchingScanoneResults =
            new HashSet<ScanOneResult>();
        for(RObject currScanoneRObject: allScanoneResultRObjects)
        {
            matchingScanoneResults.add(new ScanOneResult(
                    currScanoneRObject.getRInterface(),
                    currScanoneRObject.getAccessorExpressionString(),
                    this));
        }
        
        this.scanOneResults.retainAll(matchingScanoneResults);
        matchingScanoneResults.removeAll(this.scanOneResults);
        this.scanOneResults.addAll(matchingScanoneResults);
        
        // return a copy of the matches
        return new HashSet<ScanOneResult>(this.scanOneResults);
    }
    
    /**
     * Get the scantwo results
     * @return
     *          the resutls
     */
    public Set<ScanTwoResult> getScanTwoResults()
    {
        // grab all scantwo results
        List<RObject> allScantwoResultRObjects = JRIUtilityFunctions.getTopLevelObjectsOfType(
                this.getRInterface(),
                ScanTwoResult.SCANTWO_RESULT_TYPE_STRING);
        
        // filter out any scantwo results that don't belong to this cross
        this.removeObjectsNotOwnedByThis(allScantwoResultRObjects);
        Set<ScanTwoResult> matchingScantwoResults =
            new HashSet<ScanTwoResult>();
        for(RObject currScantwoRObject: allScantwoResultRObjects)
        {
            matchingScantwoResults.add(new ScanTwoResult(
                    currScantwoRObject.getRInterface(),
                    currScantwoRObject.getAccessorExpressionString(),
                    this));
        }
        
        this.scanTwoResults.retainAll(matchingScantwoResults);
        matchingScantwoResults.removeAll(this.scanTwoResults);
        this.scanTwoResults.addAll(matchingScantwoResults);
        
        // return a copy of the matches
        return new HashSet<ScanTwoResult>(this.scanTwoResults);
    }
    
    /**
     * Getter for the fit results
     * @return
     *          the fit results
     */
    public Set<FitQtlResult> getFitQtlResults()
    {
        // grab all of the fit results
        List<RObject> fitQtlRObjects = JRIUtilityFunctions.getTopLevelObjectsOfType(
                this.getRInterface(),
                FitQtlResult.FIT_QTL_RESULT_TYPE_STRING);
        
        // filter out and fit's that don't belong to this cross
        this.removeObjectsNotOwnedByThis(fitQtlRObjects);
        Set<FitQtlResult> matchingFitQtlResults =
            new HashSet<FitQtlResult>(fitQtlRObjects.size());
        for(RObject currFitRObject: fitQtlRObjects)
        {
            matchingFitQtlResults.add(new FitQtlResult(
                    currFitRObject.getRInterface(),
                    currFitRObject.getAccessorExpressionString(),
                    this));
        }
        
        this.fitQtlResults.retainAll(matchingFitQtlResults);
        matchingFitQtlResults.removeAll(this.fitQtlResults);
        this.fitQtlResults.addAll(matchingFitQtlResults);
        
        // return a copy of the matches
        return new HashSet<FitQtlResult>(this.fitQtlResults);
    }
    
    /**
     * This function determines whether or not the error lod values have
     * been calculated or not
     * @return
     *          true iff the error lods have been calculated
     */
    public boolean getErrorLodsExist()
    {
        if(this.genotypeData.size() == 0)
        {
            return false;
        }
        else
        {
            CrossChromosome firstChromosome = this.genotypeData.get(0);
            return firstChromosome.getErrorLodsExist();
        }
    }
    
    /**
     * Get the data for "pgm" or "sex" if it's available
     * @param phenotype
     *          the phenotype we're looking for
     * @return
     *          the paternal grandmother phenotype, sex phenotype or null if the
     *          data are not available
     */
    public NamedCategoricalData getAssumedCategoricalPhenotype(
            AssumedCategoricalPhenotype phenotype)
    {
        String header = phenotype.getColumnHeader();
        for(NamedData<Number> currData: this.phenotypeData.getNamedDataList())
        {
            if(currData.getNameOfData().equalsIgnoreCase(header))
            {
                if(currData instanceof NamedCategoricalData)
                {
                    return (NamedCategoricalData)currData;
                }
                else
                {
                    LOG.warning(
                            currData.getNameOfData() + " was expected to " +
                            "be categorical data but it is actually: " +
                            currData.getClass().getName());
                    return null;
                }
            }
        }
        
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ObjectUtil.hashObject(this.getAccessorExpressionString());
    }
}
