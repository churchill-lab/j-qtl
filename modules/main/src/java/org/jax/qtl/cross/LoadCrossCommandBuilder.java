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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.RCommandParameter;
import org.jax.r.RMethodInvocationCommand;
import org.jax.r.RUtilities;

/**
 * The command builder for loading a cross
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadCrossCommandBuilder
{
    /**
     * The enum for cross file format
     */
    public enum CrossFileFormat
    {
        /**
         * if the file is comma delimited
         */
        COMMA_DELIMITED
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Comma-Delimited";
            }
        },
        
        /**
         * if the comma delimited file is rotated
         */
        ROTATED_COMMA_DELIMITED
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString()
            {
                return "Rotated Comma-Delimited";
            }
        }
    }
    
    private volatile CrossFileFormat format = CrossFileFormat.COMMA_DELIMITED;
    
    private volatile File dataFile = null;
    
    private volatile String[] genotypes = new String[] {"A", "H", "B", "D", "C"};
    
    private volatile String[] naStrings = new String[] {"-"};
    
    private volatile String crossName = "";
    
    private volatile boolean convertXData = true;

    /**
     * Getter for the cross file format
     * @return
     *          the cross file format
     */
    public CrossFileFormat getFormat()
    {
        return this.format;
    }

    /**
     * Setter for the cross file format
     * @param format
     *          the format to set
     */
    public void setFormat(CrossFileFormat format)
    {
        this.format = format;
    }

    /**
     * Getter for the data file
     * @return
     *          the data file
     */
    public File getDataFile()
    {
        return this.dataFile;
    }

    /**
     * Setter for the data file
     * @param dataFile
     *          the data file
     */
    public void setDataFile(File dataFile)
    {
        this.dataFile = dataFile;
    }

    /**
     * Getter for the genotypes
     * @return
     *          the genotypes
     */
    public String[] getGenotypes()
    {
        return this.genotypes;
    }

    /**
     * Setter for the genotypes
     * @param genotypes
     *          the genotypes
     */
    public void setGenotypes(String[] genotypes)
    {
        this.genotypes = genotypes;
    }

    /**
     * Getter for the NA strings
     * @return
     *          the NA strings
     */
    public String[] getNaStrings()
    {
        return this.naStrings;
    }

    /**
     * Setter for the NA strings
     * @param naStrings
     *          the NA strings
     */
    public void setNaStrings(String[] naStrings)
    {
        this.naStrings = naStrings;
    }

    /**
     * Getter for the cross name
     * @return
     *          the cross name
     */
    public String getCrossName()
    {
        return this.crossName;
    }
    
    /**
     * Setter for the cross name
     * @param crossName
     *          the cross name
     */
    public void setCrossName(String crossName)
    {
        this.crossName = crossName;
    }
    
    /**
     * Determines if we convert the X data
     * @return
     *          true if we do convert the X data
     */
    public boolean getConvertXData()
    {
        return this.convertXData;
    }
    
    /**
     * Setter for determining if we convert the X data
     * @param convertXData
     *          true if we convert the X data
     */
    public void setConvertXData(boolean convertXData)
    {
        this.convertXData = convertXData;
    }
    
    /**
     * Getter for the command
     * @return
     *          the command
     */
    public RCommand getCommand()
    {
        RCommand readCrossCommand = new RMethodInvocationCommand(
                "read.cross",
                this.getCommandParameters());
        
        String crossName = this.crossName;
        if(crossName == null || crossName.trim().length() == 0)
        {
            return readCrossCommand;
        }
        else
        {
            return new RAssignmentCommand(
                    crossName.trim(),
                    readCrossCommand.getCommandText());
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
        
        String formatString = null;
        CrossFileFormat format = this.format;
        if(format != null)
        {
            switch(format)
            {
                case COMMA_DELIMITED:
                {
                    formatString = "csv";
                }
                break;
                
                case ROTATED_COMMA_DELIMITED:
                {
                    formatString = "csvr";
                }
                break;
            }
        }
        if(formatString != null)
        {
            parameters.add(new RCommandParameter(
                    "format",
                    RUtilities.javaStringToRString(formatString)));
        }
        
        File dataFile = this.dataFile;
        if(dataFile != null)
        {
            parameters.add(new RCommandParameter(
                    "file",
                    RUtilities.javaStringToRString(dataFile.getAbsolutePath())));
        }
        
        String[] genotypes = this.genotypes;
        if(genotypes != null)
        {
            parameters.add(new RCommandParameter(
                    "genotypes",
                    RUtilities.stringArrayToRVector(genotypes)));
        }
        
        String[] naStrings = this.naStrings;
        if(naStrings != null)
        {
            parameters.add(new RCommandParameter(
                    "na.strings",
                    RUtilities.stringArrayToRVector(naStrings)));
        }
        
        parameters.add(new RCommandParameter(
                "convertXdata",
                RUtilities.javaBooleanToRBoolean(this.convertXData)));
        
        return parameters;
    }
}
