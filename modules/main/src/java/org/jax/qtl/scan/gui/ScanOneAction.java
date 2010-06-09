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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.qtl.scan.ScanType;
import org.jax.r.RCommand;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.project.ProjectChangeListener;
import org.jax.util.project.ProjectManager;

/**
 * Action for showing the "scan one" command dialog.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ScanOneAction extends AbstractAction implements ProjectChangeListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -9028294159002679385L;
    
    /**
     * the selected cross object for this scan action
     */
    private final Cross selectedCross;
    
    /**
     * action listener that responds to an approval of the scan action
     */
    private final ActionListener scanApprovedListener = new ActionListener()
    {
        public void actionPerformed(ActionEvent e)
        {
            ScanDialog sourceDialog = (ScanDialog)e.getSource();
            final ScanCommandBuilder scanCommandBuilder = sourceDialog.getScanCommand();
            
            Thread evaluateCommandThread = new Thread()
            {
                @Override
                public void run()
                {
                    ScanOneAction.this.evaluateScanCommand(
                            scanCommandBuilder);
                }
            };
            
            evaluateCommandThread.start();
        }
    };
    
    /**
     * Constructor for creating a scanone action without any specific cross.
     */
    public ScanOneAction()
    {
        this(null);
    }
    
    /**
     * Create a scanone action for the given cross.
     * @param selectedCross
     *          the cross
     */
    public ScanOneAction(Cross selectedCross)
    {
        super("Run One QTL Genome Scan ...");
        
        this.selectedCross = selectedCross;
        
        if(selectedCross == null)
        {
            // add a listener to the project so that we know when to refresh
            // our updated state
            QtlProjectManager projectManager = QtlProjectManager.getInstance();
            projectManager.addProjectChangeListener(this);
            this.projectChangeOccurred(projectManager);
        }
    }
    
    /**
     * Evaluate the given scan command
     * @param scanCommandBuilder
     *          the scan command to evaluate
     */
    private void evaluateScanCommand(final ScanCommandBuilder scanCommandBuilder)
    {
        // run the scan
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        rInterface.insertComment(
                "running scanone on cross: " +
                scanCommandBuilder.getCross().getAccessorExpressionString());
        rInterface.evaluateCommandNoReturn(
                scanCommandBuilder.getCommandWithoutPermutations());
        RCommand permutationsCommand = scanCommandBuilder.getCommandWithPermutations();
        if(permutationsCommand != null)
        {
            rInterface.insertComment(
                    "running scanone permutations (this can take a while)");
            rInterface.evaluateCommandNoReturn(permutationsCommand);
        }
        
        // add a record of which phenotype(s) we scanned
        RCommand phenotypeCommand =
            scanCommandBuilder.getPhenotypeAttributeCommand();
        if(phenotypeCommand != null)
        {
            rInterface.insertComment(
                    "adding an attribute so we know which phenotype(s) " +
                    "this scan came from");
            rInterface.evaluateCommandNoReturn(phenotypeCommand);
        }
        
        rInterface.flushCommands();
        QtlProjectManager.getInstance().notifyActiveProjectModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        SwingUtilities.invokeLater(
                new Runnable()
                {
                    public void run()
                    {
                        QtlProject activeProject =
                            QtlProjectManager.getInstance().getActiveProject();
                        QtlDataModel dataModel =
                            activeProject.getDataModel();
                        Cross[] crosses = dataModel.getCrosses();
                        
                        Cross selectedCross = ScanOneAction.this.selectedCross;
                        if(selectedCross == null)
                        {
                            QtlProjectTree projectTree =
                                QTL.getInstance().getProjectTree();
                            selectedCross = projectTree.getSelectedCross();
                        }
                        
                        ScanDialog scanDialog = new ScanDialog(
                                QTL.getInstance().getApplicationFrame(),
                                ScanType.SCANONE,
                                crosses,
                                selectedCross);
                        scanDialog.addActionListener(
                                ScanOneAction.this.scanApprovedListener);
                        scanDialog.setVisible(
                                true);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    public void projectChangeOccurred(ProjectManager projectManager)
    {
        // we shouldn't enable this unless we have some active crosses
        QtlProjectManager qtlProjectManager = (QtlProjectManager)projectManager;
        QtlDataModel dataModel =
            qtlProjectManager.getActiveProject().getDataModel();
        final boolean anyCrosses = !dataModel.getCrossMap().isEmpty();
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                ScanOneAction.this.setEnabled(anyCrosses);
            }
        });
    }
}
