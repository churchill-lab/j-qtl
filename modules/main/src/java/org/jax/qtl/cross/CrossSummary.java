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

import org.jax.qtl.Constants;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;


/**
 * An object that holds summary info about a {@link Cross}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class CrossSummary
{
    private final String crossAccessor;
    
    private final String crossType;
    
    private final int individualCount;
    
    private final double[] missingPhenotypeRatios;
    
    private final String[] autosomeNames;
    
    private final String xChromosomeName;
    
    private final int[] markersPerChromosome;
    
    private final double missingGenotypeRatio;
    
    private final double[] genotypeRatios;
    
    private final Cross cross;
    
    /**
     * Constructor
     * @param crossSummaryCommand
     *          the summary command
     */
    public CrossSummary(CrossSummaryCommand crossSummaryCommand)
    {
        this.cross = crossSummaryCommand.getCross();
        
        this.crossAccessor = this.cross.getAccessorExpressionString();
        
        RInterface rInterface = this.cross.getRInterface();
        REXP summaryRExpression = rInterface.evaluateCommand(
                new SilentRCommand(crossSummaryCommand));
        RVector summaryRVector = summaryRExpression.asVector();
        
        this.crossType = this.getLongCrossTypeString(
                summaryRVector.at(0).asString());
        this.individualCount = summaryRVector.at(1).asInt();
        this.missingPhenotypeRatios = summaryRVector.at(7).asDoubleArray();
        if(summaryRVector.at(8).asString() == null)
        {
            this.autosomeNames = new String[0];
        }
        else
        {
            this.autosomeNames = summaryRVector.at(8).asStringArray();
        }
        this.xChromosomeName = summaryRVector.at(9).asString();
        this.markersPerChromosome = summaryRVector.at(4).asIntArray();
        this.missingGenotypeRatio = summaryRVector.at(5).asDouble();
        this.genotypeRatios = summaryRVector.at(6).asDoubleArray();
    }
    
    /**
     * Getter for the cross
     * @return the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }
    
    /**
     * Get the long cross string for the given short string
     * @param crossTypeShortString
     *        the short string
     * @return the long string
     */
    private String getLongCrossTypeString(String crossTypeShortString)
    {
        if(crossTypeShortString.equals("bc"))
        {
            return "Backcross";
        }
        else if(crossTypeShortString.equals("f2"))
        {
            return "F2 Intercross";
        }
        else
        {
            return "4-Way Cross";
        }
    }

    /**
     * Get the total # of chromosomes
     * @return
     *          the total
     */
    public int getTotalMarkerCount()
    {
        int totalCount = 0;
        for(int currCount: this.markersPerChromosome)
        {
            totalCount += currCount;
        }
        
        return totalCount;
    }

    /**
     * Getter for the cross accessor string
     * @return
     *          the accessor
     */
    public String getCrossAccessor()
    {
        return this.crossAccessor;
    }

    /**
     * Get the cross type string
     * @return
     *          the cross type string
     */
    public String getCrossType()
    {
        return this.crossType;
    }

    /**
     * Get the individual count
     * @return
     *          the individual count
     */
    public int getIndividualCount()
    {
        return this.individualCount;
    }

    /**
     * Get a string representation for the percent phenotyped
     * @return
     *          the % phenotyped string
     */
    public String getPercentPhenotypedString()
    {
        String result = "";
        int arrayLength = this.missingPhenotypeRatios.length;
        for (int i=0; i<arrayLength; i++)
        {
            result += Constants.ONE_DIGIT_FORMATTER.format(
                    (1 - this.missingPhenotypeRatios[i])*100) + "  ";
        }
        result += "%";
        return result;
    }
    
    /**
     * Get the missing phenotype ratios
     * @return
     *          the missing phenotype ratios
     */
    public double[] getMissingPhenotypeRatios()
    {
        return this.missingPhenotypeRatios;
    }
    
    /**
     * Getter for the autosome name string
     * @return
     *          the autosome name string
     */
    public String getAutosomeNamesString()
    {
        String result = "";
        int arrayLength = this.autosomeNames.length;
        for (int i=0; i<arrayLength; i++)
        {
            result += this.autosomeNames[i] + "  ";
        }
        return result;
    }

    /**
     * Getter for the autosome names
     * @return
     *          the autosome names
     */
    public String[] getAutosomeNames()
    {
        return this.autosomeNames;
    }

    /**
     * Getter for the x chromosomes name
     * @return
     *          the x chromosome name
     */
    public String getXChromosomeName()
    {
        return this.xChromosomeName;
    }
    
    /**
     * Get the marker counts as a string
     * @return
     *          the marker counts
     */
    public String getMarkersPerChromosomeString()
    {
        String result = "";
        int arrayLength = this.markersPerChromosome.length;
        for (int i=0; i<arrayLength; i++)
        {
            result += this.markersPerChromosome[i] + "  ";
        }
        return result;
    }

    /**
     * Get the marker count per chromosome
     * @return
     *          the marker count per chromosome
     */
    public int[] getMarkersPerChromosome()
    {
        return this.markersPerChromosome;
    }
    
    /**
     * Getter for the percent genotyped string
     * @return
     *          the percentage genotyped
     */
    public String getPercentGenotypedString()
    {
        return Constants.ONE_DIGIT_FORMATTER.format(
                (1-this.missingGenotypeRatio)*100) + "%";
    }

    /**
     * Get the missing genotype ratio
     * @return
     *          the missing genotype ratio
     */
    public double getMissingGenotypeRatio()
    {
        return this.missingGenotypeRatio;
    }
    
    /**
     * Getter for the genotype ratios string
     * @return
     *          the genotype ratios string
     */
    public String getGenotypeRatiosString()
    {
        Cross cross = this.getCross();
        String[] markerValues =
            cross.getCrossSubType().getMarkerDataCategoricalValues();
        StringBuffer stringBuffer = new StringBuffer();
        for(int genoIndex = 0; genoIndex < this.genotypeRatios.length; genoIndex++)
        {
            stringBuffer.append(
                    markerValues[genoIndex] + ":" +
                    Constants.ONE_DIGIT_FORMATTER.format(
                            (this.genotypeRatios[genoIndex])*100) + "  ");
        }
        
        return stringBuffer.toString();
    }

    /**
     * Get the genotype ratios
     * @return
     *          the genotype ratios
     */
    public double[] getGenotypeRatios()
    {
        return this.genotypeRatios;
    }
}
