package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.collab.annotation.gui.panel.AnnotationsTabPanel;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.changes.ClassChangeListener;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsDisplayComponent extends SelectableContainer {
	private KnowledgeBase kb;
	
	private Instance currentInstance;
	
	private Selectable selectable;
	
	private JComponent annotationBodyTextComponent;
	
	private AnnotationsTabHolder annotationsTabHolder;
	
	private ClassChangeListener classChangeListener;
	
	private AnnotationClassListener annotationsListener;
	
	private SelectionListener clsTreeSelectionListener;
	
		
	public AnnotationsDisplayComponent(KnowledgeBase kb, final Selectable selectable) {
		this.kb = kb;
		this.selectable = selectable;		
		
		annotationsTabHolder = createAnnotationsTabHolder();
		annotationBodyTextComponent = createAnnotationBodyComponent();
		
		setSelectable(annotationsTabHolder);
		
		LabeledComponent labeledComponentTabHolder = new LabeledComponent("Annotations", annotationsTabHolder, true);
		LabeledComponent labeledComponentText = new LabeledComponent("Annotation body", annotationBodyTextComponent, true);
		
		JSplitPane topBottomSplitPane = ComponentFactory.createTopBottomSplitPane(labeledComponentTabHolder, labeledComponentText);
		//labeledComponentText.setMinimumSize(new Dimension(0, 100));
		labeledComponentText.setPreferredSize(new Dimension(100, 200));
		topBottomSplitPane.setDividerLocation(150 + topBottomSplitPane.getInsets().bottom);
		
		//labeledComponentText.setMinimumSize(new Dimension(0, 100));
		
		attachAnnotationTreeSelectionListener();	
		
		classChangeListener = new ClassChangeListener() {
			@Override
			public void refreshClassDisplay(FrameEvent event) {
				refreshAllTabs();				
			}			
		};
		
		
		annotationsTabHolder.getTabbedPane().addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				AnnotationsTabPanel annotTabPanel = annotationsTabHolder.getSelectedTab();
				//why??
				if (annotTabPanel == null) {
					return;
				}

				annotTabPanel.setInstance(currentInstance);
				
				setSelectable(annotTabPanel.getSelectable());
				
				Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotTabPanel.getSelection());
				
				((InstanceDisplay)annotationBodyTextComponent).setInstance(annotInstance);								
			}
		});
	
		
		annotationsListener = new AnnotationClassListener(kb);
		ChangeOntologyUtil.getChangesKb(kb).addKnowledgeBaseListener(annotationsListener);
	
		clsTreeSelectionListener = new SelectionListener() {

			public void selectionChanged(SelectionEvent event) {				
				setInstances(event.getSelectable().getSelection());
			}
			
		};
		
		if (selectable == null) {
			attachTabChangeListener();
		}
		
		add(topBottomSplitPane);
	}
	

	protected void attachAnnotationTreeSelectionListener() {
		for (AnnotationsTabPanel annotationPanel : annotationsTabHolder.getTabs()) {
			annotationPanel.addSelectionListener(new SelectionListener() {

				public void selectionChanged(SelectionEvent event) {				
					Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelectedTab().getAnnotationsTree().getSelection());
					((InstanceDisplay)annotationBodyTextComponent).setInstance(annotInstance);
				}
			});
		}		
	}

	
	private void attachTabChangeListener() {
		ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
		
		//just for the init
		TabWidget currentTab = view.getSelectedTab();
		if (currentTab != null && currentTab instanceof AbstractTabWidget) {
			selectable = (Selectable) ((AbstractTabWidget)currentTab).getClsTree();	
			if (selectable != null) {
				selectable.addSelectionListener(clsTreeSelectionListener);
				setInstances(selectable.getSelection());
			}
		}
		
		view.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {			
									
				if (selectable != null) {					
					selectable.removeSelectionListener(clsTreeSelectionListener);
				}
					
				TabWidget tabWidget = (TabWidget) ((JTabbedPane)e.getSource()).getSelectedComponent();
				if (tabWidget == null){
					selectable = null;
					return;
				}
				
				//this is a particular case. How should it be treated?
				if (tabWidget instanceof InstancesTab) {
					selectable = ((InstancesTab)tabWidget).getDirectInstancesList();
				} else {
					selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
				}
				
				//do I need this here?
				setInstances(selectable == null ? null : selectable.getSelection());

				if (selectable != null) {					
					selectable.addSelectionListener(clsTreeSelectionListener);
				}
			}			
			
		});
	}
	

	protected AnnotationsTabHolder createAnnotationsTabHolder() {
		annotationsTabHolder = new AnnotationsTabHolder(kb);
		return annotationsTabHolder;
	}


	protected JComponent createAnnotationBodyComponent() {
		if (!ChangeOntologyUtil.isChangesOntologyPresent(kb)) {
			Log.getLogger().warning("Change ontology is not present. Cannot display annotations for it.");
			annotationBodyTextComponent = new InstanceDisplay(ProjectManager.getProjectManager().getCurrentProject());
		} else {
			annotationBodyTextComponent = new InstanceDisplay(ChangeOntologyUtil.getChangesKb(kb).getProject());
		}
		
		return annotationBodyTextComponent;
	}
	
	public void setInstance(Instance instance) {
		
		if (currentInstance != null) {
			currentInstance.removeFrameListener(classChangeListener);
		}
		
		currentInstance = instance;
		
		if (currentInstance != null) {
			currentInstance.addFrameListener(classChangeListener);
		}
		
		annotationsTabHolder.setInstance(currentInstance);
		refreshDisplay();	
	}
	
	public void setInstances(Collection instances) {		
		//reimplement this
		setInstance(instances == null ? null : (Instance) CollectionUtilities.getFirstItem(instances));
	}


	protected void refreshDisplay() {
				
		annotationsTabHolder.refreshDisplay();
				
		Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelection());
		
		((InstanceDisplay)annotationBodyTextComponent).setInstance(annotInstance);
	}
	
	protected void refreshAllTabs() {
		annotationsTabHolder.refreshAllTabs();
		
		Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelection());
		
		((InstanceDisplay)annotationBodyTextComponent).setInstance(annotInstance);
	}
	
}
