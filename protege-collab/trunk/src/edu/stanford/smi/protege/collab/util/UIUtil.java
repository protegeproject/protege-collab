package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.AnnotationBrowserSlotPattern;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.FramesWithAnnotationsRenderer;
import edu.stanford.smi.protege.collab.projectPlugin.ProtegeCollabGUIProjectPlugin;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.InstanceDisplay;
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
				selectable = ((OWLPropertiesTab)tabWidget).getNestedSelectable();
			} else if (tabWidget instanceof OWLIndividualsTab) {
				selectable = ((OWLIndividualsTab)tabWidget).getNestedSelectable();
			} else if (tabWidget instanceof AbstractTabWidget) {
				selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
			}
		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Error at getting selectable for tab " + tabWidget, t);
		}

		return selectable == null ? null : selectable.getSelection();
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
				FramesWithAnnotationsRenderer treeRenderer = new FramesWithAnnotationsRenderer(renderer);
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

	public static void adjustAnnotationBrowserPattern(KnowledgeBase kb) {
		KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(kb);
		if (chaoKb == null) { return; }
		AnnotationFactory annotationFactory = new AnnotationFactory(chaoKb);
		AnnotationBrowserSlotPattern pattern = new AnnotationBrowserSlotPattern(chaoKb);
		annotationFactory.getAnnotationClass().setDirectBrowserSlotPattern(pattern);
		annotationFactory.getProposalClass().setDirectBrowserSlotPattern(pattern);
		annotationFactory.getVoteClass().setDirectBrowserSlotPattern(pattern);
	}

	public static InstanceDisplay createAnnotationInstanceDisplay(Annotation annotation, KnowledgeBase chaoKb) {
		Instance annotInst = ((AbstractWrappedInstance)annotation).getWrappedProtegeInstance();
		InstanceDisplay instDispl = new InstanceDisplay(chaoKb.getProject(), false, true);
		instDispl.setInstance(annotInst);

		/*
		 * does not work - need to figure out another way of enabling/disabling the widgets
		 */
		/*
		//make sure that the subject and the body are not read-only
		ClsWidget clsWidget = instDispl.getFirstClsWidget();
		if (clsWidget == null) { return instDispl; }

		AnnotationFactory annotationFactory = new AnnotationFactory(chaoKb);
		((AbstractSlotWidget)clsWidget.getSlotWidget(annotationFactory.getSubjectSlot())).setEditable(true);
		((AbstractSlotWidget)clsWidget.getSlotWidget(annotationFactory.getSubjectSlot())).setEnabled(true);
		((AbstractSlotWidget)clsWidget.getSlotWidget(annotationFactory.getBodySlot())).setEditable(true);
		*/

		return instDispl;
	}
	
	public static boolean isCollaborationPanelEnabled(Project p) {
		Boolean value = (Boolean) p.getClientInformation(ProtegeCollabGUIProjectPlugin.SHOW_COLLAB_PANEL_PRJ_INFO);
		return value == null ? false : Boolean.valueOf(value);
	}

	public static void setCollaborationPanelEnabled(Project p, boolean enabled) {
		p.setClientInformation(ProtegeCollabGUIProjectPlugin.SHOW_COLLAB_PANEL_PRJ_INFO, Boolean.valueOf(enabled));
	}
	
}
