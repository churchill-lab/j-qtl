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

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import org.jax.qtl.Constants;
import org.jax.qtl.configuration.QtlApplicationConfigurationManager;
import org.jax.qtl.cross.LoadCrossCommandBuilder;
import org.jax.qtl.cross.LoadCrossCommandBuilder.CrossFileFormat;
import org.jax.qtl.jaxbgenerated.JQtlApplicationState;
import org.jax.qtl.ui.VerticalTableHeaderRenderer;
import org.jax.qtl.util.CSVFileFilter;
import org.jax.qtl.util.FileUtils;
import org.jax.r.RCommand;
import org.jax.r.RSyntaxException;
import org.jax.r.RUtilities;
import org.jax.r.gui.RCommandEditorPanel;
import org.jax.r.jaxbgenerated.FileType;
import org.jax.r.jriutilities.JRIUtilityFunctions;
import org.jax.r.jriutilities.RInterfaceFactory;
import org.jax.util.ConfigurationUtilities;
import org.jax.util.TextWrapper;

/**
 * The load cross panel
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadCrossPanel extends RCommandEditorPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 5456531576562855210L;
    
    private static final Logger LOG = Logger.getLogger(
            LoadCrossPanel.class.getName());
    
    private static final String SAMPLE_DATA_DIR_NAME = "sample-data";
    
    private final LoadCrossCommandBuilder loadCrossCommandBuilder;
    
    private final DocumentListener anyDocumentChangeListener = new DocumentListener()
    {
        public void changedUpdate(DocumentEvent e)
        {
            LoadCrossPanel.this.updateLoadCrossCommand();
        }

        public void insertUpdate(DocumentEvent e)
        {
            LoadCrossPanel.this.updateLoadCrossCommand();
        }

        public void removeUpdate(DocumentEvent e)
        {
            LoadCrossPanel.this.updateLoadCrossCommand();
        }
    };
    
    /**
     * Constructor
     * @param loadCrossCommandBuilder
     *          the command builder to use
     */
    public LoadCrossPanel(LoadCrossCommandBuilder loadCrossCommandBuilder)
    {
        this.loadCrossCommandBuilder = loadCrossCommandBuilder;
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of the GUI initialization that isn't handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.crossNameTextField.setText(
                RUtilities.fromRIdentifierToReadableName(
                        this.loadCrossCommandBuilder.getCrossName()));
        this.crossNameTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        
        String[] genotypeCodes = this.loadCrossCommandBuilder.getGenotypes();
        int genotypeCodeIndex = 0;
        this.aaGenoCodeTextField.setText(genotypeCodes[genotypeCodeIndex]);
        genotypeCodeIndex++;
        this.aaGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        this.abGenoCodeTextField.setText(genotypeCodes[genotypeCodeIndex]);
        genotypeCodeIndex++;
        this.abGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        this.bbGenoCodeTextField.setText(genotypeCodes[genotypeCodeIndex]);
        genotypeCodeIndex++;
        this.bbGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        this.notBbGenoCodeTextField.setText(genotypeCodes[genotypeCodeIndex]);
        genotypeCodeIndex++;
        this.notBbGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        this.notAaGenoCodeTextField.setText(genotypeCodes[genotypeCodeIndex]);
        genotypeCodeIndex++;
        this.notAaGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        this.missingGenoCodeTextField.setText(
                this.loadCrossCommandBuilder.getNaStrings()[0]);
        this.missingGenoCodeTextField.getDocument().addDocumentListener(
                this.anyDocumentChangeListener);
        
        this.convertXDataCheckBox.setSelected(
                this.loadCrossCommandBuilder.getConvertXData());
        
        this.fileFormatComboBox.addItem(
                CrossFileFormat.COMMA_DELIMITED);
        this.fileFormatComboBox.addItem(
                CrossFileFormat.ROTATED_COMMA_DELIMITED);
        this.crossFileTextField.getDocument().addDocumentListener(
                new DocumentListener()
                {
                    public void changedUpdate(DocumentEvent e)
                    {
                        this.crossFileChanged();
                    }

                    public void insertUpdate(DocumentEvent e)
                    {
                        this.crossFileChanged();
                    }

                    public void removeUpdate(DocumentEvent e)
                    {
                        this.crossFileChanged();
                    }

                    private void crossFileChanged()
                    {
                        LoadCrossPanel.this.updateLoadCrossCommand();
                        
                        File dataFile = LoadCrossPanel.this.loadCrossCommandBuilder.getDataFile();
                        if(dataFile != null)
                        {
                            String fileName = dataFile.getName();

                            int lastDotIndex = fileName.lastIndexOf('.');
                            if(lastDotIndex != -1)
                            {
                                try
                                {
                                    LoadCrossPanel.this.loadCrossCommandBuilder.setCrossName(
                                            RUtilities.fromReadableNameToRIdentifier(
                                                    fileName.substring(0, lastDotIndex)));
                                }
                                catch(RSyntaxException ex)
                                {
                                    LOG.log(Level.FINE,
                                            "can't convert filename into an cross identifier",
                                            ex);
                                }
                            }
                        }
                        
                        LoadCrossPanel.this.refreshGui();
                    }
                });
        
        this.refreshGui();
    }
    
    private void refreshGui()
    {
        this.dataPreviewScrollPane.setViewportView(
                this.createDataPreviewTable());
        String crossName = this.loadCrossCommandBuilder.getCrossName();
        if(crossName != null)
        {
            this.crossNameTextField.setText(RUtilities.fromRIdentifierToReadableName(
                    crossName));
        }
    }

    private boolean validateFileData()
    {
        File dataFile = this.loadCrossCommandBuilder.getDataFile();
        String validationErrorMessage = null;
        if(dataFile == null || dataFile.getPath().trim().length() == 0)
        {
            validationErrorMessage = "Please select a file before proceeding.";
        }
        else if(!dataFile.isFile())
        {
            if(dataFile.isDirectory())
            {
                validationErrorMessage =
                    "The selected file \"" + dataFile.getPath() + "\" is a " +
                    "directory. Please select a normal file before proceeding.";
            }
            else
            {
                validationErrorMessage =
                    "The selected file \"" + dataFile.getPath() + "\" cannot " +
                    "be read. Please correct this error before proceeding.";
            }
        }
        
        if(validationErrorMessage != null)
        {
            JOptionPane.showMessageDialog(
                    this,
                    TextWrapper.wrapText(
                            validationErrorMessage,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                            "Validation Failed",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Validate all the data in the GUI and prompt the user if there's
     * something wrong
     * @return
     *          true iff the data is valid
     */
    public boolean validateData()
    {
        if(!this.validateFileData())
        {
            return false;
        }
        else
        {
            String readableCrossName = this.crossNameTextField.getText().trim();
            String validationErrorMessage =
                RUtilities.getErrorMessageForReadableName(
                        readableCrossName);

            if(validationErrorMessage == null)
            {
                if(readableCrossName.length() == 0)
                {
                    validationErrorMessage =
                        "The cross name cannot be empty. See help for " +
                        "more detailed information.";
                }
                else if(JRIUtilityFunctions.isTopLevelObject(
                        this.loadCrossCommandBuilder.getCrossName(),
                        RInterfaceFactory.getRInterfaceInstance()))
                {
                    validationErrorMessage =
                        "The name \"" + readableCrossName + "\" conflicts with " +
                        "an existing data object. Please choose another name.";
                }
                else if(this.aaGenoCodeTextField.getText().trim().length() == 0)
                {
                    validationErrorMessage =
                        "The AA genotype code cannot be empty. See help for " +
                        "more detailed information.";
                }
                else if(this.abGenoCodeTextField.getText().trim().length() == 0)
                {
                    validationErrorMessage =
                        "The AB genotype code cannot be empty. See help for " +
                        "more detailed information.";
                }
            }

            if(validationErrorMessage != null)
            {
                JOptionPane.showMessageDialog(
                        this,
                        TextWrapper.wrapText(
                                validationErrorMessage,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                                "Validation Failed",
                                JOptionPane.WARNING_MESSAGE);
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    
    /**
     * Update the load cross command using the current state of this panel
     */
    private void updateLoadCrossCommand()
    {
        CrossFileFormat selectedFormat =
            (CrossFileFormat)this.fileFormatComboBox.getSelectedItem();
        this.loadCrossCommandBuilder.setFormat(selectedFormat);
        
        String fileName = this.crossFileTextField.getText().trim();
        if(fileName.length() > 0)
        {
            this.loadCrossCommandBuilder.setDataFile(
                    new File(fileName));
        }
        else
        {
            this.loadCrossCommandBuilder.setDataFile(null);
        }
        
        try
        {
            this.loadCrossCommandBuilder.setCrossName(
                    RUtilities.fromReadableNameToRIdentifier(
                            this.crossNameTextField.getText()));
        }
        catch(RSyntaxException ex)
        {
            LOG.log(Level.FINE,
                    "cannot convert readable cross name to an R identifier",
                    ex);
        }
        
        List<String> genotypeCodesList = new ArrayList<String>();
        genotypeCodesList.add(this.aaGenoCodeTextField.getText().trim());
        genotypeCodesList.add(this.abGenoCodeTextField.getText().trim());
        genotypeCodesList.add(this.bbGenoCodeTextField.getText().trim());
        genotypeCodesList.add(this.notBbGenoCodeTextField.getText().trim());
        genotypeCodesList.add(this.notAaGenoCodeTextField.getText().trim());
        for(int i = genotypeCodesList.size() - 1; i >= 0; i--)
        {
            if(genotypeCodesList.get(i).length() == 0)
            {
                // remove any empty values from the tail
                genotypeCodesList.remove(i);
            }
            else
            {
                break;
            }
        }
        String[] genotypeCodes = genotypeCodesList.toArray(
                new String[genotypeCodesList.size()]);
        
        this.loadCrossCommandBuilder.setGenotypes(genotypeCodes);
        
        String naString = this.missingGenoCodeTextField.getText().trim();
        if(naString.length() > 0)
        {
            this.loadCrossCommandBuilder.setNaStrings(
                    new String[] {this.missingGenoCodeTextField.getText().trim()});
        }
        else
        {
            this.loadCrossCommandBuilder.setNaStrings(new String[0]);
        }
        
        this.loadCrossCommandBuilder.setConvertXData(
                this.convertXDataCheckBox.isSelected());
        
        this.fireCommandModified();
    }
    
    /**
     * {@inheritDoc}
     */
    public RCommand[] getCommands()
    {
        return new RCommand[] {this.loadCrossCommandBuilder.getCommand()};
    }
    
    private JComponent createDataPreviewTable()
    {
        int previewLinesCount = 13;
        
        File dataFile = this.loadCrossCommandBuilder.getDataFile();
        if(dataFile != null)
        {
            try
            {
                List<String[]> previewLines = new ArrayList<String[]>();
                BufferedReader reader = new BufferedReader(new FileReader(dataFile));
                String delim = ",";
                for(int i = 0; i < previewLinesCount; i++)
                {
                    String line = reader.readLine();
                    if(line == null)
                    {
                        if(i < previewLinesCount)
                            previewLinesCount = i;
                        break;
                    }
                    if(i == 0)
                    {
                        if(line.indexOf(";") != -1)
                            delim = ";";
                        else if(line.indexOf("\t") != -1)
                            delim = "\t";
                    }
                    String[] items = FileUtils.readInLine(line, delim);
                    previewLines.add(items);
                }
    
                int previewHeaderColumnsCount = previewLines.get(0).length;
                String[] header = new String[previewHeaderColumnsCount];
                String[][] preview = new String[previewLinesCount - 1][];
                for(int i = 0; i < previewLinesCount; i++)
                {
                    if(i == 0)
                    {
                        header = previewLines.get(i);
                    }
                    else
                    {
                        preview[i - 1] = previewLines.get(i);
                    }
                }
    
                JTable previewTable = new JTable(preview, header)
                {
                    /**
                     * for serialization
                     */
                    private static final long serialVersionUID = 0L;

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isCellEditable(int row, int column)
                    {
                        return false;
                    }
                };
                // set the header renderer to vertical alignment
                for(int i = 0; i < previewHeaderColumnsCount; i++)
                {
                    TableColumn tempColumn = previewTable.getColumnModel().getColumn(i);
                    tempColumn.setHeaderRenderer(new VerticalTableHeaderRenderer());
                }
                previewTable.setPreferredScrollableViewportSize(new Dimension(700,
                        80));
                previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                previewTable.revalidate();
    
                int totalColumns = previewTable.getColumnModel().getColumnCount();
    
                for(int i = 0; i < totalColumns; i++)
                {
                    TableColumn column = previewTable.getColumnModel().getColumn(i);
                    
                    if(this.loadCrossCommandBuilder.getFormat() == CrossFileFormat.ROTATED_COMMA_DELIMITED &&
                       i == 0)
                    {
                        // make the column with marker name wider
                        column.setPreferredWidth(Constants.CHECK_BOX_TABLE_CELL_WIDTH);
                    }
                    else
                    {
                        column.setPreferredWidth(Constants.CHECK_BOX_VERTICAL_TABLE_CELL_WIDTH);
                    }
                }
                
                return previewTable;
            }
            catch(Exception ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to create data preview table",
                        ex);
            }
        }
        
        JTextArea textArea = new JTextArea(
                "Please select a cross data file in order to " +
                "generate a preview");
        textArea.setEditable(false);
        return textArea;
    }
    
    private void browseCrossFiles()
    {
        CSVFileFilter csvFilter = new CSVFileFilter();
        JFileChooser chooser = new JFileChooser(
                this.getStartingDataDirectory());
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(csvFilter);
        if(JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this))
        {
            File selectedFile = chooser.getSelectedFile();
            this.crossFileTextField.setText(selectedFile.getPath());
            this.setStartingDataDirectory(selectedFile.getParentFile());
        }
    }
    
    /**
     * Get the starting directory for loading data.
     * @return
     *          the starting data
     */
    private File getStartingDataDirectory()
    {
        FileType dataDirs = this.getJaxbRecentCrossDataDirectory();
        File startingCrossDataDir = null;
        if(dataDirs.getFileName() == null || dataDirs.getFileName().length() == 0)
        {
            // since it's not set, use the samples dir
            ConfigurationUtilities configUtil;
            try
            {
                configUtil = new ConfigurationUtilities();
                File baseDir = configUtil.getBaseDirectory();
                startingCrossDataDir = new File(baseDir, SAMPLE_DATA_DIR_NAME);
                this.setStartingDataDirectory(startingCrossDataDir);
            }
            catch(Exception ex)
            {
                LOG.log(Level.SEVERE,
                        "failed to get default data dir",
                        ex);
            }
        }
        else
        {
            startingCrossDataDir = new File(dataDirs.getFileName());
        }
        
        return startingCrossDataDir;
    }
    
    /**
     * Set the starting data directory. This will persist in application
     * state
     * @see QtlApplicationConfigurationManager#getApplicationState()
     * @param startingDataDirectory
     *          the starting dir
     */
    private void setStartingDataDirectory(File startingDataDirectory)
    {
        String absolutePath = startingDataDirectory.getAbsolutePath();
        if(absolutePath != null)
        {
            this.getJaxbRecentCrossDataDirectory().setFileName(
                    absolutePath);
        }
    }
    
    /**
     * Convenience function for getting (and initializing if needed) the
     * JAXB data directory.
     * @see QtlApplicationConfigurationManager#getApplicationState()
     * @return
     *          the data directory
     */
    private FileType getJaxbRecentCrossDataDirectory()
    {
        QtlApplicationConfigurationManager configurationManager =
            QtlApplicationConfigurationManager.getInstance();
        JQtlApplicationState applicationState =
            configurationManager.getApplicationState();
        
        FileType crossDataDir = applicationState.getRecentCrossDataDirectory();
        if(crossDataDir == null)
        {
            org.jax.r.jaxbgenerated.ObjectFactory objectFactory =
                new org.jax.r.jaxbgenerated.ObjectFactory();
            crossDataDir = objectFactory.createFileType();
            applicationState.setRecentCrossDataDirectory(crossDataDir);
        }
        
        return crossDataDir;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        crossFileLabel = new javax.swing.JLabel();
        crossFileTextField = new javax.swing.JTextField();
        browseCrossFilesButton = new javax.swing.JButton();
        fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel crossDataPreviewLabel = new javax.swing.JLabel();
        dataPreviewScrollPane = new javax.swing.JScrollPane();
        javax.swing.JLabel crossNameLabel = new javax.swing.JLabel();
        crossNameTextField = new javax.swing.JTextField();
        javax.swing.JLabel genotypeCodesLabel = new javax.swing.JLabel();
        javax.swing.JLabel aaGenoCodeLabel = new javax.swing.JLabel();
        aaGenoCodeTextField = new javax.swing.JTextField();
        javax.swing.JLabel abGenoCodeLabel = new javax.swing.JLabel();
        abGenoCodeTextField = new javax.swing.JTextField();
        bbGenoCodeLabel = new javax.swing.JLabel();
        bbGenoCodeTextField = new javax.swing.JTextField();
        notBbGenoCodeLabel = new javax.swing.JLabel();
        notBbGenoCodeTextField = new javax.swing.JTextField();
        notAaGenoCodeLabel = new javax.swing.JLabel();
        notAaGenoCodeTextField = new javax.swing.JTextField();
        javax.swing.JLabel missingGenoCodeLabel = new javax.swing.JLabel();
        missingGenoCodeTextField = new javax.swing.JTextField();
        convertXDataCheckBox = new javax.swing.JCheckBox();

        crossFileLabel.setText("Cross Data File:");

        browseCrossFilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/action/browse-16x16.png"))); // NOI18N
        browseCrossFilesButton.setText("Browse Files ...");
        browseCrossFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCrossFilesButtonActionPerformed(evt);
            }
        });

        fileFormatLabel.setText("File Format:");

        fileFormatComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fileFormatComboBoxItemStateChanged(evt);
            }
        });

        crossDataPreviewLabel.setText("Cross Data Preview (First 10 Lines):");

        dataPreviewScrollPane.setMinimumSize(new java.awt.Dimension(23, 150));

        crossNameLabel.setText("Cross Name:");

        genotypeCodesLabel.setText("Genotype Codes (You must specify at least AA and AB. The cross type will be inferred from the data):");

        aaGenoCodeLabel.setText("AA:");

        abGenoCodeLabel.setText("AB:");

        bbGenoCodeLabel.setText("BB:");

        notBbGenoCodeLabel.setText("Not BB:");

        notAaGenoCodeLabel.setText("Not AA:");

        missingGenoCodeLabel.setText("Missing:");

        convertXDataCheckBox.setText("Convert X Chromosome Data to Internal Format (Recommended)");
        convertXDataCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                convertXDataCheckBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dataPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(crossFileLabel)
                            .add(fileFormatLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(63, 63, 63))
                            .add(layout.createSequentialGroup()
                                .add(crossFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(browseCrossFilesButton))
                    .add(layout.createSequentialGroup()
                        .add(crossNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(crossNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(aaGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(aaGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(abGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(abGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bbGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bbGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(notBbGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(notBbGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(notAaGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(notAaGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(missingGenoCodeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(missingGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(genotypeCodesLabel)
                    .add(crossDataPreviewLabel)
                    .add(convertXDataCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossFileLabel)
                    .add(browseCrossFilesButton)
                    .add(crossFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileFormatLabel)
                    .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(crossDataPreviewLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dataPreviewScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(crossNameLabel)
                    .add(crossNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(genotypeCodesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(aaGenoCodeLabel)
                    .add(aaGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(abGenoCodeLabel)
                    .add(abGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bbGenoCodeLabel)
                    .add(bbGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(notBbGenoCodeLabel)
                    .add(notBbGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(notAaGenoCodeLabel)
                    .add(notAaGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(missingGenoCodeLabel)
                    .add(missingGenoCodeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(convertXDataCheckBox)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void convertXDataCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_convertXDataCheckBoxItemStateChanged
        this.updateLoadCrossCommand();
    }//GEN-LAST:event_convertXDataCheckBoxItemStateChanged

    private void browseCrossFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseCrossFilesButtonActionPerformed
        this.browseCrossFiles();
    }//GEN-LAST:event_browseCrossFilesButtonActionPerformed

    private void fileFormatComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fileFormatComboBoxItemStateChanged
        this.updateLoadCrossCommand();
    }//GEN-LAST:event_fileFormatComboBoxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField aaGenoCodeTextField;
    private javax.swing.JTextField abGenoCodeTextField;
    private javax.swing.JLabel bbGenoCodeLabel;
    private javax.swing.JTextField bbGenoCodeTextField;
    private javax.swing.JButton browseCrossFilesButton;
    private javax.swing.JCheckBox convertXDataCheckBox;
    private javax.swing.JLabel crossFileLabel;
    private javax.swing.JTextField crossFileTextField;
    private javax.swing.JTextField crossNameTextField;
    private javax.swing.JScrollPane dataPreviewScrollPane;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JLabel fileFormatLabel;
    private javax.swing.JTextField missingGenoCodeTextField;
    private javax.swing.JLabel notAaGenoCodeLabel;
    private javax.swing.JTextField notAaGenoCodeTextField;
    private javax.swing.JLabel notBbGenoCodeLabel;
    private javax.swing.JTextField notBbGenoCodeTextField;
    // End of variables declaration//GEN-END:variables
    
}
