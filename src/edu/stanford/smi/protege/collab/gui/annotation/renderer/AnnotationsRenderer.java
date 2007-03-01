package edu.stanford.smi.protege.collab.gui.annotation.renderer;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.collab.gui.annotation.AnnotationsIcons;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.server_changes.model.Model;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsRenderer extends FrameRenderer {

	@Override
	protected void loadInstance(Instance instance) {	
		super.loadInstance(instance);
		
		KnowledgeBase changesKb = instance.getKnowledgeBase();
		
		//cache this later
		Cls annotationCls = changesKb.getCls(Model.CLS_NAME_ANNOTATE);
		Cls changeCls = changesKb.getCls(Model.CLS_NAME_CHANGE);
		Cls ontologyCompCls = changesKb.getCls(ChangeOntologyUtil.CLS_NAME_ONTOLOGY_COMPONENT);
		Cls voteCls = changesKb.getCls(ChangeOntologyUtil.CLS_NAME_VOTE);
		Cls proposalCls = changesKb.getCls(ChangeOntologyUtil.CLS_NAME_PROPOSAL);
		
		if (instance.hasType(changeCls)) {
			setMainIcon(AnnotationsIcons.getChangeAnnotationIcon());
		} else if (instance.hasType(ontologyCompCls)) {
			setMainIcon(AnnotationsIcons.getOntologyAnnotationIcon());
		}else if (instance.hasType(voteCls)) {
			setMainIcon(Icons.getYesIcon());
		}else if (instance.hasType(proposalCls)) {
			setMainIcon(Icons.getCopyIcon());
		}else if (instance.hasType(annotationCls)) {
			setMainIcon(AnnotationsIcons.getCommentIcon());
		} 
		
	}
	
	
}
