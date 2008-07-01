package edu.stanford.smi.protege.collab.annotation.tree.filter;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.SimpleStringMatcher;
import edu.stanford.smi.protege.util.StringMatcher;

public class SlotValueFilter extends AbstractFilter {
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
	
	public boolean isValid(Frame frame) {
		Object value = getFilterValue();
		
		if (value == null || slot == null) {
			return true;
		}
		
		Object frameSlotValue = frame.getOwnSlotValue(slot);
		
		if (frameSlotValue == null) {
			return false;
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
