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

package org.jax.qtl.cross.gui;

import java.awt.CardLayout;

import javax.swing.JPanel;

import org.jax.qtl.cross.SimulateCrossCommandBuilder;
import org.jax.qtl.cross.SimulateMapCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditor;
import org.jax.r.gui.RCommandEditorListener;
import org.jax.r.gui.RCommandEditorPanel;

/**
 * The panel that holds all of the other panels needed to simulate a cross
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class AllSimulateCrossPanels extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3881750077019866478L;

    private final static String SIM_MAP_PANEL_ID = "sim.map";
    
    private final SimulateMapPanel simulateMapPanel;
    
    private final static String SIM_CROSS_PANEL1_ID = "sim.cross1";
    
    private final SimulateCrossPanelOne simulateCrossPanelOne;
    
    private final static String SIM_CROSS_PANEL2_ID = "sim.cross2";
    
    private final SimulateCrossPanelTwo simulateCrossPanelTwo;
    
    private final CardLayout cardLayout;

    private final SimulateMapCommandBuilder simulateMapCommandBuilder;

    private final SimulateCrossCommandBuilder simulateCrossCommandBuilder;
    
    private volatile JPanel activePanel;

    private final RCommandEditorListener internalCommandEditorListener =
        new RCommandEditorListener()
        {
            public void commandModified(RCommandEditor editor)
            {
                AllSimulateCrossPanels.this.fireCommandModified();
            }
        };
    
    /**
     * Constructor
     * @param simulateMapCommandBuilder
     *          the simulate map command
     * @param simulateCrossCommandBuilder
     *          the simulate cross command
     */
    public AllSimulateCrossPanels(
            SimulateMapCommandBuilder simulateMapCommandBuilder,
            SimulateCrossCommandBuilder  simulateCrossCommandBuilder)
    {
        this.simulateMapCommandBuilder = simulateMapCommandBuilder;
        this.simulateCrossCommandBuilder = simulateCrossCommandBuilder;
        this.simulateMapPanel = new SimulateMapPanel(
                simulateMapCommandBuilder);
        this.simulateCrossPanelOne = new SimulateCrossPanelOne(
                simulateMapCommandBuilder,
                simulateCrossCommandBuilder);
        this.simulateCrossPanelTwo = new SimulateCrossPanelTwo(
                simulateMapCommandBuilder,
                simulateCrossCommandBuilder);
        
        this.simulateMapPanel.addRCommandEditorListener(
                this.internalCommandEditorListener);
        this.simulateCrossPanelOne.addRCommandEditorListener(
                this.internalCommandEditorListener);
        this.simulateCrossPanelTwo.addRCommandEditorListener(
                this.internalCommandEditorListener);
        
        this.cardLayout = new CardLayout();
        this.setLayout(this.cardLayout);
        
        this.add(this.simulateMapPanel, SIM_MAP_PANEL_ID);
        this.add(this.simulateCrossPanelOne, SIM_CROSS_PANEL1_ID);
        this.add(this.simulateCrossPanelTwo, SIM_CROSS_PANEL2_ID);
        
        this.activePanel = this.simulateMapPanel;
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        this.simulateCrossCommandBuilder.setMapAccessorString(
                this.simulateMapCommandBuilder.getCommand().getCommandText());
        return new RCommand[] {this.simulateCrossCommandBuilder.getCommand()};
    }

    /**
     * go forward one panel
     */
    public void back()
    {
        if(this.activePanel == this.simulateCrossPanelOne)
        {
            this.cardLayout.show(this, SIM_MAP_PANEL_ID);
            this.activePanel = this.simulateMapPanel;
        }
        else if(this.activePanel == this.simulateCrossPanelTwo)
        {
            this.cardLayout.show(this, SIM_CROSS_PANEL1_ID);
            this.activePanel = this.simulateCrossPanelOne;
        }
    }

    /**
     * go back one panel
     */
    public void next()
    {
        if(this.activePanel == this.simulateMapPanel)
        {
            if(this.simulateMapPanel.validateData())
            {
                this.cardLayout.show(this, SIM_CROSS_PANEL1_ID);
                this.activePanel = this.simulateCrossPanelOne;
            }
        }
        else if(this.activePanel == this.simulateCrossPanelOne)
        {
            if(this.simulateCrossPanelOne.validateData())
            {
                this.simulateCrossPanelTwo.refreshGui();
                this.cardLayout.show(this, SIM_CROSS_PANEL2_ID);
                this.activePanel = this.simulateCrossPanelTwo;
            }
        }
    }

    /**
     * Determine if its valid to do a next
     * @return
     *          true if the user should be able to do a next
     */
    public boolean isNextValid()
    {
        return this.activePanel != this.simulateCrossPanelTwo;
    }

    /**
     * Determine if it's valid for the user to go back
     * @return
     *          true if the user should be able to go back
     */
    public boolean isBackValid()
    {
        return this.activePanel != this.simulateMapPanel;
    }

    /**
     * Determine if it's valid for the user to finish
     * @return
     *          true if the user should be able to finish
     */
    public boolean isFinishValid()
    {
        return this.activePanel == this.simulateCrossPanelTwo;
    }

    /**
     * Determine if it's valid to finish
     * @return
     *          true iff it's valid to finish
     */
    public boolean validateData()
    {
        return this.simulateMapPanel.validateData() &&
               this.simulateCrossPanelOne.validateData() &&
               this.simulateCrossPanelTwo.validateData();
    }
}
