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

/**
 * A class for holding the data in an ANOVA table
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AnovaTable
{
    private final String[] rowNames;
    
    private final String[] columnNames;
    
    private final double[][] matrixData;

    /**
     * Construct a new ANOVA table
     * @param rowNames
     * @param columnNames
     * @param matrixData
     */
    public AnovaTable(
            String[] rowNames,
            String[] columnNames,
            double[][] matrixData)
    {
        this.rowNames = rowNames;
        this.columnNames = columnNames;
        this.matrixData = matrixData;
    }
    
    /**
     * Getter for the row names
     * @return the rowNames
     */
    public String[] getRowNames()
    {
        return this.rowNames;
    }
    
    /**
     * Getter for the column names
     * @return the columnNames
     */
    public String[] getColumnNames()
    {
        return this.columnNames;
    }
    
    /**
     * Getter for the matrix data
     * @return the matrixData
     */
    public double[][] getMatrixData()
    {
        return this.matrixData;
    }
}
