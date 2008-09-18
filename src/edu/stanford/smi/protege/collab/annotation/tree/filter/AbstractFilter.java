package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractFilter<X> implements TreeFilter<X> {
	private Object filterValue;

	public Collection<X> getFilteredCollection(Collection<X> frames) {
		Collection<X> filteredFrames = new ArrayList<X>();

		for (X frame : frames) {
			if (isValid(frame)) {
				filteredFrames.add(frame);
			}
		}
		return filteredFrames;
	}

	abstract public boolean isValid(X object);

	public Object getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}

}
