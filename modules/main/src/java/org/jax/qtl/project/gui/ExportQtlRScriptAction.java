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

package org.jax.qtl.project.gui;

import javax.swing.JFrame;

import org.jax.qtl.QTL;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.project.QtlProjectManager;
import org.jax.r.configuration.RApplicationConfigurationManager;
import org.jax.r.gui.ExportRScriptAction;
import org.jax.util.project.ProjectManager;

/**
 * An implementation of {@link ExportRScriptAction} for the QTL application
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportQtlRScriptAction extends ExportRScriptAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6367919293065938300L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected RApplicationConfigurationManager getApplicationConfigurationManager()
    {
        return QtlApplicationConfigurationManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JFrame getParentFrame()
    {
        return QTL.getInstance().getApplicationFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProjectManager getProjectManager()
    {
        return QtlProjectManager.getInstance();
    }
}
