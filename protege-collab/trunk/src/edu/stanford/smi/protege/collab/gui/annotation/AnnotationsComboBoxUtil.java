package edu.stanford.smi.protege.collab.gui.annotation;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;

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
	
	public Collection getAllowableAnnotations(Instance annotationInstance) {
		Cls annotationCls = changesKb.getCls(ChangeCreateUtil.CLS_NAME_ANNOTATE);
		
		return annotationCls.getSubclasses();
	}
	
	
}
