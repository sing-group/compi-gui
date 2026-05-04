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
import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.sing_group.compi.gui.controller.AppController;

public class MainFrame {

    private JFrame window;
    private JTabbedPane tabs;
    private JLabel placeholder;
    private CardLayout cardLayout;
    private JPanel centerPanel;
    private AppController controller;

    public MainFrame(JFrame window) {
        this.window = window;
        initializeUI();
    }

    private void initializeUI() {
        window.setTitle("Compi GUI");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 2;
        int height = screenSize.height / 2;
        window.setSize(width, height);
        window.setLocationRelativeTo(null);

        PipelinePanel pipelinePanel = new PipelinePanel();
        pipelinePanel.createPipelinePanelInterface();

        ParamsConfigurationPanel paramsPanel = new ParamsConfigurationPanel();

        RunPanel runPanel = new RunPanel();
        runPanel.createRunInterface();

        CompiRunParametersPanel compiRunParametersPanel = new CompiRunParametersPanel(window);

        WorkingDirectoryPanel workingDirPanel = new WorkingDirectoryPanel();

        tabs = new JTabbedPane();

        placeholder = new JLabel("Please, select a working directory to start", SwingConstants.CENTER);
        placeholder.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));

        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(placeholder, "placeholder");
        centerPanel.add(tabs, "tabs");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(workingDirPanel, BorderLayout.SOUTH);

        window.add(mainPanel);

        controller = new AppController(
                window,
                pipelinePanel,
                paramsPanel,
                runPanel,
                compiRunParametersPanel,
                workingDirPanel,
                tabs,
                this
        );

        pipelinePanel.addListener(controller);
        paramsPanel.addListener(controller);
        compiRunParametersPanel.addListener(controller);
        workingDirPanel.addWorkingDirectoryListener(controller);
    }

    public void view() {
        window.setVisible(true);
    }

    public void showTabs() {
        cardLayout.show(centerPanel, "tabs");
    }
}
