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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class OnDoubleClickCopyMouseAdapter extends MouseAdapter {
    
    private final boolean provideFeedback;
    
    public OnDoubleClickCopyMouseAdapter() {
        this(true);
    }
    
    public OnDoubleClickCopyMouseAdapter(boolean provideFeedback) {
        this.provideFeedback = provideFeedback;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            Component component = e.getComponent();
            
            if (component instanceof JTextComponent) {
                JTextComponent textComponent = (JTextComponent) component;
                String text = textComponent.getText();
                
                if (text != null && !text.isEmpty()) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection selection = new StringSelection(text);
                    clipboard.setContents(selection, null);
                    
                    if (provideFeedback) {
                        Color originalColor = textComponent.getBackground();
                        textComponent.setBackground(Color.LIGHT_GRAY);
                        
                        new Thread(() -> {
                            try {
                                Thread.sleep(200);
                                SwingUtilities.invokeLater(() -> {
                                    textComponent.setBackground(originalColor);
                                });
                            } catch (InterruptedException ex) {
                            }
                        }).start();
                    }
                }
            }
        }
    }
}
