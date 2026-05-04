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
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.sing_group.compi.core.pipeline.Pipeline;

public interface PipelineInfoProvider {

    public List<GuiParam> listParams();

    public Pipeline getPipeline();

    public void updateParamList(List<GuiParam> list);

    public File getPipelineDir();

    /**
     * Returns a short human-readable message describing the loaded pipeline
     * and its source (e.g. "Source: Local | 5 tasks" or "Source: Docker | 3 tasks").
     * Displayed in the Pipeline panel info label on successful load.
     */
    public String getPipelineLoadedMessage();

    public void saveToWorkingDirectory(File workingDir);

    public void loadFromWorkingDirectory(File workingDir);

    public void loadFromFile(File configFile);

    public JSONObject getParamTypesAsJSON();

    /**
     * Returns the full run command string to be written to run.sh.
     *
     * @param runDir        the run execution directory
     * @param paramsFile    the compi.params file
     * @param paramValues   map of parameter name to value (used to collect volume mounts for Docker)
     * @param cliParameters extra Compi CLI parameters (flags, task selectors, etc.)
     * @return the full command string
     */
    public String getRunCommand(File runDir, File paramsFile, Map<String, String> paramValues, String cliParameters);
}
