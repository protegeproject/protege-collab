package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsComboBoxUtil {
	private KnowledgeBase changesKb;
	private static AnnotationsComboBoxUtil annotationsComboBoxUtil;
	
	private static ArrayList<AnnotationCls> allAnnotationTypes = new ArrayList<AnnotationCls>();
	private static Set<AnnotationCls> filteredOutAnnotationTypes = new HashSet<AnnotationCls>();

	
	private AnnotationsComboBoxUtil(KnowledgeBase changesKb) {
		this.changesKb = changesKb;
		initializeAllAnnoatationTypes();
	}
	
	private void initializeAllAnnoatationTypes() {
		ChangeModel model = new ChangeModel(changesKb);
		
		AnnotationCls[] annotationClses = AnnotationCls.values();
		
		for (int i = 0; i < annotationClses.length; i++) {
			AnnotationCls annotationCls = annotationClses[i];
			
			if (!model.getCls(annotationCls).isAbstract()) {
				allAnnotationTypes.add(annotationCls);
			}
		}
		
		filterOutUnneededAnnotationTypes();
	}

	private void filterOutUnneededAnnotationTypes() {
		//do this in a more general way, later
		filteredOutAnnotationTypes.add(AnnotationCls.FiveStarsVote);
		filteredOutAnnotationTypes.add(AnnotationCls.AgreeDisagreeVote);
		filteredOutAnnotationTypes.add(AnnotationCls.SimpleProposal);
		filteredOutAnnotationTypes.add(AnnotationCls.FiveStarsVoteProposal);
		filteredOutAnnotationTypes.add(AnnotationCls.AgreeDisagreeVoteProposal);
		
		allAnnotationTypes.removeAll(filteredOutAnnotationTypes);
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
		//ArrayList<AnnotationCls> allowableAnnotations = new ArrayList<AnnotationCls>(allAnnotationTypes);
		//return allowableAnnotations;
		return allAnnotationTypes;
	}
	
}
