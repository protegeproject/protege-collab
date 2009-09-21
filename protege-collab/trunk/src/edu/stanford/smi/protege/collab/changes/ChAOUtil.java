package edu.stanford.smi.protege.collab.changes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.change.api.Composite_Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyComponentCache;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
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
import edu.stanford.smi.protegex.server_changes.GetAnnotationProjectName;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 * 
 */
public class ChAOUtil {

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

    public static Collection<Change> getChanges(Frame frame) {
        ArrayList<Change> changeInstances = new ArrayList<Change>();

        Ontology_Component ontologyComponent = getOntologyComponent(frame);

        if (ontologyComponent != null) {
            changeInstances.addAll(ontologyComponent.getChanges());
        }

        return changeInstances;
    }

    public static Collection<Change> getTopLevelChanges(Frame frame) {
        Collection<Change> allChanges = getChanges(frame);
        if (allChanges == null || allChanges.size() == 0) {
            return allChanges;
        }

        Collection<Change> toRemove = new ArrayList<Change>();
        for (Change changeInstance : allChanges) {
            Composite_Change cc = OntologyJavaMappingUtil.as(changeInstance, Composite_Change.class);
            if (cc != null) {
                toRemove.addAll(cc.getSubChanges());
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
        Collection<Annotation> allAnnotations = getAnnotations(kb);
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
    public static Collection<Annotation> getAnnotations(KnowledgeBase kb) {
        AnnotationFactory factory = new AnnotationFactory(getChangesKb(kb));
        return factory.getAllAnnotationObjects(true);
    }

    @SuppressWarnings("unchecked")
    public static Collection<Change> getChanges(KnowledgeBase kb) {
        ChangeFactory factory = new ChangeFactory(getChangesKb(kb));
        return factory.getAllChangeObjects(true);
    }

    //this should be cached in future
    public static Collection<Annotation> getTopLevelDiscussionThreads(KnowledgeBase kb) {
        Collection<Annotation> discussionThreadAnnotations = new ArrayList<Annotation>();
        Collection<Annotation> allAnnotations = getAnnotations(kb);

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

    public static int getAnnotationCount(Frame frame) {
        return HasAnnotationCache.getAnnotationCount(frame);
    }

    public static boolean isChangesOntologyPresent(KnowledgeBase kb) {
        return getChangesKb(kb) != null;
    }

    public static KnowledgeBase getChangesKb(KnowledgeBase kb) {
        return ChAOKbManager.getChAOKb(kb);
    }

    private static KnowledgeBase getServerSideChangeKb(KnowledgeBase kb) {
        String annotationName = (String) new GetAnnotationProjectName(kb).execute();
        if (annotationName == null) {
            Log.getLogger().warning(
                    "annotation project not configured (use "
                            + GetAnnotationProjectName.METAPROJECT_ANNOTATION_PROJECT_SLOT + " slot)");
        }
        RemoteProjectManager project_manager = RemoteProjectManager.getInstance();
        FrameStoreManager framestore_manager = ((DefaultKnowledgeBase) kb).getFrameStoreManager();
        RemoteClientFrameStore remote_frame_store = framestore_manager
                .getFrameStoreFromClass(RemoteClientFrameStore.class);
        RemoteServer server = remote_frame_store.getRemoteServer();
        RemoteSession session = remote_frame_store.getSession();
        try {
            session = server.cloneSession(session);
        } catch (RemoteException e) {
            Log.getLogger().info("Could not find server side change project " + e);
            return null;
        }
        Project changes_project = project_manager.connectToProject(server, session, annotationName);
        return changes_project == null ? null : changes_project.getKnowledgeBase();
    }

    public static Annotation createAnnotationOnAnnotation(KnowledgeBase kb, Frame annotatedFrame, Cls annotationType) {
        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);
        Annotation annot = OntologyJavaMappingUtil.createObject(changesKb, null, annotationType.getName(),
                Annotation.class);
        if (annotatedFrame == null) {
            return annot;
        }
        //maybe annotatedFrame is in the changes kb
        if (annotatedFrame.getKnowledgeBase().equals(changesKb)) { //annotated frame is an annotatable thing
            AnnotatableThing thing = OntologyJavaMappingUtil.getSpecificObject(changesKb, (Instance) annotatedFrame,
                    AnnotatableThing.class);
            if (thing == null) {
                Log.getLogger().warning("Could not find annotatable thing: " + annotatedFrame);
                return annot;
            }
            thing.addAssociatedAnnotations(annot);

        } else { //annotated frame is a domain entity
            Ontology_Component ontologyComp = getOntologyComponent(annotatedFrame, true);
            if (ontologyComp != null) {
                annot.setAnnotates(CollectionUtilities.createCollection(ontologyComp));
            }
        }
        return annot;
    }

    /**
     * This will return a collection of all the tree nodes that have the root
     * the collection argument that fullfill the filter condition.
     * 
     * @param collection
     * @param filter
     * @return
     */
    public static <X> Collection<? extends X> getFilteredCollection(Collection<? extends X> collection,
            TreeFilter<X> filter) {
        if (filter == null) {
            return collection;
        }

        Collection<X> filteredCollection = new ArrayList<X>();
        AnnotationsTreeRoot treeRoot = new AnnotationsTreeRoot(collection);
        filteredCollection.addAll(filterNodes(treeRoot, filter));

        return filteredCollection;
    }

    private static <X> Collection<X> filterNodes(LazyTreeNode node, TreeFilter<X> filter) {
        Collection<X> filteredCollection = new HashSet<X>();
        X userObject = (X) node.getUserObject();

        //userObject is a collection if the node is a root, and we don't want to add that
        if (!(userObject instanceof Collection)) {
            if (filter.isValid(userObject)) {
                filteredCollection.add(userObject);
            }
        }

        Enumeration<X> children = node.children();
        while (children.hasMoreElements()) {
            filteredCollection.addAll(filterNodes((LazyTreeNode) children.nextElement(), filter));
        }
        return filteredCollection;
    }

    public static Collection<Ontology_Component> getOntologyComponents(KnowledgeBase kb) {
        List<Ontology_Component> oComps = new ArrayList<Ontology_Component>();
        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);
        if (changesKb == null) {
            return oComps;
        }

        OntologyComponentFactory factory = new OntologyComponentFactory(changesKb);
        return factory.getAllOntology_ComponentObjects(true);
    }

    public static Collection<Ontology_Component> getOntologyComponentsWithAnnotations(KnowledgeBase kb) {
        List<Ontology_Component> oComps = new ArrayList<Ontology_Component>();
        KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);
        if (changesKb == null) {
            return oComps;
        }
        OntologyComponentFactory factory = new OntologyComponentFactory(changesKb);
        Collection<Ontology_Component> comps = factory.getAllOntology_ComponentObjects(true);
        for (Ontology_Component comp : comps) {
            if (comp.hasAssociatedAnnotations()) {
                oComps.add(comp);
            }
        }
        return oComps;
    }

    public static void fillAnnotationSystemFields(KnowledgeBase kb, Annotation annotation) {
        AnnotationFactory factory = new AnnotationFactory(getChangesKb(kb));
        factory.fillDefaultValues(annotation);
    }

}
