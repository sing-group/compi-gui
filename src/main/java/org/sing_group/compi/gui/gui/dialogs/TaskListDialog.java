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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;

public class TaskListDialog extends JDialog {

    private List<Task> tasks;
    private List<Task> selectedTasks;
    private List<JCheckBox> checkBoxes;

    public TaskListDialog(JFrame parent, List<Task> tasks, List<Task> selectedTasks, boolean isFromTaskMode) {
        super(parent, isFromTaskMode ? "Select From Tasks" : "Select After Tasks", true);
        this.tasks = tasks;
        this.selectedTasks = new ArrayList<>(selectedTasks);
        this.checkBoxes = new ArrayList<>();
        initDialog();
    }

    private void initDialog() {
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        List<InputParameter> inputParameters = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            JCheckBox checkBox = new JCheckBox();
            checkBoxes.add(checkBox);

            if (searchSelected(tasks.get(i))) {
                checkBox.setSelected(true);
            }

            final int index = i;
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (checkBox.isSelected()) {
                        addTask(index);
                    } else {
                        removeTask(index);
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
                updateSelectedTasks();
                dispose();
            }
        });
        buttonPanel.add(closeButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(taskListPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private boolean searchSelected(Task task) {
        for (Task t : selectedTasks) {
            if (t.getId().equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private void updateSelectedTasks() {
        selectedTasks.clear();
        for (int i = 0; i < tasks.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                selectedTasks.add(tasks.get(i));
            }
        }
    }

    private void addTask(int index) {
        Task task = tasks.get(index);
        if (!searchSelected(task)) {
            selectedTasks.add(task);
        }
    }

    private void removeTask(int index) {
        Task task = tasks.get(index);
        selectedTasks.removeIf(t -> t.getId().equals(task.getId()));
    }

    public List<Task> getSelectedTasks() {
        return selectedTasks;
    }

    public String getSelectedTasksAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedTasks.size(); i++) {
            sb.append(selectedTasks.get(i).getId());
            if (i < selectedTasks.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
