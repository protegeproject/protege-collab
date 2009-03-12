package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangesAnnotationsPanel extends AbstractAnnotationsTabPanel {	
	private static final long serialVersionUID = -6242832743918390294L;

	public ChangesAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Changes");		
	}
	
	@Override
	protected JButton createNewThreadButton() {
		return null;
	}


	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			setLabel("Change history (nothing selected)");
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		setLabel("Change history for " + getCurrentInstance().getBrowserText());
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
	}

	@Override
	public Icon getIcon() {
		return AnnotationsIcons.getChangeAnnotationIcon();
	}

}
