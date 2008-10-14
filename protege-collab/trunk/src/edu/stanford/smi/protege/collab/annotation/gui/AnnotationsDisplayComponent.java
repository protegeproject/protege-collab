package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.panel.AnnotationsTabPanel;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.changes.ChangesKbFrameListener;
import edu.stanford.smi.protege.collab.changes.ClassChangeListener;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyComponentCache;
import edu.stanford.smi.protege.collab.util.UIUtil;
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
	private SelectionListener clsTreeSelectionListener;
	private ChangesKbFrameListener changesKbFrameListener;
	private ChangeListener protegeTabChangeListener;
	private ChangeListener collabTabChangeListener;

	public AnnotationsDisplayComponent(KnowledgeBase kb) {
		this.kb = kb;

		annotationsTabHolder = createAnnotationsTabHolder();
		annotationBodyTextComponent = createAnnotationBodyComponent();

		LabeledComponent labeledComponentTabHolder = new LabeledComponent("Collaboration", annotationsTabHolder, true);
		LabeledComponent labeledComponentText = new LabeledComponent("Details", annotationBodyTextComponent, true);

		JSplitPane topBottomSplitPane = ComponentFactory.createTopBottomSplitPane(labeledComponentTabHolder, labeledComponentText, true);
		labeledComponentText.setPreferredSize(new Dimension(100, 200));
		topBottomSplitPane.setResizeWeight(1);
		topBottomSplitPane.setOneTouchExpandable(true);

		classChangeListener = getClassChangeListener();

		setSelectable(annotationsTabHolder);

		attachAnnotationTreeSelectionListener();

		annotationsTabHolder.getTabbedPane().addChangeListener(getCollabTabChangeListener());

		attachProtegeTabTreeListener();

		attachChangeKbListeners();

		add(topBottomSplitPane);
	}


	/*
	 * Listeners
	 */


	protected void attachChangeKbListeners() {
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(kb);

		changesKbFrameListener = new ChangesKbFrameListener();
		changesKb.addFrameListener(changesKbFrameListener);
	}


	protected void attachAnnotationTreeSelectionListener() {
		for (AnnotationsTabPanel annotationPanel : annotationsTabHolder.getTabs()) {
			annotationPanel.addSelectionListener(new SelectionListener() {

				public void selectionChanged(SelectionEvent event) {
					AnnotatableThing thing = (AnnotatableThing) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelectedTab().getAnnotationsTree().getSelection());
					if (thing == null) {
						((InstanceDisplay)annotationBodyTextComponent).setInstance(null);
					} else {
						Instance annotInstance = ((AbstractWrappedInstance)thing).getWrappedProtegeInstance();
						((InstanceDisplay)annotationBodyTextComponent).setInstance(annotInstance);
					}
				}
			});
		}
	}


	protected ChangeListener getProtegeTabChangeListener() {
		if (protegeTabChangeListener != null) {
			return protegeTabChangeListener;
		}

		protegeTabChangeListener = new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				//System.out.println("Tab changed " + ((JTabbedPane)e.getSource()).getSelectedComponent()  + "\nselectable before = " + selectable);
				clsTreeSelectionListener = getClsTreeSelectionListener();

				if (selectable != null) {
					selectable.removeSelectionListener(clsTreeSelectionListener);
				}

				TabWidget tabWidget = (TabWidget) ((JTabbedPane)e.getSource()).getSelectedComponent();
				selectable = UIUtil.getSelectableForTab(tabWidget);

				//do I need this here?
				setInstances(selectable == null ? null : selectable.getSelection());

				if (selectable != null) {
					selectable.addSelectionListener(clsTreeSelectionListener);
				}
			}

		};

		return protegeTabChangeListener;
	}


	protected ChangeListener getCollabTabChangeListener() {
		if (collabTabChangeListener == null) {
			collabTabChangeListener = new ChangeListener() {

				public void stateChanged(ChangeEvent arg0) {
					AnnotationsTabPanel annotTabPanel = annotationsTabHolder.getSelectedTab();
					//why??
					if (annotTabPanel == null) {
						return;
					}

					annotTabPanel.setInstance(currentInstance);
					setSelectable(annotTabPanel.getSelectable());
					AnnotatableThing annot = (AnnotatableThing) CollectionUtilities.getFirstItem(annotTabPanel.getSelection());
					Instance wrappedInst = new AnnotationFactory(ChAOUtil.getChangesKb(kb)).getWrappedProtegeInstance(annot);
					((InstanceDisplay)annotationBodyTextComponent).setInstance(wrappedInst);
				}
			};
		}

		return collabTabChangeListener;
	}


	protected SelectionListener getClsTreeSelectionListener() {
		if (clsTreeSelectionListener == null) {
			clsTreeSelectionListener = new SelectionListener() {

				public void selectionChanged(SelectionEvent event) {
					setInstances(event.getSelectable().getSelection());
				}

			};
		}
		return clsTreeSelectionListener;

	}

	protected ClassChangeListener getClassChangeListener() {
		if (classChangeListener == null) {
			classChangeListener = new ClassChangeListener() {
				@Override
				public void refreshClassDisplay(FrameEvent event) {
					refreshAllTabs();
				}
			};}
		return classChangeListener;
	}


	private void attachProtegeTabTreeListener() {
		ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
		TabWidget currentTab = view.getSelectedTab();

		selectable = UIUtil.getSelectableForTab(currentTab);
		if (selectable != null) {
			clsTreeSelectionListener = getClsTreeSelectionListener();
			selectable.addSelectionListener(clsTreeSelectionListener);
			setInstances(selectable.getSelection());
		}

		init();
	}


	/*
	 * GUI components
	 */

	protected AnnotationsTabHolder createAnnotationsTabHolder() {
		annotationsTabHolder = new AnnotationsTabHolder(kb);
		return annotationsTabHolder;
	}


	protected JComponent createAnnotationBodyComponent() {
		if (!ChAOUtil.isChangesOntologyPresent(kb)) {
			Log.getLogger().warning("Change ontology is not present. Cannot display annotations for it.");
			annotationBodyTextComponent = new InstanceDisplay(ProjectManager.getProjectManager().getCurrentProject());
		} else {
			annotationBodyTextComponent = new InstanceDisplay(ChAOUtil.getChangesKb(kb).getProject(), false, false);
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
	}

	public void setInstances(Collection instances) {
		//TODO: handle multiple selections
		setInstance(instances == null ? null : (Instance) CollectionUtilities.getFirstItem(instances));
	}

	//TODO: find a better solution for the initialization
	public void init() {
		ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();

		protegeTabChangeListener = getProtegeTabChangeListener();

		view.removeChangeListener(protegeTabChangeListener);
		view.addChangeListener(protegeTabChangeListener);
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


	public void reloadCollabTabs() {
		annotationsTabHolder.reload();
		attachAnnotationTreeSelectionListener();
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	@Override
	public void dispose() {
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(kb);

		if (changesKb == null) {
			Log.getLogger().warning("Cannot dispose properly the annotations component because changes kb is null");
			return;
		}

		try {
			changesKb.removeFrameListener(changesKbFrameListener);
		} catch (Exception e) {
			Log.getLogger().warning("Error at disposing changes ontology kb listener");
		}

		//clear caches
		OntologyComponentCache.getCache(changesKb).clearCache();
		HasAnnotationCache.getCache(changesKb).clearCache();

		try {
			annotationsTabHolder.dispose();
		} catch (Exception e) {
			Log.getLogger().warning("Error at disposing the annotations tab holder");
		}

		//TODO: dispose other listeners

		super.dispose();
	}

}
