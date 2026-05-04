/*
 * #%L
 * Compi GUI
 * %%
 * Copyright (C) 2023 - 2025 Florindo González Doval and Hugo López-Fernández
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.gui.gui;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.gui.gui.util.JFileChooserPanelWrapper;
import org.sing_group.compi.gui.gui.util.UISettings;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.models.CompiPipeline;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanelBuilder;
import org.sing_group.gc4s.input.filechooser.SelectionMode;
import org.sing_group.gc4s.input.filechooser.event.FileChooserListener;
import org.sing_group.gc4s.ui.CenteredJPanel;


public class LocalSelectionPanel extends JPanel {

    private List<PipelineListener> listenersList;
    private JFileChooserPanel pipelineDirPanel;
    private JFileChooserPanelWrapper pipelineDirWrapper;
    private JTextField pipelineFileText;
    private JTextField compiPathText;
    private JTextArea compiVersionArea;
    private String lastLoadedPipelinePath;

    public LocalSelectionPanel() {
        this.listenersList = new ArrayList<>();
    }

    public void createLocalSelectionInterface() {

        pipelineDirPanel = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
            .withLabel("")
            .withFileChooserSelectionMode(SelectionMode.DIRECTORIES)
            .build();

        pipelineDirWrapper = new JFileChooserPanelWrapper(pipelineDirPanel);
        pipelineDirWrapper.setBackground(UISettings.INVALID_BACKGROUND);

        pipelineDirPanel.addFileChooserListener(new FileChooserListener() {
            @Override
            public void onFileChoosed(ChangeEvent event) {
                if (UISettings.isValid(pipelineDirPanel.getSelectedFile().getAbsolutePath()))
                    pipelineDirWrapper.setBackground(UISettings.VALID_BACKGROUND);
                else
                    pipelineDirWrapper.setBackground(UISettings.INVALID_BACKGROUND);

                loadPipelineFromSelection();
            }
        });

        compiPathText = new JTextField("compi", 30);
        compiPathText.setName("localCompiPathTextField");

        compiVersionArea = new JTextArea(3, 30);
        compiVersionArea.setEditable(false);
        compiVersionArea.setLineWrap(true);
        compiVersionArea.setWrapStyleWord(true);
        compiVersionArea.setBackground(UISettings.INVALID_BACKGROUND);

        FocusAdapter compiPathFocusAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkCompiVersion();
                loadPipelineFromSelection();
            }
        };
        compiPathText.addFocusListener(compiPathFocusAdapter);

        InputParameter[] parameters = new InputParameter[4];

        parameters[0] = new InputParameter(
            "Pipeline directory: ",
            pipelineDirPanel,
            "Select the directory containing the pipeline file"
        );

        parameters[1] = new InputParameter(
            "Pipeline file: ",
            createFileTextField(),
            "Name of the pipeline XML file"
        );

        parameters[2] = new InputParameter(
            "Compi path: ",
            compiPathText,
            "Path to the Compi executable (e.g. 'compi' or '/usr/local/bin/compi')"
        );

        parameters[3] = new InputParameter(
            "Compi version: ",
            compiVersionArea,
            "Output of running compi --version"
        );

        InputParametersPanel inputPanel = new InputParametersPanel(parameters);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new CenteredJPanel(inputPanel), BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    private JTextField createFileTextField() {
        pipelineFileText = new JTextField("pipeline.xml", 30);
        pipelineFileText.setName("pipelineFileTextField");
        pipelineFileText.setBackground(UISettings.INVALID_BACKGROUND);

        pipelineFileText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lastLoadedPipelinePath = null; // force reload when filename changes
                loadPipelineFromSelection();
            }
        });

        return pipelineFileText;
    }

    /**
     * Runs "compi --version" (or whatever path is set) in a background thread
     * and displays the output in the version text area.
     */
    private void checkCompiVersion() {
        String compiPath = compiPathText.getText().trim();
        if (compiPath.isEmpty()) {
            return;
        }

        new Thread(() -> {
            String version = runCompiVersion(compiPath);
            final boolean ok = version != null && !version.trim().isEmpty();
            final String displayText = ok ? version.trim() : "Compi not found at path: " + compiPath;

            SwingUtilities.invokeLater(() -> {
                compiVersionArea.setText(displayText);
                compiVersionArea.setBackground(ok ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
            });
        }).start();
    }

    private String runCompiVersion(String compiPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(compiPath, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            return exitCode == 0 ? output.toString() : null;
        } catch (IOException | InterruptedException e) {
            System.err.println("[Local] Error checking Compi version: " + e.getMessage());
            return null;
        }
    }

    public void addListener(PipelineListener pp) {
        this.listenersList.add(pp);
    }

    public void onUpdatePipeline(PipelineInfoProvider pp, PipelineRunConfiguration prc) {
        for (PipelineListener listener : listenersList) {
            listener.onUpdatePipeline(pp, prc);
        }
    }

    private void onPipelineLoadError(String message) {
        for (PipelineListener listener : listenersList) {
            listener.onPipelineLoadError(message);
        }
    }

    public void setPipelineInfo(File pipelineDir, String pipelineFile) {
        pipelineDirPanel.setSelectedFile(pipelineDir);
        pipelineFileText.setText(pipelineFile);
        if (pipelineDir != null && pipelineDir.exists()) {
            pipelineDirWrapper.setBackground(UISettings.VALID_BACKGROUND);
            String fileName = (pipelineFile != null && !pipelineFile.isEmpty()) ? pipelineFile : "pipeline.xml";
            lastLoadedPipelinePath = new File(pipelineDir.getAbsolutePath(), fileName).getAbsolutePath();
        } else {
            pipelineDirWrapper.setBackground(UISettings.INVALID_BACKGROUND);
        }
    }

    public void setPipelineInfo(File pipelineDir, String pipelineFile, String compiPath) {
        setPipelineInfo(pipelineDir, pipelineFile);
        if (compiPath != null && !compiPath.isEmpty()) {
            compiPathText.setText(compiPath);
            checkCompiVersion();
        }
    }

    public String getCompiPath() {
        return compiPathText != null ? compiPathText.getText().trim() : "compi";
    }

    private void loadPipelineFromSelection() {
        if (pipelineDirPanel.getSelectedFile() == null) {
            return;
        }
        if (UISettings.isValid(pipelineDirPanel.getSelectedFile().getAbsolutePath()))
            pipelineDirWrapper.setBackground(UISettings.VALID_BACKGROUND);
        else
            pipelineDirWrapper.setBackground(UISettings.INVALID_BACKGROUND);

        String fileName = pipelineFileText.getText();
        if (fileName == null || fileName.isEmpty()) {
            fileName = "pipeline.xml";
        }

        File newPipelineFile = new File(pipelineDirPanel.getSelectedFile().getAbsolutePath(), fileName);
        if (!newPipelineFile.exists()) {
            pipelineFileText.setBackground(UISettings.INVALID_BACKGROUND);
            onPipelineLoadError("Pipeline not loaded: file not found (" + fileName + ")");
            return;
        }

        String newPipelinePath = newPipelineFile.getAbsolutePath();
        if (lastLoadedPipelinePath != null && lastLoadedPipelinePath.equals(newPipelinePath)) {
            return;
        }

        lastLoadedPipelinePath = newPipelinePath;

        String compiPath = compiPathText != null ? compiPathText.getText().trim() : "compi";
        if (compiPath.isEmpty()) {
            compiPath = "compi";
        }

        try {
            Pipeline p = PipelineParserFactory.createPipelineParser().parsePipeline(newPipelineFile);
            pipelineFileText.setBackground(UISettings.VALID_BACKGROUND);
            onUpdatePipeline(
                new CompiPipeline(p, pipelineDirPanel.getSelectedFile(), fileName, compiPath),
                new PipelineRunConfiguration(p, fileName)
            );
        } catch (IllegalArgumentException | IOException e1) {
            pipelineFileText.setBackground(UISettings.INVALID_BACKGROUND);
            onPipelineLoadError("Pipeline not loaded: " + e1.getMessage());
            e1.printStackTrace();
        }
    }
}
