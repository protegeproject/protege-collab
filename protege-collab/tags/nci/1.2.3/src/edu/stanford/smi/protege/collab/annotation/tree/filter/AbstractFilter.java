package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Frame;

public abstract class AbstractFilter implements TreeFilter {
	private Object filterValue;
	
	public Collection getFilteredCollection(Collection frames) {
		Collection<Frame> filteredFrames = new ArrayList<Frame>();
		
		for (Iterator iter = frames.iterator(); iter.hasNext();) {
			Frame frame = (Frame) iter.next();
			if (isValid(frame)) {
				filteredFrames.add(frame);
			}			
		}
		
		return filteredFrames;
	}

	abstract public boolean isValid(Frame frame);

	public Object getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}

}
