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
package org.jax.qtl.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.ToolTipManager;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.util.Tools;

/**
 * <p>Title: QTL data analysis</p>
 *
 * <p>Description: </p>
 *
 * <p>Company: The Jackson Laboratory</p>
 *
 * @author Lei Wu
 * @version 1.0
 */
@SuppressWarnings("all")
public class RfPlot extends OneDimensionPlot {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -3979909287609271087L;
    private Cross cross;
    private double[][] rfLod;
    private Color[] colorMap;
    private double minlodLower = 0, maxlodLower = MAX_RF_LOD, minlodUpper = 0, maxlodUpper = MAX_RF_LOD;
    private int leftConerX, leftConerY, width, height;
    private CrossChromosome[] selectedChromosomes;

    private RfGrid[][] dataPoints;
    private List<GeneticMarker> allSelectedMarkers;

    public RfPlot(Cross cross, double[][] allRFLod, int[] selectedChromosomeIndexes) {
        super();

        this.drawOutlineBox = false;
        
        List<CrossChromosome> allChromosomes = cross.getGenotypeData();
        int numSelectedChromosomes = selectedChromosomeIndexes.length;
        this.allSelectedMarkers = new ArrayList<GeneticMarker>();
        this.selectedChromosomes = new CrossChromosome[numSelectedChromosomes];
        
        int allChromosomesCount = allChromosomes.size();
        List<List<GeneticMarker>> allChromosomeMarkers =
            new ArrayList<List<GeneticMarker>>();
        int[] cumulativeChromosomeMarkerCounts = new int[allChromosomesCount];
        for(int i = 0; i < allChromosomesCount; i++)
        {
            List<GeneticMarker> markerPositions =
                allChromosomes.get(i).getAnyGeneticMap().getMarkerPositions();
            allChromosomeMarkers.add(
                    markerPositions);
            
            cumulativeChromosomeMarkerCounts[i] =
                markerPositions.size();
            if(i > 0)
            {
                cumulativeChromosomeMarkerCounts[i] +=
                    cumulativeChromosomeMarkerCounts[i - 1];
            }
        }
        
        for(int selectionIndex = 0;
            selectionIndex < numSelectedChromosomes;
            selectionIndex++)
        {
            CrossChromosome currSelectedChromosome =
                allChromosomes.get(selectedChromosomeIndexes[selectionIndex]);
            this.selectedChromosomes[selectionIndex] = currSelectedChromosome;
            this.allSelectedMarkers.addAll(
                    allChromosomeMarkers.get(
                            selectedChromosomeIndexes[selectionIndex]));
        }
        
        int selectedMarkerCount = this.allSelectedMarkers.size();
        this.rfLod = new double[selectedMarkerCount][selectedMarkerCount];
        {
            int outerRfDataIndex = 0;
            for(int outerChromoIndex = 0; outerChromoIndex < numSelectedChromosomes; outerChromoIndex++)
            {
                int outerStartingMarkerIndexInclusive =
                    selectedChromosomeIndexes[outerChromoIndex] == 0 ?
                    0 :
                    cumulativeChromosomeMarkerCounts[selectedChromosomeIndexes[outerChromoIndex] - 1];
                int outerEndingMarkerIndexExclusive =
                    cumulativeChromosomeMarkerCounts[selectedChromosomeIndexes[outerChromoIndex]];
                
                for(int outerMarkerIndex = outerStartingMarkerIndexInclusive;
                    outerMarkerIndex < outerEndingMarkerIndexExclusive;
                    outerMarkerIndex++)
                {
                    int innerRfDataIndex = 0;
                    for(int innerChromoIndex = 0; innerChromoIndex < numSelectedChromosomes; innerChromoIndex++)
                    {
                        int innerStartingMarkerIndexInclusive =
                            selectedChromosomeIndexes[innerChromoIndex] == 0 ?
                            0 :
                            cumulativeChromosomeMarkerCounts[selectedChromosomeIndexes[innerChromoIndex] - 1];
                        int innerEndingMarkerIndexExclusive =
                            cumulativeChromosomeMarkerCounts[selectedChromosomeIndexes[innerChromoIndex]];
                        
                        for(int innerMarkerIndex = innerStartingMarkerIndexInclusive;
                            innerMarkerIndex < innerEndingMarkerIndexExclusive;
                            innerMarkerIndex++)
                        {
                            this.rfLod[outerRfDataIndex][innerRfDataIndex] =
                                allRFLod[outerMarkerIndex][innerMarkerIndex];
                            
                            innerRfDataIndex++;
                        }
                    }
                    
                    outerRfDataIndex++;
                }
            }
        }
        
        // TODO add back the interaction
//        int numChr = chrs.length;
//        for (int i=0; i<numChr; i++) {
//            int numMarkers = chrs[i].getNumMarkers();
//            for (int j=0; j<numMarkers; j++) {
//                GeneticMap map = ((MarkerData)chrs[i].getMarkers().elementAt(j)).getMap();
//                map.addGenoDataSelectionChangeListener(this);
//                markers.add(map);
//            }
//        }

        // make the dictionary for showing tip
        this.dataPoints = new RfGrid[selectedMarkerCount][selectedMarkerCount];
        for (int row=0; row<selectedMarkerCount; row++) {
            for (int col=0; col<selectedMarkerCount; col++) {
                String markerName1 =
                    this.allSelectedMarkers.get(row).getMarkerName();
                String markerName2 =
                    this.allSelectedMarkers.get(col).getMarkerName();
                // set the correct value for displayed in tip
                double rf = 0, lod = 0;
                if (row == col) rf = lod = this.rfLod[row][col];
                else {
                    if (row > col) { // lower in data, upper in plot
                        rf = this.rfLod[row][col];
                        lod = this.rfLod[col][row];
                    }
                    else { // upper in data, lower in plot
                        rf = this.rfLod[col][row];
                        lod = this.rfLod[row][col];
                    }
                }
                if (rf > 0.5) rf = 0.5;
                this.dataPoints[row][col] = new RfGrid(markerName1, markerName2, rf, lod);
            }
        }
        // make color map
        this.colorMap = Tools.makeColormap(NUM_COLORS);
        // set selection box "color"
        this.selectionBoxColor = new Color(0,0,0,30);
        // set title and labels
        setTitle("Pairwise recombination fractions and LOD scores ");

        int preferedWidth = 500, preferedHeight = 500;
        setPreferredSize(new Dimension(preferedWidth, preferedHeight));
    }

    /**
     * plot the rf plot
     */
    void plot() {
        this.allDots = new HashSet<Dot>();
        this.leftConerX = this.inset.left;
        this.leftConerY = this.inset.top;

        // total number of rows, columns (rows=columns=numMarkers)
        int numMarkers = this.allSelectedMarkers.size();

        int gridWidth = this.plotWidth/numMarkers;
        int gridHeight = this.plotHeight/numMarkers;

        this.width = gridWidth * numMarkers;
        this.height = gridHeight * numMarkers;

        // draw grids
        for (int row=0; row<numMarkers; row++) {
            for (int col=0; col<numMarkers; col++) {
                int x = gridWidth * col + this.leftConerX;
                int y = this.leftConerY + this.height - gridHeight * (row+1);
                Shape currentGrid = new Rectangle2D.Double(x,y,this.width/numMarkers,this.height/numMarkers);
                // set the correct value for displayed in tip
                Dot dot = new Dot(
                        this.allSelectedMarkers.get(row),
                        this.allSelectedMarkers.get(col),
                        currentGrid);
                this.allDots.add(dot);

                // set color for current grid
                boolean isLower = true;
                if (row==col) // on diagnal
                    this.big.setColor(Color.red);
                else {
                    double plotValue = this.rfLod[row][col];
                    if (row > col) { // (rf) lower in data, upper in plot
                        isLower = false;
                        plotValue = this.rfLod[row][col];
                        plotValue = (-4) * (Math.log(plotValue)/Math.log(2) + 1);
                        if (plotValue < 0) plotValue = 0;
                    }
                    if (plotValue > MAX_RF_LOD) // (lod) upper in data, lower in plot
                        plotValue = MAX_RF_LOD;

                    this.big.setColor(getColor(plotValue, isLower));
                }
                this.big.fill(currentGrid);

                // highlight the selected grid
                // TODO add back the interaction
//                if (((GeneticMap)markers.elementAt(row)).isSelected() && ((GeneticMap)markers.elementAt(col)).isSelected() && (row!=col)) {
//                    big.setColor(Color.white);
//                    big.setStroke(new BasicStroke(1.8f));
//                    big.draw(currentGrid);
//                }
            }
        }

        // draw chromosome dividers, (numChr - 1) crossed lines
        int numChr = this.selectedChromosomes.length;
        int cumulatedNumMarkers = 0;
        int lastx = this.leftConerX, lasty = this.leftConerY + this.height;
        for (int i=0; i<numChr; i++) {
            int numMarkersOnOneChr =
                this.selectedChromosomes[i].getAnyGeneticMap().getMarkerPositions().size();
            cumulatedNumMarkers += numMarkersOnOneChr;
            int x = cumulatedNumMarkers * gridWidth + this.leftConerX;
            int y = this.leftConerY + this.height - cumulatedNumMarkers * gridHeight;
            int tickx = lastx + (numMarkersOnOneChr * gridWidth)/2;
            int ticky = lasty - (numMarkersOnOneChr * gridHeight)/2;
            lastx = x;
            lasty = y;
            this.big.setColor(Color.white);
            this.big.setStroke(this.normalLinetype);
            // draw vertical lines
            this.big.drawLine(x, this.leftConerY, x, this.leftConerY + this.height);
            // draw horizontal lines
            this.big.drawLine(this.leftConerX, y, this.leftConerX + this.width, y);

            // draw ticks
            this.big.setColor(this.normalColor);
            this.big.drawLine(tickx, this.leftConerY + this.height, tickx, this.leftConerY + this.height + this.tickHeight); // x axis ticks
            this.big.drawLine(this.leftConerX - this.tickHeight, ticky, this.leftConerX, ticky); // y axis ticks

            // label width and height
            Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(
                    this.selectedChromosomes[i].getChromosomeName(),
                    this.context);
            double labelWidth = labelBounds.getWidth();
            double labelHeight = labelBounds.getHeight();

            // find the right place to start to draw labels on y axis
            float yAxisLabelStartX = this.leftConerX - (float) labelWidth - this.tickHeight - this.labelToTick;
            float yAxisLabelStartY = (float) (ticky + labelHeight / 2 - 1); // -1 is only for looks prettier
            // draw tick label on y axis
            this.big.setFont(this.tickLabelFont);
            this.big.drawString(
                    this.selectedChromosomes[i].getChromosomeName(),
                    yAxisLabelStartX,
                    yAxisLabelStartY); // vertical

            // find the right place to start to draw labels on x axis
            float xAxisLabelStartX = tickx - (float)labelWidth/2 + 0.5f; // 0.5f is only for looks prettier
            float xAxisLabelStartY = this.leftConerY + this.height + this.tickHeight + (float)labelHeight + this.labelToTick;
            // draw tick label on x axis
            this.big.drawString(
                    this.selectedChromosomes[i].getChromosomeName(),
                    xAxisLabelStartX,
                    xAxisLabelStartY); // horizontal
        }

        // draw outlines around the plot
        this.big.drawRect(this.leftConerX, this.leftConerY, this.width, this.height);
    }

    public void setSize(Dimension size) {
//        this.setSize(new Dimension(500, 500));
    }

    // for MouseMotionListener
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if ((x > this.leftConerX && x < this.leftConerX + this.width) && (y > this.leftConerY && y < this.leftConerY + this.height)) {
            // find the current grid
            int numMarkers = this.allSelectedMarkers.size();
            int deltaX = x - this.leftConerX; // distance to left of plot
            int deltaY = this.leftConerY + this.height - y; // distance to top of plot
            int r = numMarkers * deltaX / this.width; // row index
            int c = numMarkers * deltaY / this.height; // col index
            RfGrid currentGrid = this.dataPoints[r][c];

            // make tip
            String tip = "<html>";
            if (currentGrid.markerName1.equalsIgnoreCase(currentGrid.markerName2)) // on diagnal
                tip += currentGrid.markerName1 + "<p>typed meioses: " + currentGrid.lod;
            else
                tip += currentGrid.markerName1 + ":" + currentGrid.markerName2 +
                    "<p>rf: " + FOUR_DIGIT_FORMATTER.format(currentGrid.rf) +
                    "<p>LOD: " + FOUR_DIGIT_FORMATTER.format(currentGrid.lod);

            // show tip within plot area
            ToolTipManager.sharedInstance().setEnabled(true);
            this.setToolTipText(tip);
            setCursor(this.plotCursor);
            return;
        }
        else {
            ToolTipManager.sharedInstance().setEnabled(false);
            setCursor(this.normalCursor);
        }
    }

    private Color getColor(double value, boolean isLower) {
        int index = 0;
        if (isLower)
            index = (int) ( (value - this.minlodLower) / ( (this.maxlodLower - this.minlodLower) / NUM_COLORS));
        else
            index = (int) ( (value - this.minlodUpper) / ( (this.maxlodUpper - this.minlodUpper) / NUM_COLORS));
        if (index < 0) index = 0;
        if (index > 255) index = 255;
        return this.colorMap[index];
    }

    class RfGrid {
        double rf, lod;
        String markerName1, markerName2;
        RfGrid(String mname1, String mname2, double rf, double lod) {
            this.markerName1 = mname1;
            this.markerName2 = mname2;
            this.rf = rf;
            this.lod = lod;
        }
    }
}
