package edu.stanford.smi.protege.collab.gui.annotation.panel;

import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.gui.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.gui.collabClassesTab.CollaborativeClsesTab;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class OntologyComponentAnnotationsPanel extends AnnotationsTabPanel {
	//private AllowableAction replyAction;
	
	public OntologyComponentAnnotationsPanel(KnowledgeBase kb) {
		super(kb, "Ontology Components");
	}	


	public void refreshDisplay() {
		if (getCurrentInstance() == null) {
			getAnnotationsTree().setRoot(null);
			repaint();
			return;
		}
		
		Cls ontologyCompCls = ChangeOntologyUtil.getChangesKB(getCurrentInstance().getKnowledgeBase()).getCls(ChangeOntologyUtil.CLS_NAME_ONTOLOGY_COMPONENT);
		
		Collection<Frame> annotationsRoots = ChangeOntologyUtil.getTopLevelOntologyComponentAnnotations(getCurrentInstance().getKnowledgeBase(), getCurrentInstance().getName());
		
		getAnnotationsTree().setRoot(new AnnotationsTreeRoot(annotationsRoots));
		
	}	
	
	@Override
	protected void onCreateAnnotation() {
		Cls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
		
		Collection clsTreeSelection = getClsTreeSelection();
		
		if (clsTreeSelection == null || clsTreeSelection.size() == 0) {
			return;
		}
		
		Object firstSelection = CollectionUtilities.getFirstItem(clsTreeSelection);
		String applyToValue = "";
		
		if (firstSelection instanceof Frame) {
			applyToValue = ((Frame)firstSelection).getName();
		} else {
			applyToValue = firstSelection.toString();
		}
		
						
		Instance ontoCompInst = ChangeOntologyUtil.createOntologyComponentInstance(getCurrentInstance().getKnowledgeBase(), applyToValue, "(Empty)");
					
		refreshDisplay();
		
		ComponentUtilities.extendSelection(getAnnotationsTree(), ontoCompInst);

	}
	
	
	private Collection getClsTreeSelection() {
		String collabClsesTabClassName = CollaborativeClsesTab.class.getName();
		
		CollaborativeClsesTab clsesTab = (CollaborativeClsesTab) ProjectManager.getProjectManager().getCurrentProjectView().getTabByClassName(collabClsesTabClassName);
		
		if (clsesTab == null) {
			return null;
		}
		
		return ComponentUtilities.getSelection(clsesTab.getClsTree());		
		
	}	
}
