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

package org.jax.analyticgraph.graph.histogram;

import java.util.Random;

import javax.swing.WindowConstants;

import org.jax.analyticgraph.data.NamedRealData;
import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;

/**
 * main tester for {@link Histogram}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class HistogramMain
{
    /**
     * main function for histogram tester
     * @param args
     *          don't care
     */
    public static void main(String[] args)
    {
        displayTestGUI();
    }
    
    /**
     * Test method for testing the display GUI.
     */
    private static void displayTestGUI()
    {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        
        org.jax.analyticgraph.framework.Graph2DComponent graphComponent =
            new org.jax.analyticgraph.framework.Graph2DComponent();
        graphComponent.addGraph2D(createExampleHistogram());
        
        frame.setContentPane(graphComponent);
        
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.repaint();
    }
    
    /**
     * Create a histogram with a normal distribution
     * @return
     *          the histogram
     */
    public static Histogram createExampleHistogram()
    {
        Random rand = new Random();
        
        Double[] randomData = new Double[2000];
        for(int i = 0; i < randomData.length; i++)
        {
            randomData[i] = rand.nextGaussian();
        }
        
        NamedRealData namedRealDataX = new NamedRealData("Gaussian Distribution", randomData);

        GraphCoordinateConverter coordinateConverter = new SimpleGraphCoordinateConverter(
                0.0, 0.0,
                1.0, 1.0);
        Histogram histogram = new Histogram(coordinateConverter);
        histogram.setGraphData(namedRealDataX);
        
        return histogram;
    }
}
