package edu.stanford.smi.protege.collab.util;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class GetOntologyComponentFromServer extends ProtegeJob {
	private static final long serialVersionUID = -3635898210009873778L;
	
	private Frame frame;
	private boolean createOntologyComponent;

	public GetOntologyComponentFromServer(KnowledgeBase kb, Frame frame, boolean create) {
		super(kb);
		this.frame = frame;
		this.createOntologyComponent = create;
	}


	@Override
	public Ontology_Component run() throws ProtegeException {
		Ontology_Component oc = ServerChangesUtil.getOntologyComponent(ChAOKbManager.getChAOKb(getKnowledgeBase()),
				frame, createOntologyComponent);
		return 	oc;
	}

	/*
     * the result is localized against the wrong knowledgebase.
	 */
	@Override
	public Ontology_Component execute() {
	    Ontology_Component oc = (Ontology_Component) super.execute();
	    KnowledgeBase changekb = ChAOKbManager.getChAOKb(getKnowledgeBase());
	    LocalizeUtils.localize(oc, changekb);
	    return oc;
	}

	@Override
	public void localize(KnowledgeBase kb) {
		super.localize(kb);
		LocalizeUtils.localize(frame, kb);
	}

}
