package edu.stanford.smi.protege.collab.annotation.tree.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.smi.protege.collab.annotation.gui.StatusComboBoxUtil;
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
	private SlotValueFilter statusFilter;
	private DateFilter dateFilter;

	private StringFilterComponent authorFilterComp;
	private StringFilterComponent annnotationTextFilterComp;
	private TypeFilterComponent typeFilterComp;
	private StatusFilterComponent statusFilterComp;
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
		statusFilter = new SlotValueFilter(new AnnotationFactory(changesKb).getHasStatusSlot());
		dateFilter = new DateFilter();

		authorFilterComp = new StringFilterComponent();
		annnotationTextFilterComp = new StringFilterComponent();
		typeFilterComp = new TypeFilterComponent(kb);
		statusFilterComp = new StatusFilterComponent(kb);
		dateFilterComp = new DateFilterComponent();

		valueComponent = buildGUI();
	}

	
	private JComponent buildGUI() {		
		JPanel panel = new JPanel();		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(authorFilterComp.getValueComponent(), BorderLayout.CENTER);
		p1.add(new JLabel("Author: "), BorderLayout.WEST);
		panel.add(p1);

		panel.add(Box.createVerticalStrut(3));
		
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(annnotationTextFilterComp.getValueComponent(), BorderLayout.CENTER);
		p2.add(new JLabel("Text:    "), BorderLayout.WEST);
		panel.add(p2);
		
		panel.add(Box.createVerticalStrut(3));

		JPanel p3 = new JPanel(new BorderLayout());
		p3.add(typeFilterComp.getValueComponent(), BorderLayout.CENTER);
		p3.add(new JLabel("Type:   "), BorderLayout.WEST);
		panel.add(p3);
		
		panel.add(Box.createVerticalStrut(3));
		
		JPanel p4 = new JPanel(new BorderLayout());
		p4.add(statusFilterComp.getValueComponent(), BorderLayout.CENTER);
		p4.add(new JLabel("Status:  "), BorderLayout.WEST);
		panel.add(p4);
		
		panel.add(Box.createVerticalStrut(3));

		JPanel p5 = new JPanel(new BorderLayout());
		p5.add(dateFilterComp.getValueComponent(), BorderLayout.CENTER);
		p5.add(new JLabel("Date:   "), BorderLayout.WEST);
		panel.add(p5);

		panel.add(Box.createVerticalStrut(3));
		
		andRadioButton = new JRadioButton("AND ");
		andRadioButton.setSelected(true);
		orRadioButton = new JRadioButton("OR ");

		ButtonGroup group = new ButtonGroup();
		group.add(andRadioButton);
		group.add(orRadioButton);

		JPanel p6 = new JPanel();
		p6.setLayout(new BoxLayout(p6, BoxLayout.X_AXIS));
		p6.add(new JLabel("Condition "));
		p6.add(andRadioButton);
		p6.add(orRadioButton);
		panel.add(p6);

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
		statusFilter.setFilterValue(statusFilterComp.getValue());
		dateFilter.setFilterValue(dateFilterComp.getValue());

		Collection<TreeFilter<AnnotatableThing>> filters = new ArrayList<TreeFilter<AnnotatableThing>>();
		filters.add(authorFilter);
		filters.add(annnotationTextFilter);
		filters.add(typeFilter);
		filters.add(statusFilter);
		filters.add(dateFilter);
		
		TreeFilter<AnnotatableThing> archivedFilter = getArchivedFilter();
		if (archivedFilter != null) {
			filters.add(archivedFilter);
		}

		if (andRadioButton.isSelected()) {
			return new AndFilter(filters);
		} else {
			return new OrFilter(filters);
		}
	}

	
	protected TreeFilter<AnnotatableThing> getArchivedFilter() {
		KnowledgeBase changeKb = ChAOKbManager.getChAOKb(kb);
		Slot archivedSlot = new AnnotationFactory(changeKb).getArchivedSlot();
		SlotValueFilter archivedNotesFilter = null;
		
		if (StatusComboBoxUtil.getHideArchived(kb.getProject()) && archivedSlot != null) {			
			archivedNotesFilter = new SlotValueFilter(archivedSlot);
			archivedNotesFilter.setFilterValue(Boolean.FALSE);			
		}
		
		return archivedNotesFilter;
	}
	
}
