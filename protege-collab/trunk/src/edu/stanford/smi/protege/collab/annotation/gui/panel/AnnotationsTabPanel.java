package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsComboBoxUtil;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsTreeFinder;
import edu.stanford.smi.protege.collab.annotation.gui.renderer.AnnotationsRenderer;
import edu.stanford.smi.protege.collab.changes.ChangeOntologyUtil;
import edu.stanford.smi.protege.model.Cls;
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
import edu.stanford.smi.protegex.changes.ChangeCreateUtil;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.model.ChangeModel.AnnotationCls;
import edu.stanford.smi.protegex.server_changes.model.generated.Annotation;
import edu.stanford.smi.protegex.server_changes.model.generated.Vote;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public abstract class AnnotationsTabPanel extends SelectableContainer {
	private SelectableTree annotationsTree;
	private LabeledComponent labeledComponent;
	private JComboBox annotationsComboBox;
	private JComboBox ratingComboBox;
	private JComboBox filterComboBox;
	
	private AllowableAction viewAction;
	private AllowableAction createAction;
	private AllowableAction replyAction;
	
	private Instance currentInstance = null;
	private KnowledgeBase kb;
	
	
	public AnnotationsTabPanel(KnowledgeBase kb) {
		this(kb, "Annotations Tree");
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
		
		getCreateAction().setAllowed(false);
		labeledComponent.addHeaderButton(getViewAction());
								
		annotationsComboBox = new JComboBox();		
		annotationsComboBox.setRenderer(new FrameRenderer());
		updateAnnotationsComboBoxItems();
		
		ratingComboBox = new JComboBox(new String[]{"Rate this ..." , "*****", "****", "***", "**", "*"});
		
		filterComboBox = new JComboBox(new String[]{"Filter ..." , "By author ...", "By annotation type ...", "By date ...", "Complex filter ..."});
		
		JPanel littlePanel = new JPanel(new BorderLayout());
		
		JPanel smallerPanel = new JPanel(new BorderLayout());
		smallerPanel.add(ratingComboBox, BorderLayout.WEST);
		smallerPanel.add(filterComboBox, BorderLayout.EAST);
		
			
		littlePanel.add(smallerPanel, BorderLayout.WEST);
		littlePanel.add(annotationsComboBox, BorderLayout.EAST);
		
		annotationsComboBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onAnnotationTypeChange();				
			}
			
		});

		
		ratingComboBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				onAnnotationRatingChange();				
			}
			
		});

		
		labeledComponent.setHeaderComponent(littlePanel);
			
		//change this
		labeledComponent.setFooterComponent(new AnnotationsTreeFinder(ChangeOntologyUtil.getChangesKb(kb), annotationsTree));
	
		annotationsTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent arg0) {
				onAnnotationTreeSelectionChange();			
			}			
		});
				
		
		add(labeledComponent);
	}
	
	protected void onAnnotationTreeSelectionChange() {
		updateAnnotationsComboBoxItems();
		ratingComboBox.setSelectedIndex(0);		
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
		KnowledgeBase changesKb = ChangesProject.getChangesKB(getKnowledgeBase()); 
		
		Collection selectionColl = getAnnotationsTree().getSelection();
		
		Instance selection = (selectionColl == null) ? null : (Instance) CollectionUtilities.getFirstItem(selectionColl);
		
		annotationsComboBox.removeAllItems();
		
		annotationsComboBox.addItem("Select annotation type ...");
		
		for (AnnotationCls annotCls : AnnotationsComboBoxUtil.getAnnotationsComboBoxUtil(changesKb).getAllowableAnnotationTypes(selection)) {
			annotationsComboBox.addItem(annotCls);
		}
				
	}


	@Override
	public void setEnabled(boolean enabled) {
		System.out.println(enabled);
		super.setEnabled(enabled);
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
		
		replyAction = new AllowableAction("reply", AnnotationsIcons.getCommentIcon(), getAnnotationsTree()) {

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

}
