package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.Collection;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public interface TreeFilter<X> {

	boolean isValid(X object);

	void setFilterValue(Object value);

	Object getFilterValue();

	Collection<X> getFilteredCollection(Collection<X> unfilteredCollection);

}
