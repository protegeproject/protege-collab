package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.collab.annotation.gui.panel.AllAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.AnnotationsTabPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.ChangesAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.DiscussionThreadPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.OntologyComponentAnnotationsPanel;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTabHolder extends SelectableContainer{
	private Instance currentInstance = null;

	private KnowledgeBase kb;
	
	private JTabbedPane tabbedPane;
	
	private Collection<AnnotationsTabPanel> tabs;
	
	public AnnotationsTabHolder(KnowledgeBase kb) {
		this.kb = kb;
				
		tabbedPane = createTabbedPane();
				
		//TT: maybe this won't work
		setSelectable(getSelectedTab().getSelectable());
		
		add(tabbedPane);
	}
	
	
	protected JTabbedPane createTabbedPane() {
		tabbedPane = ComponentFactory.createTabbedPane(true);

		tabs = createTabs();
		
		for (AnnotationsTabPanel annotTabPanel : tabs) {
			tabbedPane.addTab(annotTabPanel.getName(), annotTabPanel);
		}
			
		tabbedPane.setSelectedIndex(0);
		
		return tabbedPane;
	}
	
	
	protected Collection<AnnotationsTabPanel> createTabs() {
		tabs = new ArrayList<AnnotationsTabPanel>();
		
		tabs.add(new ChangesAnnotationsPanel(kb));
		tabs.add(new OntologyComponentAnnotationsPanel(kb));
		tabs.add(new AllAnnotationsPanel(kb));
		tabs.add(new DiscussionThreadPanel(kb));
				
		return tabs; 
	}
	
	
	public void setInstance(Instance instance) {		
		currentInstance = instance;
		
		AnnotationsTabPanel annotTabPanel = getSelectedTab();		
		annotTabPanel.setInstance(currentInstance);
		
		refreshDisplay();
	}
	
	public void setInstances(Collection instances) {
		//reimplement this		
		setInstance((Instance) CollectionUtilities.getFirstItem(instances));
		
	}
	

	public AnnotationsTabPanel getSelectedTab() {
		return (AnnotationsTabPanel) tabbedPane.getSelectedComponent();
	}
	
	public int getSelectedTabIndex() {
		return tabbedPane.getSelectedIndex();
	}
	
	
	public void refreshDisplay() {		
		AnnotationsTabPanel annotTabPanel = getSelectedTab();
		
		//why?
		if (annotTabPanel == null) {
			return;
		}
		
		setSelectable(annotTabPanel);
		
		annotTabPanel.refreshDisplay();
					
		repaint();
	}
	
	
	@Override
	public Selectable getSelectable() {	
		return getSelectedTab().getSelectable();
	}
	
	public Collection<AnnotationsTabPanel> getTabs() {
		return tabs;
	}


	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}


	public void refreshAllTabs() {
		for (AnnotationsTabPanel tab : tabs) {
			tab.refreshDisplay();
		}
		
	}
	
	public KnowledgeBase getKb() {
		return kb;
	}
	
	
}
