package edu.stanford.smi.protege.collab.annotation.tree.gui;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class StringFilterComponent implements FilterValueComponent {
		
	private JTextField filterValueTextField = new JTextField();
	
	public StringFilterComponent() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.collab.annotation.tree.gui.FilterValueComponent#getValueComponent()
	 */
	public JComponent getValueComponent() {
		return filterValueTextField;
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.collab.annotation.tree.gui.FilterValueComponent#getValue()
	 */
	public Object getValue() {
		String value = filterValueTextField.getText(); 
		value = value.trim();		
		return (value == null ? null : (value.length() == 0) ? null : "*" + value + "*");
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.smi.protege.collab.annotation.tree.gui.FilterValueComponent#setValue(java.lang.String)
	 */
	public void setValue(Object value) {
		if (value == null) {
			value = new String();
		}
		if (value instanceof String) {
			filterValueTextField.setText((String) value);
		}
	}

}
