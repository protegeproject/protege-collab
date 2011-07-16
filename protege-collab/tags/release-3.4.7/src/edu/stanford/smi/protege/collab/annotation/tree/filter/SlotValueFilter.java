package edu.stanford.smi.protege.collab.annotation.tree.filter;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protege.util.StringMatcher;

public class SlotValueFilter extends AbstractFilter<AnnotatableThing> {
	private Slot slot;

	public SlotValueFilter() {
		super();
	}

	public SlotValueFilter(Slot slot) {
		this(slot, null);
	}

	public SlotValueFilter(Slot slot, Object value) {
		this.slot = slot;

		if (value instanceof String && !value.toString().endsWith("*")) {
			value = value + "*";
		}

		setFilterValue(value);
	}

	@Override
	public boolean isValid(AnnotatableThing object) {
		Object value = getFilterValue();

		if (value == null || slot == null) {
			return true;
		}
		Instance inst = ((AbstractWrappedInstance) object).getWrappedProtegeInstance();
		Object frameSlotValue = inst.getOwnSlotValue(slot);

		if (frameSlotValue == null) {
			if (slot.getValueType() == ValueType.BOOLEAN) {
				frameSlotValue = false;
			} else {
				return false;
			}
		}
		//put some extra checks
		if (value instanceof String && slot.getValueType() == ValueType.STRING) {
			if (!value.toString().endsWith("*")) {
				value = value + "*";
			}

			StringMatcher stringMatcher = new SimpleStringMatcher((String)value);

			return stringMatcher.isMatch((String)frameSlotValue);
		}

		return frameSlotValue.equals(value);
	}


	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

}
