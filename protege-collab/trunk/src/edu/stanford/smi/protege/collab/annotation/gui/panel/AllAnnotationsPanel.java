package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AllAnnotationsPanel extends AbstractAnnotationsTabPanel {
	private static final long serialVersionUID = -7940956103461357906L;
	
	public AllAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "All notes");
		setLabel("All entity and ontology notes");
	}
	
	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {			
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		Collection<AnnotatableThing> annotationsRoots = new ArrayList<AnnotatableThing>();
		annotationsRoots.addAll(getOntologyAnnotationsCache().getTopOntologyAnnotations());
		annotationsRoots.addAll(ChAOUtil.getOntologyComponentsWithAnnotations(getKnowledgeBase()));
		
		Collection<Annotation> filteredRoots = (Collection<Annotation>) ChAOUtil.getFilteredTopLevelNode(getChaoKb(), annotationsRoots, getTreeFilter());
			
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, getTreeFilter());
		getAnnotationsTree().setRoot(root);		
	}

	@Override
	protected void onCreateAnnotation() {
		//code copied from OntologyAnnotationsPanel
		Cls pickedAnnotationCls = getSelectedAnnotationType();
		if (pickedAnnotationCls == null) {
			return;
		}

		Annotation annot = ChAOUtil.createAnnotationOnAnnotation(getKnowledgeBase(), null, pickedAnnotationCls);
		ChAOUtil.fillAnnotationSystemFields(getKnowledgeBase(), annot);
		annot.setBody(AbstractAnnotationsTabPanel.NEW_ANNOTATION_DEFAULT_BODY_TEXT);

		InstanceDisplay instDispl = UIUtil.createAnnotationInstanceDisplay(annot, ChAOKbManager.getChAOKb(getKnowledgeBase()));
		Instance annotInst = ((AbstractWrappedInstance)annot).getWrappedProtegeInstance();
		Object[] options = {"Post", "Cancel"};
		int ret = JOptionPane.showOptionDialog(this, instDispl, "New discussion thread ("+
				annotInst.getDirectType().getBrowserText() + ")",
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
