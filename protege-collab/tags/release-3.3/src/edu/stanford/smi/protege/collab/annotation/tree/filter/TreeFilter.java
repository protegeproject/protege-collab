package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.Collection;

import edu.stanford.smi.protege.model.Frame;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public interface TreeFilter {

	boolean isValid(Frame frame);
	
	void setFilterValue(Object value);
	
	Object getFilterValue();
	
	Collection getFilteredCollection(Collection unfilteredCollection);
	
}
