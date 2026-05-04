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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.gui.docker.CompiDockerImage;
import org.sing_group.compi.gui.gui.util.UISettings;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.models.DockerPipeline;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.ui.CenteredJPanel;

public class DockerSelectionPanel extends JPanel {

    private List<PipelineListener> listenersList;

    private JTextField imageNameText;
    private JTextField compiPathText;
    private JTextArea compiVersionArea;
    private JTextField pipelineFileText;

    public DockerSelectionPanel() {
        this.listenersList = new ArrayList<>();
    }

    public void createDockerSelectionInterface() {
        imageNameText = new JTextField(30);
        imageNameText.setName("dockerImageTextField");
        imageNameText.setBackground(UISettings.INVALID_BACKGROUND);

        compiPathText = new JTextField("/compi", 30);
        compiPathText.setName("dockerCompiPathTextField");

        compiVersionArea = new JTextArea(3, 30);
        compiVersionArea.setEditable(false);
        compiVersionArea.setLineWrap(true);
        compiVersionArea.setWrapStyleWord(true);
        compiVersionArea.setBackground(UISettings.INVALID_BACKGROUND);

        pipelineFileText = new JTextField(30);
        pipelineFileText.setName("dockerPipelineFileTextField");

        // Trigger checks when either image name or compi path loses focus
        FocusAdapter checkFocusAdapter = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                triggerDockerChecks();
            }
        };
        imageNameText.addFocusListener(checkFocusAdapter);
        compiPathText.addFocusListener(checkFocusAdapter);
        pipelineFileText.addFocusListener(checkFocusAdapter);

        InputParameter[] parameters = new InputParameter[4];

        parameters[0] = new InputParameter(
            "Docker image: ",
            imageNameText,
            "Name of the Docker image containing the Compi pipeline"
        );

        parameters[1] = new InputParameter(
            "Compi path: ",
            compiPathText,
            "Path to the Compi executable inside the Docker image"
        );

        parameters[2] = new InputParameter(
            "Compi version: ",
            compiVersionArea,
            "Output of running compi --version inside the Docker image"
        );

        parameters[3] = new InputParameter(
            "Compi pipeline: ",
            pipelineFileText,
            "Pipeline XML file inside the Docker image (auto-detected or set manually)"
        );

        InputParametersPanel inputPanel = new InputParametersPanel(parameters);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new CenteredJPanel(inputPanel), BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.add(mainPanel);
    }

    private void triggerDockerChecks() {
        String imageName = imageNameText.getText().trim();
        String compiPath = compiPathText.getText().trim();

        if (imageName.isEmpty() || compiPath.isEmpty()) {
            return;
        }

        // Run Docker checks in a background thread to avoid blocking the EDT
        new Thread(() -> {
            CompiDockerImage docker = new CompiDockerImage(imageName);

            // Step 1: Check Compi version
            String version = docker.getCompiVersion(compiPath);
            final String versionText;
            final boolean compiOk;
            if (version != null && !version.trim().isEmpty()) {
                versionText = version.trim();
                compiOk = true;
            } else {
                versionText = "Compi not found at path: " + compiPath;
                compiOk = false;
            }

            SwingUtilities.invokeLater(() -> {
                compiVersionArea.setText(versionText);
                compiVersionArea.setBackground(compiOk ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                if (compiOk) {
                    imageNameText.setBackground(UISettings.VALID_BACKGROUND);
                } else {
                    imageNameText.setBackground(UISettings.INVALID_BACKGROUND);
                }
            });

            if (!compiOk) {
                return;
            }

            // Step 2: Find pipeline file (only auto-detect if the field is currently empty)
            String currentPipelineFile = pipelineFileText.getText().trim();
            if (currentPipelineFile.isEmpty()) {
                String detectedPipeline = docker.findPipelineFileName();
                if (detectedPipeline != null && !detectedPipeline.isEmpty()) {
                    currentPipelineFile = detectedPipeline;
                    final String detected = detectedPipeline;
                    SwingUtilities.invokeLater(() -> pipelineFileText.setText(detected));
                }
            }

            if (currentPipelineFile.isEmpty()) {
                return;
            }

            // Step 3: Copy pipeline to temp dir and create DockerPipeline
            final String pipelineFileToUse = currentPipelineFile;
            try {
                File tempDir = docker.getOrCreateTempDir();
                File pipelineXmlFile = docker.copyPipelineToLocal(tempDir, pipelineFileToUse);

                if (!pipelineXmlFile.exists()) {
                    return;
                }

                Pipeline p = PipelineParserFactory.createPipelineParser().parsePipeline(pipelineXmlFile);
                DockerPipeline dockerPipeline = new DockerPipeline(p, tempDir, pipelineFileToUse, imageName, compiPath);
                PipelineRunConfiguration prc = new PipelineRunConfiguration(p, pipelineFileToUse);

                SwingUtilities.invokeLater(() -> {
                    onUpdatePipeline(dockerPipeline, prc);
                });

            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    imageNameText.setBackground(UISettings.INVALID_BACKGROUND);
                    compiVersionArea.setText(compiVersionArea.getText() + "\nError loading pipeline: " + e.getMessage());
                });
            }
        }).start();
    }

    private void onUpdatePipeline(PipelineInfoProvider pp, PipelineRunConfiguration prc) {
        for (PipelineListener listener : listenersList) {
            listener.onUpdatePipeline(pp, prc);
        }
    }

    public void addListener(PipelineListener pp) {
        this.listenersList.add(pp);
    }

    /**
     * Restores the Docker panel from a fully-loaded DockerPipeline (e.g. from
     * compi-gui.json). Populates the fields and fires onUpdatePipeline directly
     * on the EDT — no background Docker re-check, so no new DockerPipeline is
     * created and the already-loaded param types are preserved.
     */
    public void setDockerInfo(DockerPipeline dockerPipeline) {
        String imageName = dockerPipeline.getDockerImage();
        String compiPath = dockerPipeline.getCompiPath();
        String pipelineFile = dockerPipeline.getPipelineFileName();

        imageNameText.setText(imageName != null ? imageName : "");
        compiPathText.setText(compiPath != null ? compiPath : "/compi");
        pipelineFileText.setText(pipelineFile != null ? pipelineFile : "");

        compiVersionArea.setText("Loaded from saved configuration");
        compiVersionArea.setBackground(UISettings.VALID_BACKGROUND);
        imageNameText.setBackground(UISettings.VALID_BACKGROUND);

        PipelineRunConfiguration prc = new PipelineRunConfiguration(
            dockerPipeline.getPipeline(), pipelineFile);
        onUpdatePipeline(dockerPipeline, prc);
    }

    /**
     * Restores the Docker panel fields from a previously saved configuration
     * and immediately triggers Docker checks.
     */
    public void setDockerInfo(String imageName, String compiPath, String pipelineFile) {
        imageNameText.setText(imageName != null ? imageName : "");
        compiPathText.setText(compiPath != null ? compiPath : "/compi");
        pipelineFileText.setText(pipelineFile != null ? pipelineFile : "");
        // Trigger checks to revalidate and rebuild the pipeline
        triggerDockerChecks();
    }

    public String getDockerImage() {
        return imageNameText != null ? imageNameText.getText().trim() : "";
    }

    public String getCompiPath() {
        return compiPathText != null ? compiPathText.getText().trim() : "/compi";
    }

    public String getPipelineFile() {
        return pipelineFileText != null ? pipelineFileText.getText().trim() : "";
    }
}
