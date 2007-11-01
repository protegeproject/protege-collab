package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.HashMap;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeCls;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;


public class OntologyComponentCache {
	private static HashMap<KnowledgeBase, ThreeValueCache> kb2cache = new HashMap<KnowledgeBase, ThreeValueCache>();
	    	
	public static Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }
  /*  
   //TT: This was the previous implementation. I don't remember why I have changed it.
	public static Ontology_Component getOntologyComponent_Good(Frame frame, boolean create) {
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(frame.getKnowledgeBase());
		
		if (changesKb == null) {
			return null;
		}
		
		ThreeValueCache cache = getCache(changesKb);
		String key = frame.getName();
		
		if (cache.hasCacheValue(key) && !create) {
			return (Ontology_Component) cache.getCacheValue(key);
		}

		Ontology_Component ontologyComp = getOrCreateOntologyComponent(frame, create);
		cache.putCacheValue(key, ontologyComp);		

		return ontologyComp;
	}
	*/
	
	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		String key = frame.getName();
		
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(frame.getKnowledgeBase());
		
		if (changesKb == null) {
			return null;
		}
		
		ThreeValueCache cache = getCache(changesKb);
		
		if (cache.hasCacheValue(key) && !create) {
			return (Ontology_Component) cache.getCacheValue(key);
		}

		Ontology_Component ontologyComp = null;
		
		try {
			GetOntologyComponentFromServer job = new GetOntologyComponentFromServer(frame.getKnowledgeBase(), frame, create);
			ontologyComp = (Ontology_Component) job.execute();
			// Hack!! should be fixed later
			if (ontologyComp != null) {
				ontologyComp.localize(ChangeOntologyUtil.getChangesKb(frame.getKnowledgeBase()));
			}
			
			cache.putCacheValue(key, ontologyComp);
		} catch (Throwable e) {
			Log.getLogger().warning("Errors at ontology component from server. Message: " + e.getMessage());
			e.printStackTrace();
		}

		return ontologyComp;
	}
	
	
	private static Ontology_Component getOrCreateOntologyComponent(Frame frame, boolean create) {
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(frame.getKnowledgeBase());
		
		Slot currentNameSlot = changesKb.getSlot(ChangeSlot.currentName.name());
		Collection<Frame> ontologyComponents = changesKb.getMatchingFrames(currentNameSlot, null, false, frame.getName(), 2);
				
		if (ontologyComponents.size() > 1) {
			Log.getLogger().warning("Found more than one ontology component for frame " + frame + " Ontology components: " + ontologyComponents);				
		}
		
		Ontology_Component ontologyComp = null;
		
		if (ontologyComponents.size() > 0) {
			ontologyComp = (Ontology_Component) CollectionUtilities.getFirstItem(ontologyComponents);
		}
		
		if (ontologyComp == null && create) {
			ChangeCls ontologyCompChangeClass = getOntologyComponentType(frame);
			Cls ontologyCompCls = changesKb.getCls(ontologyCompChangeClass.name());
			ontologyComp = (Ontology_Component) changesKb.createInstance(null, ontologyCompCls);
			
			ontologyComp.setCurrentName(frame.getName());			
		}
		
		return ontologyComp;
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
	
	//copied from ChangeDb
    private static ChangeCls getOntologyComponentType(Frame frame) {
        if (frame instanceof Cls) {
            return ChangeCls.Ontology_Class;
        }
        else if (frame instanceof Slot) {            
            return ChangeCls.Ontology_Property;
        }
        else {
        	return ChangeCls.Ontology_Individual;            
        }        
    }


}
