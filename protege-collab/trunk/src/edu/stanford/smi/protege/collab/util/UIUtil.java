package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.InstancesTab;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.owl.ui.individuals.OWLIndividualsTab;
import edu.stanford.smi.protegex.owl.ui.properties.OWLPropertiesTab;

public class UIUtil {

	public static Selectable getSelectableForTab(TabWidget tabWidget) {
		Selectable selectable = null;

		if (tabWidget == null) {
			return null;
		}
		
		try {
			
			if (tabWidget instanceof InstancesTab) {
				selectable = ((InstancesTab)tabWidget).getDirectInstancesList();
			} else if (tabWidget instanceof OWLPropertiesTab) {
				selectable = ((OWLPropertiesTab)tabWidget).getNestedSelectable();
			} else if (tabWidget instanceof OWLIndividualsTab) {
				selectable = ((OWLIndividualsTab)tabWidget).getNestedSelectable();
			} else {
				selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
			}
			
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Error at getting selectable for tab " + tabWidget, e);
		}
		
		return selectable;
	}
	
	public static Collection getClsTreeSelection(TabWidget tabWidget) {
		Selectable selectable = null;
		
		if (tabWidget == null) {
			return null;
		}
					
		if (tabWidget instanceof InstancesTab) {
			return ((InstancesTab)tabWidget).getSelectedInstances();
		} else if (tabWidget instanceof OWLPropertiesTab) {
			selectable = (((OWLPropertiesTab)tabWidget).getNestedSelectable());				
		} else if (tabWidget instanceof OWLIndividualsTab) {
			selectable = ((OWLIndividualsTab)tabWidget).getNestedSelectable();
		} else if (tabWidget instanceof AbstractTabWidget) {			
			selectable = (Selectable) ((AbstractTabWidget)tabWidget).getClsTree();
		}
			
		return (selectable == null ? null : selectable.getSelection());		
	}	
	
}
