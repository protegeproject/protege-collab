package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeRoot;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OntologyComponentAnnotationsPanel extends AnnotationsTabPanel {

	public OntologyComponentAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Annotations");
	}


	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getLabeledComponent().setHeaderLabel("Annotations (nothing selected)");
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		getLabeledComponent().setHeaderLabel("Annotations on " + getCurrentInstance().getBrowserText());
		Collection<Annotation> annotationsRoots = ChAOUtil.getTopLevelAnnotationInstances(getCurrentInstance());
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

		Collection clsTreeSelection = UIUtil.getClsTreeSelection(ProjectManager.getProjectManager().getCurrentProjectView().getSelectedTab());
		if (clsTreeSelection == null || clsTreeSelection.size() == 0) {
			return;
		}

		Object firstSelection = CollectionUtilities.getFirstItem(clsTreeSelection);
		String applyToValue = "";

		if (!(firstSelection instanceof Frame)) {
			return;
		}

		KnowledgeBase kb = getCurrentInstance().getKnowledgeBase();

		Annotation annotation = OntologyJavaMappingUtil.createObject(ChAOKbManager.getChAOKb(kb), null, pickedAnnotationCls.getName(), Annotation.class);
		ChAOUtil.fillAnnotationSystemFields(kb, annotation);
		annotation.setBody(AnnotationsTabPanel.NEW_ANNOTATION_DEFAULT_BODY_TEXT);

		if (firstSelection instanceof AnnotatableThing) {
			AnnotatableThing annotatableThing = (AnnotatableThing) firstSelection;
			Collection<Annotation> annotations = new ArrayList<Annotation>(annotatableThing.getAssociatedAnnotations());

			annotations.add(annotation);

			annotatableThing.setAssociatedAnnotations(annotations);
		} else {
			Frame selectedFrame = (Frame) firstSelection;
			Ontology_Component ontoComp = ChAOUtil.getOntologyComponent(selectedFrame, true);

			Collection<AnnotatableThing> ontologyComponents = new ArrayList<AnnotatableThing>(annotation.getAnnotates());
			ontologyComponents.add(ontoComp);
			annotation.setAnnotates(ontologyComponents);
		}

		Instance annotInst = ((AbstractWrappedInstance)annotation).getWrappedProtegeInstance();
		InstanceDisplay instDispl = new InstanceDisplay(getChaoKb().getProject(), false, true);
		instDispl.setInstance(annotInst);

		Object[] options = {"Post", "Cancel"};
		int ret = JOptionPane.showOptionDialog(this, instDispl, "New annotation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				AnnotationsIcons.getMailIcon(),
				options,
				options[0]);

		if (ret == JOptionPane.OK_OPTION) {
			refreshDisplay();
			((LazyTreeRoot)getAnnotationsTree().getModel().getRoot()).reload();
			ComponentUtilities.setSelectedObjectPath(getAnnotationsTree(), CollectionUtilities.createCollection(annotation));
		} else {
			annotInst.delete();
		}
	}


	@Override
	public Icon getIcon() {
		return AnnotationsIcons.getOntologyAnnotationIcon();
	}
}
