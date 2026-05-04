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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.sing_group.compi.core.pipeline.ParameterDescription;
import org.sing_group.compi.core.pipeline.Pipeline;

public class ParamManager {

    private List<GuiParam> paramList;
    private boolean typesLoadedFromJSON = false;

    public ParamManager() {
        this.paramList = new ArrayList<>();
    }

    public void initializeFromPipeline(Pipeline pipeline) {
        if (typesLoadedFromJSON) {
            System.out.println("[DEBUG] ParamManager.initializeFromPipeline: skipping (types already loaded from JSON)");
            return;
        }
        
        System.out.println("[DEBUG] ParamManager.initializeFromPipeline: initializing with default types");
        List<GuiParam> lista = new ArrayList<>();
        List<ParameterDescription> listParamDesc = pipeline.getParameterDescriptions();

        for (int i = 0; i < listParamDesc.size(); i++) {
            GuiParam param;

            if (listParamDesc.get(i).isFlag()) {
                param = new GuiParam(null, Type.BOOLEAN, null);
                param.setFlag(true);
            } else {
                param = new GuiParam(null, Type.TEXT, null);
            }

            param.setDescription(listParamDesc.get(i));
            param.setName(listParamDesc.get(i).getName());

            if (listParamDesc.get(i).getDefaultValue() == null) {
                param.setValue("");
            } else {
                param.setValue(listParamDesc.get(i).getDefaultValue());
            }

            lista.add(param);
        }
        paramList = lista;
    }

    public void applyTypesFromJSON(JSONObject paramTypes) {
        if (paramTypes == null) {
            return;
        }

        System.out.println("[DEBUG] ParamManager.applyTypesFromJSON: applying types = " + paramTypes);
        List<GuiParam> updatedParams = new ArrayList<>();

        for (GuiParam param : paramList) {
            String paramName = param.getName();
            if (paramTypes.has(paramName)) {
                String typeStr = paramTypes.getString(paramName);
                Type type = parseType(typeStr);
                GuiParam updatedParam = new GuiParam(paramName, type, param.getDescription());
                if (type == Type.BOOLEAN) {
                    updatedParam.setFlag(true);
                }
                updatedParam.setValue(param.getValue());
                updatedParams.add(updatedParam);
            } else {
                updatedParams.add(param);
            }
        }

        paramList = updatedParams;
        typesLoadedFromJSON = true;
        System.out.println("[DEBUG] ParamManager.applyTypesFromJSON: typesLoadedFromJSON set to true");
    }

    private Type parseType(String typeStr) {
        if (typeStr.equals(Type.FILE.getVal())) {
            return Type.FILE;
        } else if (typeStr.equals(Type.DIRECTORY.getVal())) {
            return Type.DIRECTORY;
        } else if (typeStr.equals(Type.BOOLEAN.getVal())) {
            return Type.BOOLEAN;
        } else if (typeStr.equals(Type.INTEGER.getVal())) {
            return Type.INTEGER;
        } else if (typeStr.equals(Type.DECIMAL.getVal())) {
            return Type.DECIMAL;
        }
        return Type.TEXT;
    }

    public List<GuiParam> getParamList() {
        return paramList;
    }

    public void setParamList(List<GuiParam> paramList) {
        this.paramList = paramList;
    }

    public JSONObject toJSON() {
        JSONObject paramTypes = new JSONObject();
        for (GuiParam param : paramList) {
            paramTypes.put(param.getName(), param.getType().getVal());
        }
        return paramTypes;
    }

    public void resetForNewPipeline() {
        System.out.println("[DEBUG] ParamManager.resetForNewPipeline: resetting for new pipeline");
        typesLoadedFromJSON = false;
        paramList.clear();
    }
}
