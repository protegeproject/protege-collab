package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import javax.swing.Icon;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;

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


	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getLabeledComponent().setHeaderLabel("Changes (nothing selected)");
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		getLabeledComponent().setHeaderLabel("Changes for " + getCurrentInstance().getBrowserText());
		Collection<Change> annotationsRoots = ChAOUtil.getTopLevelChanges(getCurrentInstance());
		Collection<? extends AnnotatableThing> filteredRoots = ChAOUtil.getFilteredCollection(annotationsRoots, getTreeFilter());

		//hack, reimplement later
		TreeFilter<AnnotatableThing> filter = getTreeFilter();
		if (filter != null) {
			filter = new UnsatisfiableFilter();
		}
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, filter);
		getAnnotationsTree().setRoot(root);
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

		KnowledgeBase kb = getCurrentInstance().getKnowledgeBase();

		Annotation annotInstance = ChAOUtil.createAnnotationOnAnnotation(kb, parentAnnotation, pickedAnnotationCls);
		ChAOUtil.fillAnnotationSystemFields(kb, annotInstance);

		annotInstance.setBody("(Enter the annotation text here)");

		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();
		selectedNode.childAdded(annotInstance);
		ComponentUtilities.extendSelection(getAnnotationsTree(), annotInstance);

	}

	@Override
	public Icon getIcon() {
		return AnnotationsIcons.getChangeAnnotationIcon();
	}

}
