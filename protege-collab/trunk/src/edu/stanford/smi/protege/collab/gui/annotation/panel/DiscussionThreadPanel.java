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
			
		//Collection annotationRoots = ChangeOntologyUtil.getDiscussionThreadAnnotations(getCurrentInstance().getKnowledgeBase());
		
		Collection<Annotation> annotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(getCurrentInstance());
			
		getAnnotationsTree().setRoot(new AnnotationsTreeRoot(annotationsRoots));
		
		getAnnotationsTree().setSelectionRow(0);
		
	}

	@Override
	protected void onCreateAnnotation() {
		AnnotationCls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
				
		Annotation annotInstance = ChangeOntologyUtil.createAnnotationOnAnnotation(getCurrentInstance().getKnowledgeBase(), getCurrentInstance(), pickedAnnotationCls);
			
		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent(); 
		if (selectedNode != null) {
			selectedNode.childAdded(annotInstance);
		}
		ComponentUtilities.extendSelection(getAnnotationsTree(), annotInstance);
	}
	
	

}
