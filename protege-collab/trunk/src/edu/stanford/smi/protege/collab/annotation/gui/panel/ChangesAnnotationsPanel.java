package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
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
						
		Collection<Change> filteredRoots = getTreeFilter() == null ? annotationsRoots : getTreeFilter().getFilteredCollection(annotationsRoots);
	
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, getTreeFilter());
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
