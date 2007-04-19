package edu.stanford.smi.protege.collab.projectPlugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeCellRenderer;

import sun.security.krb5.internal.an;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.FramesWithAnnotationsRenderer;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.GetAnnotationProjectName;

public class ProtegeCollabGUIProjectPlugin extends ProjectPluginAdapter {
	AnnotationsDisplayComponent annotationsDisplayComponent;

	@Override
	public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
		KnowledgeBase kb = view.getProject().getKnowledgeBase();
		
		if (ChangeOntologyUtil.getChangesKb(kb) == null) {		
			return;
		}
		
		insertCollabPanel(view);
		adjustTreeFrameRenderers(view);
	}


	private boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return ChangeOntologyUtil.isChangesOntologyPresent(kb);		
	}


	private AnnotationsDisplayComponent insertCollabPanel(ProjectView view) {
		//this is not working in owl
		JComponent parent = (JComponent)view.getParent();		
		parent.remove(view);		
		
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
		pane.setDividerLocation(0.75);
		pane.setResizeWeight(0.75);
		pane.setLeftComponent(view);
				
		annotationsDisplayComponent = new AnnotationsDisplayComponent(view.getProject().getKnowledgeBase(), null);
		pane.setRightComponent(annotationsDisplayComponent);
		
		parent.add(pane, BorderLayout.CENTER);
		
		parent.revalidate();
		
		return annotationsDisplayComponent;
	}
	
	
	private void adjustTreeFrameRenderers(ProjectView view) {
		Collection<TabWidget> tabs = view.getTabs();
		
		for (TabWidget tabwidget : tabs) {
			adjustTreeFrameRenderer(tabwidget);
		}
	}


	private void adjustTreeFrameRenderer(TabWidget tabWidget) {
		if (!(tabWidget instanceof AbstractTabWidget)) {
			return;
		}
		
		JTree clsTree = ((AbstractTabWidget)tabWidget).getClsTree();
		
		if (clsTree == null) {
			return;
		}
		
		TreeCellRenderer cellRenderer = clsTree.getCellRenderer();
		
		if (cellRenderer instanceof FrameRenderer) {
			FramesWithAnnotationsRenderer treeRenderer = new FramesWithAnnotationsRenderer(ChangeOntologyUtil.getChangesKb(tabWidget.getKnowledgeBase())); 
		
			//replace the tree renderer
			clsTree.setCellRenderer(treeRenderer);
			
			if (tabWidget instanceof InstancesTab) {
				((InstancesTab)tabWidget).getDirectInstancesList().setListRenderer(treeRenderer);
			}
		}
		
	}
	
	@Override
	public void beforeHide(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
		//clear kb2changesKb map
				
		//remove the annotations component from the main view
		if (annotationsDisplayComponent == null) {
			return;
		}
		
		JComponent parent = (JComponent) annotationsDisplayComponent.getParent();
		JComponent parentOfParent = null;
			
		if (parent != null) {
			parentOfParent = (JComponent)parent.getParent();
		}
		 
		//this is not working. fix it
		view.getParent().remove(annotationsDisplayComponent);

		if (parentOfParent != null && parent != null) {
			parentOfParent.remove(parent);
		}	
	}
	
	
	@Override
	public void beforeClose(Project p) {
		if (p == null) {
			return;
		}
		
		if (p.isMultiUserClient() && isChangesOntologyPresent(p.getKnowledgeBase())) {
			
			if (annotationsDisplayComponent != null) {
				annotationsDisplayComponent.dispose();
			}
			
//			dispose also the changes project
			KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(p.getKnowledgeBase(), false);
			
			if (changesKb != null) {
				Project changesProject = changesKb.getProject();
				
				try {
					changesProject.dispose();
				} catch (Exception e) {
					Log.getLogger().warning("Errors at disposing changes project " + changesProject + " of project " + p);
				}
			}
			
			ChangeOntologyUtil.clearKb2ChangesKbMap();
		}	
	}
	
}
