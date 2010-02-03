package edu.stanford.smi.protege.collab.util;

import edu.stanford.smi.protege.model.Project;

public class CollabProtegeConfiguration {
	
	public static final String SHOW_COLLAB_PANEL = "ShowCollabPanel";
	

	public static boolean getShowCollabPanel(Project prj) {
		Object show = prj.getClientInformation(SHOW_COLLAB_PANEL);
		
		if (show == null) {
			return false;
		}
		
		if (show instanceof Boolean) {
			return ((Boolean)show).booleanValue();
		}
		
		return false;
	}
	
	public static void setShowCollabPanel(Project prj, boolean show) {
		prj.setClientInformation(SHOW_COLLAB_PANEL, new Boolean(show));
	}

}
