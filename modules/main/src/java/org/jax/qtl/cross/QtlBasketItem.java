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

/**
 * An abstract type for a qtl basket item
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class QtlBasketItem
{
    private String comment;

    /**
     * Constructor
     * @param comment
     */
    public QtlBasketItem(String comment)
    {
        this.comment = comment;
    }
    
    /**
     * Getter for the comment
     * @return the comment
     */
    public String getComment()
    {
        return this.comment;
    }
    
    /**
     * Setter for the comment
     * @param comment the comment to set
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
