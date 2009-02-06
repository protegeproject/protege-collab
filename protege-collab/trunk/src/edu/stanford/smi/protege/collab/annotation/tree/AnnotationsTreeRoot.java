package edu.stanford.smi.protege.collab.annotation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.util.AnnotatableThingComparator;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeRoot extends LazyTreeRoot {
	private TreeFilter filter;

	public AnnotationsTreeRoot(Collection roots) {
		this(roots, null);
	}

	public AnnotationsTreeRoot(Collection roots, TreeFilter filter) {
		super(getSortedRoots(roots));
		this.filter = filter;
	}

	protected static List<AnnotatableThing> getSortedRoots(Collection roots) {
		List<AnnotatableThing> list = new ArrayList<AnnotatableThing>(roots);
		Collections.sort(list, new AnnotatableThingComparator());
		return list;
	}

	public AnnotationsTreeRoot(Frame root) {
		this(root, null);
	}

	public AnnotationsTreeRoot(Frame root, TreeFilter filter) {
		super(root);
		this.filter = filter;
	}

	@Override
	protected LazyTreeNode createNode(Object o) {
		return new AnnotationsTreeNode(this, (AnnotatableThing) o, filter);
	}

	@Override
	protected Comparator getComparator() {
		return new AnnotatableThingComparator();
	}

	public TreeFilter getFilter() {
		return filter;
	}

	public void setFilter(TreeFilter filter) {
		this.filter = filter;
	}
}
