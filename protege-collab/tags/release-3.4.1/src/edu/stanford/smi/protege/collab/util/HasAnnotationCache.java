package edu.stanford.smi.protege.collab.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;


/**
 * A simple cache that stores whether a frame has annotations or not.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class HasAnnotationCache {
	private static HashMap<Frame, Boolean> framesHasAnnotMap = new HashMap<Frame, Boolean>();
	
	public static boolean hasAnnotations(Frame frame) {
		if (framesHasAnnotMap.containsKey(frame)) {
			Boolean value = framesHasAnnotMap.get(frame);
			return value == null ? false : value.booleanValue();
		}
		
		return false;
		/*
		 * Following code is commented out, because we want to avoid
		 * going to the server for the renderer (main user of this cache).
		 * Instead we fill in the cache when collab protege starts up
		 * and then use a listener to maintain the cache.
		 * The code from below is not deleted, in case it needs to be
		 * revived later.
		 */
		/*
		Ontology_Component ontologyComponent = OntologyComponentCache.getOntologyComponent(frame);
		int valuesCount = 0;

		//TODO: make in protege job
		if (ontologyComponent != null) {
			Instance inst = ((AbstractWrappedInstance) ontologyComponent).getWrappedProtegeInstance();
			valuesCount = inst.getOwnSlotValueCount(new AnnotationFactory(inst.getKnowledgeBase()).getAssociatedAnnotationsSlot());
		}

		if (valuesCount == 0) {
			framesHasAnnotMap.put(frame, null);			
			return false;
		} else {
			framesHasAnnotMap.put(frame, Boolean.TRUE);			
			return true;
		}
		*/
	}

	public static void put(Frame frame, boolean hasAnnotation) {
		if (frame == null) { return; }
		framesHasAnnotMap.put(frame, new Boolean(hasAnnotation));
	}
	
	public static void clearCache() {
		framesHasAnnotMap.clear();
	}
	
	@SuppressWarnings("unchecked")
	public static void fillHasAnnotationCache(KnowledgeBase kb) {
		framesHasAnnotMap.clear();
		Set<String> framesWithAnnot = null;
		try {
			framesWithAnnot = (Set<String>) new GetFramesWithAnnotations(kb).execute();
		} catch (Throwable t) {
			Log.getLogger().log(Level.WARNING, "Could not retrieve frames with annotations from server", t);
			return;
		}
		
		for (String frameName : framesWithAnnot) {
			Frame frame = kb.getFrame(frameName);
			if (frame != null) {
				framesHasAnnotMap.put(frame, Boolean.TRUE);
			}
		}
	}
	
	static class GetFramesWithAnnotations extends ProtegeJob {		
		private static final long serialVersionUID = -8929614115737849773L;

		public GetFramesWithAnnotations(KnowledgeBase kb) {
			super(kb);
		}

		@Override
		public Object run() throws ProtegeException {
			Set<String> frames = new HashSet<String>();
			KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(getKnowledgeBase());
			if (chaoKb == null) { return frames; }
			OntologyComponentFactory ocf = new OntologyComponentFactory(chaoKb);
			Collection<Ontology_Component> ocs = ocf.getAllOntology_ComponentObjects(true);
			for (Ontology_Component oc : ocs) {
				Collection<Annotation> annots = oc.getAssociatedAnnotations();
				if (annots != null && annots.size() > 0) {
					String currentName = oc.getCurrentName();
					if (currentName != null) {
						frames.add(currentName);					
					}
				}
			}			
			return frames;
		}
		
	}
	
}
