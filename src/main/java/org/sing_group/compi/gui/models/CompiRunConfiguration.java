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
package org.sing_group.compi.gui.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.sing_group.compi.core.pipeline.Task;

public class CompiRunConfiguration {

    private int numTasks;
    private boolean logs;
    private List<Task> loggedTask;
    private List<Task> noLoggedTask;
    private Task singleTask;
    private List<Task> fromTaskList;
    private List<Task> afterTaskList;
    private Task beforeTask;
    private Task untilTask;
    private File runners;
    private boolean showStdOuts;
    private boolean quiet;

    public CompiRunConfiguration(){
        this.numTasks = 6;
        this.logs = false;
        this.loggedTask = new ArrayList<>();
        this.noLoggedTask =  new ArrayList<>();
        this.singleTask = null;
        this.fromTaskList = new ArrayList<>();
        this.afterTaskList = new ArrayList<>();
        this.beforeTask =  null;
        this.untilTask = null;
        this.runners = new File("");
        this.showStdOuts = false;
        this.quiet = false;
    }

    public boolean isLogs() {
        return logs;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public boolean isShowStdOuts() {
        return showStdOuts;
    }


    public Task getBeforeTask() {
        return beforeTask;
    }

    public List<Task> getLoggedTask() {
        return loggedTask;
    }

    public List<Task> getNoLoggedTask() {
        return noLoggedTask;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public File getRunners() {
        return runners;
    }

    public Task getSingleTask() {
        return singleTask;
    }

    public Task getUntilTask() {
        return untilTask;
    }

    public void setAfterTaskList(List<Task> afterTaskList) {
        this.afterTaskList = afterTaskList;
    }
    
    public void setFromTaskList(List<Task> fromTaskList) {
        this.fromTaskList = fromTaskList;
    }

    public void setBeforeTask(Task beforeTask) {
        this.beforeTask = beforeTask;
    }

    public void setLoggedTask(List<Task> loggedTask) {
        this.loggedTask = loggedTask;
    }

    public void addLoggedTask(Task task){
        loggedTask.add(task);
    }

    public void removeLoggedTask(Task task){
        loggedTask.remove(task);
    }

    public void setLogs(boolean logs) {
        this.logs = logs;
    }

    public void setNoLoggedTask(List<Task> noLoggedTask) {
        this.noLoggedTask = noLoggedTask;
    }

    public void addNoLoggedTask(Task task){
        noLoggedTask.add(task);
    }

    public void removeNOLoggedTask(Task task){
        noLoggedTask.remove(task);
    }

    public void setNumTasks(int numTasks) {
        this.numTasks = numTasks;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public void setRunners(File runners) {
        this.runners = runners;
    }

    public void setShowStdOuts(boolean showStdOuts) {
        this.showStdOuts = showStdOuts;
    }

    public void setSingleTask(Task singleTask) {
        this.singleTask = singleTask;
    }

    public void setUntilTask(Task untilTask) {
        this.untilTask = untilTask;
    }

    public String getCliParameters(Supplier<String> logsDirSupplier) {
        StringBuilder sb = new StringBuilder();
        sb.append(" --num-tasks ").append(this.numTasks);
        
        if (this.singleTask != null) {
            sb.append(" --single-task ").append(this.singleTask.getId());
        } else if (this.untilTask != null) {
            sb.append(" --until ").append(this.untilTask.getId());
        } else if (this.beforeTask != null) {
            sb.append(" --before ").append(this.beforeTask.getId());
        }

        if (fromTaskList.size() > 0) {
            for (int i = 0; i < fromTaskList.size(); i++) {
                sb.append(" --from ").append(fromTaskList.get(i).getId());
            }

        }

        if (afterTaskList.size() > 0) {
            for (int i = 0; i < afterTaskList.size(); i++) {
                sb.append(" --after ").append(afterTaskList.get(i).getId());
            }
        }

        if(!this.runners.equals(new File(""))){
            sb.append(" --runners-config ").append(this.runners.getAbsolutePath());
        }

        if (this.quiet) {
            sb.append(" --quiet");
        }
        if (this.showStdOuts) {
            sb.append(" --show-std-outs");
        }

        if (this.logs) {
            sb.append(" --logs ").append(logsDirSupplier.get());

            if (loggedTask.size() > 0 & noLoggedTask.size() == 0) {
                for (int i = 0; i < loggedTask.size(); i++) {
                    sb.append(" --log-only-task ");
                    sb.append(loggedTask.get(i).getId());
                }
            } else if (noLoggedTask.size() > 0 & loggedTask.size() == 0) {
                for (int i = 0; i < noLoggedTask.size(); i++) {
                    sb.append(" --no-log-task ");
                    sb.append(noLoggedTask.get(i).getId());
                }
            }
        }

        return sb.toString();
    }
}
