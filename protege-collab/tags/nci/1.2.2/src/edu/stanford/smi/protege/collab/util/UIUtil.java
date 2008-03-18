package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import edu.stanford.smi.protege.collab.annotation.gui.renderer.FramesWithAnnotationsRenderer;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.owl.ui.ResourceRenderer;
import edu.stanford.smi.protegex.owl.ui.individuals.OWLIndividualsTab;
import edu.stanford.smi.protegex.owl.ui.properties.OWLPropertiesTab;
import edu.stanford.smi.protegex.owl.ui.properties.OWLPropertyHierarchiesPanel;

public class UIUtil {

	public static Selectable getSelectableForTab(TabWidget tabWidget) {
		Selectable selectable = null;

		if (tabWidget == null) {
			return null;
		}
		
		try {			
			if (tabWidget instanceof InstancesTab) {
				selectable = ((InstancesTab)tabWidget).getDirectInstancesList();
			} else if (tabWidget instanceof OWLPropertiesTab) {
				selectable = ((OWLPropertiesTab)tabWidget).getNestedSelectable();
			} else if (tabWidget instanceof OWLIndividualsTab) {
				selectable = ((OWLIndividualsTab)tabWidget).getNestedSelectable();
			} else {
				selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
			}
			
		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Error at getting selectable for tab " + tabWidget, t);
		}
		
		return selectable;
	}
	
	public static Collection getClsTreeSelection(TabWidget tabWidget) {
		Selectable selectable = null;
		
		if (tabWidget == null) {
			return null;
		}
	
		try {
			if (tabWidget instanceof InstancesTab) {
				return ((InstancesTab)tabWidget).getSelectedInstances();
			} else if (tabWidget instanceof OWLPropertiesTab) {
				selectable = (((OWLPropertiesTab)tabWidget).getNestedSelectable());				
			} else if (tabWidget instanceof OWLIndividualsTab) {
				selectable = ((OWLIndividualsTab)tabWidget).getNestedSelectable();
			} else if (tabWidget instanceof AbstractTabWidget) {			
				selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
			}			
		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Error at getting selectable for tab " + tabWidget, t);
		}
			
		return (selectable == null ? null : selectable.getSelection());		
	}	
	
	
	//maybe move somewhere else	- had to move here because of class loading problems, when OWL is not present
	public static void adjustTreeFrameRenderers(ProjectView view) {
		Collection<TabWidget> tabs = view.getTabs();
		
		for (TabWidget tabwidget : tabs) {
			adjustTreeFrameRenderer(tabwidget);
		}
	}

	public static void adjustTreeFrameRenderer(TabWidget tabWidget) {
		if (!(tabWidget instanceof AbstractTabWidget)) {
			return;
		}
		
		try {
			if (tabWidget instanceof OWLPropertiesTab) {
				ResourceRenderer renderer  = new ResourceRenderer();
				FramesWithAnnotationsRenderer treeRenderer = new FramesWithAnnotationsRenderer((FrameRenderer) renderer);
				((OWLPropertyHierarchiesPanel)((OWLPropertiesTab)tabWidget).getNestedSelectable()).setHierarchyTreeRenderer(treeRenderer);
				return;
			}			
		} catch (Throwable t) {
			//happens when OWL is not present
			Log.getLogger().warning("Errors at setting tree renderer for " + tabWidget + " Error message: " + t.getMessage());
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
			} catch (Throwable t) {
				Log.getLogger().warning("Errors at setting tree renderer for " + tabWidget + " Error message: " + t.getMessage());
			}
		}
		
	}
	
}
