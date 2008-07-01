package edu.stanford.smi.protege.collab.annotation.tree;

import java.util.Collection;
import java.util.Comparator;

import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.LazyTreeNodeFrameComparator;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protegex.server_changes.model.AnnotationCreationComparator;

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
		super(roots);
		this.filter = filter;
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
		return new AnnotationsTreeNode(this, (Frame) o, filter);	
	}

	@Override
	protected Comparator getComparator() {		
		return new AnnotationCreationComparator();
	}
	
	public TreeFilter getFilter() {
		return filter;
	}
	
	public void setFilter(TreeFilter filter) {
		this.filter = filter;
	}
}
