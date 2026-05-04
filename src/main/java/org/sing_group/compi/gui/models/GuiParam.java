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

import org.sing_group.compi.core.pipeline.ParameterDescription;

public class GuiParam {

    private String name;
    private ParameterDescription description;
    private Type type;
    private String value;
    private boolean flag;

    public GuiParam(String name, Type type, ParameterDescription description){
        this.name = name;
        this.type = type;
        this.description = description;
        this.value = "";
        this.flag = false;
    }

    public GuiParam(String name, Type type, ParameterDescription description, String value){
        this.name = name;
        this.type = type;
        this.description = description;
        this.value = value;
        this.flag = false;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean itsFlag(){
        return flag;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setType(Type type){
        this.type = type;
    }

    public void setDescription(ParameterDescription description){
        this.description = description; 
    }

    public String getName(){
        return this.name;
    }

    public Type getType(){
        return this.type;
    }

    public ParameterDescription getDescription(){
        return this.description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
