package edu.stanford.smi.protege.collab.gui.annotation.panel;

import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.gui.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class DiscussionThreadPanel extends AnnotationsTabPanel {
		
	public DiscussionThreadPanel(KnowledgeBase kb) {
		super(kb, "Discussion Threads");
	}

	
	
	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
	
		//KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase());
		
		//Cls discussionThreadCls = changesKb.getCls(ChangeOntologyUtil.CLS_NAME_DISCUSSION_THREAD);
		//check why this is invoked twice
		//Collection<Frame> annotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(changesKb, null, discussionThreadCls, false);
			
		Collection annotationRoots = ChangeOntologyUtil.getDiscussionThreadAnnotations(getCurrentInstance().getKnowledgeBase());
			
		getAnnotationsTree().setRoot(new AnnotationsTreeRoot(annotationRoots));
		
		getAnnotationsTree().setSelectionRow(0);
		
	}

	@Override
	protected void onCreateAnnotation() {
		Cls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
									
		Collection selection = getAnnotationsTree().getSelection();
		
		Frame parentAnnotation = null;
		
		if (selection != null && selection.size() > 0) {
			parentAnnotation = (Frame) CollectionUtilities.getFirstItem(selection);
		}
		
		Slot associatedAnnotation = ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase()).getSlot(ChangeCreateUtil.SLOT_NAME_ASSOC_ANNOTATIONS);
		
		Instance instDiscussionThread = pickedAnnotationCls.createDirectInstance(null);
		
		if (parentAnnotation != null) {
			parentAnnotation.addOwnSlotValue(associatedAnnotation, instDiscussionThread);
		}

		refreshDisplay();
		
		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();        
		selectedNode.childAdded(instDiscussionThread);		
		ComponentUtilities.extendSelection(getAnnotationsTree(), instDiscussionThread);
	}
	
	

}
