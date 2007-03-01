package edu.stanford.smi.protege.collab.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;

import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.model.Model;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangeOntologyUtil {

	//move this somewhere
	public final static String CLS_NAME_ONTOLOGY_COMPONENT = "Ontology_Component"; 
	public final static String CLS_NAME_COMMENT = "Comment"; 
	public final static String CLS_NAME_DISCUSSION_THREAD = "DiscussionThread";
	public final static String CLS_NAME_VOTE = "Vote";
	public final static String CLS_NAME_VOTE_5_STAR = "FiveStarsVote";
	public final static String CLS_NAME_PROPOSAL = "Proposal";
	
	public final static String SLOT_NAME_VOTE= "votes";
	public final static String SLOT_NAME_VOTE_VALUE= "voteValue";
	public final static String SLOT_NAME_PROPOSAL= "proposal";
	public final static String SLOT_NAME_SUBJECT= "subject";
	
	/**
	 * This is used as a temporary cache for frames with annotations
	 */
	private static Collection<String> framesWithAnnotations = new ArrayList<String>();

	public static Collection<Frame> getAnnotationInstances(KnowledgeBase changesKb, String frameName) {
		return getAnnotationInstances(changesKb, frameName, null);
	}

	public static Collection<Frame> getAnnotationInstances(KnowledgeBase changesKb, String frameName, Cls typeFilter) {
		return getAnnotationInstances(changesKb, frameName, typeFilter, true);
	}
	
	//The includOldValues should not be implemented here, but in the change ontology!!!
	//This is just a temporary implementation, until the changes ontology will solve this
	public static Collection<Frame> getAnnotationInstances(KnowledgeBase changesKb, String frameName, Cls typeFilter, boolean includeOldValues) {
		//not efficient implementation. Maybe find a better one

		if (changesKb == null) {
			return null;
		}

		Slot applyToSlot = changesKb.getSlot(Model.SLOT_NAME_APPLYTO);

		//Or browsing text?
		Collection<Frame> annotationInstances; 
		
		//beautify this
		if (frameName != null) {
			annotationInstances = changesKb.getMatchingFrames(applyToSlot, null, false, frameName, 1000);
		} else {
			if (typeFilter == null) {
				return new ArrayList<Frame>();
			} else {
				annotationInstances = typeFilter.getInstances();
			}
		}
		
		if (includeOldValues) {
			for (String oldName : getOldValues(changesKb, frameName)) {
				Collection<Frame> oldAnnotationInstances = changesKb.getMatchingFrames(applyToSlot, null, false, oldName, 1000);
				 
				if (oldAnnotationInstances != null) {
					annotationInstances.addAll(oldAnnotationInstances);
				}
			}			
		}

		//If there is a class filter, remove the unwanted classes
		if (typeFilter != null) {
			for (Iterator iter = annotationInstances.iterator(); iter.hasNext();) {
				Instance inst = (Instance) iter.next();
				if (!((Instance)inst).hasType(typeFilter)) {
					iter.remove();
				}				
			}			
		}

		return annotationInstances;
	}


	public static Collection<Frame> getTopLevelAnnotationInstances(KnowledgeBase changesKb, String frameName) {
		return getTopLevelAnnotationInstances(changesKb, frameName, null);
	}

	
	public static Collection<Frame> getTopLevelAnnotationInstances(KnowledgeBase changesKb, String frameName, Cls typeFilter) {
		return getTopLevelAnnotationInstances(changesKb, frameName, typeFilter, true);
	}
	
	
	public static Collection<Frame> getTopLevelAnnotationInstances(KnowledgeBase changesKb, String frameName, Cls typeFilter, boolean includeOldValues) {
		Collection<Frame> allAnnotations = getAnnotationInstances(changesKb, frameName, typeFilter); 

		if (allAnnotations == null) {
			return null;
		}

		Cls transactionCls = changesKb.getCls(Model.CHANGETYPE_TRANS_CHANGE);
		Slot changesSlot = changesKb.getSlot(Model.SLOT_NAME_CHANGES);
		Slot assocAnnotSlot = changesKb.getSlot(Model.SLOT_NAME_ASSOC_ANNOTATIONS);

		if (transactionCls == null || changesSlot == null) {
			return allAnnotations;
		}		

		Collection toRemove = new ArrayList<Frame>();

		for (Frame annotInstance : allAnnotations) {
			//if (((Instance)annotInstance).hasType(transactionCls)) {
				Collection atomicChangeAnnotations = annotInstance.getOwnSlotValues(changesSlot);
				Collection atomicOtherAnnotations = annotInstance.getOwnSlotValues(assocAnnotSlot);
				toRemove.addAll(atomicChangeAnnotations);
				toRemove.addAll(atomicOtherAnnotations);
			//}
		}

		allAnnotations.removeAll(toRemove);

		return allAnnotations;
	}


	
	private static Collection<String> getOldValues(KnowledgeBase changesKb, String frameName) {
		if (frameName == null) {
			return new ArrayList<String>();
		}
	
		Slot newNameSlot = changesKb.getSlot(Model.SLOT_NAME_NEWNAME);
		Collection<Frame> nameChangeInstances = changesKb.getMatchingFrames(newNameSlot, null, false, frameName, 1000);  

		Slot oldNameSlot = changesKb.getSlot(Model.SLOT_NAME_OLDNAME);

		ArrayList<String> oldValues = new ArrayList<String>();

		for (Frame f : nameChangeInstances) {
			String value = (String) f.getOwnSlotValue(oldNameSlot);

			if (value != null) {
				oldValues.add(value);
			}
		}

		return oldValues;
	}
	

	public static boolean hasAnnotations(KnowledgeBase changesKb, String frameName) {

		if (framesWithAnnotations.contains(frameName)) {
			return true;
		}

		//this can be implemented more efficiently
		Collection annotations = getAnnotations(changesKb, frameName);

		if ((annotations != null && annotations.size() > 0)) {
			framesWithAnnotations.add(frameName);
			return true;
		}

		return false;
	}


	public static Collection getAnnotations(KnowledgeBase changesKb, String frameName) {
		if (changesKb == null) {
			return new ArrayList();
		}

		Collection<Frame> topLevelAnnotations = getTopLevelAnnotationInstances(changesKb, frameName);

		if (topLevelAnnotations == null || topLevelAnnotations.size() == 0) {
			return new ArrayList();
		}

		Collection annotations = new ArrayList();

		for (Frame changeInstance : topLevelAnnotations) {

			annotations.addAll(getAnnotationsForChange(changeInstance));

		}

		return annotations;
	}


	private static Collection getAnnotationsForChange(Frame changeInst) {
		ArrayList annotatations = new ArrayList();


		Slot associatedAnnotationSlot = changeInst.getKnowledgeBase().getSlot(Model.SLOT_NAME_ASSOC_ANNOTATIONS);

		Slot bodySlot = changeInst.getKnowledgeBase().getSlot(Model.SLOT_NAME_BODY);

		for (Object annotInstance : changeInst.getOwnSlotValues(associatedAnnotationSlot)) {
			annotatations.addAll(((Instance)annotInstance).getOwnSlotValues(bodySlot));
		}

		return annotatations;
	}

	public static Instance createOntologyComponentInstance(KnowledgeBase kb) {
		return createOntologyComponentInstance(kb, null);
	}

	public static Instance createOntologyComponentInstance(KnowledgeBase kb, String appliedToName) {
		return createOntologyComponentInstance(kb, appliedToName, null);
	}

	public static Instance createOntologyComponentInstance(KnowledgeBase kb, String appliedToName, String annotationBody) {
		return createOntologyComponentInstance(kb, appliedToName, annotationBody, null);
	}

	public static Instance createOntologyComponentInstance(KnowledgeBase kb, String appliedToName, String annotationBody, Cls annotationCls) {
		KnowledgeBase changesKb = ChangesProject.getChangesKB(kb); 
			
		Cls ontologyComponentCls = changesKb.getCls(CLS_NAME_ONTOLOGY_COMPONENT);

		if (ontologyComponentCls == null) {
			return null;
		}

		Instance ontoCompInst = ontologyComponentCls.createDirectInstance(null);

		if (appliedToName != null) {
			Slot appliedToSlot = changesKb.getSlot(Model.SLOT_NAME_APPLYTO);

			if (appliedToSlot != null) {
				ontoCompInst.setOwnSlotValue(appliedToSlot, appliedToName);
			}			
		}

		if (annotationCls == null) {
			annotationCls = changesKb.getCls(CLS_NAME_COMMENT);
		}

		Cls annotationParentCls =changesKb.getCls(Model.CLS_NAME_ANNOTATE);

		if (annotationCls.hasSuperclass(annotationParentCls)) {
			Instance annotationInstance = annotationCls.createDirectInstance(null);
			Slot descriptionSlot = changesKb.getSlot(Model.SLOT_NAME_BODY);

			if (annotationBody != null) {
				annotationInstance.addOwnSlotValue(descriptionSlot, annotationBody);
			}

			Slot assocAnnotSlot = changesKb.getSlot(Model.SLOT_NAME_ASSOC_ANNOTATIONS);

			ontoCompInst.addOwnSlotValue(assocAnnotSlot, annotationInstance);
		}

		return ontoCompInst;

	}

	
	public static Collection getTopLevelOntologyComponentAnnotations(KnowledgeBase kb, String frameName) {
		KnowledgeBase changesKb = ChangesProject.getChangesKB(kb);
		
		Cls ontologyCompCls = changesKb.getCls(ChangeOntologyUtil.CLS_NAME_ONTOLOGY_COMPONENT);
		//check why this is invoked twice
		Collection<Frame> annotationsRoots = ChangeOntologyUtil.getTopLevelAnnotationInstances(changesKb, frameName, ontologyCompCls);

		Slot assocAnnotSlot = changesKb.getSlot(Model.SLOT_NAME_ASSOC_ANNOTATIONS);
		
		Collection<Frame> ontologyComponentAnnotations = new ArrayList<Frame>();
		
		for (Frame frame : annotationsRoots) {
			ontologyComponentAnnotations.addAll(frame.getOwnSlotValues(assocAnnotSlot));
		}
		
		
		Cls transactionCls = changesKb.getCls(Model.CHANGETYPE_TRANS_CHANGE);
		Slot changesSlot = changesKb.getSlot(Model.SLOT_NAME_CHANGES);


		Collection toRemove = new ArrayList<Frame>();

		for (Frame annotInstance : ontologyComponentAnnotations) {
			//if (((Instance)annotInstance).hasType(transactionCls)) {
				Collection atomicChangeAnnotations = annotInstance.getOwnSlotValues(changesSlot);
				Collection atomicOtherAnnotations = annotInstance.getOwnSlotValues(assocAnnotSlot);
				toRemove.addAll(atomicChangeAnnotations);
				toRemove.addAll(atomicOtherAnnotations);
			//}
		}

		ontologyComponentAnnotations.removeAll(toRemove);

		
		
		return ontologyComponentAnnotations;
	}
	

	public static KnowledgeBase getChangesKB(KnowledgeBase kb) {
		return ChangesProject.getChangesKB(kb);
	}

	//Change this when ChangesTa will de-statify	
	public static boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return (getChangesKB(kb) != null);
	}

	//move it somewhere or rename
	
	public static Collection getDiscussionThreadAnnotations(KnowledgeBase kb) {
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKB(kb);
		
		Cls annCls = changesKb.getCls(Model.CLS_NAME_ANNOTATE);
		Slot annotationSlot = changesKb.getSlot(Model.SLOT_NAME_ANNOTATES);
		
		ArrayList<Instance> discThreadAnns = new ArrayList<Instance>();
		
		for (Iterator iter = annCls.getInstances().iterator(); iter.hasNext();) {
			Instance annInstance = (Instance) iter.next();
			
			Collection values = annInstance.getOwnSlotValues(annotationSlot);
			
			if (values == null || values.size() == 0) {
				discThreadAnns.add(annInstance);
			}			
		}
		
		return discThreadAnns;
	}


}
