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

import org.jax.qtl.cross.Cross;

/**
 * Interface for listening to changes in the data model.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public interface QtlDataModelListener
{
    /**
     * Signals that a cross has been added
     * @param source
     *          the data model that fired this event
     * @param cross
     *          the cross
     */
    public void crossAdded(QtlDataModel source, Cross cross);
    
    /**
     * Signals that a cross has been added
     * @param source
     *          the data model that fired this event
     * @param cross
     *          the cross
     */
    public void crossRemoved(QtlDataModel source, Cross cross);
}
