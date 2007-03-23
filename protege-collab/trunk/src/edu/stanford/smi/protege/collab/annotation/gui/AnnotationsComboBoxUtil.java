package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsComboBoxUtil {
	private KnowledgeBase changesKb;
	private static AnnotationsComboBoxUtil annotationsComboBoxUtil;
	
	private AnnotationsComboBoxUtil(KnowledgeBase changesKb) {
		this.changesKb = changesKb;
	}
	
	//Wrong! Change me!
	public static AnnotationsComboBoxUtil getAnnotationsComboBoxUtil(KnowledgeBase changesKb){
		if (annotationsComboBoxUtil == null) {
			annotationsComboBoxUtil = new AnnotationsComboBoxUtil(changesKb);
		}
		
		return annotationsComboBoxUtil;
	}
	
	
	//TT: probably we should cache this
	public Collection<AnnotationCls> getAllowableAnnotationTypes(Instance annotationInstance) {
		ArrayList<AnnotationCls> allowableAnnotations = new ArrayList<AnnotationCls>();
		
		for (int i = 0; i < AnnotationCls.values().length; i++) {
			allowableAnnotations.add(AnnotationCls.values()[i]);
		}
		
		return allowableAnnotations;
	}
	
	
}
