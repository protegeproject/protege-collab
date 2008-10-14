package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;

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
		Collection<Annotation> annotationsRoots = ChAOUtil.getTopLevelDiscussionThreads(getKnowledgeBase());
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
		Cls pickedAnnotationCls = getSelectedAnnotationType();
		if (pickedAnnotationCls == null) {
			return;
		}

		Annotation annot = ChAOUtil.createAnnotationOnAnnotation(getKnowledgeBase(), null, pickedAnnotationCls);
		ChAOUtil.fillAnnotationSystemFields(getKnowledgeBase(), annot);
		annot.setBody(AnnotationsTabPanel.NEW_ANNOTATION_DEFAULT_BODY_TEXT);

		Instance annotInst = ((AbstractWrappedInstance)annot).getWrappedProtegeInstance();
		InstanceDisplay instDispl = new InstanceDisplay(getChaoKb().getProject(), false, true);
		instDispl.setInstance(annotInst);

		Object[] options = {"Post", "Cancel"};
		int ret = JOptionPane.showOptionDialog(this, instDispl, "Create new discussion thread",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				AnnotationsIcons.getMailIcon(),
				options,
				options[0]);

		if (ret == JOptionPane.OK_OPTION) {
			refreshDisplay();
		} else {
			annotInst.delete();
		}
	}

	@Override
	public Icon getIcon() {
		return AnnotationsIcons.getCommentIcon();
	}



}
