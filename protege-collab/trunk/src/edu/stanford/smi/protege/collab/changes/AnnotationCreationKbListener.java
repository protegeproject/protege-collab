package edu.stanford.smi.protege.collab.changes;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;

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
		Annotation annotation = OntologyJavaMappingUtil.createObjectAs(ChAOKbManager.getChAOKb(kb), null, Annotation.class);
		new AnnotationFactory(ChAOKbManager.getChAOKb(kb)).fillDefaultValues(annotation);
	}

}
