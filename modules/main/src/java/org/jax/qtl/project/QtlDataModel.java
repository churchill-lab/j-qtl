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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.qtl.cross.Cross;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterface;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.jriutilities.RObject;

/**
 * This class is used to represent all of the R data that our QTL
 * functionality cares about.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlDataModel
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(QtlDataModel.class.getName());
    
    /**
     * the R interface
     */
    private final RInterface rInterface;
    
    /**
     * a mapping from identifiers to cross maps
     */
    private final Map<String, Cross> identifierToCrossMap =
        Collections.synchronizedMap(new HashMap<String, Cross>());
    
    /**
     * a list of our listeners
     */
    private final ConcurrentLinkedQueue<QtlDataModelListener> listenerList =
        new ConcurrentLinkedQueue<QtlDataModelListener>();
    
    /**
     * Constructor
     * @param rInterface
     *          the R interface that this data model uses
     */
    public QtlDataModel(RInterface rInterface)
    {
        this.rInterface = rInterface;
        this.updateAll();
    }
    
    /**
     * Add a listener
     * @param listener
     *          the listener
     */
    public void addQtlDataModelListener(QtlDataModelListener listener)
    {
        this.listenerList.add(listener);
    }
    
    /**
     * Remove a listener
     * @param listener
     *          the listener
     */
    public void removeQtlDataModelListener(QtlDataModelListener listener)
    {
        this.listenerList.remove(listener);
    }
    
    /**
     * refresh the data model
     */
    public void updateAll()
    {
        List<RObject> crosses = QtlDataModel.getAllCrossRObjects();
        
        // add new crosses
        List<Cross> addedCrosses = new ArrayList<Cross>();
        for(RObject currCrossObj: crosses)
        {
            Cross matchingCross = this.identifierToCrossMap.get(
                    currCrossObj.getAccessorExpressionString());
            if(matchingCross == null)
            {
                matchingCross = new Cross(
                        this.rInterface,
                        currCrossObj.getAccessorExpressionString());
                this.identifierToCrossMap.put(
                        currCrossObj.getAccessorExpressionString(),
                        matchingCross);
                addedCrosses.add(matchingCross);
            }
        }
        
        // remove any missing crosses
        List<Cross> removedCrosses = new ArrayList<Cross>();
        synchronized(this.identifierToCrossMap)
        {
            Iterator<Cross> crossEntryIter =
                this.identifierToCrossMap.values().iterator();
            while(crossEntryIter.hasNext())
            {
                Cross currCross = crossEntryIter.next();
                boolean foundMatch = false;
                for(RObject currCrossRObject: crosses)
                {
                    if(currCross.getAccessorExpressionString().equals(
                       currCrossRObject.getAccessorExpressionString()))
                    {
                        foundMatch = true;
                    }
                }
                
                if(!foundMatch)
                {
                    removedCrosses.add(currCross);
                    crossEntryIter.remove();
                }
            }
        }
        
        // handle notification
        for(Cross currAddedCross: addedCrosses)
        {
            this.fireCrossAdded(currAddedCross);
        }
        
        for(Cross currRemovedCross: removedCrosses)
        {
            this.fireCrossRemoved(currRemovedCross);
        }
    }
    
    /**
     * Notify our listeners that a {@link Cross} has been added.
     * @param addedCross
     *          the {@link Cross} that was added
     */
    private void fireCrossAdded(Cross addedCross)
    {
        Iterator<QtlDataModelListener> listenerIter =
            this.listenerList.iterator();
        while(listenerIter.hasNext())
        {
            listenerIter.next().crossAdded(this, addedCross);
        }
    }

    /**
     * Notify our listeners that a {@link Cross} has been removed
     * @param removedCross
     *          the {@link Cross} that was removed
     */
    private void fireCrossRemoved(Cross removedCross)
    {
        Iterator<QtlDataModelListener> listenerIter =
            this.listenerList.iterator();
        while(listenerIter.hasNext())
        {
            listenerIter.next().crossRemoved(this, removedCross);
        }
    }

    /**
     * Get a cross map where the keys are the cross names and values are
     * the crosses
     * @return
     *          the mapping
     */
    public Map<String, Cross> getCrossMap()
    {
        return this.identifierToCrossMap;
    }
    
    /**
     * A convenience function that uses {@link #getCrossMap()} to construct
     * an array of crosses
     * @return
     *          the array
     */
    public Cross[] getCrosses()
    {
        synchronized(this.identifierToCrossMap)
        {
            Cross[] crosses = new Cross[this.identifierToCrossMap.size()];
            return this.identifierToCrossMap.values().toArray(crosses);
        }
    }
    
    /**
     * Scan the current R environment and return all cross object
     * identifiers.
     * @return
     *          the list of cross identifiers
     */
    private static List<RObject> getAllCrossRObjects()
    {
        RInterface rInterface = RInterfaceFactory.getRInterfaceInstance();
        List<RObject> crossIdentifiers = JRIUtilityFunctions.getTopLevelObjectsOfType(
                rInterface,
                Cross.TYPE_STRING);
        
        if(LOG.isLoggable(Level.FINEST))
        {
            StringBuffer message = new StringBuffer("detected crosses:");
            for(RObject currCrossId: crossIdentifiers)
            {
                message.append(" " + currCrossId.getAccessorExpressionString());
            }
            
            LOG.finest(message.toString());
        }
        
        return crossIdentifiers;
    }
    
}
