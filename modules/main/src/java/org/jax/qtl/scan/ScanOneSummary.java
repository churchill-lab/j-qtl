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

import org.jax.qtl.cross.GeneticMarker;

/**
 * A java representation of the R scanone summary object
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneSummary
{
    /**
     * A simple type for a scanone summary row
     */
    public static class ScanOneSummaryRow
    {
        private final GeneticMarker marker;
        
        private final double lodScore;
        
        private final double pValue;

        /**
         * Constructor
         * @param marker
         *          see {@link #getMarker()}
         * @param lodScore
         *          see {@link #getLodScore()}
         * @param pValue
         *          see {@link #getPValue()}
         */
        public ScanOneSummaryRow(
                GeneticMarker marker,
                double lodScore,
                double pValue)
        {
            this.marker = marker;
            this.lodScore = lodScore;
            this.pValue = pValue;
        }
        
        /**
         * Getter for the marker
         * @return the marker
         */
        public GeneticMarker getMarker()
        {
            return this.marker;
        }
        
        /**
         * Getter for the lod score
         * @return the lodScore
         */
        public double getLodScore()
        {
            return this.lodScore;
        }
        
        /**
         * Getter for the pvalue
         * @return the pValue
         */
        public double getPValue()
        {
            return this.pValue;
        }
    }
    
    private final ScanOneSummaryRow[] scanOneSummaryRows;
    
    private final boolean pValuesAreValid;

    /**
     * Constructor
     * @param scanOneSummaryRows
     *          see {@link #getScanOneSummaryRows()}
     * @param pValuesAreValid
     *          see {@link #getPValuesAreValid()}
     */
    public ScanOneSummary(
            ScanOneSummaryRow[] scanOneSummaryRows,
            boolean pValuesAreValid)
    {
        this.scanOneSummaryRows = scanOneSummaryRows;
        this.pValuesAreValid = pValuesAreValid;
    }
    
    /**
     * Getter for determining if the p-values are valid or not
     * @return
     *          true iff the p-values are valid
     */
    public boolean getPValuesAreValid()
    {
        return this.pValuesAreValid;
    }
    
    /**
     * Getter for the scan one summary rows
     * @return
     *          the scan one summary rows
     */
    public ScanOneSummaryRow[] getScanOneSummaryRows()
    {
        return this.scanOneSummaryRows;
    }
}
