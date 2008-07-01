package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;


public class HasAnnotationCache {
	private static HashMap<KnowledgeBase, ThreeValueCache> kb2cache = new HashMap<KnowledgeBase, ThreeValueCache>();
	
	public static boolean hasAnnotations(Frame frame) {
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(frame.getKnowledgeBase());		
		ThreeValueCache cache = getCache(changesKb);
		String key = frame.getName();

		
		if (cache.hasCacheValue(key)) {
			Boolean value = (Boolean) cache.getCacheValue(key); 
			return (value == null ? false: value.booleanValue());
		}
		
		Ontology_Component ontologyComponent = OntologyComponentCache.getOntologyComponent(frame);
		
		int valuesCount = 0;		
		
		if (ontologyComponent != null) {
			valuesCount = ontologyComponent.getOwnSlotValueCount(ontologyComponent.getKnowledgeBase().getSlot(ChangeSlot.associatedAnnotations.name()));
		}
		
		if (valuesCount == 0) {
			cache.putCacheValue(key, Boolean.FALSE);
			return false;
		} else {
			cache.putCacheValue(key, Boolean.TRUE);
			return true;
		}
		
	}
	
	public static void clearCache(KnowledgeBase kb) {
		ThreeValueCache cache = kb2cache.get(kb);
		
		if (cache != null) {
			cache.clearCache();
		}
		
	}

	public static ThreeValueCache getCache(KnowledgeBase kb) {
		ThreeValueCache cache = kb2cache.get(kb);
		
		if (cache == null) {
			cache = new ThreeValueCache();
			kb2cache.put(kb, cache);
		}
		
		return cache;
	}
}
