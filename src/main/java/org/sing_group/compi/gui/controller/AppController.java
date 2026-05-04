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
package org.sing_group.compi.gui.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.sing_group.compi.gui.gui.CompiRunParametersPanel;
import org.sing_group.compi.gui.gui.ParamsConfigurationPanel;
import org.sing_group.compi.gui.gui.MainFrame;
import org.sing_group.compi.gui.gui.PipelinePanel;
import org.sing_group.compi.gui.gui.RunPanel;
import org.sing_group.compi.gui.gui.WorkingDirectoryPanel;
import org.sing_group.compi.gui.gui.util.AppIcons;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.listeners.WorkingDirectoryListener;
import org.sing_group.compi.gui.models.CompiPipeline;
import org.sing_group.compi.gui.models.CompiRunConfiguration;
import org.sing_group.compi.gui.models.DockerPipeline;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;

public class AppController implements PipelineListener, WorkingDirectoryListener {

    private JFrame window;
    private JTabbedPane tabs;
    private MainFrame mainFrame;

    private PipelinePanel pipelinePanel;
    private RunPanel runPanel;
    private CompiRunParametersPanel compiRunParametersPanel;
    private ParamsConfigurationPanel paramsPanel;

    private PipelineInfoProvider compiPipeline;
    private PipelineRunConfiguration runPipeline;
    private File workingDirFile;

    public AppController(
            JFrame window,
            PipelinePanel pipelinePanel,
            ParamsConfigurationPanel paramsPanel,
            RunPanel runPanel,
            CompiRunParametersPanel compiRunParametersPanel,
            WorkingDirectoryPanel workingDirPanel,
            JTabbedPane tabs,
            MainFrame mainFrame
    ) {
        this.window = window;
        this.pipelinePanel = pipelinePanel;
        this.paramsPanel = paramsPanel;
        this.runPanel = runPanel;
        this.compiRunParametersPanel = compiRunParametersPanel;
        this.tabs = tabs;
        this.mainFrame = mainFrame;
    }

    @Override
    public void onUpdatePipeline(PipelineInfoProvider pp, PipelineRunConfiguration prc) {
        this.compiPipeline = pp;
        this.runPipeline = prc;

        pipelinePanel.setInfo(compiPipeline.getPipelineLoadedMessage(), null);

        paramsPanel.setPipeline(pp);
        paramsPanel.clearInterface();
        paramsPanel.setParamList(pp.listParams());
        paramsPanel.setParamValues(createDefaultMapValues());
        paramsPanel.createParamsConfigurationInterface(true);

        compiRunParametersPanel.setPipeline(prc);
        compiRunParametersPanel.clearInterface();
        compiRunParametersPanel.createCompiRunParametersInterface();
        compiRunParametersPanel.setDefaultConfig(runPipeline.getRunConfiguration());

        runPanel.setPipeline(prc);
        runPanel.setPipelineInfoProvider(pp);
        runPanel.setParamList(pp.listParams());
        runPanel.setParamValues(createDefaultMapValues());
        runPanel.clearInterface();
        runPanel.createRunInterface();

        refreshWindow();

        if (workingDirFile != null) {
            paramsPanel.setSaveValuesEnabled();
            pp.saveToWorkingDirectory(workingDirFile);
        }

        tabs.setEnabledAt(1, true);
        tabs.setEnabledAt(2, true);

        runPipeline.setWorkingDirectory(workingDirFile);
    }

    @Override
    public void onWorkingDirectoryChanged(File workingDir) {
        this.workingDirFile = workingDir;
        paramsPanel.setWorkingDirectory(workingDir);

        if (runPipeline != null) {
            runPipeline.setWorkingDirectory(workingDir);
        }

        if (tabs.getTabCount() == 0) {
            tabs.addTab("Pipeline", AppIcons.ICON_PIPELINE, pipelinePanel);
            tabs.addTab("Params",   AppIcons.ICON_PARAMS,   paramsPanel);
            tabs.addTab("Compi",    AppIcons.ICON_COMPI,    compiRunParametersPanel);
            tabs.addTab("Run",      AppIcons.ICON_RUN,      runPanel);
            mainFrame.showTabs();
        }

        tabs.setEnabledAt(0, true);

        // Try loading a local pipeline first
        CompiPipeline localPipeline = CompiPipeline.fromWorkingDirectory(workingDir);
        if (localPipeline != null) {
            String pipelineFileName = localPipeline.getPipelineFileName();
            PipelineRunConfiguration runConfig = new PipelineRunConfiguration(
                localPipeline.getPipeline(), pipelineFileName);
            runConfig.setWorkingDirectory(workingDir);

            pipelinePanel.setPipelineInfo(localPipeline, localPipeline.getPipelineDir(), pipelineFileName);
            pipelinePanel.selectLocalTab();
            onUpdatePipeline(localPipeline, runConfig);
        } else {
            // Try loading a Docker pipeline
            DockerPipeline dockerPipeline = DockerPipeline.fromWorkingDirectory(workingDir);
            if (dockerPipeline != null) {
                pipelinePanel.setDockerPipelineInfo(dockerPipeline);
            }
        }

        if (paramsPanel.getComponentCount() > 0) {
            paramsPanel.clearInterface();
            paramsPanel.createParamsConfigurationInterface(false);
        }

        tabs.setEnabledAt(3, true);
        paramsPanel.setSaveValuesEnabled();

        refreshWindow();
    }

    @Override
    public void onUpdateCompiRunConfiguration(CompiRunConfiguration config) {
        runPipeline.setRunConfiguration(config);
    }

    @Override
    public void onUpdateParametersValues(Map<String, String> paramValues) {
        runPipeline.updateParamValues(paramValues);
        runPanel.setPipeline(runPipeline);
        runPanel.setParamValues(paramValues);
    }

    @Override
    public void onUpdateParametersTypes(List<GuiParam> paramList) {
        compiPipeline.updateParamList(paramList);

        runPanel.setPipeline(runPipeline);
        runPanel.setParamList(paramList);
        runPanel.setParamValues(createDefaultMapValues());
        runPanel.clearInterface();
        runPanel.createRunInterface();

        paramsPanel.setPipeline(compiPipeline);
        paramsPanel.setParamList(paramList);
        paramsPanel.clearInterface();
        paramsPanel.createParamsConfigurationInterface(false);

        if (compiPipeline != null && workingDirFile != null) {
            compiPipeline.saveToWorkingDirectory(workingDirFile);
        }
    }

    @Override
    public void onInvalidValuesConfiguration() {
        runPanel.setRunButtonEnabled(false);
        runPanel.clearInterface();
        runPanel.createRunInterface();
    }

    @Override
    public void onValidValuesConfiguration() {
        runPanel.setRunButtonEnabled(true);
        runPanel.clearInterface();
        runPanel.createRunInterface();
    }

    private void refreshWindow() {
        window.invalidate();
        window.validate();
        window.repaint();
    }

    private Map<String, String> createDefaultMapValues() {
        Map<String, String> salida = new HashMap<>();

        for (int i = 0; i < compiPipeline.listParams().size(); i++) {
            if (compiPipeline.listParams().get(i).getDescription().getDefaultValue() == null) {
                salida.put(compiPipeline.listParams().get(i).getName(), "");
            } else {
                salida.put(compiPipeline.listParams().get(i).getName(),
                        compiPipeline.listParams().get(i).getDescription().getDefaultValue());
            }
        }

        return salida;
    }
}
