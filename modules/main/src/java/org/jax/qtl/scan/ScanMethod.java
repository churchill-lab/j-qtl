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
 * An enumeration for the type of scan that should be performed.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum ScanMethod
{
    /**
     * Expectation maximization
     */
    EM_ALGORITHM
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "em";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "EM Algorithm (Maximum Likelihood)";
        }
    },
    
    /**
     * Multiple imputation (Churchill)
     */
    MULTIPLE_IMPUTATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "imp";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Multiple Imputation";
        }
    },
    
    /**
     * 
     */
    HALEY_KNOTT_REGRESSION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "hk";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Haley-Knott Regression";
        }
    },
    
    /**
     * 
     */
    EXTENDED_HALEY_KNOTT_METHOD
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "ehk";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Extended Haley-Knott Method";
        }
    },
    
    /**
     * Marker regression where we drop any missing genotypes
     */
    MARKER_REGRESSION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "mr";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Marker Regression (Drop Missing Genotypes)";
        }
    },
    
    /**
     * Marker regression where we fill missing genotypes using imputation
     */
    MARKER_REGRESSION_IMPUTATION
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "mr-imp";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Marker Regression (Fill Using Single Imputation)";
        }
    },
    
    /**
     * Marker regression where we fill missing genotypes using the
     * viterbi algorithm
     */
    MARKER_REGRESSION_VITERBI
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue()
        {
            return "mr-argmax";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Marker Regression (Fill Using Viterbi Algorithm)";
        }
    };
    
    /**
     * Get the value string that should be used for this
     * method type
     * @return
     *          the value string
     */
    public abstract String getValue();
    
    /**
     * The parameter name to use (applies to all)
     */
    public static final String PARAMETER_NAME = "method";
}