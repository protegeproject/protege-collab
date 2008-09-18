package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AllAnnotationsPanel extends AnnotationsTabPanel {

	public AllAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "All (Ann. & Chg.)");
	}


	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getLabeledComponent().setHeaderLabel("Annotations and Changes (nothing selected)");
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		getLabeledComponent().setHeaderLabel("Annotations and Changes on " + getCurrentInstance().getBrowserText());

		Collection<Change> changeAnnotationsRoots = ChAOUtil.getTopLevelChanges(getCurrentInstance());
		Collection<Annotation> ontologyCompAnnotationsRoots = ChAOUtil.getTopLevelAnnotationInstances(getCurrentInstance());

		Collection<AnnotatableThing> annotationsRoots = new ArrayList<AnnotatableThing>();
		annotationsRoots.addAll(changeAnnotationsRoots);
		annotationsRoots.addAll(ontologyCompAnnotationsRoots);

		Collection filteredRoots = ChAOUtil.getFilteredCollection(annotationsRoots, getTreeFilter());

		//hack, reimplement later
		TreeFilter filter = getTreeFilter();

		if (filter != null) {
			filter = new UnsatisfiableFilter();
		}
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, filter);

		getAnnotationsTree().setRoot(root);
	}

	@Override
	protected void onCreateAnnotation() {
		//TODO: Implement this.
		Log.getLogger().info("Functionality not implemented yet");
	}

	@Override
	public Icon getIcon() {
		//return AnnotationsIcons.getOntologyAnnotationAndChangeIcon();
		return null;
	}


}
