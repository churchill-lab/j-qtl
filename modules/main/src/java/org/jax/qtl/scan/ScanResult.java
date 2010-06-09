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

import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Base class for scan one and two
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class ScanResult extends RObject
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanResult.class.getName());
    
    /**
     * the parent cross for this scan result
     */
    private final Cross parentCross;
    
    private static final String LOD_P_PREFIX = "lod.p.";
    
    private static final String LOD_MU_PREFIX = "lod.mu.";
    
    private static final String LOD_P_MU_PREFIX = "lod.p.mu.";

    /**
     * Constructor
     * @param rInterface
     *          the r interface
     * @param accessorExpressionString
     *          the accessor expression string
     * @param parentCross
     *          the parent cross
     */
    public ScanResult(
            RInterface rInterface,
            String accessorExpressionString,
            Cross parentCross)
    {
        super(rInterface, accessorExpressionString);
        this.parentCross = parentCross;
    }
    
    /**
     * Getter for this scan's parent cross
     * @return the parentCross
     */
    public Cross getParentCross()
    {
        return this.parentCross;
    }
    
    /**
     * Getter for the scanned phenotype indices
     * @return
     *          the scanned (zero-based) phenotype indices
     */
    protected int[] getScannedPhenotypeIndices()
    {
        Cross parentCross = this.parentCross;
        if(parentCross != null)
        {
            RCommandParameter[] attrParameters = new RCommandParameter[] {
                    new RCommandParameter(this.getAccessorExpressionString()),
                    new RCommandParameter(RUtilities.javaStringToRString(
                            ScanCommandBuilder.PHENOTYPE_INDICES_PARAMETER_NAME))};
            RMethodInvocationCommand phenoAttributeCommand =
                new RMethodInvocationCommand(
                        "attr",
                        attrParameters);
            REXP phenoRExpression =
                this.getRInterface().evaluateCommand(new SilentRCommand(
                        phenoAttributeCommand));
            if(phenoRExpression.getType() == REXP.XT_NULL)
            {
                LOG.warning(
                        "cant get scanned phenotype since " +
                        phenoAttributeCommand.getCommandText() +
                        " is null");
                return new int[0];
            }
            else
            {
                Integer[] phenotypeIndexObjects =
                    JRIUtilityFunctions.extractIntegerValues(phenoRExpression);
                int[] phenotypeIndices = new int[phenotypeIndexObjects.length];
                for(int i = 0; i < phenotypeIndices.length; i++)
                {
                    phenotypeIndices[i] = phenotypeIndexObjects[i] - 1;
                }
                
                return phenotypeIndices;
            }
        }
        else
        {
            LOG.warning("cant get scanned phenotype. parent cross is null");
            return new int[0];
        }
    }

    /**
     * Get the index of the scanned phenotype
     * @return
     *          the index of the scanned phenotype
     */
    private int getScannedPhenotypeIndex()
    {
        int[] phenotypeIndices = this.getScannedPhenotypeIndices();
        if(phenotypeIndices.length == 1)
        {
            return phenotypeIndices[0];
        }
        else
        {
            LOG.warning(
                    "cant return a single phenotype index since " +
                    phenotypeIndices.length + " were scanned");
            return -1;
        }
    }
    
    /**
     * Get the scanned phenotype name
     * @return
     *          the name
     */
    protected String getScannedPhenotypeName()
    {
        int phenotypeIndex = this.getScannedPhenotypeIndex();
        if(phenotypeIndex >= 0)
        {
            return this.parentCross.getPhenotypeData().getDataNames()[phenotypeIndex];
        }
        else
        {
            LOG.warning("bad phenotypeIndex value: " + phenotypeIndex);
            return null;
        }
    }
    
    /**
     * Get the scanned phenotype name
     * @return
     *          the name
     */
    public String[] getScannedPhenotypeNames()
    {
        int[] scannedPhenotypeIndices = this.getScannedPhenotypeIndices();
        String[] scannedPhenotypeNames = new String[scannedPhenotypeIndices.length];
        String[] allPhenotypeNames = this.parentCross.getPhenotypeData().getDataNames();
        for(int i = 0; i < scannedPhenotypeIndices.length; i++)
        {
            if(scannedPhenotypeIndices[i] < allPhenotypeNames.length)
            {
                scannedPhenotypeNames[i] = allPhenotypeNames[scannedPhenotypeIndices[i]];
            }
            else
            {
                LOG.warning(
                        "phenotype index is out of bounds... returning an " +
                        "empty array");
                return new String[0];
            }
        }
        
        return scannedPhenotypeNames;
    }
    
    /**
     * Use a very permissive search to try to find the phenotype that
     * matches up with a lod scan column
     * @param scanColumnName
     *          the LOD column from the scan
     * @return
     *          the matching phenotype name or null if we can't find a match
     */
    public String findScannedPhenotypeNameForScanColumn(String scanColumnName)
    {
        String[] scannedPhenotypeNames = this.getScannedPhenotypeNames();
        
        // first see if just one phenotype was scanned
        if(scannedPhenotypeNames.length == 1)
        {
            return scannedPhenotypeNames[0];
        }
        
        // OK, we have more than one phenotype. see if any are exact matches
        for(int i = 0; i < scannedPhenotypeNames.length; i++)
        {
            if(scanColumnName.equals(scannedPhenotypeNames[i]))
            {
                return scannedPhenotypeNames[i];
            }
        }
        
        // OK, there are no exact matches. lets see if we can find a
        // match based on a two-part scan distribution
        for(int i = 0; i < scannedPhenotypeNames.length; i++)
        {
            if(scanColumnName.equals(LOD_P_PREFIX + scannedPhenotypeNames[i]) ||
               scanColumnName.equals(LOD_MU_PREFIX + scannedPhenotypeNames[i]) ||
               scanColumnName.equals(LOD_P_MU_PREFIX + scannedPhenotypeNames[i]))
            {
                return scannedPhenotypeNames[i];
            }
        }
        
        // we could not find any match
        LOG.warning("failed to find a phenotype to match column name: " + scanColumnName);
        return null;
    }
}
