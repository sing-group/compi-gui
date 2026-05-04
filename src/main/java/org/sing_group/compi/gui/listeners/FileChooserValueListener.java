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

import javax.swing.event.ChangeEvent;

import org.sing_group.gc4s.input.filechooser.event.FileChooserListener;

public class FileChooserValueListener implements FileChooserListener{

    private String paramName;

    public FileChooserValueListener(String paramName){
        this.paramName = paramName;
    }
    @Override
    public void onFileChoosed(ChangeEvent event) {
        //Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onFileChoosed'");
    }

    public String getParamName() {
        return paramName;
    }

}
