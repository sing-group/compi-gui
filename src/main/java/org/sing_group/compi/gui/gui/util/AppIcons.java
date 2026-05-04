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

import javax.swing.ImageIcon;

public class AppIcons {

    private static final String ICONS_PATH = "/org/sing_group/compi/gui/icons/";

    public static final ImageIcon ICON_PIPELINE       = load("pipeline.png");
    public static final ImageIcon ICON_PIPELINE_LOCAL = load("pipeline_local.png");
    public static final ImageIcon ICON_DOCKER         = load("docker.png");
    public static final ImageIcon ICON_PARAMS         = load("params.png");
    public static final ImageIcon ICON_COMPI          = load("compi.png");
    public static final ImageIcon ICON_RUN            = load("run.png");

    private static ImageIcon load(String filename) {
        java.net.URL url = AppIcons.class.getResource(ICONS_PATH + filename);
        if (url == null) {
            System.err.println("[AppIcons] Resource not found: " + ICONS_PATH + filename);
            return null;
        }
        return new ImageIcon(url);
    }
}
