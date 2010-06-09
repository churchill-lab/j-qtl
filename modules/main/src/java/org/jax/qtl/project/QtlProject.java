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

package org.jax.qtl.project;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.qtl.cross.GeneticMarker;
import org.jax.qtl.cross.GeneticMarkerPair;
import org.jax.qtl.cross.MarkerPairQtlBasketItem;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.cross.QtlBasketItem;
import org.jax.qtl.cross.SingleMarkerQtlBasketItem;
import org.jax.qtl.jaxbgenerated.CommentType;
import org.jax.qtl.jaxbgenerated.CrossType;
import org.jax.qtl.jaxbgenerated.GeneticMarkerType;
import org.jax.qtl.jaxbgenerated.JQtlProjectMetadata;
import org.jax.qtl.jaxbgenerated.QtlBasketItemType;
import org.jax.qtl.jaxbgenerated.QtlBasketType;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.project.RProject;

/**
 * The data structure that is at the root of a QTL project.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlProject extends RProject
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            QtlProject.class.getName());
    
    /**
     * the data model for this project
     */
    private final QtlDataModel dataModel;
    
    /**
     * our JAXB object factory
     */
    private final org.jax.qtl.jaxbgenerated.ObjectFactory objectFactory =
        new org.jax.qtl.jaxbgenerated.ObjectFactory();
    
    /**
     * Constructor for restoring a saved project.
     * @param rInterface
     *          the R interface for this project
     * @param projectMetadata
     *          the project metadata
     */
    public QtlProject(
            RInterface rInterface,
            JQtlProjectMetadata projectMetadata)
    {
        super(rInterface, projectMetadata);
        
        this.dataModel = new QtlDataModel(rInterface);
        this.restoreCrossMetaData(projectMetadata.getCross());
    }
    
    private void restoreCrossMetaData(List<CrossType> jaxbCrosses)
    {
        // iterate through the JAXB cross metadata, and add the
        // metadata to our projects data structures
        Map<String, Cross> crossMap = this.dataModel.getCrossMap();
        for(CrossType jaxbCross: jaxbCrosses)
        {
            Cross matchingCross = crossMap.get(jaxbCross.getCrossIdentifier());
            if(matchingCross == null)
            {
                LOG.warning(
                        "could not find cross named " +
                        jaxbCross.getCrossIdentifier() +
                        " in restored R workspace");
            }
            else
            {
                // add all QTL baskets to the current cross
                List<QtlBasketType> jaxbQtlBaskets = jaxbCross.getQtlBasket();
                if(!jaxbQtlBaskets.isEmpty())
                {
                    for(QtlBasketType jaxbQtlBasket: jaxbQtlBaskets)
                    {
                        QtlBasket qtlBasket = new QtlBasket(
                                matchingCross,
                                jaxbQtlBasket.getBasketName());
                        
                        for(QtlBasketItemType jaxbItem: jaxbQtlBasket.getQtlBasketItem())
                        {
                            if(jaxbItem.getMarker().size() == 1)
                            {
                                // since the marker size is 1, we're adding
                                // a single qtl maker item
                                GeneticMarkerType currJaxbMarker =
                                    jaxbItem.getMarker().get(0);
                                GeneticMarker currMarker = new GeneticMarker(
                                        currJaxbMarker.getMarkerName(),
                                        currJaxbMarker.getChromosomeName(),
                                        currJaxbMarker.getMarkerPositionCentimorgans());
                                
                                SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                                    new SingleMarkerQtlBasketItem(
                                            currMarker,
                                            jaxbItem.getComment().getContent());
                                
                                qtlBasket.getContents().add(
                                        singleMarkerQtlBasketItem);
                            }
                            else if(jaxbItem.getMarker().size() == 2)
                            {
                                // since the marker size is 2, we're adding
                                // a marker pair
                                GeneticMarkerType jaxbMarker1 =
                                    jaxbItem.getMarker().get(0);
                                GeneticMarker marker1 = new GeneticMarker(
                                        jaxbMarker1.getMarkerName(),
                                        jaxbMarker1.getChromosomeName(),
                                        jaxbMarker1.getMarkerPositionCentimorgans());
                                
                                GeneticMarkerType jaxbMarker2 =
                                    jaxbItem.getMarker().get(1);
                                GeneticMarker marker2 = new GeneticMarker(
                                        jaxbMarker2.getMarkerName(),
                                        jaxbMarker2.getChromosomeName(),
                                        jaxbMarker2.getMarkerPositionCentimorgans());
                                
                                GeneticMarkerPair currMarkerPair = new GeneticMarkerPair(
                                        marker1,
                                        marker2);
                                
                                MarkerPairQtlBasketItem markerPairQtlBasketItem =
                                    new MarkerPairQtlBasketItem(
                                            currMarkerPair,
                                            jaxbItem.getComment().getContent());
                                
                                qtlBasket.getContents().add(
                                        markerPairQtlBasketItem);
                            }
                            else
                            {
                                // we can't deal with anything but 1 or 2!
                                LOG.severe(
                                        "bad marker count in QTL basket " + 
                                        "configuration: " + jaxbItem.getMarker().size());
                            }
                        }
                        
                        // toss the qtl basket map into the cross that it
                        // belongs to
                        matchingCross.getQtlBasketMap().put(
                                jaxbQtlBasket.getBasketName(),
                                qtlBasket);
                    }
                }
            }
        }
    }
    
    /**
     * Constructor for creating a new project
     * @param rInterface
     *          the R interface for this project
     */
    public QtlProject(
            RInterface rInterface)
    {
        super(rInterface);
        
        this.dataModel = new QtlDataModel(rInterface);
    }
    
    /**
     * Get the data model for this project
     * @return the dataModel
     */
    public QtlDataModel getDataModel()
    {
        return this.dataModel;
    }
    
    /**
     * Extract the metadata from our project data structure
     * @return
     *          the metadata
     */
    @Override
    public JQtlProjectMetadata getMetadata()
    {
        JQtlProjectMetadata jaxbProjectMetadata =
            this.objectFactory.createJQtlProjectMetadata();
        
        jaxbProjectMetadata.setProjectName(
                this.getName());
        
        jaxbProjectMetadata.getRHistoryItem().addAll(
                this.getRHistory());
        
        // iterate through all of the crosses, pulling out metadata as we go
        List<CrossType> jaxbCrosses = jaxbProjectMetadata.getCross();
        jaxbCrosses.clear();
        Cross[] crosses = this.dataModel.getCrosses();
        for(Cross cross: crosses)
        {
            CrossType currJaxbCross = this.objectFactory.createCrossType();
            currJaxbCross.setCrossIdentifier(cross.getAccessorExpressionString());
            
            // extract all of the QTL baskets from the current cross
            for(QtlBasket qtlBasket: cross.getQtlBaskets())
            {
                QtlBasketType currJaxbBasket =
                    this.objectFactory.createQtlBasketType();
                currJaxbBasket.setBasketName(qtlBasket.getName());
                
                List<QtlBasketItem> contents = qtlBasket.getContents();
                for(QtlBasketItem qtlBasketItem: contents)
                {
                    QtlBasketItemType jaxbQtlBasketItem =
                        this.objectFactory.createQtlBasketItemType();
                    
                    List<GeneticMarkerType> jaxbMarkers =
                        jaxbQtlBasketItem.getMarker();
                    if(qtlBasketItem instanceof SingleMarkerQtlBasketItem)
                    {
                        // convert the single-marker QTL type into a
                        // JAXB type
                        SingleMarkerQtlBasketItem singleMarkerQtlBasketItem =
                            (SingleMarkerQtlBasketItem)qtlBasketItem;
                        GeneticMarker marker =
                            singleMarkerQtlBasketItem.getMarker();
                        
                        GeneticMarkerType jaxbMarker =
                            this.objectFactory.createGeneticMarkerType();
                        jaxbMarker.setChromosomeName(marker.getChromosomeName());
                        jaxbMarker.setMarkerName(marker.getMarkerName());
                        jaxbMarker.setMarkerPositionCentimorgans(
                                marker.getMarkerPositionCentimorgans());
                        jaxbMarkers.add(jaxbMarker);
                    }
                    else
                    {
                        // convert the marker pair QTL type into a
                        // JAXB type
                        MarkerPairQtlBasketItem markerPairQtlBasketItem =
                            (MarkerPairQtlBasketItem)qtlBasketItem;
                        GeneticMarker[] markerPair = new GeneticMarker[2];
                        markerPair[0] = markerPairQtlBasketItem.getMarkerPair().getMarkerOne();
                        markerPair[1] = markerPairQtlBasketItem.getMarkerPair().getMarkerTwo();
                        
                        for(GeneticMarker marker: markerPair)
                        {
                            GeneticMarkerType jaxbMarker =
                                this.objectFactory.createGeneticMarkerType();
                            jaxbMarker.setChromosomeName(marker.getChromosomeName());
                            jaxbMarker.setMarkerName(marker.getMarkerName());
                            jaxbMarker.setMarkerPositionCentimorgans(
                                    marker.getMarkerPositionCentimorgans());
                            jaxbMarkers.add(jaxbMarker);
                        }
                    }
                    
                    // add the item's comment too
                    CommentType jaxbComment =
                        this.objectFactory.createCommentType();
                    jaxbComment.setContent(qtlBasketItem.getComment());
                    jaxbQtlBasketItem.setComment(jaxbComment);
                    
                    currJaxbBasket.getQtlBasketItem().add(jaxbQtlBasketItem);
                }
                
                currJaxbCross.getQtlBasket().add(currJaxbBasket);
            }
            
            jaxbCrosses.add(currJaxbCross);
        }
        
        return jaxbProjectMetadata;
    }
}
