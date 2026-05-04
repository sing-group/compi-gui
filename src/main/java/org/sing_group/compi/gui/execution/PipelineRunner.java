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
package org.sing_group.compi.gui.execution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;
import org.sing_group.compi.gui.models.Type;

public class PipelineRunner {

    private PipelineRunConfiguration pipeline;
    private PipelineInfoProvider pipelineInfoProvider;
    private File runDir;

    private Map<String, String> paramValues;
    private List<GuiParam> paramList;

    public PipelineRunner(
        File runDir,
        PipelineRunConfiguration pipeline,
        PipelineInfoProvider pipelineInfoProvider,
        Map<String, String> paramValues,
        List<GuiParam> paramList
    ) {
        this.runDir = Objects.requireNonNull(runDir);
        if (!runDir.exists()) {
            if (!runDir.mkdirs()) {
                throw new IllegalArgumentException("Could not create run directory: " + runDir.getAbsolutePath());
            }
        }
        this.pipeline = Objects.requireNonNull(pipeline);
        this.pipelineInfoProvider = Objects.requireNonNull(pipelineInfoProvider);
        this.paramValues = Objects.requireNonNull(paramValues);
        this.paramList = Objects.requireNonNull(paramList);
    }

    public List<String> getCommandParamsList(File runDir) {
        String os = System.getProperty("os.name").toLowerCase();
        List<String> lista = new ArrayList<>();
        if (os.contains("win")) {
            lista.add("wsl");
            lista.add(fixPath(runDir.getAbsolutePath() + "/run.sh"));
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            lista.add(runDir.getAbsolutePath() + "/run.sh");
        }
        return lista;
    }

    public void execute(InputLineCallback callback) throws IOException, InterruptedException {
        System.out.println("RUN COMPI");

        saveTypesOnJSONFile();
        saveValuesOnFile();
        saveRunFile();

        new AbstractBinariesExecutor().executeCommand(
                getCommandParamsList(this.runDir),
                callback
        );
    }

    private void saveTypesOnJSONFile() {
        JSONObject types = new JSONObject();
        JSONArray arrayTypes = new JSONArray();

        for (int i = 0; i < paramList.size(); i++) {
            types.put(paramList.get(i).getName(), paramList.get(i).getType().getVal());
        }

        arrayTypes.put(types);

        try (FileWriter fw = new FileWriter(new File(runDir, "compi-gui.json").getAbsolutePath())) {
            fw.write(arrayTypes.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Type getParamType(String paramName) {
        for (GuiParam param : paramList) {
            if (param.getName().equals(paramName)) {
                return param.getType();
            }
        }
        throw new IllegalArgumentException("Parameter not found: " + paramName);
    }

    private void saveValuesOnFile() {
        StringBuilder salida = new StringBuilder();
        Iterator<String> it = paramValues.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            String value = paramValues.get(key);
            if ("true".equals(value) || "false".equals(value)) {
                salida.append(key);
            } else {
                Type currentParamType = getParamType(key);
                if (currentParamType.equals(Type.DIRECTORY) || currentParamType.equals(Type.FILE)) {
                    salida.append(key).append("=").append(fixPath(value));
                } else {
                    salida.append(key).append("=").append(value);
                }
            }
            salida.append(System.lineSeparator());
        }

        try (FileWriter fw = new FileWriter(new File(runDir, "compi.params").getAbsolutePath())) {
            fw.write(salida.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String fixPath(String path) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            System.out.println("Estás en Windows: " + path);
            path = path.replaceAll("(?i)C:\\\\", "/mnt/c/");
            path = path.replaceAll("\\\\", "/");
            System.out.println("Estás en Windows: " + path);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            System.out.println("Estás en Linux o Unix");
        }

        return path;
    }

    private void saveRunFile() {
        File runFile = new File(runDir, "run.sh");
        try (FileWriter fw = new FileWriter(runFile.getAbsolutePath())) {
            File paramsFile = new File(runDir, "compi.params");
            String cliParameters = pipeline.getRunConfiguration().getCliParameters(this::createLogsDir);

            String runCommand = pipelineInfoProvider.getRunCommand(runDir, paramsFile, paramValues, cliParameters);

            // Apply path fixes for Windows (WSL path conversion)
            runCommand = fixPath(runCommand);

            StringBuilder sb = new StringBuilder();
            sb.append("#!/bin/bash\n\n").append(runCommand).append("\n");

            fw.write(sb.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runFile.setExecutable(true, true);
    }

    private String createLogsDir() {
        File logDir = new File(this.runDir, "logs");
        logDir.mkdirs();
        return fixPath(logDir.getAbsolutePath());
    }
}
