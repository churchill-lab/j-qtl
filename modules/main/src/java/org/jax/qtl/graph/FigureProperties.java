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
import java.util.Hashtable;


/**
 * 
 * @author unascribed
 * @version 1.0
 */
@SuppressWarnings("all")
public class FigureProperties {
  // for geno Plot
  public static final String GENOPLOT_XSIZE = "genoplot-xsize";
  public static final String GENOPLOT_YSIZE = "genoplot-ysize";
  public static final String GENOPLOT_SORTBY = "genoplot-sortby";
  public static final String GENOPLOT_PALETTE = "genoplot-palette";
  public static final String GENOPLOT_WHAT = "genoplot-what";
  public static final String GENOPLOT_INT = "genoplot-interactive";
  public static final String GENOPLOT_CHROM = "genoplot-chromosome";
  public static final String GENOPLOT_IND = "genoplot-individuals";
  public static final String GENOPLOT_IN_MARKER_DIST = "genoplot-in-marker-distance";
  public static final String GENOPLOT_ERRORLOD_BREAKS = "genoplot-errorlod-breaks";
  public static final String GENOPLOT_ERRORLOD_COLORS = "genoplot-errorlod-colors";

  /**
   * make default figure settings for RI plot
   * @return
   */
  public static Hashtable defaultGenoPlotProperties() {
    Hashtable GenoPlotProperties = new Hashtable();
    GenoPlotProperties.put(GENOPLOT_XSIZE, new Integer(5));
    GenoPlotProperties.put(GENOPLOT_YSIZE, new Integer(5));
    GenoPlotProperties.put(GENOPLOT_SORTBY, new Integer(0));
    GenoPlotProperties.put(GENOPLOT_WHAT, new Integer(0));
    GenoPlotProperties.put(GENOPLOT_INT, new Boolean(true));
//    int ngeno = ReadDataDialogBox.genoNameString.length;
    Color[] GenoColor = {new Color(255,0,0), new Color(0,255,0),
      new Color(0,0,255), new Color(0,255,255), new Color(255,255,0),
      new Color(0,0,0)};
    GenoPlotProperties.put(GENOPLOT_PALETTE, GenoColor);
    GenoPlotProperties.put(GENOPLOT_CHROM, "all");
    GenoPlotProperties.put(GENOPLOT_IND, "all");
    GenoPlotProperties.put(GENOPLOT_IN_MARKER_DIST, new Boolean(false));
    // for errorlod
    double[] breaks = {-1,2,3,4.5,Double.MAX_VALUE};
    GenoPlotProperties.put(GENOPLOT_ERRORLOD_BREAKS, breaks);
    Color[] colors = {Color.white, Color.lightGray, Color.pink, Color.cyan};
    GenoPlotProperties.put(GENOPLOT_ERRORLOD_COLORS, colors);
    return(GenoPlotProperties);
  }
}
