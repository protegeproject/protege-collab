package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;


public class ConfigureCollabProtegeAction extends AbstractAction {
	
	private AnnotationsDisplayComponent annotationsDisplayComponent;
	
	public ConfigureCollabProtegeAction(AnnotationsDisplayComponent annotDispComp) {
		super("Configure", OWLIcons.getPreferencesIcon());
		this.annotationsDisplayComponent = annotDispComp;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		ConfigureCollabProtegePanel configPanel = new ConfigureCollabProtegePanel(annotationsDisplayComponent.getKnowledgeBase().getProject());
		int sel = ModalDialog.showDialog(annotationsDisplayComponent, configPanel, "Configure Collaborative Protege", ModalDialog.MODE_OK_CANCEL);
		
		if (sel == ModalDialog.OPTION_OK) {
			annotationsDisplayComponent.reloadCollabTabs();
		}		
	}

}
