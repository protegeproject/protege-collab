package edu.stanford.smi.protege.collab.collabClassesTab;

import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.FramesWithAnnotationsRenderer;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.widget.ClsesTab;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class CollaborativeClsesTab extends ClsesTab {
	private KnowledgeBase changesKB = null;
	
	private AllowableAction viewAnnotationsAction;
	
	private AnnotationsDisplayComponent annotationDisplayComponent;
	
		
	public CollaborativeClsesTab() {		
		viewAnnotationsAction = getViewAnnotationsAction();
	}
	
	@Override
	public void initialize() {	
		super.initialize();
		
		changesKB = getChangesKB();
		
		getClsTree().setCellRenderer(new FramesWithAnnotationsRenderer(changesKB));
		
		setLabel("CollaborativeClsesTab");
	}
	
	@Override
	protected ClsesPanel createClsesPanel() {
		CollaborativeClsesPanel panel = new CollaborativeClsesPanel(getProject(), getViewAnnotationsAction());		
		//needed because of initialization sequence
		panel.setViewAnnotationsAction(getViewAnnotationsAction());
					
		panel.addSelectionListener(new SelectionListener() {
			public void selectionChanged(SelectionEvent event) {
				transmitSelection();
			}
		});
		return panel;
	}
	
	
	
	protected AllowableAction getViewAnnotationsAction() {
		if (viewAnnotationsAction != null) {
			return viewAnnotationsAction;
		}
		
		viewAnnotationsAction = new AllowableAction(new ResourceKey("View Annotations"), (Selectable) getClsTree()) {

			public void actionPerformed(ActionEvent arg0) {
				System.out.println("View Annotations " + changesKB);
			}
		};
		
		return viewAnnotationsAction;
	}

	
	//fix this when the time comes
	public KnowledgeBase getChangesKB() {
		if (changesKB != null) {
			return changesKB;
		}
		
		return ChangeOntologyUtil.getChangesKb(getKnowledgeBase());
	}
	
	public void setChangesKB(KnowledgeBase changesKB) {
		this.changesKB = changesKB;		
	}

	@Override
	protected JComponent createClsDisplay() {
		
		JComponent instanceDisplay = super.createClsDisplay();
		
		if (!ChangeOntologyUtil.isChangesOntologyPresent(getKnowledgeBase())) {
			Log.getLogger().warning("Change ontology is not present. Cannot display annotations for it.");
			annotationDisplayComponent = null;
		} else {		
			annotationDisplayComponent = new AnnotationsDisplayComponent(getProject());
		}
		
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane(instanceDisplay,annotationDisplayComponent, true);
        	
		return pane;
	}
	
	@Override
	protected void transmitSelection() {		
		super.transmitSelection();
    
		if (annotationDisplayComponent != null && getClsTree() != null) {
			annotationDisplayComponent.setInstances(((Selectable)getClsTree()).getSelection());
		}
      
	}
	
}
