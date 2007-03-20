package edu.stanford.smi.protege.collab.gui.annotation.panel;

import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.gui.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangesAnnotationsPanel extends AnnotationsTabPanel {
	
	public ChangesAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Changes");
		
		//just a guess
		getLabeledComponent().removeHeaderButton(1);
	}
	

	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
			
		Collection<Change> annotationsRoots = ChangeOntologyUtil.getTopLevelChangeInstances(getCurrentInstance());
				
		getAnnotationsTree().setRoot(new AnnotationsTreeRoot(annotationsRoots));		
		getAnnotationsTree().setSelectionRow(0);		
		repaint();
	}
	
	@Override
	protected void onCreateAnnotation() {
		AnnotationCls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}

		Collection selection = getAnnotationsTree().getSelection();
		
		Frame parentAnnotation = null;
		
		if (selection != null && selection.size() > 0) {
			parentAnnotation = (Frame) CollectionUtilities.getFirstItem(selection);
		}
		
		Annotation annotInstance = ChangeOntologyUtil.createAnnotationOnAnnotation(getCurrentInstance().getKnowledgeBase(), parentAnnotation, pickedAnnotationCls);
			
		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();        
		selectedNode.childAdded(annotInstance);		
		ComponentUtilities.extendSelection(getAnnotationsTree(), annotInstance);
			
	}
		
}
