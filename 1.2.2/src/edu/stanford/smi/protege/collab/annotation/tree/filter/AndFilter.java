package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Frame;

public class AndFilter extends AbstractFilter {
	private Collection<TreeFilter> filters = new ArrayList<TreeFilter>();

	public AndFilter() {
		super();
	}
	
	public AndFilter(Collection<TreeFilter> andFilterCollection) {
		filters = andFilterCollection;
	}	

	public boolean isValid(Frame frame) {
		for (TreeFilter filter : filters) {
			if (filter != null && !filter.isValid(frame)){
				return false;
			}
		}
		return true;
	}
	
	public Collection<TreeFilter> getFilters() {
		return filters;
	}
	
	public void setFilters(Collection<TreeFilter> filters) {
		this.filters = filters;
	}

}
