package edu.stanford.smi.protege.collab.collabClassesTab;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.ui.SubclassPane;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class CollaborativeSubclassPane extends SubclassPane {
	
	Action viewAnnotationsAction;


	public CollaborativeSubclassPane(Action doubleClickAction, Cls rootCls, Action createCls, Action deleteCls, Action viewAnnotationsAction) {
		super(doubleClickAction, rootCls, createCls, deleteCls);
				
		this.viewAnnotationsAction = viewAnnotationsAction;
	}


	@Override
	protected JPopupMenu createPopupMenu() {	
		JPopupMenu popupMenu = super.createPopupMenu();
		
		popupMenu.insert(viewAnnotationsAction, 0);
	
		return popupMenu;
	}


	public Action getViewAnnotationsAction() {
		return viewAnnotationsAction;
	}


	public void setViewAnnotationsAction(Action viewAnnotationsAction) {
		this.viewAnnotationsAction = viewAnnotationsAction;
	}
	
	
}
