package edu.stanford.smi.protege.collab.annotation.tree.filter;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;

public class NotFilter extends AbstractFilter<AnnotatableThing> {

	private TreeFilter<AnnotatableThing> negatedFilter;
	
	public void setNegatedFilter(TreeFilter<AnnotatableThing> negatedFilter) {
		this.negatedFilter = negatedFilter;
	}
	
	public TreeFilter<AnnotatableThing> getNegatedFilter() {
		return negatedFilter;
	}
	
	@Override
	public boolean isValid(AnnotatableThing object) {
		return negatedFilter == null?  true : !negatedFilter.isValid(object);
	}

}
