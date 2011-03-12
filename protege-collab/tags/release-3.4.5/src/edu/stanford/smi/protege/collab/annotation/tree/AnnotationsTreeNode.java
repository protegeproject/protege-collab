package edu.stanford.smi.protege.collab.annotation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.util.AnnotatableThingComparator;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.util.LazyTreeNode;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeNode extends LazyTreeNode {
	private TreeFilter<AnnotatableThing> filter;

	public AnnotationsTreeNode(LazyTreeNode parent, AnnotatableThing node) {
        super(parent, node);
    }

	public AnnotationsTreeNode(LazyTreeNode root, AnnotatableThing node, TreeFilter<AnnotatableThing> filter) {
		this(root, node);
		this.filter = filter;
	}

	@Override
	protected LazyTreeNode createNode(Object o) {
		return new AnnotationsTreeNode(this, (AnnotatableThing) o, filter);
	}

	@Override
	protected int getChildObjectCount() {
		//TODO: reimplement!!
		return getChildObjects().size();
	}


	@Override
	protected Collection<AnnotatableThing> getChildObjects() {
		List<AnnotatableThing> allChildren = new ArrayList<AnnotatableThing>();
		AnnotatableThing annotThing = getAnnotatableThing();
		allChildren.addAll(annotThing.getAssociatedAnnotations());
		//TODO: make more efficient
		allChildren = new ArrayList<AnnotatableThing>(filter(allChildren));
		Collections.sort(allChildren, new AnnotatableThingComparator());
		return allChildren;
	}


	protected Collection<AnnotatableThing> filter(Collection<AnnotatableThing> allChildren) {
		if (filter == null) {
			return allChildren;
		}
		return (Collection<AnnotatableThing>) filter.getFilteredCollection(allChildren);
	}

	private AnnotatableThing getAnnotatableThing() {
		return (AnnotatableThing) getUserObject();
	}

	@Override
	protected Comparator getComparator() {
		return new AnnotatableThingComparator();
	}

	public TreeFilter<AnnotatableThing> getFilter() {
		return filter;
	}

	public void setFilter(TreeFilter<AnnotatableThing> filter) {
		this.filter = filter;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
}
