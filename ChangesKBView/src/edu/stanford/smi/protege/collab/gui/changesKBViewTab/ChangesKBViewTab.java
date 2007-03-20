package edu.stanford.smi.protege.collab.gui.changesKBViewTab;

import java.awt.Container;
import java.util.Collection;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.DirectInstancesList;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectableList;
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
		
		//getClsTree().setCellRenderer(new FrameIDRenderer());
		adjustDirectInstanceRenderer();	
		
		add(ciTab);
	};
	
	
	public Project getChangesProject() {
		if (changesProject != null) {
			return changesProject;
		}
		
		KnowledgeBase changesKB = null;
		
		if (getProject() != null) {
			changesKB = ChangeOntologyUtil.getChangesKb(getKnowledgeBase());
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
	   
	
	private void adjustDirectInstanceRenderer() {	
		try {
			//ugly way of getting the instance list, because there are no getter methods in the superclass
			DirectInstancesList dirList = (DirectInstancesList)((Container)((Container)((Container)getComponent(0)).getComponent(2)).getComponent(1)).getComponent(1);
			((SelectableList)((DirectInstancesList) dirList).getSelectable()).setCellRenderer(new FrameIDRenderer());
			
		} catch (Exception e) {
			Log.getLogger().warning("Error at setting browser slot " + e.getMessage());			
		}
	}
	
	private final class FrameIDRenderer extends FrameRenderer {		
		@Override
		public void load(Object value) {
			super.load(value);
			if (value instanceof Frame)
				appendText(" id=" + ((Frame)value).getFrameID().getLocalPart());
		}
	}
	
}
