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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.jax.qtl.Constants;
import org.jax.qtl.action.GenoDataSelectionChangeEvent;
import org.jax.qtl.action.GenoDataSelectionChangeListener;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.util.NonRepeatVector;
import org.jax.util.ObjectUtil;

/**
 * <p>Title: OneDemensionalPlot</p>
 *
 * <p>Description: This is a base class for all one dimensional plots. It has
 * the selection box, basic dot class, xLabel, yLabel, title </p>
 *
 * <p>Company: The Jackson Laboratory</p>
 *
 * @author Lei Wu
 * @version 1.0
 */
@SuppressWarnings("all")
public abstract class OneDimensionPlot extends JPanel implements MouseMotionListener, MouseListener, GenoDataSelectionChangeListener, Constants{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2975206803854712333L;
    private String title="", xLabel="", yLabel="";
    Graphics2D big;
    NonRepeatVector selectedDots = new NonRepeatVector(); // vector of all selected Dots on this plot
    Set<Dot> allDots = new HashSet<Dot>(); // vector of all Dots with lod score and related GeneticMap on this plot
    BufferedImage bi;
    int x = -1, y = -1, width = 0, height = 0, leftx = -1, lefty = -1; // selection box location and size
    final int DEFAULT_DOT_SIZE = 1;
    int distToBorder = 60, distToAxis = 5, tickHeight = 5, dotSize = this.DEFAULT_DOT_SIZE;
    int yTickLength = 4, labelToTick = 2;
    Insets inset = new Insets(this.distToBorder - 10, this.distToBorder + 10, this.distToBorder, this.distToBorder - 20);
    Insets plotInset = new Insets(this.distToAxis, this.distToAxis, this.distToAxis + this.tickHeight, this.distToAxis);
    int plotWidth, plotHeight;
    Font labelFont = new Font("SansSerif", Font.BOLD, 12);
    Font titleFont = new Font("SansSerif", Font.BOLD, 14);
    Font tickLabelFont = new Font("SansSerif", Font.PLAIN, 11);
    Color normalColor = Color.black, highlightColor = Color.red, lineColor = Color.black;
    Color selectionBoxColor = new Color(0,0,0,5);
    Stroke lodLinetype = new BasicStroke(2f), normalLinetype = new BasicStroke(1f);
    // cursor types
    Cursor plotCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    boolean drawOutlineBox = true;
    FontRenderContext context;
    protected volatile boolean graphRasterNeedsRepaint = true;

    OneDimensionPlot() {
//        ToolTipManager.sharedInstance().setInitialDelay(0);
        // add mouse listeners
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addComponentListener(new ComponentListener()
        {
            public void componentHidden(ComponentEvent e)
            {
            }

            public void componentMoved(ComponentEvent e)
            {
            }

            public void componentResized(ComponentEvent e)
            {
                OneDimensionPlot.this.graphRasterNeedsRepaint = true;
                repaint();
            }

            public void componentShown(ComponentEvent e)
            {
            }
        });
    }
    
    /**
     * Getter for the x label
     * @return the xLabel
     */
    public String getXLabel()
    {
        return this.xLabel;
    }

    /**
     * Setter for the x label
     * @param label
     *          the new label
     */
    public void setXlabel(String label)
    {
        this.xLabel = label;
        this.graphRasterNeedsRepaint = true;
        this.repaint();
    }
    
    /**
     * Getter for the y label
     * @return the yLabel
     */
    public String getYLabel()
    {
        return this.yLabel;
    }
    
    /**
     * Setter for the Y label
     * @param label
     *          the new y label
     */
    public void setYlabel(String label)
    {
        this.yLabel = label;
        this.graphRasterNeedsRepaint = true;
        this.repaint();
    }
    
    /**
     * Getter for the title
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }
    
    /**
     * Setter for the title
     * @param title
     *          the new title
     */
    public void setTitle(String title)
    {
        this.title = title;
        this.graphRasterNeedsRepaint = true;
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        // plot the data
        if(this.graphRasterNeedsRepaint)
        {
            setupPlot();
            this.context = this.big.getFontRenderContext();

            this.graphRasterNeedsRepaint = false;
            plot();
            drawTitle();
            drawXlabel();
            drawYlabel();
            drawSelectionBox();
        }
        
        // draw to screen
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.bi, 0, 0, getWidth(), getHeight(), null);
    }
    
    // this function need to be override, otherwise, nothing will be drawn
    abstract void plot();

    public void setSize(Dimension size) {
        super.setSize(size);
    }

    void drawXlabel() {
        this.big.setFont(this.labelFont);
        this.big.setColor(this.normalColor);
        FontRenderContext context = this.big.getFontRenderContext();
        Rectangle2D labelBounds = this.labelFont.getStringBounds(this.xLabel, context);
        // draw x label
        int labelWidth = (int)labelBounds.getWidth();
        int labelHeight = (int)labelBounds.getHeight();
        int labelStartX = (this.plotWidth - labelWidth) / 2 + this.inset.left;
        int labelStartY = this.inset.top + this.plotHeight + (this.inset.bottom + labelHeight)/2;
        this.big.drawString(this.xLabel, labelStartX, labelStartY);
    }

    void drawTitle() {
        this.big.setFont(this.titleFont);
        this.big.setColor(this.normalColor);
        FontRenderContext context = this.big.getFontRenderContext();
        Rectangle2D labelBounds = this.titleFont.getStringBounds(this.title, context);
        // draw x label
        int labelWidth = (int)labelBounds.getWidth();
        int labelHeight = (int)labelBounds.getHeight();
        int labelStartX = (this.plotWidth - labelWidth)/2 + this.inset.left;
        int labelStartY = (this.inset.top + labelHeight)/2;
        this.big.drawString(this.title, labelStartX, labelStartY);
    }

    void drawYlabel() {
        // set the font and size
        this.big.setFont(this.labelFont);
        this.big.setColor(this.normalColor);
        // center the label
        FontRenderContext context = this.big.getFontRenderContext();
        Rectangle2D labelBounds = this.labelFont.getStringBounds(this.yLabel, context);
        double labelHeight = labelBounds.getHeight();
        double labelWidth = labelBounds.getWidth();

        int labelStartX = (int) (this.inset.left + labelHeight) / 3;
        int labelStartY = this.inset.top + (int) ((this.plotHeight + labelWidth) / 2);

        // clockwise 270 degrees
        this.big.rotate(-Math.PI / 2, labelStartX, labelStartY);
        // draw label
        this.big.drawString(this.yLabel, labelStartX, labelStartY);
        this.big.rotate(Math.PI / 2, labelStartX, labelStartY);
    }

    void drawYtickAndLabel(double yscaler, double loc, double startingPos) {
        drawYtickAndLabel(yscaler, loc, loc+"", startingPos);
    }

    // loc should be larger than startingPos
    void drawYtickAndLabel(double yscaler, double loc, String label, double startingPos){
        double yaxisy = this.inset.top + this.plotHeight - this.plotInset.bottom - (loc - startingPos) * yscaler;
//System.out.println("yaxisy="+yaxisy + "  inset.top+plotHeight-plotInset.bottom=" + (inset.top + plotHeight - plotInset.bottom));
        this.big.drawLine(this.inset.left - this.yTickLength, (int)yaxisy, this.inset.left, (int)yaxisy);

        // find the right place to start to draw labels on x axis
        FontRenderContext context = this.big.getFontRenderContext();
        Rectangle2D labelBounds = this.tickLabelFont.getStringBounds(label, context);

        double labelWidth = labelBounds.getWidth();
        double labelHeight = labelBounds.getHeight();

        float yaxisLabelStartX = this.inset.left - (float) labelWidth - this.yTickLength - this.labelToTick;
        float yaxisLabelStartY = (float)(yaxisy + labelHeight/2 - 1); // -1 is only for looks better
        this.big.setColor(this.lineColor);
        this.big.setFont(this.tickLabelFont);
        this.big.drawString(label, yaxisLabelStartX, yaxisLabelStartY);
    }

    // setup the plot with initial outline and ticks, labels
    private void setupPlot() {
        this.bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        this.big = this.bi.createGraphics();
        // fill the background with white
        this.big.setColor(Color.white);
        this.big.fill(new Rectangle2D.Double(0,0,getWidth(), getHeight()));

        this.big.setColor(this.lineColor);
        int oldPlotWidth = this.plotWidth, oldPlotHeight = this.plotHeight;
        this.plotWidth = getWidth() - (this.inset.right + this.inset.left);
        this.plotHeight = getHeight() - (this.inset.bottom + this.inset.top);

        // remove all selection box if the size of plot changed
        if ( (oldPlotWidth != this.plotWidth) || (oldPlotHeight != this.plotHeight)) {
            removeSelectionBox();
            deSelectPoints();
        }
        // draw outline box
        if (this.drawOutlineBox)
            this.big.drawRect(this.inset.left, this.inset.top, this.plotWidth, this.plotHeight);
        //
        this.allDots = new HashSet<Dot>();
    }

    /**
     * draw the user selection box
     */
    private void drawSelectionBox() {
        // draw selection box
        Rectangle2D selectionBox = new Rectangle2D.Double(this.leftx, this.lefty, this.width, this.height);
        this.big.setColor(Color.gray);
        this.big.draw(selectionBox);
        this.big.setColor(this.selectionBoxColor);
        this.big.fill(selectionBox);
    }


    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e) {
        this.x = e.getX();
        this.y = e.getY();
        removeSelectionBox();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e) {
        // remove the tiny spot on the graph if nothing selected
        if (this.width == 0 && this.height == 0) {
            deSelectPoints();
        }
        else {
            this.selectedDots = new NonRepeatVector();
            Rectangle2D selectionBox = new Rectangle2D.Double(this.leftx, this.lefty, this.width, this.height);
            for(Dot dot: this.allDots)
            {
                if (selectionBox.contains(dot.shape.getBounds2D())) {
                    dot.setSelected(true);
                    this.selectedDots.add(dot);
                    System.out.println("selectedDot width=" + dot.shape.getBounds2D().getWidth());
                }
                else {
                    dot.setSelected(false);
                }
            }
            System.out.println("numSelectedDots=" + this.selectedDots.size());
        }
        repaint();
    }

    private void removeSelectionBox() {
        this.width = 0;
        this.height = 0;
    }

    private void deSelectPoints() {
        this.leftx = -1;
        this.lefty = -1;
        int numSelectedDots = this.selectedDots.size();
        for (int i = 0; i < numSelectedDots; i++)
            ( (Dot) this.selectedDots.elementAt(i)).setSelected(false);
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent e) {}
    
    /**
     * {@inheritDoc}
     */
    public void mouseDragged(MouseEvent e) {
        // for selection box
        int curX = e.getX();
        int curY = e.getY();
        this.width = Math.abs(this.x - curX);
        this.height = Math.abs(this.y - curY);

        if (curX < this.x) this.leftx = curX;
        else this.leftx = this.x;

        if (curY < this.y) this.lefty = curY;
        else this.lefty = this.y;

        repaint();
    }

    /**
     * {@inheritDoc}
     */
    public void GenoDataSelectionChanged(GenoDataSelectionChangeEvent e) {
        repaint();
    }

    class Dot
    {
        private final GeneticMarker marker1;
        
        private final GeneticMarker marker2;
        
        private Shape shape;
        
        /**
         * Constructor for 1 marker dot (the second marker is set to null)
         * @param marker1
         *          the 1st marker
         * @param shape
         *          the shape
         */
        public Dot(
                GeneticMarker marker1,
                Shape shape)
        {
            this.marker1 = marker1;
            this.marker2 = null;
            this.shape = shape;
        }

        /**
         * Constructor for 2 marker dot
         * @param marker1
         *          the 1st marker
         * @param marker2
         *          the 2nd marker
         * @param shape
         *          the shape
         */
        public Dot(
                GeneticMarker marker1,
                GeneticMarker marker2,
                Shape shape)
        {
            this.marker1 = marker1;
            this.marker2 = marker2;
            this.shape = shape;
        }

        void setSelected(boolean selected) {
            // TODO add back selection
//            int numMaps = map.length;
//            for (int i=0; i<numMaps; i++) {
//                map[i].setSelected(selected);
//            }
        }
        
        /**
         * The 1st marker
         * @return
         *          the 1st marker
         */
        public GeneticMarker getMarker1()
        {
            return this.marker1;
        }
        
        /**
         * The 2nd marker
         * @return
         *          the 2nd marker
         */
        public GeneticMarker getMarker2()
        {
            return this.marker2;
        }
        
        /**
         * Getter for the shape of this dot
         * @return
         *          the shape
         */
        public Shape getShape()
        {
            return this.shape;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return ObjectUtil.hashObject(this.shape) +
                   ObjectUtil.hashObject(this.marker1);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Dot)
            {
                Dot otherDot = (Dot)obj;
                return ObjectUtil.areEqual(this.marker1, otherDot.marker1) &&
                       ObjectUtil.areEqual(this.marker2, otherDot.marker2) &&
                       ObjectUtil.areEqual(this.shape, otherDot.shape);
            }
            else
            {
                return false;
            }
        }
    }
}
