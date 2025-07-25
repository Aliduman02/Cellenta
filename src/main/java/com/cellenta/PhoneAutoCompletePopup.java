package com.cellenta;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class PhoneAutoCompletePopup extends JWindow {
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private Consumer<String> onSuggestionSelected;
    
    public PhoneAutoCompletePopup(Consumer<String> onSuggestionSelected) {
        this.onSuggestionSelected = onSuggestionSelected;
        initializeComponents();
    }
    
    private void initializeComponents() {
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        suggestionList.setBackground(Color.WHITE);
        suggestionList.setForeground(new Color(17, 24, 39));
        suggestionList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (isSelected) {
                    setBackground(new Color(59, 130, 246, 30));
                    setForeground(new Color(17, 24, 39));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(75, 85, 99));
                }
                
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                setOpaque(true);
                
                return this;
            }
        });
        
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    String selectedValue = suggestionList.getSelectedValue();
                    if (selectedValue != null && onSuggestionSelected != null) {
                        onSuggestionSelected.accept(selectedValue);
                        setVisible(false);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        getContentPane().add(scrollPane);
        setFocusable(false);
    }
    
    public void updateSuggestions(List<String> suggestions) {
        listModel.clear();
        
        if (suggestions != null && !suggestions.isEmpty()) {
            for (String suggestion : suggestions) {
                if (suggestion != null && !suggestion.trim().isEmpty()) {
                    listModel.addElement(suggestion);
                }
            }
            
            if (listModel.getSize() > 0) {
                suggestionList.setSelectedIndex(0);
                
                int itemHeight = 40;
                int maxHeight = Math.min(suggestions.size() * itemHeight, 200);
                
                setSize(250, maxHeight);
                setVisible(true);
            } else {
                setVisible(false);
            }
        } else {
            setVisible(false);
        }
    }
    
    public void selectNext() {
        int currentIndex = suggestionList.getSelectedIndex();
        int nextIndex = (currentIndex + 1) % listModel.getSize();
        suggestionList.setSelectedIndex(nextIndex);
        suggestionList.ensureIndexIsVisible(nextIndex);
    }
    
    public void selectPrevious() {
        int currentIndex = suggestionList.getSelectedIndex();
        int prevIndex = currentIndex <= 0 ? listModel.getSize() - 1 : currentIndex - 1;
        suggestionList.setSelectedIndex(prevIndex);
        suggestionList.ensureIndexIsVisible(prevIndex);
    }
    
    public String getSelectedSuggestion() {
        return suggestionList.getSelectedValue();
    }
    
    public boolean hasSuggestions() {
        return listModel.getSize() > 0;
    }
}