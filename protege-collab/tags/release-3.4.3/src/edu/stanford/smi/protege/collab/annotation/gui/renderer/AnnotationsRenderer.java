package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Advice;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.Comment;
import edu.stanford.bmir.protegex.chao.annotation.api.Example;
import edu.stanford.bmir.protegex.chao.annotation.api.Explanation;
import edu.stanford.bmir.protegex.chao.annotation.api.Proposal;
import edu.stanford.bmir.protegex.chao.annotation.api.Question;
import edu.stanford.bmir.protegex.chao.annotation.api.SeeAlso;
import edu.stanford.bmir.protegex.chao.annotation.api.Vote;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Class;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Individual;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.Disposable;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsRenderer extends FrameRenderer implements Disposable {

	private KnowledgeBase kb;
	private KnowledgeBase chaoKb;
	private AnnotationFactory factory;

	public AnnotationsRenderer(KnowledgeBase kb) {
		this.kb = kb;
		this.chaoKb = ChAOKbManager.getChAOKb(kb);
		this.factory = new AnnotationFactory(chaoKb);
	}

	@Override
	public void load(Object value) {
		if (!(value instanceof AbstractWrappedInstance)) {
			super.load(value);
			return;
		}

		AbstractWrappedInstance inst = (AbstractWrappedInstance) value;

		if (inst.canAs(Change.class)) {
			setMainIcon(AnnotationsIcons.getChangeAnnotationIcon());
		}else if (inst.canAs(Ontology_Component.class)) {
			renderOntologyComponent(inst.as(Ontology_Component.class));
			return;
		}else if (inst.canAs(Vote.class)) {
			setMainIcon(AnnotationsIcons.getIcon("v"));
		}else if (inst.canAs(Proposal.class)) {
			setMainIcon(AnnotationsIcons.getIcon("p"));
		}else if (inst.canAs(Explanation.class)) {
			setMainIcon(AnnotationsIcons.getIcon("e"));
		}else if (inst.canAs(Advice.class)) {
			setMainIcon(AnnotationsIcons.getIcon("a"));
		}else if (inst.canAs(Example.class)) {
			setMainIcon(AnnotationsIcons.getIcon("e_small"));
		}else if (inst.canAs(SeeAlso.class)) {
			setMainIcon(AnnotationsIcons.getIcon("s"));
		}else if (inst.canAs(Comment.class)) {
			setMainIcon(AnnotationsIcons.getIcon("c"));
		}else if (inst.canAs(Question.class)) {
			setMainIcon(AnnotationsIcons.getIcon("q"));
		}else if (inst.canAs(Annotation.class)) {
			setMainIcon(AnnotationsIcons.getCommentIcon());
		}

		setMainText(inst.getWrappedProtegeInstance().getBrowserText());
	}

	protected void renderOntologyComponent(Ontology_Component oComp) {
		if (oComp.canAs(Ontology_Class.class)) {
			setMainIcon(Icons.getClsIcon());
		} else if (oComp.canAs(Ontology_Property.class)) {
			setMainIcon(Icons.getSlotIcon());
		} else if (oComp.canAs(Ontology_Individual.class)) {
			setMainIcon(Icons.getInstanceIcon());
		} else {
			setMainIcon(AnnotationsIcons.getOntologyAnnotationIcon());	
		}
		
		String name = oComp.getCurrentName();
		if (name == null) {
			setMainText(((AbstractWrappedInstance)oComp).getWrappedProtegeInstance().getBrowserText());
		} else {
			Frame frame = kb.getFrame(name);
			if (frame != null) {
				setMainText(frame.getBrowserText());
			} else {
				setMainText(name + " (invalid)");
			}
		}				
	}
	
	
	@Override
	protected void loadCls(Cls cls) {
		if (cls.equals(factory.getExplanationClass())) {
			setMainIcon(AnnotationsIcons.getIcon("e"));
			setMainText(cls.getBrowserText());
		} else if (cls.equals(factory.getAdviceClass())) {
			setMainIcon(AnnotationsIcons.getIcon("a"));
			setMainText(cls.getBrowserText());
		} else if (cls.equals(factory.getExampleClass())) {
			setMainIcon(AnnotationsIcons.getIcon("e_small"));
			setMainText(cls.getBrowserText());
		} else if (cls.equals(factory.getSeeAlsoClass())) {
			setMainIcon(AnnotationsIcons.getIcon("s"));
			setMainText(cls.getBrowserText());
		} else if (cls.equals(factory.getCommentClass())) {
			setMainIcon(AnnotationsIcons.getIcon("c"));
			setMainText(cls.getBrowserText());
		} else if (cls.equals(factory.getQuestionClass())) {
			setMainIcon(AnnotationsIcons.getIcon("q"));
			setMainText(cls.getBrowserText());
		}  else if (cls.hasSuperclass(factory.getVoteClass())) {
			setMainIcon(AnnotationsIcons.getIcon("v"));
			setMainText(cls.getBrowserText());
		} else if (cls.hasSuperclass(factory.getProposalClass())) {
			setMainIcon(AnnotationsIcons.getIcon("p"));
			setMainText(cls.getBrowserText());
		} else if (cls.hasSuperclass(factory.getAnnotationClass())){ //has to be last
			setMainIcon(AnnotationsIcons.getCommentIcon());
			setMainText(cls.getBrowserText());
		} else {
			super.loadCls(cls);
		}
	}
	
	public void dispose() {
		_value = null;
		chaoKb = null;
		factory = null;
		kb = null;
	}	
	

}
