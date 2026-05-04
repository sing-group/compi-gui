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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.sing_group.compi.gui.execution.InputLineCallback;
import org.sing_group.compi.gui.execution.PipelineRunner;
import org.sing_group.compi.gui.gui.util.OnDoubleClickCopyMouseAdapter;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;
import org.sing_group.gc4s.ui.icons.Icons;

public class RunPanel extends JPanel {

    private PipelineRunConfiguration pipeline;
    private PipelineInfoProvider pipelineInfoProvider;
    private boolean enabled;

    private Map<String, String> paramValues;
    private List<GuiParam> paramList;

    private JTextArea errorLogText;
    private InputLineCallback callbacks = new InputLineCallback() {
        @Override
        public void info(String message) {
            SwingUtilities.invokeLater(() -> errorLogText.append(message + "\n"));
        }

        @Override
        public void line(String line) {
            SwingUtilities.invokeLater(() -> errorLogText.append(line + "\n"));
        }

        @Override
        public void error(String message, Exception e) {
            SwingUtilities.invokeLater(() -> errorLogText.append(message + "\n"));
            if (e != null) {
                e.printStackTrace();
            }
        }

        @Override
        public void inputStarted() {
        }

        @Override
        public void inputFinished() {
        }
    };

    public void createRunInterface() {
        System.out.println("SE PUEDE EJECUTAR: " + enabled);
        JPanel runPanel = new JPanel(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel errorLogPanel = new JPanel(new BorderLayout());

        JPanel runDirPanel = new JPanel(new FlowLayout());
        JButton run = new JButton("RUN");
        JTextField runDirText = new JTextField();
        runDirText.setEnabled(false);
        runDirText.setDisabledTextColor(Color.BLACK);
        runDirText.setColumns(30);
        runDirText.addMouseListener(new OnDoubleClickCopyMouseAdapter());
        run.setEnabled(enabled);

        runDirPanel.add(run);
        runDirPanel.add(runDirText);

        centerPanel.add(runDirPanel, BorderLayout.NORTH);

        errorLogText = new JTextArea();
        errorLogText.setEditable(false);
        errorLogText.setAutoscrolls(true);
        errorLogText.setLineWrap(true);
        errorLogText.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(errorLogText);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JButton clearLogBtn = new JButton("Clear", Icons.ICON_TRASH_16);
        clearLogBtn.addActionListener(e -> errorLogText.setText(""));
        JPanel clearLogPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        clearLogPanel.add(clearLogBtn);

        errorLogPanel.add(clearLogPanel, BorderLayout.NORTH);
        errorLogPanel.add(scroll, BorderLayout.CENTER);

        errorLogPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10), "Execution log:"));
        centerPanel.add(errorLogPanel, BorderLayout.CENTER);

        run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File runDir = getTempRunDir();
                SwingUtilities.invokeLater(() -> runDirText.setText(runDir.getAbsolutePath()));
                callbacks.info("\nStarting new execution in: " + runDir.getAbsolutePath() + "\n");
                PipelineRunner runner = new PipelineRunner(runDir, pipeline, pipelineInfoProvider, paramValues, paramList);
                new Thread(() -> {
                    try {
                        runner.execute(callbacks);
                    } catch (IOException | InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    callbacks.info("\nFinished execution in: " + runDir.getAbsolutePath() + "\n");
                }).start();
            }
        });

        runPanel.add(centerPanel, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.add(runPanel, BorderLayout.CENTER);
    }

    private static String createTimestamp() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" +
                (calendar.get(Calendar.MONTH) + 1) + "_" +
                calendar.get(Calendar.DAY_OF_MONTH) + "_" +
                calendar.get(Calendar.HOUR_OF_DAY) + "_" +
                calendar.get(Calendar.MINUTE) + "_" +
                calendar.get(Calendar.SECOND);
    }

    private File getTempRunDir() {
        File runDir = new File(pipeline.getWorkingDirectory() + "/runs/", "run_" + createTimestamp());
        runDir.mkdirs();
        System.out.println(runDir);
        return runDir;
    }

    public void clearInterface() {
        this.removeAll();
    }

    public void setPipeline(PipelineRunConfiguration pp) {
        this.pipeline = pp;
    }

    public void setPipelineInfoProvider(PipelineInfoProvider pip) {
        this.pipelineInfoProvider = pip;
    }

    public void setRunButtonEnabled(boolean enable) {
        this.enabled = enable;
    }

    public void setParamList(List<GuiParam> paramList) {
        this.paramList = paramList;
    }

    public void setParamValues(Map<String, String> paramValues) {
        this.paramValues = paramValues;
    }
}
