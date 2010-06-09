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

package org.jax.r.jriutilities;

import org.junit.Test;
import org.rosuda.JRI.REXP;

/**
 * JUnit test for {@link BasicRInterface}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BasicRInterfaceTest
{
    /**
     * @throws Exception
     */
    @Test()
    public void testEvaluateCommand() throws Exception
    {
        System.out.println(System.getenv("PATH"));
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("evaluating ls()");
        BasicRInterface basicRInterface = (BasicRInterface)RInterfaceFactory.getRInterfaceInstance();
        REXP resultOfLs = basicRInterface.evaluateCommand("ls()");
        System.out.println("ls(): " + resultOfLs.toString());
    }
}
