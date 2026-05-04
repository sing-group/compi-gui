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
package org.sing_group.compi.gui.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;

public class TaskLogConfigurationDialog extends JDialog {

    private List<Task> tasks;
    private List<Task> loggedTasks;
    private List<Task> noLoggedTasks;
    private List<JCheckBox> checkBoxes;

    private boolean loggedTaskRadioSelected = true;

    public TaskLogConfigurationDialog(JFrame parent, List<Task> tasks, List<Task> loggedTasks, List<Task> noLoggedTasks) {
        super(parent, "Task Log Configuration", true);
        this.tasks = tasks;
        this.loggedTasks = new ArrayList<>(loggedTasks);
        this.noLoggedTasks = new ArrayList<>(noLoggedTasks);
        this.checkBoxes = new ArrayList<>();
        initDialog();
    }

    private void initDialog() {
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel radioPanel = new JPanel();

        JRadioButton loggedTaskRadio = new JRadioButton("Log Task(s)");
        loggedTaskRadio.setToolTipText("Task id(s) whose output will be logged, other tasks' output will be ignored.");
        loggedTaskRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedTaskRadioSelected = true;
                loggedTasks.addAll(noLoggedTasks);
                noLoggedTasks.clear();
                updateCheckBoxes();
            }
        });

        JRadioButton noLoggedTaskRadio = new JRadioButton("Without Log Task(s)");
        noLoggedTaskRadio.setToolTipText("Task id(s) whose output will be ignored, other tasks' output will be saved");
        noLoggedTaskRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loggedTaskRadioSelected = false;
                noLoggedTasks.addAll(loggedTasks);
                loggedTasks.clear();
                updateCheckBoxes();
            }
        });

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(noLoggedTaskRadio);
        radioGroup.add(loggedTaskRadio);

        if (noLoggedTasks.size() > 0) {
            noLoggedTaskRadio.setSelected(true);
            loggedTaskRadioSelected = false;
        } else {
            loggedTaskRadio.setSelected(true);
            loggedTaskRadioSelected = true;
        }

        radioPanel.add(loggedTaskRadio);
        radioPanel.add(noLoggedTaskRadio);

        List<InputParameter> inputParameters = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBoxes.add(checkBox);

            if (loggedTaskRadioSelected) {
                checkBox.setSelected(searchLogged(tasks.get(i)));
            } else {
                checkBox.setSelected(searchNoLogged(tasks.get(i)));
            }

            final int index = i;
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                        if (loggedTaskRadioSelected) {
                            addLoggedTask(index);
                        } else {
                            addNoLoggedTask(index);
                        }
                    } else {
                        if (loggedTaskRadioSelected) {
                            removeLoggedTask(index);
                        } else {
                            removeNoLoggedTask(index);
                        }
                    }
                }
            });

            InputParameter ip = new InputParameter(
                tasks.get(i).getId(),
                checkBox,
                "Task: " + tasks.get(i).getId()
            );
            inputParameters.add(ip);
        }

        InputParametersPanel taskListPanel = new InputParametersPanel(
            inputParameters.toArray(new InputParameter[0])
        );

        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTaskLists();
                dispose();
            }
        });
        buttonPanel.add(closeButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(radioPanel, BorderLayout.NORTH);
        mainPanel.add(taskListPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void updateCheckBoxes() {
        for (int i = 0; i < tasks.size(); i++) {
            if (loggedTaskRadioSelected) {
                checkBoxes.get(i).setSelected(searchLogged(tasks.get(i)));
            } else {
                checkBoxes.get(i).setSelected(searchNoLogged(tasks.get(i)));
            }
        }
    }

    private void updateTaskLists() {
        loggedTasks.clear();
        noLoggedTasks.clear();
        for (int i = 0; i < tasks.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                if (loggedTaskRadioSelected) {
                    loggedTasks.add(tasks.get(i));
                } else {
                    noLoggedTasks.add(tasks.get(i));
                }
            }
        }
    }

    private boolean searchLogged(Task task) {
        for (Task t : loggedTasks) {
            if (t.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean searchNoLogged(Task task) {
        for (Task t : noLoggedTasks) {
            if (t.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private void addLoggedTask(int index) {
        Task task = tasks.get(index);
        if (!searchLogged(task)) {
            loggedTasks.add(task);
        }
    }

    private void removeLoggedTask(int index) {
        Task task = tasks.get(index);
        loggedTasks.removeIf(t -> t.getId().equals(task.getId()));
    }

    private void addNoLoggedTask(int index) {
        Task task = tasks.get(index);
        if (!searchNoLogged(task)) {
            noLoggedTasks.add(task);
        }
    }

    private void removeNoLoggedTask(int index) {
        Task task = tasks.get(index);
        noLoggedTasks.removeIf(t -> t.getId().equals(task.getId()));
    }

    public List<Task> getLoggedTasks() {
        return loggedTasks;
    }

    public List<Task> getNoLoggedTasks() {
        return noLoggedTasks;
    }
}
