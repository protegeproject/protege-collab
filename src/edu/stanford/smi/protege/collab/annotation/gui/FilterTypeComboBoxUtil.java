package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.smi.protege.collab.annotation.tree.filter.DateFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TypeFilter;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;

public class FilterTypeComboBoxUtil {	
	private static FilterTypeComboBoxUtil filterTypeComboBoxUtil;
	
	private static HashMap<Integer, ChangeSlot> indexToChangesSlotMap = new HashMap<Integer, ChangeSlot>();
	private static HashMap<Integer, String> indexToDescriptionMap = new HashMap<Integer, String>();
	private static HashMap<Integer, Class> indexToFilterClassMap = new HashMap<Integer, Class>();
	
	private FilterTypeComboBoxUtil() {	
		initializeAllFilterTypes();
	}
	
	private void initializeAllFilterTypes() {
		indexToChangesSlotMap.put(new Integer(0), null);
		indexToDescriptionMap.put(new Integer(0), "<None>");
		indexToFilterClassMap.put(new Integer(0), null);

		indexToChangesSlotMap.put(new Integer(1), ChangeSlot.author);
		indexToDescriptionMap.put(new Integer(1), "By author...");
		indexToFilterClassMap.put(new Integer(1), SlotValueFilter.class);
		
		indexToChangesSlotMap.put(new Integer(2), ChangeSlot.body);
		indexToDescriptionMap.put(new Integer(2), "By annotation text...");
		indexToFilterClassMap.put(new Integer(2), SlotValueFilter.class);
		
		//How to handle this in a more elegant way?
		indexToChangesSlotMap.put(new Integer(3), null);
		indexToDescriptionMap.put(new Integer(3), "By annotation type...");
		indexToFilterClassMap.put(new Integer(3), TypeFilter.class);
		
		indexToChangesSlotMap.put(new Integer(4), null);
		indexToDescriptionMap.put(new Integer(4), "By date...");
		indexToFilterClassMap.put(new Integer(4), DateFilter.class);
	}

		
	public static FilterTypeComboBoxUtil getFilterTypeComboBoxUtil(){
		if (filterTypeComboBoxUtil == null) {
			filterTypeComboBoxUtil = new FilterTypeComboBoxUtil();
		}
		
		return filterTypeComboBoxUtil;
	}
	
	
	public String[] getTypeFilterComboboxItems() {
		Collection<String> items = new ArrayList<String>();
		
		ArrayList<Integer> keys = new ArrayList<Integer>(indexToDescriptionMap.keySet());
		Collections.sort(keys);
		
		for (Integer index : keys) {
			String value = indexToDescriptionMap.get(index);
			
			if (value != null) {
				items.add(value);
			}
		}
		
		String[] itemsStringList = new String[indexToDescriptionMap.keySet().size()];
		
		items.toArray(itemsStringList);
		
		return itemsStringList;
	}

	public Class getFilterClass(int index) {		
		return indexToFilterClassMap.get(new Integer(index));
	}
	
	public ChangeSlot getAssociatedChangeSlot(int index) {
		if (index < 0) {
			return null;
		}
		return indexToChangesSlotMap.get(new Integer(index));
	}	
	
	public TreeFilter getTreeFilter(int index) {
		Class filterClass = getFilterClass(index);
		
		if (filterClass == null) {
			return null;
		}
		
		TreeFilter filter = null;
		
		try {			
			filter = (TreeFilter) filterClass.newInstance();		
		} catch (Exception e) {
			// do nothing
		}
		
		return filter;
		
	}
	
	
}
