package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.bmir.protegex.chao.annotation.api.AgreeDisagreeVote;
import edu.stanford.bmir.protegex.chao.annotation.api.AgreeDisagreeVoteProposal;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.FiveStarsVoteProposal;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Disposable;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsComboBoxUtil implements Disposable {
	private KnowledgeBase changesKb;
	private ArrayList<Cls> allAnnotationTypes = new ArrayList<Cls>();
	private Set<Cls> filteredOutAnnotationTypes = new HashSet<Cls>();

	public AnnotationsComboBoxUtil(KnowledgeBase changesKb) {
		this.changesKb = changesKb;
		initializeAllAnnoatationTypes();
	}

	private void initializeAllAnnoatationTypes() {
		AnnotationFactory factory = new AnnotationFactory(changesKb);
		Cls annotCls = factory.getAnnotationClass();
		for (Object obj : annotCls.getSubclasses()) {
			Cls annotSubCls = (Cls) obj;
			if (!annotSubCls.isAbstract()) {
				allAnnotationTypes.add(annotSubCls);
			}
		}
		filterOutUnneededAnnotationTypes();
	}

	private void filterOutUnneededAnnotationTypes() {
		AnnotationFactory factory = new AnnotationFactory(changesKb);
		filteredOutAnnotationTypes.add(factory.getSimpleProposalClass());
		allAnnotationTypes.removeAll(filteredOutAnnotationTypes);
	}


	//TT: probably we should cache this
	public Collection<Cls> getAllowableAnnotationTypes(AnnotatableThing thing) {
		ArrayList<Cls> allowableAnnotations = new ArrayList<Cls>(allAnnotationTypes);
		AnnotationFactory factory = new AnnotationFactory(changesKb);

		if (thing == null) { //all except votes
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteClass());
			return allowableAnnotations;
		}

		//TODO: these rules should not be hard-coded
		if (OntologyJavaMappingUtil.canAs(thing, FiveStarsVoteProposal.class)) {
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteClass());
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteProposalClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteProposalClass());
		} else if (OntologyJavaMappingUtil.canAs(thing, AgreeDisagreeVoteProposal.class)) {
			allowableAnnotations.remove(factory.getFiveStarsVoteClass());
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteProposalClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteProposalClass());
		} else if (OntologyJavaMappingUtil.canAs(thing, AgreeDisagreeVote.class) ||
				OntologyJavaMappingUtil.canAs(thing, FiveStarsVoteProposal.class)) {
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteClass());
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteProposalClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteProposalClass());
		} else { //remove by default the votes
			allowableAnnotations.remove(factory.getAgreeDisagreeVoteClass());
			allowableAnnotations.remove(factory.getFiveStarsVoteClass());
		}
		return allowableAnnotations;
	}

	public void dispose() {
		allAnnotationTypes.clear();
		filteredOutAnnotationTypes.clear();		
		changesKb = null;
	}
	
}
