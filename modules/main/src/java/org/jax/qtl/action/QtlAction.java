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

package org.jax.qtl.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jax.qtl.Constants;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.gui.CrossSelectionDialog;
import org.jax.qtl.graph.FigureProperties;
import org.jax.qtl.graph.GenoPlotPanel;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.qtl.ui.AboutDialog;
import org.jax.qtl.ui.EstimateMapDialog;
import org.jax.qtl.ui.GeneticMapDialog;
import org.jax.qtl.ui.RfPlotDialog;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;


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
public class QtlAction extends AbstractAction implements Constants, ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 475211037006107854L;

    // menu items under File menu
    @SuppressWarnings("all")
    public static final String EXIT = "Exit";

    // menu items under sub-menu of Analysis menu
    @SuppressWarnings("all")
    public static final String GENETIC_MAP = "Display Genetic Map ...";
    @SuppressWarnings("all")
    public static final String RF_PLOT = "Display RF Plot ...";
    @SuppressWarnings("all")
    public static final String GENO_PLOT = "Display Genotype Plot ...";
    @SuppressWarnings("all")
    public static final String EST_MAP = "Estimate Genetic Map ...";

    // menu items under Help menu
    @SuppressWarnings("all")
    public static final String HELP_TOPIC = "Help Topics";
    @SuppressWarnings("all")
    public static final String ABOUT = "About ...";

    private String actionName;
    
    /**
     * Constructor
     * @param actionName
     *          the action name
     */
    public QtlAction (String actionName) {
        super(actionName);
        this.actionName = actionName;
        putValue(Action.NAME, actionName);
        putValue(Action.SMALL_ICON, null);
        putValue(Action.SHORT_DESCRIPTION, actionName);
        
        // add a listener to the project so that we know when to refresh
        // our updated state
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        projectManager.addProjectChangeListener(this);
        this.projectChangeOccurred(projectManager);
    }

    /**
     * Constructor
     * @param actionName
     *          the action name
     * @param discription
     *          the action description
     * @param icon
     *          the action icon
     */
    public QtlAction (String actionName, String discription, Icon icon) {
        super(actionName);
        this.actionName = actionName;
        putValue(Action.NAME, actionName);
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, discription);
        
        // add a listener to the project so that we know when to refresh
        // our updated state
        QtlProjectManager projectManager = QtlProjectManager.getInstance();
        projectManager.addProjectChangeListener(this);
        this.projectChangeOccurred(projectManager);
    }

    private void about() {
        AboutDialog about = new AboutDialog();
        about.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(ProjectManager projectManager)
    {
        // Enable or disable this action based on whether or not it makes
        // sense to execute this action
        QtlProject activeProject =
            (QtlProject)projectManager.getActiveProject();
        QtlDataModel dataModel =
            activeProject.getDataModel();
        
        if(this.actionName.equals(GENETIC_MAP) ||
           this.actionName.equals(RF_PLOT) ||
           this.actionName.equals(GENO_PLOT) ||
           this.actionName.equals(EST_MAP))
        {
            final boolean anyCrosses = !dataModel.getCrossMap().isEmpty();
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    QtlAction.this.setEnabled(anyCrosses);
                }
            });
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent evt) {
        String actionName = evt.getActionCommand();
        if (actionName.equals(EXIT)) {QTL.getInstance().getApplicationFrame().closeApplication();}
        else if (actionName.equals(GENETIC_MAP)) {new GeneticMapDialog();}
        else if (actionName.equals(RF_PLOT)) {new RfPlotDialog();}
        else if (actionName.equals(GENO_PLOT))
        {
            Thread showGenoPlotThread = new Thread()
            {
                @Override
                public void run()
                {
                    QtlAction.this.showGenoPlot();
                }
            };
            showGenoPlotThread.start();
        }
        else if (actionName.equals(EST_MAP)) {new EstimateMapDialog();}
        else if (actionName.equals(ABOUT)) {about();}
    }

    /**
     * 
     */
    private void showGenoPlot()
    {
        QtlDataModel dataModel =
            QtlProjectManager.getInstance().getActiveProject().getDataModel();
        Cross[] crosses = dataModel.getCrosses();
        if(crosses.length == 0)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    final String message =
                        "Cannot display genotype plot because the active J/qtl " +
                        "does not have any associated crosses loaded";
                    JOptionPane.showMessageDialog(
                            QTL.getInstance().getApplicationFrame(),
                            TextWrapper.wrapText(
                                    message,
                                    TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                                    "Cannot Display Genotype Plot",
                                    JOptionPane.WARNING_MESSAGE);
                }
            });
        }
        else
        {
            final Cross selectedCross = this.getGenotypeCrossSelection(crosses);
            
            if(selectedCross != null)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        GenoPlotPanel genoPlotPanel = new GenoPlotPanel(
                                selectedCross,
                                FigureProperties.defaultGenoPlotProperties());
                        QTL.getInstance().getDesktop().createInternalFrame(
                                genoPlotPanel,
                                "Genotype Plot for: " + selectedCross.getAccessorExpressionString(),
                                null,
                                selectedCross.getAccessorExpressionString() + ".genoplot");
                    }
                });
            }
        }
    }
    
    private Cross getGenotypeCrossSelection(Cross[] candidateCrosses)
    {
        if(candidateCrosses.length == 0)
        {
            return null;
        }
        if(candidateCrosses.length == 1)
        {
            return candidateCrosses[0];
        }
        else
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            Cross initialCrossSelection = projectTree.getSelectedCross();
            final CrossSelectionDialog selectionDialog = new CrossSelectionDialog(
                    QTL.getInstance().getApplicationFrame(),
                    "Select Cross for Genotype Plot",
                    candidateCrosses,
                    initialCrossSelection);
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    selectionDialog.pack();
                    selectionDialog.setVisible(true);
                }
            });
            return selectionDialog.getSelectedCross();
        }
    }
}
