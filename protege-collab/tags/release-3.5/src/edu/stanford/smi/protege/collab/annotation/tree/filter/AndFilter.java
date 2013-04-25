package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;

public class AndFilter extends AbstractFilter<AnnotatableThing> {
	private Collection<TreeFilter<AnnotatableThing>> filters = new ArrayList<TreeFilter<AnnotatableThing>>();

	public AndFilter() {
		super();
	}

	public AndFilter(Collection<TreeFilter<AnnotatableThing>> andFilterCollection) {
		filters = andFilterCollection;
	}

	@Override
	public boolean isValid(AnnotatableThing object) {
		for (TreeFilter<AnnotatableThing> filter : filters) {
			if (filter != null && !filter.isValid(object)){
				return false;
			}
		}
		return true;
	}

	public Collection<TreeFilter<AnnotatableThing>> getFilters() {
		return filters;
	}

	public void setFilters(Collection<TreeFilter<AnnotatableThing>> filters) {
		this.filters = filters;
	}
	
	public void addFilter(TreeFilter<AnnotatableThing> filter) {
		this.filters.add(filter);
	}

}
