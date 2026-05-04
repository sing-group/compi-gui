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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import org.sing_group.compi.gui.gui.util.JFileChooserPanelWrapper;
import org.sing_group.compi.gui.gui.util.UISettings;
import org.sing_group.compi.gui.listeners.WorkingDirectoryListener;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanelBuilder;
import org.sing_group.gc4s.input.filechooser.SelectionMode;
import org.sing_group.gc4s.input.filechooser.event.FileChooserListener;

public class WorkingDirectoryPanel extends JPanel {

    private JFileChooserPanel fileChooser;
    private JFileChooserPanelWrapper wrapper;
    private List<WorkingDirectoryListener> listeners;

    public WorkingDirectoryPanel() {
        this.listeners = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        fileChooser = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
            .withLabel("Working directory: ")
            .withFileChooserSelectionMode(SelectionMode.DIRECTORIES)
            .build();

        wrapper = new JFileChooserPanelWrapper(fileChooser);
        wrapper.setBackground(UISettings.INVALID_BACKGROUND);

        fileChooser.addFileChooserListener(new FileChooserListener() {
            @Override
            public void onFileChoosed(ChangeEvent event) {
                File selectedDir = fileChooser.getSelectedFile();
                wrapper.setBackground(UISettings.VALID_BACKGROUND);
                notifyListeners(selectedDir);
            }
        });

        add(fileChooser);
    }

    public void addWorkingDirectoryListener(WorkingDirectoryListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeWorkingDirectoryListener(WorkingDirectoryListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(File directory) {
        for (WorkingDirectoryListener listener : listeners) {
            listener.onWorkingDirectoryChanged(directory);
        }
    }

    public File getSelectedDirectory() {
        return fileChooser.getSelectedFile();
    }

    public void setValidState(boolean valid) {
        if (valid) {
            wrapper.setBackground(UISettings.VALID_BACKGROUND);
        } else {
            wrapper.setBackground(UISettings.INVALID_BACKGROUND);
        }
    }
}
