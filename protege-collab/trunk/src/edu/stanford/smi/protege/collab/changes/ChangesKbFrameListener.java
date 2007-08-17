package edu.stanford.smi.protege.collab.changes;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyComponentCache;
import edu.stanford.smi.protege.collab.util.ThreeValueCache;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;

public class ChangesKbFrameListener extends FrameAdapter {

	@Override
	public void ownSlotValueChanged(FrameEvent event) {
		Frame frame = event.getFrame();
		Slot slot = event.getSlot();
		
		if (frame instanceof Annotation) {
			treatAnnotation(frame, slot);
		} else if (frame instanceof Ontology_Component) {
			treatOntologyComponent(frame, slot);
		}
	}
	
	
	private void treatOntologyComponent(Frame frame, Slot slot) {
		if (slot.getName().equals(ChangeSlot.currentName.name()) || slot.getName().equals(ChangeSlot.associatedAnnotations.name())) {
			updateCaches((Ontology_Component)frame);
		}		
	}


	private void treatAnnotation(Frame frame, Slot slot) {
		if (slot.getName().equals(ChangeSlot.annotates.name())){
			updateCaches((Annotation)frame);	
		}
	}


	private void updateCaches(Annotation annotation) {		
		Collection annotatesColl = annotation.getAnnotates();
		
		ThreeValueCache cache = HasAnnotationCache.getCache(annotation.getKnowledgeBase());
		
		for (Iterator iter = annotatesColl.iterator(); iter.hasNext();) {
			AnnotatableThing annotThing = (AnnotatableThing) iter.next();
			
			String key = null;	
			if (annotThing instanceof Ontology_Component) {
				key = ((Ontology_Component)annotThing).getCurrentName();
			} else if (annotThing instanceof Change) {
				Ontology_Component applyTo = (Ontology_Component)((Change)annotThing).getApplyTo();
				
				if (applyTo != null) {
					key = applyTo.getCurrentName();
				}
			}
			
			if (key != null) {
				//cache.removeCacheValue(key);
				cache.putCacheValue(key, Boolean.TRUE);
			}
		}		
	}	
	
	private void updateCaches(Ontology_Component ontoComp) {
					
		String currentName = ontoComp.getCurrentName();

		if (currentName == null) {
			//Log.getLogger().warning("Cannot find current name for " + ontoComp);
			return;			
		}

		ThreeValueCache cache =  OntologyComponentCache.getCache(ontoComp.getKnowledgeBase());
		cache.putCacheValue(currentName, ontoComp);

		ThreeValueCache annotationCache = HasAnnotationCache.getCache(ontoComp.getKnowledgeBase());
		annotationCache.removeCacheValue(currentName);	
	}
	
}
