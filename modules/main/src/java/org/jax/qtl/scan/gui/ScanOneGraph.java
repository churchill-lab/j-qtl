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

package org.jax.qtl.scan.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;

import org.jax.analyticgraph.framework.AbstractGraph2DWithAxes;
import org.jax.analyticgraph.framework.GraphCoordinateConverter;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisDescription;
import org.jax.analyticgraph.graph.RegularIntervalAxisDescription;
import org.jax.analyticgraph.graph.AxisDescription.AxisType;
import org.jax.qtl.Constants;
import org.jax.qtl.action.OpenMgdUrlAction;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.gui.MarkerAxisDescription;
import org.jax.qtl.cross.gui.MarkerPositionManager;
import org.jax.qtl.cross.gui.ShowEffectPlotAction;
import org.jax.qtl.scan.ScanOneMarkerSignificanceValues;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.ScanOneThreshold;
import org.jax.qtl.scan.gui.ScanOneInterval.IntervalPoint;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * For graphing marker values
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneGraph extends AbstractGraph2DWithAxes
{
    private static final String DEFAULT_Y_AXIS_NAME_SUFFIX = "LOD Scores";
    
    private static final int DEFAULT_Y_AXIS_TICK_COUNT = 10;
    
    private static final int DEFAULT_Y_AXIS_SIGNIFICANT_DIGITS = 2;
    
    private static final String DEFAULT_X_AXIS_NAME = "Genetic Markers";
    
    private static final double[] DEFAULT_ALPHA_THRESHOLDS = {
        0.05,
        0.10,
        0.63};
    
    /**
     * our mouse motion listener
     */
    private MouseMotionListener containerComponentMotionListener =
        new MouseMotionListener()
        {
            public void mouseDragged(MouseEvent event)
            {
                // no-op
            }
    
            public void mouseMoved(MouseEvent event)
            {
                ScanOneGraph.this.containerComponentMouseMoved(event);
            }
        };

    /**
     * our mouse listener
     */
    private MouseListener containerComponentMouseListener =
        new MouseListener()
        {
            public void mouseClicked(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScanOneGraph.this.showPopupMenu(event.getPoint());
                }
            }

            public void mousePressed(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScanOneGraph.this.showPopupMenu(event.getPoint());
                }
            }

            public void mouseReleased(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScanOneGraph.this.showPopupMenu(event.getPoint());
                }
            }

            public void mouseEntered(MouseEvent e)
            {
                // no-op
            }

            public void mouseExited(MouseEvent e)
            {
                ScanOneGraph.this.getContainerComponent().remove(
                        ScanOneGraph.this.toolTip);
            }
        };
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ScanOneGraph.class.getName());
    
    private final JToolTip toolTip;
    
    private volatile Point lastMousePosition = new Point();
    
    /**
     * for figuring our where the markers are positioned
     */
    private final MarkerPositionManager markerPositionManager;

    private volatile List<List<ScanOneMarkerSignificanceValues>> markerSignificanceValues;
    
    private volatile ScanOneMarkerSignificanceValues markerSignificanceValueToHighlight;
    
    private volatile ScanOneResult scanOneResult;

    private static final Stroke CHROMOSOME_SEPARATOR_STROKE = new BasicStroke(
            0.5F,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0F,
            new float[] {6.0F, 3.0F},
            0.0F);
    
    private static final Stroke ALPHA_THRESHOLD_STROKE = new BasicStroke(
            0.5F,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0F,
            new float[] {4.0F, 4.0F},
            0.0F);
    
    private static final Stroke INTERVAL_CENTER_STROKE = new BasicStroke(
            0.5F,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0F,
            new float[] {1.0F, 4.0F},
            0.0F);
    
    private static final Stroke INTERVAL_BOUNDARY_STROKE = new BasicStroke(
            0.5F,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER);
    
    // TODO when we go to 6.0 we can use type safe "SansSerif"
    private static final Font GRAPH_TEXT_FONT =
        new Font("SansSerif", Font.PLAIN, 10);

    /**
     * the cursor offset to use
     */
    // TODO ideally we'd be able to query this but it doesn't look like that's
    //      possible, so we should at least move this to a config file
    private static final int CURSOR_Y_OFFSET = 16;
    
    private final GeneticMap[] geneticMaps;
    
    private volatile MarkerAxisDescription xAxisDescription;
    
    private volatile RegularIntervalAxisDescription yAxisDescription;
    
    private volatile ScanOneThreshold[] thresholdsToRender;
    
    private final ScanOneIntervalCommandBuilder scanOneIntervalCommandBuilder;

    private final PropertyChangeListener scanOneIntervalPropertyListener =
        new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                ScanOneGraph.this.scanOneIntervalChanged();
            }
        };
    
    private String lodColumnName;
    
    private volatile boolean showThresholdLabel = true;
    
    /**
     * Constructor
     * @param scanOneResult
     *          the scanone result to show
     * @param markerPositionManager
     *          the marker position manager to use
     * @param geneticMaps
     *          the genetic maps to use
     */
    public ScanOneGraph(
            ScanOneResult scanOneResult,
            MarkerPositionManager markerPositionManager,
            GeneticMap[] geneticMaps)
    {
        super(new SimpleGraphCoordinateConverter(
                0, 0,
                1, 1));
        
        this.markerPositionManager = markerPositionManager;
        this.geneticMaps = geneticMaps;
        this.toolTip = new JToolTip();
        this.scanOneIntervalCommandBuilder =
            new ScanOneIntervalCommandBuilder();
        this.scanOneIntervalCommandBuilder.setScanOneResult(
                scanOneResult);
        this.scanOneIntervalCommandBuilder.addPropertyChangeListener(
                this.scanOneIntervalPropertyListener);
        this.lodColumnName = scanOneResult.getSignificanceValueColumnNames()[0];
        
        this.setScanOneResult(scanOneResult);
    }
    
    /**
     * Getter for determining whether or not to show the threshold labels
     * @return
     *          true iff we should show the label
     */
    public boolean getShowThresholdLabel()
    {
        return this.showThresholdLabel;
    }
    
    /**
     * Setter for determining whether or not to show the threshold label
     * @param showThresholdLabel
     *          if true we should show the label
     */
    public void setShowThresholdLabel(boolean showThresholdLabel)
    {
        this.showThresholdLabel = showThresholdLabel;
        this.getContainerComponent().repaint();
    }
    
    /**
     * respond to a change in the interval
     */
    private void scanOneIntervalChanged()
    {
        this.getContainerComponent().repaint();
    }

    /**
     * Show the popup menu at the given location
     * @param popupPoint
     *          the point that we're showing the popup menu at
     */
    private void showPopupMenu(Point popupPoint)
    {
        if(this.getGraphCoordinateConverter().isPixelPointInBounds(popupPoint))
        {
            ScanOneResult scanOneResult = this.scanOneResult;
            Cross parentCross = scanOneResult.getParentCross();
            String phenotypeName = scanOneResult.findScannedPhenotypeNameForScanColumn(
                    this.lodColumnName);
            ScanOneMarkerSignificanceValues closestMarkerValue =
                this.getClosestMarkerSignificanceValue(popupPoint);
            GeneticMarker closestTrueMarker =
                this.getClosestTrueMarker(popupPoint);
            
            if(parentCross != null && phenotypeName != null)
            {
                JPopupMenu popupMenu = new JPopupMenu();
                if(closestTrueMarker != null)
                {
                    popupMenu.add(new ShowEffectPlotAction(
                            parentCross,
                            phenotypeName,
                            closestTrueMarker));
                }
                
                if(closestMarkerValue != null)
                {
                    popupMenu.add(new OpenMgdUrlAction(closestMarkerValue.getMarker()));
                }
                
                if(closestTrueMarker != null || closestMarkerValue != null)
                {
                    popupMenu.show(
                            this.getContainerComponent(),
                            popupPoint.x,
                            popupPoint.y);
                }
            }
            else
            {
                LOG.warning(
                        "can't show effect plot since we dont have all " +
                        "of the data we need: parentCross=" + parentCross +
                        " phenotypeName=" + phenotypeName +
                        " closestMarker=" + closestMarkerValue);
            }
        }
    }

    /**
     * Get the true marker closest to the given point (pseudo markers
     * are not considered)
     * @param point
     *          the point (in pixel units)
     * @return
     *          the marker or null if we can't find the closest marker
     */
    private GeneticMarker getClosestTrueMarker(Point point)
    {
        double pointGraphX =
            this.getGraphCoordinateConverter().convertJava2DXCoordinateToGraphXCoordinate(
                    point.getX());
        
        GeneticMarker closestMarker = null;
        double closestDistance = Double.MAX_VALUE;
        for(GeneticMap currMap: this.geneticMaps)
        {
            for(GeneticMarker currMarker: currMap.getMarkerPositions())
            {
                Double currMarkerPosition =
                    this.markerPositionManager.getMarkerPositionInGraphUnits(
                            currMarker);
                
                if(currMarkerPosition != null)
                {
                    double currDistance = Math.abs(
                            pointGraphX - currMarkerPosition);
                    
                    if(closestMarker == null || currDistance < closestDistance)
                    {
                        closestDistance = currDistance;
                        closestMarker = currMarker;
                    }
                }
                else
                {
                    LOG.warning(
                            "couldn't find marker position for: " + currMarker);
                }
            }
        }
        
        return closestMarker;
    }

    /**
     * Respond to a mouse move event
     * @param event
     *          the event
     */
    private void containerComponentMouseMoved(MouseEvent event)
    {
        this.lastMousePosition = event.getPoint();
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        JComponent container = this.getContainerComponent();
        if(coordConverter.isPixelPointInBounds(event.getPoint()))
        {
            if(this.toolTip.getParent() == null)
            {
                container.add(this.toolTip);
            }
            
            ScanOneMarkerSignificanceValues closestMarker =
                this.getClosestMarkerSignificanceValue(
                        event.getPoint());
            
            if(this.markerSignificanceValueToHighlight != closestMarker)
            {
                this.markerSignificanceValueToHighlight = closestMarker;
                container.repaint();
            }
            
            this.updateToolTipLayout();
        }
        else
        {
            container.remove(this.toolTip);
            if(this.markerSignificanceValueToHighlight != null)
            {
                this.markerSignificanceValueToHighlight = null;
                this.getContainerComponent().repaint();
            }
        }
    }

    /**
     * Update the layout of the tooltip
     */
    private void updateToolTipLayout()
    {
        JComponent container = this.getContainerComponent();
        
        // if the tool tip goes off the right edge of the screen, move it to the
        // left side of the cursor
        if(this.lastMousePosition.x + this.toolTip.getPreferredSize().width >
           container.getWidth())
        {
            this.toolTip.setLocation(
                    this.lastMousePosition.x - this.toolTip.getPreferredSize().width,
                    this.lastMousePosition.y + CURSOR_Y_OFFSET);
        }
        else
        {
            this.toolTip.setLocation(
                    this.lastMousePosition.x,
                    this.lastMousePosition.y + CURSOR_Y_OFFSET);
        }
        this.toolTip.setSize(this.toolTip.getPreferredSize());
    }
    
    /**
     * Get the marker value that's closest to the given point
     * @param point
     *          the point
     * @return
     *          the marker significance values closest to the given point
     */
    private ScanOneMarkerSignificanceValues getClosestMarkerSignificanceValue(
            Point point)
    {
        double pointGraphX =
            this.getGraphCoordinateConverter().convertJava2DXCoordinateToGraphXCoordinate(
                    point.getX());
        
        ScanOneMarkerSignificanceValues closestMarker = null;
        double closestDistance = Double.MAX_VALUE;
        
        if(this.markerSignificanceValues != null)
        {
            for(List<ScanOneMarkerSignificanceValues> currSigValsList:
                this.markerSignificanceValues)
            {
                for(ScanOneMarkerSignificanceValues currSigVals: currSigValsList)
                {
                    GeneticMarker currMarker = currSigVals.getMarker();
                    double currMarkerGraphX =
                        this.markerPositionManager.getMarkerPositionInGraphUnits(
                                currMarker);
                    double currMarkerDistance =
                        Math.abs(pointGraphX - currMarkerGraphX);
                    
                    // are we closer than any other marker we've seen?
                    if(closestMarker == null || currMarkerDistance < closestDistance)
                    {
                        // do we care if the marker is true or imputed
                        closestMarker = currSigVals;
                        closestDistance = currMarkerDistance;
                    }
                }
            }
        }
        
        return closestMarker;
    }

    /**
     * Getter for the result that we're plotting
     * @return the scanOneResult
     */
    public ScanOneResult getScanOneResult()
    {
        return this.scanOneResult;
    }
    
    /**
     * Updater for the lod column
     * @param lodColumnName the LOD column name to set
     * @param lodColumnIndex the LOD column index to set
     */
    public void updateLodColumn(String lodColumnName, int lodColumnIndex)
    {
        this.lodColumnName = lodColumnName;
        this.scanOneIntervalCommandBuilder.setLodColumnIndex(lodColumnIndex);
        this.setScanOneResult(this.scanOneResult);
        this.getContainerComponent().repaint();
    }
    
    /**
     * Getter for the LOD column name
     * @return the LOD column name
     */
    public String getLodColumnName()
    {
        return this.lodColumnName;
    }
    
    /**
     * Setter for the result that we're plotting.
     * @param scanOneResult the scanOneResult to set
     */
    private void setScanOneResult(final ScanOneResult scanOneResult)
    {
        this.scanOneResult = scanOneResult;
        this.markerSignificanceValues =
            scanOneResult.getMarkerSignificanceValuesByChromosome(
                    this.lodColumnName);
        if(scanOneResult.getPermutationsWereCalculated())
        {
            this.thresholdsToRender = scanOneResult.calculateThresholds(
                    DEFAULT_ALPHA_THRESHOLDS,
                    this.lodColumnName);
        }
        else
        {
            this.thresholdsToRender = null;
        }
        
        this.updateGraphDimensions();
    }
    
    /**
     * Getter for the interval command builder.
     * @return
     *          the command builder
     */
    public ScanOneIntervalCommandBuilder getScanOneIntervalCommandBuilder()
    {
        return this.scanOneIntervalCommandBuilder;
    }
    
    /**
     * update the dimensions of this graph
     */
    private void updateGraphDimensions()
    {
        // The LOD scale should not be less than one
        double maxLodValue = 1.0;
        
        List<List<GeneticMarker>> allMarkers = new ArrayList<List<GeneticMarker>>(
                this.markerSignificanceValues.size());
        final List<GeneticMarker> infiniteMarkers = new ArrayList<GeneticMarker>();
        final List<GeneticMarker> nanMarkers = new ArrayList<GeneticMarker>();
        for(List<ScanOneMarkerSignificanceValues> currSigList:
            this.markerSignificanceValues)
        {
            List<GeneticMarker> currMarkerList = new ArrayList<GeneticMarker>(
                    currSigList.size());
            allMarkers.add(currMarkerList);
            for(ScanOneMarkerSignificanceValues currMarkerSigValues: currSigList)
            {
                currMarkerList.add(currMarkerSigValues.getMarker());
                double currLod =
                    currMarkerSigValues.getLodScore();
                if(Double.isInfinite(currLod))
                {
                    infiniteMarkers.add(currMarkerSigValues.getMarker());
                }
                else if(Double.isNaN(currLod))
                {
                    nanMarkers.add(currMarkerSigValues.getMarker());
                }
                else if(maxLodValue < currLod)
                {
                    maxLodValue = currLod;
                }
            }
        }
        
        if(!infiniteMarkers.isEmpty())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    MessageDialogUtilities.warn(
                            ScanOneGraph.this.getContainerComponent(),
                            "The following markers have infinite LOD scores: " +
                            SequenceUtilities.toString(infiniteMarkers, ", "),
                            "Found Infinite LOD Scores");
                }
            });
        }
        
        if(!nanMarkers.isEmpty())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                /**
                 * {@inheritDoc}
                 */
                public void run()
                {
                    MessageDialogUtilities.warn(
                            ScanOneGraph.this.getContainerComponent(),
                            "The following markers have \"Not a Number\" " +
                            "LOD scores: " +
                            SequenceUtilities.toString(nanMarkers, ", "),
                            "Found Bad LOD Scores");
                }
            });
        }
        
        this.getGraphCoordinateConverter().updateGraphDimensions(
                0, 0, 1, maxLodValue);
        
        // update the axes
        this.xAxisDescription = new MarkerAxisDescription(
                this.getGraphCoordinateConverter(),
                AxisType.X_AXIS,
                DEFAULT_X_AXIS_NAME,
                this.geneticMaps,
                allMarkers);
        String lodColumnName = this.getLodColumnName();
        String yAxisName =
            lodColumnName == null || lodColumnName.equalsIgnoreCase("lod") ?
                    DEFAULT_Y_AXIS_NAME_SUFFIX :
                    lodColumnName + " " + DEFAULT_Y_AXIS_NAME_SUFFIX;
        this.yAxisDescription = new RegularIntervalAxisDescription(
                this.getGraphCoordinateConverter(),
                AxisType.Y_AXIS,
                yAxisName,
                DEFAULT_Y_AXIS_TICK_COUNT,
                DEFAULT_Y_AXIS_SIGNIFICANT_DIGITS,
                true);
    }
    
    /**
     * {@inheritDoc}
     */
    public void renderGraph(Graphics2D graphics2D)
    {
        List<List<ScanOneMarkerSignificanceValues>> markerSignificanceValues =
            this.markerSignificanceValues;
        if(markerSignificanceValues != null)
        {
            for(int i = 0; i < markerSignificanceValues.size(); i++)
            {
                List<ScanOneMarkerSignificanceValues> currSigList =
                    markerSignificanceValues.get(i);
                
                // draw a separator line if this isn't the 1st chromosome
                if(i != 0)
                {
                    String currChromosomeName =
                        currSigList.get(0).getMarker().getChromosomeName();
                    if(currChromosomeName == null)
                    {
                        LOG.warning(
                                "cannot draw a separator since i don't know " +
                                "what the chromosome's name is");
                    }
                    else
                    {
                        double separatorPosition =
                            this.markerPositionManager.getChromosomeStartingPositionInGraphUnits(
                                    currChromosomeName);
                        this.drawChromosomeSeparator(
                                graphics2D,
                                separatorPosition);
                    }
                }
                
                // iterate through the current list which represents all of
                // the markers in a chromosome
                ScanOneMarkerSignificanceValues prevMarkerSigValues = null;
                for(ScanOneMarkerSignificanceValues currMarkerSigValues: currSigList)
                {
                    if(prevMarkerSigValues != null)
                    {
                            this.connectMarkerValuesPairs(
                                    graphics2D,
                                    prevMarkerSigValues.getMarker(),
                                    prevMarkerSigValues.getLodScore(),
                                    currMarkerSigValues.getMarker(),
                                    currMarkerSigValues.getLodScore());
                    }
                    
                    prevMarkerSigValues = currMarkerSigValues;
                }
            }
            
            ScanOneMarkerSignificanceValues markerSignificanceValueToHighlight =
                this.markerSignificanceValueToHighlight;
            if(markerSignificanceValueToHighlight == null)
            {
                this.getContainerComponent().remove(this.toolTip);
            }
            else
            {
                GeneticMarker markerToHighlight =
                    markerSignificanceValueToHighlight.getMarker();
                this.drawMarkerHighlight(
                        graphics2D,
                        markerToHighlight);
                
                StringBuffer tipText = new StringBuffer(
                    "<html>Marker Name: " + markerToHighlight.getMarkerName() + ":" +
                    "<p>Chromosome: " + markerToHighlight.getChromosomeName() +
                    "<p>Location (cM): " + markerToHighlight.getMarkerPositionCentimorgans() +
                    "<p>LOD Score: " + markerSignificanceValueToHighlight.getLodScore());
                tipText.append("</html>");
                
                this.toolTip.setTipText(tipText.toString());
                this.updateToolTipLayout();
            }
            
            this.renderIntervals(
                    graphics2D,
                    this.scanOneIntervalCommandBuilder.getScanOneIntervals(
                            RInterfaceFactory.getRInterfaceInstance()));
            this.drawThresholds(graphics2D);
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine("not doing anything for render since data is null");
            }
        }
    }
    
    /**
     * render the intervals
     * @param intervals
     *          the intervals
     */
    private void renderIntervals(
            Graphics2D graphics2D,
            List<ScanOneInterval> intervals)
    {
        if(intervals != null)
        {
            for(ScanOneInterval interval: intervals)
            {
                String chromosomeName = interval.getChromosomeName();
                this.renderIntervalLine(
                        graphics2D,
                        chromosomeName,
                        interval.getIntervalShape().getLeftFlankPoint(),
                        INTERVAL_BOUNDARY_STROKE);
                this.renderIntervalLine(
                        graphics2D,
                        chromosomeName,
                        interval.getIntervalShape().getRightFlankPoint(),
                        INTERVAL_BOUNDARY_STROKE);
                this.renderIntervalLine(
                        graphics2D,
                        chromosomeName,
                        interval.getIntervalShape().getPeakPoint(),
                        INTERVAL_CENTER_STROKE);
            }
        }
    }
    
    private void renderIntervalLine(
            Graphics2D graphics2D,
            String chromosomeName,
            IntervalPoint intervalPoint,
            Stroke stroke)
    {
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        double yBottomPixels =
            coordConverter.convertGraphYCoordinateToJava2DYCoordinate(0.0);
        double yTopPixels =
            coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                    intervalPoint.getLodScore());
        double xGraphUnits = this.markerPositionManager.getPositionInGraphUnits(
                chromosomeName,
                intervalPoint.getPositionInCentimorgans());
        double xPixels =
            coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                    xGraphUnits);
        
        Line2D intervalLine = new Line2D.Double(
                xPixels, yBottomPixels,
                xPixels, yTopPixels);
        graphics2D.draw(stroke.createStrokedShape(intervalLine));
    }
    
    /**
     * Draw all of the thresholds
     * @param graphics2D
     *          the graphics context to use
     */
    private void drawThresholds(Graphics2D graphics2D)
    {
        ScanOneThreshold[] thresholds = this.thresholdsToRender;
        if(thresholds != null)
        {
            for(ScanOneThreshold threshold: thresholds)
            {
                this.drawThreshold(graphics2D, threshold);
            }
        }
    }

    /**
     * Draw the given threshold value
     * @param graphics2D
     *          the graphics context to write to
     * @param threshold
     *          the threshold to render
     */
    private void drawThreshold(
            Graphics2D graphics2D,
            ScanOneThreshold threshold)
    {
        GraphCoordinateConverter coordConverter =
            this.getGraphCoordinateConverter();
        double xAllChromosStartInPixels =
            coordConverter.getAbsoluteXOffsetInPixels();
        double xAllChromosStopInPixels =
            xAllChromosStartInPixels +
            coordConverter.getAbsoluteWidthInPixels();
        
        if(threshold.getXChromosomePValuesAreSeparate())
        {
            double xChromoLodYInPixels = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                    threshold.getXChromosomeLodValue());
            double autoLodYInPixels = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                    threshold.getAutosomeLodValue());
            
            List<ScanOneMarkerSignificanceValues> xChromoSigValues =
                this.markerSignificanceValues.get(
                        this.markerSignificanceValues.size() - 1);
            String xChromosomeName =
                xChromoSigValues.get(0).getMarker().getChromosomeName();
            double xChromoXStartInGraphUnits =
                this.markerPositionManager.getChromosomeStartingPositionInGraphUnits(
                    xChromosomeName);
            double xChromoXStartInPixels = 
                coordConverter.convertGraphXCoordinateToJava2DXCoordinate(
                        xChromoXStartInGraphUnits);
            
            Line2D xChromoAlphaThresholdLine = new Line2D.Double(
                    xChromoXStartInPixels, xChromoLodYInPixels,
                    xAllChromosStopInPixels, xChromoLodYInPixels);
            Line2D autoAlphaThresholdLine = new Line2D.Double(
                    xAllChromosStartInPixels, autoLodYInPixels,
                    xChromoXStartInPixels, autoLodYInPixels);
            
            graphics2D.draw(ScanOneGraph.ALPHA_THRESHOLD_STROKE.createStrokedShape(
                    xChromoAlphaThresholdLine));
            graphics2D.draw(ScanOneGraph.ALPHA_THRESHOLD_STROKE.createStrokedShape(
                    autoAlphaThresholdLine));
            
            if(this.getShowThresholdLabel())
            {
                String xChromoLabelString =
                    "X Chromosome \u03B1 Threshold: " +
                    Constants.THREE_DIGIT_FORMATTER.format(threshold.getAlphaThreshold());
                String autoChromoLabelString =
                    "Autosome \u03B1 Threshold: " +
                    Constants.THREE_DIGIT_FORMATTER.format(threshold.getAlphaThreshold());
                
                Shape xChromoLabelShape = this.createGraphTextShape(
                        graphics2D,
                        xChromoLabelString);
                Rectangle2D xChromoLabelBounds = xChromoLabelShape.getBounds2D();
                AffineTransform xChromoAffTrans = new AffineTransform();
                xChromoAffTrans.translate(
                        xAllChromosStopInPixels - xChromoLabelBounds.getWidth() - 1,
                        xChromoLodYInPixels - 1.0);
                xChromoLabelShape = xChromoAffTrans.createTransformedShape(
                        xChromoLabelShape);
                graphics2D.fill(xChromoLabelShape);
                
                Shape autoLabelShape = this.createGraphTextShape(
                        graphics2D,
                        autoChromoLabelString);
                Rectangle2D autoLabelBounds = autoLabelShape.getBounds2D();
                AffineTransform autoAffTrans = new AffineTransform();
                autoAffTrans.translate(
                        (xChromoXStartInPixels / 2.0) - (autoLabelBounds.getWidth() / 2.0),
                        autoLodYInPixels - 1.0);
                autoLabelShape = autoAffTrans.createTransformedShape(autoLabelShape);
                graphics2D.fill(autoLabelShape);
            }
        }
        else
        {
            double lodYInPixels = coordConverter.convertGraphYCoordinateToJava2DYCoordinate(
                    threshold.getLodValue());
            
            Line2D alphaThresholdLine = new Line2D.Double(
                    xAllChromosStartInPixels, lodYInPixels,
                    xAllChromosStopInPixels, lodYInPixels);
            graphics2D.draw(ScanOneGraph.ALPHA_THRESHOLD_STROKE.createStrokedShape(
                    alphaThresholdLine));
            
            if(this.getShowThresholdLabel())
            {
                String labelString =
                    "\u03B1 Threshold : " +
                    Constants.THREE_DIGIT_FORMATTER.format(threshold.getAlphaThreshold());
                
                Shape labelShape = this.createGraphTextShape(
                        graphics2D,
                        labelString);
                Rectangle2D labelBounds = labelShape.getBounds2D();
                AffineTransform at = new AffineTransform();
                at.translate(
                        ((xAllChromosStopInPixels + xAllChromosStartInPixels) / 2.0) -
                        (labelBounds.getWidth() / 2.0),
                        lodYInPixels - 1.0);
                labelShape = at.createTransformedShape(labelShape);
                graphics2D.fill(labelShape);
            }
        }
    }

    /**
     * Turn the given string into a shape object
     * @param graphics
     *          the graphics context that the shape will live in
     * @param text
     *          the text to create a shape for
     * @return
     *          the shape of the text given the graphics context
     */
    private Shape createGraphTextShape(
            Graphics2D graphics,
            String text)
    {
        FontRenderContext frc = graphics.getFontRenderContext();
        GlyphVector labelGlyphVector = GRAPH_TEXT_FONT.createGlyphVector(
                frc,
                text);
        Shape textShape = labelGlyphVector.getOutline();
        
        return textShape;
    }
    
    /**
     * Draw a line highlighting the given marker
     * @param graphics2D
     *          the graphics context
     * @param markerToHighlight
     *          the marker to highlight
     */
    private void drawMarkerHighlight(
            Graphics2D graphics2D,
            GeneticMarker markerToHighlight)
    {
        double markerPositionX = this.markerPositionManager.getMarkerPositionInGraphUnits(
                markerToHighlight);
        double markerPositionXInPixels = this.getGraphCoordinateConverter().convertGraphXCoordinateToJava2DXCoordinate(
                markerPositionX);
        
        double highlightYStartInPixels =
            this.getGraphCoordinateConverter().getAbsoluteYOffsetInPixels();
        double highlightYStopInPixels =
            this.getGraphCoordinateConverter().getAbsoluteHeightInPixels() +
            highlightYStartInPixels;
        
        Line2D highlightLine = new Line2D.Double(
                markerPositionXInPixels, highlightYStartInPixels,
                markerPositionXInPixels, highlightYStopInPixels);
        
        // TODO color should not be hard coded
        Color pushColor = graphics2D.getColor();
        graphics2D.setColor(Color.RED);
        graphics2D.draw(highlightLine);
        graphics2D.setColor(pushColor);
    }
    
    /**
     * Render a chromosome separator
     * @param graphics2D
     *          the graphics context to render the separator to
     * @param separatorPosition
     *          where we should put the separator
     */
    private void drawChromosomeSeparator(
            Graphics2D graphics2D,
            double separatorPosition)
    {
        double separatorXInPixels =
            this.getGraphCoordinateConverter().convertGraphXCoordinateToJava2DXCoordinate(
                    separatorPosition);
        double separatorYStartInPixels =
            this.getGraphCoordinateConverter().getAbsoluteYOffsetInPixels();
        double separatorYStopInPixels =
            this.getGraphCoordinateConverter().getAbsoluteHeightInPixels() +
            separatorYStartInPixels;
        
        Line2D separatorLine = new Line2D.Double(
                separatorXInPixels, separatorYStartInPixels,
                separatorXInPixels, separatorYStopInPixels);
        
        // TODO color should not be hard coded
        Color pushColor = graphics2D.getColor();
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.draw(ScanOneGraph.CHROMOSOME_SEPARATOR_STROKE.createStrokedShape(
                separatorLine));
        graphics2D.setColor(pushColor);
    }

    /**
     * Draw a line connecting the given marker values
     * @param graphics2D
     *          the graphics context to render to
     * @param marker1
     *          the 1st marker
     * @param marker1Value
     *          the 1st marker's value
     * @param marker2
     *          the 2nd marker
     * @param marker2Value
     *          the 2nd marker's value
     */
    private void connectMarkerValuesPairs(
            Graphics2D graphics2D,
            GeneticMarker marker1,
            double marker1Value,
            GeneticMarker marker2,
            double marker2Value)
    {
        // figure our the location of each marker on the x axis
        double marker1PosX =
            this.markerPositionManager.getMarkerPositionInGraphUnits(
                    marker1);
        double marker2PosX =
            this.markerPositionManager.getMarkerPositionInGraphUnits(
                    marker2);
        
        // do a conversion from graph space to pixel space
        double pixleX1 =
            this.getGraphCoordinateConverter().convertGraphXCoordinateToJava2DXCoordinate(
                    marker1PosX);
        double pixleY1 =
            this.getGraphCoordinateConverter().convertGraphYCoordinateToJava2DYCoordinate(
                    marker1Value);
        double pixleX2 =
            this.getGraphCoordinateConverter().convertGraphXCoordinateToJava2DXCoordinate(
                    marker2PosX);
        double pixleY2 =
            this.getGraphCoordinateConverter().convertGraphYCoordinateToJava2DYCoordinate(
                    marker2Value);
        
        // render the connection
        graphics2D.draw(new Line2D.Double(
                pixleX1,
                pixleY1,
                pixleX2,
                pixleY2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setContainerComponent(
            JComponent containerComponent)
    {
        // stop listening to old component
        JComponent currContainerComponent = this.getContainerComponent();
        if(currContainerComponent != null)
        {
            currContainerComponent.removeMouseListener(
                    this.containerComponentMouseListener);
            currContainerComponent.removeMouseMotionListener(
                    this.containerComponentMotionListener);
            currContainerComponent.remove(this.toolTip);
        }
        
        super.setContainerComponent(containerComponent);
        
        // start listening to new component
        if(containerComponent != null)
        {
            containerComponent.addMouseListener(
                    this.containerComponentMouseListener);
            containerComponent.addMouseMotionListener(
                    this.containerComponentMotionListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AxisDescription getXAxisDescription()
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
    
    /**
     * Setter for the thresholds that this graph should render
     * @param thresholdsToRender the thresholdsToRender to set
     */
    public void setThresholdsToRender(ScanOneThreshold[] thresholdsToRender)
    {
        this.thresholdsToRender = thresholdsToRender;
        this.getContainerComponent().repaint();
    }
    
    /**
     * Getter for the thresholds to render
     * @return the thresholdsToRender
     */
    public ScanOneThreshold[] getThresholdsToRender()
    {
        return this.thresholdsToRender;
    }
}
