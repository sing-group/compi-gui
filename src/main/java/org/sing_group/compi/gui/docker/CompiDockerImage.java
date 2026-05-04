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
package org.sing_group.compi.gui.docker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiDockerImage {

    private static final String TEMP_DIR_PREFIX = "compi-docker-temp-";

    private final String imageName;
    private File tempDir;

    public CompiDockerImage(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean hasCompi() {
        return getCompiVersion("/compi") != null;
    }

    /**
     * Runs "<compiPath> --version" inside the Docker image and returns the output string,
     * or null if the command fails or Compi is not found at that path.
     */
    public String getCompiVersion(String compiPath) {
        try {
            List<String> command = buildCommandWithEntrypoint(compiPath, "--version");
            String output = executeCommandWithOutput(command);
            return output;
        } catch (IOException | InterruptedException e) {
            System.err.println("[Docker] Error checking for Compi at " + compiPath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds a "docker run --rm --entrypoint <entrypoint> <image> [args...]" command.
     * Unlike buildCommand(), this always uses the given entrypoint regardless of path format.
     */
    private List<String> buildCommandWithEntrypoint(String entrypoint, String... args) {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("--entrypoint");
        command.add(entrypoint);
        command.add(imageName);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    public String findPipelineFileName() {
        try {
            List<String> command = buildCommandWithEntrypoint("/bin/sh", "-c", "ls /");
            String output = executeCommandWithOutput(command);
            
            if (output == null || output.isEmpty()) {
                return null;
            }

            Pattern pattern = Pattern.compile("^pipeline.*\\.xml$", Pattern.CASE_INSENSITIVE);
            String[] files = output.split("\n");
            for (String file : files) {
                file = file.trim();
                Matcher matcher = pattern.matcher(file);
                if (matcher.matches()) {
                    return file;
                }
            }
            return null;
        } catch (IOException | InterruptedException e) {
            System.err.println("[Docker] Error finding pipeline file: " + e.getMessage());
            return null;
        }
    }

    public File copyPipelineToLocal(File destDir) throws IOException {
        String pipelineFileName = findPipelineFileName();
        if (pipelineFileName == null) {
            throw new IOException("No pipeline file found in Docker image");
        }
        return copyPipelineToLocal(destDir, pipelineFileName);
    }

    public File copyPipelineToLocal(File destDir, String pipelineFileName) throws IOException {
        File destFile = new File(destDir, pipelineFileName);

        try {
            List<String> command = buildCommandWithEntrypoint("/bin/sh", "-c", "cat /" + pipelineFileName);
            String output = executeCommandWithOutput(command);
            if (output == null) {
                throw new IOException("Failed to read pipeline file from Docker image: " + pipelineFileName);
            }

            try (FileWriter writer = new FileWriter(destFile)) {
                writer.write(output);
            }

            return destFile;
        } catch (IOException | InterruptedException e) {
            throw new IOException("Error copying pipeline file from Docker: " + e.getMessage(), e);
        }
    }

    public File getOrCreateTempDir() throws IOException {
        if (tempDir == null || !tempDir.exists()) {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
        }
        return tempDir;
    }

    public void cleanupTempDir() {
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
            tempDir = null;
        }
    }

    private String executeCommandWithOutput(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return null;
        }
        
        return output.toString();
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
