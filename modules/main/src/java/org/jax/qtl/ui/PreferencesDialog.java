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

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.SecondaryWindow;
import javax.swing.JDialog;

import org.jax.qtl.QTL;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.r.jaxbgenerated.RApplicationConfiguration;
import org.jax.r.jaxbgenerated.RInstallationType;
import org.jax.r.jaxbgenerated.RLaunchConfigurationType;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.r.rintegration.PlatformSpecificRFunctions;
import org.jax.r.rintegration.PlatformSpecificRFunctionsFactory;
import org.jax.r.rintegration.RInstallation;
import org.jax.r.rintegration.RLaunchConfiguration;
import org.jax.r.rintegration.gui.MemoryConfigurationPanel;
import org.jax.r.rintegration.gui.RHomeSelectorPanel;
import org.jax.util.TypeSafeSystemProperties;
import org.jax.util.TypeSafeSystemProperties.OsFamily;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * Dialog used for setting J/qtl preferences.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PreferencesDialog extends JDialog
{
    /**
     * Every {@link java.io.Serializable} is supposed to have one of these.
     */
    private static final long serialVersionUID = 998933803900286794L;

    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PreferencesDialog.class.getName());

    private static final String HELP_ID_STRING = "Edit_Preferences";
    
    /**
     * the platform specific stuff
     */
    private final PlatformSpecificRFunctions platformSpecificRFunctions;
    
    /**
     * the R_HOME selector
     */
    private final RHomeSelectorPanel rInstallationPanel;
    
    /**
     * The panel for setting memory allocation limits
     */
    private final MemoryConfigurationPanel memoryConfigurationPanel;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     */
    public PreferencesDialog(Frame parent)
    {
        super(parent, true);
        
        // initialize the r installation panel
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        RApplicationConfiguration applicationConfiguration =
            configurationManager.getApplicationConfiguration();
        PlatformSpecificRFunctionsFactory factory =
            PlatformSpecificRFunctionsFactory.getInstance();
        this.platformSpecificRFunctions =
            factory.getPlatformSpecificRFunctions();
        RInstallation rInstallation = null;
        
        try
        {
            RInstallationType selectedInstallation =
                applicationConfiguration.getRConfiguration().getRLaunchConfiguration().getSelectedRInstallation();
            rInstallation = new RInstallation(
                    new File(selectedInstallation.getRHomeDirectory()),
                    new File(selectedInstallation.getLibraryDirectory()),
                    selectedInstallation.getVersion());
        }
        catch(NullPointerException ex)
        {
            // don't care
        }
        
        this.rInstallationPanel = new RHomeSelectorPanel(
                rInstallation,
                this.platformSpecificRFunctions,
                false);
        
        // initialize the memory limits panel
        Long javaMemLimitMB = applicationConfiguration.getJavaMemoryLimitMegabytes();
        Long rMemLimitMB = applicationConfiguration.getRMemoryLimitMegabytes();
        if(javaMemLimitMB != null && rMemLimitMB != null)
        {
            this.memoryConfigurationPanel = new MemoryConfigurationPanel(
                    rMemLimitMB.intValue(),
                    javaMemLimitMB.intValue());
        }
        else
        {
            this.memoryConfigurationPanel = new MemoryConfigurationPanel();
        }
        
        // run the GUI builder initialization code
        this.initComponents();
        
        HelpSet hs = QTL.getInstance().getMenubar().getHelpSet();
        CSH.setHelpIDString(
                this.helpButton,
                HELP_ID_STRING);
        this.helpButton.addActionListener(
                new CSH.DisplayHelpFromSource(
                        hs,
                        SecondaryWindow.class.getName(),
                        null));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okCancelPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        tabPanel = new javax.swing.JTabbedPane();
        rInstallationPanelDownCast = this.rInstallationPanel;
        memoryConfigurationPanelDownCast = this.memoryConfigurationPanel;

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        okCancelPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        okCancelPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/help-16x16.png"))); // NOI18N
        helpButton.setText("Help ...");
        okCancelPanel.add(helpButton);

        tabPanel.addTab("R Home", rInstallationPanelDownCast);
        tabPanel.addTab("Memory Limits", memoryConfigurationPanelDownCast);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(okCancelPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
            .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(tabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(okCancelPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if(this.rInstallationPanel.apply())
        {
            RLaunchConfiguration selectedLaunchConfig =
                this.rInstallationPanel.getSelectedLaunchConfiguration();
            this.updateApplicationConfiguration(
                    selectedLaunchConfig);
            this.dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Update the launch configuration
     * @param updatedRLaunchConfig
     *          the new launch configuration
     */
    private void updateApplicationConfiguration(
            RLaunchConfiguration updatedRLaunchConfig)
    {
        boolean restartRequired = false;
        
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        RApplicationConfiguration configuration =
            configurationManager.getApplicationConfiguration();
        
        // see if the launch configuration has changed at all
        RLaunchConfigurationType oldJaxbRLaunchConfigurationType =
            configuration.getRConfiguration().getRLaunchConfiguration();
        RLaunchConfiguration oldRLaunchConfiguration =
            QtlApplicationConfigurationManager.fromJaxbToNativeRLaunchConfiguration(
                    oldJaxbRLaunchConfigurationType);
        if(!updatedRLaunchConfig.equals(oldRLaunchConfiguration))
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "updating configured R launch configuration to: " +
                        updatedRLaunchConfig);
            }
            
            // convert to the jaxb type then replace the old configuration
            RLaunchConfigurationType updatedJaxbRLaunchConfig =
                QtlApplicationConfigurationManager.fromNativeToJaxbRLaunchConfiguration(
                        updatedRLaunchConfig);
            configuration.getRConfiguration().setRLaunchConfiguration(
                    updatedJaxbRLaunchConfig);
            
            restartRequired = true;
        }
        else
        {
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "ignoring the selected R launch config update " +
                        "because it is the same as the previous value");
            }
        }
        
        Long oldJavaMemLimitMB =
            configuration.getJavaMemoryLimitMegabytes();
        int newJavaMemLimitMB =
            this.memoryConfigurationPanel.getJavaMemoryLimitMegabytes();
        
        if(oldJavaMemLimitMB == null || oldJavaMemLimitMB.intValue() != newJavaMemLimitMB)
        {
            restartRequired = true;
            configuration.setJavaMemoryLimitMegabytes(Long.valueOf(
                    newJavaMemLimitMB));
        }
        
        Long oldRMemLimitMB =
            configuration.getRMemoryLimitMegabytes();
        int newRMemLimitMB =
            this.memoryConfigurationPanel.getRMemoryLimitMegabytes();
        if(oldRMemLimitMB == null || oldRMemLimitMB.intValue() != newRMemLimitMB)
        {
            configuration.setRMemoryLimitMegabytes(Long.valueOf(
                    newRMemLimitMB));
            
            if(TypeSafeSystemProperties.getOsFamily() == OsFamily.WINDOWS_OS_FAMILY)
            {
                RInterfaceFactory.getRInterfaceInstance().insertComment(
                        "Resizing R memory ceiling (only valid on Windows)");
                RInterfaceFactory.getRInterfaceInstance().evaluateCommandNoReturn(
                        "memory.limit(" + newRMemLimitMB + ")");
            }
        }
        
        if(restartRequired)
        {
            // tell the user that changes won't take effect until
            // a restart
            String message =
                "For all of the configuration changes that you have made " +
                "to take effect you must restart the application. Please " +
                "save your project work and restart.";
            MessageDialogUtilities.inform(
                    this,
                    message,
                    "Restart Required");
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel memoryConfigurationPanelDownCast;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel okCancelPanel;
    private javax.swing.JPanel rInstallationPanelDownCast;
    private javax.swing.JTabbedPane tabPanel;
    // End of variables declaration//GEN-END:variables
    
}
