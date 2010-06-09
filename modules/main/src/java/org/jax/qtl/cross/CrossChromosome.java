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
import java.util.logging.Logger;

import org.jax.analyticgraph.data.NamedCategoricalData;
import org.jax.analyticgraph.data.NamedRealData;
import org.jax.qtl.cross.Cross.AssumedCategoricalPhenotype;
import org.jax.qtl.cross.Cross.CrossSubType;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Holds cross specific chromosome information
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CrossChromosome extends RObject
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            CrossChromosome.class.getName());
    
    private final String chromosomeName;
    
    private final RObject markerDataRObject;
    
    private final RObject errorLodRObject;
    
    private final Cross containerCross;
    
    private final SexAwareGeneticMap sexAwareGeneticMap;
    
    /**
     * the genotype sub-component of any cross
     */
    public static final String GENO_COMPONENT = "$geno";
    
    /**
     * Constructor
     * @param containerCross
     *          the cross that this chromosome belongs to
     * @param chromosomeName
     *          the chromosome name
     */
    public CrossChromosome(
            Cross containerCross,
            String chromosomeName)
    {
        super(containerCross.getRInterface(),
              containerCross.getAccessorExpressionString() + GENO_COMPONENT +
              "$\"" + chromosomeName + "\"");
        this.containerCross = containerCross;
        this.chromosomeName = chromosomeName;
        this.sexAwareGeneticMap = new SexAwareGeneticMap(
                containerCross.getRInterface(),
                this.getAccessorExpressionString() + "$map",
                chromosomeName);
        this.markerDataRObject = new RObject(
                containerCross.getRInterface(),
                this.getAccessorExpressionString() + "$data");
        this.errorLodRObject = new RObject(
                containerCross.getRInterface(),
                this.getAccessorExpressionString() + "$errorlod");
    }
    
    /**
     * Get a handle on the R object backing the genotype data
     * @return
     *          the marker data object
     */
    public RObject getMarkerDataRObject()
    {
        return this.markerDataRObject;
    }

    /**
     * Getter for the chromosome name
     * @return the chromosomeName
     */
    public String getChromosomeName()
    {
        return this.chromosomeName;
    }

    /**
     * Use this getter to determine if the chromosome has sex specific maps,
     * or if there is just one sex agnostic map
     * @return
     *          true iff this chromosome has sex specific maps
     */
    public boolean getHasSexSpecificGenotypeMaps()
    {
        return this.sexAwareGeneticMap.getHasSexSpecificGenotypeMaps();
    }

    /**
     * Getter for the male map
     * @return
     *          the male map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false
     */
    public GeneticMap getMaleGeneticMap()
    {
        return this.sexAwareGeneticMap.getMaleGeneticMap();
    }

    /**
     * Getter for the female map
     * @return
     *          the female map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false
     */
    public GeneticMap getFemaleGeneticMap()
    {
        return this.sexAwareGeneticMap.getFemaleGeneticMap();
    }

    /**
     * Getter for the sex agnostic map
     * @return
     *          the sex agnostic map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is true
     */
    public GeneticMap getSexAgnosticGeneticMap()
    {
        return this.sexAwareGeneticMap.getSexAgnosticGeneticMap();
    }
    
    /**
     * Gets any valid genetic map.
     * @return
     *          the female genetic map if
     *          {@link #getHasSexSpecificGenotypeMaps()} is true, and
     *          the sex agnostic map if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false.
     *          This should never be a null value.
     */
    public GeneticMap getAnyGeneticMap()
    {
        return this.sexAwareGeneticMap.getAnyGeneticMap();
    }
    
    /**
     * Determines if this is the sex chromosome (X).
     * @return
     *          true iff this is the sex chromosome
     */
    public boolean isXChromosome()
    {
        return JRIUtilityFunctions.inheritsRClass(this, "X") ||
               JRIUtilityFunctions.inheritsRClass(this, "x");
    }
    
    /**
     * Get the names of all of the markers on this chromosome
     * @return
     *          the marker names
     */
    public String[] getMarkerNames()
    {
        return JRIUtilityFunctions.getColumnNames(this.markerDataRObject);
    }
    
    /**
     * Get the marker genotypes for this chromosome.
     * @return
     *          a list of named categorical data where the names are marker
     *          names and 
     */
    public List<NamedCategoricalData> getMarkerGenotypes()
    {
        CrossSubType crossSubType = this.containerCross.getCrossSubType();
        
        String[] markerNames = this.getMarkerNames();
        List<NamedCategoricalData> markerGenotypes =
            new ArrayList<NamedCategoricalData>(markerNames.length);
        
        Integer[] paternalGrandmotherData = null;
        Integer[] sexData = null;
        if(this.isXChromosome())
        {
            NamedCategoricalData pgm =
                this.containerCross.getAssumedCategoricalPhenotype(
                        AssumedCategoricalPhenotype.PATERNAL_GRANDMOTHER);
            if(pgm != null)
            {
                paternalGrandmotherData = pgm.getCategoricalNumericalData();
            }
            
            NamedCategoricalData sex =
                this.containerCross.getAssumedCategoricalPhenotype(
                        AssumedCategoricalPhenotype.SEX);
            if(sex != null)
            {
                sexData = sex.getCategoricalNumericalData();
            }
        }
        
        for(int markerIndex = 0; markerIndex < markerNames.length; markerIndex++)
        {
            String currMarkerDataCommand =
                this.markerDataRObject.getAccessorExpressionString() +
                "[, " + (markerIndex + 1) + "]";
            REXP result = this.getRInterface().evaluateCommand(
                    new SilentRCommand(currMarkerDataCommand));
            double[] resultAsDoubles = result.asDoubleArray();
            Integer[] resultAsIntegers = new Integer[resultAsDoubles.length];
            for(int individualIndex = 0;
                individualIndex < resultAsDoubles.length;
                individualIndex++)
            {
                if(this.isInvalidRawGenotypeValue(resultAsDoubles[individualIndex]))
                {
                    resultAsIntegers[individualIndex] = null;
                }
                else
                {
                    // decrement by one since R uses 1 based indices
                    resultAsIntegers[individualIndex] = Integer.valueOf(
                            ((int)Math.round(resultAsDoubles[individualIndex]) - 1));
                    
                    // TODO move to doing enums instead of this "categorical"
                    //      junk
                    if(sexData != null)
                    {
                        if(sexData[individualIndex] == null)
                        {
                            LOG.warning(
                                    "sex data is null for individual #: " +
                                    individualIndex);
                        }
                        else if(sexData[individualIndex] == 1)
                        {
                            // see special rules for males on x
                            // as described in
                            // http://www.rqtl.org/manual/html/read.cross.html
                            if(resultAsIntegers[individualIndex] ==  1)
                            {
                                resultAsIntegers[individualIndex] = 2;
                            }
                        }
                        else if(sexData[individualIndex] == 0)
                        {
                            // give special treatment for female pgm==1 on x
                            // chromosome as described in
                            // http://www.rqtl.org/manual/html/read.cross.html
                            if(paternalGrandmotherData != null &&
                               paternalGrandmotherData[individualIndex] != null &&
                               paternalGrandmotherData[individualIndex] == 1 &&
                               resultAsIntegers[individualIndex] == 0)
                            {
                                resultAsIntegers[individualIndex] = 2;
                            }
                        }
                    }
                }
                
            }
            
            NamedCategoricalData currCategoricalData = new NamedCategoricalData(
                    markerNames[markerIndex],
                    resultAsIntegers,
                    crossSubType.getMarkerDataCategoricalValues(),
                    "Missing");
            markerGenotypes.add(currCategoricalData);
        }
        
        return markerGenotypes;
    }
    
    /**
     * Get the marker error lod values for this chromosome
     * @return
     *          the error lod values. if the error lods have not been calculated
     *          we return an empty list
     */
    public List<NamedRealData> getMarkerErrorLods()
    {
        String[] markerNames = this.getMarkerNames();
        List<NamedRealData> markerErrorLods =
            new ArrayList<NamedRealData>(markerNames.length);
        
        if(this.containerCross.getErrorLodsExist())
        {
            for(int i = 0; i < markerNames.length; i++)
            {
                String currMarkerDataCommand =
                    this.errorLodRObject.getAccessorExpressionString() +
                    "[, " + (i + 1) + "]";
                REXP result = this.getRInterface().evaluateCommand(
                        new SilentRCommand(currMarkerDataCommand));
                Double[] resultAsDoubles =
                    JRIUtilityFunctions.extractDoubleValues(result);
                NamedRealData currRealData = new NamedRealData(
                        markerNames[i],
                        resultAsDoubles);
                
                markerErrorLods.add(currRealData);
            }
        }
        
        return markerErrorLods;
    }
    
    /**
     * Determines if the given "raw" value from R is valid for this
     * chromosomes genotype
     * @param genotypeValue
     *          the raw genotype value
     * @return
     *          true iff it represents a valid value
     */
    private boolean isInvalidRawGenotypeValue(double genotypeValue)
    {
        int maxValue =
            this.containerCross.getCrossSubType().getMarkerDataCategoricalValues().length;
        return  Double.isNaN(genotypeValue) ||
                genotypeValue > maxValue ||
                genotypeValue < 1;
    }

    /**
     * Determine if error LOD values have been calculated for this chromosome
     * @return
     *          true iff the error lods exist
     */
    public boolean getErrorLodsExist()
    {
        return JRIUtilityFunctions.inheritsRClass(
                this.errorLodRObject,
                "matrix");
    }
}
