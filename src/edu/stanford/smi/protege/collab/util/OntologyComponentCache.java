package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.Log;


public class OntologyComponentCache {
	private static HashMap<Frame, Ontology_Component> frame2OntoCompMap = new HashMap<Frame, Ontology_Component>();

	public static Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }

	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		if (frame == null) { return null; }		
		if (frame2OntoCompMap.containsKey(frame) && !create) {
			return frame2OntoCompMap.get(frame);
		}
				
		Ontology_Component ontologyComp = null;
		try {
			GetOntologyComponentFromServer job = new GetOntologyComponentFromServer(frame.getKnowledgeBase(), frame, create);
			ontologyComp = job.execute();
			// Hack!! should be fixed later
			if (ontologyComp != null) {
				Instance inst = ((AbstractWrappedInstance)ontologyComp).getWrappedProtegeInstance();
				inst.getFrameID().localize(ChAOUtil.getChangesKb(frame.getKnowledgeBase()));
			}

			frame2OntoCompMap.put(frame, ontologyComp);
		} catch (Throwable e) {
			Log.getLogger().warning("Errors at ontology component from server. Message: " + e.getMessage());
			e.printStackTrace();
		}
		return ontologyComp;
	}
	
	public static void put(Frame frame, Ontology_Component ontComp) {		
		if (frame == null) { return;}
		frame2OntoCompMap.put(frame, ontComp);
	}

	public static void clearCache() {
		frame2OntoCompMap.clear();
	}
	
}
