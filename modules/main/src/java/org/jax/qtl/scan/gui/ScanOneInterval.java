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

package org.jax.qtl.scan.gui;

/**
 * A class for holding information about a scanone confidence interval
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneInterval
{
    /**
     * Enumeration for specifying whether the interval 
     */
    public enum IntervalType
    {
        /**
         * For bayesian credible intervals
         */
        BAYESIAN_CREDIBLE,
        
        /**
         * For LOD support intervals (with LOD drop specified)
         */
        LOD_SUPPORT
    }
    
    /**
     * Class holding info for a single interval point
     */
    public static class IntervalPoint
    {
        private final double positionInCentimorgans;
        
        private final double lodScore;

        /**
         * Constructor
         * @param positionInCentimorgans
         *          the position in cM
         * @param lodScore
         *          the LOD score
         */
        public IntervalPoint(double positionInCentimorgans, double lodScore)
        {
            this.positionInCentimorgans = positionInCentimorgans;
            this.lodScore = lodScore;
        }
        
        /**
         * Getter for the position in cM
         * @return the positionInCentimorgans
         */
        public double getPositionInCentimorgans()
        {
            return this.positionInCentimorgans;
        }
        
        /**
         * Getter for the LOD score
         * @return the lodScore
         */
        public double getLodScore()
        {
            return this.lodScore;
        }
    }
    
    /**
     * This class holds info about the left flank, the peak and the right
     * point.
     */
    public static class IntervalShape
    {
        private final IntervalPoint leftFlankPoint;
        
        private final IntervalPoint peakPoint;
        
        private final IntervalPoint rightFlankPoint;

        /**
         * Constructor
         * @param leftFlankPoint
         *          the left flank
         * @param peakPoint
         *          the peak
         * @param rightFlankPoint
         *          the right point
         */
        public IntervalShape(
                IntervalPoint leftFlankPoint,
                IntervalPoint peakPoint,
                IntervalPoint rightFlankPoint)
        {
            super();
            this.leftFlankPoint = leftFlankPoint;
            this.peakPoint = peakPoint;
            this.rightFlankPoint = rightFlankPoint;
        }

        /**
         * Getter for the left flank
         * @return
         *          the point for the left flank
         */
        public IntervalPoint getLeftFlankPoint()
        {
            return this.leftFlankPoint;
        }

        /**
         * Getter for the peak
         * @return
         *          the peak
         */
        public IntervalPoint getPeakPoint()
        {
            return this.peakPoint;
        }

        /**
         * Getter for the right flank
         * @return
         *          the right flank point
         */
        public IntervalPoint getRightFlankPoint()
        {
            return this.rightFlankPoint;
        }
    }
    
    private final IntervalType intervalType;
    
    private final IntervalShape intervalShape;
    
    private final double intervalConstraint;
    
    private final String chromosomeName;

    /**
     * Construct a new scanone confidence interval
     * @param intervalType
     *          the interval type
     * @param intervalShape
     *          the interval shape
     * @param intervalConstraint
     *          the interval constraint
     * @param chromosomeName
     *          the chromosome name
     */
    public ScanOneInterval(
            IntervalType intervalType,
            IntervalShape intervalShape,
            double intervalConstraint,
            String chromosomeName)
    {
        this.intervalType = intervalType;
        this.intervalShape = intervalShape;
        this.intervalConstraint = intervalConstraint;
        this.chromosomeName = chromosomeName;
    }

    /**
     * Getter for the interval type
     * @return
     *          the interval type
     */
    public IntervalType getIntervalType()
    {
        return this.intervalType;
    }

    /**
     * getter for the interval shape
     * @return
     *          the interval shape
     */
    public IntervalShape getIntervalShape()
    {
        return this.intervalShape;
    }

    /**
     * getter for the interval constraint
     * @return
     *          the interval constraint
     */
    public double getIntervalConstraint()
    {
        return this.intervalConstraint;
    }

    /**
     * getter for the chromosome name
     * @return
     *          the chromosome name
     */
    public String getChromosomeName()
    {
        return this.chromosomeName;
    }
}
