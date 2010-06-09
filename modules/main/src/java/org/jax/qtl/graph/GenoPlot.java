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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import org.jax.analyticgraph.data.NamedCategoricalData;
import org.jax.analyticgraph.data.NamedData;
import org.jax.analyticgraph.data.NamedDataMatrix;
import org.jax.analyticgraph.data.NamedRealData;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.util.math.Matlab;

/**
 * Panel for plotting genotype data
 * @author Hao Wu
 * @author Keith Sheppard (minor modifications for integrating w/ J/qtl 1.0)
 */
public class GenoPlot extends JPanel implements MouseMotionListener
{
    /**
     * 
     */
    private static final long serialVersionUID = 6986895085781481197L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            GenoPlot.class.getName());
    
  @SuppressWarnings("unchecked")
  private Hashtable GenoPlotProperties; // figure properties
  private BufferedImage bImage;
  private Graphics2D g2;
  private static final int LEFT=50, TOP=70, BOTTOM=20, RIGHT=20;
  private int WIDTH, HEIGHT; // width and height of the figure
  private int NCOL=0, NROW=0; // number of rows and columns in geno data to be plot
  private int plotLeft=LEFT, plotTop=TOP, plotRight, plotBottom;
  private int currentx=0, currenty=0;
  private int thisChrIdx=0, thisIndIdx=0, thisMarIdx=0;
  private boolean inPlotRegion;
  private int SPACING; // space between chromosomes
  private int YSPACING=3; // space between individuals for plotting in real marker distance
  private String title = "";
  private final Cross cross;
  private int nchr; // total number of chromosomes
  private double[] chrlen; // chromosome length
  private int nind; // total number of individuals
  private int[] nmar; // number of markers on each chromosome
  // adjusted marker position for all chromosomes
  // this is only used when plotting in real marker distance
  private double[][] mpos=null;
  private double[][] xregion;
  private int[] sortIdx;
  // mouse listener
  private MyMouseListener mouselistener;

  // ======== figure properties ============
  // colors
  private Color[] GenoColor; // color for genotypes
  private Color MissingColor; // color for missing genotype
  // block size
  private int XSPACE, YSPACE;
  // sort by what
  private int sortbyidx;
  // what to plot: 0 - genotype, 1 - crossoves, 2 - missing, 3-errorlod
  private int whattoplot;
  // whether the plot should be interactive
  private boolean interactive;
  // whether to plot in the real marker distance or not
  private boolean inMarkDist;
  // chr and ind index
  private int[] chridx, indidx;
  // for plotting error lod
  private double[] errorlod_breaks;
  private Color[] errorlod_colors;

  /**
   * Constructor
   * @param c the cross
   * @param properties the properties
   */
  @SuppressWarnings("unchecked")
  public GenoPlot(Cross c, Hashtable properties) {
    this.GenoPlotProperties = properties;
    this.cross = c;
    // total number of chromosomes and inds
    this.nchr = this.cross.getNumberOfChromosomes();
    this.nind = this.cross.getNumberOfIndividuals();
    this.nmar = this.cross.getNumberOfMarkers();
    // chromosome length
    this.chrlen = GenoPlot.getAllChromosomeLengths(this.cross);
    // get figure properties
    getFigureProperties();
    // sort the individuals
    sort(this.sortbyidx);
    // prepare the figure
    calcPlotRegion();
    setBackground(Color.white);
    setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
    createBufferedImage();
    // add mouse listeners if it's interactive plot
    this.mouselistener = new MyMouseListener();
    if(this.interactive) {
      addMouseListener(this.mouselistener);
      addMouseMotionListener(this);
    }
  }
  
  private static double[] getAllChromosomeLengths(Cross cross)
  {
    List<CrossChromosome> genotypeData = cross.getGenotypeData();
    double[] allChromosomeLengths = new double[genotypeData.size()];
    for(int i = 0; i < allChromosomeLengths.length; i++)
    {
        // TODO need to handle sex specific here too
        allChromosomeLengths[i] = GeneticMap.getTotalExtentOfMarkerListInCentimorgans(
                genotypeData.get(i).getAnyGeneticMap().getMarkerPositions());
    }
    
    return allChromosomeLengths;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(this.bImage, 0, 0, this.WIDTH, this.HEIGHT, this);
  }

  /**
   * sort image by a given pheno type
   * @param pheidx the index
   */
  public void sort(int pheidx) {
    if(pheidx == 0) {
      this.sortIdx = new int[this.nind];
      for(int i=0; i<this.nind; i++)
        this.sortIdx[i] = i;
    }
    else{
      // get the phenotype
      List<Number> phenoList = this.cross.getPhenotypeData().getNamedDataList().get(pheidx-1).getData();
      double[] pheno = new double[phenoList.size()];
      for(int i = 0; i < pheno.length; i++)
      {
          Number currPhenoNumber = phenoList.get(i);
          if(currPhenoNumber == null)
          {
              pheno[i] = Double.NEGATIVE_INFINITY;
          }
          else
          {
              pheno[i] = currPhenoNumber.doubleValue();
          }
      }
      this.sortIdx = Matlab.order(pheno);
      
      if(LOG.isLoggable(Level.FINE))
      {
          LOG.fine(
                  "Sorted individuals by phenotype: " +
                  this.cross.getPhenotypeData().getDataNames()[pheidx - 1]);
          StringBuffer newOrderBuffer = new StringBuffer("New order is:");
          StringBuffer sortedPhenoBuffer = new StringBuffer(
          "Phenotypes in sorted order are:");
          for(int currIndex: this.sortIdx)
          {
              newOrderBuffer.append(" ");
              newOrderBuffer.append(currIndex);
              sortedPhenoBuffer.append(" ");
              sortedPhenoBuffer.append(pheno[currIndex]);
          }
          LOG.fine(newOrderBuffer.toString());
          LOG.fine(sortedPhenoBuffer.toString());
      }
    }
  }

  // get the figure properties from hash table
  private void getFigureProperties() {
    this.sortbyidx = ((Integer)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_SORTBY)).intValue();
    this.XSPACE = ((Integer)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_XSIZE)).intValue();
    this.YSPACE = ((Integer)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_YSIZE)).intValue();
    this.SPACING = (this.XSPACE+1) * 2; // calculate spacing based on xspace
    this.GenoColor = (Color[])this.GenoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
    this.MissingColor = this.GenoColor[this.GenoColor.length-1];
    this.whattoplot = ((Integer)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_WHAT)).intValue();
    this.interactive = ((Boolean)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_INT)).booleanValue();
//    if(whattoplot != 0)
//      interactive = false;
    this.inMarkDist = ((Boolean)this.GenoPlotProperties.get(FigureProperties.GENOPLOT_IN_MARKER_DIST))
                 .booleanValue();
    // for errorlod
    this.errorlod_breaks = (double [])this.GenoPlotProperties.get(FigureProperties.GENOPLOT_ERRORLOD_BREAKS);
    this.errorlod_colors = (Color[])this.GenoPlotProperties.get(FigureProperties.GENOPLOT_ERRORLOD_COLORS);
    // chrmosome index
    Object otmp;
    otmp = this.GenoPlotProperties.get(FigureProperties.GENOPLOT_CHROM);
    if(otmp == "all") { // all chromosomes
      this.chridx = new int[this.nchr];
      for(int i=0; i<this.nchr; i++)
        this.chridx[i] = i;
    }
    else { //chose some chromosome. The property entry should be a int[]
      this.chridx = (int[])this.GenoPlotProperties.get(FigureProperties.GENOPLOT_CHROM);
    }
    // individual index
    otmp = this.GenoPlotProperties.get(FigureProperties.GENOPLOT_IND);
    if(otmp.equals("all")) { // all individuals
      this.indidx = new int[this.nind];
      for(int i=0; i<this.nind; i++)
        this.indidx[i] = i;
    }
    else {// I will do this later
      this.indidx = (int[])otmp;
    }
    // some other related parameters
    this.xregion = new double[this.chridx.length][2];
    this.NROW = this.indidx.length;
    this.NCOL = 0;
    for(int i=0; i<this.chridx.length; i++)
      this.NCOL = this.NCOL + this.nmar[this.chridx[i]];
    //    NCOL = cross.getNtotmar();
    // marker positions
    if(this.inMarkDist) {
      this.mpos = new double[this.chridx.length][];
      for(int i = 0; i < this.chridx.length; i++)
      {
          List<GeneticMarker> currMarkerPositions =
              this.cross.getGenotypeData().get(i).getAnyGeneticMap().getMarkerPositions();
          this.mpos[i] = new double[currMarkerPositions.size()];
          
          // use the starting position to adjust all other markers
          double mpos0 = currMarkerPositions.get(0).getMarkerPositionCentimorgans();
          for(int j = 0; j < this.nmar[this.chridx[i]]; j++)
          {
              this.mpos[i][j] = currMarkerPositions.get(j).getMarkerPositionCentimorgans() - mpos0;
          }
      }
    }
  }

  // create buffered image
  private void createBufferedImage() {
    // Create buffered image that does not support transparency
    this.bImage = new BufferedImage(this.WIDTH, this.HEIGHT,
                               BufferedImage.TYPE_3BYTE_BGR);
    this.g2 = this.bImage.createGraphics();
    // fill background with white
//    g2.setColor(Color.GRAY);
    this.g2.fillRect(0, 0, this.WIDTH, this.HEIGHT);
    doGenoPlot();
    this.g2.dispose();
  }

  // generate genotype plot
  private void doGenoPlot() {
    int x=0, y=0, xsp, ysp;
//    double data; // genotype data or errorlod
    // re-calculate plot region
//    calcPlotRegion();
    int colidx = 0;
    int xoffset = GenoPlot.LEFT + 1;
    // the calculation of xregion is a little off
    // I don't want to waste time to correct it 'cause it's not that important.
    // the following code works fine for all cases.
    if(this.inMarkDist)
      this.xregion[0][0] = xoffset;
    else
      this.xregion[0][0] = xoffset - 1;

    // grab genocolor from figure property
    this.GenoColor = (Color[])this.GenoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
    this.MissingColor = this.GenoColor[this.GenoColor.length-1];

    // the xspace and yspace value depends on whether there's seperation lines
    // or not.
    if(this.interactive) {
      xsp = this.XSPACE + 1; ysp = this.YSPACE + 1;
    }
    else {
      xsp = this.XSPACE; ysp = this.YSPACE;
    }
    // plot some solid lines if plotting in real marker distance
    if(this.inMarkDist) {
//      Stroke solid_line = new BasicStroke(2f);
//      g2.setStroke(solid_line);
      this.g2.setColor(Color.black);
      x = GenoPlot.LEFT+1;
      for(int i=0; i<this.chridx.length; i++) {
        y = GenoPlot.TOP+ysp/2;
        int thischrlen = (int)this.chrlen[this.chridx[i]]+this.nmar[this.chridx[i]]*xsp-1;
        for(int j=0; j<this.indidx.length; j++) {
          this.g2.drawLine(x, y, x+thischrlen-1, y);
          y = y + (this.YSPACING+ysp);
        }
        x = x + thischrlen + this.SPACING;
      }
    }
    // plotting genotypes/missing/crossover/errorlod
    int[] markerCounts = this.cross.getNumberOfMarkers();
    if(this.whattoplot == 0) {
      // loop thru chromosomes
      for(int i=0; i<this.chridx.length; i++) {
        int thischr = this.chridx[i];
        CrossChromosome currChromo = this.cross.getGenotypeData().get(thischr);
        List<NamedCategoricalData> currGenotypes =
            currChromo.getMarkerGenotypes();
        // loop thru markers
        for(int j=0; j<markerCounts[thischr]; j++) {
          if(this.inMarkDist) {
            x = xoffset + colidx*xsp + (int)this.mpos[i][j];
          }
          else
            x = colidx*xsp + xoffset;
          // loop thru individuals
          for(int k=0; k<this.indidx.length; k++) {
            int thisind = this.indidx[k];
            if(this.inMarkDist)
              y = GenoPlot.TOP+1+(ysp+this.YSPACING)*k;
            else
              y = k*ysp + 1 + GenoPlot.TOP;
            Number data = currGenotypes.get(j).getData().get(this.sortIdx[thisind]);
            if(data == null)
              this.g2.setColor(this.MissingColor);
            else
              this.g2.setColor(this.GenoColor[data.intValue()]);
            this.g2.fillRect(x, y, this.XSPACE, this.YSPACE);
          }
          colidx ++;
        }
        xoffset = xoffset + this.SPACING;
        if(this.inMarkDist)
          xoffset = xoffset + (int)this.chrlen[thischr] - 1;
        this.xregion[i][1] = x + this.XSPACE;
        if(i != this.chridx.length-1)
          this.xregion[i+1][0] = x + this.XSPACE + this.SPACING;
      }
    }
    else if(this.whattoplot == 1) { // for plotting crossovers
      if(this.inMarkDist) { // in real marker distance
        // plot "/" for single crossover and "X" for double crossover
        // loop thru chromosomes
        for(int i=0; i<this.chridx.length; i++) {
          int thischr = this.chridx[i];
          colidx ++;
          // loop thru markers
          CrossChromosome currChromo = this.cross.getGenotypeData().get(thischr);
          List<NamedCategoricalData> currGenotypes =
              currChromo.getMarkerGenotypes();
          for(int j=1; j<markerCounts[thischr]; j++) {
            x = xoffset + colidx*xsp + (int)this.mpos[i][j];
            // loop thru individuals
            for(int k=0; k<this.indidx.length; k++) {
              int thisind = this.indidx[k];
              Number previous = currGenotypes.get(j - 1).getData().get(this.sortIdx[thisind]);
              Number current = currGenotypes.get(j).getData().get(this.sortIdx[thisind]);
              if(current != null && previous != null && !current.equals(previous))
              {
                  this.g2.setColor(Color.BLACK);
                  y = GenoPlot.TOP+1+(ysp+this.YSPACING)*k;
                  
                  if(Math.abs(previous.intValue()-current.intValue()) == 1) //single crossover
                  {
                      drawSlash(x,y);
                  }
                  else // double crossover
                  {
                      drawCross(x,y);
                  }
              }
            }
            colidx ++;
          }
          xoffset = xoffset + this.SPACING + (int)this.chrlen[thischr] - 1;
          this.xregion[i][1] = x + this.XSPACE;
          if(i != this.chridx.length-1)
            this.xregion[i+1][0] = x + this.XSPACE + this.SPACING;
        }
      }
      else{      // if not in real marker distance
        // loop thru chromosomes
        for(int i=0; i<this.chridx.length; i++) {
          int thischr = this.chridx[i];
          // loop thru markers
          colidx ++;
          CrossChromosome currChromo = this.cross.getGenotypeData().get(thischr);
          List<NamedCategoricalData> currGenotypes =
              currChromo.getMarkerGenotypes();
          for(int j=1; j<markerCounts[thischr]; j++) {
            x = colidx*xsp + xoffset;
            // loop thru individuals
            for(int k=0; k<this.indidx.length; k++) {
              int thisind = this.indidx[k];
              Number previous = currGenotypes.get(j - 1).getData().get(this.sortIdx[thisind]);
              Number current = currGenotypes.get(j).getData().get(this.sortIdx[thisind]);
              if(current != null && previous != null && !current.equals(previous))
              {
                  y = k*ysp + 1 + TOP;
                  // plot previous and current
                  this.g2.setColor(this.GenoColor[previous.intValue()]);
                  this.g2.fillRect(x-xsp, y, this.XSPACE, this.YSPACE);
                  this.g2.setColor(this.GenoColor[current.intValue()]);
                  this.g2.fillRect(x, y, this.XSPACE, this.YSPACE);
              }
            }
            colidx ++;
          }
          xoffset = xoffset + this.SPACING;
          this.xregion[i][1] = x + this.XSPACE;
          if(i != this.chridx.length-1)
            this.xregion[i+1][0] = x + this.XSPACE + this.SPACING;
          // plot a box for this chromosome if it's not in real marker distance
          this.g2.setColor(Color.lightGray);
          this.g2.drawRect((int)this.xregion[i][0], TOP,
                      (int)(this.xregion[i][1]-this.xregion[i][0]), ysp*this.nind);
        }
      }
    }
    else if(this.whattoplot == 2) { // plot missing values only
      // loop thru chromosomes
      for(int i=0; i<this.chridx.length; i++) {
        int thischr = this.chridx[i];
        CrossChromosome currChromo = this.cross.getGenotypeData().get(thischr);
        List<NamedCategoricalData> currGenotypes =
            currChromo.getMarkerGenotypes();
        // loop thru markers
        for(int j=0; j<markerCounts[thischr]; j++) {
          if(this.inMarkDist) {
            x = xoffset + colidx*xsp + (int)this.mpos[i][j];
          }
          else
            x = colidx*xsp + xoffset;
          // loop thru individuals
          for(int k=0; k<this.indidx.length; k++) {
            int thisind = this.indidx[k];
            Number data = currGenotypes.get(j).getData().get(this.sortIdx[thisind]);
            if(data == null) {
              this.g2.setColor(this.MissingColor);
              if(this.inMarkDist)
                y = TOP+1+(ysp+this.YSPACING)*k;
              else
                y = k*ysp + 1 + TOP;
              this.g2.fillRect(x, y, this.XSPACE, this.YSPACE);
            }
          }
          colidx ++;
        }
        xoffset = xoffset + this.SPACING;
        if(this.inMarkDist)
          xoffset = xoffset + (int)this.chrlen[thischr] - 1;
        this.xregion[i][1] = x + this.XSPACE;
        if(i != this.chridx.length-1)
          this.xregion[i+1][0] = x + this.XSPACE + this.SPACING;
        // plot a box for this chromosome if it's not in real marker distance
        if(!this.inMarkDist) {
          this.g2.setColor(Color.lightGray);
          this.g2.drawRect((int)this.xregion[i][0], TOP,
                      (int)(this.xregion[i][1]-this.xregion[i][0]), ysp*this.nind);
        }
      }
    }
    else if(this.whattoplot == 3) { // plot error lod
      Color col;
      
      // calculate the error lods if they haven't been calculated yet.
      if(!this.cross.getErrorLodsExist())
      {
          this.cross.calculateErrorLods();
      }
      
      // loop thru chromosomes
      for(int i=0; i<this.chridx.length; i++) {
        int thischr = this.chridx[i];
        CrossChromosome currChromo = this.cross.getGenotypeData().get(thischr);
        List<NamedRealData> currErrorLods = currChromo.getMarkerErrorLods();
        // loop thru markers
        for(int j=0; j<markerCounts[thischr]; j++) {
          if(this.inMarkDist) {
            x = xoffset + colidx*xsp + (int)this.mpos[i][j];
          }
          else
            x = colidx*xsp + xoffset;
          // loop thru individuals
          for(int k=0; k<this.indidx.length; k++) {
            double data =
                currErrorLods.get(j).getRealNumericalData()[k].doubleValue();
            // find the color
            col = this.errorlod_colors[0];
            for(int ii=this.errorlod_breaks.length-2; ii>0; ii--) {
              if(data > this.errorlod_breaks[ii]) {
                col = this.errorlod_colors[ii];
                break;
              }
            }
            this.g2.setColor(col);
            if(this.inMarkDist)
              y = TOP+1+(ysp+this.YSPACING)*k;
            else
              y = k*ysp + 1 + TOP;
            this.g2.fillRect(x, y, this.XSPACE, this.YSPACE);
          }
          colidx ++;
        }
        xoffset = xoffset + this.SPACING;
        if(this.inMarkDist)
          xoffset = xoffset + (int)this.chrlen[thischr] - 1;
        this.xregion[i][1] = x + this.XSPACE;
        if(i != this.chridx.length-1)
          this.xregion[i+1][0] = x + this.XSPACE + this.SPACING;
        // plot a box for this chromosome if it's not in real marker distance
        if(!this.inMarkDist) {
          this.g2.setColor(Color.lightGray);
          this.g2.drawRect((int)this.xregion[i][0], TOP,
                      (int)(this.xregion[i][1]-this.xregion[i][0]), ysp*this.nind);
        }
      }
    }
    // draw title and chrmosome ID
    this.g2.setColor(Color.black);
    drawTitle();
    drawChrID();
  }

  // function to plot a slash and a cross - these are called by doGenoPlot
  // the size of the cross depends on XSPACE and YSPACE
  private void drawSlash(int x, int y) {
    this.g2.drawLine(x, y, x+this.XSPACE, y+this.YSPACE);
  }

  private void drawCross(int x, int y) {
    this.g2.drawLine(x, y, x+this.XSPACE, y+this.YSPACE);
    this.g2.drawLine(x, y+this.YSPACE, x+this.XSPACE, y);
  }

  // function to calculate the plot region
  private void calcPlotRegion() {
    if(this.inMarkDist) { // in real marker distance
      if(this.interactive) {
        this.WIDTH = this.NCOL*(this.XSPACE+1) + LEFT + RIGHT + this.SPACING*(this.chridx.length-1) + 1;
        this.HEIGHT = this.NROW*(this.YSPACE+this.YSPACING+1) + TOP + BOTTOM + 1;
        // plus the marker length
        for(int i=0; i<this.chridx.length; i++) {
          this.WIDTH = this.WIDTH + (int)this.chrlen[this.chridx[i]];
        }
      }
      else { // non-interactive plot
        this.WIDTH = this.NCOL*this.XSPACE + LEFT + RIGHT + this.SPACING*(this.chridx.length-1) + 1;
        this.HEIGHT = this.NROW*(this.YSPACE+this.YSPACING) + TOP + BOTTOM + 1;
        // plus the marker length
        for(int i=0; i<this.chridx.length; i++) {
          this.WIDTH = this.WIDTH + (int)this.chrlen[this.chridx[i]];
        }
      }
    }
    else {
      if(this.interactive) {
        this.WIDTH = this.NCOL*(this.XSPACE+1) + LEFT + RIGHT + this.SPACING*(this.chridx.length-1) + 1;
        this.HEIGHT = this.NROW*(this.YSPACE+1) + TOP + BOTTOM + 1;
      }
      else {
        this.WIDTH = this.NCOL*this.XSPACE + LEFT + RIGHT + this.SPACING*(this.chridx.length-1) + 1;
        this.HEIGHT = this.NROW*this.YSPACE + TOP + BOTTOM + 1;
      }
    }
    this.plotRight = this.WIDTH - RIGHT - 1;
    this.plotBottom = this.HEIGHT - BOTTOM - 1;
  }

//  /* zoom in and out functions */
//  public void ZoomIn() {
//    if(this.XSPACE<=MAXELESIZE) {
//      this.XSPACE = this.XSPACE + 2;
//      this.YSPACE = this.YSPACE + 2;
//      this.SPACING = (this.XSPACE+1) * 2;
//      // save the setting
//      this.GenoPlotProperties.put(FigureProperties.GENOPLOT_XSIZE, new Integer(this.XSPACE));
//      this.GenoPlotProperties.put(FigureProperties.GENOPLOT_YSIZE, new Integer(this.YSPACE));
//      // redraw the figure
//      this.reDraw();
//    }
//  }

//  @SuppressWarnings("unchecked")
//private void ZoomOut() {
//    if(this.XSPACE>=MINELESIZE) {
//      this.XSPACE = this.XSPACE - 2;
//      this.YSPACE = this.YSPACE - 2;
//      this.SPACING = (this.XSPACE+1) * 2;
//      // save the setting
//      this.GenoPlotProperties.put(FigureProperties.GENOPLOT_XSIZE, new Integer(this.XSPACE));
//      this.GenoPlotProperties.put(FigureProperties.GENOPLOT_YSIZE, new Integer(this.YSPACE));
//      // redraw the figure
//      this.reDraw();
//    }
//  }

/*  public void drawColorBox(Graphics g, Color color, int idx) {
    int xcoord = (col[idx]-1) * (XSPACE+1) + LEFT;
    int ycoord = (row[idx]-1) * (YSPACE+1) + TOP;
    g.setColor(color);
    g.drawRect(xcoord, ycoord, XSPACE+1, YSPACE+1);
  }
*/
/*  public void drawColorBox(Graphics g, Color color, int x, int y) {
    boolean draw = true;
    int c, r, xcoord=0, ycoord=0;
    g.setColor(color);
    int xsp, ysp;
    if(interactive) {
      xsp = XSPACE + 1;
      ysp = YSPACE + 1;
    }
    else {
      xsp = XSPACE;
      ysp = YSPACE;
    }
    if(inMarkDist) { // in real marker distance
      // y-axis, this is easier
      r = (y-TOP) / (ysp+YSPACING);
      ycoord = r*(ysp+YSPACING) + TOP;
      // x-axis, this is troubler
      int xres = x;
      int x_chr=0, x_mar=0;
      // find which chromosome it's on
      for(int i=0; i<chridx.length; i++) {
        if((x>xregion[i][0]) && (x<xregion[i][1])) {
          x_chr = i;
          break;
        }
      }
      // find which marker it's on
      int start=LEFT, end=start+xsp;
      int thischrnmar = nmar[chridx[x_chr]];
      for(int i=0; i<thischrnmar; i++) {
        if( (x>=start) && (x<=end) ) {
          xcoord = start;
          break;
        }
        if(i<thischrnmar-1) {
          start = end + (int)mpos[x_chr][i+1];
          end = start + xsp;
        }
      }
    }
    else { // not on real marker distance. This is much easier
      c = (x-LEFT) / xsp;
      r = (y-TOP) / ysp;
      xcoord = c*xsp + LEFT;
      ycoord = r*ysp + TOP;
    }
    g.drawRect(xcoord, ycoord, xsp, ysp);
  }
*/

  // show marker and individual name
  private void showTip() {
      // TODO add me back
//    int thischridx, markeridx, thisindidx;
//    thischridx = chridx[thisChrIdx];
//    markeridx = thisMarIdx;
//    thisindidx = sortIdx[indidx[thisIndIdx]];
//    CrossChromosome chr = cross.getGenotypeData().get(thischridx);
//    String mname = chr.get.getMarkerName(markeridx);
//    String msg="";
//    // make the tips according to what to plot
//    // I will show genotypes if not plotting errorlod
//    if(whattoplot != 3) { // plot geno
//      // genotype
//      double geno = chr.getGenoData().get(thisindidx, markeridx);
//      String genoStr = "";
//      if(Double.isNaN(geno))
//        genoStr = "Missing";
//      else {
//        String[] genocode = cross.getGenoCode();
//        genoStr = "" + genocode[(int)geno-1];
//      }
//      msg = "<html>Marker Name: " + mname + "<p>Individual Index: " +
//                   (thisindidx+1) + "<p>Genotype: " + genoStr + "</html>";
//    }
//    else if(whattoplot == 3) { // errorlod
//      DecimalFormat myFormatter = new DecimalFormat("#########0.000");
//      double errorlod = chr.getErrorLodD().get(thisindidx, markeridx);
//      msg = "<html>Marker Name: " + mname + "<p>Individual Index: " +
//                   (thisindidx+1) + "<p>Error LOD: " +
//                   myFormatter.format(errorlod) + "</html>";
//    }
//    setToolTipText(msg);
  }

  // plot highlight the selected marker
  private void highLightBox(int x, int y) {
    this.inPlotRegion = true;
    int start=0, end; //c, r, xcoord=0, ycoord=0,
    int xsp, ysp;
    Graphics g = getGraphics();
    // box size
    if(this.interactive) {
      xsp = this.XSPACE + 1;
      ysp = this.YSPACE + 1;
    }
    else {
      xsp = this.XSPACE;
      ysp = this.YSPACE;
    }

    // see if it's in a valid plot region
    if(y<=this.plotTop) this.inPlotRegion = false;
    else if(y>=this.plotBottom) this.inPlotRegion = false;
    else if(x<=this.plotLeft) this.inPlotRegion = false;
    else if(x>=this.plotRight) this.inPlotRegion = false;

    // if not in real marker distance
    if(!this.inMarkDist) {
      // (x,y) must be within a chromosome
      if(this.inPlotRegion) {
        this.inPlotRegion = false;
        for(int i=0; i<this.chridx.length; i++) {
          if( (x>this.xregion[i][0]) && (x<this.xregion[i][1]) ) {
            this.thisChrIdx = i;
            this.inPlotRegion = true;
            break;
          }
        }
        // calculate the indices for this marker and this individual
        this.thisIndIdx = (y-TOP) / (ysp+this.YSPACING);
        this.thisMarIdx = (int)((x-this.xregion[this.thisChrIdx][0])/xsp);
      }
    }
    else { // in real marker distance
      if(this.inPlotRegion) {
        // if in real marker distance, must be within an individual
        int res;
        res = Matlab.mod(y-TOP, ysp+this.YSPACING);
        if(res>ysp)
          this.inPlotRegion = false;
        else
          this.thisIndIdx = ((y-TOP)/(ysp+this.YSPACING));
      }
      if(this.inPlotRegion) {
        this.inPlotRegion = false;
        // this also must be on a marker
        //      int x_chr=0, x_mar=0;
        // find which chromosome it's on
        for(int i=0; i<this.chridx.length; i++) {
          if((x>this.xregion[i][0]) && (x<this.xregion[i][1])) {
            this.thisChrIdx = i;
            break;
          }
        }
        // find which marker it's on
        // the calculation of xregion is a little off, I have to offset it here
        int start0 = (int)this.xregion[this.thisChrIdx][0] - 1;
        start=start0;
        end=start+xsp;
        int thischrnmar = this.nmar[this.chridx[this.thisChrIdx]];
        for(int i=0; i<thischrnmar; i++) {
          if( (x>=start) && (x<=end) ) {
            this.thisMarIdx = i;
            this.inPlotRegion = true;
            break;
          }
          if(i<thischrnmar-1) {
            start = start0 + (i+1)*xsp + (int)this.mpos[this.thisChrIdx][i+1];
            end = start + xsp;
          }
        }
      }
    }

    // overwrite the black box
    if( (this.currentx!=0) || (this.currenty!=0) ) {
      g.setColor(Color.white);
      g.drawRect(this.currentx, this.currenty, xsp, ysp);
    }

    if(!this.inPlotRegion) {    // if it's not in a valid plot region
      //reset currentx and currenty
      this.currentx = 0; this.currenty = 0;
    }
    else { // it's in a valid plot region
      // calculate currentx and currenty
      if(this.inMarkDist) {
        this.currentx = start;
        this.currenty = this.thisIndIdx*(ysp+this.YSPACING) + TOP;
      }
      else {
        int c = (x-LEFT) / xsp;
        this.currentx = c*xsp + LEFT;
//        currentx = (int)xregion[thisChrIdx][0] + thisMarIdx*xsp;
        this.thisIndIdx = (y-TOP) / ysp;
        this.currenty = this.thisIndIdx*ysp + TOP;
      }
      // plot a black box and set currentx and currenty
      g.setColor(Color.black);
      g.drawRect(this.currentx, this.currenty, xsp, ysp);
    }
  }

/*  private boolean inPlotRegion(int x, int y) {
    if(y<plotTop) return false;
    else if(y>plotBottom) return false;
    else if(x<plotLeft) return false;
    else if(x>plotRight) return false;

    // (x,y) must be within a chromosome
    boolean flag = false;
    for(int i=0; i<chridx.length; i++) {
      if( (x>xregion[i][0]) && (x<xregion[i][1]) ) {
        flag = true;
        break;
      }
    }
    if(!flag) return false;

    // if in real marker distance, must be within an individual
    if(inMarkDist) {
      int res;
      int xsp, ysp;
      if(interactive) {
        xsp = XSPACE + 1;
        ysp = YSPACE + 1;
      }
      else {
        xsp = XSPACE;
        ysp = YSPACE;
      }
      res = Matlab.mod(y-TOP, ysp+YSPACING);
      if(res>ysp)
        return false;

      // this also must be on a marker
      int xres = x;
      int x_chr=0, x_mar=0;
      // find which chromosome it's on
      for(int i=0; i<chridx.length; i++) {
        if((x>xregion[i][0]) && (x<xregion[i][1])) {
          x_chr = i;
          break;
        }
      }
      // find which marker it's on
      int start=LEFT, end=start+xsp;
      int thischrnmar = nmar[chridx[x_chr]];
      for(int i=0; i<thischrnmar; i++) {
        if( (x>=start) && (x<=end) ) {
          return false;
        }
        if(i<thischrnmar-1) {
          start = end + (int)mpos[x_chr][i+1];
          end = start + xsp;
        }
      }
    }
    return true;
  }
*/

  // draw chromosome IDs
  private void drawChrID() {
    Font f = new Font("SansSerif", Font.BOLD, 12);
    this.g2.setFont(f);
    FontMetrics fim = this.g2.getFontMetrics(f);
    int w;
//    String chrid;
//    for(int i=0; i<chridx.length; i++) {
//      chrid = cross.getChr(chridx[i]).getChrID();
//      w = fim.stringWidth(chrid);
//      int cx = (int)((xregion[i][0]+xregion[i][1]-w) / 2);
//      g2.drawString(chrid, cx, TOP-12);
//    }
    List<CrossChromosome> chromosomes = this.cross.getGenotypeData();
    for(int i = 0; i < this.chridx.length; i++)
    {
        String chromosomeName = chromosomes.get(this.chridx[i]).getChromosomeName();
        w = fim.stringWidth(chromosomeName);
        int cx = (int)((this.xregion[i][0]+this.xregion[i][1]-w) / 2);
        this.g2.drawString(chromosomeName, cx, TOP-12);
    }
  }

  // draw title
  private void drawTitle() {
    Font f = new Font("SansSerif", Font.BOLD, 14);
    this.g2.setFont(f);
    // find title position
    FontMetrics fim = this.g2.getFontMetrics(f);
    int w = fim.stringWidth(this.title);
    int cx = (this.WIDTH-w) / 2;
    this.g2.drawString(this.title, cx, TOP-40);
  }

  /**
   * Redraw the plot
   */
  public void reDraw() {
    getFigureProperties();
    // sort index
    sort(this.sortbyidx);
    // re-calculate plot region
    calcPlotRegion();
    setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
    // recreate bufferedimage
    createBufferedImage();
    // add mouse listeners if it's interactive plot
    if(this.interactive) {
      if(this.getMouseMotionListeners().length == 0)
        addMouseListener(this.mouselistener);
        addMouseMotionListener(this);
    }
    else {
      if(this.getMouseMotionListeners().length > 0)
        this.removeMouseMotionListener(this);
        this.removeMouseListener(this.mouselistener);
    }
    this.repaint();
    this.revalidate();
  }

  /**
   * {@inheritDoc}
   */
  public void mouseDragged(MouseEvent event) {}
  
  /**
   * {@inheritDoc}
   */
  public void mouseMoved(MouseEvent event) {
    // currently not working for crossover on real marker distance
    if( (this.whattoplot==1) && this.inMarkDist) {
      ToolTipManager.sharedInstance().setEnabled(false);
      return;
    }

    // highlight the pointed marker for an individual
    highLightBox(event.getX(), event.getY());
    // show marker and individual name if in plot region
    if(this.inPlotRegion) {
      ToolTipManager.sharedInstance().setEnabled(true);
      showTip();
    }
    else // disable tooltip
      ToolTipManager.sharedInstance().setEnabled(false);

  }

  // inner class to handle mouse event
  private class MyMouseListener extends MouseAdapter {
    @Override
    public void mouseReleased(MouseEvent event) {
      // if it's popup menu trigger, display a popup menu
      if(event.isPopupTrigger()) {
        // TODO add back effect plot and MGD link capability
//        RightClickMenu pop = new RightClickMenu(event, new MyActionListener());
//        if(XSPACE >= MAXELESIZE)
//          pop.setEnableMenuItem(ActionManager.GENOPLOT_ZOOMIN_ACTION, false);
//        if(XSPACE <= MINELESIZE)
//          pop.setEnableMenuItem(ActionManager.GENOPLOT_ZOOMOUT_ACTION, false);
//        pop.show(event.getComponent(), event.getX(), event.getY());
      }
      else {
        if(GenoPlot.this.interactive) {
          if( GenoPlot.this.inPlotRegion ) {
            // if this is left click, show a dialog box for spot info
            // if the current figure is interactive
            SpotInfoDialogBox d = new SpotInfoDialogBox();
            d.setVisible(true);
          }
        }
      }
    }

    // for unix system
    @Override
    public void mousePressed(MouseEvent event) {
        // TODO same as above todo
//      if(event.isPopupTrigger()) { // this is a pop up menu trigger
//        ActionManager manager = qtl.mainFrame.getActionManager();
//        RightClickMenu pop = new RightClickMenu(event, new MyActionListener());
//        if(XSPACE >= MAXELESIZE)
//          pop.setEnableMenuItem(ActionManager.GENOPLOT_ZOOMIN_ACTION, false);
//        if(XSPACE <= MINELESIZE)
//          pop.setEnableMenuItem(ActionManager.GENOPLOT_ZOOMOUT_ACTION, false);
//        pop.show(event.getComponent(), event.getX(), event.getY());
//      }
    }
  }

  // inner class for handle zoom in/out from right click menu
//  private class MyActionListener implements ActionListener {
//    public void actionPerformed(ActionEvent event) {
//      String command = event.getActionCommand();
//      if(command.equals(ActionManager.GENOPLOT_ZOOMIN_ACTION))
//        ZoomIn();
//      else if(command.equals(ActionManager.GENOPLOT_ZOOMOUT_ACTION))
//        ZoomOut();
//    }
//  }

  // inner class for spot info dialog box
  private class SpotInfoDialogBox extends JDialog implements ActionListener {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 466801490922375037L;
    JEditorPane infoDisplayTextPane;
    int thischridx, markeridx, thisindidx;
    String infoText;
    // constructor
    public SpotInfoDialogBox() {
      super(QTL.getInstance().getApplicationFrame(), "Spot Information", true);
      this.thischridx = GenoPlot.this.chridx[GenoPlot.this.thisChrIdx];
      this.markeridx = GenoPlot.this.thisMarIdx;
      this.thisindidx = GenoPlot.this.sortIdx[GenoPlot.this.indidx[GenoPlot.this.thisIndIdx]];

      // make the dialog box
      this.infoDisplayTextPane = new JEditorPane();
      this.infoDisplayTextPane.setContentType("text/html");
      Font infoDisplayFont = new Font("monospaced", Font.PLAIN, 10);
      this.infoDisplayTextPane.setFont(infoDisplayFont);
      this.infoDisplayTextPane.setEditable(false);
      makeInfoText();
      this.infoDisplayTextPane.setText(this.infoText);

      // make a scroll pane
      JPanel p = new JPanel(new BorderLayout());
      p.add(this.infoDisplayTextPane, BorderLayout.CENTER);
      p.setBackground(Color.white);
//    p.setPreferredSize(new Dimension(500, 480));
      JScrollPane infoScrollPane = new JScrollPane(p);
//    infoScrollPane.add(infoDisplayTextPane);
//    infoScrollPane.add(profile);
      infoScrollPane.setPreferredSize(new Dimension(450, 400));
  /*    JPanel p = new JPanel();
      p.add(infoScrollPane, "North");
      p.add(profile, "South") */
      JButton okButton = new JButton("Ok");
      okButton.addActionListener(this);
      okButton.setPreferredSize(new Dimension(80, 30));
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(okButton);
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

      getContentPane().add(infoScrollPane, "North");
      getContentPane().add(buttonPanel, "South");

      // center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getPreferredSize();
      setLocation((screenSize.width - frameSize.width) / 2,
                  (screenSize.height - frameSize.height) / 2);
      pack();
    }

    public void actionPerformed(ActionEvent e) {
      dispose();
    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
        dispose();
      }
      super.processWindowEvent(e);
    }

    // make info text
    private void makeInfoText() {
      CrossChromosome chr = GenoPlot.this.cross.getGenotypeData().get(this.thischridx);
      
      // TODO this should be sensitive to sex specific maps
      List<GeneticMarker> markerPositions =
          chr.getAnyGeneticMap().getMarkerPositions();
      NamedCategoricalData selectedMarkerGenotypes = chr.getMarkerGenotypes().get(
              this.markeridx);
      NamedDataMatrix<Number> phenotypeData = GenoPlot.this.cross.getPhenotypeData();
      
      // genotype info
      this.infoText =
          "<center><H2>Genotype Information</H2></center>" +
          "<table border=1 width=400 align=center>";
      // chr number
      this.infoText +=
          "<tr><td>Chromosome</td><td>" + chr.getChromosomeName() + "</td></tr>";
      // marker name
      this.infoText +=
          "<tr><td>Marker Name</td><td>" +
          chr.getMarkerNames()[this.markeridx] + "</td></tr>";
      // marker position
      this.infoText +=
          "<tr><td>Marker Position (cM)</td><td>" +
          markerPositions.get(this.markeridx).getMarkerPositionCentimorgans() +
          "</td></tr>";
      // genotype
      String genoStr = selectedMarkerGenotypes.integerToCategoryString(
              (Integer)selectedMarkerGenotypes.getData().get(this.thisindidx));
      
      this.infoText += "<tr><td>Genotype</td><td>" + genoStr + "</td></tr>";
      
      // error LOD
      if(chr.getErrorLodsExist())
      {
          Number errorLods =
              chr.getMarkerErrorLods().get(this.markeridx).getData().get(this.thisindidx);
          if(errorLods != null)
          {
              this.infoText =
                  this.infoText + "<tr><td>Error LOD</td><td>" +
                  errorLods.toString() + "</td></tr>";
          }
      }
      
      this.infoText += "</table>";
      
      // individual info
      this.infoText =
          this.infoText + "<center><H2>Phenotype Information</H2></center>" +
          "<table border=1 width=400 align=center>";
      List<NamedData<Number>> phenotypeDataList = phenotypeData.getNamedDataList();
      for(NamedData<Number> namedData: phenotypeDataList)
      {
          String currStringValue;
          if(namedData instanceof NamedCategoricalData)
          {
              // if it's categorical data use the category name
              NamedCategoricalData namedCategoricalData =
                  (NamedCategoricalData)namedData;
              currStringValue = namedCategoricalData.getCategoryStringAt(
                      this.thisindidx);
              if(currStringValue == null)
              {
                  currStringValue = "Missing";
              }
          }
          else
          {
              Number currValue = namedData.getData().get(this.thisindidx);
              currStringValue = currValue == null ? "Missing" : currValue.toString();
          }
          this.infoText =
              this.infoText + "<tr><td>" + namedData.getNameOfData() + "</td><td>" +
              currStringValue + "</td></tr>";
      }
      this.infoText = this.infoText + "</table>";
    }
  }
}
