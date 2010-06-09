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

package org.jax.qtl.cross.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jax.analyticgraph.framework.AbstractGraph2DWithAxes;
import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisDescription;
import org.jax.analyticgraph.graph.CategoricalAxisDescription;
import org.jax.analyticgraph.graph.RegularIntervalAxisDescription;
import org.jax.analyticgraph.graph.AxisDescription.AxisType;

/**
 * For rendering effect plots
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EffectPlot extends AbstractGraph2DWithAxes
{
    /**
     * the colors that we can cycle through for drawing multiple lines
     */
    private static final Color[] AVAILABLE_COLORS = new Color[] {
        Color.BLUE,
        Color.BLACK,
        Color.ORANGE,
        Color.CYAN,
        Color.GREEN,
        Color.GRAY,
        Color.PINK,
        Color.YELLOW,
        Color.LIGHT_GRAY};
    
    private static final int DEFAULT_Y_AXIS_TICK_INTERVAL_SIGNIFICANT_DIGITS = 2;
    
    private static final int DEFAULT_Y_AXIS_TICK_COUNT = 10;
    
    /**
     * A data point for the effect plot
     */
    public static class EffectPlotDataPoint
    {
        private final double value;
        
        private final double standardDeviation;

        /**
         * Constructor
         * @param value
         *          see {@link #getValue()}
         * @param standardDeviation
         *          see {@link #getStandardDeviation()}
         */
        public EffectPlotDataPoint(double value, double standardDeviation)
        {
            this.value = value;
            this.standardDeviation = standardDeviation;
        }
        
        /**
         * Getter for the value
         * @return the value
         */
        public double getValue()
        {
            return this.value;
        }
        
        /**
         * Getter for the standard deviation
         * @return the standardDeviation
         */
        public double getStandardDeviation()
        {
            return this.standardDeviation;
        }
        
        /**
         * Get the low point of the effect bar
         * @return
         *          the low point
         */
        public double getLowerStandardDeviationBarPosition()
        {
            return this.value - this.standardDeviation;
        }
        
        /**
         * Get the high point on the effect bar
         * @return
         *          the high point
         */
        public double getUpperStandardDeviationBarPosition()
        {
            return this.value + this.standardDeviation;
        }
    }
    
    /**
     * The effect plot data.
     */
    public static class EffectPlotData
    {
        private final EffectPlotDataPoint[][] effectLines;
        
        private final String[] effectLineNames;
        
        private final String[] effectPointNames;
        
        private final String xAxisName;
        
        private final String yAxisName;
        
        private final String effectLinesGroupingName;
        
        /**
         * Constructor
         * @param xAxisName
         *          the x axis name
         * @param yAxisName
         *          the y axis name
         * @param effectPointNames
         *          the names of the effect points
         * @param effectLine
         *          the effect line data
         */
        public EffectPlotData(
                String xAxisName,
                String yAxisName,
                String[] effectPointNames,
                EffectPlotDataPoint[] effectLine)
        {
            this.xAxisName = xAxisName;
            this.yAxisName = yAxisName;
            this.effectLinesGroupingName = "";
            this.effectPointNames = effectPointNames;
            this.effectLines = new EffectPlotDataPoint[][] {effectLine};
            this.effectLineNames = new String[0];
        }
        
        /**
         * Constructor
         * @param xAxisName
         *          the x axis name
         * @param yAxisName
         *          the y axis name
         * @param effectLinesGroupingName
         *          the effect lines grouping name
         * @param effectPointNames
         *          see {@link #effectPointNames}
         * @param effectLines
         *          see {@link #getEffectLines()}
         * @param effectLineNames
         *          see {@link #getEffectLineNames()}
         */
        public EffectPlotData(
                String xAxisName,
                String yAxisName,
                String effectLinesGroupingName,
                String[] effectPointNames,
                EffectPlotDataPoint[][] effectLines,
                String[] effectLineNames)
        {
            this.xAxisName = xAxisName;
            this.yAxisName = yAxisName;
            this.effectLinesGroupingName = effectLinesGroupingName;
            this.effectPointNames = effectPointNames;
            this.effectLines = effectLines;
            this.effectLineNames = effectLineNames;
        }
        
        /**
         * Getter for the effect lines
         * @return
         *          the effect lines
         */
        public EffectPlotDataPoint[][] getEffectLines()
        {
            return this.effectLines;
        }
        
        /**
         * Getter for the effect line names
         * @return
         *          the effect line names
         */
        public String[] getEffectLineNames()
        {
            return this.effectLineNames;
        }
        
        /**
         * Getter for the effect point names
         * @return
         *          the effect point names
         */
        public String[] getEffectPointNames()
        {
            return this.effectPointNames;
        }
        
        /**
         * @return the xAxisName
         */
        public String getXAxisName()
        {
            return this.xAxisName;
        }
        
        /**
         * @return the yAxisName
         */
        public String getYAxisName()
        {
            return this.yAxisName;
        }
        
        /**
         * @return the effectLinesGroupingName
         */
        public String getEffectLinesGroupingName()
        {
            return this.effectLinesGroupingName;
        }
    }
    
    private final EffectPlotData effectPlotData;
    
    private final CategoricalAxisDescription xAxisDescription;
    
    private final AxisDescription yAxisDescription;
    
    /**
     * The default size in pixles that we use for the cap on the effect
     * bars
     */
    public static final int DEFAULT_EFFECT_BAR_CAP_SIZE_IN_PIXELS = 6;
    
    /**
     * @see #getEffectBarCapSizeInPixels()
     */
    private volatile int effectBarCapSizeInPixels = DEFAULT_EFFECT_BAR_CAP_SIZE_IN_PIXELS;
    
    /**
     * Constructor
     * @param effectPlotData
     *          the data to plot
     */
    public EffectPlot(
            EffectPlotData effectPlotData)
    {
        super(new SimpleGraphCoordinateConverter());
        
        this.effectPlotData = effectPlotData;
        
        double minYRange = EffectPlot.getMinimumValueMinusStandardDeviation(
                effectPlotData.getEffectLines());
        double maxYRange = EffectPlot.getMaximumValuePlusStandardDeviation(
                effectPlotData.getEffectLines());
        this.getGraphCoordinateConverter().updateGraphDimensions(
                0.0,
                minYRange,
                1.0,
                maxYRange - minYRange);
        this.xAxisDescription = new CategoricalAxisDescription(
                this.getGraphCoordinateConverter(),
                AxisType.X_AXIS,
                effectPlotData.getXAxisName(),
                effectPlotData.getEffectPointNames());
        this.yAxisDescription = new RegularIntervalAxisDescription(
                this.getGraphCoordinateConverter(),
                AxisType.Y_AXIS,
                effectPlotData.getYAxisName(),
                DEFAULT_Y_AXIS_TICK_COUNT,
                DEFAULT_Y_AXIS_TICK_INTERVAL_SIGNIFICANT_DIGITS,
                true);
    }
    
    /**
     * Getter for the effect bar cap size in pixels
     * @return the effectBarCapSizeInPixels
     */
    public int getEffectBarCapSizeInPixels()
    {
        return this.effectBarCapSizeInPixels;
    }
    
    /**
     * Setter for the effect bar cap size in pixels. If this is not a
     * multiple of 2 it will be treated as if it's the next lower multiple
     * of two (Eg: 9 gets treated like 8).
     * @param effectBarCapSizeInPixels the effectBarCapSizeInPixels to set
     */
    public void setEffectBarCapSizeInPixels(int effectBarCapSizeInPixels)
    {
        this.effectBarCapSizeInPixels = effectBarCapSizeInPixels;
    }
    
    /**
     * This is the opposite of
     * {@link #getMinimumValueMinusStandardDeviation(org.jax.qtl.cross.gui.EffectPlot.EffectPlotDataPoint[][])}
     * @param effectLines
     *          the effect lines we're checking
     * @return
     *          the maximum value
     */
    private static double getMaximumValuePlusStandardDeviation(
            EffectPlotDataPoint[][] effectLines)
    {
        double max = effectLines[0][0].getUpperStandardDeviationBarPosition();
        for(EffectPlotDataPoint[] currEffectLine: effectLines)
        {
            for(EffectPlotDataPoint currEffectPoint: currEffectLine)
            {
                double currUpperBarPosition =
                    currEffectPoint.getUpperStandardDeviationBarPosition();
                if(currUpperBarPosition > max)
                {
                    max = currUpperBarPosition;
                }
            }
        }
        
        return max;
    }

    /**
     * Basically this gets the lowest point that the error bar will dip to
     * @param effectLines
     *          the effect lines we're checking
     * @return
     *          the minimum value
     */
    private static double getMinimumValueMinusStandardDeviation(
            EffectPlotDataPoint[][] effectLines)
    {
        double min = effectLines[0][0].getLowerStandardDeviationBarPosition();
        for(EffectPlotDataPoint[] currEffectLine: effectLines)
        {
            for(EffectPlotDataPoint currEffectPoint: currEffectLine)
            {
                double currLowerBarPosition =
                    currEffectPoint.getLowerStandardDeviationBarPosition();
                if(currLowerBarPosition < min)
                {
                    min = currLowerBarPosition;
                }
            }
        }
        
        return min;
    }

    /**
     * Getter for the effect plot data that this class is rendering
     * @return
     *          the data
     */
    public EffectPlotData getEffectPlotData()
    {
        return this.effectPlotData;
    }

    /**
     * {@inheritDoc}
     */
    public void renderGraph(Graphics2D graphics2D)
    {
        // save the current color
        Color saveColor = graphics2D.getColor();
        
        EffectPlotData effectPlotData = this.effectPlotData;
        EffectPlotDataPoint[][] effectLines =
            effectPlotData.getEffectLines();
        for(int currLineIndex = 0;
            currLineIndex < effectLines.length;
            currLineIndex++)
        {
            graphics2D.setColor(
                    AVAILABLE_COLORS[currLineIndex % AVAILABLE_COLORS.length]);
            
            EffectPlotDataPoint prevPoint = null;
            double prevPointX = 0;
            EffectPlotDataPoint[] currEffectLine = effectLines[currLineIndex];
            for(int currPointIndex = 0;
                currPointIndex < currEffectLine.length;
                currPointIndex++)
            {
                EffectPlotDataPoint currPoint =
                    currEffectLine[currPointIndex];
                double pointX = this.xAxisDescription.getCategoryAxisPosition(
                        currPointIndex);
                
                // render the current effect point and bars
                this.renderLine(
                        graphics2D,
                        pointX,
                        currPoint.getUpperStandardDeviationBarPosition(),
                        pointX,
                        currPoint.getLowerStandardDeviationBarPosition());
                this.renderPoint(
                        graphics2D,
                        pointX,
                        currPoint.getValue());
                
                // render the caps
                this.renderCapLine(
                        graphics2D,
                        pointX,
                        currPoint.getUpperStandardDeviationBarPosition());
                this.renderCapLine(
                        graphics2D,
                        pointX,
                        currPoint.getLowerStandardDeviationBarPosition());
                
                if(prevPoint != null)
                {
                    // connect the center of this effect bar to the previous
                    // effect bar
                    this.renderLine(
                            graphics2D,
                            prevPointX,
                            prevPoint.getValue(),
                            pointX,
                            currPoint.getValue());
                }
                
                // remember the point data for the next iteration (if there
                // is one)
                prevPoint = currPoint;
                prevPointX = pointX;
            }
        }
        
        // render the key only if we have more than one on the graph
        String[] effectLineNames = effectPlotData.getEffectLineNames();
        if(effectLineNames != null && effectLineNames.length > 1)
        {
            this.renderKey(
                    graphics2D,
                    effectPlotData.getEffectLinesGroupingName(),
                    effectLineNames,
                    effectPlotData.getEffectPointNames().length);
        }
        
        // restore the color
        graphics2D.setColor(saveColor);
    }
    
    /**
     * this font name comes with {@link java.awt.Font} as of java 6.0,
     * but we need to be java 5.0 compatible
     */
    // TODO for now this is OK, but get rid of this when we move to 6.0
    private static final String SANS_SERIF_FONT_NAME = "SansSerif";
    private static final Font KEY_LABEL_FONT = new Font(
            SANS_SERIF_FONT_NAME,
            Font.PLAIN, 10);
    private static final Font KEY_GROUP_FONT = new Font(
            SANS_SERIF_FONT_NAME,
            Font.PLAIN, 14);
    private static final int KEY_BUFFER_PIXELS = 3;
    private static final int KEY_LINE_LENGTH_PIXELS = 20;
    
    /**
     * Render the key using the given line names
     * @param graphics2D
     *          the graphics context to render to
     * @param effectLineNames
     *          the line names in the key
     */
    private void renderKey(
            Graphics2D graphics2D,
            String effectLineGroupName,
            String[] effectLineNames,
            int numEffectPoints)
    {
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        
        graphics2D.setColor(Color.BLACK);
        
        FontRenderContext frc = graphics2D.getFontRenderContext();
        GlyphVector groupLabelGlyph = KEY_GROUP_FONT.createGlyphVector(
                frc,
                effectLineGroupName);
        
        // figure out the placement of the key
        double maxWidthPixels = 0.0;
        double cumulativeHeightPixels = 0.0;
        GlyphVector[] glyphVectors = new GlyphVector[effectLineNames.length];
        for(int lineIndex = 0; lineIndex < effectLineNames.length; lineIndex++)
        {
            String currEffectLineName = effectLineNames[lineIndex];
            GlyphVector glyphVector =
                KEY_LABEL_FONT.createGlyphVector(frc, currEffectLineName);
            glyphVectors[lineIndex] = glyphVector;
            
            Rectangle2D currBounds = glyphVector.getLogicalBounds();
            cumulativeHeightPixels += currBounds.getHeight();
            
            if(currBounds.getWidth() > maxWidthPixels)
            {
                maxWidthPixels = currBounds.getWidth();
            }
        }
        
        double keyWidthPixels =
            KEY_BUFFER_PIXELS + maxWidthPixels + KEY_BUFFER_PIXELS +
            KEY_LINE_LENGTH_PIXELS + KEY_BUFFER_PIXELS;
        double keyHeightPixels = cumulativeHeightPixels + (2 * effectLineNames.length);
        
        // center the key between the 1st and last points
        double lastPosition = this.xAxisDescription.getCategoryAxisPosition(
                numEffectPoints - 1);
        double secondToLastPosition = this.xAxisDescription.getCategoryAxisPosition(
                numEffectPoints - 2);
        double keyCenterXGraph = (lastPosition + secondToLastPosition) / 2.0;
        double keyCenterXPixel =
            coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                    keyCenterXGraph);
        double keyLeftXPixel = keyCenterXPixel - (keyWidthPixels / 2.0);
        
        // draw the group label
        double yPixelCursor =
            coordConverter.getAbsoluteYOffsetInPixels() + KEY_BUFFER_PIXELS;
        {
            Rectangle2D groupLogicalBounds = groupLabelGlyph.getLogicalBounds();
            
            AffineTransform at = new AffineTransform();
            at.translate(
                    keyLeftXPixel,
                    yPixelCursor + groupLogicalBounds.getHeight());
            Shape groupLabel =
                at.createTransformedShape(groupLabelGlyph.getOutline());
            graphics2D.fill(groupLabel);
            yPixelCursor +=
                groupLabelGlyph.getLogicalBounds().getHeight() +
                KEY_BUFFER_PIXELS;
        }
        
        // iterate through the names
        double keyTopYPixels = yPixelCursor;
        
        // draw the box for the key
        Rectangle2D.Double keyRectangle = new Rectangle2D.Double(
                keyLeftXPixel,
                keyTopYPixels,
                keyWidthPixels,
                keyHeightPixels);
        graphics2D.setColor(Color.WHITE);
        graphics2D.fill(keyRectangle);
        graphics2D.setColor(Color.BLACK);
        graphics2D.draw(keyRectangle);
        
        for(int lineIndex = 0; lineIndex < glyphVectors.length; lineIndex++)
        {
            graphics2D.setColor(Color.BLACK);
            
            double currLeftMargin = keyLeftXPixel + KEY_BUFFER_PIXELS;
            
            GlyphVector currGlyph = glyphVectors[lineIndex];
            Rectangle2D currLogicalBounds = currGlyph.getLogicalBounds();
            AffineTransform at = new AffineTransform();
            at.translate(
                    currLeftMargin,
                    yPixelCursor + currLogicalBounds.getHeight());
            Shape currLabel = at.createTransformedShape(
                    currGlyph.getOutline());
            graphics2D.fill(currLabel);
            
            currLeftMargin += maxWidthPixels + KEY_BUFFER_PIXELS;
            graphics2D.setColor(
                    AVAILABLE_COLORS[lineIndex % AVAILABLE_COLORS.length]);
            graphics2D.draw(new Line2D.Double(
                    currLeftMargin,
                    yPixelCursor + (currLogicalBounds.getHeight() / 2.0),
                    currLeftMargin + KEY_LINE_LENGTH_PIXELS,
                    yPixelCursor + (currLogicalBounds.getHeight() / 2.0)));
            
            yPixelCursor +=
                currLogicalBounds.getHeight() + KEY_BUFFER_PIXELS;
        }
    }

    /**
     * Render one of the caps that sits at either end of the effect bar.
     * @param graphics2D
     *          the graphics context
     * @param capPositionGraphX
     *          the x position of the cap
     * @param capPositionGraphY
     *          the y position of the cap
     */
    private void renderCapLine(
            Graphics2D graphics2D,
            double capPositionGraphX,
            double capPositionGraphY)
    {
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        double capPositionPixleX = coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                capPositionGraphX);
        double capPositionPixleY = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                capPositionGraphY);
        
        graphics2D.draw(new Line2D.Double(
                capPositionPixleX - this.effectBarCapSizeInPixels,
                capPositionPixleY,
                capPositionPixleX + this.effectBarCapSizeInPixels,
                capPositionPixleY));
    }

    private void renderLine(
            Graphics2D graphics2D,
            double graphX1, double graphY1,
            double graphX2, double graphY2)
    {
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        double pixleX1 = coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                graphX1);
        double pixleY1 = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                graphY1);
        double pixleX2 = coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                graphX2);
        double pixleY2 = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                graphY2);
        
        graphics2D.draw(new Line2D.Double(
                pixleX1, pixleY1,
                pixleX2, pixleY2));
    }

    /**
     * {@inheritDoc}
     */
    public CategoricalAxisDescription getXAxisDescription()
    {
        return this.xAxisDescription;
    }

    /**
     * {@inheritDoc}
     */
    public AxisDescription getYAxisDescription()
    {
        return this.yAxisDescription;
    }
}
