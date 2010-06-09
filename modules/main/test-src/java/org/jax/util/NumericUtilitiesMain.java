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

package org.jax.util;

import java.math.RoundingMode;

import org.jax.util.math.NumericUtilities;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class NumericUtilitiesMain
{
    /**
     * private... no instances allowed
     */
    private NumericUtilitiesMain()
    {
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("PI in increasing significance rounding up");
        for(int i = 0; i < 20; i++)
        {
            System.out.println(i + " sig dig: " + NumericUtilities.roundToSignificantDigitsDouble(Math.PI, i, RoundingMode.UP));
        }
        System.out.println();
        
        System.out.println("PI in increasing significance rounding default");
        for(int i = 0; i < 20; i++)
        {
            System.out.println(i + " sig dig: " + NumericUtilities.roundToSignificantDigitsDouble(Math.PI, i));
        }
        System.out.println();
        
        System.out.println("PI*100000 in increasing significance rounding default");
        for(int i = 0; i < 20; i++)
        {
            System.out.println(i + " sig dig: " + NumericUtilities.roundToSignificantDigitsDouble(Math.PI*100000, i));
        }
        System.out.println();
        
        System.out.println("PI/100000 in increasing significance rounding default");
        for(int i = 0; i < 20; i++)
        {
            System.out.println(i + " sig dig: " + NumericUtilities.roundToSignificantDigitsDouble(Math.PI/100000, i));
        }
        System.out.println();
        
        System.out.println("PI rounded to 10's position");
        for(int i = -4; i < 4; i++)
        {
            System.out.println(i + " 10's position: " + NumericUtilities.roundToDecimalPositionDouble(Math.PI, i));
        }
        System.out.println();
        
        System.out.println("PI*100 rounded to 10's position");
        for(int i = -4; i < 4; i++)
        {
            System.out.println(i + " 10's position: " + NumericUtilities.roundToDecimalPositionDouble(Math.PI*100, i));
        }
        System.out.println();
        
        System.out.println("PI/100 rounded to 10's position");
        for(int i = -4; i < 4; i++)
        {
            System.out.println(i + " 10's position: " + NumericUtilities.roundToDecimalPositionDouble(Math.PI/100, i));
        }
        System.out.println();
        
        System.out.println("Most significant decimal of: " + Math.PI);
        System.out.println(NumericUtilities.getMostSignificantDecimalPosition(Math.PI));
        
        System.out.println("Most significant decimal of: " + (Math.PI/1000));
        System.out.println(NumericUtilities.getMostSignificantDecimalPosition(Math.PI/1000));
        
        System.out.println("Most significant decimal of: " + (Math.PI*100));
        System.out.println(NumericUtilities.getMostSignificantDecimalPosition(Math.PI*100));
        
        System.out.println("again, but rounded to most significant - 1");
        System.out.println(NumericUtilities.roundToDecimalPositionDouble(
                Math.PI,
                NumericUtilities.getMostSignificantDecimalPosition(Math.PI) - 1));
        
        System.out.println("again, but rounded to most significant - 1");
        System.out.println(NumericUtilities.roundToDecimalPositionDouble(
                Math.PI/1000,
                NumericUtilities.getMostSignificantDecimalPosition(Math.PI/1000) - 1));
        
        System.out.println("again, but rounded to most significant - 1");
        System.out.println(NumericUtilities.roundToDecimalPositionDouble(
                Math.PI*100,
                NumericUtilities.getMostSignificantDecimalPosition(Math.PI*100) - 1));
    }
}
