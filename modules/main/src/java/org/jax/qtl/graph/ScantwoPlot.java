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
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.gui.ShowEffectPlotAction;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.qtl.scan.ScanTwoResult.MarkerIndexPair;
import org.jax.qtl.scan.ScanTwoResult.ScanTwoGeneticMarker;
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
public class ScantwoPlot extends OneDimensionPlot
{
    /**
     * 
     */
    private static final long serialVersionUID = -4492867589691649975L;

    private static final Logger LOG = Logger.getLogger(
            ScantwoPlot.class.getName());
    
    private final ScanTwoResult scantwo;
    private final GeneticMap[] geneticMaps;
    
    private int selectedPhenoIndex; // selected pheno index
    private double minlodLower, maxlodLower, minlodUpper, maxlodUpper;
    private final String[] chromosomeNames; // selected chromosome names for this plot
    // graph parameters
    private Color[] colorMap;
    private double[][] lods;
    private int[] numMarkersOnEachChromosome;
    private List<List<ScanTwoGeneticMarker>> markersPerChromosome;
    private List<ScanTwoGeneticMarker> markers;
    private boolean plotColorScale;
    private int colorBarWidth = 10, colorBarSpace = 80;
    
    /**
     * our mouse listener
     */
    private final MouseListener containerComponentMouseListener =
        new MouseListener()
        {
            public void mouseClicked(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScantwoPlot.this.showPopupMenu(event.getPoint());
                }
            }

            public void mousePressed(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScantwoPlot.this.showPopupMenu(event.getPoint());
                }
            }

            public void mouseReleased(MouseEvent event)
            {
                if(event.isPopupTrigger())
                {
                    ScantwoPlot.this.showPopupMenu(event.getPoint());
                }
            }

            public void mouseEntered(MouseEvent e)
            {
                // no-op
            }

            public void mouseExited(MouseEvent e)
            {
                // no-op
            }
        };

    public ScantwoPlot(
            ScanTwoResult scantwo,
            GeneticMap[] geneticMaps,
            int selectedPhenoIndex,
            int upperLodIndex,
            int lowerLodIndex,
            int[] selectedChromosomes,
            boolean plotColorScale)
    {
        super();
        
        this.addMouseListener(this.containerComponentMouseListener);
        
        // select all chromosomes (temp)
        // TODO this is pretty questionable
        int numChromosome = selectedChromosomes.length;
        selectedChromosomes = new int[numChromosome];
        for (int i=0; i<numChromosome; i++)
            selectedChromosomes[i] = i;
        // end


        this.drawOutlineBox = false;
        this.scantwo = scantwo;
        this.geneticMaps = geneticMaps;
        this.selectedPhenoIndex = selectedPhenoIndex;
        this.plotColorScale = plotColorScale;

        // use selectedChromosomes to get chromosomeNames, numMarkersOnEachChromosome
        int numSelectedChr = selectedChromosomes.length;
        this.numMarkersOnEachChromosome = new int[numSelectedChr];
        this.chromosomeNames = new String[numSelectedChr];
        String[] allChromosomeNames = scantwo.getScannedChromosomeNames();
        int totalMarkers = 0;
        this.markersPerChromosome = scantwo.getGeneticMarkersPerChromosome();
        for (int i=0; i<numSelectedChr; i++) {
            this.numMarkersOnEachChromosome[i] = this.markersPerChromosome.get(selectedChromosomes[i]).size();
                
            totalMarkers += this.numMarkersOnEachChromosome[i];
            this.chromosomeNames[i] = allChromosomeNames[selectedChromosomes[i]];
        }

        // get selectedMarkers
        this.markers = new ArrayList<ScanTwoGeneticMarker>(totalMarkers);
        int markerIndex = 0;
        for (int i=0; i<numSelectedChr; i++) {
            this.markers.addAll(this.markersPerChromosome.get(selectedChromosomes[i]));
        }

        int preferedWidth = 500, preferedHeight = 500;
        if (plotColorScale) {
            preferedWidth += this.colorBarSpace;
        }
        // set the initial size
        setPreferredSize(new Dimension(preferedWidth, preferedHeight));

        // calculate the lods used in this plot based on user selection
        getLodsInThisPlot(totalMarkers, upperLodIndex, lowerLodIndex);


        // get min and max lod of the given dataset
        setMinMaxValue();

        this.colorMap = Tools.makeColormap(NUM_COLORS);

        // TODO add interaction back in
        // add GenoDataSelectionChangeListener to all markers in this scantwo result
//        for (int i=0; i<totalMarkers; i++) {
//            markers[i].addGenoDataSelectionChangeListener(this);
//        }

        // set selection box "color"
        this.selectionBoxColor = new Color(0,0,0,30);
        // set title and labels
        setTitle("Two QTL Genome Scan - " +
                 scantwo.toString() + " - " +
                 scantwo.getScannedPhenotypeNames()[selectedPhenoIndex] +
                 " (" + LOD_TYPE[upperLodIndex] + " / " + LOD_TYPE[lowerLodIndex] + ")");
        setXlabel("Chromosome (Maximum lod = " + FOUR_DIGIT_FORMATTER.format(this.maxlodLower) + ")");
        setYlabel("Chromosome (Maximum lod = " + FOUR_DIGIT_FORMATTER.format(this.maxlodUpper) + ")");
    }

    /**
     * Show the popup menu at the given click point
     * @param popupPoint
     *          the click point
     */
    private void showPopupMenu(Point popupPoint)
    {
        Iterator dotIter = this.allDots.iterator();
        while(dotIter.hasNext())
        {
            Dot currDot = (Dot)dotIter.next();
            if(currDot.getShape().contains(popupPoint))
            {
                ScanTwoResult scanTwoResult = this.scantwo;
                Cross parentCross = scanTwoResult.getParentCross();
                String phenotypeName = scanTwoResult.findScannedPhenotypeNameForScanColumn(
                        scanTwoResult.getScannedPhenotypeNames()[this.selectedPhenoIndex]);
                GeneticMarker trueMarker1 = this.getNearestTrueMarker(
                        currDot.getMarker1());
                GeneticMarker trueMarker2 = this.getNearestTrueMarker(
                        currDot.getMarker2());
                
                if(parentCross != null && phenotypeName != null &&
                   trueMarker1 != null && trueMarker2 != null)
                {
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(new ShowEffectPlotAction(
                            parentCross,
                            phenotypeName,
                            trueMarker1,
                            trueMarker2));
                    popupMenu.show(
                            this,
                            popupPoint.x,
                            popupPoint.y);
                }
                else
                {
                    LOG.warning(
                            "can't show effect plot since we dont have all " +
                            "of the data we need: parentCross=" + parentCross +
                            " phenotypeName=" + phenotypeName +
                            " marker1=" + trueMarker1 +
                            " marker2=" + trueMarker2);
                }
            }
        }
    }
    
    /**
     * Get the true genetic marker that is nearest the given genetic marker
     * (which may be a true marker or a pseudo marker)
     * @param marker
     *          the marker we're trying to get as close as possible to
     * @return
     *          the nearest true marker
     */
    private GeneticMarker getNearestTrueMarker(GeneticMarker marker)
    {
        GeneticMarker nearestTrueMarker = null;
        double nearestCmDistance = Double.MAX_VALUE;
        
        double markerPositionInCm = marker.getMarkerPositionCentimorgans();
        for(GeneticMap currMap: this.geneticMaps)
        {
            // don't bother with the map unless the chromosome names match up
            if(currMap.getChromosomeName().equals(marker.getChromosomeName()))
            {
                for(GeneticMarker currTrueMarker: currMap.getMarkerPositions())
                {
                    double currCmDistance = Math.abs(
                        markerPositionInCm -
                        currTrueMarker.getMarkerPositionCentimorgans());
                    if(nearestTrueMarker == null || currCmDistance < nearestCmDistance)
                    {
                        nearestTrueMarker = currTrueMarker;
                        nearestCmDistance = currCmDistance;
                    }
                }
            }
        }
        
        return nearestTrueMarker;
    }

    /**
     * Calculate the lod used in this plot based on LOD type user chosen.
     * @param upperLodIndex int
     * @param lowerLodIndex int
     */
    private void getLodsInThisPlot(int totalMarkers, int upperLodIndex, int lowerLodIndex) {
        this.lods = new double[totalMarkers][totalMarkers];
        // used the same logic as in scantwoDot
        for (int r=0; r<totalMarkers; r++) { // lower-left corner in data, upper-left corner in plot
            for (int c = 0; c <= r; c++) {
                if (r==c) {
                    this.lods[r][c] = this.scantwo.getScanOneLod(
                            this.selectedPhenoIndex,
                            r);
                }
                else {
                    switch (upperLodIndex) {
                        case LOD_FULL:
                            this.lods[r][c] = this.scantwo.getFullLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(c, r));
                            break;
                        case LOD_ADD:
                            this.lods[r][c] = this.scantwo.getAdditiveLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(c, r));
                            break;
                        case LOD_COND_INT:
                            this.lods[r][c] = this.scantwo.getFullVersusScanOneLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(c, r));
                            break;
                        case LOD_COND_ADD:
                            this.lods[r][c] = this.scantwo.getAdditiveVersusScanOneLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(c, r));
                            break;
                        case LOD_INT:
                            this.lods[r][c] = this.scantwo.getFullVersusAdditiveLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(c, r));
                            break;

                    }
                }
            }
        }
        for (int r=0; r<totalMarkers; r++) {  // upper-right corner in data, lower-right corner in plot
            for (int c = r; c <totalMarkers; c++) {
                if (c > r) { // it is handled above for (c==r)
                    switch (lowerLodIndex) {
                        case LOD_FULL:
                            this.lods[r][c] = this.scantwo.getFullLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(r, c));
                            break;
                        case LOD_ADD:
                            this.lods[r][c] = this.scantwo.getAdditiveLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(r, c));
                            break;
                        case LOD_COND_INT:
                            this.lods[r][c] = this.scantwo.getFullVersusScanOneLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(r, c));
                            break;
                        case LOD_COND_ADD:
                            this.lods[r][c] = this.scantwo.getAdditiveVersusScanOneLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(r, c));
                            break;
                        case LOD_INT:
                            this.lods[r][c] = this.scantwo.getFullVersusAdditiveLod(
                                    this.selectedPhenoIndex,
                                    new ScanTwoResult.MarkerIndexPair(r, c));
                            break;
                    }
                }
            }
        }
    }

    /**
     * set the maximum and minimum value on this graph for upper and lower triangle.
     * @param selectedChromosomes int[]
     */
    void setMinMaxValue() {
        int numMarkers = this.lods.length;
        for (int i = 0; i < numMarkers; i++) {
            for (int j = 0; j < numMarkers; j++) {
                if (i != j) {
                    double lod = this.lods[i][j];

                    if (i < j) { // upper
                        if (this.minlodLower > lod) this.minlodLower = lod;
                        if (this.maxlodLower < lod) this.maxlodLower = lod;
                    }

                    else { // lower
                        if (this.minlodUpper > lod) this.minlodUpper = lod;
                        if (this.maxlodUpper < lod) this.maxlodUpper = lod;
                    }
                }
            }
        }
    }
    
    private int graphWidth;
    
    private int graphHeight;
    
    /**
     * plot the scantwo result
     */
    void plot() {
        this.allDots = new HashSet<Dot>();
        int leftConerX = this.inset.left;
        int leftConerY = this.inset.top;
        int width = this.plotWidth;
        if (this.plotColorScale)
            width -= this.colorBarSpace;
        int height = this.plotHeight;

        // total number of rows, columns (rows=columns=numMarkers)
        int numMarkers = this.lods.length;

        int gridWidth = width/numMarkers;
        int gridHeight = height/numMarkers;

        width = gridWidth * numMarkers;
        height = gridHeight * numMarkers;
        
        this.graphWidth = width;
        this.graphHeight = height;

        // if need to plot color scale
        if (this.plotColorScale) {
            plotColorScaleBar(leftConerX, leftConerY, height);
        }

        // draw grids
        for (int row=0; row<numMarkers; row++) {
            for (int col=0; col<numMarkers; col++) {
                int x = gridWidth * col + leftConerX;
                int y = leftConerY + height - gridHeight * (row+1);
                Shape currentGrid = new Rectangle2D.Double(x,y,width/numMarkers,height/numMarkers);
                Dot dot = new Dot(this.markers.get(row), this.markers.get(col), currentGrid);
                this.allDots.add(dot);

                boolean isLower = true;
                if (row==col) // on diagnal
                    this.big.setColor(Color.blue);
                else {
                    if (row > col) { // lower in data, upper in plot
                        isLower = false;
                    }
                    this.big.setColor(getColor(this.lods[row][col], isLower));
                }
                this.big.fill(currentGrid);
                // TODO add interaction back in
//                if (markers[row].isSelected() && markers[col].isSelected() && (row!=col)) {
//                    big.setColor(Color.white);
//                    big.setStroke(new BasicStroke(1.8f));
//                    big.draw(currentGrid);
//                }
            }
        }

        // draw chromosome dividers, (numChr - 1) crossed lines
        int numChr = this.numMarkersOnEachChromosome.length;
        int cumulatedNumMarkers = 0;
        int lastx = leftConerX, lasty = leftConerY + height;
        for (int i=0; i<numChr; i++) {
            cumulatedNumMarkers += this.numMarkersOnEachChromosome[i];
            int x = cumulatedNumMarkers * gridWidth + leftConerX;
            int y = leftConerY + height - cumulatedNumMarkers * gridHeight;
            int tickx = lastx + (this.numMarkersOnEachChromosome[i] * gridWidth)/2;
            int ticky = lasty - (this.numMarkersOnEachChromosome[i] * gridHeight)/2;
            lastx = x;
            lasty = y;
            this.big.setColor(this.normalColor);
            this.big.setStroke(this.normalLinetype);
            // draw vertical lines
            this.big.drawLine(x, leftConerY, x, leftConerY + height);
            // draw horizontal lines
            this.big.drawLine(leftConerX, y, leftConerX + width, y);

            // draw ticks
            this.big.drawLine(tickx, leftConerY + height, tickx, leftConerY + height + this.tickHeight); // y axis ticks
            this.big.drawLine(leftConerX - this.tickHeight, ticky, leftConerX, ticky); // x axis ticks

            // label width and height
            Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(this.chromosomeNames[i], this.context);
            double labelWidth = labelBounds.getWidth();
            double labelHeight = labelBounds.getHeight();

            // find the right place to start to draw labels on y axis
            float yAxisLabelStartX = leftConerX - (float) labelWidth - this.tickHeight - this.labelToTick;
            float yAxisLabelStartY = (float) (ticky + labelHeight / 2 - 1); // -1 is only for looks prettier
            // draw tick label on y axis
            this.big.setFont(this.tickLabelFont);
            this.big.drawString(this.chromosomeNames[i], yAxisLabelStartX, yAxisLabelStartY); // vertical

            // find the right place to start to draw labels on x axis
            float xAxisLabelStartX = tickx - (float)labelWidth/2 + 0.5f; // 0.5f is only for looks prettier
            float xAxisLabelStartY = leftConerY + height + this.tickHeight + (float)labelHeight + this.labelToTick;
            // draw tick label on x axis
            this.big.drawString(this.chromosomeNames[i], xAxisLabelStartX, xAxisLabelStartY); // horizontal
        }

        // last two outlines in left and bottom of the plot
        this.big.drawLine(leftConerX, leftConerY, leftConerX, leftConerY+height); // vertical
        this.big.drawLine(leftConerX, leftConerY+height, leftConerX+width, leftConerY+height); // horizontal
    }

    // for MouseMotionListener
    public void mouseMoved(MouseEvent e)
    {
        int markerCount = this.markers.size();
        int x = e.getX();
        int y = e.getY();
        
        double heightRatio =
            (this.graphHeight - (y - this.inset.top))/(double)this.graphHeight;
        double widthRatio =
            (x - this.inset.left)/(double)this.graphWidth;
        
        int xMarkerIndex =
            (int)Math.floor((markerCount + 1) * widthRatio);
        int yMarkerIndex =
            (int)Math.floor((markerCount + 1) * heightRatio);
        
        if(xMarkerIndex < markerCount && yMarkerIndex < markerCount &&
           xMarkerIndex >= 0 && yMarkerIndex >= 0)
        {
            final String tip;
            if(xMarkerIndex == yMarkerIndex)
            {
                ScanTwoGeneticMarker marker = this.markers.get(xMarkerIndex);
                
                if(marker.isXChromosome())
                {
                    tip =
                        "<html>" + marker.getMarkerName()
                        + "<p>Scanone X LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getScanOneXLod(
                                this.selectedPhenoIndex,
                                xMarkerIndex));
                }
                else
                {
                    tip =
                        "<html>" + marker.getMarkerName()
                        + "<p>Scanone LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getScanOneLod(
                                this.selectedPhenoIndex,
                                xMarkerIndex));
                }
            }
            else
            {
                ScanTwoGeneticMarker xMarker = this.markers.get(xMarkerIndex);
                ScanTwoGeneticMarker yMarker = this.markers.get(yMarkerIndex);
                
                final MarkerIndexPair markerIndexPair;
                if(xMarkerIndex < yMarkerIndex)
                {
                    markerIndexPair = new MarkerIndexPair(xMarkerIndex, yMarkerIndex);
                }
                else
                {
                    markerIndexPair = new MarkerIndexPair(yMarkerIndex, xMarkerIndex);
                }
                
                tip =
                        "<html>" + xMarker.getMarkerName()
                        + ":" + yMarker.getMarkerName()
                        + "<p>Full LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getFullLod(
                                this.selectedPhenoIndex,
                                markerIndexPair))
                        + "<p>Add LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getAdditiveLod(
                                this.selectedPhenoIndex,
                                markerIndexPair))
                        + "<p>Cond-Int LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getFullVersusScanOneLod(
                                this.selectedPhenoIndex,
                                markerIndexPair))
                        + "<p>Cond-Add LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getAdditiveVersusScanOneLod(
                                this.selectedPhenoIndex,
                                markerIndexPair))
                        + "<p>Epstasis LOD: "
                        + FOUR_DIGIT_FORMATTER.format(this.scantwo.getFullVersusAdditiveLod(
                                this.selectedPhenoIndex,
                                markerIndexPair));
            }
            
            ToolTipManager.sharedInstance().setEnabled(true);
            this.setToolTipText(tip);
            setCursor(this.handCursor);
            return;
        }
    }

    private void plotColorScaleBar(int leftConerX, int leftConerY, int height) {
        // color bar
        int x = leftConerX + this.plotWidth - this.colorBarWidth - (this.colorBarSpace - this.colorBarWidth) / 2;
        for (int i = 0; i < 256; i++) {
            this.big.setColor(this.colorMap[i]);
            double y = leftConerY + height - height * i / 256.0;
            this.big.fill(new Rectangle2D.Double(x, y, this.colorBarWidth, height / 256.0));
        }
        // tick for upper triangle
        int maxIntLod = (int) this.maxlodUpper;
        this.big.setColor(this.normalColor);
        this.big.setFont(this.tickLabelFont);
        for (int i = 0; i <= maxIntLod; i++) {
            Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(i + "", this.context);
            double y = leftConerY + height - height * i / this.maxlodUpper;
            this.big.drawLine(x - this.tickHeight, (int) y, x, (int) y);
            this.big.drawString(i + "", (float) (x - this.tickHeight - labelBounds.getWidth() - this.labelToTick), (float) (y - 1 + labelBounds.getHeight() / 2)); // -1 is only for prettier
        }
        // tick for lower triangle
        maxIntLod = (int) this.maxlodLower;
        for (int i = 0; i <= maxIntLod; i++) {
//            if ((maxIntLod > 5) && (maxIntLod % (maxIntLod/5) == 0)) {
                Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(i + "", this.context);
                double y = leftConerY + height - height * i / this.maxlodLower;
                this.big.drawLine(x + this.colorBarWidth + this.tickHeight, (int) y, x + this.colorBarWidth, (int) y);
                this.big.drawString(i + "", (float) (x + this.colorBarWidth + this.tickHeight + this.labelToTick), (float) (y - 1 + labelBounds.getHeight() / 2)); // -1 is only for prettier
//            }
        }
        this.big.drawRect(x, leftConerY, this.colorBarWidth, height);
    }

    private Color getColor(double value, boolean isLower) {
        int index = 0;
        if (isLower)
            index = (int)((value-this.minlodLower)/((this.maxlodLower-this.minlodLower)/NUM_COLORS))-1;
        else
            index = (int)((value-this.minlodUpper)/((this.maxlodUpper-this.minlodUpper)/NUM_COLORS))-1;
        if (index == -1) index = 0;
        if (index > 255) index = 255;
        return this.colorMap[index];
    }
}
