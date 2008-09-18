package edu.stanford.smi.protege.collab.util;

import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ChangesProject;

public class GetOntologyComponentFromServer extends ProtegeJob {
	private Frame frame;
	private boolean createOntologyComponent;

	public GetOntologyComponentFromServer(KnowledgeBase kb, Frame frame, boolean create) {
		super(kb);
		this.frame = frame;
		this.createOntologyComponent = create;
	}


	@Override
	public Object run() throws ProtegeException {
		PostProcessorManager changesDb = ChangesProject.getChangesDb(getKnowledgeBase());
		Ontology_Component oc = changesDb.getOntologyComponent(frame, createOntologyComponent);
		return 	oc;
	}

	@Override
	public void localize(KnowledgeBase kb) {
		super.localize(kb);
		LocalizeUtils.localize(frame, kb);
	}

}
