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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.xmlio.PipelineParserFactory;

public class CompiPipeline implements PipelineInfoProvider {

    private static final String CONFIG_FILE_NAME = "compi-gui.json";
    private static final String LOCAL_PIPELINE_INFO_KEY = "local_pipeline_info";

    private final Pipeline pipeline;
    private File pipelineDir;
    private String pipelineFileName;
    private String compiPath;
    private ParamManager paramManager;

    public CompiPipeline(Pipeline pipeline, File pipelineDir) {
        this(pipeline, pipelineDir, "pipeline.xml", "compi");
    }

    public CompiPipeline(Pipeline pipeline, File pipelineDir, String pipelineFileName) {
        this(pipeline, pipelineDir, pipelineFileName, "compi");
    }

    public CompiPipeline(Pipeline pipeline, File pipelineDir, String pipelineFileName, String compiPath) {
        this.pipeline = pipeline;
        this.pipelineDir = pipelineDir;
        this.pipelineFileName = pipelineFileName;
        this.compiPath = (compiPath != null && !compiPath.isEmpty()) ? compiPath : "compi";
        this.paramManager = new ParamManager();
        paramManager.initializeFromPipeline(pipeline);
    }

    public static CompiPipeline fromWorkingDirectory(File workingDir) {
        System.out.println("[DEBUG] CompiPipeline.fromWorkingDirectory() called with: " + workingDir);
        if (workingDir == null || !workingDir.exists()) {
            return null;
        }

        File configFile = new File(workingDir, CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            return null;
        }

        try {
            String content = readFile(configFile);
            JSONArray jsonArray = new JSONArray(content);
            if (jsonArray.length() == 0) {
                return null;
            }

            JSONObject config = jsonArray.getJSONObject(0);

            if (!config.has(LOCAL_PIPELINE_INFO_KEY)) {
                System.out.println("[DEBUG] CompiPipeline.fromWorkingDirectory: no " + LOCAL_PIPELINE_INFO_KEY + " in config");
                return null;
            }

            JSONObject pipelineInfo = config.getJSONObject(LOCAL_PIPELINE_INFO_KEY);
            String pipelineDirPath = pipelineInfo.getString("pipeline_directory");
            String pipelineFileName = pipelineInfo.getString("pipeline_file");
            String compiPath = pipelineInfo.optString("compi_path", "compi");

            System.out.println("[DEBUG] CompiPipeline.fromWorkingDirectory: pipelineDirPath=" + pipelineDirPath
                + ", pipelineFileName=" + pipelineFileName + ", compiPath=" + compiPath);

            File pipelineDirFile = new File(pipelineDirPath);
            File pipelineXmlFile = new File(pipelineDirFile, pipelineFileName);

            if (!pipelineDirFile.exists() || !pipelineXmlFile.exists()) {
                System.out.println("[DEBUG] CompiPipeline.fromWorkingDirectory: pipeline files don't exist");
                return null;
            }

            Pipeline p = PipelineParserFactory.createPipelineParser().parsePipeline(pipelineXmlFile);
            CompiPipeline compiPipeline = new CompiPipeline(p, pipelineDirFile, pipelineFileName, compiPath);

            if (config.has("param_types")) {
                compiPipeline.applyTypesFromJSON(config.getJSONObject("param_types"));
            }

            return compiPipeline;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<GuiParam> listParams() {
        return paramManager.getParamList();
    }

    @Override
    public Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void updateParamList(List<GuiParam> list) {
        paramManager.setParamList(list);
    }

    @Override
    public File getPipelineDir() {
        return pipelineDir;
    }

    public String getCompiPath() {
        return compiPath;
    }

    public void setCompiPath(String compiPath) {
        this.compiPath = (compiPath != null && !compiPath.isEmpty()) ? compiPath : "compi";
    }

    public void applyTypesFromJSON(JSONObject paramTypes) {
        paramManager.applyTypesFromJSON(paramTypes);
    }

    public ParamManager getParamManager() {
        return paramManager;
    }

    @Override
    public JSONObject getParamTypesAsJSON() {
        return paramManager.toJSON();
    }

    @Override
    public void saveToWorkingDirectory(File workingDir) {
        System.out.println("[DEBUG] CompiPipeline.saveToWorkingDirectory() called with: " + workingDir);
        if (workingDir == null || !workingDir.exists()) {
            return;
        }

        File configFile = new File(workingDir, CONFIG_FILE_NAME);
        JSONObject existingParamTypes = null;
        if (configFile.exists()) {
            try {
                String content = readFile(configFile);
                JSONArray existingArray = new JSONArray(content);
                if (existingArray.length() > 0) {
                    JSONObject existingConfig = existingArray.getJSONObject(0);
                    if (existingConfig.has("param_types")) {
                        existingParamTypes = existingConfig.getJSONObject("param_types");
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        JSONObject paramTypes = getParamTypesAsJSON();
        if ((paramTypes == null || paramTypes.isEmpty()) && existingParamTypes != null) {
            paramTypes = existingParamTypes;
        }

        JSONObject config = new JSONObject();
        if (paramTypes != null && !paramTypes.isEmpty()) {
            config.put("param_types", paramTypes);
        }

        JSONObject pipelineInfo = new JSONObject();
        pipelineInfo.put("pipeline_directory", pipelineDir.getAbsolutePath());
        pipelineInfo.put("pipeline_file", getPipelineFileName());
        pipelineInfo.put("compi_path", compiPath);
        config.put(LOCAL_PIPELINE_INFO_KEY, pipelineInfo);

        JSONArray array = new JSONArray();
        array.put(config);
        System.out.println("[DEBUG] CompiPipeline.saveToWorkingDirectory: Final JSON = " + array.toString());
        writeToFile(configFile, array.toString());
    }

    @Override
    public void loadFromWorkingDirectory(File workingDir) {
        if (workingDir == null || !workingDir.exists()) {
            return;
        }

        File configFile = new File(workingDir, CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            return;
        }

        try {
            String content = readFile(configFile);
            JSONArray jsonArray = new JSONArray(content);
            if (jsonArray.length() == 0) {
                return;
            }

            JSONObject config = jsonArray.getJSONObject(0);
            if (config.has("param_types")) {
                applyTypesFromJSON(config.getJSONObject("param_types"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromFile(File configFile) {
        if (configFile == null || !configFile.exists()) {
            return;
        }

        try {
            String content = readFile(configFile);
            JSONArray jsonArray = new JSONArray(content);
            if (jsonArray.length() == 0) {
                return;
            }

            JSONObject config = jsonArray.getJSONObject(0);
            if (config.has("param_types")) {
                applyTypesFromJSON(config.getJSONObject("param_types"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the full run command for local execution:
     * <compiPath> run -pa <paramsFile> -p <pipelineDir>/<pipelineFile> <cliParameters>
     */
    @Override
    public String getRunCommand(File runDir, File paramsFile, Map<String, String> paramValues, String cliParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(compiPath)
            .append(" run -pa ")
            .append(paramsFile.getAbsolutePath())
            .append(" -p ")
            .append(new File(pipelineDir, pipelineFileName).getAbsolutePath());
        if (cliParameters != null && !cliParameters.isEmpty()) {
            sb.append(" ").append(cliParameters);
        }
        return sb.toString();
    }

    @Override
    public String getPipelineLoadedMessage() {
        return "Source: Local | " + pipeline.getTasks().size() + " tasks";
    }

    public String getPipelineFileName() {
        return pipelineFileName;
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void writeToFile(File file, String content) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
