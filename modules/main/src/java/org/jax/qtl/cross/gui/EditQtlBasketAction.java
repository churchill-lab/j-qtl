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

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.jax.qtl.QTL;
import org.jax.qtl.cross.QtlBasket;
import org.jax.qtl.project.gui.QtlProjectTree;
import org.jax.util.gui.desktoporganization.Desktop;

/**
 * Action for showing the view/edit panel for qtl baskets
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class EditQtlBasketAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5081330711924430685L;

    /**
     * the basket we're going to view or edit
     */
    private final QtlBasket qtlBasket;

    /**
     * Constructor
     * @param qtlBasket
     *          the qtl basket to view/edit
     */
    public EditQtlBasketAction(QtlBasket qtlBasket)
    {
        super("View/Edit QTL Basket ...");
        this.qtlBasket = qtlBasket;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        QtlBasket qtlBasket = this.qtlBasket;
        if(qtlBasket == null)
        {
            QtlProjectTree projectTree = QTL.getInstance().getProjectTree();
            qtlBasket = projectTree.getSelectedQtlBasket();
        }
        
        if(qtlBasket != null)
        {
            final QtlBasketPanel qtlBasketPanel = new QtlBasketPanel(qtlBasket);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    String title = "View/Edit QTL Basket";
                    Desktop desktop = QTL.getInstance().getDesktop();
                    desktop.createInternalFrame(
                            qtlBasketPanel,
                            title,
                            null,
                            title);
                }
            });
        }
    }
}
