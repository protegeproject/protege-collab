package edu.stanford.smi.protege.collab.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.util.AnnotatableThingComparator;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;


public class OntologyAnnotationsCache implements Disposable {
	private KnowledgeBase chaoKb;
	private AnnotationFactory factory;
	private KnowledgeBaseListener chaoKbListener;
	private FrameListener annotationListener;
	private Collection<Annotation> topOntologyAnnotations = new ArrayList<Annotation>();

	public OntologyAnnotationsCache(KnowledgeBase kb) {
		this.chaoKb = kb;
		this.factory = new AnnotationFactory(chaoKb);
		fillOntologyAnnotations();
		installListeners();
	}

	 private void installListeners() {
		 annotationListener = new FrameAdapter() {
			@Override
			public void ownSlotValueChanged(FrameEvent event) {
				Slot annotatesSlot = factory.getAnnotatesSlot();
				if (!event.getSlot().equals(annotatesSlot)) { return; }
				Frame frame = event.getFrame();
				if (frame.getOwnSlotValueCount(annotatesSlot) > 0) {
					topOntologyAnnotations.remove(new DefaultAnnotation((Instance) frame));
					frame.removeFrameListener(annotationListener);
				}
			}
		 };		 
		 
		 chaoKbListener = new KnowledgeBaseAdapter() {
			@Override
			public void instanceCreated(KnowledgeBaseEvent event) {
				Cls annotationCls = factory.getAnnotationClass();
				Instance inst = (Instance) event.getFrame();
				if (!inst.hasType(annotationCls)) { return; }
				//TODO: this might be tricky - we add things that we will remove later
				topOntologyAnnotations.add(factory.getAnnotation(inst.getName()));
				inst.addFrameListener(annotationListener);
			}
			
			@Override
			public void instanceDeleted(KnowledgeBaseEvent event) {
				Cls annotationCls = factory.getAnnotationClass();
				Instance inst = (Instance) event.getFrame();
				//if (!inst.hasType(annotationCls)) { return; }				
				topOntologyAnnotations.remove(new DefaultAnnotation(inst));
				inst.removeFrameListener(annotationListener);
			}
			
		};
		
		chaoKb.addKnowledgeBaseListener(chaoKbListener);
	}

	@SuppressWarnings("unchecked")
	public void fillOntologyAnnotations() {
		 topOntologyAnnotations.clear();
		 Collection<Annotation> topOntAnnots = null;
		 try {
			 topOntAnnots = (Collection<Annotation>) new GetTopOntologyAnnotations(chaoKb).execute();
		 } catch (Throwable t) {
			 Log.getLogger().log(Level.WARNING, "Could not get top ontology annotations from server", t);
			 return;
		 }		
		 topOntologyAnnotations.addAll(topOntAnnots);
	 }
	
	public Collection<Annotation> getTopOntologyAnnotations() {
		return topOntologyAnnotations;
	}
	
	
	static class GetTopOntologyAnnotations extends ProtegeJob {	
		private static final long serialVersionUID = -8544986624668899601L;

		public GetTopOntologyAnnotations(KnowledgeBase chaoKb) {
			super(chaoKb);
		}

		@Override
		public Object run() throws ProtegeException {
			List<Annotation> topAnnotations = new ArrayList<Annotation>();
			AnnotationFactory fact = new AnnotationFactory(getKnowledgeBase());
			Collection<Annotation> allAnnotations = fact.getAllAnnotationObjects(true);
			for (Annotation annotation : allAnnotations) {
				Collection<AnnotatableThing> annotates = annotation.getAnnotates();
				if (annotates == null || annotates.size() == 0) {
					topAnnotations.add(annotation);
				}
			}
			Collections.sort(topAnnotations, new AnnotatableThingComparator());
			return topAnnotations;
		}
	}
	
	public void dispose() {
		chaoKb.removeKnowledgeBaseListener(chaoKbListener);
		topOntologyAnnotations.clear();
	}

}
