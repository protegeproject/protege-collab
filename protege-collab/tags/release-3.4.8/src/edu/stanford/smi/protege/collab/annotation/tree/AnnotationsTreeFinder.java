package edu.stanford.smi.protege.collab.annotation.tree;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Reference;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.FrameTreeFinder;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringMatcher;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeFinder extends FrameTreeFinder implements Disposable {
    private static final long serialVersionUID = -404780170757617443L;
    private JTree tree;
	private Slot ancestorSlot;

	public AnnotationsTreeFinder(KnowledgeBase kb, JTree tree, Slot ancestorSlot) {
		super(kb, tree, "Find annotations in tree");

		this.tree = tree;
		this.ancestorSlot = ancestorSlot;
	}

	@Override
	protected Collection getAncestors(Frame frame) {
		return getKnowledgeBase().getDirectOwnSlotValuesClosure(frame, ancestorSlot);
	}

	@Override
	protected Slot getBrowserSlot(KnowledgeBase kb) {
		return null;
	}

	@Override
	protected Collection getParents(Frame frame) {
		return frame.getOwnSlotValues(ancestorSlot);
	}

	@Override
	protected boolean isCorrectType(Frame frame) {
		return true;
	}


	@Override
	protected List<Frame> getMatches(String text, int maxMatches) {
		if (!text.startsWith("*")) {
			text = "*" + text;
		}
		Cls kbRoot = getKnowledgeBase().getRootCls();
		Set<Frame> matches = getMatchingFrames(text, maxMatches);
		LazyTreeRoot root = (LazyTreeRoot) tree.getModel().getRoot();
		Set rootNodes = new HashSet((Collection) root.getUserObject());
		if (rootNodes.size() != 1 || !equals(CollectionUtilities.getFirstItem(rootNodes), kbRoot)) {
			// Log.trace("removing bad matches", this, "getMatches");
			Iterator i = matches.iterator();
			while (i.hasNext()) {
				boolean isValid = false;
				Frame frame = (Frame) i.next();
				if (frame instanceof Instance) {
					AnnotatableThing annThing = new DefaultAnnotatableThing((Instance)frame);
					isValid = rootNodes.contains(annThing);
				}
				if (!isValid) {
					Collection parents = new HashSet(getAncestors(frame));
					isValid = convertToAnnotThing(parents).removeAll(rootNodes);
				}
				if (!isValid) {
					i.remove();
				}
			}
		}
		List<Frame> sortedMatches = new ArrayList<Frame>(matches);
		Collections.sort(sortedMatches, new FrameComparator<Frame>());
		return sortedMatches;
	}


	
    @Override
	protected Set<Frame> getMatchingFrames(String text, int maxMatches) {
        if (!text.endsWith("*")) {
            text += '*';
        }
        StringMatcher matcher = getStringMatcher(text);
        Set<Frame> matches = new LinkedHashSet<Frame>();
        Collection<Reference> matchingReferences = getKnowledgeBase().getMatchingReferences(text, maxMatches);
        Iterator i = matchingReferences.iterator();
        while (i.hasNext()) {
            Reference ref = (Reference) i.next();
            Frame frame = ref.getFrame();
            if (isCorrectType(frame)) {
                if (isMatch(matcher, (Instance) frame)) {
                    matches.add(frame);
                }
            }
        }
        return matches;
    }
    
    protected boolean isMatch(StringMatcher matcher, Instance inst) {
    	boolean match = matcher.isMatch(inst.getBrowserText());
    	if (match) { return true; }
    	AnnotationFactory factory = new AnnotationFactory(getKnowledgeBase());
    	if (inst.hasType(factory.getAnnotationClass())) {
    		Annotation ann = new DefaultAnnotation(inst);
    		String text = ann.getBody() + ann.getSubject();
    		return matcher.isMatch(text);
    	}
    	return false;
    }
	

	@Override
	protected void getVisiblePathToRoot(Frame frame, Collection path) {
		if (!(frame instanceof Instance)) { return; }
		AnnotatableThing annotThing = new DefaultAnnotatableThing((Instance) frame); 
		Collection roots = new ArrayList((Collection) ((LazyTreeNode) tree.getModel().getRoot()).getUserObject());
		Iterator i = roots.iterator();
		while (i.hasNext()) {
			AbstractWrappedInstance root = (AbstractWrappedInstance) i.next();			
			if (!root.getWrappedProtegeInstance().isVisible()) {
				i.remove();
			}
		}
		path.add(annotThing);
		if (!roots.contains(annotThing)) {
			boolean succeeded = getVisiblePathToRoot(annotThing, roots, path);
			if (!succeeded) {
				Log.getLogger().warning("No visible path found for " + frame);
			}
		}
	}
	
	protected boolean getVisiblePathToRoot(AnnotatableThing annotThing, Collection roots, Collection path) {
		boolean found = false;
		Iterator<AnnotatableThing> i = getParents(annotThing).iterator();
		while (i.hasNext() && !found) {
			AnnotatableThing parent = i.next();
			if (((AbstractWrappedInstance)parent).getWrappedProtegeInstance().isVisible() && !path.contains(parent)) {
				path.add(parent);
				if (roots.contains(parent)) {
					found = true;
				} else {
					found = getVisiblePathToRoot(parent, roots, path);
				}
				if (!found) {
					path.remove(parent);
				}
			}
		}
		return found;
	}

	protected Collection<AnnotatableThing> getParents(AnnotatableThing annotThing) {		
		return convertToAnnotThing(getParents(((AbstractWrappedInstance)annotThing).getWrappedProtegeInstance()));		
	}

	private Collection<AnnotatableThing> convertToAnnotThing(Collection instances) {
		Collection<AnnotatableThing> annotThings = new ArrayList<AnnotatableThing>();
		for (Iterator iterator = instances.iterator(); iterator.hasNext();) {
			Instance inst = (Instance) iterator.next();
			annotThings.add(new DefaultAnnotatableThing(inst));			
		}
		return annotThings;
	}
	
	public void dispose() {
		ancestorSlot = null;
		tree = null;		
	}
}