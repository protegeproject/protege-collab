package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;
import edu.stanford.smi.protegex.server_changes.model.generated.Proposal;
import edu.stanford.smi.protegex.server_changes.model.generated.Vote;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsRenderer extends FrameRenderer {

	@Override
	protected void loadInstance(Instance instance) {	
		super.loadInstance(instance);
		
		KnowledgeBase changesKb = instance.getKnowledgeBase();
		
		if (instance instanceof Change) {
			setMainIcon(AnnotationsIcons.getChangeAnnotationIcon());
		} else if (instance instanceof Ontology_Component) {
			setMainIcon(AnnotationsIcons.getOntologyAnnotationIcon());
		}else if (instance instanceof Vote) {
			setMainIcon(Icons.getYesIcon());
		}else if (instance instanceof Proposal) {
			setMainIcon(Icons.getCopyIcon());
		}else if (instance instanceof Annotation) {
			setMainIcon(AnnotationsIcons.getCommentIcon());
		} 
		
	}	
	
}
