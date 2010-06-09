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

/**
 * A scan one threshold value.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneThreshold
{
    private final double alphaThreshold;
    
    private final double autosomeLodValue;
    
    private final double xChromosomeLodValue;

    private final boolean xChromosomePValuesAreSeparate;

    /**
     * Constructor to use when the x chromosome lods are the same as the
     * autosome lods. This constructor sets the
     * {@link #getXChromosomePValuesAreSeparate()} property to false
     * @param alphaThreshold
     *          the alpha value
     * @param lodValue
     *          the threshold
     */
    public ScanOneThreshold(
            double alphaThreshold,
            double lodValue)
    {
        this.alphaThreshold = alphaThreshold;
        this.autosomeLodValue = lodValue;
        this.xChromosomeLodValue = lodValue;
        this.xChromosomePValuesAreSeparate = false;
    }

    /**
     * Constructor to use when x and autosome lods are separate. This
     * constructor sets the
     * {@link #getXChromosomePValuesAreSeparate()} property to true
     * @param alphaThreshold
     *          the alpha
     * @param autosomeLodValue
     *          the autosome lod
     * @param chromosomeLodValue
     *          the chromosome lod
     */
    public ScanOneThreshold(
            double alphaThreshold,
            double autosomeLodValue,
            double chromosomeLodValue)
    {
        this.alphaThreshold = alphaThreshold;
        this.autosomeLodValue = autosomeLodValue;
        this.xChromosomeLodValue = chromosomeLodValue;
        this.xChromosomePValuesAreSeparate = true;
    }

    /**
     * Getter for the alpha value that was used
     * @return
     *          the alpha
     */
    public double getAlphaThreshold()
    {
        return this.alphaThreshold;
    }
    
    /**
     * Getter for the single LOD threshold. This function throws an exception
     * if this object wasn't constructed using the
     * {@link #ScanOneThreshold(double, double)} constructor
     * @return
     *          the threshold
     * @throws IllegalStateException
     *          if {@link #getXChromosomePValuesAreSeparate()} is true
     */
    public double getLodValue() throws IllegalStateException
    {
        return this.autosomeLodValue;
    }
    
    /**
     * Getter for the autosome lod score
     * @return
     *          the autosome lod score
     */
    public double getAutosomeLodValue()
    {
        return this.autosomeLodValue;
    }

    /**
     * Getter for the x chromosome lod threshold
     * @return
     *          the x chromosome lod threshold
     */
    public double getXChromosomeLodValue()
    {
        return this.xChromosomeLodValue;
    }
    
    /**
     * Getter for finding out if the x and autosome thresholds were
     * calculated separately
     * @return the xChromosomePValuesAreSeparate
     */
    public boolean getXChromosomePValuesAreSeparate()
    {
        return this.xChromosomePValuesAreSeparate;
    }
}
