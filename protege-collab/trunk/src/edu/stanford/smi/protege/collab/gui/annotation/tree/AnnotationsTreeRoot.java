package edu.stanford.smi.protege.collab.gui.annotation.tree;

import java.util.Collection;
import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.LazyTreeNodeFrameComparator;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeRoot extends LazyTreeRoot {
	private TreeFilter filter;

	public AnnotationsTreeRoot(Collection roots) {	
		super(roots);
	}
	
	public AnnotationsTreeRoot(Frame root) {
		super(root);
	}

	@Override
	protected LazyTreeNode createNode(Object o) {
		return new AnnotationsTreeNode(this, (Frame) o, filter);
	}

	@Override
	protected Comparator getComparator() {		
		return new LazyTreeNodeFrameComparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return super.compare(o1, o2);
			}
		};
	}
}
