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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.gui.SaveGraphImageAction;

/**
 * The panel that holds the genotype plot
 * @see GenoPlot
 * @author Hao Wu
 * @author Keith Sheppard (minor modifications to integrate with J/qtl 1.0)
 */
@SuppressWarnings("all")
public class GenoPlotPanel extends JPanel implements ActionListener {
  /**
     * 
     */
    private static final long serialVersionUID = 1007933136535351682L;
private Cross cross;
  private boolean inMarkDist;
  // ui stuff
  private Hashtable genoPlotProperties;
  private JPanel headerPanel;
  private GenoPlot plotPanel;
  private JScrollPane plotPane;
  private JComboBox sortList, plotList;
  private JCheckBox interactivePlot, cbInMarkDist;
  private JList chrList;
  private JScrollPane chrScrollPane; // chromosome list
  private JTextField tfInd;  // for individuals
  private JButton plotParameters; // some parameters for plot
  private JButton reDrawButton;

  //layout
  private GridBagConstraints gbc;

  // set and get genoplot properties
  public void setProperty(Object key, Object value) {
    this.genoPlotProperties.put(key, value);
  }
  public Hashtable getProperty() {return this.genoPlotProperties;}

  public GenoPlot getGenoPlot() {return this.plotPanel;}

  // constructor
  public GenoPlotPanel(Cross c, Hashtable properties) {
    this.genoPlotProperties = properties;
    // get some properties
    this.inMarkDist = ((Boolean)this.genoPlotProperties.get(FigureProperties.GENOPLOT_IN_MARKER_DIST)).booleanValue();
    this.cross = c;
    setLayout(new BorderLayout());
    // make two panels
    this.plotPanel = new GenoPlot(c, this.genoPlotProperties);
    makeHeaderPanel();
    this.plotPane = new JScrollPane();
    this.plotPane.getViewport().add(this.plotPanel);
    // put them together
    add(this.headerPanel, BorderLayout.NORTH);
    add(this.plotPane, BorderLayout.CENTER);
  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();

    // regenerate the plot if click redraw button
    if(src == this.reDrawButton) {
      // ====== get the figure properties =========
      // sortby
      setProperty(FigureProperties.GENOPLOT_SORTBY,
                  new Integer(this.sortList.getSelectedIndex()));
      //things to plot
      setProperty(FigureProperties.GENOPLOT_WHAT,
                  new Integer(this.plotList.getSelectedIndex()));
      //plot in marker distance
      setProperty(FigureProperties.GENOPLOT_IN_MARKER_DIST,
                  new Boolean(this.cbInMarkDist.isSelected()));
      // chromosome list
      int[] itmp = this.chrList.getSelectedIndices();
      if(itmp[0] == 0) { // chose "all"
        setProperty(FigureProperties.GENOPLOT_CHROM, "all");
      }
      else {// choose some chromosomes
        int[] chridx = new int[itmp.length];
        for(int i=0; i<itmp.length; i++)
          chridx[i] = itmp[i]-1;
        setProperty(FigureProperties.GENOPLOT_CHROM, chridx);
      }
      // individual list - this need some thinking, I'll put it in anyway
      // the text will be converted into all lower case letters
      String indStr = this.tfInd.getText().toLowerCase();
      if(indStr.equals("all")) // all
        setProperty(FigureProperties.GENOPLOT_IND, indStr);
      else {
        // something else - the valid syntax will be "1:30, 50:60"
        // means to plot individuals 1 to 30 and 50 to 60
        // --------------------------------------------------------
        // note that this is the individual *INDEX* after sorting.
        // I might need to change them to the actually individual
        // index. But I think to use sorting-based index makes more sense
        //  Hao Wu Mar 21, 2005
        // -----------------------------------------------------------
        int nind = 0; // number of individuals
        // allocate memory, trim later
        int[] indidx = new int[this.cross.getNumberOfIndividuals()];
        String tmpStr;
        // parse the string
        StringTokenizer t1, t2;
        t1 = new StringTokenizer(indStr, ",");
        try{
          do {
            tmpStr = t1.nextToken();
            t2 = new StringTokenizer(tmpStr, ":");
            // t2 should have one or two tokens
            if(t2.countTokens() == 1) {
              indidx[nind] = Integer.parseInt(tmpStr)-1;
              nind ++;
            }
            else if(t2.countTokens() == 2) {
              int start = Integer.parseInt(t2.nextToken().trim());
              int end = Integer.parseInt(t2.nextToken().trim());
              for(int i=start-1; i<end; i++) {
                indidx[nind] = i;
                nind ++;
              }
            }
            else { // error
              String msg = "Error in Individual index - \"" + tmpStr + "\"";
              JOptionPane.showMessageDialog(this, msg,
                  "Individual Index Error",
                  JOptionPane.ERROR_MESSAGE);
            }
          } while(t1.hasMoreTokens());
          // trim indidx
          int[] tmp = new int[nind];
          System.arraycopy(indidx, 0, tmp, 0, nind);
          setProperty(FigureProperties.GENOPLOT_IND, tmp);
        }
        catch(Exception ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(this, "Error happened",
                                        "Individual Index Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
        // redraw the figure
      if(this.plotPanel != null) {
        this.plotPanel.reDraw();
      }
    }
    else if(src == this.plotParameters) { // plot parameter button
      // what to plot
      int plotidx = this.plotList.getSelectedIndex();
      PlotParamDialog d = new PlotParamDialog(plotidx);
      d.setVisible(true);
    }
    if(src == this.plotList) { // change sort list
      if(this.plotParameters==null)
        return;
      int plotidx = this.plotList.getSelectedIndex();
      // no parameters for plotting crossover, missing or error lod
      this.plotParameters.setEnabled(plotidx == 0);
    }
/*    else if(src == plotList) { // change things to plot
      int plotidx = plotList.getSelectedIndex();
      boolean plotgeno = (plotidx==0);
      // enable/disable combobox and buttons according to plotgeno
      if(interactivePlot != null)
        interactivePlot.setEnabled(plotgeno);
      if(GenoColorButton != null) {
        for(int i=0; i<GenoColorButton.length; i++)
          if(GenoColorButton[i] != null)
            GenoColorButton[i].setEnabled(plotgeno);
      }
      setProperty(FigureProperties.GENOPLOT_WHAT, new Integer(plotidx));
      if(plotPanel != null) {
        plotPanel.reDraw();
      }
    }
    else if(src == interactivePlot) { // interactive plot
      boolean b = interactivePlot.isSelected();
      setProperty(FigureProperties.GENOPLOT_INT, new Boolean(b));
      if(plotPanel != null) {
        plotPanel.reDraw();
      }
    }
    else if(src instanceof JButton) {
      final int buttonIdx = Integer.valueOf(((JButton)src).getActionCommand()).intValue();
      Color defaultColor = ((JButton)src).getBackground();
      final JColorChooser chooser = new JColorChooser(defaultColor);
      //Color selected
      JDialog dialog = JColorChooser.createDialog(qtl.mainFrame, "test", true, chooser,
          new ActionListener()  { // for ok button
             public void actionPerformed(ActionEvent e) {
               setGenoColor(buttonIdx, chooser.getColor());
             }
          } ,
          null);
      dialog.show();
    } */
  }

  // function to set the color for a genotype
/*  private void setGenoColor(int idx, Color color) {
    Color[] genocolor = (Color[])GenoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
    genocolor[idx] = color;
    // save the property change
    setProperty(FigureProperties.GENOPLOT_PALETTE, genocolor);
    // set the button
    GenoColorButton[idx].setBackground(color);
    // redraw the figure
    plotPanel.reDraw();
  }
*/
  // function to make the control panel for genoplot
  private void makeHeaderPanel() {
    // start to build the header panel
    this.headerPanel = new JPanel();
    this.headerPanel.setBackground(Color.white);
    // set layout
    this.headerPanel.setLayout(new GridBagLayout());
    this.gbc = new GridBagConstraints();
    this.gbc.anchor = GridBagConstraints.WEST;
    this.gbc.fill = GridBagConstraints.BOTH;
    //gbc.ipadx = 30;
    this.gbc.insets = new Insets(3,5,3,5);

    // ========== sortby list ==============
    String[] pnames = this.cross.getPhenotypeData().getDataNames();
    String[] sortByPnames = new String[pnames.length+1];
    sortByPnames[0] = "Input Order";
    for(int i=1; i<pnames.length+1; i++ )
      sortByPnames[i] = pnames[i-1];
    // make a combo box for sort list
    this.sortList = new JComboBox(sortByPnames);
    this.sortList.setBackground(Color.white);
    this.sortList.addActionListener(this);
    int sortbyidx = ((Integer)this.genoPlotProperties.get(FigureProperties.GENOPLOT_SORTBY)).intValue();
    this.sortList.setSelectedIndex(sortbyidx);
    // set a border
//    sortList.setBorder(BorderFactory.createTitledBorder(
//        BorderFactory.createEmptyBorder(0, 0, 0, 0), "Sort By"));
    add(this.headerPanel, new JLabel("Sort By"), this.gbc, 0,0,1,1);
    add(this.headerPanel, this.sortList, this.gbc, 1,0,1,1);

    // ========== plot list ==============
    String[] whattoplot = {"Genotype", "Crossovers", "Missing data",
      "Genotyping Error"};
    this.plotList = new JComboBox(whattoplot);
    this.plotList.setBackground(Color.white);
    this.plotList.addActionListener(this);
    int plotidx = ((Integer)this.genoPlotProperties.get(FigureProperties.GENOPLOT_WHAT)).intValue();
    this.plotList.setSelectedIndex(plotidx);
    // set a border
//    plotList.setBorder(BorderFactory.createTitledBorder(
//        BorderFactory.createEmptyBorder(0, 0, 0, 0), "Things to plot"));
    add(this.headerPanel, new JLabel("Things to plot"), this.gbc, 0,1,1,1);
    add(this.headerPanel, this.plotList, this.gbc, 1,1,1,1);
    this.plotParameters = new JButton("Plot Colors");
    this.plotParameters.addActionListener(this);
    add(this.headerPanel, this.plotParameters, this.gbc, 2,1,1,1);

    // ========== individual list ==============
    add(this.headerPanel, new JLabel("Choose Individuals"), this.gbc, 0,2,1,1);
    add(this.headerPanel, this.tfInd=new JTextField("all"), this.gbc, 1,2,1,1);

    // ========== plot in real marker ==============
    this.cbInMarkDist = new JCheckBox("Plot in real marker distance");
    this.cbInMarkDist.setBackground(Color.white);
    this.cbInMarkDist.setSelected(this.inMarkDist);
    add(this.headerPanel, this.cbInMarkDist, this.gbc, 0,3,2,1);
    
    JButton exportImageButton = new JButton(
            new SaveGraphImageAction(this.plotPanel));
    this.gbc.fill = GridBagConstraints.NONE;
    this.add(
            this.headerPanel,
            exportImageButton,
            this.gbc,
            0, 4, 2, 1);
    this.gbc.fill = GridBagConstraints.BOTH;

    // ========== chromosome list ==============
    int nchr = this.cross.getNumberOfChromosomes();
    String[] chrs = new String[nchr+1];
    chrs[0] = "All";
    for(int i=1; i<=nchr; i++)
      chrs[i] = "Chromosome " + this.cross.getGenotypeData().get(i-1).getChromosomeName();
    this.chrList = new JList(chrs);
    this.chrList.setSelectedIndex(0);
    this.chrScrollPane = new JScrollPane(this.chrList);
    this.chrScrollPane.setPreferredSize(new Dimension(150, 85));
/*    chrScrollPane.setBackground(Color.white);
    chrScrollPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEmptyBorder(0, 0, 0, 0), "Choose Chromosome"));
//    add(headerPanel, new JLabel("Choose Chromosome"), gbc, 3,0,1,1); */
    add(this.headerPanel, this.chrScrollPane, this.gbc, 3,0,1,4);


    // ========== redraw button ==============
    this.reDrawButton = new JButton("Redraw");
    this.reDrawButton.addActionListener(this);
    add(this.headerPanel, this.reDrawButton, this.gbc, 3,4,1,1);
/*    // make buttons for choosing color
    String[] genocode = cross.getGenoCode();
    JPanel PalettePanel = new JPanel();
    PalettePanel.setBackground(Color.white);
    Color[] genocolor = (Color[])GenoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
    GenoColorButton = new JButton[genocolor.length];
    for(int i=0; i<genocolor.length; i++) {
      if(genocode[i].length() > 0) {
        GenoColorButton[i] = new JButton();
        GenoColorButton[i].setPreferredSize(new Dimension(25,25));
        GenoColorButton[i].setBackground(genocolor[i]);
        GenoColorButton[i].setActionCommand(""+i);
        GenoColorButton[i].addActionListener(this);
        PalettePanel.add(new JLabel(ReadDataDialogBox.genoNameString[i]));
        PalettePanel.add(GenoColorButton[i]);
      }
    }
    PalettePanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEmptyBorder(0, 30, 0, 0), "Choose Color"));
    headerPanel.add(PalettePanel);

    // checkbox for interactive plot
    interactivePlot = new JCheckBox("Interactive plot");
    boolean inter = ((Boolean)(GenoPlotProperties.get(FigureProperties.GENOPLOT_INT))).booleanValue();
    interactivePlot.setSelected(inter);
    interactivePlot.setBackground(Color.white);
    interactivePlot.addActionListener(this);
    headerPanel.add(interactivePlot);
*/
    // add an empty border
    this.headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
  }

  // for gridbaglayout
  private void add(Container mother, Component child, GridBagConstraints gbc, int x, int y,
                   int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    mother.add(child, gbc);
  }

  // Several internal classes for plot properties

  // for plot parameter
  private class PlotParamDialog extends JDialog implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -8508002897781827481L;
    private JPanel contentPanel, buttonPanel;
    private JButton okButton, cancelButton;
    private JButton[] GenoColorButton;
    private boolean ok=false;

    public boolean getOk() {return this.ok;}

    // constructor
    PlotParamDialog(int what) {
      super((JFrame)null, "Genotype plot properties");
      // make content panel
      makeContentPanel(what);
      // make button panel
      makeButtonPanel();
      // put together
      // make the dialog
      Container c = getContentPane();
      c.add(this.contentPanel, "North");
      c.add(this.buttonPanel, "South");

      // center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getPreferredSize();
      setLocation((screenSize.width - frameSize.width) / 2,
                  (screenSize.height - frameSize.height) / 2);

      pack();
    }

    // function to make button panel
    private void makeButtonPanel() {
      this.buttonPanel = new JPanel();
      this.okButton = new JButton("OK");
      this.okButton.addActionListener(this);
      this.buttonPanel.add(this.okButton);
      this.cancelButton = new JButton("Cancel");
      this.cancelButton.addActionListener(this);
      this.buttonPanel.add(this.cancelButton);
/*      JButton helpButton = new JButton("Help");
      helpButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              String title = "Calculate genotype probability";
              JDialog help = new HelpDialogBox("GenoPlotPanel", title,
                                               GenoPlotPanel.this);
              help.show();
          }
      });
      helpButton.setPreferredSize(new Dimension(80, 30));
      buttonPanel.add(helpButton);*/

      this.buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    }

    // function to make content panel
    private void makeContentPanel(int what) {
      this.contentPanel = new JPanel();
      if(what == 0) { // plot geno
       JColorChooser chooser;
       this.contentPanel.setBackground(Color.white);
       Color[] genocolor = (Color[])GenoPlotPanel.this.genoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
       this.GenoColorButton = new JButton[genocolor.length];
//       String[] genocode = cross.getGenoCode();
       String[] genocode = GenoPlotPanel.this.cross.getCrossSubType().getMarkerDataCategoricalValues();
       for(int i=0; i<genocolor.length; i++) {
           this.GenoColorButton[i] = new JButton();
           this.GenoColorButton[i].setPreferredSize(new Dimension(25,25));
           this.GenoColorButton[i].setBackground(genocolor[i]);
           this.GenoColorButton[i].setActionCommand(""+i);
           this.GenoColorButton[i].addActionListener(this);
           if(i < genocode.length)
           {
               this.contentPanel.add(new JLabel(genocode[i]));
           }
           else
           {
               this.contentPanel.add(new JLabel("Missing"));
           }
           this.contentPanel.add(this.GenoColorButton[i]);
       }
       this.contentPanel.setBorder(BorderFactory.createTitledBorder(
           BorderFactory.createEmptyBorder(0, 30, 0, 0), "Choose Color"));
      }
    }

    // function to set the color for a genotype
    private void setGenoColor(int idx, Color color) {
      Color[] genocolor = (Color[])GenoPlotPanel.this.genoPlotProperties.get(FigureProperties.GENOPLOT_PALETTE);
      genocolor[idx] = color;
      // save the property change
      setProperty(FigureProperties.GENOPLOT_PALETTE, genocolor);
      // set the button
      this.GenoColorButton[idx].setBackground(color);
      // redraw the figure
//      plotPanel.reDraw();
    }

    // respond mouse click
    public void actionPerformed(ActionEvent e) {
      Object src = e.getSource();
      if(src == this.cancelButton) {
        dispose();
      }
      else if(src == this.okButton) {
        this.ok = true;
        dispose();
      }
      else if(src instanceof JButton) {
        // show palette
        final int buttonIdx = Integer.valueOf(
                ((JButton)src).getActionCommand()).intValue();
        Color defaultColor = ((JButton)src).getBackground();
        final JColorChooser chooser = new JColorChooser(defaultColor);
        // Color selected
        JDialog dialog = JColorChooser.createDialog(
                QTL.getInstance().getApplicationFrame(),
                "test",
                true,
                chooser,
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setGenoColor(buttonIdx, chooser.getColor());
                    }
                },
                null);
        dialog.setVisible(true);
      }
    }
  }

/*  private class GenoPaletteDialog extends JDialog implements ActionListener {
    private JButton ok, cancel;
    private JPanel buttonPanel;
    private JColorChooser chooser;

    public GenoPaletteDialog() {
      super(frame, "Choose color", true);
      // make button panel
      buttonPanel = new JPanel();
      ok = new JButton("OK");
      ok.addActionListener(this);
      buttonPanel.add(ok);
      cancel = new JButton("Cancel");
      cancel.addActionListener(this);
      buttonPanel.add(cancel);
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

      // make color chooser
      chooser = new JColorChooser();

      // make the dialog
      Container c = getContentPane();
      c.add(chooser, "North");
      c.add(buttonPanel, "South");

      // center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getPreferredSize();
      setLocation((screenSize.width - frameSize.width) / 2,
                  (screenSize.height - frameSize.height) / 2);

      pack();
    }


    // respond mouse click
    public void actionPerformed(ActionEvent e) {
      if(e.getSource() == cancel) {
        dispose();
      }
      else if(e.getSource() == ok) {
      }
    }
  } */
}
