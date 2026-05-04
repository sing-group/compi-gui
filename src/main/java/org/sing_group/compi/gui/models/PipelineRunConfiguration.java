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
import java.util.Map;

import org.sing_group.compi.core.pipeline.Pipeline;

public class PipelineRunConfiguration{

    private File workingDirectory;
    private Map<String, String> paramValues;
    private CompiRunConfiguration compiConfiguration;
    private Pipeline pipeline;
    private String pipelineFileName;
    
    public PipelineRunConfiguration(Pipeline pipeline, String pipelineFileName){
        this.pipelineFileName = pipelineFileName;
        this.pipeline = pipeline;
        this.compiConfiguration = new CompiRunConfiguration();
        this.workingDirectory = new File("");
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public String getPipelineFileName() {
        return pipelineFileName;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public CompiRunConfiguration getRunConfiguration(){
        return this.compiConfiguration;
    }

    public void setRunConfiguration(CompiRunConfiguration config){
        this.compiConfiguration = config;
    }

    public Map<String,String> getParameterValues(){
        return paramValues;
    }

    public void updateParamValues(Map<String, String> map){
        this.paramValues = map;
    }
        
}
