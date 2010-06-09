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

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;

import org.jax.analyticgraph.framework.Graph2DComponent;
import org.jax.analyticgraph.framework.SimpleGraphCoordinateConverter;
import org.jax.analyticgraph.graph.AxisRenderingGraph;
import org.jax.qtl.QTL;
import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.EffectPlotCommandBuilder;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.gui.EffectPlot.EffectPlotData;
import org.jax.qtl.cross.gui.EffectPlot.EffectPlotDataPoint;
import org.jax.qtl.gui.SimpleGraphContainerPanel;
import org.jax.r.RAssignmentCommand;
import org.jax.r.RCommand;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.RObject;
import org.jax.r.jriutilities.SilentRCommand;
import org.jax.util.gui.desktoporganization.Desktop;
import org.rosuda.JRI.REXP;

/**
 * Action class for showing effect plots
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowEffectPlotAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1231114942692497182L;
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            ShowEffectPlotAction.class.getName());
    
    /**
     * Anything followed by a final dot separator followed by anything
     */
    private static final Pattern EFFECT_DATA_NAME_PATTERN =
        Pattern.compile("(.*)\\.(.*)");
    
    /**
     * We need a variable name that we can use as a temporary place holder
     * for effect plot data
     */
    private static final String TEMP_EFFECT_PLOT_DATA_ACCESSOR =
        "temp.effect.plot.data";
    
    private static final String EFFECT_MEANS_SUFFIX = "$Means";
    
    private static final String EFFECT_STD_ERROR_SSUFFIX = "$SEs";
    
    private final EffectPlotCommandBuilder effectPlotCommandBuilder;

    private final String phenotypeName;

    /**
     * Constructor
     * @param cross
     *          the cross
     * @param phenotypeName
     *          the index of the phenotype we're showing an effect for
     * @param marker
     *          the genetic marker that we're showing an effect for
     */
    public ShowEffectPlotAction(
            Cross cross,
            String phenotypeName,
            GeneticMarker marker)
    {
        super("Show Effect Plot for " + marker.getMarkerName() + " ...");
        this.phenotypeName = phenotypeName;
        
        this.effectPlotCommandBuilder = new EffectPlotCommandBuilder();
        
        this.effectPlotCommandBuilder.setCross(cross);
        this.effectPlotCommandBuilder.setFirstMarker(marker);
        
        List<String> allPhenotypeNames =
            Arrays.asList(cross.getPhenotypeData().getDataNames());
        this.effectPlotCommandBuilder.setPhenotypeIndex(
                allPhenotypeNames.indexOf(phenotypeName));
    }
    
    /**
     * Constructor
     * @param cross
     *          the cross
     * @param phenotypeName
     *          the index of the phenotype we're showing an effect for
     * @param marker1
     *          the 1st genetic marker that we're showing an effect for
     * @param marker2
     *          the 2nd genetic marker that we're showing an effect for
     */
    public ShowEffectPlotAction(
            Cross cross,
            String phenotypeName,
            GeneticMarker marker1,
            GeneticMarker marker2)
    {
        super("Show Effect Plot for " + marker1.getMarkerName() + " and " + marker2.getMarkerName() + " ...");
        this.phenotypeName = phenotypeName;
        
        this.effectPlotCommandBuilder = new EffectPlotCommandBuilder();
        
        this.effectPlotCommandBuilder.setCross(cross);
        this.effectPlotCommandBuilder.setFirstMarker(marker1);
        this.effectPlotCommandBuilder.setSecondMarker(marker2);
        
        List<String> allPhenotypeNames =
            Arrays.asList(cross.getPhenotypeData().getDataNames());
        this.effectPlotCommandBuilder.setPhenotypeIndex(
                allPhenotypeNames.indexOf(phenotypeName));
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        EffectPlotData effectsPlotData = this.extractEffectsPlotData();
        EffectPlot effectPlot = new EffectPlot(effectsPlotData);
        
        AxisRenderingGraph axisGraph = new AxisRenderingGraph(
                new SimpleGraphCoordinateConverter());
        axisGraph.setInteriorGraph(effectPlot);
        
        Graph2DComponent graphComponent = new Graph2DComponent();
        graphComponent.addGraph2D(axisGraph);
        
        SimpleGraphContainerPanel simpleGraphContainerPanel =
            new SimpleGraphContainerPanel(graphComponent, effectPlot);
        
        Desktop desktop = QTL.getInstance().getDesktop();
        desktop.createInternalFrame(
                simpleGraphContainerPanel,
                this.phenotypeName + " Effect Plot",
                null,
                this.effectPlotCommandBuilder.getCommand().getCommandText());
    }
    
    /**
     * Pick the plot data out of the effect plot R object.
     * @return
     *          the effect plot data suitable for our graphs
     */
    private EffectPlotData extractEffectsPlotData()
    {
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        
        synchronized(rInterface)
        {
            // create a temporary variable with the effect data in it
            RCommand effectPlotCommand = this.effectPlotCommandBuilder.getCommand();
            RAssignmentCommand tempAssignment = new RAssignmentCommand(
                    TEMP_EFFECT_PLOT_DATA_ACCESSOR,
                    effectPlotCommand.getCommandText());
            rInterface.evaluateCommandNoReturn(
                    new SilentRCommand(tempAssignment));
            
            // poke at the data... see if it's 1D or 2D (ie: a matrix)
            RObject meansAccessor = new RObject(
                    rInterface,
                    TEMP_EFFECT_PLOT_DATA_ACCESSOR + EFFECT_MEANS_SUFFIX);
            RObject stdErrorsAccessor = new RObject(
                    rInterface,
                    TEMP_EFFECT_PLOT_DATA_ACCESSOR + EFFECT_STD_ERROR_SSUFFIX);
            
            final EffectPlotData returnData;
            if(JRIUtilityFunctions.inheritsRClass(meansAccessor, "matrix"))
            {
                returnData = this.extractDataForTwoEffects(
                        meansAccessor,
                        stdErrorsAccessor);
            }
            else
            {
                returnData = this.extractDataForOneEffect(
                        meansAccessor,
                        stdErrorsAccessor);
            }
            
            // clean up the temp data before we return
            rInterface.evaluateCommand(new SilentRCommand(
                    "rm(" + TEMP_EFFECT_PLOT_DATA_ACCESSOR + ")"));
            return returnData;
        }
    }

    /**
     * Extract the effect plot data assuming that it is a 2D effect
     * @param meansAccessor
     *          the means R object
     * @param stdErrorsAccessor
     *          the standard errors R object
     * @return
     *          the plot data
     */
    private EffectPlotData extractDataForTwoEffects(
            RObject meansAccessor,
            RObject stdErrorsAccessor)
    {
        String[] meansColNames =
            JRIUtilityFunctions.getColumnNames(meansAccessor);
        String[] stdErrorsColNames =
            JRIUtilityFunctions.getColumnNames(stdErrorsAccessor);
        
        // make sure they're both equal
        if(!Arrays.equals(meansColNames, stdErrorsColNames))
        {
            LOG.severe(
                    meansAccessor.getAccessorExpressionString() +
                    " column names dont match up with " +
                    stdErrorsAccessor.getAccessorExpressionString() +
                    " column names");
            return null;
        }
        
        String[] meansRowNames =
            JRIUtilityFunctions.getRowNames(meansAccessor);
        String[] stdErrorRowNames =
            JRIUtilityFunctions.getRowNames(stdErrorsAccessor);
        
        // make sure they're both equal
        if(!Arrays.equals(meansRowNames, stdErrorRowNames))
        {
            LOG.severe(
                    meansAccessor.getAccessorExpressionString() +
                    " row names dont match up with " +
                    stdErrorsAccessor.getAccessorExpressionString() +
                    " row names");
            return null;
        }
        
        EffectCategoryAndPointNames columnCategoryAndPoints =
            ShowEffectPlotAction.extractCategoryAndPointNames(
                    meansColNames);
        EffectCategoryAndPointNames rowCategoryAndPoints =
            ShowEffectPlotAction.extractCategoryAndPointNames(
                    meansRowNames);
        
        // strangely enough, the rows will become the main grouping and the
        // columns will be the line values
        EffectPlotDataPoint[][] dataLines =
            new EffectPlotDataPoint[meansColNames.length][];
        for(int lineIndex = 0; lineIndex < dataLines.length; lineIndex++)
        {
            EffectPlotDataPoint[] currLine =
                new EffectPlotDataPoint[meansRowNames.length];
            dataLines[lineIndex] = currLine;
            
            // read in the current columns
            REXP meansColumnRExpression = meansAccessor.getRInterface().evaluateCommand(
                    new SilentRCommand(
                            meansAccessor.getAccessorExpressionString() +
                            "[," + (lineIndex + 1) + "]"));
            double[] meansLineValues = meansColumnRExpression.asDoubleArray();
            
            REXP stdErrorsColumnRExpression = stdErrorsAccessor.getRInterface().evaluateCommand(
                    new SilentRCommand(
                            stdErrorsAccessor.getAccessorExpressionString() +
                            "[," + (lineIndex + 1) + "]"));
            double[] stdErrorsValues = stdErrorsColumnRExpression.asDoubleArray();
            
            for(int pointIndex = 0; pointIndex < stdErrorsValues.length; pointIndex++)
            {
                EffectPlotDataPoint currPoint = new EffectPlotDataPoint(
                        meansLineValues[pointIndex],
                        stdErrorsValues[pointIndex]);
                currLine[pointIndex] = currPoint;
            }
        }
        
        return new EffectPlotData(
                rowCategoryAndPoints.getCategory(),
                this.phenotypeName,
                columnCategoryAndPoints.getCategory(),
                rowCategoryAndPoints.getPointNames(),
                dataLines,
                columnCategoryAndPoints.getPointNames());
    }

    /**
     * Extract the effect plot data assuming that it is a 1D effect
     * @param meansAccessor
     *          the means R object
     * @param stdErrorsAccessor
     *          the standard errors R object
     * @return
     *          the plot data
     */
    private EffectPlotData extractDataForOneEffect(
            RObject meansAccessor,
            RObject stdErrorsAccessor)
    {
        String[] meansNames =
            JRIUtilityFunctions.getNames(meansAccessor);
        String[] stdErrorsNames =
            JRIUtilityFunctions.getNames(stdErrorsAccessor);
        
        // make sure they're equal
        if(!Arrays.equals(meansNames, stdErrorsNames))
        {
            LOG.severe(
                    meansAccessor.getAccessorExpressionString() +
                    " names dont match up with " +
                    stdErrorsAccessor.getAccessorExpressionString() +
                    " names");
            return null;
        }
        
        EffectCategoryAndPointNames categoryAndPoints =
            ShowEffectPlotAction.extractCategoryAndPointNames(meansNames);
        
        // extract the effect line data
        REXP meansRExpression = meansAccessor.getRInterface().evaluateCommand(
                new SilentRCommand(meansAccessor.getAccessorExpressionString()));
        double[] meansValues = meansRExpression.asDoubleArray();
        
        REXP stdErrorsRExpression = stdErrorsAccessor.getRInterface().evaluateCommand(
                new SilentRCommand(stdErrorsAccessor.getAccessorExpressionString()));
        double[] stdErrorValues = stdErrorsRExpression.asDoubleArray();
        
        EffectPlotDataPoint[] line = new EffectPlotDataPoint[meansValues.length];
        for(int pointIndex = 0; pointIndex < line.length; pointIndex++)
        {
            line[pointIndex] = new EffectPlotDataPoint(
                    meansValues[pointIndex],
                    stdErrorValues[pointIndex]);
        }
        
        return new EffectPlotData(
                categoryAndPoints.getCategory(),
                this.phenotypeName,
                categoryAndPoints.getPointNames(),
                line);
    }
    
    /**
     * Parse the raw column or row names to figure out what the effect point
     * and effect category names are
     * @param dataNames
     *          the names to parse
     * @return
     *          the parsed names
     */
    private static EffectCategoryAndPointNames extractCategoryAndPointNames(
            String[] dataNames)
    {
        String category = null;
        String[] pointNames = new String[dataNames.length];
        
        for(int nameIndex = 0; nameIndex < pointNames.length; nameIndex++)
        {
            Matcher currMatcher =
                EFFECT_DATA_NAME_PATTERN.matcher(dataNames[nameIndex]);
            if(currMatcher.matches())
            {
                String categoryGroup = currMatcher.group(1);
                String pointNameGroup = currMatcher.group(2);
                
                if(category != null)
                {
                    // the category should be equal to the category group
                    if(!category.equals(categoryGroup))
                    {
                        LOG.severe(
                                "failed to parse effect plot category. " +
                                "expected to see " + category + " but saw " +
                                categoryGroup + " instead");
                        return null;
                    }
                }
                else
                {
                    category = categoryGroup;
                }
                
                pointNames[nameIndex] = pointNameGroup;
            }
            else
            {
                LOG.severe(
                        "cannot extract effect plot data because the string " +
                        dataNames[nameIndex] + " doesn't match the pattern " +
                        "that we were expecting");
                return null;
            }
        }
        
        return new EffectCategoryAndPointNames(category, pointNames);
    }
    
    /**
     * a simple holder class for the effect category an point names
     */
    private static class EffectCategoryAndPointNames
    {
        private final String category;
        
        private final String[] pointNames;

        /**
         * @param category
         * @param pointNames
         */
        public EffectCategoryAndPointNames(String category, String[] pointNames)
        {
            this.category = category;
            this.pointNames = pointNames;
        }
        
        /**
         * @return the category
         */
        public String getCategory()
        {
            return this.category;
        }
        
        /**
         * @return the pointNames
         */
        public String[] getPointNames()
        {
            return this.pointNames;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object otherObject)
        {
            if(otherObject instanceof EffectCategoryAndPointNames)
            {
                EffectCategoryAndPointNames otherEffectCategoryAndPointNames =
                    (EffectCategoryAndPointNames)otherObject;
                return this.category.equals(otherEffectCategoryAndPointNames.category) &&
                       Arrays.equals(
                               this.pointNames,
                               otherEffectCategoryAndPointNames.pointNames);
            }
            else
            {
                return false;
            }
        }
    }
}
