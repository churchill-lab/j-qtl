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

import org.jax.qtl.cross.GeneticMarkerPair;

/**
 * Class for holding the scantwo summary data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanTwoSummary
{
    /**
     * From R/qtl:
     * c("best", "full", "add", "int")
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    public enum ModelToOptimize
    {
        /**
         * The best model
         */
        BEST
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Full and Additive Models (Best)";
            }
        },
        
        /**
         * the full model
         */
        FULL
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
         * the additive model
         */
        ADDITIVE
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
         * the interactive model
         */
        INTERACTIVE
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Interaction (Full Model - Additive Model)";
            }
        }
    }
    
    /**
     * Class for holding a single row of the scantwo summary data
     */
    public static class ScanTwoSummaryRow
    {
        private GeneticMarkerPair fullMarkerPair;
        
        private GeneticMarkerPair additiveMarkerPair;
        
        private double fullLodScore;
        
        private double fullPValue;
        
        private double fullVsOneLodScore;
        
        private double fullVsOnePValue;
        
        private double interactiveLodScore;
        
        private double interactivePValue;
        
        private double additiveLodScore;
        
        private double additivePValue;
        
        private double additiveVsOneLodScore;
        
        private double additiveVsOnePValue;
        
        /**
         * Getter for the marker pair.
         * @return
         *          the marker pair
         */
        public GeneticMarkerPair getMarkerPair()
        {
            return this.fullMarkerPair;
        }
        
        /**
         * Setter for the marker pair
         * @param markerPair
         *          the marker pair
         */
        public void setMarkerPair(GeneticMarkerPair markerPair)
        {
            this.fullMarkerPair = markerPair;
            this.additiveMarkerPair = markerPair;
        }

        /**
         * Getter for the full marker pair
         * @return
         *          the full marker pair
         */
        public GeneticMarkerPair getFullMarkerPair()
        {
            return this.fullMarkerPair;
        }

        /**
         * Setter for the full marker pair
         * @param fullMarkerPair
         *          the full marker pair
         */
        public void setFullMarkerPair(GeneticMarkerPair fullMarkerPair)
        {
            this.fullMarkerPair = fullMarkerPair;
        }

        /**
         * Getter for the additive marker pair
         * @return
         *          the additive marker pair
         */
        public GeneticMarkerPair getAdditiveMarkerPair()
        {
            return this.additiveMarkerPair;
        }

        /**
         * Setter for the additive marker pair
         * @param additiveMarkerPair
         *          the additive marker pair
         */
        public void setAdditiveMarkerPair(GeneticMarkerPair additiveMarkerPair)
        {
            this.additiveMarkerPair = additiveMarkerPair;
        }

        /**
         * getter for the full LOD score
         * @return
         *          the full LOD score
         */
        public double getFullLodScore()
        {
            return this.fullLodScore;
        }

        /**
         * Setter for the full LOD score
         * @param fullLodScore
         *          the full LOD score
         */
        public void setFullLodScore(double fullLodScore)
        {
            this.fullLodScore = fullLodScore;
        }

        /**
         * Getter for the full p-value
         * @return
         *          the full p-value
         */
        public double getFullPValue()
        {
            return this.fullPValue;
        }

        /**
         * Setter for the full p-value
         * @param fullPValue
         *          the full p-value
         */
        public void setFullPValue(double fullPValue)
        {
            this.fullPValue = fullPValue;
        }

        /**
         * Getter for the full vs. one LOD score
         * @return
         *          the full vs. one LOD score
         */
        public double getFullVsOneLodScore()
        {
            return this.fullVsOneLodScore;
        }

        /**
         * Setter for the full vs one LOD score
         * @param fullVsOneLodScore
         *          the full vs. one LOD score
         */
        public void setFullVsOneLodScore(double fullVsOneLodScore)
        {
            this.fullVsOneLodScore = fullVsOneLodScore;
        }

        /**
         * Getter for the full vs. one p-value
         * @return
         *          the full vs. one p-value
         */
        public double getFullVsOnePValue()
        {
            return this.fullVsOnePValue;
        }

        /**
         * Setter for the full vs. one p-value
         * @param fullVsOnePValue
         */
        public void setFullVsOnePValue(double fullVsOnePValue)
        {
            this.fullVsOnePValue = fullVsOnePValue;
        }

        /**
         * Getter for the interactive LOD score
         * @return
         *          the interactive LOD score
         */
        public double getInteractiveLodScore()
        {
            return this.interactiveLodScore;
        }

        /**
         * Setter for the interactive LOD score
         * @param interactiveLodScore
         *          the interactive LOD score
         */
        public void setInteractiveLodScore(double interactiveLodScore)
        {
            this.interactiveLodScore = interactiveLodScore;
        }

        /**
         * Getter for the interactive p-value
         * @return
         *          the interactive p-value
         */
        public double getInteractivePValue()
        {
            return this.interactivePValue;
        }

        /**
         * Setter for the interactive p-value
         * @param interactivePValue
         *          the interactive p-value
         */
        public void setInteractivePValue(double interactivePValue)
        {
            this.interactivePValue = interactivePValue;
        }

        /**
         * Getter for the additive LOD score
         * @return
         *          the additive LOD score
         */
        public double getAdditiveLodScore()
        {
            return this.additiveLodScore;
        }

        /**
         * Setter for the additive LOD score
         * @param additiveLodScore
         *          the additive LOD score
         */
        public void setAdditiveLodScore(double additiveLodScore)
        {
            this.additiveLodScore = additiveLodScore;
        }

        /**
         * Getter for the additive p-value
         * @return
         *          the additive p-value
         */
        public double getAdditivePValue()
        {
            return this.additivePValue;
        }

        /**
         * Setter for the additive p-value
         * @param additivePValue
         *          the additive p-value
         */
        public void setAdditivePValue(double additivePValue)
        {
            this.additivePValue = additivePValue;
        }

        /**
         * Getter for the additive vs one LOD score
         * @return
         *          the additive vs one LOD score
         */
        public double getAdditiveVsOneLodScore()
        {
            return this.additiveVsOneLodScore;
        }

        /**
         * Setter for the additive vs one LOD score
         * @param additiveVsOneLodScore
         */
        public void setAdditiveVsOneLodScore(double additiveVsOneLodScore)
        {
            this.additiveVsOneLodScore = additiveVsOneLodScore;
        }

        /**
         * Getter for the additive vs one p-value
         * @return
         *          the additive vs one p-value
         */
        public double getAdditiveVsOnePValue()
        {
            return this.additiveVsOnePValue;
        }

        /**
         * Setter for the additive vs one p-value
         * @param additiveVsOnePValue
         *          the additive vs one p-value
         */
        public void setAdditiveVsOnePValue(double additiveVsOnePValue)
        {
            this.additiveVsOnePValue = additiveVsOnePValue;
        }
    }
    
    private final ScanTwoSummaryRow[] scanTwoSummaryRows;
    
    private final ModelToOptimize modelToMaximize;
    
    /**
     * Constructor
     * @param modelToMaximize
     *          the model that this result maximized
     * @param scanTwoSummaryRows
     *          the summary rows that make up this summary
     */
    public ScanTwoSummary(
            ModelToOptimize modelToMaximize,
            ScanTwoSummaryRow[] scanTwoSummaryRows)
    {
        this.modelToMaximize = modelToMaximize;
        this.scanTwoSummaryRows = scanTwoSummaryRows;
    }
    
    /**
     * Getter for the scantwo summary rows
     * @return the scanTwoSummaryRows
     */
    public ScanTwoSummaryRow[] getScanTwoSummaryRows()
    {
        return this.scanTwoSummaryRows;
    }
    
    /**
     * Getter for the model that we're maximizing
     * @return the modelToMaximize
     */
    public ModelToOptimize getModelToMaximize()
    {
        return this.modelToMaximize;
    }
}
