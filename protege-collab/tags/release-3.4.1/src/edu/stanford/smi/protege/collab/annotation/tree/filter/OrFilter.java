package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;

public class OrFilter extends AbstractFilter<AnnotatableThing> {
	private Collection<TreeFilter<AnnotatableThing>> filters = new ArrayList<TreeFilter<AnnotatableThing>>();

	public OrFilter() {
		super();
	}

	public OrFilter(Collection<TreeFilter<AnnotatableThing>> andFilterCollection) {
		filters = andFilterCollection;
	}

	@Override
	public boolean isValid(AnnotatableThing object) {
		for (TreeFilter<AnnotatableThing> filter : filters) {
			if (filter != null && filter.isValid(object)){
				return true;
			}
		}
		return false;
	}

	public Collection<TreeFilter<AnnotatableThing>> getFilters() {
		return filters;
	}

	public void setFilters(Collection<TreeFilter<AnnotatableThing>> filters) {
		this.filters = filters;
	}

}
