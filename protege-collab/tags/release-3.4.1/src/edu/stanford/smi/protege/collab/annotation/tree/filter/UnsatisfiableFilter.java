package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;

public class UnsatisfiableFilter extends AbstractFilter<AnnotatableThing>{

	public UnsatisfiableFilter() {
		super();
	}

	@Override
	public Collection getFilteredCollection(Collection unfilteredCollection) {
		return new ArrayList();
	}

	@Override
	public boolean isValid(AnnotatableThing thing) {
		return false;
	}
}
