package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AllAnnotationsPanel extends AnnotationsTabPanel {
	
	public AllAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "All (C & OC)");
	}
	

	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
		
		Collection<Change> changeAnnotationsRoots = ChangeOntologyUtil.getTopLevelChangeInstances(getCurrentInstance());
		Collection<Annotation> ontologyCompAnnotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(getCurrentInstance());
		
		Collection annotationsRoots = new ArrayList();
		annotationsRoots.addAll(changeAnnotationsRoots);
		annotationsRoots.addAll(ontologyCompAnnotationsRoots);
		
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
		System.out.println("Create");
		
	}
	
	
}
