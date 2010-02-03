package edu.stanford.smi.protege.collab.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.ConfigureOptionsTabPanel;
import edu.stanford.smi.protege.collab.annotation.gui.StatusComboBoxUtil;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.LocalizeUtils;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

/**
 * A simple cache that stores the number of annotations a frame has.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 * 
 */
public class HasAnnotationCache {
    //FIXME: make a map from KB to the caches; add kb listener, remove caches if kb is closed
    //FIXME: update of annotation count when there are cycles does not work!!! This is because we do the computation on the server, and
    //the changes have already happened. We need the state before the change. We could do this on the client, but it is expensive

    //FIXME: the cache relies on several listerers form collab. Get the listeners here.

    private static Map<Frame, Integer> frame2AnnCountMap = new HashMap<Frame, Integer>();
    private static Map<Frame, Integer> frame2ChildrenAnnCountMap = new HashMap<Frame, Integer>();

    public static boolean hasAnnotations(Frame frame) {
        return getAnnotationCount(frame) > 0;
    }

    public static int getAnnotationCount(Frame frame) {
        if (frame2AnnCountMap.containsKey(frame)) {
            Integer value = frame2AnnCountMap.get(frame);
            return value == null ? 0 : value.intValue();
        }
        return 0;
    }

    public static int getChildrenAnnotationCount(Frame frame) {
        if (frame2ChildrenAnnCountMap.containsKey(frame)) {
            Integer value = frame2ChildrenAnnCountMap.get(frame);
            return value == null ? 0 : value.intValue();
        }
        return 0;
    }

    public static void addAnnotation(Frame frame) {
        if (frame == null) {
            return;
        }
        int existingAnn = getAnnotationCount(frame);
        frame2AnnCountMap.put(frame, existingAnn + 1);

        //update the annotation counts of all parents
        if (frame instanceof Cls) {
            Cls cls = (Cls) frame;
            Collection<Cls> parents = cls.getSuperclasses();
            for (Cls parent : parents) {
                int count = getChildrenAnnotationCount(parent);
                frame2ChildrenAnnCountMap.put(parent, count + 1);
            }
        }
    }
    
    public static void removeAnnotation(Frame frame) {
        if (frame == null) {
            return;
        }
        int existingAnn = getAnnotationCount(frame);
        frame2AnnCountMap.put(frame, existingAnn - 1);

        //update the annotation counts of all parents
        if (frame instanceof Cls) {
            Cls cls = (Cls) frame;
            Collection<Cls> parents = cls.getSuperclasses();
            for (Cls parent : parents) {
                int count = getChildrenAnnotationCount(parent);
                frame2ChildrenAnnCountMap.put(parent, count - 1);
            }
        }
    }
    
    public static void clearCache() { //TODO: add a listener on the kb
        frame2AnnCountMap.clear();
        frame2ChildrenAnnCountMap.clear();
    }

    @SuppressWarnings("unchecked")
    public static void fillHasAnnotationCache(KnowledgeBase kb) {
        frame2AnnCountMap.clear();
        frame2ChildrenAnnCountMap.clear();

        Map<String, AnnotationCount> frams2AnnCount = null;
        try {
            frams2AnnCount = (Map<String, AnnotationCount>) new GetFramesWithAnnotations(kb).execute();
        } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Could not retrieve frames with annotations from server", t);
            return;
        }

        if (frams2AnnCount != null) {
            for (String frameName : frams2AnnCount.keySet()) {
                Frame frame = kb.getFrame(frameName);
                if (frame != null) {
                    frame2AnnCountMap.put(frame, frams2AnnCount.get(frameName).getAnnCount());
                    frame2ChildrenAnnCountMap.put(frame, frams2AnnCount.get(frameName).getChildrenAnnCount());
                }
            }
        }
    }

    public static void onDirectSuperClassAdded(Cls cls, Cls superclass) {
        int totalAnnotationsCount = getAnnotationCount(cls) + getChildrenAnnotationCount(cls);

        if (totalAnnotationsCount == 0) {
            return;
        }

        KnowledgeBase kb = cls.getKnowledgeBase();

        Collection<String> newAncestorsString = null;
        try {
            newAncestorsString = (Collection<String>) new DirectSuperclassAddedHandler(kb, cls.getName(), superclass
                    .getName()).execute();
        } catch (Exception e) {
            Log.getLogger().log(
                    Level.WARNING,
                    "Error at updating annotations count at direct superclass added. Cls: " + cls + " supercls: "
                            + superclass);
        }

        if (newAncestorsString != null) {
            for (String newAncestorString : newAncestorsString) {
                Cls newAncestor = kb.getCls(newAncestorString);
                int count = getChildrenAnnotationCount(newAncestor) + totalAnnotationsCount;
                frame2ChildrenAnnCountMap.put(newAncestor, count);
            }
        }

    }

    public static void onDirectSuperClassRemoved(Cls cls, Cls superclass) {
        int totalAnnotationsCount = getAnnotationCount(cls) + getChildrenAnnotationCount(cls);

        if (totalAnnotationsCount == 0) {
            return;
        }

        KnowledgeBase kb = cls.getKnowledgeBase();

        Collection<String> removedAncestorsString = null;
        try {
            removedAncestorsString = (Collection<String>) new DirectSuperclassRemovedHandler(kb, cls.getName(),
                    superclass.getName()).execute();
        } catch (Exception e) {
            Log.getLogger().log(
                    Level.WARNING,
                    "Error at updating annotations count at direct superclass removed. Cls: " + cls + " supercls: "
                            + superclass);
        }

        if (removedAncestorsString != null) {
            for (String removedAncestorString : removedAncestorsString) {
                Cls removedAncestor = kb.getCls(removedAncestorString);
                int count = getChildrenAnnotationCount(removedAncestor) - totalAnnotationsCount;
                frame2ChildrenAnnCountMap.put(removedAncestor, count);
            }
        }
    }

    public static void onClsDeleted(Cls cls, Cls oldParent) {
        frame2AnnCountMap.remove(cls);
        frame2ChildrenAnnCountMap.remove(cls);

        //update the annotation counts of all parents       
        Collection<Cls> parents = new ArrayList(cls.getSuperclasses());
        if (parents.size() == 0) {
            parents = new ArrayList<Cls>(oldParent.getSuperclasses());
            parents.add(oldParent);
        }

        for (Cls parent : parents) {
            int count = getChildrenAnnotationCount(parent);
            if (count > 0) {
                frame2ChildrenAnnCountMap.put(parent, count - 1);
            }
        }
    }
    
    public static void archiveStatusChanged(Project prj, Annotation annotation, Collection<Ontology_Component> annotatedFramesOC) {    	
    	if (hideArchived(prj)) {
    		boolean isArchived = annotation.getArchived();
    		for (Ontology_Component oc : annotatedFramesOC) {
    			Instance domainAnnotatedFrame = null;           
    			String key = oc.getCurrentName();
    			if (key != null) {
    				domainAnnotatedFrame = prj.getKnowledgeBase().getInstance(key);
    			}            
    			if (domainAnnotatedFrame != null) {
    				if (isArchived) {
    					removeAnnotation(domainAnnotatedFrame);
    				} else {
    					addAnnotation(domainAnnotatedFrame);
    				}
    			}
    		}
    	}    	
    }
     
    
    public static boolean hideArchived(Project prj) {
    	return StatusComboBoxUtil.getHideArchived(prj);
    }

    /*
     * Remote call - Get the cache of annotation counts from the server, if it runs in client-server mode
     */

    static class GetFramesWithAnnotations extends ProtegeJob {
        private static final long serialVersionUID = -8929614115737849773L;

        //TODO - should be unstatic?
        private static Map<String, AnnotationCount> frames2annCount;

        public GetFramesWithAnnotations(KnowledgeBase kb) {
            super(kb);
        }

        @Override
        public Object run() throws ProtegeException {
            //TODO: It is more efficient to compute the annotations starting with 
            //the annnotations rather than the ontology components
            KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(getKnowledgeBase());
            frames2annCount = new HashMap<String, AnnotationCount>();
            if (chaoKb == null) {
                return frames2annCount;
            }
                        
            OntologyComponentFactory ocf = new OntologyComponentFactory(chaoKb);
            Collection<Ontology_Component> ocs = ocf.getAllOntology_ComponentObjects(true);

            Slot associatedAnnotationsSlot = new AnnotationFactory(chaoKb).getAssociatedAnnotationsSlot();
            Slot archivedSlot = new AnnotationFactory(chaoKb).getArchivedSlot();
            
            boolean hideArchived = getHideArchived(getKnowledgeBase().getProject());
            
            //calculate the number of local annotations
            for (Ontology_Component oc : ocs) {
                Collection<Annotation> annots = oc.getAssociatedAnnotations();
                if (annots != null && annots.size() > 0) {
                    String currentName = oc.getCurrentName();
                    if (currentName != null) {
                        AnnotationCount ac = new AnnotationCount();
						ac.setAnnCount(getAnnotatesTransitive(oc, associatedAnnotationsSlot, 
                        		archivedSlot, hideArchived));
                        frames2annCount.put(currentName, ac);
                    }
                }
            }

            Set<String> origKeySet = new HashSet<String>(frames2annCount.keySet());
            //calculate the number of children annotations
            for (String frameName : origKeySet) {
                updateParentAnnotationCount(frameName);
            }

            return frames2annCount;
        }

        private int getAnnotatesTransitive(Ontology_Component oc, Slot associatedAnnotationsSlot, 
        		Slot archivedSlot, boolean hideArchived) {
            Instance inst = ((AbstractWrappedInstance) oc).getWrappedProtegeInstance();
            Set closure = inst.getKnowledgeBase().getDirectOwnSlotValuesClosure(inst, associatedAnnotationsSlot);
            if (closure == null) { return 0; }
            int size = closure.size();
            if (hideArchived) {
            	for (Iterator iterator = closure.iterator(); iterator.hasNext();) {
					Instance annInst = (Instance) iterator.next();
					Boolean b = (Boolean) annInst.getOwnSlotValue(archivedSlot);
					if (b != null && b == Boolean.TRUE) {
						size = size - 1;
					}
				}
            }
            return size;
        }
        
        private boolean getHideArchived(Project project) {
    		String hide = (String) project.getClientInformation(ConfigureOptionsTabPanel.HIDE_ARCHIVED);
    		return hide == null ? false : hide.equalsIgnoreCase("false") ? false : true;
    	}

        private void updateParentAnnotationCount(String frameName) {
            Frame frame = getKnowledgeBase().getFrame(frameName);
            if (!(frame instanceof Cls)) {
                return;
            }
            Cls cls = (Cls) frame;
            Collection<List<Cls>> pathsToRoot = ModelUtilities.getPathsToRoot(cls);
            Set<Cls> allParents = new HashSet<Cls>();
            for (List<Cls> path : pathsToRoot) {
                allParents.addAll(path);
            }
            allParents.remove(cls);

            int localCount = getLocalAnnotationCount(frameName);
            //add the local annotation count to all parents
            for (Cls parent : allParents) {
                int parentChildrenCount = getChildrenAnnotationCount(parent.getName()) + localCount;
                getAnnotationCount(parent.getName()).setChildrenAnnCount(parentChildrenCount);
            }
        }

        private int getLocalAnnotationCount(String name) {
            AnnotationCount ac = getAnnotationCount(name);
            return (ac == null) ? 0 : ac.getAnnCount() == null ? 0 : ac.getAnnCount();
        }

        private int getChildrenAnnotationCount(String name) {
            AnnotationCount ac = getAnnotationCount(name);
            return (ac == null) ? 0 : ac.getChildrenAnnCount() == null ? 0 : ac.getChildrenAnnCount();
        }

        private AnnotationCount getAnnotationCount(String name) {
            AnnotationCount ac = frames2annCount.get(name);
            if (ac == null) {
                ac = new AnnotationCount();
                frames2annCount.put(name, ac);
            }
            return frames2annCount.get(name);
        }
    }

    static class AnnotationCount implements Serializable, Localizable {
        private Integer annCount;
        private Integer childrenAnnCount;

        public AnnotationCount() {

        }

        public Integer getAnnCount() {
            return annCount;
        }

        public void setAnnCount(Integer annCount) {
            this.annCount = annCount;
        }

        public Integer getChildrenAnnCount() {
            return childrenAnnCount;
        }

        public void setChildrenAnnCount(Integer childrenAnnCount) {
            this.childrenAnnCount = childrenAnnCount;
        }

        public void localize(KnowledgeBase kb) {
            LocalizeUtils.localize(annCount, kb);
            LocalizeUtils.localize(childrenAnnCount, kb);
        }
    }

    static class DirectSuperclassAddedHandler extends ProtegeJob {
        private String clsName;
        private String superclsName;

        public DirectSuperclassAddedHandler(KnowledgeBase kb, String cls, String supercls) {
            super(kb);
            this.clsName = cls;
            this.superclsName = supercls;
        }

        @Override
        public Object run() throws ProtegeException {
            Cls cls = getKnowledgeBase().getCls(clsName);
            Cls superclass = getKnowledgeBase().getCls(superclsName);

            Collection<Cls> oldParents = new ArrayList(cls.getDirectSuperclasses());
            oldParents.remove(superclass);

            Set<Cls> oldAncestors = new HashSet<Cls>();
            for (Cls oldParent : oldParents) {
                Collection<List<Cls>> pathsToRoot = ModelUtilities.getPathsToRoot(oldParent);
                for (List<Cls> path : pathsToRoot) {
                    oldAncestors.addAll(path);
                }
            }

            Set<Cls> newAncestors = new HashSet<Cls>();
            Collection<List<Cls>> pathsToRoot = ModelUtilities.getPathsToRoot(superclass);
            for (List<Cls> path : pathsToRoot) {
                newAncestors.addAll(path);
            }

            newAncestors.removeAll(oldAncestors);

            Collection<String> newAncestorsString = new ArrayList<String>();
            for (Cls anc : newAncestors) {
                newAncestorsString.add(anc.getName());
            }
            return newAncestorsString;
        }

    }

    static class DirectSuperclassRemovedHandler extends ProtegeJob {
        private String clsName;
        private String superclsName;

        public DirectSuperclassRemovedHandler(KnowledgeBase kb, String cls, String supercls) {
            super(kb);
            this.clsName = cls;
            this.superclsName = supercls;
        }

        @Override
        public Object run() throws ProtegeException {
            Cls cls = getKnowledgeBase().getCls(clsName);
            Cls superclass = getKnowledgeBase().getCls(superclsName);

            Set<Cls> newAncestors = new HashSet<Cls>();
            Collection<List<Cls>> pathsToRoot = ModelUtilities.getPathsToRoot(cls);
            for (List<Cls> path : pathsToRoot) {
                newAncestors.addAll(path);
            }

            Set<Cls> removedAncestors = new HashSet<Cls>();
            pathsToRoot = ModelUtilities.getPathsToRoot(superclass);
            for (List<Cls> path : pathsToRoot) {
                removedAncestors.addAll(path);
            }

            removedAncestors.removeAll(newAncestors);

            Collection<String> removedAncestorsString = new ArrayList<String>();
            for (Cls anc : removedAncestors) {
                removedAncestorsString.add(anc.getName());
            }
            return removedAncestorsString;
        }
    }

}
