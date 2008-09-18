package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.HashMap;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;


public class OntologyComponentCache {
	private static HashMap<KnowledgeBase, ThreeValueCache<String>> kb2cache = new HashMap<KnowledgeBase, ThreeValueCache<String>>();

	public static Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }

	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		if (frame == null) {
			return null;
		}
		String key = frame.getName();
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(frame.getKnowledgeBase());
		if (changesKb == null) {
			return null;
		}

		ThreeValueCache<String> cache = getCache(changesKb);

		if (cache.hasCacheValue(key) && !create) {
			return (Ontology_Component) cache.getCacheValue(key);
		}

		Ontology_Component ontologyComp = null;
		try {
			GetOntologyComponentFromServer job = new GetOntologyComponentFromServer(frame.getKnowledgeBase(), frame, create);
			ontologyComp = (Ontology_Component) job.execute();
			// Hack!! should be fixed later
			if (ontologyComp != null) {
				Instance inst = ((AbstractWrappedInstance)ontologyComp).getWrappedProtegeInstance();
				inst.getFrameID().localize(ChAOUtil.getChangesKb(frame.getKnowledgeBase()));
			}

			cache.putCacheValue(key, ontologyComp);
		} catch (Throwable e) {
			Log.getLogger().warning("Errors at ontology component from server. Message: " + e.getMessage());
			e.printStackTrace();
		}
		return ontologyComp;
	}


	private static Ontology_Component getOrCreateOntologyComponent(Frame frame, boolean create) {
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(frame.getKnowledgeBase());

		Slot currentNameSlot = new AnnotationFactory(changesKb).getCurrentNameSlot();
		Collection<Frame> ontologyComponents = changesKb.getMatchingFrames(currentNameSlot, null, false, frame.getName(), 2);

		if (ontologyComponents.size() > 1) {
			Log.getLogger().warning("Found more than one ontology component for frame " + frame + " Ontology components: " + ontologyComponents);
		}

		Ontology_Component ontologyComp = null;

		if (ontologyComponents.size() > 0) {
			ontologyComp = (Ontology_Component) CollectionUtilities.getFirstItem(ontologyComponents);
		}

		if (ontologyComp == null && create) {
			ontologyComp = ServerChangesUtil.getOntologyComponent(changesKb, frame, true);
		}

		return ontologyComp;
	}

	public static void clearCache(KnowledgeBase kb) {
		ThreeValueCache cache = kb2cache.get(kb);

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
