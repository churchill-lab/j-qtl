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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.analyticgraph.framework.Graph2DComponent;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisRenderingGraph;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.CrossChromosome;
import org.jax.qtl.cross.GeneticMap;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.gui.MarkerPositionManager;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.qtl.scan.ScanOneMarkerSignificanceValues;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.util.gui.desktoporganization.Desktop;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * An action for plotting QTL scan results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PlotScanOneResultAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -2017248038558694204L;
    
    /**
     * the result for us to plot
     */
    private final ScanOneResult resultsToPlot;

    /**
     * Constructor
     */
    public PlotScanOneResultAction()
    {
        this(null);
    }
    
    /**
     * Constructor
     * @param resultsToPlot
     *          the results of the scan
     */
    public PlotScanOneResultAction(
            ScanOneResult resultsToPlot)
    {
        super("Plot One QTL Scan Results ...");
        
        this.resultsToPlot = resultsToPlot;
        
        if(resultsToPlot == null)
        {
            // add a listener to the project so that we know when to refresh
            // our updated state
            QtlProjectManager projectManager = QtlProjectManager.getInstance();
            projectManager.addProjectChangeListener(this);
            this.projectChangeOccurred(projectManager);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        if(this.resultsToPlot != null)
        {
            PlotScanOneResultAction.showScanResult(
                    this.resultsToPlot);
        }
        else
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            QtlDataModel dataModel =
                QtlProjectManager.getInstance().getActiveProject().getDataModel();

            final SelectScanOneResultsDialog showScanResultsDialog = new SelectScanOneResultsDialog(
                      QTL.getInstance().getApplicationFrame(),
                      "Select Scan Results To Plot",
                      projectTree.getSelectedCross(),
                      dataModel.getCrosses());
            showScanResultsDialog.setVisible(true);
            
            Thread waitForResultsThread = new Thread()
            {
                @Override
                public void run()
                {
                    final ScanOneResult scanOneResult =
                        showScanResultsDialog.getSelectedScanOneResult();
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            PlotScanOneResultAction.showScanResult(
                                    scanOneResult);
                        }
                    });
                }
            };
            waitForResultsThread.start();
        }
    }
    
    /**
     * Show the given scanone result
     * @param resultsToPlot
     *          the results
     */
    public static void showScanResult(
            ScanOneResult resultsToPlot)
    {
        Cross scannedCross = resultsToPlot.getParentCross();
        List<CrossChromosome> scannedCrossGenotypeData =
            scannedCross.getGenotypeData();
        GeneticMap[] geneticMaps =
            new GeneticMap[scannedCrossGenotypeData.size()];
        for(int i = 0; i < geneticMaps.length; i++)
        {
            geneticMaps[i] =
                scannedCrossGenotypeData.get(i).getAnyGeneticMap();
        }
        
        SimpleGraphCoordinateConverter coordinateConverter =
            new SimpleGraphCoordinateConverter(
                0.0, 0.0,
                1.0, 1.0);
        
        
        List<List<GeneticMarker>> markerLists =
            new ArrayList<List<GeneticMarker>>();
        {
            List<List<ScanOneMarkerSignificanceValues>> markerSigLists =
                resultsToPlot.getMarkerSignificanceValuesByChromosome(
                        resultsToPlot.getSignificanceValueColumnNames()[0]);
            {
                for(List<ScanOneMarkerSignificanceValues> currSigList: markerSigLists)
                {
                    ArrayList<GeneticMarker> newMarkerList =
                        new ArrayList<GeneticMarker>(currSigList.size());
                    markerLists.add(newMarkerList);
                    
                    for(ScanOneMarkerSignificanceValues currSigValue: currSigList)
                    {
                        newMarkerList.add(currSigValue.getMarker());
                    }
                }
            }
        }
        
        AxisRenderingGraph markerValuesGraphWithAxes =
            new AxisRenderingGraph(coordinateConverter);
        
        MarkerPositionManager markerPositionManager =
            new MarkerPositionManager(markerLists);
        ScanOneGraph scanOneGraph = new ScanOneGraph(
                resultsToPlot,
                markerPositionManager,
                geneticMaps);
        
        markerValuesGraphWithAxes.setInteriorGraph(
                scanOneGraph);
        
        Graph2DComponent graphComponent = new Graph2DComponent();
        graphComponent.setBackground(Color.WHITE);
        graphComponent.addGraph2D(markerValuesGraphWithAxes);
        graphComponent.setPreferredSize(new Dimension(400, 400));
        
        ScanOnePanel scanOnePanel = new ScanOnePanel(
                graphComponent,
                scanOneGraph);
        
        Desktop desktop = QTL.getInstance().getDesktop();
        desktop.createInternalFrame(
                scanOnePanel,
                "One QTL Scan Results For: " +
                scannedCross.getAccessorExpressionString(),
                null,
                resultsToPlot.getAccessorExpressionString());
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(final ProjectManager projectManager)
    {
        QtlProjectManager qtlProjectManager = (QtlProjectManager)projectManager;
        QtlDataModel dataModel =
            qtlProjectManager.getActiveProject().getDataModel();
        final boolean anyScanones =
            ScanOneResult.anyScanoneResultsExist(dataModel);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                PlotScanOneResultAction.this.setEnabled(anyScanones);
            }
        });
    }
}
