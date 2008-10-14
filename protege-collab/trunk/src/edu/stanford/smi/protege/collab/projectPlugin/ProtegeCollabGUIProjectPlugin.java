package edu.stanford.smi.protege.collab.projectPlugin;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.collab.annotation.gui.ConfigureCollabProtegeAction;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProjectViewEvent;
import edu.stanford.smi.protege.util.ProjectViewListener;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.server_changes.ChangesProject;


public class ProtegeCollabGUIProjectPlugin extends ProjectPluginAdapter {

	public final static String TOOLS_MENU = "Tools";

	private AnnotationsDisplayComponent annotationsDisplayComponent;
	private ProjectViewListener projectViewListener;
	private JMenu collabMenu;


	@Override
	public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
		KnowledgeBase kb = view.getProject().getKnowledgeBase();

		if (!ChangesProject.isChangeTrackingEnabled(view.getProject()) || ChAOUtil.getChangesKb(kb) == null) {
			return;
		}

		Log.getLogger().info("Started Collaborative Protege");

		insertCollabPanel(view);
		attachProjectViewListener(view);
		UIUtil.adjustTreeFrameRenderers(view);
		insertCollabMenu(menuBar);
	}



	private void attachProjectViewListener(ProjectView view) {
		projectViewListener = new ProjectViewListener() {

			public void closed(ProjectViewEvent event) {
				//System.out.println("Project view closed " + event);
			}

			public void saved(ProjectViewEvent event) {
				//System.out.println("Project view saved " + event);
			}

			public void tabAdded(ProjectViewEvent event) {
				//System.out.println("Tab added " + event);
				UIUtil.adjustTreeFrameRenderer((TabWidget)event.getWidget());
				annotationsDisplayComponent.init();
			}
		};
		view.addProjectViewListener(projectViewListener);
	}


	private boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return ChAOUtil.isChangesOntologyPresent(kb);
	}


	private void insertCollabMenu(ProjectMenuBar menuBar) {
		collabMenu = new JMenu("Collaboration");

		JMenuItem configureItem = new JMenuItem(new ConfigureCollabProtegeAction(annotationsDisplayComponent));
		collabMenu.add(configureItem);

		menuBar.add(collabMenu);
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

		//remove menu
		JMenuBar mainMenuBar = ProjectManager.getProjectManager().getCurrentProjectMenuBar();
		mainMenuBar.remove(collabMenu);
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
		}
	}

}
