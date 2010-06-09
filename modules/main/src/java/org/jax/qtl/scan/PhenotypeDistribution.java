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
 * An enumeration for the different kinds of phenotypes that our scan
 * methods know how to deal with.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum PhenotypeDistribution
{
    /**
     * Everyones favorite distribution
     */
    NORMAL
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getModelParameterValue()
        {
            return "normal";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Normal";
        }
    },
    
    /**
     * Binary distribution
     */
    BINARY
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getModelParameterValue()
        {
            return "binary";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Binary";
        }
    },
    
    /**
     * Two-part distribution with the unknown part spiking up
     */
    TWO_PART_SPIKES_UP
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getModelParameterValue()
        {
            return "2part";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Two Part (Spikes Up)";
        }
    },
    
    /**
     * Two part distribution with the unknown phenotype spiking down
     */
    TWO_PART_SPIKES_DOWN
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getModelParameterValue()
        {
            return "2part";
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Two Part (Spikes Down)";
        }
    },
    
    /**
     * Non-parametric distribution
     */
    OTHER
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getModelParameterValue()
        {
            return "np";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Other (Non-Parametric)";
        }
    };
    
    /**
     * Get the value string that should be used for this
     * distribution type
     * @return
     *          the value string
     */
    public abstract String getModelParameterValue();
    
    /**
     * The parameter name for the two part spike direction
     */
    public static final String SPIKE_DIRECTION_PARAMETER_NAME = "upper";
    
    /**
     * The model parameter name to use in the R command (applies to all)
     */
    public static final String MODEL_PARAMETER_NAME = "model";
    
    /**
     * The default distribution to use
     */
    public static final PhenotypeDistribution DEFAULT_DISTRIBUTION =
        NORMAL;
}