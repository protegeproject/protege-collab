package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Frame;

public class UnsatisfiableFilter extends AbstractFilter{

	public UnsatisfiableFilter() {
		super();
	}
	
	public Collection getFilteredCollection(Collection unfilteredCollection) {
		return new ArrayList();
	}

	public boolean isValid(Frame frame) {
		return false;
	}

}
