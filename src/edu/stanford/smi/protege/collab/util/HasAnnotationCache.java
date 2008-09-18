package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;


public class HasAnnotationCache {
	private static HashMap<KnowledgeBase, ThreeValueCache<String>> kb2cache = new HashMap<KnowledgeBase, ThreeValueCache<String>>();

	public static boolean hasAnnotations(Frame frame) {
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(frame.getKnowledgeBase());
		ThreeValueCache<String> cache = getCache(changesKb);
		String key = frame.getName();

		if (cache.hasCacheValue(key)) {
			Boolean value = (Boolean) cache.getCacheValue(key);
			return value == null ? false: value.booleanValue();
		}

		Ontology_Component ontologyComponent = OntologyComponentCache.getOntologyComponent(frame);
		int valuesCount = 0;

		if (ontologyComponent != null) {
			Instance inst = ((AbstractWrappedInstance) ontologyComponent).getWrappedProtegeInstance();
			valuesCount = inst.getOwnSlotValueCount(new AnnotationFactory(inst.getKnowledgeBase()).getAssociatedAnnotationsSlot());
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
		ThreeValueCache<String> cache = kb2cache.get(kb);
		if (cache != null) {
			cache.clearCache();
		}
	}

	public static ThreeValueCache<String> getCache(KnowledgeBase kb) {
		ThreeValueCache<String> cache = kb2cache.get(kb);
		if (cache == null) {
			cache = new ThreeValueCache<String>();
			kb2cache.put(kb, cache);
		}
		return cache;
	}
}
