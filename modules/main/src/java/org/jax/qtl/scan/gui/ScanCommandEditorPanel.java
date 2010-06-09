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

package org.jax.qtl.scan.gui;

import org.jax.qtl.scan.ScanCommandBuilder;
import org.jax.r.RCommand;
import org.jax.r.gui.RCommandEditorPanel;

/**
 * An abstract class that makes creating a scan editor panel a little easier
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class ScanCommandEditorPanel extends RCommandEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 8073053285911140910L;

    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        RCommand permutationsCommand =
            this.getScanCommand().getCommandWithPermutations();
        if(permutationsCommand == null)
        {
            return new RCommand[] {
                    this.getScanCommand().getCommandWithoutPermutations()};
        }
        else
        {
            return new RCommand[] {
                    this.getScanCommand().getCommandWithoutPermutations(),
                    permutationsCommand};
        }
    }
    
    /**
     * Getter for the scan command
     * @return
     *          the scan command
     */
    protected abstract ScanCommandBuilder getScanCommand();
}
