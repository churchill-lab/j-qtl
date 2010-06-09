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

import java.util.ArrayList;
import java.util.List;

import org.jax.qtl.cross.Cross.CrossSubType;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * Class for creating a simulate cross command
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SimulateCrossCommandBuilder
{
    /**
     * The map function enum
     */
    public enum MapFunction
    {
        /**
         * haldane enum
         */
        HALDANE
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Haldane";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRString()
            {
                return "haldane";
            }
        },
        
        /**
         * kosambi enum
         */
        KOSAMBI
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Kosambi";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRString()
            {
                return "kosambi";
            }
        },
        
        /**
         * carter-falconer enum
         */
        CARTER_FALCONER
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Carter-Falconer";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRString()
            {
                return "c-f";
            }
        },
        
        /**
         * morgan enum
         */
        MORGAN
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Morgan";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public String getRString()
            {
                return "morgan";
            }
        };
        
        /**
         * Get the string that should be used in an R command
         * @return
         *          the r string
         */
        public abstract String getRString();
    }
    
    /**
     * Class representing a simulated QTL
     * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
     */
    public static class SimulatedQtl
    {
        private volatile int chromosomeNumber = 1;
        
        private volatile double positionInCentimorgans;
        
        private volatile double effectOne;
        
        private volatile double effectTwo;
        
        private volatile double effectThree;
        
        /**
         * Getter for the chromosome number
         * @return the chromosomeNumber
         */
        public int getChromosomeNumber()
        {
            return this.chromosomeNumber;
        }
        
        /**
         * Setter for the chromosome number
         * @param chromosomeNumber the chromosomeNumber to set
         */
        public void setChromosomeNumber(int chromosomeNumber)
        {
            this.chromosomeNumber = chromosomeNumber;
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
         * Setter for the position in cM
         * @param positionInCentimorgans the positionInCentimorgans to set
         */
        public void setPositionInCentimorgans(double positionInCentimorgans)
        {
            this.positionInCentimorgans = positionInCentimorgans;
        }
        
        /**
         * The 1st effect
         * @return the effectOne
         */
        public double getEffectOne()
        {
            return this.effectOne;
        }
        
        /**
         * Setter for the 1st effect
         * @param effectOne the effectOne to set
         */
        public void setEffectOne(double effectOne)
        {
            this.effectOne = effectOne;
        }
        
        /**
         * Getter for the 2nd effect
         * @return the effectTwo
         */
        public double getEffectTwo()
        {
            return this.effectTwo;
        }
        
        /**
         * Setter for the 2nd effect
         * @param effectTwo the effectTwo to set
         */
        public void setEffectTwo(double effectTwo)
        {
            this.effectTwo = effectTwo;
        }
        
        /**
         * Getter for the 3rd effect
         * @return the effectThree
         */
        public double getEffectThree()
        {
            return this.effectThree;
        }
        
        /**
         * Setter for the 3rd effect
         * @param effectThree the effectThree to set
         */
        public void setEffectThree(double effectThree)
        {
            this.effectThree = effectThree;
        }
    }
    
    private volatile String mapAccessorString;
    
    private volatile int numIndividuals = 100;
    
    private volatile CrossSubType crossType = CrossSubType.F2;
    
    private volatile MapFunction mapFunction = MapFunction.HALDANE;
    
    private volatile double genotypingErrorRate;
    
    private volatile double missingGenotypeRate;
    
    private volatile double partiallyInformativeRate;
    
    private volatile double probabilityOfNoInterference;
    
    private volatile double interferenceParameter;
    
    private volatile SimulatedQtl[] simulatedQtls = new SimulatedQtl[0];
    
    private volatile String crossName;
    
    /**
     * Getter for the map accessor string
     * @return the mapAccessorString
     */
    public String getMapAccessorString()
    {
        return this.mapAccessorString;
    }
    
    /**
     * Setter for the map accessor string
     * @param mapAccessorString the mapAccessorString to set
     */
    public void setMapAccessorString(String mapAccessorString)
    {
        this.mapAccessorString = mapAccessorString;
    }
    
    /**
     * Getter for the number of individuals
     * @return the numIndividuals
     */
    public int getNumIndividuals()
    {
        return this.numIndividuals;
    }
    
    /**
     * Setter for the number of individuals
     * @param numIndividuals the numIndividuals to set
     */
    public void setNumIndividuals(int numIndividuals)
    {
        this.numIndividuals = numIndividuals;
    }
    
    /**
     * Getter for the cross type
     * @return the crossType
     */
    public CrossSubType getCrossType()
    {
        return this.crossType;
    }
    
    /**
     * Setter for the cross type
     * @param crossType the crossType to set
     */
    public void setCrossType(CrossSubType crossType)
    {
        this.crossType = crossType;
    }
    
    /**
     * Getter for the map function
     * @return the mapFunction
     */
    public MapFunction getMapFunction()
    {
        return this.mapFunction;
    }
    
    /**
     * Setter for the map function
     * @param mapFunction the mapFunction to set
     */
    public void setMapFunction(MapFunction mapFunction)
    {
        this.mapFunction = mapFunction;
    }
    
    /**
     * Getter for the genotyping error rate
     * @return the genotypingErrorRate
     */
    public double getGenotypingErrorRate()
    {
        return this.genotypingErrorRate;
    }
    
    /**
     * Setter for the genotyping error rate
     * @param genotypingErrorRate the genotypingErrorRate to set
     */
    public void setGenotypingErrorRate(double genotypingErrorRate)
    {
        this.genotypingErrorRate = genotypingErrorRate;
    }
    
    /**
     * Getter for the missing genotype rate
     * @return the missingGenotypeRate
     */
    public double getMissingGenotypeRate()
    {
        return this.missingGenotypeRate;
    }
    
    /**
     * Setter for the missing genotype rate
     * @param missingGenotypeRate the missingGenotypeRate to set
     */
    public void setMissingGenotypeRate(double missingGenotypeRate)
    {
        this.missingGenotypeRate = missingGenotypeRate;
    }
    
    /**
     * Getter for the partially informative rate
     * @return the partiallyInformativeRate
     */
    public double getPartiallyInformativeRate()
    {
        return this.partiallyInformativeRate;
    }
    
    /**
     * Setter for the partially informative rate
     * @param partiallyInformativeRate the partiallyInformativeRate to set
     */
    public void setPartiallyInformativeRate(double partiallyInformativeRate)
    {
        this.partiallyInformativeRate = partiallyInformativeRate;
    }
    
    /**
     * Getter for the probability of no interference
     * @return the probabilityOfNoInterference
     */
    public double getProbabilityOfNoInterference()
    {
        return this.probabilityOfNoInterference;
    }
    
    /**
     * Setter for the probability of no interference
     * @param probabilityOfNoInterference the probabilityOfNoInterference to set
     */
    public void setProbabilityOfNoInterference(
            double probabilityOfNoInterference)
    {
        this.probabilityOfNoInterference = probabilityOfNoInterference;
    }
    
    /**
     * Getter for the interference parameter
     * @return the interferenceParameter
     */
    public double getInterferenceParameter()
    {
        return this.interferenceParameter;
    }
    
    /**
     * Setter for the interface parameter
     * @param interferenceParameter the interferenceParameter to set
     */
    public void setInterferenceParameter(double interferenceParameter)
    {
        this.interferenceParameter = interferenceParameter;
    }
    
    /**
     * Getter for the simulated qtls
     * @return the simulatedQtls
     */
    public SimulatedQtl[] getSimulatedQtls()
    {
        return this.simulatedQtls;
    }
    
    /**
     * Setter for the simulated qtls
     * @param simulatedQtls the simulatedQtls to set
     */
    public void setSimulatedQtls(SimulatedQtl[] simulatedQtls)
    {
        this.simulatedQtls = simulatedQtls;
    }
    
    /**
     * Getter for the cross name
     * @return the crossName
     */
    public String getCrossName()
    {
        return this.crossName;
    }
    
    /**
     * Setter for the cross name
     * @param crossName the crossName to set
     */
    public void setCrossName(String crossName)
    {
        this.crossName = crossName;
    }
    
    /**
     * Getter for the simulate cross command
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        List<RCommandParameter> commandParameters =
            this.getCommandParameters();
        
        RMethodInvocationCommand simCrossMethodCommand = new RMethodInvocationCommand(
                "sim.cross",
                commandParameters);
        
        String crossName = this.crossName;
        if(crossName == null || crossName.trim().length() == 0)
        {
            return simCrossMethodCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    crossName.trim(),
                    simCrossMethodCommand.getCommandText());
        }
    }

    /**
     * Getter for the command parameters
     * @return
     *          the command parameters
     */
    private List<RCommandParameter> getCommandParameters()
    {
        List<RCommandParameter> parameters = new ArrayList<RCommandParameter>();
        
        String mapAccessorString = this.mapAccessorString;
        if(mapAccessorString != null && mapAccessorString.trim().length() != 0)
        {
            parameters.add(new RCommandParameter(
                    mapAccessorString.trim()));
        }
        
        CrossSubType crossType = this.crossType;
        SimulatedQtl[] simulatedQtls = this.simulatedQtls;
        if(simulatedQtls != null && simulatedQtls.length > 0)
        {
            StringBuffer qtlsBuffer = new StringBuffer("rbind(");
            for(int qtlIndex = 0; qtlIndex < simulatedQtls.length; qtlIndex++)
            {
                if(qtlIndex >= 1)
                {
                    qtlsBuffer.append(", ");
                }
                
                qtlsBuffer.append("c(");
                qtlsBuffer.append(RUtilities.javaIntToRInt(
                        simulatedQtls[qtlIndex].getChromosomeNumber()));
                qtlsBuffer.append(", ");
                qtlsBuffer.append(RUtilities.javaDoubleToRDouble(
                        simulatedQtls[qtlIndex].getPositionInCentimorgans()));
                qtlsBuffer.append(", ");
                qtlsBuffer.append(RUtilities.javaDoubleToRDouble(
                        simulatedQtls[qtlIndex].getEffectOne()));
                if(crossType == CrossSubType.F2 || crossType == CrossSubType.FOUR_WAY)
                {
                    qtlsBuffer.append(", ");
                    qtlsBuffer.append(RUtilities.javaDoubleToRDouble(
                            simulatedQtls[qtlIndex].getEffectTwo()));
                    if(crossType == CrossSubType.FOUR_WAY)
                    {
                        qtlsBuffer.append(", ");
                        qtlsBuffer.append(RUtilities.javaDoubleToRDouble(
                                simulatedQtls[qtlIndex].getEffectThree()));
                    }
                }
                
                qtlsBuffer.append(")");
            }
            qtlsBuffer.append(")");
            
            parameters.add(new RCommandParameter(
                    "model",
                    qtlsBuffer.toString()));
        }
        
        parameters.add(new RCommandParameter(
                "n.ind",
                RUtilities.javaIntToRInt(this.numIndividuals)));
        
        parameters.add(new RCommandParameter(
                "type",
                RUtilities.javaStringToRString(crossType.getTypeString())));
        
        parameters.add(new RCommandParameter(
                "error.prob",
                RUtilities.javaDoubleToRDouble(this.genotypingErrorRate)));
        
        parameters.add(new RCommandParameter(
                "missing.prob",
                RUtilities.javaDoubleToRDouble(this.missingGenotypeRate)));
        
        parameters.add(new RCommandParameter(
                "partial.missing.prob",
                RUtilities.javaDoubleToRDouble(this.partiallyInformativeRate)));
        
        parameters.add(new RCommandParameter(
                "m",
                RUtilities.javaDoubleToRDouble(this.interferenceParameter)));
        
        parameters.add(new RCommandParameter(
                "p",
                RUtilities.javaDoubleToRDouble(this.probabilityOfNoInterference)));
        
        parameters.add(new RCommandParameter(
                "map.function",
                RUtilities.javaStringToRString(this.mapFunction.getRString())));
        
        return parameters;
    }
}
