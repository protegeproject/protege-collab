package edu.stanford.smi.protege.collab.gui.annotation;

import java.util.Date;

import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

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
		
		if (!(frame instanceof Instance)) {
			return;
		}
		
		Instance instance = (Instance) frame;
					
		KnowledgeBase changesKb = ChangesProject.getChangesKB(kb);
		Cls annotationCls = changesKb.getCls(ChangeCreateUtil.CLS_NAME_ANNOTATE);
		
		if (!instance.hasType(annotationCls)) {
			return;
		}
		
		Slot authorSlot = changesKb.getSlot(ChangeCreateUtil.SLOT_NAME_AUTHOR);
				
		instance.setOwnSlotValue(authorSlot, kb.getUserName());
		
		Slot creationDateSlot = changesKb.getSlot(ChangeCreateUtil.SLOT_NAME_CREATED);
		instance.setOwnSlotValue(creationDateSlot, (new Date()).toString());
		
	}
}
