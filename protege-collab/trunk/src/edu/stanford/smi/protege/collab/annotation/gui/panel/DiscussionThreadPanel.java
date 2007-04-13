package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
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
	
		Collection<Annotation> annotationsRoots = ChangeOntologyUtil.getTopLevelDiscussionThreads(getCurrentInstance().getKnowledgeBase());
		
		Collection filteredRoots = ChangeOntologyUtil.getFilteredCollection(annotationsRoots, getTreeFilter());
		
		//hack, reimplement later
		TreeFilter filter = getTreeFilter();
		
		if (filter != null) {
			filter = new UnsatisfiableFilter();
		}
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, filter);
		
		getAnnotationsTree().setRoot(root);
		
		root.reload();
		
		getAnnotationsTree().setSelectionRow(0);
		
	}

	@Override
	protected void onCreateAnnotation() {
		AnnotationCls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
				
		Annotation annotInstance = ChangeOntologyUtil.createAnnotationOnAnnotation(getCurrentInstance().getKnowledgeBase(), null, pickedAnnotationCls);
		annotInstance.setBody("(Enter the annotation text here)");
		
		refreshDisplay();
	}
	
	

}
