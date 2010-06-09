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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.SexAwareGeneticMap;


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
public class EstimateMapPane extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2440669745683476013L;
    
    private final Cross cross;
    private final List<SexAwareGeneticMap> estimatedGeneticMaps;
    
    /**
     * Constructor
     * @param cross
     *          the cross
     * @param estimatedGeneticMaps
     *          the estimated map for the cross
     */
    public EstimateMapPane(
            Cross cross,
            List<SexAwareGeneticMap> estimatedGeneticMaps)
    {
        this.cross = cross;
        this.estimatedGeneticMaps = estimatedGeneticMaps;
        setCurrentPane(makeContentPane());
        setVisible(true);
    }

    private void setCurrentPane(JPanel content)
    {
        removeAll();
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(content);
        this.validate();
    }

    private JPanel makeContentPane()
    {
        JButton replaceMapButton = new JButton("Replace Map");
        replaceMapButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // TODO send R command to replace the genetic map
            }
        });
        
        JPanel replaceMapButtonPane = new JPanel();
        replaceMapButtonPane.add(replaceMapButton);
        
        CompareGeneticMapPlot estMap = new CompareGeneticMapPlot(
                this.cross,
                this.estimatedGeneticMaps);
        JPanel result = new JPanel(new BorderLayout());
        result.add(estMap, BorderLayout.CENTER);
        result.add(replaceMapButtonPane, BorderLayout.SOUTH);
        return result;
    }
}
