package edu.stanford.smi.protege.collab.annotation.tree.gui;

import edu.stanford.smi.protege.collab.annotation.tree.filter.DateFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TypeFilter;
import edu.stanford.smi.protege.model.KnowledgeBase;

public class FilterComponentUtil {
	
	public static FilterValueComponent getFilterValueComponent(TreeFilter filter, KnowledgeBase kb) {
		if (filter instanceof SlotValueFilter) {
			return new StringFilterComponent();
		} else if (filter instanceof DateFilter) {
			return new DateFilterComponent();
		} else if (filter instanceof TypeFilter) {
			return new TypeFilterComponent(kb);
		}
		
		return new StringFilterComponent();
	}
	
}
