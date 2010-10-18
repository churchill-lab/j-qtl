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
package org.jax.util.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test for {@link Matlab} class.
 */
public class MatlabTest {

    /**
     * Test for the {@link Matlab#abs(int[])} method.
     */
    @Test
    public void testAbsIntArray()
    {
        // generate an integer array with mixed positives and negatives.
        int[] intArray = new int[100];
        int[] posIntArray = new int[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = (i % 2 == 0) ? i : -i;
            posIntArray[i] = i;
        }
        
        int[] absIntArray = Matlab.abs(intArray);
        
        // make sure we got back what we were expecting
        for (int i = 0; i < absIntArray.length; i++) {
            Assert.assertArrayEquals(absIntArray, intArray);
        }
    }
}
