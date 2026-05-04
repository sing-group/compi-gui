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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.sing_group.compi.gui.gui.dialogs.ConfigureTypesDialog;
import org.sing_group.compi.gui.gui.util.JFileChooserPanelWrapper;
import org.sing_group.compi.gui.gui.util.UISettings;
import org.sing_group.compi.gui.listeners.FileChooserValueListener;
import org.sing_group.compi.gui.listeners.ParamsValueListener;
import org.sing_group.compi.gui.listeners.PipelineListener;
import org.sing_group.compi.gui.models.GuiParam;
import org.sing_group.compi.gui.models.PipelineInfoProvider;
import org.sing_group.gc4s.input.InputParameter;
import org.sing_group.gc4s.input.InputParametersPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanel;
import org.sing_group.gc4s.input.filechooser.JFileChooserPanelBuilder;
import org.sing_group.gc4s.input.filechooser.SelectionMode;
import org.sing_group.gc4s.input.text.DoubleTextField;
import org.sing_group.gc4s.input.text.JIntegerTextField;

public class ParamsConfigurationPanel extends JPanel {

	private PipelineInfoProvider pipeline;
    private List<GuiParam> paramList;
    private Map<String, String> paramValues;
    private File workingDirectory;
    private List<PipelineListener> listenersList;
    private List<JFileChooserPanelWrapper> fileChooserWrappers;

    private JButton save_values = new JButton();
    private boolean save_values_enabled = false;
    
    public ParamsConfigurationPanel(){
        setLayout(new BorderLayout());
        this.listenersList = new ArrayList<>();
        this.workingDirectory = new File("");
        this.fileChooserWrappers = new ArrayList<>();
    }

    public void createParamsConfigurationInterface(boolean checkTypes) {
        for (int i = 0; i < pipeline.listParams().size(); i++) {
            System.out.println("value of "+i+ " : "+paramValues.get(paramList.get(i).getName()));
        }
        JPanel paramsPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new FlowLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JPanel southPanel = new JPanel(new FlowLayout());

        ArrayList<InputParameter> ipList = new ArrayList<InputParameter>();
        ArrayList<InputParameter> ipListOptional = new ArrayList<InputParameter>();

        paramList = pipeline.listParams();
        
        setFlagValues(paramList);

        JXTaskPaneContainer containerColapsableNormal = new JXTaskPaneContainer();
        containerColapsableNormal.setOpaque(false);
        JXTaskPane colapsableNormal = new JXTaskPane();
        colapsableNormal.setTitle("Current Params");
        containerColapsableNormal.add(colapsableNormal);

        JXTaskPaneContainer containerColapsableOptional = new JXTaskPaneContainer();
        containerColapsableOptional.setOpaque(false);
        JXTaskPane colapsableOptional = new JXTaskPane();
        colapsableOptional.setTitle("Optional Params");
        containerColapsableOptional.add(colapsableOptional);

        InputParametersPanel parametersPanel;
       
        JButton types = new JButton("Configure Types");
        JButton load_types = new JButton("Load Types");
        JButton load_values = new JButton("Load Param Values");
        save_values = new JButton("Save Current Values");

        for(int i = 0; i<paramList.size(); i++){
            final int paramIndex = i;
            if(paramList.get(i).getType().getVal().equals("text")){
                System.out.println("typetext: " + (paramList.get(i).getType()));
                JTextField paramValue = new JTextField(paramValues.get(paramList.get(i).getName()));
                String currentValue = paramValues.get(paramList.get(i).getName());
                paramValue.setBackground(UISettings.isValid(currentValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                paramValue.addActionListener(new ParamsValueListener(paramList.get(i).getName()){
                    @Override 
                    public void actionPerformed(ActionEvent e){
                        handleTextFieldChange(this.getParamName(), paramValue);
                    }
                });
                paramValue.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        handleTextFieldChange(paramList.get(paramIndex).getName(), paramValue);
                    }
                });

                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);

            }else if(paramList.get(i).getType().getVal().equals("boolean")){
                System.out.println("typeboll: " + (paramList.get(i).getType())+" flag: "+paramList.get(i).itsFlag());
                JCheckBox paramValue = new JCheckBox();
                System.out.println("pueba para fich: "+paramValues.get(paramList.get(i).getName()));
                String currentBooleanValue = paramValues.get(paramList.get(i).getName());
                if("true".equals(currentBooleanValue)) paramValue.setSelected(true);;
                paramValue.addActionListener(new ParamsValueListener(paramList.get(i).getName()){
                @Override
                    public void actionPerformed(ActionEvent e){
                        if(paramValue.isSelected()) paramtersChanged(this.getParamName(), "true");                            
                        else paramtersChanged(this.getParamName(), "false");
                    }
                });
                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);
                
            }else if(paramList.get(i).getType().getVal().equals("decimal")){
                DoubleTextField paramValue;

                System.out.println("typedec: " + (paramList.get(i).getType()));
                System.out.println("valor typedec: "+paramValues.get(paramList.get(i).getName()));

                String currentDecValue = paramValues.get(paramList.get(i).getName());
                if(currentDecValue == null ||
                 currentDecValue.equals("")||
                 !isDouble(currentDecValue)) paramValue = new DoubleTextField();
                else paramValue = new DoubleTextField(Double.parseDouble(currentDecValue));

                paramValue.setBackground(UISettings.isValid(currentDecValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                paramValue.addActionListener(new ParamsValueListener(paramList.get(i).getName()){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        handleTextFieldChange(this.getParamName(), paramValue);
                    }
                });
                paramValue.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        handleTextFieldChange(paramList.get(paramIndex).getName(), paramValue);
                    }
                });

                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);
                
            }else if(paramList.get(i).getType().getVal().equals("integer")){
                JIntegerTextField paramValue;
                System.out.println("typeint: " + (paramList.get(i).getType()));

                String currentIntValue = paramValues.get(paramList.get(i).getName());
                if(currentIntValue == null ||
                 currentIntValue.equals("") ||
                 !isInteger(currentIntValue)) paramValue = new JIntegerTextField();
                else paramValue = new JIntegerTextField(Integer.parseInt(currentIntValue));

                paramValue.setBackground(UISettings.isValid(currentIntValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                System.out.println(paramValue.getClass());
                paramValue.addActionListener(new ParamsValueListener(paramList.get(i).getName()){
                    @Override
                    public void actionPerformed(ActionEvent e){
                        handleTextFieldChange(this.getParamName(), paramValue);
                    }
                });
                paramValue.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        handleTextFieldChange(paramList.get(paramIndex).getName(), paramValue);
                    }
                });

                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);
                
            }else if(paramList.get(i).getType().getVal().equals("file")){
                JFileChooserPanel paramValue = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
			    .withFileChooserSelectionMode(SelectionMode.FILES)
			    .build();
                JFileChooserPanelWrapper wrapper = new JFileChooserPanelWrapper(paramValue);
                fileChooserWrappers.add(wrapper);
                String currentFileValue = paramValues.get(paramList.get(i).getName());
                if (currentFileValue != null && !currentFileValue.isEmpty()) {
                    paramValue.setSelectedFile(new File(currentFileValue));
                    wrapper.setBackground(UISettings.isValid(currentFileValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                } else {
                    wrapper.setBackground(UISettings.INVALID_BACKGROUND);
                }
                paramValue.addFileChooserListener(new FileChooserValueListener(paramList.get(i).getName()){
                    @Override
                    public void onFileChoosed(ChangeEvent e){
                        if(UISettings.isValid(paramValue.getSelectedFile().getAbsolutePath())) wrapper.setBackground(UISettings.VALID_BACKGROUND);
                        else wrapper.setBackground(UISettings.INVALID_BACKGROUND);
                        paramtersChanged(this.getParamName(), paramValue.getSelectedFile().getAbsolutePath());
                    }
                });

                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);
                
            }else if(paramList.get(i).getType().getVal().equals("directory")){
                System.out.println("typedir: " + (paramList.get(i).getType()));
                JFileChooserPanel paramValue = JFileChooserPanelBuilder.createOpenJFileChooserPanel()
			    .withFileChooserSelectionMode(SelectionMode.DIRECTORIES)
			    .build();
                JFileChooserPanelWrapper wrapper = new JFileChooserPanelWrapper(paramValue);
                fileChooserWrappers.add(wrapper);
                String currentDirValue = paramValues.get(paramList.get(i).getName());
                if (currentDirValue != null && !currentDirValue.isEmpty()) {
                    paramValue.setSelectedFile(new File(currentDirValue));
                    wrapper.setBackground(UISettings.isValid(currentDirValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                } else {
                    wrapper.setBackground(UISettings.INVALID_BACKGROUND);
                }
                wrapper.setBackground(UISettings.isValid(currentDirValue) ? UISettings.VALID_BACKGROUND : UISettings.INVALID_BACKGROUND);
                paramValue.addFileChooserListener(new FileChooserValueListener(paramList.get(i).getName()){
                    @Override
                    public void onFileChoosed(ChangeEvent e){
                        if(UISettings.isValid(paramValue.getSelectedFile().getAbsolutePath())) wrapper.setBackground(UISettings.VALID_BACKGROUND);
                        else wrapper.setBackground(UISettings.INVALID_BACKGROUND);
                        paramtersChanged(this.getParamName(), paramValue.getSelectedFile().getAbsolutePath());
                    }
                });
                InputParameter ip = new InputParameter(paramList.get(i).getName(), paramValue, paramList.get(i).getDescription().getDescription());
                if(paramList.get(i).getDescription().getDefaultValue() !=null) ipListOptional.add(ip);
                else ipList.add(ip);
                
            }
        }

        parametersPanel = new InputParametersPanel(castToArray(ipList));
       
        colapsableNormal.add(parametersPanel);
        InputParametersPanel parametersPanelOptional= new InputParametersPanel(castToArray(ipListOptional));
        colapsableOptional.add(parametersPanelOptional);

        if(ipListOptional.size() == 0){
            containerColapsableOptional.remove(colapsableOptional);
        } 

        types.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
              showTypeParams();
			}
		});

        load_values.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
              System.out.println("Traer valores");
              updateParamValues();
			}
		});

        load_types.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
              System.out.println("Traer tipos");
              JFileChooser typesFile = new JFileChooser();
              typesFile.setFileSelectionMode(JFileChooser.FILES_ONLY);

              if(typesFile.showOpenDialog(ParamsConfigurationPanel.this) == JFileChooser.APPROVE_OPTION){
                  if (pipeline instanceof org.sing_group.compi.gui.models.CompiPipeline) {
                      ((org.sing_group.compi.gui.models.CompiPipeline) pipeline).loadFromFile(typesFile.getSelectedFile());
                      List<GuiParam> updatedParams = pipeline.listParams();
                      onUpdateParametersTypes(updatedParams);
                  }
               }
			}
		});
        save_values.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
              System.out.println("Guardar Valores actuales");
              saveValuesOnFile();
			}
		});

        save_values.setEnabled(save_values_enabled);

        centerPanel.add(containerColapsableNormal);
        centerPanel.add(containerColapsableOptional);
        JScrollPane intermediate = new JScrollPane(centerPanel);
        southPanel.add(types);
        southPanel.add(load_types);
        southPanel.add(load_values);
        southPanel.add(save_values);
        paramsPanel.add(northPanel, BorderLayout.NORTH);
        paramsPanel.add(intermediate, BorderLayout.CENTER);
        paramsPanel.add(southPanel, BorderLayout.SOUTH);
        this.add(paramsPanel);

    }

    public void setPipeline(PipelineInfoProvider pp){
        this.pipeline = pp;
    }

    public void setParamList(List<GuiParam> paramList) {
        this.paramList = paramList;
    }

    public void setParamValues(Map<String, String> paramVal) {
        this.paramValues = paramVal;
    }

    public void clearInterface(){
        fileChooserWrappers.clear();
        this.removeAll();
    }

    private void handleTextFieldChange(String paramName, JTextField paramValue) {
        if(UISettings.isValid(paramValue.getText())) {
            paramValue.setBackground(UISettings.VALID_BACKGROUND);
        } else {
            paramValue.setBackground(UISettings.INVALID_BACKGROUND);
        }
        paramtersChanged(paramName, paramValue.getText());
    }

    public void addListener(PipelineListener pp){
	
		this.listenersList.add(pp);
	}

    public void setWorkingDirectory(File workingDir){
        this.workingDirectory = workingDir;
    }

    public void setSaveValuesEnabled(){
        save_values.setEnabled(true);
        save_values_enabled = true;
    }

    private void updateParamValues(){

        JFileChooser valuesFile = new JFileChooser();
        valuesFile.setCurrentDirectory(workingDirectory);
        valuesFile.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if(valuesFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            getValuesFromFile(valuesFile.getSelectedFile());
              
            SwingUtilities.invokeLater(() -> {
                clearInterface();
                createParamsConfigurationInterface(false);
            });

            if(!checkValidValues()){
                System.out.println("Se pone run mal");
                onInvalidValuesConfiguration();
            }else{
                System.out.println("Se pone run BIEN");
                onValidValuesConfiguration();
            }
        }
    }

    private void showTypeParams() {
        ConfigureTypesDialog dialog = new ConfigureTypesDialog(null, paramList);
        dialog.setVisible(true);

        List<GuiParam> list = dialog.getResultList();
        if (list != null && !list.isEmpty()) {
            onUpdateParametersTypes(list);
            pipeline.updateParamList(list);

            SwingUtilities.invokeLater(() -> {
                clearInterface();
                createParamsConfigurationInterface(false);
            });
        }
    }

    private InputParameter[] castToArray(List<InputParameter> list){

        InputParameter[] salida = new InputParameter[list.size()];
        
        for (int i = 0; i < salida.length; i++) {
            salida[i] = list.get(i);
        }
        return salida;
    }

    public void paramtersChanged(String paramName, String value) {

        paramValues.replace(paramName, value);
       
        if(!checkValidValues()){
            System.out.println("Se pon run mal");
            onInvalidValuesConfiguration();
        }else{
            System.out.println("Se pon run BIEN");
            onValidValuesConfiguration();
        }

        onUpdateParametersValues(paramValues);
    }

    private boolean checkValidValues(){
        boolean salida = true;
        Iterator<String> keysIterator = paramValues.keySet().iterator();

        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            String value = paramValues.get(key);
            if(value == null || value.length()<1)salida = false;
            System.out.println("valid values " + salida);
        }
        return salida;
    }

    private void getValuesFromFile(File filePath){
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String linea;

            while((linea = br.readLine())!= null){
                sb.append(linea).append(System.lineSeparator());
            }

            System.out.println(sb.toString());
            String[] arrayString = sb.toString().split(System.lineSeparator());

            paramValues = updateValuesFromFile(arrayString);
            onUpdateParametersValues(updateValuesFromFile(arrayString));

        } catch (FileNotFoundException e) {
            //  Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            //  Auto-generated catch block
            e.printStackTrace();
        } 
    }

    private Map<String, String> updateValuesFromFile(String[] arrayString){
        
        Map<String, String> salida =  new HashMap<>();
        System.out.println("Actualizar Valores");

        for (int i = 0; i < arrayString.length; i++) {
            if(arrayString[i].split("=").length>1){
                salida.put(arrayString[i].split("=")[0], arrayString[i].split("=")[1]);
            }else salida.put(arrayString[i].split("=")[0], "true");
        }
        
        for (int i = 0; i < pipeline.listParams().size(); i++) {
            System.out.println("value of "+i+ " : "+salida.get(paramList.get(i).getName()));
        }
        return salida;
    }
    
    private void saveValuesOnFile(){

        StringBuilder salida = new StringBuilder();
        Iterator<String> it = paramValues.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            String value = paramValues.get(key);
            if ("true".equals(value) || "false".equals(value)){
                salida.append(key);
            } else {
                salida.append(key).append("=").append(value);
            }
            salida.append(System.lineSeparator());
        }

        try (FileWriter fw = new FileWriter(new File(workingDirectory, "compi.params").getAbsolutePath())) {
            fw.write(salida.toString());
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateParametersValues(Map<String, String> map) {
       for (PipelineListener listener : listenersList) {
			listener.onUpdateParametersValues(map);
		}
    }

    public void onUpdateParametersTypes(List<GuiParam> paramList) {
       for (PipelineListener listener : listenersList) {
			listener.onUpdateParametersTypes(paramList);
		}
    }

    public void onInvalidValuesConfiguration() {
        for (PipelineListener listener : listenersList) {
			listener.onInvalidValuesConfiguration();
		}
    }

    public void onValidValuesConfiguration() {
        for (PipelineListener listener : listenersList) {
			listener.onValidValuesConfiguration();
		}
    }

    private boolean isInteger(String numero){
        try{
            Integer.parseInt(numero);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    private boolean isDouble(String numero){
        try{
            Double.parseDouble(numero);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    private void setFlagValues(List<GuiParam> lista){

        for (int i = 0; i < lista.size(); i++) {
            System.out.println("valor del falg: "+paramValues.get(lista.get(i).getName()));
            if(lista.get(i).itsFlag()){
                if(paramValues.get(lista.get(i).getName()).equals("")){
                    paramValues.replace(lista.get(i).getName(), "false");
                }  
            }
        }
    }

}
