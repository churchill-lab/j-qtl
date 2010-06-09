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

package org.jax.qtl.project.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.RunJittermapAction;
import org.jax.qtl.cross.gui.EditQtlBasketAction;
import org.jax.qtl.cross.gui.ShowCrossSummaryAction;
import org.jax.qtl.fit.FitQtlResult;
import org.jax.qtl.fit.gui.FitQtlAction;
import org.jax.qtl.fit.gui.ShowFitQtlResultsAction;
import org.jax.qtl.project.QtlDataModel;
import org.jax.qtl.project.QtlDataModelListener;
import org.jax.qtl.project.QtlProject;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.scan.ScanOneResult;
import org.jax.qtl.scan.ScanTwoResult;
import org.jax.qtl.scan.gui.PlotScanOneResultAction;
import org.jax.qtl.scan.gui.PlotScanTwoResultAction;
import org.jax.qtl.scan.gui.ScanOneSummaryAction;
import org.jax.qtl.scan.gui.ScanTwoSummaryAction;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.gui.Iconifiable;
import org.jax.util.gui.SafeDeleteAction;
import org.jax.util.gui.SwingTreeUtilities;
import org.jax.util.project.Project;
import org.jax.util.project.gui.ProjectTree;

/**
 * For displaying J/qtl project data as a {@link JTree}
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlProjectTree extends ProjectTree
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 7242325371548165892L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QtlProjectTree.class.getName());
    
    /**
     * listens to the data model for changes
     */
    private QtlDataModelListener dataModelListener = new QtlDataModelListener()
    {
        public void crossAdded(QtlDataModel source, Cross cross)
        {
            this.changeOccured();
        }
        
        public void crossRemoved(QtlDataModel source, Cross cross)
        {
            this.changeOccured();
        }

        private void changeOccured()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    QtlProjectTree.this.refreshCrossNodes();
                }
            });
        }
    };
    
    /**
     * @see #getSelectedCross()
     */
    private volatile Cross selectedCross = null;

    /**
     * @see #getSelectedScanOneResult()
     */
    private volatile ScanOneResult selectedScanOneResult = null;
    
    /**
     * @see #getSelectedScanTwoResult()
     */
    private volatile ScanTwoResult selectedScanTwoResult = null;

    /**
     * @see #getSelectedQtlBasket()
     */
    private volatile QtlBasket selectedQtlBasket;
    
    /**
     * Constructor
     */
    public QtlProjectTree()
    {
        this.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent treeSelectionEvent)
            {
                QtlProjectTree.this.treeSelectionChanged(treeSelectionEvent);
            }
        });
    }

    /**
     * For responding to a tree selection event
     * @param treeSelectionEvent
     *          the selection event to respond to
     */
    private void treeSelectionChanged(TreeSelectionEvent treeSelectionEvent)
    {
        TreePath selectionPath = treeSelectionEvent.getNewLeadSelectionPath();
        if(selectionPath == null)
        {
            this.setSelectedCross(null);
        }
        else
        {
            // search for the selected cross going toward the root
            Object[] treePath = selectionPath.getPath();
            boolean foundSelectedCross = false;
            boolean foundSelectedScanOneResult = false;
            boolean foundSelectedScanTwoResult = false;
            boolean foundSelectedQtlBasketResult = false;
            for(int i = treePath.length - 1; i >= 0; i--)
            {
                Object currSelectedNode = treePath[i];
                if(currSelectedNode instanceof CrossTreeNode)
                {
                    if(!foundSelectedCross)
                    {
                        CrossTreeNode currCrossTreeNode = (CrossTreeNode)currSelectedNode;
                        this.setSelectedCross(currCrossTreeNode.getCross());
                        foundSelectedCross = true;
                    }
                }
                else if(currSelectedNode instanceof ScanOneTreeNode)
                {
                    if(!foundSelectedScanOneResult)
                    {
                        ScanOneTreeNode currScanOneTreeNode = (ScanOneTreeNode)currSelectedNode;
                        this.setSelectedScanOneResult(currScanOneTreeNode.getScanOneResult());
                        foundSelectedScanOneResult = true;
                    }
                }
                else if(currSelectedNode instanceof ScanTwoTreeNode)
                {
                    if(!foundSelectedScanTwoResult)
                    {
                        ScanTwoTreeNode currScanTwoTreeNode = (ScanTwoTreeNode)currSelectedNode;
                        this.setSelectedScanTwoResult(currScanTwoTreeNode.getScanTwoResult());
                        foundSelectedScanTwoResult = true;
                    }
                }
                else if(currSelectedNode instanceof QtlBasketNode)
                {
                    if(!foundSelectedQtlBasketResult)
                    {
                        QtlBasketNode currQtlBasketNode = (QtlBasketNode)currSelectedNode;
                        this.setSelectedQtlBasket(currQtlBasketNode.getQtlBasket());
                        foundSelectedQtlBasketResult = true;
                    }
                }
            }
            
            // set to null if we couldn't find what we were looking for
            if(!foundSelectedCross)
            {
                this.setSelectedCross(null);
            }
            
            if(!foundSelectedScanOneResult)
            {
                this.setSelectedScanOneResult(null);
            }
            
            if(!foundSelectedScanTwoResult)
            {
                this.setSelectedScanTwoResult(null);
            }
            
            if(!foundSelectedQtlBasketResult)
            {
                this.setSelectedQtlBasket(null);
            }
        }
    }

    /**
     * Setter for the selected QTL basket
     * @param selectedQtlBasket
     *          the selected QTL basket
     */
    private void setSelectedQtlBasket(QtlBasket selectedQtlBasket)
    {
        if(LOG.isLoggable(Level.FINE))
        {
            LOG.fine("selected QTL basket set to: " + selectedQtlBasket);
        }
        
        this.selectedQtlBasket = selectedQtlBasket;
    }
    
    /**
     * Getter for the selected QTL basket
     * @return the selectedQtlBasket
     */
    public QtlBasket getSelectedQtlBasket()
    {
        return this.selectedQtlBasket;
    }

    /**
     * Setter for the selected scanone result
     * @param selectedScanOneResult
     *          the result to set
     */
    private void setSelectedScanOneResult(ScanOneResult selectedScanOneResult)
    {
        if(LOG.isLoggable(Level.FINE))
        {
            LOG.fine("selected scan one result set to: " + selectedScanOneResult);
        }
        
        this.selectedScanOneResult = selectedScanOneResult;
    }
    
    /**
     * Getter for the selected scanone result
     * @return
     *          the selected scan one result
     */
    public ScanOneResult getSelectedScanOneResult()
    {
        return this.selectedScanOneResult;
    }
    
    /**
     * Setter for the selected scantwo result
     * @param selectedScanTwoResult
     *          the selectedScanTwoResult to set
     */
    public void setSelectedScanTwoResult(ScanTwoResult selectedScanTwoResult)
    {
        if(LOG.isLoggable(Level.FINE))
        {
            LOG.fine("selected scan two result set to: " + selectedScanTwoResult);
        }
        
        this.selectedScanTwoResult = selectedScanTwoResult;
    }
    
    /**
     * Getter for the selected scantwo result
     * @return
     *          the selected scantwo result
     */
    public ScanTwoResult getSelectedScanTwoResult()
    {
        return this.selectedScanTwoResult;
    }

    /**
     * Setter for the selected cross
     * @param selectedCross
     *          the new value for selected cross
     */
    private void setSelectedCross(Cross selectedCross)
    {
        if(LOG.isLoggable(Level.FINE))
        {
            LOG.fine("selected cross set to: " + selectedCross);
        }
        
        this.selectedCross = selectedCross;
    }
    
    /**
     * Getter for the cross that the user has selected
     * @return
     *          the cross or null if no cross is selected
     */
    public Cross getSelectedCross()
    {
        return this.selectedCross;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QtlProject getActiveProject()
    {
        return (QtlProject)super.getActiveProject();
    }

    /**
     * Set the currently active project
     * @param activeProject
     *          the new active project
     */
    @Override
    public void setActiveProject(Project activeProject)
    {
        QtlProject qtlActiveProject = (QtlProject)activeProject;
        {
            QtlProject oldActiveProject = this.getActiveProject();
            if(oldActiveProject != null)
            {
                oldActiveProject.getDataModel().removeQtlDataModelListener(
                        this.dataModelListener);
            }
        }
        
        if(qtlActiveProject != null)
        {
            qtlActiveProject.getDataModel().addQtlDataModelListener(
                    this.dataModelListener);
        }
        
        super.setActiveProject(activeProject);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QtlProjectManager getProjectManager()
    {
        return (QtlProjectManager)super.getProjectManager();
    }
    
    /**
     * refresh the project tree
     */
    @Override
    protected void refreshProjectTree()
    {
        QtlProject activeProject = this.getProjectManager().getActiveProject();
        final boolean activeProjectIsRoot;
        {
            final Object rootObject = this.getModel().getRoot();
            if(rootObject instanceof ProjectTreeNode)
            {
                final ProjectTreeNode root = (ProjectTreeNode)rootObject;
                activeProjectIsRoot =
                    root.getProject() == activeProject;
            }
            else
            {
                activeProjectIsRoot = false;
            }
        }
        
        if(!activeProjectIsRoot)
        {
            this.getModel().setRoot(new ProjectTreeNode(activeProject));
        }
        
        this.refreshCrossNodes();
    }
    
    /**
     * refresh all of the cross nodes
     */
    private void refreshCrossNodes()
    {
        ProjectTreeNode projectNode = (ProjectTreeNode)this.getModel().getRoot();
        QtlProject project = projectNode.getProject();
        
        QtlDataModel dataModel = project.getDataModel();
        
        // remove tree nodes not in the QTL data model
        for(int crossNodeIndex = projectNode.getChildCount() - 1;
            crossNodeIndex >= 0;
            crossNodeIndex--)
        {
            CrossTreeNode currCrossNode =
                (CrossTreeNode)projectNode.getChildAt(crossNodeIndex);
            boolean crossInDataModel = dataModel.getCrossMap().containsKey(
                    currCrossNode.getCross().getAccessorExpressionString());
            if(!crossInDataModel)
            {
                this.getModel().removeNodeFromParent(currCrossNode);
            }
        }
        
        // add nodes that are missing (refresh existing)
        for(Cross currCross: dataModel.getCrossMap().values())
        {
            int indexOfCross = SwingTreeUtilities.indexOfChildWithUserObject(
                    projectNode,
                    currCross);
            
            CrossTreeNode currCrossNode;
            if(indexOfCross == -1)
            {
                // append the cross to the end of the project node
                currCrossNode = new CrossTreeNode(currCross);
                this.getModel().insertNodeInto(
                        currCrossNode,
                        projectNode,
                        projectNode.getChildCount());
                this.getModel().insertNodeInto(
                        currCrossNode.getScanOneFolderNode(),
                        currCrossNode,
                        currCrossNode.getChildCount());
                this.getModel().insertNodeInto(
                        currCrossNode.getScanTwoFolderNode(),
                        currCrossNode,
                        currCrossNode.getChildCount());
                this.getModel().insertNodeInto(
                        currCrossNode.getQtlBasketFolderNode(),
                        currCrossNode,
                        currCrossNode.getChildCount());
                this.getModel().insertNodeInto(
                        currCrossNode.getFitResultsFolderNode(),
                        currCrossNode,
                        currCrossNode.getChildCount());
                this.expandPath(new TreePath(
                        currCrossNode.getPath()));
            }
            else
            {
                currCrossNode =
                    (CrossTreeNode)projectNode.getChildAt(indexOfCross);
            }
            
            this.refreshCross(currCrossNode);
        }
        
        this.getModel().nodeChanged(projectNode);
    }

    /**
     * Refresh the given cross
     * @param crossNodeToRefresh
     *          the cross to refresh
     */
    private void refreshCross(CrossTreeNode crossNodeToRefresh)
    {
        this.refreshScanOneFolder(crossNodeToRefresh.getScanOneFolderNode());
        this.refreshScanTwoFolder(crossNodeToRefresh.getScanTwoFolderNode());
        this.refreshQtlBasketFolder(crossNodeToRefresh.getQtlBasketFolderNode());
        this.refreshFitResultsFolder(crossNodeToRefresh.getFitResultsFolderNode());
    }
    
    /**
     * Refresh the given qtl basket folder
     * @param qtlBasketFolderNode
     *          the qtl basket folder
     */
    private void refreshQtlBasketFolder(QtlBasketFolderNode qtlBasketFolderNode)
    {
        Cross cross = qtlBasketFolderNode.getCross();
        Map<String, QtlBasket> qtlBasketMap = cross.getQtlBasketMap();
        boolean anyNewNodes = false;
        
        // remove tree nodes that are no longer in the cross
        synchronized(qtlBasketMap)
        {
            for(int crossChildIndex = qtlBasketFolderNode.getChildCount() - 1;
                crossChildIndex >= 0;
                crossChildIndex--)
            {
                TreeNode currCrossChild =
                    qtlBasketFolderNode.getChildAt(crossChildIndex);
                if(currCrossChild instanceof QtlBasketNode)
                {
                    QtlBasketNode currQtlBasketNode =
                        (QtlBasketNode)currCrossChild;
                    if(!qtlBasketMap.containsKey(currQtlBasketNode.getQtlBasket().getName()))
                    {
                        // the basket node needs to be removed
                        this.getModel().removeNodeFromParent(currQtlBasketNode);
                    }
                }
            }
            
            // add the child nodes that are missing from the tree
            for(QtlBasket currQtlBasket: qtlBasketMap.values())
            {
                int indexOfBasket = SwingTreeUtilities.indexOfChildWithUserObject(
                        qtlBasketFolderNode,
                        currQtlBasket);
                if(indexOfBasket == -1)
                {
                    QtlBasketNode newQtlBasketNode =
                        new QtlBasketNode(currQtlBasket);
                    this.getModel().insertNodeInto(
                            newQtlBasketNode,
                            qtlBasketFolderNode,
                            qtlBasketFolderNode.getChildCount());
                    anyNewNodes = true;
                }
            }
        }
        
        if(anyNewNodes)
        {
            this.expandPath(new TreePath(
                    qtlBasketFolderNode.getPath()));
        }
        
        this.getModel().nodeChanged(qtlBasketFolderNode);
    }

    /**
     * Refresh the scan one folder for a cross
     * @param scanOneFolder
     *          the folder that we're refreshing
     */
    private void refreshScanOneFolder(ScanOneFolderNode scanOneFolder)
    {
        Cross cross = scanOneFolder.getCross();
        Set<ScanOneResult> scanOneResults = cross.getScanOneResults();
        
        // remove tree nodes that are no longer in the cross
        for(int crossChildIndex = scanOneFolder.getChildCount() - 1;
            crossChildIndex >= 0;
            crossChildIndex--)
        {
            TreeNode currCrossChild =
                scanOneFolder.getChildAt(crossChildIndex);
            if(currCrossChild instanceof ScanOneTreeNode)
            {
                ScanOneTreeNode currScanOneNode =
                    (ScanOneTreeNode)currCrossChild;
                if(!scanOneResults.contains(currScanOneNode.getScanOneResult()))
                {
                    // the scan one node needs to be removed
                    this.getModel().removeNodeFromParent(currScanOneNode);
                }
            }
        }
        
        // add the child nodes that are missing from the tree
        boolean anyNewNodes = false;
        for(ScanOneResult currScanOneResult: scanOneResults)
        {
            int indexOfScanResult = SwingTreeUtilities.indexOfChildWithUserObject(
                    scanOneFolder,
                    currScanOneResult);
            if(indexOfScanResult == -1)
            {
                ScanOneTreeNode newScanOneNode = new ScanOneTreeNode(
                        currScanOneResult); 
                this.getModel().insertNodeInto(
                        newScanOneNode,
                        scanOneFolder,
                        scanOneFolder.getChildCount());
                anyNewNodes = true;
            }
        }
        
        if(anyNewNodes)
        {
            this.expandPath(new TreePath(
                    scanOneFolder.getPath()));
        }
        
        this.getModel().nodeChanged(scanOneFolder);
    }
    
    /**
     * Refresh the scan one folder for a cross
     * @param scanTwoFolder
     *          the folder that we're refreshing
     */
    private void refreshScanTwoFolder(ScanTwoFolderNode scanTwoFolder)
    {
        Cross cross = scanTwoFolder.getCross();
        Set<ScanTwoResult> scanTwoResults = cross.getScanTwoResults();
        
        // remove tree nodes that are no longer in the cross
        for(int crossChildIndex = scanTwoFolder.getChildCount() - 1;
            crossChildIndex >= 0;
            crossChildIndex--)
        {
            TreeNode currCrossChild =
                scanTwoFolder.getChildAt(crossChildIndex);
            if(currCrossChild instanceof ScanTwoTreeNode)
            {
                ScanTwoTreeNode currScanTwoNode =
                    (ScanTwoTreeNode)currCrossChild;
                if(!scanTwoResults.contains(currScanTwoNode.getScanTwoResult()))
                {
                    // the scan one node needs to be removed
                    this.getModel().removeNodeFromParent(currScanTwoNode);
                }
            }
        }
        
        // add the child nodes that are missing from the tree
        boolean anyNewNodes = false;
        for(ScanTwoResult currScanTwoResult: scanTwoResults)
        {
            int indexOfScanResult = SwingTreeUtilities.indexOfChildWithUserObject(
                    scanTwoFolder,
                    currScanTwoResult);
            if(indexOfScanResult == -1)
            {
                ScanTwoTreeNode newScanTwoNode = new ScanTwoTreeNode(
                        currScanTwoResult); 
                this.getModel().insertNodeInto(
                        newScanTwoNode,
                        scanTwoFolder,
                        scanTwoFolder.getChildCount());
                anyNewNodes = true;
            }
        }
        
        if(anyNewNodes)
        {
            this.expandPath(new TreePath(
                    scanTwoFolder.getPath()));
        }
        
        this.getModel().nodeChanged(scanTwoFolder);
    }
    
    private void refreshFitResultsFolder(FitResultsFolderNode fitResultsFolder)
    {
        Cross cross = fitResultsFolder.getCross();
        Set<FitQtlResult> fitQtlResults = cross.getFitQtlResults();
        
        // remove tree nodes that are no longer in the cross
        for(int crossChildIndex = fitResultsFolder.getChildCount() - 1;
            crossChildIndex >= 0;
            crossChildIndex--)
        {
            TreeNode currCrossChild =
                fitResultsFolder.getChildAt(crossChildIndex);
            if(currCrossChild instanceof FitResultsTreeNode)
            {
                FitResultsTreeNode currFitResultsTreeNode =
                    (FitResultsTreeNode)currCrossChild;
                if(!fitQtlResults.contains(currFitResultsTreeNode.getFitQtlResult()))
                {
                    // the fit node needs to be removed
                    this.getModel().removeNodeFromParent(currFitResultsTreeNode);
                }
            }
        }
        
        // add the child nodes that are missing from the tree
        boolean anyNewNodes = false;
        for(FitQtlResult currFitQtlResult: fitQtlResults)
        {
            int indexOfFitResult = SwingTreeUtilities.indexOfChildWithUserObject(
                    fitResultsFolder,
                    currFitQtlResult);
            if(indexOfFitResult == -1)
            {
                FitResultsTreeNode newFitTreeNode = new FitResultsTreeNode(
                        currFitQtlResult);
                this.getModel().insertNodeInto(
                        newFitTreeNode,
                        fitResultsFolder,
                        fitResultsFolder.getChildCount());
                anyNewNodes = true;
            }
        }
        
        if(anyNewNodes)
        {
            this.expandPath(new TreePath(
                    fitResultsFolder.getPath()));
        }
        
        this.getModel().nodeChanged(fitResultsFolder);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultTreeModel getModel()
    {
        return (DefaultTreeModel)super.getModel();
    }
    
    /**
     * A folder node for holding scan one results
     */
    private static class ScanOneFolderNode
    extends DefaultMutableTreeNode
    implements Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -8306864579326456888L;
        
        /**
         * the name this "folder" always has
         */
        private static final String FOLDER_NAME = "One QTL Scan Results";
        
        private static final String ICON_RESOURCE =
            "/images/scanone-16x16.png";
        private static final Icon SHARED_ICON;
        static
        {
            URL iconUrl = ScanOneFolderNode.class.getResource(ICON_RESOURCE);
            SHARED_ICON = new ImageIcon(iconUrl);
        }
        
        /**
         * @see #getCross()
         */
        private final Cross cross;
        
        /**
         * Constructor
         * @param cross
         *          see {@link #getCross()}
         */
        public ScanOneFolderNode(Cross cross)
        {
            this.cross = cross;
        }

        /**
         * Getter for the cross this scan one folder is for
         * @return the cross
         */
        public Cross getCross()
        {
            return this.cross;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            int childCount = this.getChildCount();
            return FOLDER_NAME +
                   " (" + (childCount == 0 ? "empty" : childCount) + ")";
        }
        
        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return SHARED_ICON;
        }
    }
    
    /**
     * A folder node for holding scan one results
     */
    private static class ScanTwoFolderNode
    extends DefaultMutableTreeNode
    implements Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -2517496924963282330L;
        
        /**
         * the name this "folder" always has
         */
        private static final String FOLDER_NAME = "Two QTL Scan Results";
        
        private static final String ICON_RESOURCE =
            "/images/scantwo-16x16.png";
        private static final Icon SHARED_ICON;
        static
        {
            URL iconUrl = ScanTwoFolderNode.class.getResource(ICON_RESOURCE);
            SHARED_ICON = new ImageIcon(iconUrl);
        }
        
        /**
         * @see #getCross()
         */
        private final Cross cross;
        
        /**
         * Constructor
         * @param cross
         *          see {@link #getCross()}
         */
        public ScanTwoFolderNode(Cross cross)
        {
            this.cross = cross;
        }

        /**
         * Getter for the cross this scan one folder is for
         * @return the cross
         */
        public Cross getCross()
        {
            return this.cross;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            int childCount = this.getChildCount();
            return FOLDER_NAME +
                   " (" + (childCount == 0 ? "empty" : childCount) + ")";
        }
        
        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return SHARED_ICON;
        }
    }
    
    /**
     * A folder node for holding fit results
     */
    private static class FitResultsFolderNode
    extends DefaultMutableTreeNode
    implements Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = 1441161822612112130L;

        /**
         * the name this "folder" always has
         */
        private static final String FOLDER_NAME = "Fit QTL Results";
        
        private static final String ICON_RESOURCE =
            "/images/fitqtl-16x16.png";
        private static final Icon SHARED_ICON;
        static
        {
            URL iconUrl = FitResultsFolderNode.class.getResource(ICON_RESOURCE);
            SHARED_ICON = new ImageIcon(iconUrl);
        }
        
        /**
         * @see #getCross()
         */
        private final Cross cross;
        
        /**
         * Constructor
         * @param cross
         *          see {@link #getCross()}
         */
        public FitResultsFolderNode(Cross cross)
        {
            this.cross = cross;
        }

        /**
         * Getter for the cross this fitqtl folder is for
         * @return the cross
         */
        public Cross getCross()
        {
            return this.cross;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            int childCount = this.getChildCount();
            return FOLDER_NAME +
                   " (" + (childCount == 0 ? "empty" : childCount) + ")";
        }
        
        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return SHARED_ICON;
        }
    }
    
    /**
     * node for holding scanone results
     */
    private class ScanOneTreeNode
    extends DefaultMutableTreeNode
    implements MouseListener
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one
         */
        private static final long serialVersionUID = -4902056183384667513L;
        
        /**
         * Constructor
         * @param scanOneResult
         *          the scan result
         */
        public ScanOneTreeNode(
                ScanOneResult scanOneResult)
        {
            super(scanOneResult);
        }
        
        /**
         * Getter for the scan one result
         * @return
         *          the scan result
         */
        public ScanOneResult getScanOneResult()
        {
            return (ScanOneResult)this.getUserObject();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.getScanOneResult().toString();
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        @SuppressWarnings("serial")
        private void popupTriggered(MouseEvent e)
        {
            JPopupMenu popupMenu = new JPopupMenu(
                    this.getScanOneResult().toString());
            popupMenu.add(new ScanOneSummaryAction(
                    this.getScanOneResult()));
            popupMenu.add(new PlotScanOneResultAction(
                    this.getScanOneResult()));
            popupMenu.addSeparator();
            popupMenu.add(new SafeDeleteAction(
                    this.getScanOneResult().toString(),
                    e.getComponent(),
                    QTL.getInstance().getDesktop())
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete()
                {
                    ScanOneTreeNode.this.delete();
                }
            });
            
            popupMenu.show(
                    (Component)e.getSource(),
                    e.getX(),
                    e.getY());
        }

        /**
         * Delete this scanone
         */
        private void delete()
        {
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            rInterface.insertComment(
                    "Deleting scanone object");
            rInterface.evaluateCommand(
                    "rm(" +
                    this.getScanOneResult().getAccessorExpressionString() +
                    ")");
            QtlProjectManager projectManager =
                QtlProjectTree.this.getProjectManager();
            projectManager.notifyActiveProjectModified();
            projectManager.refreshProjectDataStructures();
        }
    }
    
    /**
     * Node for holding fit results
     */
    private class FitResultsTreeNode
    extends DefaultMutableTreeNode
    implements MouseListener
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -3610675330618423138L;
        
        /**
         * Constructor
         * @param fitQtlResult
         *          see {@link #getFitQtlResult()}
         */
        public FitResultsTreeNode(FitQtlResult fitQtlResult)
        {
            super(fitQtlResult);
        }

        /**
         * Getter for the fit result
         * @return the fitQtlResult
         */
        public FitQtlResult getFitQtlResult()
        {
            return (FitQtlResult)this.getUserObject();
        }
        
        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        @SuppressWarnings("serial")
        private void popupTriggered(MouseEvent e)
        {
            JPopupMenu popupMenu = new JPopupMenu(
                    this.getFitQtlResult().toString());
            
            popupMenu.add(new ShowFitQtlResultsAction(
                    this.getFitQtlResult()));
            popupMenu.addSeparator();
            popupMenu.add(new SafeDeleteAction(
                    this.getFitQtlResult().toString(),
                    e.getComponent(),
                    QTL.getInstance().getDesktop())
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete()
                {
                    FitResultsTreeNode.this.delete();
                }
            });
            
            popupMenu.show(
                    (Component)e.getSource(),
                    e.getX(),
                    e.getY());
        }
        
        /**
         * delete this fit
         */
        private void delete()
        {
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            rInterface.insertComment(
                    "Deleting fit result");
            rInterface.evaluateCommand(
                    "rm(" +
                    this.getFitQtlResult().getAccessorExpressionString() +
                    ")");
            QtlProjectManager projectManager =
                QtlProjectTree.this.getProjectManager();
            projectManager.notifyActiveProjectModified();
            projectManager.refreshProjectDataStructures();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.getFitQtlResult().toString();
        }
    }
    
    /**
     * node for holding scantwo results
     */
    private class ScanTwoTreeNode
    extends DefaultMutableTreeNode
    implements MouseListener
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one
         */
        private static final long serialVersionUID = 6030556705431694911L;
        
        /**
         * Constructor
         * @param scanTwoResult
         *          the scan result
         */
        public ScanTwoTreeNode(
                ScanTwoResult scanTwoResult)
        {
            super(scanTwoResult);
        }
        
        /**
         * Getter for the scan one result
         * @return
         *          the scan result
         */
        public ScanTwoResult getScanTwoResult()
        {
            return (ScanTwoResult)this.getUserObject();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.getScanTwoResult().toString();
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        @SuppressWarnings("serial")
        private void popupTriggered(MouseEvent e)
        {
            JPopupMenu popupMenu = new JPopupMenu(
                    this.getScanTwoResult().toString());
            popupMenu.add(new ScanTwoSummaryAction(
                    this.getScanTwoResult()));
            popupMenu.add(new PlotScanTwoResultAction(
                    this.getScanTwoResult()));
            popupMenu.addSeparator();
            popupMenu.add(new SafeDeleteAction(
                    this.getScanTwoResult().toString(),
                    e.getComponent(),
                    QTL.getInstance().getDesktop())
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete()
                {
                    ScanTwoTreeNode.this.delete();
                }
            });
            
            popupMenu.show(
                    (Component)e.getSource(),
                    e.getX(),
                    e.getY());
        }

        /**
         * delete this scantwo
         */
        private void delete()
        {
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            rInterface.insertComment(
                    "Deleting scantwo object");
            rInterface.evaluateCommand(
                    "rm(" +
                    this.getScanTwoResult().getAccessorExpressionString() +
                    ")");
            QtlProjectManager projectManager =
                QtlProjectTree.this.getProjectManager();
            projectManager.notifyActiveProjectModified();
            projectManager.refreshProjectDataStructures();
        }
    }
    
    /**
     * A folder node for holding scan one results
     */
    private static class QtlBasketFolderNode
    extends DefaultMutableTreeNode
    implements Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -4193900392082425451L;

        /**
         * the name this "folder" always has
         */
        private static final String FOLDER_NAME = "QTL Baskets";
        
        private static final String ICON_RESOURCE =
            "/images/qtl-basket-16x16.png";
        private static final Icon SHARED_ICON;
        static
        {
            URL iconUrl = QtlBasketFolderNode.class.getResource(ICON_RESOURCE);
            SHARED_ICON = new ImageIcon(iconUrl);
        }

        /**
         * @see #getCross()
         */
        private final Cross cross;
        
        /**
         * Constructor
         * @param cross
         *          see {@link #getCross()}
         */
        public QtlBasketFolderNode(Cross cross)
        {
            this.cross = cross;
        }

        /**
         * Getter for the cross this scan one folder is for
         * @return the cross
         */
        public Cross getCross()
        {
            return this.cross;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            int childCount = this.getChildCount();
            return FOLDER_NAME +
                   " (" + (childCount == 0 ? "empty" : childCount) + ")";
        }
        
        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return SHARED_ICON;
        }
    }
    
    /**
     * node for holding QTL baskets
     */
    private class QtlBasketNode
    extends DefaultMutableTreeNode
    implements MouseListener
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = 9222373128292290190L;

        /**
         * Constructor
         * @param qtlBasket
         *          the basket
         */
        public QtlBasketNode(QtlBasket qtlBasket)
        {
            super(qtlBasket);
        }
        
        /**
         * Getter for the QTL basket
         * @return
         *          the QTL basket
         */
        public QtlBasket getQtlBasket()
        {
            return (QtlBasket)this.getUserObject();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.getQtlBasket().toString();
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        @SuppressWarnings("serial")
        private void popupTriggered(MouseEvent e)
        {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new EditQtlBasketAction(this.getQtlBasket()));
            popupMenu.add(new FitQtlAction(
                    this.getQtlBasket()));
            popupMenu.addSeparator();
            popupMenu.add(new SafeDeleteAction(
                    this.getQtlBasket().getName(),
                    e.getComponent(),
                    QTL.getInstance().getDesktop())
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete()
                {
                    QtlBasketNode.this.delete();
                }
            });
            
            popupMenu.show(
                    (Component)e.getSource(),
                    e.getX(),
                    e.getY());
        }

        /**
         * Delete this qtl basket
         */
        private void delete()
        {
            this.getQtlBasket().getParentCross().getQtlBasketMap().remove(
                    this.getQtlBasket().getName());
            QtlProjectManager projectManager =
                QtlProjectTree.this.getProjectManager();
            projectManager.notifyActiveProjectModified();
            projectManager.refreshProjectDataStructures();
        }
    }
    
    /**
     * node for holding cross objects
     */
    private class CrossTreeNode
    extends DefaultMutableTreeNode
    implements MouseListener, Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one
         * of these
         */
        private static final long serialVersionUID = 1271152284893969725L;
        
        private static final String ICON_RESOURCE =
            "/images/cross-16x16.png";
        private final Icon icon;
        
        /**
         * @see #getScanOneFolderNode()
         */
        private final ScanOneFolderNode scanOneFolderNode;
        
        /**
         * @see #getScanTwoFolderNode()
         */
        private final ScanTwoFolderNode scanTwoFolderNode;
        
        /**
         * @see #getQtlBasketFolderNode()
         */
        private final QtlBasketFolderNode qtlBasketFolderNode;
        
        /**
         * @see #getFitResultsFolderNode()
         */
        private final FitResultsFolderNode fitResultsFolderNode;
        
        /**
         * Constructor
         * @param cross
         *          the cross
         */
        public CrossTreeNode(Cross cross)
        {
            super(cross);
            
            this.icon = new ImageIcon(CrossTreeNode.class.getResource(
                    ICON_RESOURCE));
            
            this.scanOneFolderNode = new ScanOneFolderNode(cross);
            this.scanTwoFolderNode = new ScanTwoFolderNode(cross);
            this.qtlBasketFolderNode = new QtlBasketFolderNode(cross);
            this.fitResultsFolderNode = new FitResultsFolderNode(cross);
        }
        
        /**
         * Getter for the {@link FitResultsFolderNode}
         * @return
         *          the fit folder
         */
        public FitResultsFolderNode getFitResultsFolderNode()
        {
            return this.fitResultsFolderNode;
        }
        
        /**
         * Getter for the {@link QtlBasketFolderNode}
         * @return
         *          QTL basket folder
         */
        public QtlBasketFolderNode getQtlBasketFolderNode()
        {
            return this.qtlBasketFolderNode;
        }
        
        /**
         * Getter for the {@link ScanOneFolderNode}
         * @return
         *          the scan one folder
         */
        public ScanOneFolderNode getScanOneFolderNode()
        {
            return this.scanOneFolderNode;
        }
        
        /**
         * Getter for the {@link ScanTwoFolderNode}
         * @return
         *          the scan two folder node
         */
        public ScanTwoFolderNode getScanTwoFolderNode()
        {
            return this.scanTwoFolderNode;
        }
        
        /**
         * Getter for the cross
         * @return
         *          the cross
         */
        public Cross getCross()
        {
            return (Cross)this.getUserObject();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return this.getCross().toString();
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        @SuppressWarnings("serial")
        private void popupTriggered(MouseEvent e)
        {
            Cross cross = this.getCross();
            
            JPopupMenu popupMenu = new JPopupMenu(
                    cross.toString());
            popupMenu.add(new ShowCrossSummaryAction(cross));
            popupMenu.add(new RunJittermapAction(cross));
            popupMenu.addSeparator();
            popupMenu.add(new SafeDeleteAction(
                    this.getCross().toString(),
                    e.getComponent(),
                    QTL.getInstance().getDesktop())
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void delete()
                {
                    CrossTreeNode.this.delete();
                }
            });
            
            popupMenu.show(
                    (Component)e.getSource(),
                    e.getX(),
                    e.getY());
        }
        
        /**
         * Delete this cross
         */
        private void delete()
        {
            RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
            rInterface.insertComment("Deleting cross object");
            for(ScanTwoResult scanTwoResult: this.getCross().getScanTwoResults())
            {
                rInterface.evaluateCommand(
                        "rm(" +
                        scanTwoResult.getAccessorExpressionString() +
                        ")");
            }
            
            for(ScanOneResult scanOneResult: this.getCross().getScanOneResults())
            {
                rInterface.evaluateCommand(
                        "rm(" +
                        scanOneResult.getAccessorExpressionString() +
                        ")");
            }
            
            for(FitQtlResult fitResult: this.getCross().getFitQtlResults())
            {
                rInterface.evaluateCommand(
                        "rm(" +
                        fitResult.getAccessorExpressionString() +
                        ")");
            }
            
            rInterface.evaluateCommand(
                    "rm(" +
                    this.getCross().getAccessorExpressionString() +
                    ")");
            
            QtlProjectManager projectManager =
                QtlProjectTree.this.getProjectManager();
            projectManager.notifyActiveProjectModified();
            projectManager.refreshProjectDataStructures();
        }

        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return this.icon;
        }
    }
    
    /**
     * tree node for project
     */
    private static class ProjectTreeNode
    extends DefaultMutableTreeNode
    implements MouseListener, Iconifiable
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = -7499045875913172724L;
        
        private static final String ICON_RESOURCE =
            "/images/project-16x16.png";
        private static final Icon SHARED_ICON;
        static
        {
            URL iconUrl = ProjectTreeNode.class.getResource(ICON_RESOURCE);
            SHARED_ICON = new ImageIcon(iconUrl);
        }
        
        /**
         * the String to use if {@link QtlProject#getName()} is null
         */
        private static final String DEFAULT_PROJECT_NAME = "New J/qtl Project";
        
        /**
         * @param project
         */
        public ProjectTreeNode(QtlProject project)
        {
            super(project);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            int childCount = this.getChildCount();
            String childCountString =
                " (" + (childCount == 0 ? "empty" : childCount) + ")";
            String name = this.getProject().getName();
            return (name == null ? DEFAULT_PROJECT_NAME : name) +
                   childCountString;
        }
        
        /**
         * Getter for the project
         * @return the project
         */
        public QtlProject getProject()
        {
            return (QtlProject)this.getUserObject();
        }

        /**
         * {@inheritDoc}
         */
        public void mouseClicked(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseEntered(MouseEvent e)
        {
        }

        /**
         * Don't care
         * @param e
         *          the event we don't care about
         */
        public void mouseExited(MouseEvent e)
        {
        }


        /**
         * {@inheritDoc}
         */
        public void mousePressed(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {
                this.popupTriggered(e);
            }
        }
        
        /**
         * Respond to a popup trigger event.
         * @param e
         *          the event we're responding to
         */
        private void popupTriggered(MouseEvent e)
        {
            // TODO implement me
        }
        
        /**
         * {@inheritDoc}
         */
        public Icon getIcon()
        {
            return SHARED_ICON;
        }
    }
}
