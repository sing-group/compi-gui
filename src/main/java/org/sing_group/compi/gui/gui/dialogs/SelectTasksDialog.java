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

public class SelectTasksDialog extends JDialog {

    private List<Task> tasks;
    private List<JCheckBox> tasksToShow;
    private List<Task> selectedTasks;

    public SelectTasksDialog(JFrame parent, List<Task> tasks) {
        super(parent, "Select Tasks", true);
        this.tasks = tasks;
        this.tasksToShow = new ArrayList<>();
        this.selectedTasks = new ArrayList<>();
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
            tasksToShow.add(checkBox);

            InputParameter ip = new InputParameter(
                tasks.get(i).getId(),
                checkBox,
                "Select task: " + tasks.get(i).getId()
            );
            inputParameters.add(ip);
        }

        InputParametersPanel tasksPanel = new InputParametersPanel(
            inputParameters.toArray(new InputParameter[0])
        );

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Task> selected = new ArrayList<>();
                for (int i = 0; i < tasks.size(); i++) {
                    if (tasksToShow.get(i).isSelected()) {
                        selected.add(tasks.get(i));
                    }
                }
                selectedTasks = selected;
                dispose();
            }
        });

        buttonPanel.add(saveButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tasksPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public List<Task> getSelectedTasks() {
        return selectedTasks;
    }
}
