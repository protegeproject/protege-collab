package edu.stanford.smi.protege.collab.gui.annotation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.LazyTreeNodeFrameComparator;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.server_changes.model.generated.AnnotatableThing;



/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeNode extends LazyTreeNode {
	private TreeFilter filter;
	
	public AnnotationsTreeNode(LazyTreeNode parent, Frame frame) {
        super(parent, frame);
    }
	
	public AnnotationsTreeNode(AnnotationsTreeRoot root, Frame frame, TreeFilter filter) {
		this(root, frame);
		
		this.filter = filter;
	}

	@Override
	protected LazyTreeNode createNode(Object o) {		
		return new AnnotationsTreeNode(this, (Frame) o);
	}

	@Override
	protected int getChildObjectCount() {
		//reimplement!!
		return getChildObjects().size();
	}


	@Override
	protected Collection<Frame> getChildObjects() {			
		Collection<Frame> allChildren = new ArrayList();
		
		Frame frame = getFrame();
		
		if (frame != null && frame instanceof AnnotatableThing) {
			allChildren.addAll(((AnnotatableThing)frame).getAssociatedAnnotations());
		}
		
		return filter(allChildren);
	}
		
	
	protected Collection filter(Collection allChildren) {
		if (filter == null) {
			return allChildren;			
		}
		
		ArrayList<Frame> filteredChildren = new ArrayList<Frame>();
		
		for (Frame node : filteredChildren) {
			if (filter.isValid(node)) {
				filteredChildren.add(node);
			}
		}
		
		return filteredChildren;
	}



	private Frame getFrame() {
		return (Frame) getUserObject();		
	}
	
	@Override
	protected Comparator getComparator() {		
		//return new InstanceDateComparator()
		return new LazyTreeNodeFrameComparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return super.compare(o1, o2);
			}
		};
	}
	
}
