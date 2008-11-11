package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.collab.annotation.tree.filter.DateFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TypeFilter;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public class FilterTypeComboBoxUtil {
	private FilterTypeComboBoxUtil filterTypeComboBoxUtil;

	private HashMap<Integer, Slot> indexToChangesSlotMap = new HashMap<Integer, Slot>();
	private HashMap<Integer, String> indexToDescriptionMap = new HashMap<Integer, String>();
	private HashMap<Integer, Class> indexToFilterClassMap = new HashMap<Integer, Class>();

	private KnowledgeBase changeKb;

	public FilterTypeComboBoxUtil(KnowledgeBase changeKb) {
		this.changeKb = changeKb;
		initializeAllFilterTypes();
	}

	private void initializeAllFilterTypes() {
		ChangeFactory factory = new ChangeFactory(changeKb);
		indexToChangesSlotMap.put(new Integer(0), null);
		indexToDescriptionMap.put(new Integer(0), "<None>");
		indexToFilterClassMap.put(new Integer(0), null);

		indexToChangesSlotMap.put(new Integer(1), factory.getAuthorSlot());
		indexToDescriptionMap.put(new Integer(1), "By author...");
		indexToFilterClassMap.put(new Integer(1), SlotValueFilter.class);

		indexToChangesSlotMap.put(new Integer(2), factory.getBodySlot());
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

	public Slot getAssociatedChangeSlot(int index) {
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
