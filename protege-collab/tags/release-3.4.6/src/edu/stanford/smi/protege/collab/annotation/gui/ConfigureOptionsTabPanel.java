package edu.stanford.smi.protege.collab.annotation.gui;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.AbstractValidatableComponent;
import edu.stanford.smi.protege.util.ComponentFactory;

public class ConfigureOptionsTabPanel extends AbstractValidatableComponent{
		
	private static final long serialVersionUID = 1721210807464280686L;

	public static final String HIDE_ARCHIVED = "collab.hide.archived";
	
	private Project project;
	private JCheckBox hideArchivedCheckBox;
	
	public ConfigureOptionsTabPanel(Project project) {
		this.project = project;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		hideArchivedCheckBox = ComponentFactory.createCheckBox("Hide archived notes");
		hideArchivedCheckBox.setSelected(StatusComboBoxUtil.getHideArchived(project));
		
		add(hideArchivedCheckBox);		
	}

	public void saveContents() {
		StatusComboBoxUtil.setHideArchived(project, hideArchivedCheckBox.isSelected());		
	}

	public boolean validateContents() {
		return true;
	}
    
}
