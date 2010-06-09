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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.gui.ScanOneInterval.IntervalPoint;
import org.jax.qtl.scan.gui.ScanOneInterval.IntervalShape;
import org.jax.qtl.scan.gui.ScanOneInterval.IntervalType;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Class for building the R command for calculating
 * intervals on scanone results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneIntervalCommandBuilder
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneIntervalCommandBuilder.class.getName());
    
    private static final String BAYESIAN_METHOD_NAME = "bayesint";
    
    private static final String LOD_METHOD_NAME = "lodint";
    
    /**
     * The property name used when {@link #setIntervalType(IntervalType)}
     * is called
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public static final String INTERVAL_TYPE_PROPERTY_NAME = "intervalType";
    
    private volatile IntervalType intervalType = IntervalType.BAYESIAN_CREDIBLE;
    
    /**
     * The property name used when {@link #setScanOneResult(ScanOneResult)}
     * is called
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public static final String SCAN_ONE_RESULT_PROPERTY_NAME = "scanOneResult";
    
    private static final String SCAN_ONE_RESULT_PARAMETER_NAME = "results";
    
    private volatile ScanOneResult scanOneResult;
    
    /**
     * The property name used when {@link #setChromosomeNames(String[])} is
     * called
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public static final String CHROMOSOME_NAMES_PROPERTY_NAME =
        "chromosomeNames";
    
    private static final String CHROMOSOME_NAME_PARAMETER_NAME = "chr";
    
    private volatile String[] chromosomeNames;
    
    /**
     * The property name used when {@link #setProbabilityCoverage(double)} is
     * called
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public static final String PROBABILITY_COVERAGE_PROPERTY_NAME =
        "probabilityCoverage";
    
    private static final String PROBABILITY_COVERAGE_PARAMETER_NAME = "prob";
    
    private volatile double probabilityCoverage = 0.95;
    
    /**
     * The property name used when {@link #setLodDrop(double)} is called
     * @see #addPropertyChangeListener(PropertyChangeListener)
     */
    public static final String LOD_DROP_PROPERTY_NAME = "lodDrop";
    
    private static final String LOD_DROP_PARAMETER_NAME = "drop";
    
    private volatile double lodDrop = 1.5;
    
    private final PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);
    
    /**
     * The property name used when {@link #setLodColumnIndex(int)} is called
     */
    private static final String LOD_COLUMN_INDEX_PROPERTY_NAME = "lodColumnIndex";
    
    private static final String LOD_COLUMN_INDEX_PARAMETER_NAME = "lodcolumn";
    
    private volatile int lodColumnIndex;
    
    /**
     * The offset we need to use when indexing the result of an interval
     * command
     */
    private final static int INTERVAL_RESULT_LOD_COLUMN_INDEX_OFFSET = 3;
    
    /**
     * Adds a new listener to this command builder. The given listener will
     * be notified whenever one of the setters for this builder is called
     * @param listener
     *          the new listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Remove the given listener from this command builder
     * @param listener
     *          the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Setter for the zero-based LOD column index
     * @param lodColumnIndex
     *          the lodColumnIndex to set
     */
    public void setLodColumnIndex(int lodColumnIndex)
    {
        this.lodColumnIndex = lodColumnIndex;
        
        this.propertyChangeSupport.firePropertyChange(
                LOD_COLUMN_INDEX_PROPERTY_NAME,
                null,
                Integer.valueOf(lodColumnIndex));
    }
    
    /**
     * Getter for the zero-based LOD column index
     * @return
     *          the lodColumnIndex
     */
    public int getLodColumnIndex()
    {
        return this.lodColumnIndex;
    }
    
    /**
     * Getter for the interval type. This effects which R function is used.
     * @return
     *          the interval type
     */
    public IntervalType getIntervalType()
    {
        return this.intervalType;
    }
    
    /**
     * Setter for the interval type
     * @param intervalType
     *          the new value for the interval type
     */
    public void setIntervalType(IntervalType intervalType)
    {
        synchronized(this)
        {
            this.intervalType = intervalType;
        }
        
        this.propertyChangeSupport.firePropertyChange(
                INTERVAL_TYPE_PROPERTY_NAME,
                null,
                intervalType);
    }

    /**
     * Getter for the scan one result that the command builder is using
     * @return
     *          the scan one result that we're using
     */
    public ScanOneResult getScanOneResult()
    {
        return this.scanOneResult;
    }

    /**
     * Setter for the scan one result that this command builder should use
     * @param scanOneResult
     *          the new scan one result that we should use
     */
    public void setScanOneResult(ScanOneResult scanOneResult)
    {
        this.scanOneResult = scanOneResult;
        
        this.propertyChangeSupport.firePropertyChange(
                SCAN_ONE_RESULT_PROPERTY_NAME,
                null,
                scanOneResult);
    }

    /**
     * Getter for the chromosome names that we should calculate
     * a credible interval for
     * @return
     *          the chromosome names
     */
    public String[] getChromosomeNames()
    {
        return this.chromosomeNames;
    }

    /**
     * Setter for the chromosome names that this command builder will
     * calculate credible intervals for
     * @param chromosomeNames
     *          the chromosome names (zero based)
     */
    public void setChromosomeNames(String[] chromosomeNames)
    {
        this.chromosomeNames = chromosomeNames;
        
        this.propertyChangeSupport.firePropertyChange(
                CHROMOSOME_NAMES_PROPERTY_NAME,
                null,
                chromosomeNames);
    }

    /**
     * Getter for the probability coverage that we should use for
     * the credible intervals
     * @return
     *          the probability coverage to use (range is 0 to 1)
     */
    public double getProbabilityCoverage()
    {
        return this.probabilityCoverage;
    }

    /**
     * Setter for the probability coverage that we should use for
     * the credible intervals
     * @param probabilityCoverage
     *          the new probability coverage to use (range is 0 to 1)
     */
    public void setProbabilityCoverage(double probabilityCoverage)
    {
        this.probabilityCoverage = probabilityCoverage;
        
        this.propertyChangeSupport.firePropertyChange(
                PROBABILITY_COVERAGE_PROPERTY_NAME,
                null,
                Double.valueOf(probabilityCoverage));
    }
    
    /**
     * Getter for the lod drop parameter (only used when
     * the method is set to {@link IntervalType#LOD_SUPPORT})
     * @return
     *          the lod drop parameter value
     */
    public double getLodDrop()
    {
        return this.lodDrop;
    }
    
    /**
     * Setter for the LOD drop parameter
     * @param lodDrop the lodDrop to set
     */
    public void setLodDrop(double lodDrop)
    {
        this.lodDrop = lodDrop;
        
        this.propertyChangeSupport.firePropertyChange(
                LOD_DROP_PROPERTY_NAME,
                null,
                Double.valueOf(lodDrop));
    }
    
    /**
     * Get the scanone intervals
     * @param rInterface
     *          the R interface
     * @return
     *          the scanone intervals
     */
    public List<ScanOneInterval> getScanOneIntervals(RInterface rInterface) 
    {
        List<RCommand> commands = this.getCommands();
        if(commands == null)
        {
            return null;
        }
        else
        {
            List<ScanOneInterval> intervals = new ArrayList<ScanOneInterval>(
                    commands.size());
            for(RCommand command: commands)
            {
                // TODO this is pretty inefficient since we're invoking
                //      the command 3 times
                REXP chromosomeExpression = rInterface.evaluateCommand(
                        new SilentRCommand(command.getCommandText() + "$chr"));
                REXP positionCmExpression = rInterface.evaluateCommand(
                        new SilentRCommand(command.getCommandText() + "$pos"));
                REXP lodExpression = rInterface.evaluateCommand(
                        new SilentRCommand(
                                command.getCommandText() + "[ , " + (this.lodColumnIndex + INTERVAL_RESULT_LOD_COLUMN_INDEX_OFFSET) + "]"));
                
                // convert expressions
                String[] chromosomeNames = JRIUtilityFunctions.extractStringArrayFromFactor(
                        chromosomeExpression);
                double[] positionsInCm = positionCmExpression.asDoubleArray();
                double[] lods = lodExpression.asDoubleArray();
                
                if(chromosomeNames.length != 3 ||
                   positionsInCm.length != 3 ||
                   lods.length != 3)
                {
                    LOG.warning(
                            "expexted all interval arrays to be of length 3: " +
                            "chromosomeNames=" + chromosomeNames.length +
                            ", positionsInCm=" + positionsInCm.length +
                            ", lods=" + lods.length);
                    return null;
                }
                else
                {
                    IntervalShape intervalShape = new IntervalShape(
                            new IntervalPoint(
                                    positionsInCm[0],
                                    lods[0]),
                            new IntervalPoint(
                                    positionsInCm[1],
                                    lods[1]),
                            new IntervalPoint(
                                    positionsInCm[2],
                                    lods[2]));
                    double intervalConstraint =
                        this.intervalType == IntervalType.BAYESIAN_CREDIBLE ?
                                this.probabilityCoverage :
                                this.lodDrop;
                    ScanOneInterval currInterval = new ScanOneInterval(
                            this.intervalType,
                            intervalShape,
                            intervalConstraint,
                            chromosomeNames[0]);
                    intervals.add(currInterval);
                }
            }
            
            return intervals;
        }
    }
    
    /**
     * Get the list of commands that results from all of the properties
     * set on this builder. The commands will be returned in order
     * corresponding to the order of the chromosome names set in
     * {@link #setChromosomeNames(String[])}
     * @return
     *          the list of commands (or null if we can't create a valid
     *          command)
     */
    public synchronized List<RCommand> getCommands()
    {
        List<RCommandParameter> commandsParamsWithoutChromosome =
            this.getCommandParametersWithoutChromosomeName();
        List<RCommandParameter> chromosomeNameParameters =
            this.getChromosomeNameParameters();
        
        if(commandsParamsWithoutChromosome != null &&
           chromosomeNameParameters != null)
        {
            List<RCommand> rCommands = new ArrayList<RCommand>(
                    chromosomeNameParameters.size());
            for(RCommandParameter currChromoParam: chromosomeNameParameters)
            {
                List<RCommandParameter> currFullParameterList =
                    new ArrayList<RCommandParameter>(
                            commandsParamsWithoutChromosome);
                currFullParameterList.add(currChromoParam);
                
                String methodName =
                    this.intervalType == IntervalType.BAYESIAN_CREDIBLE ?
                            BAYESIAN_METHOD_NAME :
                            LOD_METHOD_NAME;
                RCommand currCommand = new RMethodInvocationCommand(
                        methodName,
                        currFullParameterList);
                rCommands.add(currCommand);
            }
            
            return rCommands;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get the command parameters without a chromosome name parameter (since we
     * can have many chromosome names)
     * @return
     *          the parameters minus the chromosome names parameter
     */
    private List<RCommandParameter> getCommandParametersWithoutChromosomeName()
    {
        List<RCommandParameter> commands = new ArrayList<RCommandParameter>();
        
        ScanOneResult scanOneResult = this.scanOneResult;
        if(scanOneResult != null)
        {
            commands.add(new RCommandParameter(
                    SCAN_ONE_RESULT_PARAMETER_NAME,
                    scanOneResult.getAccessorExpressionString()));
        }
        else
        {
            return null;
        }
        
        IntervalType intervalType = this.intervalType;
        if(intervalType == IntervalType.BAYESIAN_CREDIBLE)
        {
            double probabilityCoverage = this.probabilityCoverage;
            if(probabilityCoverage > 0.0 && probabilityCoverage < 1.0)
            {
                commands.add(new RCommandParameter(
                        PROBABILITY_COVERAGE_PARAMETER_NAME,
                        Double.toString(probabilityCoverage)));
            }
            else
            {
                return null;
            }
        }
        else
        {
            double lodDrop = this.lodDrop;
            if(lodDrop > 0.0)
            {
                commands.add(new RCommandParameter(
                        LOD_DROP_PARAMETER_NAME,
                        Double.toString(lodDrop)));
            }
            else
            {
                return null;
            }
        }
        
        commands.add(new RCommandParameter(
                LOD_COLUMN_INDEX_PARAMETER_NAME,
                Integer.toString(this.lodColumnIndex + 1)));
        
        return commands;
    }
    
    /**
     * Each one of these parameters should be a part of a separate command.
     * @return
     *          a list of chromosome name parameters
     */
    private List<RCommandParameter> getChromosomeNameParameters()
    {
        String[] chromosomeNames = this.chromosomeNames;
        if(chromosomeNames != null && chromosomeNames.length > 0)
        {
            List<RCommandParameter> chromosomeNameParameters =
                new ArrayList<RCommandParameter>(chromosomeNames.length);
            for(String currChromosomeName: chromosomeNames)
            {
                chromosomeNameParameters.add(new RCommandParameter(
                        CHROMOSOME_NAME_PARAMETER_NAME,
                        RUtilities.javaStringToRString(currChromosomeName)));
            }
            return chromosomeNameParameters;
        }
        else
        {
            return null;
        }
    }
}
