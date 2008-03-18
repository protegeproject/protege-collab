package edu.stanford.smi.protege.collab.changes;

import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.collab.util.CacheManager;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.ThreeValueCache;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;
import edu.stanford.smi.protegex.server_changes.model.generated.Timestamp;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationCreationKbListener extends KnowledgeBaseAdapter {
	private KnowledgeBase kb;
	
	public AnnotationCreationKbListener(KnowledgeBase kb) {
		this.kb = kb;	
	}	
	
	@Override
	public void instanceCreated(KnowledgeBaseEvent event) {
		Frame frame = event.getFrame();

		if (!(frame instanceof Annotation)) {
			return;
		}
		
		fillAutoValue(frame);	
	}

	private void fillAutoValue(Frame frame) {
		Annotation annotation = (Annotation) frame;
		annotation.setCreated(Timestamp.getTimestamp(ChangeOntologyUtil.getChangeModel(kb)));
		annotation.setAuthor(kb.getUserName());				
	}

}
