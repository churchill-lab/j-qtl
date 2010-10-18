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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.jax.qtl.Constants;
import org.jax.qtl.cross.CrossSummary;
import org.jax.qtl.util.GrayTableCellRenderer;
import org.jax.qtl.util.HasSavableTable;


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
public class CrossSummaryPane extends JPanel implements HasSavableTable, Constants {
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -648213606621787793L;
    private String[][] tableContent;
    private static String[] columnNames = new String[]{"Information type","Content"};
    private final CrossSummary crossSummary;
    
    /**
     * Constructor
     * @param crossSummary
     *          the summary to show in the panel
     */
    public CrossSummaryPane(CrossSummary crossSummary) {
        this.crossSummary = crossSummary;
        this.setBackground(Color.white);
        makeTableContent();

        JPanel forColor = new JPanel();
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        GrayTableCellRenderer rowHeaderRenderer = new GrayTableCellRenderer(forColor.getBackground());

        JTable tbCrossSummary = new JTable(this.tableContent, columnNames);
        tbCrossSummary.setEnabled(false);
        tbCrossSummary.getColumnModel().getColumn(0).setCellRenderer(rowHeaderRenderer);
        tbCrossSummary.getColumnModel().getColumn(1).setCellRenderer(renderer);
        JScrollPane tableHolder1 = new JScrollPane(tbCrossSummary);
        tableHolder1.setPreferredSize(new Dimension(600, 210));
        JLabel crossInfo = new JLabel("<html>Cross Summary Table</html>");
        crossInfo.setFont(new Font("sansserif", Font.BOLD, 16));
        crossInfo.setHorizontalAlignment(JLabel.CENTER);
        this.setLayout(new BorderLayout());
        add(crossInfo, BorderLayout.NORTH);
        add(tableHolder1, BorderLayout.CENTER);
    }

    private void makeTableContent() {
        String xChrName = this.crossSummary.getXChromosomeName();
        String autosomes = this.crossSummary.getAutosomeNamesString();
        if ((xChrName == null) || (autosomes == null))
            this.tableContent = new String[11][2];
        else
            this.tableContent = new String[12][2];

        int row = 0;
        this.tableContent[row][0] = "Cross Name";
        this.tableContent[row++][1] = this.crossSummary.getCrossAccessor();
        this.tableContent[row][0] = "Cross Type";
        this.tableContent[row++][1] = this.crossSummary.getCrossType();
        this.tableContent[row][0] = "Number of individuals";
        this.tableContent[row++][1] = this.crossSummary.getIndividualCount() + "";
        this.tableContent[row][0] = "Number of phenotypes";
        this.tableContent[row++][1] = this.crossSummary.getCross().getNumberOfPhenotypes() + "";
        this.tableContent[row][0] = "Percent phenotyped";
        this.tableContent[row++][1] = this.crossSummary.getPercentPhenotypedString();
        this.tableContent[row][0] = "Number of chromosomes";
        this.tableContent[row++][1] = this.crossSummary.getCross().getNumberOfChromosomes() + "";
        if (autosomes != null) {
            this.tableContent[row][0] = "Autosomes";
            this.tableContent[row++][1] = autosomes;
        }
        if (xChrName != null) {
            this.tableContent[row][0] = "X chromosome";
            this.tableContent[row++][1] = xChrName;
        }
        this.tableContent[row][0] = "Total markers";
        this.tableContent[row++][1] = this.crossSummary.getTotalMarkerCount() + "";
        this.tableContent[row][0] = "Number of markers on each chromosome";
        this.tableContent[row++][1] = this.crossSummary.getMarkersPerChromosomeString();
        this.tableContent[row][0] = "Percent genotyped";
        this.tableContent[row++][1] = this.crossSummary.getPercentGenotypedString();
        this.tableContent[row][0] = "Genotypes (%)";
        this.tableContent[row++][1] = this.crossSummary.getGenotypeRatiosString();
    }

    /**
     * {@inheritDoc}
     */
    public String getSavableTable() {
        String result = TAB + "Cross Summary" + EOL + EOL;
        int numCols = columnNames.length;
/*        result += columnNames[0];
        for (int i=1; i<numCols; i++) {
            result += TAB + columnNames[i];
        }
        result += EOL;*/
        int numRows = this.tableContent.length;
        for (int i=0; i<numRows; i++) {
            result += this.tableContent[i][0] + ":";
            for (int j=1; j<numCols; j++) {
                result += TAB + this.tableContent[i][j];
            }
            result += EOL;
        }

        return result;
    }
}
