package com.doggyzhang.plugin.widget;


import javax.swing.*;
import java.awt.*;
 
public class ModuleJCheckBox<ModuleData> extends JCheckBox implements ListCellRenderer<ModuleData> {
	public ModuleJCheckBox() {
		super();
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ModuleData> list, ModuleData value, int index, boolean isSelected, boolean cellHasFocus) {
		this.setText(value.toString());
		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		this.setSelected(isSelected);
		return this;
	}

	@Override
	protected void fireStateChanged() {
		super.fireStateChanged();
	}
}