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
package org.sing_group.compi.gui.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.ui.CenteredJPanel;

public class ConfigureTypesDialog extends JDialog {

    private List<GuiParam> paramList;
    private List<JComboBox<String>> typeValues;
    private List<GuiParam> resultList;

    public ConfigureTypesDialog(JFrame parent, List<GuiParam> paramList) {
        super(parent, "Configure Types", true);
        this.paramList = new ArrayList<>(paramList);
        this.typeValues = new ArrayList<>();
        this.resultList = new ArrayList<>();
        initDialog();
    }

    private void initDialog() {
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        List<InputParameter> inputParameters = new ArrayList<>();

        for (int i = 0; i < paramList.size(); i++) {
            if (!paramList.get(i).itsFlag()) {
                JComboBox<String> comboBox = new JComboBox<String>();
                for (org.sing_group.compi.gui.models.Type val : org.sing_group.compi.gui.models.Type.values()) {
                    if (!val.getVal().equals(org.sing_group.compi.gui.models.Type.BOOLEAN.getVal())) {
                        comboBox.addItem(val.getVal());
                    }
                }
                comboBox.setSelectedItem(paramList.get(i).getType().getVal());
                typeValues.add(comboBox);

                InputParameter ip = new InputParameter(
                    paramList.get(i).getName(),
                    comboBox,
                    paramList.get(i).getDescription().getDescription()
                );
                inputParameters.add(ip);
            } else {
                JComboBox<String> comboBox = new JComboBox<String>();
                comboBox.addItem(paramList.get(i).getType().getVal());
                typeValues.add(comboBox);
            }
        }

        InputParametersPanel paramsPanel = new InputParametersPanel(
            inputParameters.toArray(new InputParameter[0])
        );

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new CenteredJPanel(paramsPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processTypes();
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultList = null;
                dispose();
            }
        });

        add(mainPanel);
    }

    private void processTypes() {
        resultList = new ArrayList<>();
        for (int i = 0; i < paramList.size(); i++) {
            if (paramList.get(i).getType().equals(typeValues.get(i).getSelectedItem().toString())) {
                resultList.add(paramList.get(i));
            } else {
                org.sing_group.compi.gui.models.Type type = org.sing_group.compi.gui.models.Type.TEXT;
                String selected = typeValues.get(i).getSelectedItem().toString();
                if (selected.equals(org.sing_group.compi.gui.models.Type.BOOLEAN.getVal())) {
                    type = org.sing_group.compi.gui.models.Type.BOOLEAN;
                } else if (selected.equals(org.sing_group.compi.gui.models.Type.INTEGER.getVal())) {
                    type = org.sing_group.compi.gui.models.Type.INTEGER;
                } else if (selected.equals(org.sing_group.compi.gui.models.Type.DECIMAL.getVal())) {
                    type = org.sing_group.compi.gui.models.Type.DECIMAL;
                } else if (selected.equals(org.sing_group.compi.gui.models.Type.FILE.getVal())) {
                    type = org.sing_group.compi.gui.models.Type.FILE;
                } else if (selected.equals(org.sing_group.compi.gui.models.Type.DIRECTORY.getVal())) {
                    type = org.sing_group.compi.gui.models.Type.DIRECTORY;
                }
                GuiParam newGuiParam = new GuiParam(paramList.get(i).getName(), type, paramList.get(i).getDescription());
                if (type == org.sing_group.compi.gui.models.Type.BOOLEAN) {
                    newGuiParam.setFlag(true);
                }
                resultList.add(newGuiParam);
            }
        }
    }

    public List<GuiParam> getResultList() {
        return resultList;
    }
}
