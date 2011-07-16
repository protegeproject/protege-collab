package edu.stanford.smi.protege.collab.annotation.tree.gui;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsComboBoxUtil;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.AnnotationsRenderer;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Disposable;

public class TypeFilterComponent implements FilterValueComponent, Disposable {
	
	private static final String ANY_TYPE = "Any type";
	
	private KnowledgeBase kb;
	private JComboBox typeComboBox;
	private AnnotationsComboBoxUtil annotationsComboBoxUtil;
	private AnnotationsRenderer annotationsRenderer;

	public TypeFilterComponent(KnowledgeBase kb) {
		this.kb = kb;
		//clean up this
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(kb);
		annotationsComboBoxUtil = new AnnotationsComboBoxUtil(changesKb);
		typeComboBox = new JComboBox();
		typeComboBox.addItem(ANY_TYPE);
		annotationsRenderer = new AnnotationsRenderer(kb);
		typeComboBox.setRenderer(annotationsRenderer);	

		for (Cls annotCls : annotationsComboBoxUtil.getAllowableAnnotationTypes(null)) {
			typeComboBox.addItem(annotCls);
		}
		//annotationsComboBox.setSelectedItem(AnnotationCls.Comment);
	}

	public Object getValue() {
		return getSelectedAnnotationType();
	}

	public JComponent getValueComponent() {
		return typeComboBox;
	}

	public void setValue(Object value) {
		if (value == null) {
			typeComboBox.setSelectedItem(ANY_TYPE);
		} else 	if (value instanceof Annotation) {
			typeComboBox.setSelectedItem(value);
		}
	}

	private Cls getSelectedAnnotationType(){
		Object selection = typeComboBox.getSelectedItem();
		return selection instanceof Cls ? (Cls) selection : null;
	}
	
	public void dispose() {
		annotationsRenderer.dispose();
		annotationsRenderer = null;
		annotationsComboBoxUtil.dispose();
		annotationsComboBoxUtil = null;
		typeComboBox.setRenderer(null);
		typeComboBox.removeAllItems();
		typeComboBox = null;
		kb = null;
	}

}
