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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.gui.docker.CompiDockerImage;
import org.sing_group.compi.xmlio.PipelineParserFactory;

public class DockerPipeline implements PipelineInfoProvider {

    private static final String CONFIG_FILE_NAME = "compi-gui.json";
    private static final String DOCKER_PIPELINE_INFO_KEY = "docker_pipeline_info";

    private final Pipeline pipeline;
    private final File pipelineDir;
    private final String pipelineFileName;
    private final String dockerImage;
    private String compiPath;
    private ParamManager paramManager;

    public DockerPipeline(Pipeline pipeline, File pipelineDir, String pipelineFileName, String dockerImage, String compiPath) {
        this.pipeline = pipeline;
        this.pipelineDir = pipelineDir;
        this.pipelineFileName = pipelineFileName;
        this.dockerImage = dockerImage;
        this.compiPath = (compiPath != null && !compiPath.isEmpty()) ? compiPath : "/compi";
        this.paramManager = new ParamManager();
        paramManager.initializeFromPipeline(pipeline);
    }

    public static DockerPipeline fromWorkingDirectory(File workingDir) {
        System.out.println("[DEBUG] DockerPipeline.fromWorkingDirectory() called with: " + workingDir);
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

            if (!config.has(DOCKER_PIPELINE_INFO_KEY)) {
                System.out.println("[DEBUG] DockerPipeline.fromWorkingDirectory: no " + DOCKER_PIPELINE_INFO_KEY + " in config");
                return null;
            }

            JSONObject pipelineInfo = config.getJSONObject(DOCKER_PIPELINE_INFO_KEY);
            String dockerImage = pipelineInfo.getString("docker_image");
            String pipelineFileName = pipelineInfo.getString("pipeline_file");
            String compiPath = pipelineInfo.optString("compi_path", "/compi");

            System.out.println("[DEBUG] DockerPipeline.fromWorkingDirectory: dockerImage=" + dockerImage
                + ", pipelineFileName=" + pipelineFileName + ", compiPath=" + compiPath);

            CompiDockerImage docker = new CompiDockerImage(dockerImage);
            File tempDir = docker.getOrCreateTempDir();
            File pipelineXmlFile = docker.copyPipelineToLocal(tempDir, pipelineFileName);

            if (!pipelineXmlFile.exists()) {
                System.out.println("[DEBUG] DockerPipeline.fromWorkingDirectory: pipeline file doesn't exist after copy");
                return null;
            }

            Pipeline p = PipelineParserFactory.createPipelineParser().parsePipeline(pipelineXmlFile);
            DockerPipeline dockerPipeline = new DockerPipeline(p, tempDir, pipelineFileName, dockerImage, compiPath);

            if (config.has("param_types")) {
                dockerPipeline.applyTypesFromJSON(config.getJSONObject("param_types"));
            }

            return dockerPipeline;

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

    public String getDockerImage() {
        return dockerImage;
    }

    public String getPipelineFileName() {
        return pipelineFileName;
    }

    public String getCompiPath() {
        return compiPath;
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
        System.out.println("[DEBUG] DockerPipeline.saveToWorkingDirectory() called with: " + workingDir);
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
        pipelineInfo.put("docker_image", dockerImage);
        pipelineInfo.put("pipeline_file", pipelineFileName);
        pipelineInfo.put("compi_path", compiPath);
        config.put(DOCKER_PIPELINE_INFO_KEY, pipelineInfo);
        System.out.println("[DEBUG] DockerPipeline.saveToWorkingDirectory: pipelineInfo = " + pipelineInfo);

        JSONArray array = new JSONArray();
        array.put(config);
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

    @Override
    public String getPipelineLoadedMessage() {
        return "Source: Docker | " + pipeline.getTasks().size() + " tasks";
    }

    /**
     * Returns the full run command for Docker execution:
     * docker run --rm
     *   -v <runDir>:<runDir>
     *   [-v <parentDir>:<parentDir>]...   (one per file/directory parameter)
     *   --entrypoint <compiPath>
     *   <dockerImage>
     *   run -pa <paramsFile> -p /<pipelineFile> <cliParameters>
     */
    @Override
    public String getRunCommand(File runDir, File paramsFile, Map<String, String> paramValues, String cliParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("docker run --rm");

        // Always mount the run directory
        String runDirPath = runDir.getAbsolutePath();
        sb.append(" -v ").append(runDirPath).append(":").append(runDirPath);

        // Collect unique parent directories of file/directory parameters
        Set<String> mountedDirs = new LinkedHashSet<>();
        mountedDirs.add(runDirPath);

        for (GuiParam param : paramManager.getParamList()) {
            if (param.getType() == Type.FILE || param.getType() == Type.DIRECTORY) {
                String value = paramValues.get(param.getName());
                if (value != null && !value.isEmpty()) {
                    File paramFile = new File(value);
                    File parentDir = param.getType() == Type.FILE ? paramFile.getParentFile() : paramFile;
                    if (parentDir != null) {
                        String parentPath = parentDir.getAbsolutePath();
                        if (!mountedDirs.contains(parentPath)) {
                            mountedDirs.add(parentPath);
                            sb.append(" -v ").append(parentPath).append(":").append(parentPath);
                        }
                    }
                }
            }
        }

        sb.append(" --entrypoint ").append(compiPath);
        sb.append(" ").append(dockerImage);
        sb.append(" run -pa ").append(paramsFile.getAbsolutePath());
        sb.append(" -p /").append(pipelineFileName);

        if (cliParameters != null && !cliParameters.isEmpty()) {
            sb.append(" ").append(cliParameters);
        }

        return sb.toString();
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
