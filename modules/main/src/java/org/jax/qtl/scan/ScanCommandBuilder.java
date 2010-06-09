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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * This class is basically just a more safe and convenient way of creating
 * a scan command than creating the command string directly.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
// TODO add weights, incl.markers, clean.output, perm.strata,
//      ties.random, & start functionality
public class ScanCommandBuilder
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanCommandBuilder.class.getName());
    
    private volatile Cross cross;
    
    private static final String CROSS_PARAMETER_NAME = "cross";
    
    private volatile String[] chromosomeNames;

    private static final String CHROMOSOME_NAMES_PARAMETER_NAME = "chr";
    
    private volatile int[] phenotypeIndices;
    
    /**
     * The parameter name used for the phenotype indices
     */
    public static final String PHENOTYPE_INDICES_PARAMETER_NAME = "pheno.col";
    
    private volatile PhenotypeDistribution phenotypeDistribution =
        PhenotypeDistribution.NORMAL;
    
    private volatile ScanMethod scanMethod = ScanMethod.EM_ALGORITHM;
    
    private volatile ScanType scanType = ScanType.SCANONE;
    
    private volatile String[] additivePhenotypeCovariates;
    
    private volatile List<GeneticMarker> additiveGenotypeCovariates;
    
    private static final String ADDITIVE_COVARIATES_PARAMETER_NAME = "addcovar";
    
    private volatile String[] interactivePhenotypeCovariates;
    
    private volatile List<GeneticMarker> interactiveGenotypeCovariates;
    
    private static final String INTERACTIVE_COVARIATES_PARAMETER_NAME = "intcovar";
    
    private volatile boolean useMissingPhenotypes;
    
    private static final String USE_MISSING_PHENOTYPES_PARAMETER_NAME = "use";
    
    private static final String USE_MISSING_TRUE_PARAMETER_VALUE = "all.obs";
    
    private static final String USE_MISSING_FALSE_PARAMETER_VALUE = "complete.obs";
    
    /**
     * a string representation of the convergence tolerance value that is used
     * by default if no value is given
     */
    public static final String UNSPECIFIED_CONVERGENCE_TOLERANCE = "1e-4";
    
    private volatile Double convergenceTolerance;
    
    private static final String CONVERGENCE_TOLERANCE_PARAMETER_NAME = "tol";
    
    private volatile Integer maximumNumberOfIterations;
    
    private static final String MAXIMUM_NUMBER_OF_ITERATIONS_PARAMETER_NAME = "maxit";
    
    private volatile Integer numberOfPermutations;
    
    private static final String NUMBER_OF_PERMUTATIONS_PARAMETER_NAME = "n.perm";
    
    private volatile boolean seperatePermutationsForAutosome;
    
    private static final String SEPERATE_PERMUTATIONS_FOR_AUTOSOME_PARAMETER_NAME = "perm.Xsp";
    
    private volatile String scanResultName;

    private volatile boolean verbosePermutationsOutput;
    
    private static final String VERBOSE_PERMUTATIONS_OUTPUT_PERAMETER_NAME = "verbose";
    
    private volatile boolean useAllMarkers;
    
    private static final String USE_ALL_MARKERS_PARAMETER_NAME = "incl.markers";
    
    private volatile boolean cleanOutput;
    
    private static final String CLEAN_OUTPUT_PARAMETER_NAME = "clean.output";
    
    /**
     * This suffix that we tack on to the identifier when we create an
     * assignment command for {@link #getCommandWithPermutations()}
     */
    public static final String PERMUTATION_IDENTIFIER_SUFFIX = ".permutations";
    
    /**
     * Getter for the cross that this scan works on.
     * @return the cross
     */
    public Cross getCross()
    {
        return this.cross;
    }

    /**
     * Setter for the cross that this scan works on.
     * @param selectedCross the cross to set
     */
    public void setCross(Cross selectedCross)
    {
        this.cross = selectedCross;
    }

    /**
     * The names of the chromosomes that this scan with work on
     * @return the chromosome names
     */
    public String[] getChromosomeNames()
    {
        return this.chromosomeNames;
    }

    /**
     * Setter for the names of the chromosomes that this scan will work on
     * @param chromosomeNames the chromosome names to set
     */
    public void setChromosomeIndices(String[] chromosomeNames)
    {
        this.chromosomeNames = chromosomeNames;
    }

    /**
     * Getter for the phenotypes that this scan will work on
     * @return the phenotype indices
     */
    public int[] getPhenotypeIndices()
    {
        return this.phenotypeIndices;
    }

    /**
     * Setter for the selected phenotypes that this command will work on
     * @param phenotypeIndices
     *          the phenotype indices
     */
    public void setPhenotypeIndices(int[] phenotypeIndices)
    {
        this.phenotypeIndices = phenotypeIndices;
    }

    /**
     * Getter for the phenotype distribution to use for this scan
     * @return the phenotypeDistribution
     */
    public PhenotypeDistribution getPhenotypeDistribution()
    {
        return this.phenotypeDistribution;
    }

    /**
     * Setter for the phenotype distribution to use for this scan
     * @param phenotypeDistribution the phenotypeDistribution to set
     */
    public void setPhenotypeDistribution(
            PhenotypeDistribution phenotypeDistribution)
    {
        this.phenotypeDistribution = phenotypeDistribution;
    }

    /**
     * Getter for the scan method to use
     * @return the scanMethod
     */
    public ScanMethod getScanMethod()
    {
        return this.scanMethod;
    }

    /**
     * Setter for the scan method to use
     * @param scanMethod the scanMethod to set
     */
    public void setScanMethod(ScanMethod scanMethod)
    {
        this.scanMethod = scanMethod;
    }

    /**
     * Getter for the scan type
     * @return
     *      the scanType
     */
    public ScanType getScanType()
    {
        return this.scanType;
    }

    /**
     * Setter for the scan type
     * @param scanType
     *          the scanType to set
     */
    public void setScanType(ScanType scanType)
    {
        this.scanType = scanType;
    }

    /**
     * Getter for the additive covariate names
     * @return the additivePhenotypeCovariates
     */
    public String[] getAdditiveCovariates()
    {
        return this.additivePhenotypeCovariates;
    }

    /**
     * Setter for the additive covariate names
     * @param additivePhenotypeCovariates the additive covariates to set
     */
    public void setAdditivePhenotypeCovariates(String[] additivePhenotypeCovariates)
    {
        this.additivePhenotypeCovariates = additivePhenotypeCovariates;
    }

    /**
     * Getter for the interactive covariate names
     * @return the interactiveCovariates
     */
    public String[] getInteractivePhenotypeCovariates()
    {
        return this.interactivePhenotypeCovariates;
    }

    /**
     * Setter for the interactive covariate names
     * @param interactivePhenotypeCovariates the interactive covariates to set
     */
    public void setInteractivePhenotypeCovariates(String[] interactivePhenotypeCovariates)
    {
        this.interactivePhenotypeCovariates = interactivePhenotypeCovariates;
    }

    /**
     * If true use missing phenotypes (assuming we're scanning more than
     * one phenotype)
     * @return the useMissingPhenotypes
     */
    public boolean getUseMissingPhenotypes()
    {
        return this.useMissingPhenotypes;
    }

    /**
     * Setter for use missing phenotypes
     * @see #getUseMissingPhenotypes()
     * @param useMissingPhenotypes the useMissingPhenotypes to set
     */
    public void setUseMissingPhenotypes(boolean useMissingPhenotypes)
    {
        this.useMissingPhenotypes = useMissingPhenotypes;
    }

    /**
     * Getter for the maximum number of iterations to use. Null indicates
     * that the default value should be used.
     * @return the maximumNumberOfIterations
     */
    public Integer getMaximumNumberOfIterations()
    {
        return this.maximumNumberOfIterations;
    }

    /**
     * Setter for the maximum number of iterations to use.
     * @param maximumNumberOfIterations the maximumNumberOfIterations to set
     */
    public void setMaximumNumberOfIterations(Integer maximumNumberOfIterations)
    {
        this.maximumNumberOfIterations = maximumNumberOfIterations;
    }

    /**
     * Getter for the convergence tolerance to use. Null means that the default
     * should be used.
     * @return the convergenceTolerance
     */
    public Double getConvergenceTolerance()
    {
        return this.convergenceTolerance;
    }

    /**
     * Setter for the convergence tolerance to use
     * @param convergenceTolerance the convergenceTolerance to set
     */
    public void setConvergenceTolerance(Double convergenceTolerance)
    {
        this.convergenceTolerance = convergenceTolerance;
    }

    /**
     * Getter for the number of permutations to use. Null means to use the
     * default.
     * @return the numberOfPermutations
     * @see #getCommandWithPermutations()
     */
    public Integer getNumberOfPermutations()
    {
        return this.numberOfPermutations;
    }

    /**
     * Setter for the number of permutations to use.
     * @param numberOfPermutations the numberOfPermutations to set
     */
    public void setNumberOfPermutations(Integer numberOfPermutations)
    {
        this.numberOfPermutations = numberOfPermutations;
    }

    /**
     * Getter that determines if we use seperate permutations for the
     * autosome
     * @return the seperatePermutationsForAutosome
     */
    public boolean getSeperatePermutationsForAutosome()
    {
        return this.seperatePermutationsForAutosome;
    }

    /**
     * Setter for determining whether or not we use seperate permutations
     * for the autosome
     * @param seperatePermutationsForAutosome the seperatePermutationsForAutosome to set
     */
    public void setSeperatePermutationsForAutosome(
            boolean seperatePermutationsForAutosome)
    {
        this.seperatePermutationsForAutosome = seperatePermutationsForAutosome;
    }
    
    /**
     * Getter for the name that the scan result should be given
     * @return the scanResultName
     */
    public String getScanResultName()
    {
        String scanResultName = this.scanResultName;
        if(scanResultName == null)
        {
            return null;
        }
        else
        {
            return scanResultName.trim();
        }
    }

    /**
     * Setter for the name that the scan result should be given
     * @param scanResultName the scanResultName to set
     */
    public void setScanResultName(String scanResultName)
    {
        this.scanResultName = scanResultName;
    }

    /**
     * Create a parameter list that includes all of the parameters from
     * {@link #createParameterListWithoutPermutations()} plus any permutation
     * specific parameters that we need.
     * @return
     *          the parameter list or null if there are no permutation specific
     *          parameters
     */
    private List<RCommandParameter> createParameterListWithPermutations()
    {
        // take care of "n.perm" parameter
        ScanType scanType = this.getScanType();
        Integer numberOfPermutations = this.numberOfPermutations;
        if(numberOfPermutations == null || numberOfPermutations.intValue() == 0)
        {
            return null;
        }
        else
        {
            List<RCommandParameter> commandParameters =
                this.createParameterListWithoutPermutations();
            commandParameters.add(new RCommandParameter(
                    ScanCommandBuilder.NUMBER_OF_PERMUTATIONS_PARAMETER_NAME,
                    numberOfPermutations.toString()));
            
            // take care of "perm.Xsp" & "verbose" parameter
            if(numberOfPermutations.intValue() > 0)
            {
                if(scanType == ScanType.SCANONE)
                {
                    // seperate permutations command is only valid
                    // for scan one
                    commandParameters.add(new RCommandParameter(
                            ScanCommandBuilder.SEPERATE_PERMUTATIONS_FOR_AUTOSOME_PARAMETER_NAME,
                            RUtilities.javaBooleanToRBoolean(
                                    this.seperatePermutationsForAutosome)));
                }
                
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.VERBOSE_PERMUTATIONS_OUTPUT_PERAMETER_NAME,
                        RUtilities.javaBooleanToRBoolean(
                                this.verbosePermutationsOutput)));
            }
            
            return commandParameters;
        }
    }
    
    /**
     * Create a parameter list for the current settings
     * @return
     *          the parameter list
     */
    private List<RCommandParameter> createParameterListWithoutPermutations()
    {
        List<RCommandParameter> commandParameters =
            new ArrayList<RCommandParameter>();
        
        // throughout this method, we're grabbing local copies of "this"
        // references before using them. we do this for thread safety
        
        // create the cross parameter
        Cross cross = this.cross;
        if(cross != null)
        {
            commandParameters.add(new RCommandParameter(
                    CROSS_PARAMETER_NAME,
                    cross.getAccessorExpressionString()));
            
            // take care of the additive covariates
            String[] additivePhenotypeCovariates = this.additivePhenotypeCovariates;
            List<GeneticMarker> additiveGenotypeCovariates = this.additiveGenotypeCovariates;
            if(additivePhenotypeCovariates != null && additivePhenotypeCovariates.length > 0 ||
               additiveGenotypeCovariates != null && !additiveGenotypeCovariates.isEmpty())
            {
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.ADDITIVE_COVARIATES_PARAMETER_NAME,
                        this.covariatesToExpression(
                                additivePhenotypeCovariates,
                                additiveGenotypeCovariates,
                                cross)));
            }
            
            // take care of the interactive covariates
            String[] interactivePhenotypeCovariates = this.interactivePhenotypeCovariates;
            List<GeneticMarker> interactiveGenotypeCovariates = this.interactiveGenotypeCovariates;
            if(interactivePhenotypeCovariates != null && interactivePhenotypeCovariates.length > 0 ||
               interactiveGenotypeCovariates != null && !interactiveGenotypeCovariates.isEmpty())
            {
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.INTERACTIVE_COVARIATES_PARAMETER_NAME,
                        this.covariatesToExpression(
                                interactivePhenotypeCovariates,
                                interactiveGenotypeCovariates,
                                cross)));
            }
        }
        
        // create the chromosome indices parameter
        String[] chromosomeNames = this.chromosomeNames;
        if(chromosomeNames != null && chromosomeNames.length > 0)
        {
            commandParameters.add(new RCommandParameter(
                    CHROMOSOME_NAMES_PARAMETER_NAME,
                    RUtilities.stringArrayToRVector(chromosomeNames)));
        }
        
        // create the phenotype indices parameter
        int[] phenotypeIndices = this.phenotypeIndices;
        if(phenotypeIndices != null && phenotypeIndices.length > 0)
        {
            // R has 1 based indices
            int[] onesBasedPhenotypeIndices = new int[phenotypeIndices.length];
            for(int i = 0; i < phenotypeIndices.length; i++)
            {
                onesBasedPhenotypeIndices[i] = phenotypeIndices[i] + 1;
            }
            commandParameters.add(new RCommandParameter(
                    PHENOTYPE_INDICES_PARAMETER_NAME,
                    RUtilities.intArrayToRVector(onesBasedPhenotypeIndices)));
        }
        
        // create the model parameter
        PhenotypeDistribution phenotypeDistribution = this.phenotypeDistribution;
        if(phenotypeDistribution != null)
        {
            commandParameters.add(new RCommandParameter(
                    PhenotypeDistribution.MODEL_PARAMETER_NAME,
                    RUtilities.javaStringToRString(
                            phenotypeDistribution.getModelParameterValue())));
            
            // take care of the "upper" parameter
            if(phenotypeDistribution == PhenotypeDistribution.TWO_PART_SPIKES_UP)
            {
                commandParameters.add(new RCommandParameter(
                        PhenotypeDistribution.SPIKE_DIRECTION_PARAMETER_NAME,
                        RUtilities.javaBooleanToRBoolean(true)));
            }
            else if(phenotypeDistribution == PhenotypeDistribution.TWO_PART_SPIKES_DOWN)
            {
                commandParameters.add(new RCommandParameter(
                        PhenotypeDistribution.SPIKE_DIRECTION_PARAMETER_NAME,
                        RUtilities.javaBooleanToRBoolean(false)));
            }
        }
        
        // create the method parameter
        ScanMethod scanMethod = this.scanMethod;
        if(scanMethod != null)
        {
            commandParameters.add(new RCommandParameter(
                    ScanMethod.PARAMETER_NAME,
                    RUtilities.javaStringToRString(
                            scanMethod.getValue())));
            
            // take care of "maxit" parameter
            Integer maximumNumberOfIterations = this.maximumNumberOfIterations;
            if(maximumNumberOfIterations != null &&
                    (scanMethod == ScanMethod.EM_ALGORITHM ||
                            scanMethod == ScanMethod.EXTENDED_HALEY_KNOTT_METHOD))
            {
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.MAXIMUM_NUMBER_OF_ITERATIONS_PARAMETER_NAME,
                        maximumNumberOfIterations.toString()));
            }
            
            // take care of "tol" parameter
            Double convergenceTolerance = this.convergenceTolerance;
            if(convergenceTolerance != null &&
                    (scanMethod == ScanMethod.EM_ALGORITHM ||
                            scanMethod == ScanMethod.EXTENDED_HALEY_KNOTT_METHOD))
            {
                // TODO is this always going to be OK (Double.toString())?
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.CONVERGENCE_TOLERANCE_PARAMETER_NAME,
                        convergenceTolerance.toString()));
            }
        }
        
        // take care of the "use" parameters
        if(phenotypeIndices != null && phenotypeIndices.length > 1)
        {
            if(this.useMissingPhenotypes)
            {
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.USE_MISSING_PHENOTYPES_PARAMETER_NAME,
                        RUtilities.javaStringToRString(
                                ScanCommandBuilder.USE_MISSING_TRUE_PARAMETER_VALUE)));
            }
            else
            {
                commandParameters.add(new RCommandParameter(
                        ScanCommandBuilder.USE_MISSING_PHENOTYPES_PARAMETER_NAME,
                        RUtilities.javaStringToRString(
                                ScanCommandBuilder.USE_MISSING_FALSE_PARAMETER_VALUE)));
            }
        }
        
        ScanType scanType = this.getScanType();
        if(scanType == ScanType.SCANTWO)
        {
            // take care of some scantwo only stuff
            commandParameters.add(new RCommandParameter(
                    ScanCommandBuilder.USE_ALL_MARKERS_PARAMETER_NAME,
                    RUtilities.javaBooleanToRBoolean(this.useAllMarkers)));
            
            commandParameters.add(new RCommandParameter(
                    ScanCommandBuilder.CLEAN_OUTPUT_PARAMETER_NAME,
                    RUtilities.javaBooleanToRBoolean(this.cleanOutput)));
        }
        
        return commandParameters;
    }
    
    /**
     * Convert the covariates to an expression string
     * @param phenotypeCovariates
     *          the phenotype covariates
     * @param genotypeCovariates
     *          the genotype covariates
     * @param cross
     *          the cross object
     * @return
     *          the R expression
     */
    private String covariatesToExpression(
            String[] phenotypeCovariates,
            List<GeneticMarker> genotypeCovariates,
            Cross cross)
    {
        // In R this might look like:
        // cbind(fake.f2$pheno[, "sex"], fake.f2$pheno[, "pgm"], fake.f2$geno$"X"$data[,3])
        StringBuffer expressionBuffer = new StringBuffer("cbind(");
        
        if(phenotypeCovariates != null && phenotypeCovariates.length > 0)
        {
            for(int i = 0; i < phenotypeCovariates.length; i++)
            {
                if(i >=1)
                {
                    expressionBuffer.append(",");
                }
                
                expressionBuffer.append(cross.getAccessorExpressionString());
                expressionBuffer.append("$pheno[, ");
                expressionBuffer.append(
                        RUtilities.javaStringToRString(phenotypeCovariates[i]));
                expressionBuffer.append("]");
            }
        }
        
        {
            StringBuffer genotypeExpressionBuffer = new StringBuffer();
            boolean anyGenotypeErrors = false;
            
            if(phenotypeCovariates != null && phenotypeCovariates.length > 0 &&
               genotypeCovariates != null && !genotypeCovariates.isEmpty())
            {
                genotypeExpressionBuffer.append(",");
            }
            
            if(genotypeCovariates != null && !genotypeCovariates.isEmpty())
            {
                List<CrossChromosome> genotypeData = cross.getGenotypeData();
                int numGenoCovars = genotypeCovariates.size();
                for(int currGenoCovarIndex = 0;
                    currGenoCovarIndex < numGenoCovars;
                    currGenoCovarIndex++)
                {
                    if(currGenoCovarIndex >=1)
                    {
                        genotypeExpressionBuffer.append(",");
                    }
                    
                    GeneticMarker currMarker = genotypeCovariates.get(currGenoCovarIndex);
                    int matchingChromoIndex = cross.getIndexOfChromosomeNamed(
                            currMarker.getChromosomeName());
                    
                    if(matchingChromoIndex >= 0)
                    {
                        CrossChromosome matchingChromosome =
                            genotypeData.get(matchingChromoIndex);
                        
                        int matchingMarkerIndex = -1;
                        List<GeneticMarker> geneticMarkers =
                            matchingChromosome.getAnyGeneticMap().getMarkerPositions();
                        int numMarkersInMap = geneticMarkers.size();
                        for(int j = 0; j < numMarkersInMap; j++)
                        {
                            GeneticMarker otherCurrMarker = geneticMarkers.get(j);
                            if(otherCurrMarker != null &&
                               otherCurrMarker.getMarkerName().equals(currMarker.getMarkerName()))
                            {
                                matchingMarkerIndex = j;
                                break;
                            }
                        }
                        
                        if(matchingMarkerIndex >= 0)
                        {
                            String markerDataIndexExpression = RUtilities.columnIndexExpression(
                                    matchingChromosome.getMarkerDataRObject().getAccessorExpressionString(),
                                    matchingMarkerIndex);
                            genotypeExpressionBuffer.append(markerDataIndexExpression);
                        }
                        else
                        {
                            LOG.severe(
                                    "Failed to find marker for: " + currMarker);
                            anyGenotypeErrors = true;
                            break;
                        }
                    }
                    else
                    {
                        LOG.severe(
                                "Failed to find chromosome for: " + currMarker);
                        anyGenotypeErrors = true;
                        break;
                    }
                }
            }
            
            if(!anyGenotypeErrors)
            {
                expressionBuffer.append(genotypeExpressionBuffer);
            }
        }
        
        expressionBuffer.append(")");
        
        return expressionBuffer.toString();
    }
    
    /**
     * Get the command for scanning, ignoring any permutation settings
     * @return
     *          the command
     */
    public RCommand getCommandWithoutPermutations()
    {
        List<RCommandParameter> parameters = this.createParameterListWithoutPermutations();
        String scanResultName = this.scanResultName;
        
        RMethodInvocationCommand methodInvocation =
            new RMethodInvocationCommand(
                    this.getScanType().getRMethodName(),
                    parameters);
        if(scanResultName == null || scanResultName.trim().length() == 0)
        {
            return methodInvocation;
        }
        else
        {
            RAssignmentCommand assignmentCommand =
                new RAssignmentCommand(
                        scanResultName.trim(),
                        methodInvocation.getCommandText());
            return assignmentCommand;
        }
    }
    
    /**
     * Get the command with permutations
     * @return
     *          the command with permutations, or null if the permutation
     *          specific parameters are not set
     * @see #getNumberOfPermutations()
     */
    public RCommand getCommandWithPermutations()
    {
        List<RCommandParameter> parameters = this.createParameterListWithPermutations();
        String scanResultName = this.getScanResultName();
        
        if(parameters == null)
        {
            return null;
        }
        else
        {
            RMethodInvocationCommand methodInvocation =
                new RMethodInvocationCommand(
                        this.getScanType().getRMethodName(),
                        parameters);
            if(scanResultName == null || scanResultName.length() == 0)
            {
                return methodInvocation;
            }
            else
            {
                RAssignmentCommand assignmentCommand =
                    new RAssignmentCommand(
                            scanResultName + PERMUTATION_IDENTIFIER_SUFFIX,
                            methodInvocation.getCommandText());
                return assignmentCommand;
            }
        }
    }
    
    /**
     * Get the command that provides an accessor to the phenotype attribute.
     * @return
     *          the phenotype accessor command
     */
    public RCommand getPhenotypeAttributeCommand()
    {
        String scanResultName = this.getScanResultName();
        if(scanResultName != null)
        {
            List<RCommandParameter> parameterList =
                new ArrayList<RCommandParameter>();
            parameterList.add(new RCommandParameter(this.getScanResultName()));
            parameterList.add(new RCommandParameter(
                    RUtilities.javaStringToRString(
                            PHENOTYPE_INDICES_PARAMETER_NAME)));
            RMethodInvocationCommand attributeCommand =
                new RMethodInvocationCommand(
                        "attr",
                        parameterList);
            
            int[] phenotypeIndices = this.phenotypeIndices;
            if(phenotypeIndices != null && phenotypeIndices.length > 0)
            {
                // R has 1 based indices
                int[] onesBasedPhenotypeIndices = new int[phenotypeIndices.length];
                for(int i = 0; i < phenotypeIndices.length; i++)
                {
                    onesBasedPhenotypeIndices[i] = phenotypeIndices[i] + 1;
                }
                String phenoIndices =
                    RUtilities.intArrayToRVector(onesBasedPhenotypeIndices);
                RAssignmentCommand attibuteAssignmentCommand =
                    new RAssignmentCommand(
                            attributeCommand.getCommandText(),
                            phenoIndices);
                return attibuteAssignmentCommand;
            }
        }
        
        // return null if we can't create a good phenotype attribute command
        return null;
    }
    
    /**
     * See if this command represents a valid assignment.
     * @return
     *          null if it is valid, or the error message if it isn't
     */
    public String getInvalidAssignmentCommandMessage()
    {
        String invalidCommandMessage = this.getInvalidCommandMessage();
        if(invalidCommandMessage != null)
        {
            return invalidCommandMessage;
        }
        
        String scanResultName = this.scanResultName;
        if(scanResultName == null || scanResultName.trim().length() == 0)
        {
            return "Scan result name required";
        }
        
        scanResultName = scanResultName.trim();
        
        // we crossed all of the hurdles OK
        return null;
    }
    
    /**
     * See if this command is valid (assignment or not)
     * @return
     *          true if the command is valid
     */
    // TODO messages should come from a properties file
    public String getInvalidCommandMessage()
    {
        Cross cross = this.cross;
        if(cross == null)
        {
            return "Command requires a cross to scan";
        }
        
        String crossName = cross.getAccessorExpressionString();
        if(crossName == null || crossName.length() == 0)
        {
            return "Could not find a name for the selected cross";
        }
        
        PhenotypeDistribution phenotypeDistribution = this.phenotypeDistribution;
        if(phenotypeDistribution == null)
        {
            return "Command requires a phenotype distribution";
        }
        else
        {
            if(phenotypeDistribution != PhenotypeDistribution.OTHER && this.scanMethod == null)
            {
                return "Command requires a scan method";
            }
        }
        
        String[] chromosomeNames = this.chromosomeNames;
        if(chromosomeNames == null || chromosomeNames.length == 0)
        {
            return "Command requires that at least one chromosome is selected";
        }
        
        int[] phenotypeIndices = this.phenotypeIndices;
        if(phenotypeIndices == null || phenotypeIndices.length == 0)
        {
            return "Command requires that at least one phenotype is selected";
        }
        
        // we passed all of the hurdles OK
        return null;
    }

    /**
     * Setter that determines if the permutations output is verbose or quiet
     * @param verbosePermutationsOutput
     *          the value to set
     */
    public void setVerbosePermutationsOutput(boolean verbosePermutationsOutput)
    {
        this.verbosePermutationsOutput = verbosePermutationsOutput;
    }
    
    /**
     * Getter that determines if our permutations output is verbose or not.
     * @return
     *          true if it is verbose
     */
    public boolean getVerbosePermutationsOutput()
    {
        return this.verbosePermutationsOutput;
    }

    /**
     * @return the useAllMarkers
     */
    public boolean isUseAllMarkers()
    {
        return this.useAllMarkers;
    }

    /**
     * @param useAllMarkers the useAllMarkers to set
     */
    public void setUseAllMarkers(boolean useAllMarkers)
    {
        this.useAllMarkers = useAllMarkers;
    }

    /**
     * @return the cleanOutput
     */
    public boolean isCleanOutput()
    {
        return this.cleanOutput;
    }

    /**
     * @param cleanOutput the cleanOutput to set
     */
    public void setCleanOutput(boolean cleanOutput)
    {
        this.cleanOutput = cleanOutput;
    }

    /**
     * Setter for the additive genotype covariates
     * @param additiveGenotypeCovariates
     *          the additive genotype covariates
     */
    public void setAdditiveGenotypeCovariates(
            List<GeneticMarker> additiveGenotypeCovariates)
    {
        this.additiveGenotypeCovariates = additiveGenotypeCovariates;
    }

    /**
     * Setter for the interactive genotype covariates
     * @param interactiveGenotypeCovariates
     *          the covariates
     */
    public void setInteractiveGenotypeCovariates(
            List<GeneticMarker> interactiveGenotypeCovariates)
    {
        this.interactiveGenotypeCovariates = interactiveGenotypeCovariates;
    }
}
