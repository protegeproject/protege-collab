package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.collab.annotation.gui.panel.AbstractAnnotationsTabPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.ChatPanel;
import edu.stanford.smi.protege.collab.util.CollabTabsConfiguration;
import edu.stanford.smi.protege.collab.util.OntologyAnnotationsCache;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTabHolder extends SelectableContainer {	
	private static final long serialVersionUID = 7250046306170096186L;
	
	private Instance currentInstance = null;
	private KnowledgeBase kb;

	private JTabbedPane tabbedPane;
	private Collection<AbstractAnnotationsTabPanel> tabs;
	
	private OntologyAnnotationsCache ontologyAnnotationsCache;


	public AnnotationsTabHolder(KnowledgeBase kb) {
		this.kb = kb;	
		//disabling the chat tab is not multi user client
		if (!kb.getProject().isMultiUserClient()) {
			CollabTabsConfiguration.setTabEnabled(kb.getProject(), ChatPanel.class, false);
		}

		tabbedPane = createTabbedPane();
		if (getSelectedTab() != null) {
			setSelectable(getSelectedTab().getSelectable());
		}
		add(tabbedPane);
	}

	
	public OntologyAnnotationsCache getOntologyAnnotationsCache() {
		return ontologyAnnotationsCache;
	}
	
	public void setOntologyAnnotationsCache(
			OntologyAnnotationsCache ontologyAnnotationsCache) {
		this.ontologyAnnotationsCache = ontologyAnnotationsCache;
		for (AbstractAnnotationsTabPanel tab : tabs) {
			tab.setOntologyAnnotationsCache(ontologyAnnotationsCache);
		}
	}

	private JPopupMenu getPopupMenu() {
		JPopupMenu popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("Configure", OWLIcons.getPreferencesIcon());
	    menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ConfigureCollabProtegePanel configPanel = new ConfigureCollabProtegePanel(kb.getProject());
				int sel = ModalDialog.showDialog(AnnotationsTabHolder.this, configPanel, "Configure Collaborative Protege", ModalDialog.MODE_OK_CANCEL);
				if (sel == ModalDialog.OPTION_OK) {
					reload();
				}
			}
	    });
	    popup.add(menuItem);
	    return popup;
	}


	protected JTabbedPane createTabbedPane() {
		tabbedPane = ComponentFactory.createTabbedPane(true);
		addTabs();
		if (tabbedPane.getTabCount() > 0) {
			tabbedPane.setSelectedIndex(0);
		}
		return tabbedPane;
	}


	protected void addTabs() {
		tabs = createTabs();
		for (AbstractAnnotationsTabPanel annotTabPanel : tabs) {
			tabbedPane.addTab(annotTabPanel.getName(), annotTabPanel.getIcon(), annotTabPanel);
		}
	}

	protected Collection<AbstractAnnotationsTabPanel> createTabs() {
		tabs = new ArrayList<AbstractAnnotationsTabPanel>();
		Collection<Class<?>> classes = CollabTabsConfiguration.getAllCollabTabClasses();
		for (Class<?> tabClass : classes) {
			addTab(tabClass);
		}	
		return tabs;
	}

	protected void addTab(Class<?> tabClass) {
		AbstractAnnotationsTabPanel panel = null;
		if (CollabTabsConfiguration.isTabEnabled(kb.getProject(), tabClass)) {
			try {
				Constructor<?> con = tabClass.getConstructor(new Class[] { KnowledgeBase.class});
				panel = (AbstractAnnotationsTabPanel) con.newInstance(new Object[] {kb});
			} catch (Throwable t) {
				Log.getLogger().log(Level.WARNING, "Error at constructing collaboratve panel: " + tabClass, t);
			}
			if (panel != null) {
				panel.setOntologyAnnotationsCache(ontologyAnnotationsCache);
				tabs.add(panel);
			}
		}			
	}
	

	protected void reload() {
		getSelectable().clearSelection();
		disposeTabs();
		addTabs();
		//tabbedPane.setSelectedIndex(0);
	}


	public void setInstance(Instance instance) {
		currentInstance = instance;

		AbstractAnnotationsTabPanel annotTabPanel = getSelectedTab();
		annotTabPanel.setInstance(currentInstance);
	}

	public void setInstances(Collection instances) {
		//reimplement this
		setInstance((Instance) CollectionUtilities.getFirstItem(instances));
	}


	public AbstractAnnotationsTabPanel getSelectedTab() {
		return (AbstractAnnotationsTabPanel) tabbedPane.getSelectedComponent();
	}

	public int getSelectedTabIndex() {
		return tabbedPane.getSelectedIndex();
	}


	public void refreshDisplay() {
		AbstractAnnotationsTabPanel annotTabPanel = getSelectedTab();
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

	public Collection<AbstractAnnotationsTabPanel> getTabs() {
		return tabs;
	}


	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}


	public void refreshAllTabs() {
		for (AbstractAnnotationsTabPanel tab : tabs) {
			tab.refreshDisplay();
		}

	}

	public KnowledgeBase getKb() {
		return kb;
	}

	@Override
	public void dispose() {
		disposeTabs();
		tabs.clear();
		super.dispose();
	}

	protected void disposeTabs() {
		tabbedPane.removeAll();
		for (AbstractAnnotationsTabPanel tab : tabs) {
			try {
				tab.dispose();
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Error at disposing collaborative tab: " + tab, e);
			}
		}
	}


}
