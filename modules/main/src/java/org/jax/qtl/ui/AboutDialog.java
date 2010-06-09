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
package org.jax.qtl.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.HelpBroker;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jax.qtl.QTL;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * @author Lei Wu
 * @version 1.0
 */
public class AboutDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6039415007995896653L;

    private static final String LICENSE_URL = "http://www.gnu.org/licenses/";
    
    private static final String LICENSE_ID_STRING = "Copyright_and_License";
    
    private static final Logger LOG = Logger.getLogger(
            AboutDialog.class.getName());
    
    /**
     * Constructor
     */
    public AboutDialog()
    {
        super(QTL.getInstance().getApplicationFrame(), "About J/qtl", false);

        String msg =
            "<html><body><DIV align=\"center\">J/qtl 1.3.1 - GUI for R/qtl<br>Works with R-2.10 and R/qtl 1.12 and above" +
            "<p>Copyright (c) 2009 The Jackson Laboratory</p>" +
            "<br>" +
            "This program is free software: you can redistribute it and/or modify<br>" +
            "it under the terms of the GNU General Public License as published by<br>" +
            "the Free Software Foundation, either version 3 of the License, or<br>" +
            "(at your option) any later version.<br>" +
            "<br>" +
            "This program is distributed in the hope that it will be useful,<br>" +
            "but WITHOUT ANY WARRANTY; without even the implied warranty of<br>" +
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>" +
            "<a href=\"" + LICENSE_URL + "\">" +
            "GNU General Public License</a> for more details.<br>" +
            "<br>" +
            "You can download a copy of the source code by visiting<br>" +
            "the software downloads page on the " +
            "<a href=\"http://research.jax.org/faculty/churchill/index.html\">" +
            "Churchill Lab's website</a><br>" +
            "<p>Authors: Hao Wu, Lei Wu and Keith Sheppard (current maintainer)</p>" +
            "<p><a href=\"http://research.jax.org/faculty/churchill/index.html\">Gary Churchill's Group</a>" +
            "<br>Email: <a href=\"mailto:jqtl@jax.org\"><i>jqtl@jax.org</i></a>" +
            "<br><a href=\"http://www.jax.org/\">The Jackson Laboratory</a><p></DIV></body></html>";
        JEditorPane labInfo = new JEditorPane();
        labInfo.setBackground(Color.white);
        labInfo.setContentType("text/html");
        Font f = new Font("sansserif", Font.BOLD, 20);
        labInfo.setFont(f);

        labInfo.setText(msg);
        labInfo.setEditable(false);

        labInfo.addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent evt)
            {
                // if a link was clicked
                if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    try
                    {
                        if(evt.getURL().toString().equals(LICENSE_URL))
                        {
                            AboutDialog.this.showLicense();
                        }
                        else
                        {
                            BrowserLauncher browserLauncher = new BrowserLauncher();
                            browserLauncher.openURLinBrowser(
                                    evt.getURL().toString());
                        }
                    }
                    catch(Exception ex)
                    {
                        LOG.log(Level.SEVERE,
                                "Failed to launch hyperlink: " + evt,
                                ex);
                    }
                }
            }
        });

        // ok button
        JButton ok = new JButton("Close");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        JPanel okPane = new JPanel();
        okPane.add(ok);

        // set the background of infoPane the same as the OK button (system)
        labInfo.setBackground(ok.getBackground());
        labInfo.setFont(ok.getFont());

        JPanel infoPane = new JPanel(new BorderLayout());

        infoPane.add(labInfo, BorderLayout.CENTER);
        infoPane.add(okPane, BorderLayout.SOUTH);
        infoPane.setBackground(Color.white);
        Container content = getContentPane();
        content.add(infoPane);
        setSize(new Dimension(300, 200));

        // ppsition the about window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getPreferredSize();
        setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);

        pack();
    }
    
    /**
     * Display the license
     */
    private void showLicense()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                HelpBroker hb = QTL.getInstance().getMenubar().getHelpBroker();
                hb.setDisplayed(true);
                hb.setCurrentID(LICENSE_ID_STRING);
            }
        });
    }
}
