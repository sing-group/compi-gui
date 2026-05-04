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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.gui.gui.dialogs.TaskListDialog;
import org.sing_group.compi.gui.gui.dialogs.TaskLogConfigurationDialog;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.models.CompiRunConfiguration;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.input.combobox.ComboBoxItem;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanelBuilder;
import org.sing_group.gc4s.input.filechooser.SelectionMode;
import org.sing_group.gc4s.input.filechooser.event.FileChooserListener;

public class CompiRunParametersPanel extends JPanel{

    private PipelineRunConfiguration pipeline;
    private CompiRunConfiguration runConfiguration;
    private JFrame window;
    private List<PipelineListener> listenersList;
    
    private List<Task> loggedTasks;
    private List<Task> noLoggedTasks;
    private List<Task> fromTasks;
    private List<Task> afterTasks;

    public CompiRunParametersPanel( JFrame window){
        
        this.window = window;
        this.listenersList = new ArrayList<>();
        loggedTasks = new ArrayList<>();
        noLoggedTasks = new ArrayList<>();
        fromTasks = new ArrayList<>();
        afterTasks = new ArrayList<>();
    }

    
    public void createCompiRunParametersInterface(){

        List<Task> tasks = pipeline.getPipeline().getTasks();
        Task none = new Task(null);
        none.setId("none");

        JPanel compiRunPanel = new JPanel(new BorderLayout());

        InputParametersPanel parametersPanel;
        InputParameter[] parameterArray = new InputParameter[5];

        JPanel northPanel = new JPanel(new BorderLayout());
		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel southPanel = new JPanel(new GridLayout(3,1,20,20));

        JPanel numTask = new JPanel();
        //JPanel logs = new JPanel();
        JPanel logConfiguration = new JPanel();
        JPanel colapsableContent = new JPanel(new GridLayout(5,1,20,20));

        JPanel singleTask = new JPanel();
        JPanel fromTask = new JPanel();
        JPanel afterTask = new JPanel();
        JPanel untilTask = new JPanel();
        JPanel beforeTask = new JPanel();

        JPanel quietPanel = new JPanel();
        JPanel showStdOutsPanel = new JPanel();
        JPanel checksPanel = new JPanel();
        JPanel savePanel = new JPanel();

        JLabel numTasksLabel = new JLabel("Num Tasks: ");
        JSpinner numTasksText = new JSpinner(new SpinnerNumberModel(
            6, //initial value
            1, //min
            pipeline.getPipeline().getTasks().size()+6+1, //max
            1));   //step);

        numTask.add(numTasksLabel);
        numTask.add(numTasksText);
        
        InputParameter ip0 = new InputParameter("Num Tasks: ", numTasksText, "Number of Tasks");
        parameterArray[0] = ip0;

        numTasksText.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(numTasksText.getValue().toString());
                runConfiguration.setNumTasks((int)numTasksText.getValue());
                onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

		JCheckBox logsCheck = new JCheckBox();

        logConfiguration.add(logsCheck);
        InputParameter ip1 = new InputParameter("Enable task(s) logs: ", logConfiguration, "Logging tasks or not");
        parameterArray[1] = ip1;

        JButton logConfigurationButton = new JButton("Task log configuration ");

        logsCheck.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
                if(logsCheck.isSelected()){
                    logConfiguration.add(logConfigurationButton);
                    runConfiguration.setLogs(true);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    logConfiguration.remove(logConfigurationButton);
                    runConfiguration.setLogs(false);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                } 
                
			}
		});

        logConfigurationButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
               getLoggedTask();
			}
		});

        JXTaskPaneContainer containerColapsable = new JXTaskPaneContainer();
        containerColapsable.setOpaque(false);
        
        JXTaskPane colapsable = new JXTaskPane();
        containerColapsable.add(colapsable);
        colapsable.setTitle("Fine-grained task execution control");
        colapsable.add(colapsableContent);
        colapsable.setCollapsed(true);
        
        northPanel.add(numTask, BorderLayout.NORTH);
        //northPanel.add(logs, BorderLayout.CENTER);
        northPanel.add(logConfiguration,BorderLayout.SOUTH);

        JLabel singleTaskLabel = new JLabel("Single Task: ");
        JCheckBox singleTaskCheckBox = new JCheckBox();
		JComboBox<ComboBoxItem<Task>> singleTaskCombo = createComboBox();
        singleTaskCombo.setEnabled(false);
        singleTask.add(singleTaskLabel);
        singleTask.add(singleTaskCheckBox);
        singleTask.add(singleTaskCombo);

        singleTaskCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if(singleTaskCombo.getSelectedIndex() == 0) {
                    runConfiguration.setSingleTask(null);
                    System.out.println(none.getId());
                }else{
                    runConfiguration.setSingleTask(tasks.get(singleTaskCombo.getSelectedIndex()-1));
                    System.out.println(tasks.get(singleTaskCombo.getSelectedIndex()-1).getId());
                } 
                onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

        JLabel fromTaskLabel = new JLabel("From Task: ");
        JCheckBox fromTaskCheckBox = new JCheckBox();
        JTextField fromTaskText = new JTextField();
        JButton fromTaskButton = new JButton("Añadir");
        fromTaskButton.setEnabled(false);
        fromTaskText.setEditable(false);

        fromTask.add(fromTaskLabel);
        fromTask.add(fromTaskCheckBox);
        fromTask.add(fromTaskText);
        fromTask.add(fromTaskButton);

        fromTaskButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getTaskList(true, fromTaskText);
                runConfiguration.setFromTaskList(fromTasks);
                window.invalidate();
                window.validate();
                window.repaint();
            }
            
        });

        JLabel afterTaskLabel = new JLabel("After Task: ");
        JCheckBox afterTaskCheckBox = new JCheckBox();
        JTextField afterTaskText = new JTextField();
        JButton afterTaskButton = new JButton("Añadir");
        afterTaskButton.setEnabled(false);
        afterTaskText.setEditable(false);

        afterTask.add(afterTaskLabel);
        afterTask.add(afterTaskCheckBox);
        afterTask.add(afterTaskText);
        afterTask.add(afterTaskButton);

        afterTaskButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getTaskList(false, afterTaskText);
                runConfiguration.setAfterTaskList(afterTasks);
                window.invalidate();
                window.validate();
                window.repaint();
            }
            
        });

        JLabel untilTaskLabel = new JLabel("Until Task: ");
        JCheckBox untilTaskCheckBox = new JCheckBox();
		JComboBox<ComboBoxItem<Task>> untilTaskCombo = createComboBox();
        untilTaskCombo.setEnabled(false);
        untilTask.add(untilTaskLabel);
        untilTask.add(untilTaskCheckBox);
        untilTask.add(untilTaskCombo);

        untilTaskCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(untilTaskCombo.getSelectedIndex() == 0) {
                    runConfiguration.setUntilTask(none);
                    System.out.println(none.getId());
                }else{
                    runConfiguration.setUntilTask(tasks.get(untilTaskCombo.getSelectedIndex()-1));
                    System.out.println(tasks.get(untilTaskCombo.getSelectedIndex()-1).getId());
                } 
                onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

        JLabel beforeTaskLabel = new JLabel("Before Task: ");
        JCheckBox beforeTaskCheckBox = new JCheckBox();
		JComboBox<ComboBoxItem<Task>> beforeTaskCombo = createComboBox();
        beforeTaskCombo.setEnabled(false);
        beforeTask.add(beforeTaskLabel);
        beforeTask.add(beforeTaskCheckBox);
        beforeTask.add(beforeTaskCombo);
        beforeTaskCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(beforeTaskCombo.getSelectedIndex() == 0) {
                    runConfiguration.setBeforeTask(none);
                    System.out.println(none.getId());
                }else{
                    runConfiguration.setBeforeTask(tasks.get(beforeTaskCombo.getSelectedIndex()-1));
                    System.out.println(tasks.get(beforeTaskCombo.getSelectedIndex()-1).getId());
                } 
                onUpdateCompiRunConfiguration(runConfiguration);
            }
        });

        colapsableContent.add(singleTask);
        colapsableContent.add(fromTask);
        colapsableContent.add(afterTask);
        colapsableContent.add(untilTask);
        colapsableContent.add(beforeTask);

        singleTaskCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(singleTaskCheckBox.isSelected()){
                    singleTaskCombo.setEnabled(true);
                    fromTaskCheckBox.setEnabled(false);
                    afterTaskCheckBox.setEnabled(false);
                    untilTaskCheckBox.setEnabled(false);
                    beforeTaskCheckBox.setEnabled(false);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    singleTaskCombo.setSelectedIndex(0);
                    singleTaskCombo.setEnabled(false);
                    fromTaskCheckBox.setEnabled(true);
                    afterTaskCheckBox.setEnabled(true);
                    untilTaskCheckBox.setEnabled(true);
                    beforeTaskCheckBox.setEnabled(true);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                    runConfiguration.setSingleTask(null);
                }
            }
        });

        untilTaskCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(untilTaskCheckBox.isSelected()){
                    untilTaskCombo.setEnabled(true);
                    singleTaskCheckBox.setEnabled(false);
                    beforeTaskCheckBox.setEnabled(false);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    untilTaskCombo.setSelectedIndex(0);
                    untilTaskCombo.setEnabled(false);
                    singleTaskCheckBox.setEnabled(true);
                    beforeTaskCheckBox.setEnabled(true);
                    runConfiguration.setUntilTask(null);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                }
            }
        });
        beforeTaskCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(beforeTaskCheckBox.isSelected()){
                    beforeTaskCombo.setEnabled(true);
                    singleTaskCheckBox.setEnabled(false);
                    untilTaskCheckBox.setEnabled(false);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    beforeTaskCombo.setSelectedIndex(0);
                    beforeTaskCombo.setEnabled(false);
                    singleTaskCheckBox.setEnabled(true);
                    untilTaskCheckBox.setEnabled(true);
                    runConfiguration.setBeforeTask(null);

                    window.invalidate();
                    window.validate();
                    window.repaint();
                }
            }
        });
        fromTaskCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(fromTaskCheckBox.isSelected()){
                    fromTaskButton.setEnabled(true);
                    singleTaskCheckBox.setEnabled(false);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    fromTaskButton.setEnabled(false);
                    singleTaskCheckBox.setEnabled(true);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                    runConfiguration.setFromTaskList(new ArrayList<Task>());
                    fromTaskText.setText("");
                }
            }
        });

        afterTaskCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(afterTaskCheckBox.isSelected()){
                    afterTaskButton.setEnabled(true);
                    singleTaskCheckBox.setEnabled(false);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                }else{
                    afterTaskButton.setEnabled(false);
                    singleTaskCheckBox.setEnabled(true);
                    window.invalidate();
                    window.validate();
                    window.repaint();
                    runConfiguration.setAfterTaskList(new ArrayList<Task>());
                    afterTaskText.setText("");
                }
            }
        });
        

        JFileChooserPanel runnerConfigFile = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
			.withFileChooserSelectionMode(SelectionMode.FILES)
			.build();

        /*JFileChooserPanel runnerConfigFile = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
			.withLabel("Runners config File: ")
			.withFileChooserSelectionMode(SelectionMode.FILES)
			.build();*/


        runnerConfigFile.addFileChooserListener(new FileChooserListener() {

            @Override
            public void onFileChoosed(ChangeEvent event) {
               System.out.println(runnerConfigFile.getSelectedFile().getAbsolutePath());
               runConfiguration.setRunners(runnerConfigFile.getSelectedFile());
               onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

        InputParameter ip2 = new InputParameter("Runners config File: ", runnerConfigFile, "Runners File");
        parameterArray[2] = ip2;
        
        JLabel quietLabel = new JLabel("Quiet: ");
		JCheckBox quietCheck = new JCheckBox();
        quietPanel.add(quietLabel);
        quietPanel.add(quietCheck);

        InputParameter ip3 = new InputParameter("Quiet: ", quietCheck, "Quiet");
        parameterArray[3] = ip3;

        quietCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               System.out.println(quietCheck.isSelected());
               runConfiguration.setQuiet(quietCheck.isSelected());
               onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

        JLabel showStdOutsLabel = new JLabel("Show STD Outs: ");
		JCheckBox showStdOutsCheck = new JCheckBox();
        showStdOutsPanel.add(showStdOutsLabel);
        showStdOutsPanel.add(showStdOutsCheck);

        InputParameter ip4 = new InputParameter("Show STD Outs: ", showStdOutsCheck, "Show Standar Outs");
        parameterArray[4] = ip4;

        showStdOutsCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               System.out.println(showStdOutsCheck.isSelected());
               runConfiguration.setShowStdOuts(showStdOutsCheck.isSelected());
               onUpdateCompiRunConfiguration(runConfiguration);
            }
            
        });

        checksPanel.add(quietPanel);
        checksPanel.add(showStdOutsPanel);

        southPanel.add(runnerConfigFile);
        southPanel.add(checksPanel);
        southPanel.add(savePanel);

        parametersPanel = new InputParametersPanel(parameterArray);
        centerPanel.add(parametersPanel, BorderLayout.NORTH);
        centerPanel.add(containerColapsable, BorderLayout.CENTER);



        //compiRunPanel.add(northPanel, BorderLayout.NORTH);
        compiRunPanel.add(centerPanel, BorderLayout.CENTER);
        //compiRunPanel.add(southPanel, BorderLayout.SOUTH);

        this.add(compiRunPanel);
    }

    private JComboBox<ComboBoxItem<Task>> createComboBox(){

        JComboBox<ComboBoxItem<Task>> salida = new JComboBox<ComboBoxItem<Task>>();
        Task none = new Task(null);
        none.setId("none");
        List<Task> tasks = pipeline.getPipeline().getTasks();
        salida.addItem(new ComboBoxItem<Task>(none, none.getId()));
        for (int i = 0; i < tasks.size(); i++) {
            salida.addItem(new ComboBoxItem<Task>(tasks.get(i), tasks.get(i).getId()));
        }

        return salida;

    }

    private void getLoggedTask() {
        List<Task> tasks = pipeline.getPipeline().getTasks();
        TaskLogConfigurationDialog dialog = new TaskLogConfigurationDialog(window, tasks, loggedTasks, noLoggedTasks);
        dialog.setVisible(true);

        loggedTasks = dialog.getLoggedTasks();
        noLoggedTasks = dialog.getNoLoggedTasks();

        runConfiguration.setLogs(true);
        runConfiguration.setLoggedTask(loggedTasks);
        runConfiguration.setNoLoggedTask(noLoggedTasks);
    }

    private void getTaskList(boolean flag, JTextField taskText) {
        List<Task> tasks = pipeline.getPipeline().getTasks();
        List<Task> currentSelection = flag ? fromTasks : afterTasks;

        TaskListDialog dialog = new TaskListDialog(window, tasks, currentSelection, flag);
        dialog.setVisible(true);

        List<Task> selected = dialog.getSelectedTasks();
        if (flag) {
            fromTasks = selected;
        } else {
            afterTasks = selected;
        }

        String value = dialog.getSelectedTasksAsString();
        taskText.setText(value);

        window.invalidate();
        window.validate();
        window.repaint();
    }


    public void setPipeline(PipelineRunConfiguration pp){
        this.pipeline = pp;
    }

    public void setDefaultConfig(CompiRunConfiguration config){
        this.runConfiguration = config;
    }

     public void clearInterface(){
        this.removeAll();
    }

    public void addListener(PipelineListener pp){
		this.listenersList.add(pp);
	}

    public void onUpdateCompiRunConfiguration(CompiRunConfiguration config) {
        for (PipelineListener listener : listenersList) {
			listener.onUpdateCompiRunConfiguration(config);
		}
    }

}
