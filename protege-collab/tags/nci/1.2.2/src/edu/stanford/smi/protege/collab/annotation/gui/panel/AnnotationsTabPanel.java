package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

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
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.ViewAction;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.ChangeSlot;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Vote;


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
		
	private TreeFilter treeFilter;
	
	private AllowableAction viewAction;
	private AllowableAction createAction;
	private AllowableAction replyAction;
	
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
								
		annotationsComboBox = new JComboBox();		
		annotationsComboBox.setRenderer(new FrameRenderer());
		updateAnnotationsComboBoxItems();
		
		//ratingComboBox = new JComboBox(new String[]{"Rate this ..." , "*****", "****", "***", "**", "*"});
				
		filterComboBox = new JComboBox(FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil().getTypeFilterComboboxItems());
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
		Slot annotatesSlot = ChangeOntologyUtil.getChangeModel(kb).getSlot(ChangeSlot.annotates);
		labeledComponent.setFooterComponent(new AnnotationsTreeFinder(ChangeOntologyUtil.getChangesKb(kb), annotationsTree, annotatesSlot));
	
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
				treeFilter = FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil().getTreeFilter(selectedIndex);				
			}
			
			if (treeFilter != null) {
				treeFilter.setFilterValue(value);
			}
			
			//move this to some utility
			if (treeFilter instanceof SlotValueFilter) {
				int selectedIndex = filterComboBox.getSelectedIndex();
				Slot filterSlot = ChangeOntologyUtil.getChangeModel(kb).getSlot(FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil().getAssociatedChangeSlot(selectedIndex));
				
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
		TreeFilter filter = FilterTypeComboBoxUtil.getFilterTypeComboBoxUtil().getTreeFilter(selectedIndex);
	
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
		
		Vote instVote = (Vote) ChangeOntologyUtil.createAnnotationOnAnnotation(currentInstance.getKnowledgeBase(), parentAnnotation, AnnotationCls.FiveStarsVote);
		instVote.setVoteValue(rating);
		
		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();        
		selectedNode.childAdded(instVote);	
		ComponentUtilities.extendSelection(getAnnotationsTree(), instVote);		
	}


	protected void onAnnotationTypeChange() {
		AnnotationCls annotationType = getSelectedAnnotationType();
		
		createAction.setAllowed(annotationType != null);		
	}


	protected void updateAnnotationsComboBoxItems() {
		KnowledgeBase changesKb = ChangeOntologyUtil.getChangesKb(getKnowledgeBase()); 
		
		Collection selectionColl = getAnnotationsTree().getSelection();
		
		Instance selection = (selectionColl == null) ? null : (Instance) CollectionUtilities.getFirstItem(selectionColl);
		
		annotationsComboBox.removeAllItems();
		
		//annotationsComboBox.addItem("Select annotation type ...");
		
		for (AnnotationCls annotCls : AnnotationsComboBoxUtil.getAnnotationsComboBoxUtil(changesKb).getAllowableAnnotationTypes(selection)) {
			annotationsComboBox.addItem(annotCls);
		}
		
		annotationsComboBox.setSelectedItem(AnnotationCls.Comment);
				
	}

	
	public abstract void refreshDisplay();
	
	
	protected SelectableTree createAnnotationsTree() {
		SelectableTree annotationsTree = ComponentFactory.createSelectableTree(getViewAction());
		
		annotationsTree.setSelectionRow(0);
	
		annotationsTree.setAutoscrolls(true);
		annotationsTree.setShowsRootHandles(true);    
		annotationsTree.setCellRenderer(new AnnotationsRenderer());    
		
		return annotationsTree;    
	}
	
	
	public AllowableAction getReplyAction() {
		if (replyAction != null) {
			return replyAction;
		}
		
		replyAction = new AllowableAction("Reply", AnnotationsIcons.getCommentIcon(), getAnnotationsTree()) {

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
				if (o instanceof Instance) {
					onViewAnnotation((Instance)o);
				}
			}
		};
			
		return viewAction;		
	}
	
	protected void onViewAnnotation(Instance instance) {
		instance.getProject().show(instance);
	}
	
	
	protected void onReplyAnnotation() {
		AnnotationCls pickedAnnotationCls = getSelectedAnnotationType(); 
		
		if (pickedAnnotationCls == null) {
			return;
		}
		
		Collection selection = getAnnotationsTree().getSelection();
		
		Frame parentAnnotation = null;
		
		if (selection != null && selection.size() > 0) {
			parentAnnotation = (Frame) CollectionUtilities.getFirstItem(selection);
		}

		Annotation annotInst = ChangeOntologyUtil.createAnnotationOnAnnotation(currentInstance.getKnowledgeBase(), parentAnnotation, pickedAnnotationCls);
		ChangeOntologyUtil.fillAnnotationSystemFields(currentInstance.getKnowledgeBase(), annotInst);
		//put this in a constant sometimes
		annotInst.setBody(NEW_ANNOTATION_DEFAULT_BODY_TEXT);
		
		LazyTreeNode selectedNode = (LazyTreeNode) getAnnotationsTree().getLastSelectedPathComponent();        
		selectedNode.childAdded(annotInst);	
		ComponentUtilities.extendSelection(getAnnotationsTree(), annotInst);
		
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
	
	public AnnotationCls getSelectedAnnotationType(){
		Object selection = annotationsComboBox.getSelectedItem();		
		return ((selection instanceof AnnotationCls) ? (AnnotationCls) selection : null);
	}
	
	public TreeFilter getTreeFilter() {
		return treeFilter;
	}

	public JToolBar getToolbar() {
		return toolbar;
	}
	
	public Icon getIcon() {
		return null;
	}
	
}
