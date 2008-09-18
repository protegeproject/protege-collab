package edu.stanford.smi.protege.collab.util;

import java.util.Collection;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;

//This class is not used. Fix it and use it later.
public class DiscussionThreadManager {
	private static KnowledgeBase kb;
	private static KnowledgeBaseListener kbListener;
	private static DiscussionThreadManager instance;

	private Cls annotationCls;
	private Collection<Annotation> discussionThreads;

	private DiscussionThreadManager(KnowledgeBase kb) {
		this.kb = kb;

		kbListener = new KnowledgeBaseAdapter() {
			@Override
			public void instanceCreated(KnowledgeBaseEvent event) {
				Frame frame = event.getFrame();

				if (frame instanceof Instance && ((Instance)frame).hasType(annotationCls)){
					//when to remove it?
					discussionThreads.add((Annotation)frame);
				}
			}


			@Override
			public void instanceDeleted(KnowledgeBaseEvent event) {
				// TODO Auto-generated method stub
				super.instanceDeleted(event);
			}

		};

		annotationCls = new AnnotationFactory(ChAOKbManager.getChAOKb(kb)).getAnnotationClass();
		//add the kb listener
		KnowledgeBase changesKB = ChAOUtil.getChangesKb(kb);
		changesKB.addKnowledgeBaseListener(kbListener);
		initDiscussionThreadList();
	}

	//hmmm... fix this later
	public static DiscussionThreadManager getDiscussionThreadManager(KnowledgeBase kb) {
		if (instance == null) {
			instance = new DiscussionThreadManager(kb);
		} else {
			if (!kb.equals(instance.getKb())) {
				instance.getKb().removeKnowledgeBaseListener(kbListener);
				instance = new DiscussionThreadManager(kb);
			}
		}

		return instance;
	}


	private void initDiscussionThreadList() {
		//discussionThreads.addAll(annotationCls.getInstances());
	}


	public static KnowledgeBase getKb() {
		return kb;
	}

}
