package edu.stanford.smi.protege.collab.annotation.tree.gui;

import javax.swing.JComponent;

public interface FilterValueComponent {

	public abstract JComponent getValueComponent();

	public abstract Object getValue();

	public abstract void setValue(Object value);

}