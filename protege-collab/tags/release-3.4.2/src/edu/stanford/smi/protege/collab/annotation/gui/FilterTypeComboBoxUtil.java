package edu.stanford.smi.protege.collab.annotation.gui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.collab.annotation.tree.filter.DateFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TypeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.gui.DateFilterComponent;
import edu.stanford.smi.protege.collab.annotation.tree.gui.FilterValueComponent;
import edu.stanford.smi.protege.collab.annotation.tree.gui.StatusFilterComponent;
import edu.stanford.smi.protege.collab.annotation.tree.gui.StringFilterComponent;
import edu.stanford.smi.protege.collab.annotation.tree.gui.TypeFilterComponent;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.Disposable;

public class FilterTypeComboBoxUtil implements Disposable {
	private HashMap<Integer, Slot> indexToChangesSlotMap = new HashMap<Integer, Slot>();
	private HashMap<Integer, String> indexToDescriptionMap = new HashMap<Integer, String>();
	private HashMap<Integer, Class> indexToFilterClassMap = new HashMap<Integer, Class>();
	private HashMap<Integer, Class> indexToCompMap = new HashMap<Integer, Class>();

	private KnowledgeBase kb;
	private KnowledgeBase changeKb;
	

	public FilterTypeComboBoxUtil(KnowledgeBase kb) {
		this.kb = kb;
		this.changeKb = ChAOKbManager.getChAOKb(kb);
			
		initializeAllFilterTypes();
	}	
	

	private void initializeAllFilterTypes() {
		ChangeFactory factory = new ChangeFactory(changeKb);
		indexToChangesSlotMap.put(new Integer(0), null);
		indexToDescriptionMap.put(new Integer(0), "<None>");
		indexToFilterClassMap.put(new Integer(0), null);
		indexToCompMap.put(0, null);

		indexToChangesSlotMap.put(new Integer(1), factory.getAuthorSlot());
		indexToDescriptionMap.put(new Integer(1), "By author...");
		indexToFilterClassMap.put(new Integer(1), SlotValueFilter.class);
		indexToCompMap.put(1, StringFilterComponent.class);

		indexToChangesSlotMap.put(new Integer(2), factory.getBodySlot());
		indexToDescriptionMap.put(new Integer(2), "By text...");
		indexToFilterClassMap.put(new Integer(2), SlotValueFilter.class);
		indexToCompMap.put(2, StringFilterComponent.class);

		//How to handle this in a more elegant way?
		indexToChangesSlotMap.put(new Integer(3), null);
		indexToDescriptionMap.put(new Integer(3), "By type...");
		indexToFilterClassMap.put(new Integer(3), TypeFilter.class);
		indexToCompMap.put(3, TypeFilterComponent.class);

		indexToChangesSlotMap.put(new Integer(4), null);
		indexToDescriptionMap.put(new Integer(4), "By date...");
		indexToFilterClassMap.put(new Integer(4), DateFilter.class);
		indexToCompMap.put(4, DateFilterComponent.class);
		
		indexToChangesSlotMap.put(new Integer(5), new AnnotationFactory(changeKb).getHasStatusSlot());
		indexToDescriptionMap.put(new Integer(5), "By status...");
		indexToFilterClassMap.put(new Integer(5), SlotValueFilter.class);
		indexToCompMap.put(5, StatusFilterComponent.class);
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

	public Class getFilterComponentClass(int index) {
		return indexToCompMap.get(index);
	}	
	
	public Slot getAssociatedChangeSlot(int index) {
		if (index < 0) {
			return null;
		}
		return indexToChangesSlotMap.get(new Integer(index));
	}

	/**
	 * Get the tree filter for the index passed as argument.
	 * Takes into accont also the <code>hide archived</code> configuration.
	 * It is is enabled, it will return an <code>AND</code> filter between the 
	 * filter at index and the filter for archived notes.
	 * @param index
	 * @return
	 */
	public TreeFilter<AnnotatableThing> getTreeFilter(int index) {
		Class filterClass = getFilterClass(index);
		if (filterClass == null) { return null; }
		TreeFilter<AnnotatableThing> filter = null;
		try {
			filter = (TreeFilter<AnnotatableThing>) filterClass.newInstance();
		} catch (Exception e) {
			// do nothing
		}
		return filter;
	}
	
	
	public FilterValueComponent getTreeFilterComponent(int index) {
		Class filterCompClass = getFilterComponentClass(index);		
		if (filterCompClass == null) { 
			return new StringFilterComponent(); 
		}
		FilterValueComponent filter = null;
		try {
			filter = (FilterValueComponent) filterCompClass.newInstance();
		} catch (Exception e) {		
			// do nothing
		}
		if (filter == null) { //hack
			try {
				Constructor constr = filterCompClass.getConstructor(new Class<?>[]{KnowledgeBase.class}); 
				filter = (FilterValueComponent) constr.newInstance(new Object[] {kb});
			} catch (Exception e) {		
				// do nothing
			}			
		}		

		return (filter == null) ? new StringFilterComponent() : filter;
	}
	
	public void dispose() {
		indexToChangesSlotMap.clear();
		indexToDescriptionMap.clear();
		indexToFilterClassMap.clear();		
		changeKb = null;
	}


}
