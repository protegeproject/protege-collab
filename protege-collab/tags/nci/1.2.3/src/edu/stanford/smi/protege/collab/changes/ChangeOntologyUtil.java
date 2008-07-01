package edu.stanford.smi.protege.collab.changes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyComponentCache;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.ChangesDb;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.GetAnnotationProjectName;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeCls;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Composite_Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;
import edu.stanford.smi.protegex.server_changes.model.generated.Timestamp;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangeOntologyUtil {

	//Cache for changesKb 
	private static HashMap<KnowledgeBase, KnowledgeBase> kb2changesKb = new HashMap<KnowledgeBase, KnowledgeBase>();
	private static HashMap<KnowledgeBase, ChangeModel> kb2changeModel = new HashMap<KnowledgeBase, ChangeModel>();
	private static HashMap<KnowledgeBase, ChangesDb> kb2changesDb = new HashMap<KnowledgeBase, ChangesDb>();


	public static Ontology_Component getOntologyComponent(Frame frame) {
		return OntologyComponentCache.getOntologyComponent(frame);
	}

	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		return OntologyComponentCache.getOntologyComponent(frame, create);
	}
	

	static Collection<Annotation> getAnnotationInstances(Frame frame) {
		ArrayList<Annotation> annotInstances = new ArrayList<Annotation>();

		Ontology_Component ontologyComponent = getOntologyComponent(frame);

		if (ontologyComponent != null) {
			annotInstances.addAll(ontologyComponent.getAssociatedAnnotations());
		}

		return annotInstances;
	}


	public static Collection<Change> getChangeInstances(Frame frame) {
		ArrayList<Change> changeInstances = new ArrayList<Change>();

		Ontology_Component ontologyComponent = getOntologyComponent(frame);

		if (ontologyComponent != null) {
			changeInstances.addAll(ontologyComponent.getChanges());
		}

		return changeInstances;
	}


	public static Collection<Change> getTopLevelChangeInstances(Frame frame) {
		Collection<Change> allChanges = getChangeInstances(frame); 

		if (allChanges == null) {
			return null;
		}

		Collection<Annotation> toRemove = new ArrayList<Annotation>();

		for (Change changeInstance : allChanges) {
			if (changeInstance instanceof Composite_Change) {
				toRemove.addAll(((Composite_Change)changeInstance).getSubChanges());
			}
		}

		allChanges.removeAll(toRemove);

		return allChanges;
	}

	public static Collection<Annotation> getTopLevelAnnotationInstances(Frame frame) {
		Collection<Annotation> allAnnotations = getAnnotationInstances(frame); 

		if (allAnnotations == null) {
			return null;
		}

		Collection<Annotation> toRemove = new ArrayList<Annotation>();

		for (Annotation annotInstance : allAnnotations) {
			toRemove.addAll(annotInstance.getAssociatedAnnotations());
		}

		allAnnotations.removeAll(toRemove);

		return allAnnotations;
	}

	public static Collection<Annotation> getTopLevelAnnotationInstances(KnowledgeBase kb) {
		Collection<Annotation> allAnnotations = getAnnotationInstances(kb); 

		if (allAnnotations == null) {
			return null;
		}

		Collection<Annotation> toRemove = new ArrayList<Annotation>();

		for (Annotation annotInstance : allAnnotations) {
			toRemove.addAll(annotInstance.getAssociatedAnnotations());
		}

		allAnnotations.removeAll(toRemove);

		return allAnnotations;		
	}

	@SuppressWarnings("unchecked")
	public static Collection<Annotation> getAnnotationInstances(KnowledgeBase kb) {
		Cls annotationCls = ChangeOntologyUtil.getChangeModel(kb).getCls(AnnotationCls.Annotation);

		return (Collection) annotationCls.getInstances();		
	}


	@SuppressWarnings("unchecked")
	public static Collection<Change> getChangeInstances(KnowledgeBase kb) {
		Cls changeCls = ChangeOntologyUtil.getChangeModel(kb).getCls(ChangeCls.Change);

		return (Collection) changeCls.getInstances();
	}
	
	//this should be cached in future
	public static Collection<Annotation> getTopLevelDiscussionThreads(KnowledgeBase kb) {
		Collection<Annotation> discussionThreadAnnotations = new ArrayList<Annotation>();

		Collection<Annotation> allAnnotations =getAnnotationInstances(kb);

		for (Annotation annotation : allAnnotations) {
			Collection annotates = annotation.getAnnotates();
			if (annotates == null || annotates.size() == 0) {
				discussionThreadAnnotations.add(annotation);
			}
		}	

		return discussionThreadAnnotations;
	}

	public static boolean hasAnnotations(Frame frame) {
		return HasAnnotationCache.hasAnnotations(frame);
	}


	public static boolean isChangesOntologyPresent(KnowledgeBase kb) {
		return (getChangesKb(kb, false) != null);
	}

	public static KnowledgeBase getChangesKb(KnowledgeBase kb) {
		return getChangesKb(kb, true);
	}

	//copied from ChangesTab
	public static KnowledgeBase getChangesKb(KnowledgeBase kb, boolean create) {
		KnowledgeBase changesKb = kb2changesKb.get(kb);

		//Is it better to use here a three value cache? We could cache also the null value.
		if (changesKb != null || !create) {
			return changesKb;
		}

		try {
			// NEED TO ADD IMPLEMENTATION FOR SERVER MODE
			// But this project must "essentially" be the same as the project that the project plugin is using
			// same events, contents etc.
			// it also runs after the changes project plugin has initialized.
			if (kb.getProject().isMultiUserClient()) {
				changesKb = getServerSideChangeKb(kb);
			}
			else {				
				changesKb = ChangesProject.getChangesKB(kb);
				
				//TT - This is a temporary hack, that will be removed after refactoring the ChangesTab code				
				if (changesKb == null) { // this might mean that the ChangesProject plugin has not been initialized yet
					new ChangesProject().afterLoad(kb.getProject());
					changesKb = ChangesProject.getChangesKB(kb);
				}			
			}
			
			kb2changesKb.put(kb, changesKb);

		} catch(Throwable e) {
			Log.getLogger().warning("There were errors at getting the Changes project attached to " + kb + ". Error message: " + e.getMessage());
		}

		return changesKb;
	}

	
	public static ChangeModel getChangeModel(KnowledgeBase kb) {
		ChangeModel changeModel = kb2changeModel.get(kb);

		if (changeModel != null) {
			return changeModel;
		}

		if (!kb.getProject().isMultiUserClient()) {
			changeModel = ChangesProject.getChangesDb(kb).getModel();		
		} else {		
			KnowledgeBase changeKb = getChangesKb(kb);
			if (changeKb != null) {				
				changeModel = new ChangeModel(changeKb);
			}
		}

		kb2changeModel.put(kb, changeModel);

		return changeModel;

	}
	

	private static KnowledgeBase getServerSideChangeKb(KnowledgeBase kb) {
		String annotationName = (String) new GetAnnotationProjectName(kb).execute();
		if (annotationName == null) {
			Log.getLogger().warning("annotation project not configured (use " +
					GetAnnotationProjectName.METAPROJECT_ANNOTATION_PROJECT_SLOT +
			" slot)");
		}
		RemoteProjectManager project_manager = RemoteProjectManager.getInstance();
		FrameStoreManager framestore_manager = ((DefaultKnowledgeBase) kb).getFrameStoreManager();
		RemoteClientFrameStore remote_frame_store = (RemoteClientFrameStore) framestore_manager.getFrameStoreFromClass(RemoteClientFrameStore.class);
		RemoteServer server = remote_frame_store.getRemoteServer();
        RemoteSession session = remote_frame_store.getSession();
        try {
            session = server.cloneSession(session);
        } catch (RemoteException e) {
            Log.getLogger().info("Could not find server side change project " + e);
            return null;
        }
        Project changes_project = project_manager.connectToProject(server, session, annotationName);
		return (changes_project == null ? null: changes_project.getKnowledgeBase());		
	}


	public static Annotation createAnnotationOnAnnotation(KnowledgeBase kb, Frame annotatedFrame, AnnotationCls annotationType) {
		Annotation annotInst = (Annotation) ChangeOntologyUtil.getChangeModel(kb).createInstance(annotationType);

		if (annotatedFrame == null) {
			return annotInst;
		}

		if (annotatedFrame instanceof AnnotatableThing) {
			annotInst.setAnnotates(CollectionUtilities.createCollection(annotatedFrame));
			return annotInst;
		}

		Ontology_Component ontologyComp = getOntologyComponent(annotatedFrame, true);

		if (ontologyComp != null) {
			annotInst.setAnnotates(CollectionUtilities.createCollection(ontologyComp));
		}

		return annotInst;
	}

	/**
	 * This will return a collection of all the tree nodes that have the root the collection argument
	 * that fullfill the filter condition.	 
	 * @param collection
	 * @param filter
	 * @return
	 */
	public static Collection getFilteredCollection(Collection collection, TreeFilter filter) {
		if (filter == null) {
			return collection;
		}

		Collection<Frame> filteredCollection = new ArrayList<Frame>();		
		AnnotationsTreeRoot treeRoot = new AnnotationsTreeRoot(collection);			
		filteredCollection.addAll(filterNodes(treeRoot, filter));

		return filteredCollection;
	}

	private static Collection filterNodes(LazyTreeNode node, TreeFilter filter) {
		Collection filteredCollection = new HashSet();

		Object userObject = node.getUserObject();

		//userObject is a collection if the node is a root, and we don't want to add that
		if (userObject instanceof Frame) {
			if (filter.isValid(((Frame)userObject))) {
				filteredCollection.add(userObject);
			}
		}

		Enumeration children = node.children();

		while (children.hasMoreElements()) {
			filteredCollection.addAll(filterNodes((LazyTreeNode) children.nextElement(), filter));		
		}	

		return filteredCollection;		
	}

	
	public static void fillAnnotationSystemFields(KnowledgeBase kb, Annotation annotation) {
		annotation.setCreated(Timestamp.getTimestamp(ChangeOntologyUtil.getChangeModel(kb)));
		annotation.setAuthor(kb.getUserName());	
	}
	
	public static void clearKb2ChangesKbMap() {
		kb2changesKb.clear();
		kb2changeModel.clear();
		kb2changesDb.clear();
		//clear other caches
	}

	
	
}
