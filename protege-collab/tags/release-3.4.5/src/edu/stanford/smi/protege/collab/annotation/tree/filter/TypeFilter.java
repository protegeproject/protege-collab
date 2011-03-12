package edu.stanford.smi.protege.collab.annotation.tree.filter;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;

public class TypeFilter extends AbstractFilter<AnnotatableThing> {

	public TypeFilter() {
		super();
	}

	public TypeFilter(Cls type) {
		setFilterValue(type);
	}

	
	@Override
	public boolean isValid(AnnotatableThing object) {
		Cls type = (Cls) getFilterValue();

		if (type == null) {
			return true;
		}
		Instance inst = ((AbstractWrappedInstance) object).getWrappedProtegeInstance();
		return inst.hasType(type);
	}
}
