package edu.stanford.smi.protege.collab.annotation.tree.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.collab.annotation.tree.filter.AndFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.DateFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.OrFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TypeFilter;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public class ComplexFilterComponent implements FilterValueComponent {

	private KnowledgeBase kb;
	private JComponent valueComponent;

	private SlotValueFilter authorFilter;
	private SlotValueFilter annnotationTextFilter;
	private TypeFilter typeFilter;
	private DateFilter dateFilter;

	private StringFilterComponent authorFilterComp;
	private StringFilterComponent annnotationTextFilterComp;
	private TypeFilterComponent typeFilterComp;
	private DateFilterComponent dateFilterComp;

	private JRadioButton andRadioButton;
	private JRadioButton orRadioButton;

	public ComplexFilterComponent(KnowledgeBase kb) {
		this.kb = kb;

		KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);
		ChangeFactory factory = new ChangeFactory(changesKb);

		Slot authorSlot = factory.getAuthorSlot();
		Slot annotationTextSlot = factory.getBodySlot();

		authorFilter = new SlotValueFilter(authorSlot);
		annnotationTextFilter = new SlotValueFilter(annotationTextSlot);
		typeFilter = new TypeFilter();
		dateFilter = new DateFilter();

		authorFilterComp = new StringFilterComponent();
		annnotationTextFilterComp = new StringFilterComponent();
		typeFilterComp = new TypeFilterComponent(kb);
		dateFilterComp = new DateFilterComponent();

		valueComponent = buildGUI();
	}


	private JComponent buildGUI() {
		//reimplement this nicer sometimes
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1, 10, 5));

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(authorFilterComp.getValueComponent(), BorderLayout.CENTER);
		p1.add(new JLabel("Author:                "), BorderLayout.WEST);
		panel.add(p1);

		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(annnotationTextFilterComp.getValueComponent(), BorderLayout.CENTER);
		p2.add(new JLabel("Annotation text: "), BorderLayout.WEST);
		panel.add(p2);

		JPanel p3 = new JPanel(new BorderLayout());
		p3.add(typeFilterComp.getValueComponent(), BorderLayout.CENTER);
		p3.add(new JLabel("Annotation type: "), BorderLayout.WEST);
		panel.add(p3);

		JPanel p4 = new JPanel(new BorderLayout());
		p4.add(dateFilterComp.getValueComponent(), BorderLayout.CENTER);
		p4.add(new JLabel("Date:          "), BorderLayout.WEST);
		panel.add(p4);

		andRadioButton = new JRadioButton("AND ");
		andRadioButton.setSelected(true);
		orRadioButton = new JRadioButton("OR ");

		ButtonGroup group = new ButtonGroup();
		group.add(andRadioButton);
		group.add(orRadioButton);

		JPanel p5 = new JPanel();
		p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
		p5.add(new JLabel("Condition "));
		p5.add(andRadioButton);
		p5.add(orRadioButton);
		panel.add(p5);

		return panel;
	}


	public JComponent getValueComponent() {
		return valueComponent;
	}

	public Object getValue() {
		throw new UnsupportedOperationException();
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	public TreeFilter<AnnotatableThing> getComplexFilter() {
		authorFilter.setFilterValue(authorFilterComp.getValue());
		annnotationTextFilter.setFilterValue(annnotationTextFilterComp.getValue());
		typeFilter.setFilterValue(typeFilterComp.getValue());
		dateFilter.setFilterValue(dateFilterComp.getValue());

		Collection<TreeFilter<AnnotatableThing>> filters = new ArrayList<TreeFilter<AnnotatableThing>>();
		filters.add(authorFilter);
		filters.add(annnotationTextFilter);
		filters.add(typeFilter);
		filters.add(dateFilter);

		if (andRadioButton.isSelected()) {
			return new AndFilter(filters);
		} else {
			return new OrFilter(filters);
		}
	}

}
