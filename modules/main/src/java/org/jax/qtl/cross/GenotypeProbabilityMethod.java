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

/**
 * An enum for the possible genotype probability methods
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public enum GenotypeProbabilityMethod
{
    /**
     * when "sim.geno" is used
     */
    SIMULATE_GENOTYPE
    {
        public static final String GENO_SUB_COMPONENT_NAME = "draws";
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getGenoSubComponentName()
        {
            return GENO_SUB_COMPONENT_NAME;
        }
    },
    
    /**
     * when "calc.genoprob" is used
     */
    CALCULATE_CONDITIONAL_PROBABILITIES
    {
        public static final String GENO_SUB_COMPONENT_NAME = "prob";
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getGenoSubComponentName()
        {
            return GENO_SUB_COMPONENT_NAME;
        }
    };
    
    /**
     * Getter for the genotype sub-component that should be created for
     * this genotype
     * @return
     *          the sub-component
     */
    public abstract String getGenoSubComponentName();
}
