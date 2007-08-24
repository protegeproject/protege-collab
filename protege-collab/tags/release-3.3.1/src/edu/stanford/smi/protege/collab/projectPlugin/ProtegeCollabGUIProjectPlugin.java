package edu.stanford.smi.protege.collab.projectPlugin;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.FramesWithAnnotationsRenderer;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProjectViewEvent;
import edu.stanford.smi.protege.util.ProjectViewListener;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.owl.ui.ResourceRenderer;
import edu.stanford.smi.protegex.owl.ui.individuals.OWLIndividualsTab;
import edu.stanford.smi.protegex.owl.ui.properties.OWLPropertiesTab;
import edu.stanford.smi.protegex.owl.ui.properties.OWLPropertyHierarchiesPanel;
import edu.stanford.smi.protegex.server_changes.ChangesProject;


public class ProtegeCollabGUIProjectPlugin extends ProjectPluginAdapter {
	
	public final static String TOOLS_MENU = "Tools";
	
	AnnotationsDisplayComponent annotationsDisplayComponent;
	ProjectViewListener projectViewListener;

	@Override
	public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
		KnowledgeBase kb = view.getProject().getKnowledgeBase();
		
		if (!ChangesProject.isChangeTrackingEnabled(view.getProject()) || ChangeOntologyUtil.getChangesKb(kb) == null) {		
			return;
		}
		
		//TT: Unfinished implementation. Uncomment later.
		//insertCollabMenu(menuBar);
		insertCollabPanel(view);
		attachProjectViewListener(view);
		adjustTreeFrameRenderers(view);
	}



	private void attachProjectViewListener(ProjectView view) {
		projectViewListener = new ProjectViewListener() {

			public void closed(ProjectViewEvent event) {
				// TODO Auto-generated method stub
				//System.out.println("Project view closed " + event);
				
			}

			public void saved(ProjectViewEvent event) {
				// TODO Auto-generated method stub
				//System.out.println("Project view saved " + event);
				
			}

			public void tabAdded(ProjectViewEvent event) {
				//System.out.println("Tab added " + event);
				
				adjustTreeFrameRenderer((TabWidget)event.getWidget());
				annotationsDisplayComponent.init();
			}
			
		};
		
		view.addProjectViewListener(projectViewListener);
		
	}


	private boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return ChangeOntologyUtil.isChangesOntologyPresent(kb);
	}

	
	private void insertCollabMenu(ProjectMenuBar menuBar) {
		JMenu toolsMenu = ComponentUtilities.getMenu(menuBar, TOOLS_MENU, true, 3);
				
		final JCheckBoxMenuItem collabPanelMenuItem = new JCheckBoxMenuItem("Enable collaborative panel", AnnotationsIcons.getCommentIcon());
		collabPanelMenuItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				System.out.println(collabPanelMenuItem.isSelected());				
			}			
		});
		
		toolsMenu.insert(collabPanelMenuItem, 0);
		toolsMenu.insertSeparator(1);			
	}
	

	private AnnotationsDisplayComponent insertCollabPanel(ProjectView view) {
		JComponent parent = (JComponent)view.getParent();		
		parent.remove(view);		
		
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
		pane.setDividerLocation(0.75);
		pane.setResizeWeight(0.75);
		pane.setLeftComponent(view);
				
		annotationsDisplayComponent = new AnnotationsDisplayComponent(view.getProject().getKnowledgeBase());
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
		
		if (tabWidget instanceof OWLPropertiesTab) {
			ResourceRenderer renderer  = new ResourceRenderer();
			FramesWithAnnotationsRenderer treeRenderer = new FramesWithAnnotationsRenderer((FrameRenderer) renderer);
			((OWLPropertyHierarchiesPanel)((OWLPropertiesTab)tabWidget).getNestedSelectable()).setHierarchyTreeRenderer(treeRenderer);
			return;
		}
		
		JTree clsTree = ((AbstractTabWidget)tabWidget).getClsTree();
		
		if (clsTree == null) {
			return;
		}
		
		TreeCellRenderer cellRenderer = clsTree.getCellRenderer();
		
		if (cellRenderer instanceof FrameRenderer) {
			FramesWithAnnotationsRenderer treeRenderer = new FramesWithAnnotationsRenderer((FrameRenderer) cellRenderer); 
			 try {
					//replace the tree renderer
					clsTree.setCellRenderer(treeRenderer);
					
					if (tabWidget instanceof InstancesTab) {
						treeRenderer.setDisplayDirectInstanceCount(true);
						((InstancesTab)tabWidget).getDirectInstancesList().setListRenderer(treeRenderer);
					} else if (tabWidget instanceof OWLIndividualsTab) {
						((SelectableList)((SelectableContainer)((OWLIndividualsTab)tabWidget).getNestedSelectable()).getSelectable()).setCellRenderer(treeRenderer);				
					}
			} catch (Exception e) {
				Log.getLogger().warning("Errors at setting tree renderer for " + tabWidget);
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
		
		//detach project view listener if present
		if (projectViewListener != null) {
			view.removeProjectViewListener(projectViewListener);
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
