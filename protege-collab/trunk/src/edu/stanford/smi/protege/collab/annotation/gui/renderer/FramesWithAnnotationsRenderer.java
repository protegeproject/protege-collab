package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.FrameRenderer;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class FramesWithAnnotationsRenderer extends FrameRenderer {
	private KnowledgeBase changesKb = null;
	
	public FramesWithAnnotationsRenderer(KnowledgeBase changesKb) {
		this.changesKb = changesKb;
	}

	@Override
	protected void loadCls(final Cls cls) {
		super.loadCls(cls);
			
		if (!ChangeOntologyUtil.hasAnnotations(cls)) {
			return;
		}
		
		appendText(" ");
		appendIcon(AnnotationsIcons.getCommentIcon());		
	}
	
	@Override
	protected void loadSlot(Slot slot) {
		super.loadSlot(slot);
		
		if (!ChangeOntologyUtil.hasAnnotations(slot)) {
			return;
		}
		
		appendText(" ");
		appendIcon(AnnotationsIcons.getCommentIcon());		

	}
	
	@Override
	protected void loadInstance(Instance instance) {	
		super.loadInstance(instance);
		
		if (!ChangeOntologyUtil.hasAnnotations(instance)) {
			return;
		}
		
		appendText(" ");
		appendIcon(AnnotationsIcons.getCommentIcon());		
	}
	
}
