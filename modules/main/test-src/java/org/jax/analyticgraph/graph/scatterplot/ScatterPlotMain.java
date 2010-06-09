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

package org.jax.analyticgraph.graph.scatterplot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.WindowConstants;

import org.jax.analyticgraph.data.NamedData;
import org.jax.analyticgraph.data.NamedDataMatrix;
import org.jax.analyticgraph.data.NamedRealData;
import org.jax.analyticgraph.data.SimpleSelectableNamedDataMatrix;
import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;

/**
 * A tester mini-app for {@link ScatterPlot}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScatterPlotMain
{
    /**
     * Main method that displays scatterplot for bivariate normal
     * distribution. This is just a simple test of functionality.
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
        graphComponent.addGraph2D(createExampleScatterPlot());
        
        frame.setContentPane(graphComponent);
        
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.repaint();
    }
    
    /**
     * Create a scatter plot interior with a bivariate normal distribution
     * @return
     *          the scatter plot interior
     */
    public static ScatterPlot createExampleScatterPlot()
    {
        Random rand = new Random();
        
        Double[] randomData1 = new Double[2000];
        Double[] randomData2 = new Double[randomData1.length];
        for(int i = 0; i < randomData1.length; i++)
        {
            randomData1[i] = rand.nextGaussian();
            randomData2[i] = rand.nextGaussian();
        }
        
        List<NamedData<Number>> namedDataList =
            new ArrayList<NamedData<Number>>(2);
        NamedRealData namedRealDataX = new NamedRealData("x axis", randomData1);
        namedDataList.add(namedRealDataX);
        NamedRealData namedRealDataY = new NamedRealData("y axis", randomData2);
        namedDataList.add(namedRealDataY);
        
        NamedDataMatrix<Number> data = new SimpleSelectableNamedDataMatrix<Number>(
                namedDataList);

        GraphCoordinateConverter coordinateConverter = new SimpleGraphCoordinateConverter(
                0.0, 0.0,
                1.0, 1.0);
        ScatterPlot scatterPlotInterior = new ScatterPlot(coordinateConverter);
        scatterPlotInterior.plotData(data);
        
        return scatterPlotInterior;
    }
}
