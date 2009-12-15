package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractFilter<X> implements TreeFilter<X> {
	private Object filterValue;

	public <Y extends X> Collection<Y> getFilteredCollection(Collection<Y> frames) {
		Collection<Y> filteredFrames = new ArrayList<Y>();

		for (Y frame : frames) {
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
