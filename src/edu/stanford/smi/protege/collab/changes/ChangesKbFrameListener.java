package edu.stanford.smi.protege.collab.changes;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyComponentCache;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public class ChangesKbFrameListener extends FrameAdapter {
	
	private KnowledgeBase domainKb;
	
	public ChangesKbFrameListener(KnowledgeBase domainKb) {
		this.domainKb = domainKb;
	}
	

	@Override
	public void ownSlotValueChanged(FrameEvent event) {
		Frame frame = event.getFrame();
		Slot slot = event.getSlot();
		AnnotationFactory factory = new AnnotationFactory(frame.getKnowledgeBase());
		if (((Instance)frame).hasType(factory.getAnnotationClass()) && slot.equals(factory.getAnnotatesSlot())) {
			treatAnnotation(OntologyJavaMappingUtil.getSpecificObject(frame.getKnowledgeBase(), (Instance) frame, Annotation.class), slot);
		} else if (((Instance)frame).hasType(new OntologyComponentFactory(frame.getKnowledgeBase()).getOntology_ClassClass())) {
			treatOntologyComponent(OntologyJavaMappingUtil.getSpecificObject(frame.getKnowledgeBase(), (Instance) frame, Ontology_Component.class), slot);
		}
	}


	private void treatOntologyComponent(Ontology_Component ontComp, Slot slot) {
		AnnotationFactory factory = new AnnotationFactory(slot.getKnowledgeBase());
		if (slot.equals(factory.getCurrentNameSlot())) {
			updateCaches(ontComp, slot.getKnowledgeBase());
		}
	}

	private void treatAnnotation(Annotation annot, Slot slot) {
		AnnotationFactory factory = new AnnotationFactory(slot.getKnowledgeBase());
		if (slot.equals(factory.getAnnotatesSlot())){
			updateCaches(annot, slot.getKnowledgeBase());
		}
	}


	private void updateCaches(Annotation annotation, KnowledgeBase changesKb) {
		Collection<AnnotatableThing> annotatesColl = annotation.getAnnotates();

		for (Iterator<AnnotatableThing> iter = annotatesColl.iterator(); iter.hasNext();) {
			AnnotatableThing annotThing = iter.next();

			String key = null;
			if (annotThing.canAs(Ontology_Component.class)) {
				Ontology_Component oc = annotThing.as(Ontology_Component.class);
				key = oc.getCurrentName();
			} else if (annotThing.canAs(Change.class)) {
				Change c = annotThing.as(Change.class);
				Ontology_Component applyTo = c.getApplyTo();
				if (applyTo != null) {
					key = applyTo.getCurrentName();
				}
			}
			if (key != null) {
				HasAnnotationCache.put(domainKb.getFrame(key), true);
			}
		}
	}

	private void updateCaches(Ontology_Component ontoComp, KnowledgeBase changesKb) {
		String currentName = ontoComp.getCurrentName();

		if (currentName == null) {
			//Log.getLogger().warning("Cannot find current name for " + ontoComp);
			return;
		}

		Frame domainFrame = domainKb.getFrame(currentName);
		OntologyComponentCache.put(domainFrame, ontoComp);
		HasAnnotationCache.put(domainFrame, false);		
	}

}
