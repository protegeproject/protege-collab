package edu.stanford.smi.protege.collab.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.server_changes.ChangesDb;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.GetAnnotationProjectName;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Composite_Change;
import edu.stanford.smi.protegex.server_changes.model.generated.Ontology_Component;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChangeOntologyUtil {
	
	/**
	 * This is used as a temporary cache for frames with annotations
	 */
	private static Collection<Frame> framesWithAnnotations = new ArrayList<Frame>();
	
	//Cache for changesKb 
	private static HashMap<KnowledgeBase, KnowledgeBase> kb2changesKb = new HashMap<KnowledgeBase, KnowledgeBase>();
	private static HashMap<KnowledgeBase, ChangeModel> kb2changeModel = new HashMap<KnowledgeBase, ChangeModel>();
	private static HashMap<KnowledgeBase, ChangesDb> kb2changesDb = new HashMap<KnowledgeBase, ChangesDb>();
	private static HashMap<Frame, Ontology_Component> frame2OntologyComponent = new HashMap<Frame, Ontology_Component>();
	
    public static Collection<Annotation> getAnnotationInstances(Frame frame) {
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
    
    public static Ontology_Component getOntologyComponent(Frame frame) {
    	return getOntologyComponent(frame, false);
    }
    
	public static Ontology_Component getOntologyComponent(Frame frame, boolean create) {
		Ontology_Component ontologyComp = frame2OntologyComponent.get(frame);
		
		if (ontologyComp != null) {
			return ontologyComp;
		}
				
		try {
			GetOntologyComponentFromServer job = new GetOntologyComponentFromServer(frame.getKnowledgeBase(), frame, create);
			ontologyComp = (Ontology_Component) job.execute();
			if (ontologyComp != null) {
				ontologyComp.localize(getChangesKb(frame.getKnowledgeBase()));
			}
		} catch (Throwable e) {
			Log.getLogger().warning("Errors at ontology component from server. Message: " + e.getMessage());
			e.printStackTrace();
		}
		
		if (ontologyComp != null) {
			frame2OntologyComponent.put(frame, ontologyComp);
		}
		
		return ontologyComp;
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
	
	
	public static Collection<Annotation> getAnnotationInstances(KnowledgeBase kb) {
		Cls annotationCls = ChangeOntologyUtil.getChangeModel(kb).getCls(AnnotationCls.Annotation);
		
		return annotationCls.getInstances();
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

		if (framesWithAnnotations.contains(frame)) {
			return true;
		}

		//this should be implemented more efficiently
		Collection annotations = getAnnotationInstances(frame);

		if ((annotations != null && annotations.size() > 0)) {
			framesWithAnnotations.add(frame);
			return true;
		}

		return false;
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
			changeModel = new ChangeModel(changeKb);
		}
				
		kb2changeModel.put(kb, changeModel);
		
		return changeModel;
		
	}

	
	public static void clearKb2ChangesKbMap() {
		kb2changesKb.clear();
		kb2changeModel.clear();
		kb2changesDb.clear();
		frame2OntologyComponent.clear();
	}
	
	private static KnowledgeBase getServerSideChangeKb(KnowledgeBase kb) {
		String annotationName = (String) new GetAnnotationProjectName(kb).execute();
		if (annotationName == null) {
			Log.getLogger().warning("annotation project not configured (use " +
					GetAnnotationProjectName.METAPROJECT_ANNOTATION_PROJECT_SLOT +
			" slot)");
		}
		RemoteProjectManager manager = RemoteProjectManager.getInstance();
		FrameStoreManager fsmanager = ((DefaultKnowledgeBase) kb).getFrameStoreManager();
		RemoteClientFrameStore rcfs = (RemoteClientFrameStore) fsmanager.getFrameStoreFromClass(RemoteClientFrameStore.class);
		Project changes_project = manager.connectToProject(rcfs.getRemoteServer(), rcfs.getSession(), annotationName);
		return changes_project.getKnowledgeBase();	
	}
	
	
	public static Annotation createAnnotationOnAnnotation(KnowledgeBase kb, Frame annotatedFrame, AnnotationCls annotationType) {
		
		//ChangesDb changesDb = ChangesProject.getChangesDb(kb);
		
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
		
	//	for (Iterator iter = collection.iterator(); iter.hasNext();) {
		//	Frame frame = (Frame) iter.next();
			
			//filteredCollection.add(frame);
			AnnotationsTreeRoot treeRoot = new AnnotationsTreeRoot(collection);
			
			filteredCollection.addAll(filterNodes(treeRoot, filter));
	//	}
				
		return filteredCollection;
	}
	
	private static Collection filterNodes(LazyTreeNode node, TreeFilter filter) {
		Collection filteredCollection = new ArrayList();
		
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
	
}
