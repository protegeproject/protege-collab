package edu.stanford.smi.protege.collab.changes;

import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.server_changes.OntologyComponentCache;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

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
        if (((Instance) frame).hasType(factory.getAnnotationClass())) {
        	Annotation annotation = OntologyJavaMappingUtil.getSpecificObject(frame.getKnowledgeBase(), (Instance) frame, Annotation.class);
        	if (slot.equals(factory.getAnnotatesSlot())) {        
        		treatAnnotation(annotation, slot);
        	} else if (slot.equals(factory.getArchivedSlot())) {
        		treatArchived(annotation);
        	}
        } else if (((Instance) frame).hasType(new OntologyComponentFactory(frame.getKnowledgeBase())
                .getOntology_ClassClass())) {
            treatOntologyComponent(OntologyJavaMappingUtil.getSpecificObject(frame.getKnowledgeBase(),
                    (Instance) frame, Ontology_Component.class), slot);
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
        if (slot.equals(factory.getAnnotatesSlot())) {
            updateCaches(annot, slot.getKnowledgeBase());
        }
    }


    private void treatArchived(Annotation annotation) {
    	HasAnnotationCache.archiveStatusChanged(domainKb.getProject(), annotation, ServerChangesUtil.getAnnotatedOntologyComponents(annotation));		
	}
    
    private void updateCaches(Annotation annotation, KnowledgeBase changesKb) {
        Collection<AnnotatableThing> annotatesColl = annotation.getAnnotates();

        for (AnnotatableThing annotThing : annotatesColl) {
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
            } else if (annotThing.canAs(Annotation.class)) {
                //go up the annotation tree and increase counter for annotation in cache for the ontology component
                handleAnnotationAdded(annotThing.as(Annotation.class));
            }
            if (key != null) {
                HasAnnotationCache.addAnnotation(domainKb.getFrame(key));
            }
        }
    }

    private void handleAnnotationAdded(Annotation annotation) {       
        Collection<Ontology_Component> annotatedOcs = ServerChangesUtil.getAnnotatedOntologyComponents(annotation);
        for (Ontology_Component oc : annotatedOcs) {
            String key = oc.getCurrentName();
            if (key != null) {
                HasAnnotationCache.addAnnotation(domainKb.getFrame(key));
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
    }

}
