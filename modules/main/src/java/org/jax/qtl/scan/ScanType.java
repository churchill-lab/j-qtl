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
 * An enum for scanone vs scantwo
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum ScanType
{
    /**
     * Used if the scan was a scanone
     */
    SCANONE
    {
        /**
         * The method name for {@link ScanType#SCANONE}
         */
        public static final String METHOD_NAME = "scanone";
        
        /**
         * the text to display for this scan type
         */
        public static final String USER_TEXT = "One QTL Genome Scan";
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRMethodName()
        {
            return METHOD_NAME;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public PhenotypeDistribution[] getSupportedPhenotypeDistributions()
        {
            // we support all distributions
            return PhenotypeDistribution.values();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public ScanMethod[] getSupportedScanMethods()
        {
            // we support all scan methods
            return ScanMethod.values();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return USER_TEXT;
        }
    },
    
    /**
     * Used if the scan was a scantwo
     */
    SCANTWO
    {
        /**
         * The method name for {@link ScanType#SCANTWO}
         */
        public static final String METHOD_NAME = "scantwo";
        
        /**
         * the text to display for this scan type
         */
        public static final String USER_TEXT = "Two QTL Genome Scan";
        
        /**
         * the distributions we support
         */
        private final PhenotypeDistribution[] supportedPhenotypeDistributions =
            new PhenotypeDistribution[] {
                PhenotypeDistribution.NORMAL,
                PhenotypeDistribution.BINARY};
        
        /**
         * the scan methods we support
         */
        private final ScanMethod[] supportedScanMethods = new ScanMethod[] {
                ScanMethod.EM_ALGORITHM,
                ScanMethod.MULTIPLE_IMPUTATION,
                ScanMethod.HALEY_KNOTT_REGRESSION,
                ScanMethod.MARKER_REGRESSION,
                ScanMethod.MARKER_REGRESSION_IMPUTATION,
                ScanMethod.MARKER_REGRESSION_VITERBI};
        
        /**
         * {@inheritDoc}
         */
        @Override
        public ScanMethod[] getSupportedScanMethods()
        {
            return this.supportedScanMethods;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getRMethodName()
        {
            return METHOD_NAME;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public PhenotypeDistribution[] getSupportedPhenotypeDistributions()
        {
            return this.supportedPhenotypeDistributions;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return USER_TEXT;
        }
    };

    /**
     * Get the method name that should be used for this {@link ScanType}
     * @return
     *          the R method name
     */
    public abstract String getRMethodName();
    
    /**
     * Get all of the phenotype distributions supported by this {@link ScanType}
     * @return
     *          the supported distributions
     */
    public abstract PhenotypeDistribution[] getSupportedPhenotypeDistributions();
    
    /**
     * Get all of the scan methods supported by this {@link ScanType}
     * @return
     *          the supported scan methods
     */
    public abstract ScanMethod[] getSupportedScanMethods();
}
