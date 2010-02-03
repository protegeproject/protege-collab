package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Disposable;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class StatusComboBoxUtil implements Disposable {
	
	public static final String ARCHIVED_PROP_NAME = "Archived";	
	
	private KnowledgeBase changesKb;
	private List<Instance> allStatus = new ArrayList<Instance>();

	public StatusComboBoxUtil(KnowledgeBase changesKb) {
		this.changesKb = changesKb;
		initializeAllStatus();
	}

	private void initializeAllStatus() {
		AnnotationFactory factory = new AnnotationFactory(changesKb);
		Cls statusCls = factory.getStatusClass();
		if (statusCls != null) {
			allStatus.addAll(statusCls.getInstances());
		}
	}


	public Collection<Instance> getAllStatus(AnnotatableThing thing) {
		return allStatus;
	}

	public void dispose() {
		allStatus.clear();	
		changesKb = null;
	}

	/*
	 * Util methods
	 */
	
	public static void setHideArchived(Project project, boolean show) {
		project.setClientInformation(ConfigureOptionsTabPanel.HIDE_ARCHIVED, Boolean.toString(show));
	}

	public static boolean getHideArchived(Project project) {
		String hide = (String) project.getClientInformation(ConfigureOptionsTabPanel.HIDE_ARCHIVED);
		return hide == null ? false : hide.equalsIgnoreCase("false") ? false : true;
	}
	
}
