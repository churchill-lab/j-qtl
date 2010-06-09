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

package org.jax.qtl.fit;

import java.util.Arrays;

import org.jax.qtl.cross.Cross;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.rosuda.JRI.REXP;

/**
 * Class for holding on to the R data for fitqtl
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class FitQtlResult extends RObject
{
    /**
     * the R class name used for fit results
     */
    public static final String FIT_QTL_RESULT_TYPE_STRING = "fitqtl";
    
    private static final String FULL_RESULTS_COMPONENT_NAME = "result.full";
    
    private static final String DROP_RESULTS_COMPONENT_NAME = "result.drop";
    
    private final Cross parentCross;
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface that backs this object
     * @param accessorExpressionString
     *          the R accessor expression for this object
     * @param parentCross 
     *          the parent cross for this FIT
     */
    public FitQtlResult(
            RInterface rInterface,
            String accessorExpressionString,
            Cross parentCross)
    {
        super(rInterface, accessorExpressionString);
        this.parentCross = parentCross;
    }
    
    /**
     * Getter for the parent cross of this fit
     * @return the parent cross
     */
    public Cross getParentCross()
    {
        return this.parentCross;
    }
    
    /**
     * Helper function to fetch the ANOVA table for the given component
     * @param componentName
     *          the name of the component
     * @return
     *          the table or null if we cant find one
     */
    private AnovaTable getAnovaTableForComponent(String componentName)
    {
        String[] names = JRIUtilityFunctions.getNames(this);
        if(Arrays.asList(names).contains(componentName))
        {
            RObject anovaTableObject = new RObject(
                    this.getRInterface(),
                    this.getAccessorExpressionString() +
                    "$" + componentName);
            
            // extract all the data we need from the results and
            // return the anova table
            String[] columns = JRIUtilityFunctions.getColumnNames(
                    anovaTableObject);
            String[] rows = JRIUtilityFunctions.getRowNames(
                    anovaTableObject);
            REXP matrixDataRExpression =
                anovaTableObject.getRInterface().evaluateCommand(
                        new SilentRCommand(
                                anovaTableObject.getAccessorExpressionString()));
            double[][] matrixData =
                matrixDataRExpression.asDoubleMatrix();
            
            return new AnovaTable(rows, columns, matrixData);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Getter for the full results ANOVA table
     * @return
     *          the full results ANOVA table or null if there is no full results
     *          table
     */
    public AnovaTable getFullResults()
    {
        return this.getAnovaTableForComponent(FULL_RESULTS_COMPONENT_NAME);
    }
    
    /**
     * Getter for the drop-one-term results ANOVA table
     * @return
     *          the drop-one-term table or null if there isn't one
     */
    public AnovaTable getDropOneTermResults()
    {
        return this.getAnovaTableForComponent(DROP_RESULTS_COMPONENT_NAME);
    }
}
