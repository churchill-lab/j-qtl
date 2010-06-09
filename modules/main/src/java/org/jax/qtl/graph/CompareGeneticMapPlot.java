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

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.SexAwareGeneticMap;

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
public class CompareGeneticMapPlot extends GeneticMapPlot
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -630110935598985219L;
    private final List<SexAwareGeneticMap> estimatedGeneticMaps;
    
    /**
     * Constructor
     * @param cross
     *          the cross to plot the map for
     * @param estimatedGeneticMaps
     *          the maps to plot
     */
    public CompareGeneticMapPlot(
            Cross cross,
            List<SexAwareGeneticMap> estimatedGeneticMaps)
    {
        super(cross);
        
        this.estimatedGeneticMaps = estimatedGeneticMaps;
        for(SexAwareGeneticMap currSexAwareMap: estimatedGeneticMaps)
        {
            GeneticMap currMap = currSexAwareMap.getAnyGeneticMap();
            for(GeneticMarker currMarker: currMap.getMarkerPositions())
            {
                double currPosition =
                    currMarker.getMarkerPositionCentimorgans();
                if(currPosition > this.maxPos)
                {
                    this.maxPos = currPosition;
                }
            }
        }
        
        setTitle("Comparison of Genetic Maps for " + cross.toString());
    }

    @Override
    void plot() {
        int leftConerX = this.inset.left + this.plotInset.left;
        int leftConerY = this.inset.top + this.plotInset.top;
        int width = this.plotWidth - (this.plotInset.left + this.plotInset.right);
        int height = this.plotHeight - (this.plotInset.top + this.plotInset.bottom);

        double yscaler = height / this.maxPos;
        // draw ticks and labels on y axis
        for (int i = 0; i < this.maxPos; i += 20) {
            drawYtickAndLabel(yscaler, i, 0);
        }

        for (int i = 0; i < this.numChr; i++) {
            CrossChromosome chr = this.cross.getGenotypeData().get(i);
            List<GeneticMarker> originalMarkers =
                chr.getAnyGeneticMap().getMarkerPositions();
            List<GeneticMarker> estimatedMarkers =
                this.estimatedGeneticMaps.get(i).getAnyGeneticMap().getMarkerPositions();
            int numMarkers = originalMarkers.size();
            
            double x = leftConerX + (i + 1) * width * 1.0 / (this.numChr + 1);
            double firstMarkerPos = originalMarkers.get(0).getMarkerPositionCentimorgans();
            double lastMarkerPos = originalMarkers.get(numMarkers - 1).getMarkerPositionCentimorgans();
            double firstEstMarkerPos = estimatedMarkers.get(0).getMarkerPositionCentimorgans();
            double lastEstMarkerPos = estimatedMarkers.get(numMarkers -1).getMarkerPositionCentimorgans();
            double adjustment = 0;
            if (!this.startFromZero) {
                adjustment = Math.min(firstMarkerPos, firstEstMarkerPos);
            }

            double y1 = leftConerY + height - (firstMarkerPos - adjustment) * yscaler;
            double y2 = leftConerY + height - (lastMarkerPos - adjustment) * yscaler;

            double y1Est = leftConerY + height - (firstEstMarkerPos - adjustment) * yscaler;
            double y2Est = leftConerY + height - (lastEstMarkerPos - adjustment) * yscaler;

            // draw horizontal lines for each marker
            for (int j = 0; j < numMarkers; j++) {
                GeneticMarker currOriginalMarker = originalMarkers.get(j);
                GeneticMarker currEstimatedMarker = estimatedMarkers.get(j);
                double y =
                    leftConerY + height -
                    (currOriginalMarker.getMarkerPositionCentimorgans() - adjustment) * yscaler;
                double yEst =
                    leftConerY + height -
                    (currEstimatedMarker.getMarkerPositionCentimorgans() - adjustment) * yscaler;
                // TODO add back interaction
//                if (currentMarker.getMap().isSelected())
//                    big.setColor(highlightColor);
//                else
                    this.big.setColor(this.normalColor);
                this.big.drawLine( (int) (x - this.markerLength / 2), (int) y, (int) (x + this.markerLength / 2), (int) yEst);
            }
            // draw vertical lines (old on left, new on right) for each chromosome
            this.big.setColor(this.normalColor);
            this.big.drawLine( (int)(x - this.markerLength/2), (int) y1, (int)(x - this.markerLength/2), (int) y2);
            this.big.drawLine( (int)(x + this.markerLength/2), (int) y1Est, (int)(x + this.markerLength/2), (int) y2Est);

            // draw ticks and label on x axis
            int baseY = this.inset.top + this.plotHeight;
            this.big.setColor(this.lineColor);
            this.big.setStroke(this.normalLinetype);
            this.big.drawLine( (int) x, baseY, (int) x, baseY - this.tickHeight);

            String label = chr.getChromosomeName();
            FontRenderContext context = this.big.getFontRenderContext();
            Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(label, context);
            int labelWidth = (int) labelBounds.getWidth();
            int labelHeight = (int) labelBounds.getHeight();
            int labelStartX = this.inset.left + this.plotInset.left + (int) (width * 1.0 / (this.numChr + 1) * (i + 1) - labelWidth / 2);
            int labelStartY = getHeight() - this.inset.bottom + labelHeight;
            this.big.setFont(this.tickLabelFont);
            this.big.drawString(label, labelStartX, labelStartY);
        }
    }
}
