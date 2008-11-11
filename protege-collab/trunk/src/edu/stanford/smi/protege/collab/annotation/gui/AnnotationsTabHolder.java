package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import edu.stanford.smi.protege.collab.annotation.gui.panel.AllAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.AnnotationsTabPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.ChangesAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.ChatPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.DiscussionThreadPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.OntologyComponentAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.SearchPanel;
import edu.stanford.smi.protege.collab.util.CollabTabsConfiguration;
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

	private Instance currentInstance = null;
	private KnowledgeBase kb;

	private JTabbedPane tabbedPane;
	private Collection<AnnotationsTabPanel> tabs;


	public AnnotationsTabHolder(KnowledgeBase kb) {
		this.kb = kb;

		//disabling by default the AllCollabTab
		if (kb.getProject().getClientInformation(AllAnnotationsPanel.class.getName()) == null) {
			CollabTabsConfiguration.setTabEnabled(kb.getProject(), AllAnnotationsPanel.class, false);
		}

		//disabling the chat tab is not multi user client
		if (!kb.getProject().isMultiUserClient()) {
			CollabTabsConfiguration.setTabEnabled(kb.getProject(), ChatPanel.class, false);
		}

		tabbedPane = createTabbedPane();
		setSelectable(getSelectedTab().getSelectable());
		add(tabbedPane);
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

		tabbedPane.setSelectedIndex(0);

		return tabbedPane;
	}


	protected void addTabs() {
		tabs = createTabs();

		for (AnnotationsTabPanel annotTabPanel : tabs) {
			tabbedPane.addTab(annotTabPanel.getName(), annotTabPanel.getIcon(), annotTabPanel);
		}
	}

	protected Collection<AnnotationsTabPanel> createTabs() {
		tabs = new ArrayList<AnnotationsTabPanel>();

		addTab(new OntologyComponentAnnotationsPanel(kb));
		addTab(new ChangesAnnotationsPanel(kb));
		addTab(new AllAnnotationsPanel(kb));
		addTab(new DiscussionThreadPanel(kb));
		addTab(new SearchPanel(kb));

		//special treatment for chat because it initializes chat project on server - will be changed when the plugin infrastructure is avaialbe
		if (CollabTabsConfiguration.isTabEnabled(kb.getProject(), ChatPanel.class)) {
			addTab(new ChatPanel(kb));
		}
		return tabs;
	}


	protected void addTab(AnnotationsTabPanel annotationsTabPanel) {
		if (!CollabTabsConfiguration.isTabEnabled(kb.getProject(), annotationsTabPanel.getClass())) {
			return;
		}
		try {
			tabs.add(annotationsTabPanel);
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Error at adding annotations tab " + annotationsTabPanel, e);
		}
	}

	protected void reload() {
		disposeTabs();
		addTabs();
		//tabbedPane.setSelectedIndex(0);
	}


	public void setInstance(Instance instance) {
		currentInstance = instance;

		AnnotationsTabPanel annotTabPanel = getSelectedTab();
		annotTabPanel.setInstance(currentInstance);
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

	@Override
	public void dispose() {
		disposeTabs();
		tabs.clear();
		super.dispose();
	}

	protected void disposeTabs() {
		tabbedPane.removeAll();

		for (AnnotationsTabPanel tab : tabs) {
			try {
				tab.dispose();
			} catch (Exception e) {
				Log.getLogger().warning("Error at disposing collaborative tab " + tab);
			}
		}
	}


}
