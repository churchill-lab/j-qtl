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

import org.jax.qtl.cross.GeneticMap.MapType;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RObject;

/**
 * A genetic map that knows about sex specific structure
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SexAwareGeneticMap extends RObject
{
    private final boolean hasSexSpecificGenotypeMaps;

    private final GeneticMap maleGeneticMap;

    private final GeneticMap femaleGeneticMap;

    private final GeneticMap sexAgnosticGeneticMap;
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface
     * @param accessorExpressionString
     *          the accessor expression string
     * @param chromosomeName
     *          the chromosome name
     */
    public SexAwareGeneticMap(
            RInterface rInterface,
            String accessorExpressionString,
            String chromosomeName)
    {
        super(rInterface, accessorExpressionString);
        
        this.hasSexSpecificGenotypeMaps =
            JRIUtilityFunctions.inheritsRClass(this, "matrix");
        if(this.hasSexSpecificGenotypeMaps)
        {
            this.maleGeneticMap = new GeneticMap(
                    chromosomeName,
                    MapType.MALE,
                    this);
            this.femaleGeneticMap = new GeneticMap(
                    chromosomeName,
                    MapType.FEMALE,
                    this);
            this.sexAgnosticGeneticMap = null;
        }
        else
        {
            this.maleGeneticMap = null;
            this.femaleGeneticMap = null;
            this.sexAgnosticGeneticMap = new GeneticMap(
                    chromosomeName,
                    MapType.SEX_AGNOSTIC,
                    this);
        }
    }
    
    /**
     * Extract maps from the given R object
     * @param mapsRObject
     *          the maps R object to extract data from
     * @return
     *          the genetic maps
     */
    public static List<SexAwareGeneticMap> extractMaps(
            RObject mapsRObject)
    {
        String[] chromosomeNames = JRIUtilityFunctions.getNames(mapsRObject);
        List<SexAwareGeneticMap> maps =
            new ArrayList<SexAwareGeneticMap>(chromosomeNames.length);
        for(String currChromosomeName: chromosomeNames)
        {
            SexAwareGeneticMap sexAwareGeneticMap = new SexAwareGeneticMap(
                    mapsRObject.getRInterface(),
                    mapsRObject.getAccessorExpressionString() +
                    "$\"" + currChromosomeName + "\"",
                    currChromosomeName);
            maps.add(sexAwareGeneticMap);
        }
        
        return maps;
    }

    /**
     * Use this getter to determine if the chromosome has sex specific maps,
     * or if there is just one sex agnostic map
     * @return
     *          true iff this chromosome has sex specific maps
     */
    public boolean getHasSexSpecificGenotypeMaps()
    {
        return this.hasSexSpecificGenotypeMaps;
    }

    /**
     * Getter for the male map
     * @return
     *          the male map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false
     */
    public GeneticMap getMaleGeneticMap()
    {
        return this.maleGeneticMap;
    }

    /**
     * Getter for the female map
     * @return
     *          the female map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false
     */
    public GeneticMap getFemaleGeneticMap()
    {
        return this.femaleGeneticMap;
    }

    /**
     * Getter for the sex agnostic map
     * @return
     *          the sex agnostic map or null if
     *          {@link #getHasSexSpecificGenotypeMaps()} is true
     */
    public GeneticMap getSexAgnosticGeneticMap()
    {
        return this.sexAgnosticGeneticMap;
    }
    
    /**
     * Gets any valid genetic map.
     * @return
     *          the female genetic map if
     *          {@link #getHasSexSpecificGenotypeMaps()} is true, and
     *          the sex agnostic map if
     *          {@link #getHasSexSpecificGenotypeMaps()} is false.
     *          This should never be a null value.
     */
    public GeneticMap getAnyGeneticMap()
    {
        if(this.hasSexSpecificGenotypeMaps)
        {
            return this.femaleGeneticMap;
        }
        else
        {
            return this.sexAgnosticGeneticMap;
        }
    }
}
