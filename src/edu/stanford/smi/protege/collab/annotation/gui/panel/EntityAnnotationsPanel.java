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
 * The entity notes panel that shows the notes attached to ontology
 * components (e.g. classes, properties or individuals). 
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class EntityAnnotationsPanel extends AbstractAnnotationsTabPanel {
	private static final long serialVersionUID = -5672164068804094696L;

	public EntityAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Entity notes");
	}


	@Override
	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			setLabel("Notes (nothing selected)");
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}

		setLabel("Notes for " + getCurrentInstance().getBrowserText());
		
		Collection<Annotation> filteredRoots = (Collection<Annotation>) ChAOUtil.getFilteredTopLevelNode(getCurrentInstance(), getTreeFilter());
		
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, getTreeFilter());
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
		annotation.setBody(AbstractAnnotationsTabPanel.NEW_ANNOTATION_DEFAULT_BODY_TEXT);
		
		InstanceDisplay instDispl = UIUtil.createAnnotationInstanceDisplay(annotation, ChAOKbManager.getChAOKb(kb));

		Instance annotInst = ((AbstractWrappedInstance)annotation).getWrappedProtegeInstance();

		Object[] options = {"Post", "Cancel"};
		int ret = JOptionPane.showOptionDialog(this, instDispl, "New " +
				annotInst.getDirectType().getBrowserText(),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				AnnotationsIcons.getCommentIcon(),
				options,
				options[0]);

		if (ret == JOptionPane.OK_OPTION) {
			
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
			
			refreshDisplay();
			((LazyTreeRoot)getAnnotationsTree().getModel().getRoot()).reload();
			ComponentUtilities.setSelectedObjectPath(getAnnotationsTree(), CollectionUtilities.createCollection(annotation));
		} else {
			annotInst.delete();
		}
	}


	@Override
	public Icon getIcon() {
		return AnnotationsIcons.getCommentIcon();
	}
}
