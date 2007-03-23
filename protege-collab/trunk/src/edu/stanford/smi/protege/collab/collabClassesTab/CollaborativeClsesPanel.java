package edu.stanford.smi.protege.collab.collabClassesTab;

import javax.swing.Action;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.ui.SubclassPane;
import edu.stanford.smi.protege.util.AllowableAction;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class CollaborativeClsesPanel extends ClsesPanel {

	private AllowableAction viewAnnotationsAction;


	public CollaborativeClsesPanel(Project project, AllowableAction viewAnnotationsAction) {
		super(project);
		
		this.viewAnnotationsAction = viewAnnotationsAction;
	}

	
	@Override
	protected SubclassPane createSubclassPane(Action viewAction, Cls root, Action createAction, Action action) {		
		CollaborativeSubclassPane subclassPane = new CollaborativeSubclassPane(viewAction, root, createAction, action, viewAnnotationsAction);
		subclassPane.setViewAnnotationsAction(viewAnnotationsAction);
		
		return subclassPane;
	}


	public AllowableAction getViewAnnotationsAction() {
		return viewAnnotationsAction;
	}


	//strange!
	public void setViewAnnotationsAction(AllowableAction viewAnnotationsAction) {
		this.viewAnnotationsAction = viewAnnotationsAction;
		((CollaborativeSubclassPane)_subclassPane).setViewAnnotationsAction(viewAnnotationsAction);
	}


	
}
