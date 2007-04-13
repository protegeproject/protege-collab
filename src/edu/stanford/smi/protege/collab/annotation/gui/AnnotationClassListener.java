package edu.stanford.smi.protege.collab.annotation.gui;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Timestamp;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationClassListener extends KnowledgeBaseAdapter {
	KnowledgeBase kb;
	
	public AnnotationClassListener(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	
	@Override
	public void instanceCreated(KnowledgeBaseEvent event) {
		fillAutoValue(event);	
	}

	private void fillAutoValue(KnowledgeBaseEvent event) {
		Frame frame = event.getFrame();
		
		if (!(frame instanceof Annotation)) {
			return;
		}
		
		Annotation annotation = (Annotation) frame;
		        
		//annotation.setAnnotates(annotatables);
		annotation.setCreated(Timestamp.getTimestamp(ChangeOntologyUtil.getChangeModel(kb)));
		annotation.setAuthor(kb.getUserName());
				
	}
}
