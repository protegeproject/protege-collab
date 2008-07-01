package edu.stanford.smi.protege.collab.annotation.tree.filter;

import java.util.Collection;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;

public class TypeFilter extends AbstractFilter {	
	
	public TypeFilter() {
		super();
	}
	
	public TypeFilter(Cls type) {
		setFilterValue(type);
	}
	
	public Collection getFilteredCollection(Collection unfilteredCollection) {
		throw new UnsupportedOperationException();
	}

	public boolean isValid(Frame frame) {
		Cls type = (Cls) getFilterValue();
		
		if (type == null) {
			return true;
		}
		
		if (!(frame instanceof Instance)) {
			return false;
		}
		
		return (((Instance)frame).hasType(type));
	}
}
