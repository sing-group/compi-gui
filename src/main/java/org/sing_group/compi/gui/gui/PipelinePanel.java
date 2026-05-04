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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.gui.gui.util.AppIcons;
import org.sing_group.compi.gui.gui.util.UISettings;
import org.sing_group.gc4s.ui.icons.Icons;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.models.CompiPipeline;
import org.sing_group.compi.gui.models.CompiRunConfiguration;
import org.sing_group.compi.gui.models.DockerPipeline;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;


public class PipelinePanel extends JPanel implements PipelineListener {

    private LocalSelectionPanel localPanel;
    private DockerSelectionPanel dockerPanel;
    private JTabbedPane pipelineTabs;
    private JLabel info;
    private JButton viewPipelineBtn;
    private JButton viewTasksBtn;

    private PipelineInfoProvider currentPipelineInfo;
    private PipelineRunConfiguration currentRunConfig;

    private List<PipelineListener> listenersList;

    public PipelinePanel() {

        this.localPanel = new LocalSelectionPanel();
        this.dockerPanel = new DockerSelectionPanel();

        this.listenersList = new ArrayList<>();

        this.localPanel.addListener(this);
        this.dockerPanel.addListener(this);

        localPanel.createLocalSelectionInterface();
        dockerPanel.createDockerSelectionInterface();

        this.setLayout(new BorderLayout());
    }

    public void createPipelinePanelInterface() {

        JPanel pipelinePanel = new JPanel(new BorderLayout());

        pipelineTabs = new JTabbedPane();

        pipelineTabs.addTab("Local",  AppIcons.ICON_PIPELINE_LOCAL, localPanel);
        pipelineTabs.addTab("Docker", AppIcons.ICON_DOCKER,         dockerPanel);

        JPanel infoPanel = new JPanel(new BorderLayout());
        info = new JLabel();
        info.setOpaque(true);
        info.setSize(200, 300);
        info.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        info.setBounds(ALLBITS, ABORT, 500, 60);
        infoPanel.add(info, BorderLayout.WEST);

        viewPipelineBtn = new JButton("View Pipeline", Icons.ICON_ZOOM_IN_24);
        viewPipelineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showViewPipelineDialog();
            }
        });
        viewPipelineBtn.setEnabled(false);

        viewTasksBtn = new JButton("View Tasks", Icons.ICON_ZOOM_IN_24);
        viewTasksBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showViewTasksDialog();
            }
        });
        viewTasksBtn.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewPipelineBtn);
        buttonPanel.add(viewTasksBtn);
        infoPanel.add(buttonPanel, BorderLayout.EAST);

        setInfo("No pipeline selected", UISettings.INVALID_BACKGROUND);

        pipelinePanel.add(infoPanel, BorderLayout.NORTH);
        pipelinePanel.add(pipelineTabs, BorderLayout.CENTER);

        this.add(pipelinePanel);
    }

    public void clearInterface() {
        this.removeAll();
    }

    public void createDockerSelectionInterface() {
        dockerPanel.createDockerSelectionInterface();
    }

    public void createLocalSelectionInterface() {
        localPanel.createLocalSelectionInterface();
    }

    public void addListener(PipelineListener pp) {
        this.listenersList.add(pp);
    }

    public void setInfo(String infoString, Color color) {
        this.info.setText(infoString);
        if (color == null) {
            this.info.setOpaque(false);
            this.info.setBackground(null);
        } else {
            this.info.setOpaque(true);
            this.info.setBackground(color);
        }
        this.info.repaint();
    }

    public void setError(String message) {
        setInfo(message, UISettings.INVALID_BACKGROUND);
    }

    /**
     * Restores a local pipeline configuration in the LocalSelectionPanel.
     */
    public void setPipelineInfo(CompiPipeline compiPipeline, File pipelineDir, String pipelineFile) {
        localPanel.setPipelineInfo(pipelineDir, pipelineFile, compiPipeline.getCompiPath());
        localPanel.onUpdatePipeline(compiPipeline, new PipelineRunConfiguration(compiPipeline.getPipeline(), pipelineFile));
    }

    /**
     * Restores a Docker pipeline configuration in the DockerSelectionPanel
     * and switches to the Docker tab.
     */
    public void setDockerPipelineInfo(DockerPipeline dockerPipeline) {
        dockerPanel.setDockerInfo(dockerPipeline);
        selectDockerTab();
    }

    /**
     * Programmatically selects the Docker sub-tab.
     */
    public void selectDockerTab() {
        if (pipelineTabs != null) {
            pipelineTabs.setSelectedIndex(1);
        }
    }

    /**
     * Programmatically selects the Local sub-tab.
     */
    public void selectLocalTab() {
        if (pipelineTabs != null) {
            pipelineTabs.setSelectedIndex(0);
        }
    }

    @Override
    public void onUpdatePipeline(PipelineInfoProvider pp, PipelineRunConfiguration prc) {
        this.currentPipelineInfo = pp;
        this.currentRunConfig = prc;
        setButtonsEnabled(pp.getPipeline() != null);

        for (PipelineListener listener : listenersList) {
            listener.onUpdatePipeline(pp, prc);
        }
    }

    @Override
    public void onPipelineLoadError(String message) {
        setError(message);
        setButtonsEnabled(false);
    }


    @Override
    public void onUpdateCompiRunConfiguration(CompiRunConfiguration config) {
        throw new UnsupportedOperationException("Unimplemented method 'onUpdateCompiRunConfiguration'");
    }

    @Override
    public void onUpdateParametersValues(Map<String, String> map) {
        throw new UnsupportedOperationException("Unimplemented method 'onUpdateParametersValues'");
    }

    @Override
    public void onInvalidValuesConfiguration() {
        throw new UnsupportedOperationException("Unimplemented method 'onInvalidValuesCOnfiguration'");
    }

    @Override
    public void onValidValuesConfiguration() {
        throw new UnsupportedOperationException("Unimplemented method 'onValidValuesConfiguration'");
    }

    @Override
    public void onUpdateParametersTypes(List<GuiParam> paramList) {
        throw new UnsupportedOperationException("Unimplemented method 'onUpdateParametersTypes'");
    }

    private void setButtonsEnabled(boolean enabled) {
        this.viewPipelineBtn.setEnabled(enabled);
        this.viewTasksBtn.setEnabled(enabled);
    }

    private void showViewPipelineDialog() {
        if (currentPipelineInfo == null || currentPipelineInfo.getPipeline() == null) {
            return;
        }

        JDialog dialog = new JDialog((java.awt.Frame) null, "Pipeline XML", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);

        File pipelineFile = new File(
            currentPipelineInfo.getPipelineDir(),
            currentRunConfig.getPipelineFileName()
        );

        try {
            StringBuilder content = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(pipelineFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            JPanel panel = new JPanel(new BorderLayout());

            javax.swing.JTextArea textArea = new javax.swing.JTextArea(content.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            panel.add(scrollPane, BorderLayout.CENTER);

            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            panel.add(closeBtn, BorderLayout.SOUTH);

            dialog.add(panel);
            dialog.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showViewTasksDialog() {
        if (currentPipelineInfo == null || currentPipelineInfo.getPipeline() == null) {
            return;
        }

        List<Task> tasks = currentPipelineInfo.getPipeline().getTasks();
        String[] columnNames = {"Task Name", "Description"};
        Object[][] data = new Object[tasks.size()][2];

        for (int i = 0; i < tasks.size(); i++) {
            data[i][0] = tasks.get(i).getId();
            data[i][1] = tasks.get(i).getMetadata().getDescription() != null ? tasks.get(i).getMetadata().getDescription() : "No description";
        }

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);

        JDialog dialog = new JDialog((java.awt.Frame) null, "Tasks", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
