package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.text.TabExpander;

import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeModel;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OntologyComponentAnnotationsPanel extends AnnotationsTabPanel {
		
	public OntologyComponentAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Ontology Components (OC)");
	}	


	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
		
		Collection<Annotation> annotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(getCurrentInstance());
		
		Collection filteredRoots = ChangeOntologyUtil.getFilteredCollection(annotationsRoots, getTreeFilter());
		
		//hack, reimplement later
		TreeFilter filter = getTreeFilter();
		
		if (filter != null) {
			filter = new UnsatisfiableFilter();
		}
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, filter);
		
		getAnnotationsTree().setRoot(root);
		
		root.reload();
		//((LazyTreeModel)getAnnotationsTree().getModel()).reload();
		getAnnotationsTree().setSelectionRow(0);
	}	
	
	@Override
	protected void onCreateAnnotation() {
		AnnotationCls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
		
		Collection clsTreeSelection = getClsTreeSelection();
		
		if (clsTreeSelection == null || clsTreeSelection.size() == 0) {
			return;
		}
		
		Object firstSelection = CollectionUtilities.getFirstItem(clsTreeSelection);
		String applyToValue = "";
						
		if (!(firstSelection instanceof Frame)) {
			return;
		}
		
		Annotation annotation = (Annotation) ChangeOntologyUtil.getChangeModel(getCurrentInstance().getKnowledgeBase()).createInstance(pickedAnnotationCls);
		annotation.setBody("(Enter the annotation text here)");

		if (firstSelection instanceof AnnotatableThing) {
			AnnotatableThing annotatableThing = (AnnotatableThing) firstSelection;
			Collection<Annotation> annotations = new ArrayList<Annotation>(annotatableThing.getAssociatedAnnotations());

			annotations.add(annotation);				

			annotatableThing.setAssociatedAnnotations(annotations);
		} else {
			Frame selectedFrame = (Frame) firstSelection;
			Ontology_Component ontoComp = ChangeOntologyUtil.getOntologyComponent(selectedFrame, true);
			
			Collection<Ontology_Component> ontologyComponents = new ArrayList<Ontology_Component>(annotation.getAnnotates());
			ontologyComponents.add(ontoComp);
			annotation.setAnnotates(ontologyComponents);
		}
		
		refreshDisplay();
		((LazyTreeRoot)getAnnotationsTree().getModel().getRoot()).reload();		
		ComponentUtilities.setSelectedObjectPath(getAnnotationsTree(), CollectionUtilities.createCollection(annotation));
		
	}
	
	
	private Collection getClsTreeSelection() {
		ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
		
		//just for the init
		TabWidget tabWidget = view.getSelectedTab();
		if (tabWidget != null && tabWidget instanceof AbstractTabWidget) {					
			if (tabWidget instanceof InstancesTab) {
				return ((InstancesTab)tabWidget).getSelectedInstances();
			}
			
			Selectable selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
			return (selectable == null ? null : selectable.getSelection());
		}
		
		return null;		
	}	
}
