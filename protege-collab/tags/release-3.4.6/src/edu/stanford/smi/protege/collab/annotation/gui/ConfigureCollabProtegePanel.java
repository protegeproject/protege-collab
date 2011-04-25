package edu.stanford.smi.protege.collab.annotation.gui;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ValidatableTabComponent;

/**
 * The global configuration panel.  This panel consists of a set of tabs that each handles a specific part of the 
 * configuration.
 * Class has been expanded to allow user defined configuration tabs in the project configuration panel. 
 * To add a new tab to the configuration panel, call: ConfigureProjectPanel.registerConfigureTab(String tabTitle, String clsName)
 * To remove a tab from the configuration panel, call: ConfigureProjectPanel.unregisterConfigureTab(String configTabName)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 * @author    Tania Tudorache <tudorache@stanford.edu>
 */

public class ConfigureCollabProtegePanel extends ValidatableTabComponent {	
	private Project _project;
	
    public ConfigureCollabProtegePanel(Project project) {
    	_project = project;
    	
        addTab("Collaborative Tabs", new ConfigureCollabTabsPanel(project));
        addTab("Options", new ConfigureOptionsTabPanel(project));
    }
    
}
