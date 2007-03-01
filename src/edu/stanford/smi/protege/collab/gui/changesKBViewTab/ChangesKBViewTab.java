package edu.stanford.smi.protege.collab.gui.changesKBViewTab;

import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.ClsesAndInstancesTab;
import edu.stanford.smi.protegex.changes.ChangesTab;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangesKBViewTab extends AbstractTabWidget {
	ClsesAndInstancesTab ciTab;
	Project changesProject;

	public void initialize() {	
		setLabel("ChangesKBViewTab");
		
		changesProject = getChangesProject();
		buildGUI();		
	}

	public void buildGUI() {	
		if (changesProject == null) {
			return;
		}
		
		ciTab = new ClsesAndInstancesTab();
		ciTab.setup(getDescriptor(), changesProject);
		ciTab.initialize();
		
		add(ciTab);
	};
	
	
	public Project getChangesProject() {
		if (changesProject != null) {
			return changesProject;
		}
		
		KnowledgeBase changesKB = null;
		
		if (getProject() != null) {
			changesKB = ChangeOntologyUtil.getChangesKB(getKnowledgeBase());
		}
		
		return (changesKB == null ? null : changesKB.getProject());
	}	
	
	
	public static boolean isSuitable(Project p, Collection errors) {
		try {
			KnowledgeBase kb = ChangesTab.getChangesKB();
			if (kb == null) {
				errors.add("Needs Changes ontology");
				return false;
			}
			
			return true;
		} catch (Exception e) {
			errors.add("Needs Changes ontology");
			return false;
		}
	}
	   
	
}
