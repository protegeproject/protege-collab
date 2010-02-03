package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.stanford.smi.protege.collab.projectPlugin.ProtegeCollabGUIProjectPlugin;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ModalDialog;


public class ConfigureCollabProtegeAction extends AbstractAction {	
	private static final long serialVersionUID = 4809014681677159624L;
	
	private KnowledgeBase kb;
	private ProtegeCollabGUIProjectPlugin collabPlugin;
	
	public ConfigureCollabProtegeAction(KnowledgeBase kb, ProtegeCollabGUIProjectPlugin collabPlugin) {
		super("Configure...");
		this.kb = kb;
		this.collabPlugin = collabPlugin;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		boolean oldHideArchived = StatusComboBoxUtil.getHideArchived(kb.getProject());
		
		ConfigureCollabProtegePanel configPanel = new ConfigureCollabProtegePanel(kb.getProject());
		int sel = ModalDialog.showDialog(ProjectManager.getProjectManager().getMainPanel(), 
				configPanel, "Configure Collaborative Protege", ModalDialog.MODE_OK_CANCEL);
		
		AnnotationsDisplayComponent annotationsDisplayComponent = collabPlugin.getAnnotationsDisplayComponent();
		if (sel == ModalDialog.OPTION_OK && annotationsDisplayComponent != null) {
			annotationsDisplayComponent.reloadCollabTabs();
		}
		
		boolean newHideArchived = StatusComboBoxUtil.getHideArchived(kb.getProject());
		if (oldHideArchived != newHideArchived) {
			HasAnnotationCache.fillHasAnnotationCache(kb);
			ProjectManager.getProjectManager().getCurrentProjectView().revalidate();
		}
	}

}
