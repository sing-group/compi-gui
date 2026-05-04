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
package org.sing_group.compi.gui.gui.util;

import java.awt.Color;
import java.lang.reflect.Field;

import javax.swing.JTextField;

import org.sing_group.gc4s.input.filechooser.JFileChooserPanel;

public class JFileChooserPanelWrapper {
    private final JFileChooserPanel panel;
    private final JTextField textField;

    public JFileChooserPanelWrapper(JFileChooserPanel panel) {
        this.panel = panel;
        this.textField = extractTextField();
    }

    private JTextField extractTextField() {
        try {
            Field field = JFileChooserPanel.class.getDeclaredField("fileName");
            field.setAccessible(true);
            return (JTextField) field.get(panel);
        } catch (Exception e) {
            return null;
        }
    }

    public void setBackground(Color color) {
        if (textField != null) {
            textField.setBackground(color);
        }
    }

    public JFileChooserPanel getPanel() {
        return panel;
    }
}
