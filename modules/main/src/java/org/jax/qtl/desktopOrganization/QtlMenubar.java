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
package org.jax.qtl.desktopOrganization;

import java.awt.event.KeyEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jax.qtl.QTL;
import org.jax.qtl.action.EditPreferencesAction;
import org.jax.qtl.action.OpenUrlAction;
import org.jax.qtl.action.QtlAction;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.cross.RunJittermapAction;
import org.jax.qtl.cross.gui.LoadCrossAction;
import org.jax.qtl.cross.gui.ShowHistogramAction;
import org.jax.qtl.cross.gui.ShowScatterPlotAction;
import org.jax.qtl.cross.gui.SimulateCrossAction;
import org.jax.qtl.fit.gui.CreateNewQtlBasketAction;
import org.jax.qtl.fit.gui.FitQtlAction;
import org.jax.qtl.fit.gui.ShowFitQtlResultsAction;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.qtl.project.gui.CreateQtlProjectAction;
import org.jax.qtl.project.gui.ExportQtlRScriptAction;
import org.jax.qtl.project.gui.RecentQtlProjectsMenu;
import org.jax.qtl.project.gui.SaveQtlProjectAction;
import org.jax.qtl.project.gui.SaveQtlProjectAsAction;
import org.jax.qtl.scan.gui.CalculateGenotypeProbabilitiesAction;
import org.jax.qtl.scan.gui.PlotScanOneResultAction;
import org.jax.qtl.scan.gui.PlotScanTwoResultAction;
import org.jax.qtl.scan.gui.ScanOneAction;
import org.jax.qtl.scan.gui.ScanOneSummaryAction;
import org.jax.qtl.scan.gui.ScanTwoAction;
import org.jax.qtl.scan.gui.ScanTwoSummaryAction;
import org.jax.qtl.scan.gui.SimulateGenotypeProbabilitiesAction;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.project.LoadProjectAction;
import org.jax.util.concurrent.MultiTaskProgressPanel;
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
public class QtlMenubar extends JMenuBar {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6991442656108162826L;
    private HelpBroker hb;
    private HelpSet hs;

    private JMenu fileMenu, analysisMenu, helpMenu;
    private JMenuItem exitMenuItem;
    private JMenu phenotypeCheckingMenu, genotypeCheckingMenu, mainScanMenu, pairScanMenu, fitQtlModelMenu;
    private JMenu geneticMapMenu;
    private JMenuItem calcGenoProbMenuItem, imputationMenuItem;
    private JMenuItem helpTopicsMenuItem, linkJQTLMenuItem, linkRQTLMenuItem, linkQTLArchiveMenuItem, aboutMenuItem;

    // help page settings
    static final String URL_JQTL =
        "http://research.jax.org/faculty/churchill/software/Jqtl/index.html";
    static final String URL_RQTL =
        "http://www.rqtl.org/index.html";
    static final String URL_QTL_ARCHIVE =
        "http://research.jax.org/faculty/churchill/datasets/qtl/qtlarchive/index.html";

    /**
     * getter for the help broker
     * @return
     *          the help broker
     */
    public HelpBroker getHelpBroker() {return this.hb;}
    
    /**
     * Getter for the help set
     * @return
     *          the help set
     */
    public HelpSet getHelpSet() {return this.hs;}

    /**
     * Constructor
     * @param windowMenu
     *          the menu to use
     */
    public QtlMenubar(JMenu windowMenu) {
        createHelpBroker();

        // top level menus
        this.fileMenu = new JMenu("File");
        this.analysisMenu = new JMenu("Analysis");
        this.helpMenu = new JMenu("Help");
        add(this.fileMenu);
        add(this.analysisMenu);
        add(windowMenu);
        add(this.helpMenu);

        makeFileMenu();
        makeAnalysisMenu();
        makeHelpMenu();
    }

    private void createHelpBroker() {
        try {
            // load from JAR
            ClassLoader cl = QTL.class.getClassLoader();
            URL hsURL = HelpSet.findHelpSet(cl, "org-jax-qtl-help/j-qtl.hs");
            this.hs = new HelpSet(null, hsURL);
            this.hb = this.hs.createHelpBroker();

        }
        catch(Exception e) {e.printStackTrace();}
    }

    // menuItems under it
    private void makeFileMenu() {
        // "File" menu
        this.fileMenu.add(new CreateQtlProjectAction());
        this.fileMenu.add(new LoadProjectAction()
        {
            /**
             * Every serializable is supposed to have one of these
             */
            private static final long serialVersionUID = -1207406252373433985L;

            /**
             * {@inheritDoc}
             */
            @Override
            protected RApplicationConfigurationManager getConfigurationManager()
            {
                return QtlApplicationConfigurationManager.getInstance();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected JFrame getParentFrame()
            {
                return QTL.getInstance().getApplicationFrame();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected ProjectManager getProjectManager()
            {
                return QtlProjectManager.getInstance();
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected String getProjectTypeName()
            {
                return "J/qtl";
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected MultiTaskProgressPanel getTaskProgressPanel()
            {
                return QTL.getInstance().getApplicationFrame().getTaskProgressPanel();
            }
        });
        this.fileMenu.add(new RecentQtlProjectsMenu());
        
        this.fileMenu.addSeparator();
        
        this.fileMenu.add(new SaveQtlProjectAction());
        this.fileMenu.add(new SaveQtlProjectAsAction());
        this.fileMenu.add(new ExportQtlRScriptAction());

        this.fileMenu.addSeparator();

        this.fileMenu.add(new LoadCrossAction());
        this.fileMenu.add(new SimulateCrossAction());
        
        this.fileMenu.addSeparator();
        
        this.fileMenu.add(new EditPreferencesAction());
        
        this.fileMenu.addSeparator();

        this.exitMenuItem = new JMenuItem(QtlAction.EXIT);
        this.exitMenuItem.addActionListener(new QtlAction(QtlAction.EXIT, QtlAction.EXIT, null));
        this.fileMenu.add(this.exitMenuItem);
    }

    private void makeAnalysisMenu() {
        // sub-menu of "Analysis" menu
        this.phenotypeCheckingMenu = new JMenu("Phenotype Checking");
        this.genotypeCheckingMenu = new JMenu("Genotype Checking");
        this.mainScanMenu = new JMenu("Main Scan");
        this.pairScanMenu = new JMenu("Pair Scan");
        this.fitQtlModelMenu = new JMenu("Fit QTL Model");

        this.analysisMenu.add(this.phenotypeCheckingMenu);
        this.analysisMenu.add(this.genotypeCheckingMenu);

        // menu items under Analysis Menu
        this.calcGenoProbMenuItem = new JMenuItem(
                new CalculateGenotypeProbabilitiesAction());
        this.analysisMenu.add(this.calcGenoProbMenuItem);

        this.imputationMenuItem = new JMenuItem(
                new SimulateGenotypeProbabilitiesAction());
        this.analysisMenu.add(this.imputationMenuItem);

        this.analysisMenu.add(this.mainScanMenu);
        this.analysisMenu.add(this.pairScanMenu);
        this.analysisMenu.add(this.fitQtlModelMenu);

        // menu items under each sub-menu of Analysis menu
        this.phenotypeCheckingMenu.add(new ShowScatterPlotAction());

        this.phenotypeCheckingMenu.add(new ShowHistogramAction());

        this.phenotypeCheckingMenu.addSeparator();

        this.geneticMapMenu = new JMenu("Genetic Map");
        this.genotypeCheckingMenu.add(this.geneticMapMenu);
        this.geneticMapMenu.add(new QtlAction(QtlAction.GENETIC_MAP, QtlAction.GENETIC_MAP, null));
        this.geneticMapMenu.add(new QtlAction(QtlAction.EST_MAP, QtlAction.EST_MAP, null));
        
        this.genotypeCheckingMenu.add(new QtlAction(QtlAction.RF_PLOT, QtlAction.RF_PLOT, null));
        this.genotypeCheckingMenu.add(new QtlAction(QtlAction.GENO_PLOT, QtlAction.GENO_PLOT, null));
        this.genotypeCheckingMenu.add(new RunJittermapAction());

        this.mainScanMenu.add(new ScanOneAction());
        this.mainScanMenu.add(new PlotScanOneResultAction());
        this.mainScanMenu.add(new ScanOneSummaryAction());

        this.pairScanMenu.add(new ScanTwoAction());
        this.pairScanMenu.add(new PlotScanTwoResultAction());
        this.pairScanMenu.add(new ScanTwoSummaryAction());

        this.fitQtlModelMenu.add(new CreateNewQtlBasketAction());
        this.fitQtlModelMenu.add(new FitQtlAction(null));
        this.fitQtlModelMenu.add(new ShowFitQtlResultsAction(null));
    }

    private void loadMainHelpPage(JMenuItem item) {
        CSH.setHelpIDString(item, "FirstTopic");
        item.addActionListener(new CSH.DisplayHelpFromSource(this.hb));
    }

    private void makeHelpMenu() {
        this.helpTopicsMenuItem = new JMenuItem(QtlAction.HELP_TOPIC);
        loadMainHelpPage(this.helpTopicsMenuItem);
        this.helpTopicsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

        this.helpMenu.add(this.helpTopicsMenuItem);

        this.helpMenu.addSeparator();

        this.linkJQTLMenuItem = new JMenuItem(new OpenUrlAction(
                "Visit J/qtl Website",
                URL_JQTL));
        this.helpMenu.add(this.linkJQTLMenuItem);

        this.linkRQTLMenuItem = new JMenuItem(new OpenUrlAction(
                "Visit R/qtl Website",
                URL_RQTL));
        this.helpMenu.add(this.linkRQTLMenuItem);

        this.linkQTLArchiveMenuItem = new JMenuItem(new OpenUrlAction(
                "Visit QTL Archive",
                URL_QTL_ARCHIVE));
        this.helpMenu.add(this.linkQTLArchiveMenuItem);

        this.helpMenu.addSeparator();

        this.aboutMenuItem = new JMenuItem(QtlAction.ABOUT);
        this.aboutMenuItem.addActionListener(new QtlAction(QtlAction.ABOUT, QtlAction.ABOUT, null));
        this.helpMenu.add(this.aboutMenuItem);
    }
}
