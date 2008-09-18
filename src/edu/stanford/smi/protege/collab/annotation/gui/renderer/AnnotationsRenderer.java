package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.Proposal;
import edu.stanford.bmir.protegex.chao.annotation.api.Vote;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsRenderer extends FrameRenderer {

	@Override
	public void load(Object value) {
		//super.load(value);

		if (value instanceof Change) {
			setMainIcon(AnnotationsIcons.getChangeAnnotationIcon());
		} else if (value instanceof Ontology_Component) {
			setMainIcon(AnnotationsIcons.getOntologyAnnotationIcon());
		}else if (value instanceof Vote) {
			setMainIcon(Icons.getYesIcon());
		}else if (value instanceof Proposal) {
			setMainIcon(Icons.getCopyIcon());
		}else if (value instanceof Annotation) {
			setMainIcon(AnnotationsIcons.getCommentIcon());
		}

		if (value instanceof AbstractWrappedInstance) {
			setMainText(((AbstractWrappedInstance)value).getWrappedProtegeInstance().getBrowserText());
		}
	}

}
