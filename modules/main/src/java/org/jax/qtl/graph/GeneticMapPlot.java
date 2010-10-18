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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.ToolTipManager;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMarker;


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
public class GeneticMapPlot extends OneDimensionPlot {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3913294152871247405L;
    Cross cross;
    int numChr;
    boolean startFromZero = true; // default is all chromosome pos start from zero
    double maxPos;
    // graph parameters
    int markerLength = 10;
    Insets plotInset = new Insets(this.distToAxis+10, this.distToAxis, this.distToAxis + this.tickHeight+10, this.distToAxis);

    /**
     * Constructor
     * @param cross
     *          the cross whose genetic map we're plotting
     */
    public GeneticMapPlot(Cross cross) {
        super(); // for adding mouse and mouse motion listeners
        this.cross = cross;
        this.numChr = cross.getNumberOfChromosomes();
        List<CrossChromosome> genotypeData = cross.getGenotypeData();
        this.maxPos = 0.0;
        for(CrossChromosome currChromosome: genotypeData)
        {
            List<GeneticMarker> markers =
                currChromosome.getAnyGeneticMap().getMarkerPositions();
            for(GeneticMarker geneticMarker: markers)
            {
                double currPos = geneticMarker.getMarkerPositionCentimorgans();
                if(currPos > this.maxPos)
                {
                    this.maxPos = currPos;
                }
            }
        }

        // set the initial size
        setPreferredSize(new Dimension(600,400));
        setMinimumSize(new Dimension(60,40));

        // add GenoDataSelectionChangeListener to all markers in the scanone data
        // TODO add interaction back
//        cross.addGenoDataSelectionChangeListeners(this);

        // set title and labels
        setTitle("Genetic Map for " + cross.toString());
        setXlabel("Chromosome");
        setYlabel("Location (cM)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void plot() {
        int leftConerX = this.inset.left + this.plotInset.left;
        int leftConerY = this.inset.top + this.plotInset.top;
        int width = this.plotWidth - (this.plotInset.left + this.plotInset.right);
        int height = this.plotHeight - (this.plotInset.top + this.plotInset.bottom);

        double yscaler = height/this.maxPos;
        // draw ticks and labels on y axis
        for (int i=0; i<this.maxPos; i+=20) {
            drawYtickAndLabel(yscaler, i, 0);
        }

        List<CrossChromosome> genotypeData = this.cross.getGenotypeData();
        for (int i=0; i<this.numChr; i++) {
            CrossChromosome currChromosome = genotypeData.get(i);
            List<GeneticMarker> markers = currChromosome.getAnyGeneticMap().getMarkerPositions();
            if(markers.size() > 0)
            {
            double x = leftConerX + (i+1) * width * 1.0/(this.numChr + 1);
            int numMarkers = markers.size();
            GeneticMarker firstMarker = markers.get(0);
            GeneticMarker lastMarker = markers.get(numMarkers - 1);
            double adjustment = 0;
            if (!this.startFromZero) {
                adjustment = firstMarker.getMarkerPositionCentimorgans();
            }
            double y1 =
                leftConerY + height -
                (firstMarker.getMarkerPositionCentimorgans() - adjustment)*yscaler;
            double y2 =
                leftConerY + height -
                (lastMarker.getMarkerPositionCentimorgans() - adjustment)*yscaler;

            // draw horizontal lines for each marker
            for (int j=0; j<numMarkers; j++) {
                GeneticMarker currentMarker = markers.get(j);
                double y =
                    leftConerY + height -
                    (currentMarker.getMarkerPositionCentimorgans() - adjustment) * yscaler;
                
                // TODO add back interaction
//                if (currentMarker.getMap().isSelected())
//                    big.setColor(highlightColor);
//                else
                    this.big.setColor(this.normalColor);
                Rectangle2D currentShape = new Rectangle2D.Double((x - this.markerLength/2), (int)y, this.markerLength, 1);
                this.allDots.add(new Dot(currentMarker, currentShape));
                this.big.fill(currentShape);
//                big.drawLine((int)(x - markerLength/2), (int)y, (int)(x + markerLength/2), (int)y);
            }

            // draw vertical line for each chromosome
            this.big.setColor(this.normalColor);
            this.big.drawLine((int)x, (int)y1, (int)x, (int)y2);

            // draw ticks and label on x axis
            int baseY = this.inset.top+this.plotHeight;
            this.big.setColor(this.lineColor);
            this.big.setStroke(this.normalLinetype);
            this.big.drawLine( (int) x, baseY, (int) x, baseY - this.tickHeight);

            String label = currChromosome.getChromosomeName();
            FontRenderContext context = this.big.getFontRenderContext();
            Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(label, context);
            int labelWidth = (int) labelBounds.getWidth();
            int labelHeight = (int) labelBounds.getHeight();
            int labelStartX = this.inset.left + this.plotInset.left + (int)(width * 1.0/(this.numChr + 1)*(i+1) - labelWidth/2);
            int labelStartY = getHeight() - this.inset.bottom + labelHeight;
            this.big.setFont(this.tickLabelFont);
            this.big.drawString(label, labelStartX, labelStartY);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // show the tip notes for all selected dots
        for(Dot currentDot: this.allDots)
        {
            if (currentDot.getShape().contains(new Point2D.Double(e.getX(), e.getY()))) {
                String tip = "<html>" + currentDot.getMarker1().getMarkerName() + ":" +
                    "<p>Chromosome: " + currentDot.getMarker1().getChromosomeName() +
                    "<p>Location(cM): " + ONE_DIGIT_FORMATTER.format(
                            currentDot.getMarker1().getMarkerPositionCentimorgans());

                ToolTipManager.sharedInstance().setEnabled(true);
                GeneticMapPlot.this.setToolTipText(tip);
                setCursor(this.handCursor);
                return;
            }
            else {
                ToolTipManager.sharedInstance().setEnabled(false);
                setCursor(this.normalCursor);
            }
        }
    }
}
