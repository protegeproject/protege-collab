package edu.stanford.smi.protege.collab.projectPlugin;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;

public class ProtegeCollabGUIProjectPlugin extends ProjectPluginAdapter {

	@Override
	public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
		AnnotationsDisplayComponent annotDisplay = insertCollabPanel(view);
	}

	private AnnotationsDisplayComponent insertCollabPanel(ProjectView view) {
		JComponent parent = (JComponent)view.getParent();		
		parent.remove(view);		
		
		JSplitPane pane = ComponentFactory.createLeftRightSplitPane();
		pane.setDividerLocation(0.75);
		pane.setResizeWeight(0.75);
		pane.setLeftComponent(view);
				
		AnnotationsDisplayComponent annotDisplay = new AnnotationsDisplayComponent(view.getProject().getKnowledgeBase(), null);
		pane.setRightComponent(annotDisplay);
		
		parent.add(pane, BorderLayout.CENTER);
		
		parent.revalidate();
		
		return annotDisplay;
	}
	
}
