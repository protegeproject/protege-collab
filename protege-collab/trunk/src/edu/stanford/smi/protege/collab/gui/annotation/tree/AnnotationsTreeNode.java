package edu.stanford.smi.protege.collab.gui.annotation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.LazyTreeNodeFrameComparator;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.changes.InstanceDateComparator;


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
	protected Collection getChildObjects() {			
		Collection allChildren = new ArrayList();
		
		allChildren.addAll(getChildObjectsAnnotations());
		//allChildren.addAll(getChildObjectsChanges());
		
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

	//move this to a utility class
	private Collection getValuesOnSlot(Slot slot) {
		if (slot == null) {
			return new ArrayList();
		}
		
		ArrayList values = new ArrayList(getFrame().getOwnSlotValues(slot));
		Collections.sort(values, getComparator());
		
		//try to get also the annotations for the old name of the frame, if the frame has been renamed 
		
		
		return values;	
	}

	
	//move this to a utility class
	private Collection getChildObjectsAnnotations() {
		
		Slot assocAnnot = ((Frame)getUserObject()).getKnowledgeBase().getSlot(ChangeCreateUtil.SLOT_NAME_ASSOC_ANNOTATIONS);
						
		return getValuesOnSlot(assocAnnot);
	}

	
	//move this to a utility class	
	private Collection getChildObjectsChanges() {
		Slot changesSlot =  ((Frame)getUserObject()).getKnowledgeBase().getSlot(ChangeCreateUtil.SLOT_NAME_CHANGES);
		
		return getValuesOnSlot(changesSlot);
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
