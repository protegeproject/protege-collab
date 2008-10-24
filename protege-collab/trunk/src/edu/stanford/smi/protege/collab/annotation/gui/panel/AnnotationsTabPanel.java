package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.Vote;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsComboBoxUtil;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.gui.FilterTypeComboBoxUtil;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.AnnotationsRenderer;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeFinder;
import edu.stanford.smi.protege.collab.annotation.tree.filter.SlotValueFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.gui.FilterComponentUtil;
import edu.stanford.smi.protege.collab.annotation.tree.gui.FilterValueComponent;
import edu.stanford.smi.protege.collab.annotation.tree.gui.StringFilterComponent;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.ViewAction;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public abstract class AnnotationsTabPanel extends SelectableContainer {
	public final static String NEW_ANNOTATION_DEFAULT_BODY_TEXT = "(Enter text here)";

	private SelectableTree annotationsTree;
	private LabeledComponent labeledComponent;
	private JToolBar toolbar;
	private JComboBox annotationsComboBox;
	private JComboBox ratingComboBox;
	private JComboBox filterComboBox;
	private FilterValueComponent filterValueComponent;
	private JComponent filterValueComponentComponent;
	private JButton filterButton;

	private TreeFilter<AnnotatableThing> treeFilter;

	private AllowableAction viewAction;
	private AllowableAction createAction;
	private AllowableAction replyAction;

	private AnnotationsComboBoxUtil annotComboBoxUtil;

	private Instance currentInstance = null;
	private KnowledgeBase kb;


	public AnnotationsTabPanel(KnowledgeBase kb) {
		this(kb, "Annotation Tab");
	}


	public AnnotationsTabPanel(KnowledgeBase kb, String tabName) {
		this.kb = kb;
		setName(tabName);

		annotationsTree = createAnnotationsTree();
		setSelectable(annotationsTree);

		labeledComponent = new LabeledComponent(tabName, annotationsTree, true);
		labeledComponent.add(new JScrollPane(annotationsTree));

		labeledComponent.addHeaderButton(getReplyAction());
		labeledComponent.addHeaderButton(getCreateAction());

		getCreateAction().setAllowed(true);
		labeledComponent.addHeaderButton(getViewAction());

		annotComboBoxUtil = new AnnotationsComboBoxUtil(ChAOKbManager.getChAOKb(kb));

		annotationsComboBox = new JComboBox();
		annotationsComboBox.setRenderer(new AnnotationsRenderer(ChAOKbManager.getChAOKb(kb)));
		updateAnnotationsComboBoxItems();

		//ratingComboBox = new JComboBox(new String[]{"Rate this ..." , "*****", "****", "***", "**", "*"});

		filterComboBox = new JComboBox(FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil(ChAOKbManager.getChAOKb(kb)).getTypeFilterComboboxItems());
		//TT: handle this more elegantly
		filterComboBox.setSelectedIndex(1);

		//TT change this!!!
		treeFilter = null;
		filterValueComponent = new StringFilterComponent();
		filterValueComponentComponent = filterValueComponent.getValueComponent();

		/*
		filterValueComponent.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					onFilterTree();
				}
			}
		});
		*/

		JPanel smallerPanel = new JPanel(new BorderLayout());
		JPanel filterComboPanel = new JPanel();
		filterComboPanel.setLayout(new BoxLayout(filterComboPanel, BoxLayout.X_AXIS));
		filterComboPanel.setBorder(BorderFactory.createEmptyBorder());

		filterComboPanel.add(new JLabel("Filter "));
		filterComboPanel.add(filterComboBox);
		smallerPanel.add(filterComboPanel, BorderLayout.WEST);

		smallerPanel.add(filterValueComponentComponent, BorderLayout.CENTER);

		filterButton = new JButton("Go");
		smallerPanel.add(filterButton, BorderLayout.EAST);

		filterComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				onFilterTypeChange();
			}
		});

		filterButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				onFilterTree();
			}

		});

		annotationsComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onAnnotationTypeChange();
			}
		});

		/*
		ratingComboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onAnnotationRatingChange();
			}
		});
		*/

		JPanel annotationsTypeHeaderPanel = new JPanel(new BorderLayout());
		annotationsTypeHeaderPanel.add(annotationsComboBox, BorderLayout.EAST);
		labeledComponent.setHeaderComponent(annotationsTypeHeaderPanel);

		//change this
		AnnotationFactory factory = new AnnotationFactory(ChAOKbManager.getChAOKb(kb));
		labeledComponent.setFooterComponent(new AnnotationsTreeFinder(ChAOUtil.getChangesKb(kb), annotationsTree, factory.getAnnotatesSlot()));

		annotationsTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				onAnnotationTreeSelectionChange();
			}
		});

		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.add(smallerPanel);

		add(toolbar, BorderLayout.NORTH);
		add(labeledComponent, BorderLayout.CENTER);
	}


	protected void onFilterTree() {

		Object value = filterValueComponent.getValue();

		if (value == null) {
			treeFilter = null;
		} else {

			if (treeFilter == null) {
				int selectedIndex = filterComboBox.getSelectedIndex();
				treeFilter = FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil(ChAOKbManager.getChAOKb(kb)).getTreeFilter(selectedIndex);
			}

			if (treeFilter != null) {
				treeFilter.setFilterValue(value);
			}

			//move this to some utility
			if (treeFilter instanceof SlotValueFilter) {
				int selectedIndex = filterComboBox.getSelectedIndex();
				Slot filterSlot = FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil(ChAOKbManager.getChAOKb(kb)).getAssociatedChangeSlot(selectedIndex);

				((SlotValueFilter)treeFilter).setSlot(filterSlot);
			}
		}

		if (treeFilter != null) {
			filterValueComponentComponent.setBackground(Color.YELLOW);
		} else {
			filterValueComponentComponent.setBackground(Color.WHITE);
		}

		refreshDisplay();
	}


	protected void onFilterTypeChange() {
		int selectedIndex = filterComboBox.getSelectedIndex();
		treeFilter = null;
		//reimplement this!
		TreeFilter filter = FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil(ChAOKbManager.getChAOKb(kb)).getTreeFilter(selectedIndex);

		if (filterValueComponentComponent != null) {
			JComponent parent = (JComponent) filterValueComponentComponent.getParent();
			parent.remove(filterValueComponentComponent);
			filterValueComponent = FilterComponentUtil.getFilterValueComponent(filter, kb);
			filterValueComponentComponent = filterValueComponent.getValueComponent();
			parent.add(filterValueComponentComponent, BorderLayout.CENTER);
		}
		repaint();
		revalidate();
		refreshDisplay();
	}


	protected void onAnnotationTreeSelectionChange() {
		updateAnnotationsComboBoxItems();
	}


	protected void onAnnotationRatingChange() {
		String rating = (String) ratingComboBox.getSelectedItem();
		//do this better later
		if (!rating.contains("*")) {
			return;
		}

		Collection selection = getAnnotationsTree().getSelection();
		Instance parentAnnotation = null;
		if (selection != null && selection.size() > 0) {
			parentAnnotation = (Instance) CollectionUtilities.getFirstItem(selection);
		}
		if (parentAnnotation == null) {
			return;
		}

		AnnotationFactory factory = new AnnotationFactory(ChAOKbManager.getChAOKb(kb));
		Vote instVote = (Vote) ChAOUtil.createAnnotationOnAnnotation(currentInstance.getKnowledgeBase(), parentAnnotation, factory.getFiveStarsVoteClass());
		instVote.setVoteValue(rating);

		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();
		selectedNode.childAdded(instVote);
		ComponentUtilities.extendSelection(getAnnotationsTree(), instVote);
	}


	protected void onAnnotationTypeChange() {
		Cls annotationType = getSelectedAnnotationType();
		createAction.setAllowed(annotationType != null);
	}


	protected void updateAnnotationsComboBoxItems() {
		KnowledgeBase changesKb = ChAOUtil.getChangesKb(getKnowledgeBase());
		Collection selectionColl = getAnnotationsTree().getSelection();
		AnnotatableThing selection = (AnnotatableThing) (selectionColl == null ? null : CollectionUtilities.getFirstItem(selectionColl));
		annotationsComboBox.removeAllItems();
		//annotationsComboBox.addItem("Select annotation type ...");
		for (Cls annotCls : annotComboBoxUtil.getAllowableAnnotationTypes(selection)) {
			annotationsComboBox.addItem(annotCls);
		}
		AnnotationFactory factory = new AnnotationFactory(changesKb);
		annotationsComboBox.setSelectedItem(factory.getCommentClass());
	}


	public abstract void refreshDisplay();


	protected SelectableTree createAnnotationsTree() {
		SelectableTree annotationsTree = ComponentFactory.createSelectableTree(getViewAction());
		annotationsTree.setSelectionRow(0);
		annotationsTree.setAutoscrolls(true);
		annotationsTree.setShowsRootHandles(true);
		annotationsTree.setCellRenderer(new AnnotationsRenderer(ChAOKbManager.getChAOKb(kb)));
		return annotationsTree;
	}


	public AllowableAction getReplyAction() {
		if (replyAction != null) {
			return replyAction;
		}

		replyAction = new AllowableAction("Reply", AnnotationsIcons.getReplyIcon(), getAnnotationsTree()) {
			public void actionPerformed(ActionEvent arg0) {
				onReplyAnnotation();
			}
		};

		return replyAction;
	}

	protected AllowableAction getViewAction() {
		if (viewAction != null) {
			return viewAction;
		}

		viewAction = new ViewAction("View Annotation", this) {
			@Override
			public void onView(Object o) {
				if (o instanceof AnnotatableThing) {
					onViewAnnotation((AnnotatableThing)o);
				}
			}
		};

		return viewAction;
	}

	protected void onViewAnnotation(AnnotatableThing thing) {
		Instance instance = ((AbstractWrappedInstance)thing).getWrappedProtegeInstance();
		instance.getProject().show(instance);
	}


	protected void onReplyAnnotation() {
		Cls pickedAnnotationCls = getSelectedAnnotationType();

		if (pickedAnnotationCls == null) {
			return;
		}

		Collection selection = getAnnotationsTree().getSelection();

		Annotation annot = OntologyJavaMappingUtil.createObject(ChAOKbManager.getChAOKb(kb), null, pickedAnnotationCls.getName(), Annotation.class);
		annot.setAnnotates(selection);
		ChAOUtil.fillAnnotationSystemFields(currentInstance.getKnowledgeBase(), annot);

		try {
			AnnotatableThing repliedToAnnotThing = (AnnotatableThing) CollectionUtilities.getFirstItem(selection);
			Annotation repliedToAnn = repliedToAnnotThing.as(Annotation.class);
			if (repliedToAnn != null) {
				String repliedToSubj = repliedToAnn.getSubject();
				annot.setSubject("Re: " + (repliedToSubj == null ? "" : repliedToSubj));
				annot.setBody(getQuotedReplyText(repliedToAnn));
			} else {
				annot.setBody(NEW_ANNOTATION_DEFAULT_BODY_TEXT);
			}
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Errors at creating the reply of " + selection, e);
		}

		Instance annotInst = ((AbstractWrappedInstance)annot).getWrappedProtegeInstance();
		InstanceDisplay instDispl = new InstanceDisplay(getChaoKb().getProject(), false, true);
		instDispl.setInstance(annotInst);

		Object[] options = {"Post", "Cancel"};
		int ret = JOptionPane.showOptionDialog(this, instDispl, "New reply (" +
				annotInst.getDirectType().getBrowserText() + ")",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				AnnotationsIcons.getMailIcon(),
				options,
				options[0]);

		if (ret == JOptionPane.OK_OPTION) {
			LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();
			selectedNode.childAdded(annot);
			ComponentUtilities.extendSelection(getAnnotationsTree(), annot);
		} else {
			annotInst.delete();
		}
	}

	private String getQuotedReplyText(Annotation annotation) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		String user = annotation.getAuthor();
		String date = sdf.format(((DefaultTimestamp)annotation.getCreated()).getDateParsed());
		String body = annotation.getBody();

		if (body == null) {
			body = "";
		} else {
			if (body.indexOf("<html>") >=0 && body.indexOf("<body>") > 0) { //HTML message
				try {
					int index = body.indexOf("<body>");
					StringBuffer buffer = new StringBuffer();
					buffer.append(body.substring(0, index + 6));
					buffer.append("<br><br>");
					buffer.append("=== On ");
					buffer.append(date);
					buffer.append(", ");
					buffer.append(user);
					buffer.append(" wrote:<br><br>");
					buffer.append(body.substring(index+6));
					body = buffer.toString();
				} catch (Exception e) {
					Log.emptyCatchBlock(e);
				}
			} else { //text message
				String line = "\n\n=== On " + date + ", " + user + " wrote:\n\n";
				body = line + body;
			}
		}
		return body;
	}


	protected AllowableAction getCreateAction() {
		if (createAction != null) {
			return createAction;
		}

		createAction = new CreateAction("Create Annotation", Icons.getCreateInstanceNoteIcon()) {
			@Override
			public void onCreate() {
				onCreateAnnotation();
			}
		};

		return createAction;
	}


	protected abstract void onCreateAnnotation();

	public void setInstance(Instance instance) {
		if (currentInstance != instance) {
			currentInstance = instance;

			refreshDisplay();
		}
	}

	public SelectableTree getAnnotationsTree() {
		return annotationsTree;
	}

	public Instance getCurrentInstance() {
		return currentInstance;
	}

	public LabeledComponent getLabeledComponent() {
		return labeledComponent;
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	public KnowledgeBase getChaoKb() {
		return ChAOKbManager.getChAOKb(kb);
	}

	public Cls getSelectedAnnotationType(){
		Object selection = annotationsComboBox.getSelectedItem();
		return selection instanceof Cls ? (Cls) selection : null;
	}

	public TreeFilter<AnnotatableThing> getTreeFilter() {
		return treeFilter;
	}

	public JToolBar getToolbar() {
		return toolbar;
	}

	public Icon getIcon() {
		return null;
	}

}
