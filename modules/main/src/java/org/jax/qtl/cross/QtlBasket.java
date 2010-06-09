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

package org.jax.qtl.cross;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This type is just a named collection of potential QTL markers. Its used
 * by the 
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class QtlBasket
{
    /**
     * The java beans property name to use for changes to the "contents"
     */
    public static final String CONTENTS_PROPERTY_NAME = "contents";
    
    private final PropertyChangeSupport propertyChangeSupport;
    
    /**
     * @see #getName()
     */
    private final String name;
    
    /**
     * @see #getContents()
     */
    private final List<QtlBasketItem> contents;

    /**
     * @see #getParentCross()
     */
    private final Cross parentCross;

    /**
     * Constructor
     * @param parentCross
     *          see {@link #getParentCross()}
     * @param name
     *          see {@link #getName()}
     */
    public QtlBasket(
            Cross parentCross,
            String name)
    {
        this.parentCross = parentCross;
        this.name = name;
        this.contents = Collections.synchronizedList(
                new ArrayList<QtlBasketItem>());
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * Adds the given listener to the list that we notify when a property
     * changes
     * @param listener
     *          the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes the given listener from the listener list that we notify of
     * property changes
     * @param listener
     *          the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Getter for the parent crosss
     * @return the parentCross
     */
    public Cross getParentCross()
    {
        return this.parentCross;
    }

    /**
     * Getter for this basket's name
     * @return
     *          the name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Getter for the contents of this basket
     * @return the contents
     */
    public List<QtlBasketItem> getContents()
    {
        return this.contents;
    }
    
    /**
     * Notify this qtl basket that the contents have changed (Eg: you have
     * done an add or remove on the list from {@link #getContents()}). Calling
     * this function allows this {@link QtlBasket} to notify any listeners
     * that have registered through
     * {@link #addPropertyChangeListener(PropertyChangeListener)} of the
     * change
     */
    public void notifyContentsChanged()
    {
        this.fireContentsChanged();
    }
    
    /**
     * Notify all of our listeners that the contents property has changed
     * 
     */
    private void fireContentsChanged()
    {
        this.propertyChangeSupport.firePropertyChange(
                CONTENTS_PROPERTY_NAME,
                null,
                null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return this.name;
    }
}
