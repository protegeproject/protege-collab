package edu.stanford.smi.protege.collab.annotation.tree.gui;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.smi.protege.collab.annotation.gui.StatusComboBoxUtil;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.Disposable;

public class StatusFilterComponent implements FilterValueComponent, Disposable {
	
	public static final String ANY_STATUS = "Any status";
	
	private KnowledgeBase kb;
	private JComboBox statusComboBox;
	private StatusComboBoxUtil statusComboBoxUtil;
		
	public StatusFilterComponent(KnowledgeBase kb) {
		this.kb = kb;
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(kb);
		statusComboBoxUtil = new StatusComboBoxUtil(changesKb);
		statusComboBox = new JComboBox();
		statusComboBox.addItem(ANY_STATUS);
		statusComboBox.setRenderer(new FrameRenderer());

		for (Instance annotCls : statusComboBoxUtil.getAllStatus(null)) {
			statusComboBox.addItem(annotCls);
		}	
	}

	public Object getValue() {
		return getSelectedStatus();
	}

	public JComponent getValueComponent() {
		return statusComboBox;
	}

	public void setValue(Object value) {
		if (value == null) {
			statusComboBox.setSelectedItem(ANY_STATUS);
		} else	if (value instanceof Annotation) {
			statusComboBox.setSelectedItem(value);
		}
	}

	private Instance getSelectedStatus(){
		Object selection = statusComboBox.getSelectedItem();
		return selection instanceof Instance ? (Instance) selection : null;
	}
	
	public void dispose() {		
		statusComboBoxUtil.dispose();
		statusComboBoxUtil = null;
		statusComboBox.setRenderer(null);
		statusComboBox.removeAllItems();
		statusComboBox = null;		
	}
	
}
