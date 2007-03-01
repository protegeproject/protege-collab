package edu.stanford.smi.protege.collab.gui.annotation.panel;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.gui.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.server_changes.model.Model;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AllAnnotationsPanel extends AnnotationsTabPanel {
	
	public AllAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "All");
	}
	

	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
		
		Cls changesCls = ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase()).getCls(Model.CLS_NAME_CHANGE);
		
		Collection<Frame> changesAnnotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase()), getCurrentInstance().getName(), changesCls);
			
		Collection<Frame> ontologyCompAnnotationsRoots = ChangeOntologyUtil.getTopLevelOntologyComponentAnnotations(getCurrentInstance().getKnowledgeBase(), getCurrentInstance().getName());
		
		//Collection<Frame> annotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase()), getCurrentInstance().getName());
		
		Collection<Frame> annotationsRoots = new ArrayList<Frame>();
		annotationsRoots.addAll(changesAnnotationsRoots);
		annotationsRoots.addAll(ontologyCompAnnotationsRoots);
		
		getAnnotationsTree().setRoot(new AnnotationsTreeRoot(annotationsRoots));
		
		getAnnotationsTree().setSelectionRow(0);
		
		repaint();
	}	
	
	@Override
	protected void onCreateAnnotation() {
		System.out.println("Create");
		
	}
	
	
}
