package edu.stanford.smi.protege.collab.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.collab.annotation.gui.panel.AllAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.ChangesAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.EntityAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.OntologyAnnotationsPanel;
import edu.stanford.smi.protege.collab.annotation.gui.panel.SearchPanel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.PluginUtilities;

/**
 * Note: This is a temporary implementation until a plugin architecture for the
 * collaborative tabs is implemented.
 *
 * @author ttania
 *
 */
public class CollabTabsConfiguration {

	private static List<Class<?>> allCollabTabs = new ArrayList<Class<?>>();

	static {
		fillAllCollabTabs();
	}


	private static void fillAllCollabTabs() {
		allCollabTabs.add(EntityAnnotationsPanel.class);
		allCollabTabs.add(ChangesAnnotationsPanel.class);
		allCollabTabs.add(AllAnnotationsPanel.class);
		allCollabTabs.add(OntologyAnnotationsPanel.class);
		allCollabTabs.add(SearchPanel.class);

		//ChatTab might not be present
		Class<?> chatTab = PluginUtilities.forName("edu.stanford.smi.protege.collab.annotation.gui.panel.ChatPanel", true);
		if (chatTab != null) {
			allCollabTabs.add(chatTab);
		}
	}


	public static boolean isTabEnabled(Project project, Class<?> tab){
		String b = (String) project.getClientInformation(tab.getName());
		return b == null ? true : b.equalsIgnoreCase("false") ? false : true;
	}


	public static void setTabEnabled(Project project, Class<?> tab, boolean enabled) {
		project.setClientInformation(tab.getName(), Boolean.toString(enabled));
	}

	public static Collection<String> getAllCollabTabNames() {
		ArrayList<String> names = new ArrayList<String>();

		for (Class<?> tab : allCollabTabs) {
			names.add(tab.getSimpleName());
		}

		return names;
	}

	public static Collection<Class<?>> getAllCollabTabClasses() {
		return allCollabTabs;
	}

}
