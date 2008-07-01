package edu.stanford.smi.protege.collab.annotation.tree.gui;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsComboBoxUtil;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;

public class TypeFilterComponent implements FilterValueComponent {
	private KnowledgeBase kb;
	private JComboBox typeComboBox;
	
	public TypeFilterComponent(KnowledgeBase kb) {
		this.kb = kb;

		//clean up this
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(kb, false);
		typeComboBox = new JComboBox();
		
		typeComboBox.addItem("Any type");
		
		typeComboBox.setRenderer(new FrameRenderer());
		
		ChangeModel changeModel = ChangeOntologyUtil.getChangeModel(kb);
		
		for (AnnotationCls annotCls : AnnotationsComboBoxUtil.getAnnotationsComboBoxUtil(changesKb).getAllowableAnnotationTypes(null)) {
			typeComboBox.addItem(changeModel.getCls(annotCls));
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
		if (value instanceof AnnotationCls) {
			typeComboBox.setSelectedItem(value);
		}
	}
	
	private Cls getSelectedAnnotationType(){
		Object selection = typeComboBox.getSelectedItem();		
		return ((selection instanceof Cls) ? (Cls) selection : null);
	}

}
