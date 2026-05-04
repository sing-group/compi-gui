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
package org.sing_group.compi.gui.listeners;

import java.util.List;
import java.util.Map;

import org.sing_group.compi.gui.models.CompiRunConfiguration;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.compi.gui.models.PipelineRunConfiguration;

public interface PipelineListener {

    public void onUpdatePipeline(PipelineInfoProvider pp, PipelineRunConfiguration prc);

    public void onUpdateCompiRunConfiguration(CompiRunConfiguration config);

    public void onUpdateParametersValues(Map<String,String> paramValues);

    public void onUpdateParametersTypes(List<GuiParam> paramList);

    public void onInvalidValuesConfiguration();

    public void onValidValuesConfiguration();

    /**
     * Called when a pipeline fails to load (e.g. the file does not exist).
     * Default implementation is a no-op so existing implementors don't need to override.
     */
    default void onPipelineLoadError(String message) {}

}
