package edu.stanford.smi.protege.collab.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;

import edu.stanford.smi.protegex.server_changes.ChangesDb;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeCls;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Composite_Change;
import edu.stanford.smi.protegex.server_changes.model.generated.FiveStarsVote;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;
import edu.stanford.smi.protegex.server_changes.model.generated.Vote;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangeOntologyUtil {
	
	/**
	 * This is used as a temporary cache for frames with annotations
	 */
	private static Collection<Frame> framesWithAnnotations = new ArrayList<Frame>();

	
    public static Collection<Annotation> getAnnotationInstances(Frame frame) {
    	ArrayList<Annotation> annotInstances = new ArrayList<Annotation>();
    	
		Ontology_Component ontologyComponent = getOntologyComponent(frame);
		
		if (ontologyComponent != null) {
			annotInstances.addAll(ontologyComponent.getAssociatedAnnotations());
		}

		return annotInstances;
	}

    
    public static Collection<Change> getChangeInstances(Frame frame) {
    	ArrayList<Change> changeInstances = new ArrayList<Change>();
    	
		Ontology_Component ontologyComponent = getOntologyComponent(frame);
		
		if (ontologyComponent != null) {
			changeInstances.addAll(ontologyComponent.getChanges());
		}

		return changeInstances;
	}
    
	
	public static Ontology_Component getOntologyComponent(Frame frame) {
		
		ChangesDb changesDb = ChangesProject.getChangesDb(frame.getKnowledgeBase());
			
		return changesDb.getOntologyComponent(frame, false);
	}


	public static Collection<Change> getTopLevelChangeInstances(Frame frame) {
		Collection<Change> allChanges = getChangeInstances(frame); 

		if (allChanges == null) {
			return null;
		}
		
		Collection<Annotation> toRemove = new ArrayList<Annotation>();

		for (Change changeInstance : allChanges) {
			if (changeInstance instanceof Composite_Change) {
				toRemove.addAll(((Composite_Change)changeInstance).getSubChanges());
			}
		}

		allChanges.removeAll(toRemove);

		return allChanges;
	}
	
	public static Collection<Annotation> getTopLevelAnnotationInstances(Frame frame) {
		Collection<Annotation> allAnnotations = getAnnotationInstances(frame); 

		if (allAnnotations == null) {
			return null;
		}
		
		Collection<Annotation> toRemove = new ArrayList<Annotation>();

		for (Annotation annotInstance : allAnnotations) {
			toRemove.addAll(annotInstance.getAssociatedAnnotations());
		}

		allAnnotations.removeAll(toRemove);

		return allAnnotations;
	}


	public static boolean hasAnnotations(Frame frame) {

		if (framesWithAnnotations.contains(frame)) {
			return true;
		}

		//this should be implemented more efficiently
		Collection annotations = getAnnotationInstances(frame);

		if ((annotations != null && annotations.size() > 0)) {
			framesWithAnnotations.add(frame);
			return true;
		}

		return false;
	}


	public static boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return (getChangesKb(kb) != null);
	}

	public static KnowledgeBase getChangesKb(KnowledgeBase kb) {
		return ChangesProject.getChangesDb(kb).getChangesKb();
	}

		
	public static Annotation createAnnotationOnAnnotation(KnowledgeBase kb, Frame annotatedFrame, AnnotationCls annotationType) {
		
		ChangesDb changesDb = ChangesProject.getChangesDb(kb);
		
		Annotation annotInst = (Annotation) changesDb.getModel().createInstance(annotationType);
		
		if (annotatedFrame == null) {
			return annotInst;
		}
		
		if (annotatedFrame instanceof AnnotatableThing) {
			annotInst.setAnnotates(CollectionUtilities.createCollection(annotatedFrame));
			return annotInst;
		}
		
		Ontology_Component ontologyComp = changesDb.getOntologyComponent(annotatedFrame, true);
		
		if (ontologyComp != null) {
			annotInst.setAnnotates(CollectionUtilities.createCollection(ontologyComp));
		}
		
		return annotInst;
	}
	
}
