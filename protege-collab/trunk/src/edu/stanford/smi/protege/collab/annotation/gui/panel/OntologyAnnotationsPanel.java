package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.OntologyAnnotationsCache;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OntologyAnnotationsPanel extends AbstractAnnotationsTabPanel {	
	private static final long serialVersionUID = 3435086007615287847L;
	
	private OntologyAnnotationsCache ontologyAnnotationsCache;

	public OntologyAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Ontology notes");
		setLabel("Notes on the ontology itself");
		ontologyAnnotationsCache = new OntologyAnnotationsCache(ChAOKbManager.getChAOKb(kb));
	}

	@Override
	public void refreshDisplay() {
		Collection<Annotation> annotationsRoots = ontologyAnnotationsCache.getTopOntologyAnnotations();
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

	@Override
	public void dispose() {
		ontologyAnnotationsCache.dispose();
		super.dispose();
	}


}
