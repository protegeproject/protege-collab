package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
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
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.ViewAction;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public abstract class AbstractAnnotationsTabPanel extends SelectableContainer {	
	private static final long serialVersionUID = 6203059347975153193L;

	public final static String NEW_ANNOTATION_DEFAULT_BODY_TEXT = "(Enter text here)";

	private SelectableTree annotationsTree;
	private LabeledComponent labeledComponent;
	private LabeledComponent outerLC;
	private JToolBar toolbar;
	private JComboBox annotationsComboBox;
	private JComboBox filterComboBox;
	private FilterValueComponent filterValueComponent;
	private JComponent filterValueComponentComponent;
	private JButton filterButton;
	private AnnotationsTreeFinder treeFinder;
	private AnnotationsRenderer annotationsTreeRenderer;
	private AnnotationsRenderer annotationsComboBoxRenderer;
	
	private TreeFilter<AnnotatableThing> treeFilter;

	private AllowableAction viewAction;
	private AllowableAction createAction;
	private AllowableAction replyAction;
	private AllowableAction refreshAction;
	
	private TreeSelectionListener treeSelectionListener;

	private AnnotationsComboBoxUtil annotComboBoxUtil;
	private FilterTypeComboBoxUtil filterTypeComboBoxUtil;

	private Instance currentInstance = null;
	private KnowledgeBase kb;

	
	public AbstractAnnotationsTabPanel(KnowledgeBase kb) {
		this(kb, "Annotation Tab");
	}

	public AbstractAnnotationsTabPanel(KnowledgeBase kb, String tabName) {
		this.kb = kb;
		setName(tabName);

		annotationsTreeRenderer = new AnnotationsRenderer(kb);
		
		annotationsTree = createAnnotationsTree();
		setSelectable(annotationsTree);

		labeledComponent = new LabeledComponent(null, annotationsTree, true, true);
		labeledComponent.add(new JScrollPane(annotationsTree));		
		labeledComponent.addHeaderButton(getViewAction());
		labeledComponent.addHeaderButton(getRefreshAction());
		
		annotComboBoxUtil = new AnnotationsComboBoxUtil(ChAOKbManager.getChAOKb(kb));

		annotationsComboBox = new JComboBox();
		annotationsComboBox.setRenderer(annotationsComboBoxRenderer = new AnnotationsRenderer(kb));
		updateAnnotationsComboBoxItems();

		filterTypeComboBoxUtil = new FilterTypeComboBoxUtil(ChAOKbManager.getChAOKb(kb));
		filterComboBox = new JComboBox(filterTypeComboBoxUtil.getTypeFilterComboboxItems());
		filterComboBox.setSelectedIndex(1);
		
		treeFilter = null;
		filterValueComponent = new StringFilterComponent();
		filterValueComponentComponent = filterValueComponent.getValueComponent();

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

		JPanel annotationsTypeHeaderPanel = new JPanel(new GridBagLayout());				
		JButton replyButton = new JButton(getReplyAction());
		adjustFont(replyButton, Font.PLAIN, -2);
		JButton createButton = new JButton(getCreateAction());
		getCreateAction().setAllowed(true);
		adjustFont(createButton, Font.PLAIN, -2);
		JLabel usingLabel = new JLabel(" using a ");
		adjustFont(usingLabel, Font.ITALIC, -1);
		JLabel orLabel = new JLabel(" or ");
		adjustFont(orLabel, Font.ITALIC, -1);
		annotationsTypeHeaderPanel.add(createButton);
		annotationsTypeHeaderPanel.add(orLabel);
		annotationsTypeHeaderPanel.add(replyButton);		
		annotationsTypeHeaderPanel.add(usingLabel);
		annotationsTypeHeaderPanel.add(annotationsComboBox);
		
		labeledComponent.setHeaderComponent(annotationsTypeHeaderPanel);

		AnnotationFactory factory = new AnnotationFactory(ChAOKbManager.getChAOKb(kb));
		
		treeFinder = new AnnotationsTreeFinder(ChAOUtil.getChangesKb(kb), annotationsTree, factory.getAnnotatesSlot());
		labeledComponent.setFooterComponent(treeFinder);

		treeSelectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				onAnnotationTreeSelectionChange();
			}
		};
		annotationsTree.addTreeSelectionListener(treeSelectionListener);
	
		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.add(smallerPanel);

		LabeledComponent innerLC = new LabeledComponent(null, labeledComponent, true);
		innerLC.setHeaderComponent(toolbar);		
		outerLC = new LabeledComponent("No selection", innerLC, true);				
		add(outerLC);
	}

	
	private void adjustFont(JComponent comp, int style, int delta) {
		Font font = comp.getFont();
		Font newFont = font.deriveFont(style, font.getSize() + delta);
		comp.setForeground(new Color(100, 100, 100));
		comp.setFont(newFont);	
	}
	

	protected void onFilterTree() {
		Object value = filterValueComponent.getValue();
		if (value == null) {
			treeFilter = null;
		} else {
			if (treeFilter == null) {
				int selectedIndex = filterComboBox.getSelectedIndex();
				treeFilter = filterTypeComboBoxUtil.getTreeFilter(selectedIndex);
			}
			if (treeFilter != null) {
				treeFilter.setFilterValue(value);
			}
			//move this to some utility
			if (treeFilter instanceof SlotValueFilter) {
				int selectedIndex = filterComboBox.getSelectedIndex();
				Slot filterSlot = filterTypeComboBoxUtil.getAssociatedChangeSlot(selectedIndex);
				((SlotValueFilter)treeFilter).setSlot(filterSlot);
			}
		}		
		filterValueComponentComponent.setBackground(treeFilter != null ? Color.YELLOW : Color.WHITE);		
		refreshDisplay();
	}


	protected void onFilterTypeChange() {
		int selectedIndex = filterComboBox.getSelectedIndex();
		treeFilter = null;
		//TODO: find a better way to do this
		TreeFilter filter = filterTypeComboBoxUtil.getTreeFilter(selectedIndex);
		if (filterValueComponentComponent != null) {
			JComponent parent = (JComponent) filterValueComponentComponent.getParent();
			parent.remove(filterValueComponentComponent);
			filterValueComponent = FilterComponentUtil.getFilterValueComponent(filter, kb);
			filterValueComponentComponent = filterValueComponent.getValueComponent();
			parent.add(filterValueComponentComponent, BorderLayout.CENTER);
		}
		revalidate();
		repaint();		
		refreshDisplay();
	}


	protected void onAnnotationTreeSelectionChange() {
		updateAnnotationsComboBoxItems();
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
		annotationsTree.setCellRenderer(annotationsTreeRenderer);
		return annotationsTree;
	}


	public AllowableAction getReplyAction() {
		if (replyAction != null) {
			return replyAction;
		}
		replyAction = new AllowableAction("Reply", null, getAnnotationsTree()) {
		//replyAction = new AllowableAction("Reply", AnnotationsIcons.getReplyIcon(), getAnnotationsTree()) {
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
	
	protected AllowableAction getRefreshAction() {
		if (refreshAction != null) {
			return refreshAction;
		}
		refreshAction = new AllowableAction("Refresh", AnnotationsIcons.getReloadIcon(), null) {
			public void actionPerformed(ActionEvent e) {
				refreshDisplay();						
			}
		};
		return refreshAction;
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
		createAction = new CreateAction("New Thread", null) {
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
	
	public void setLabel(String label) {
		outerLC.setHeaderLabel(label);
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
	
	@Override
	public void dispose() {		
		annotationsTree.removeTreeSelectionListener(treeSelectionListener);
		annotationsTree.setCellRenderer(null);
		
		LazyTreeRoot root = (LazyTreeRoot)annotationsTree.getModel().getRoot();
		Collection roots = (Collection) root.getUserObject();
		for (Iterator iterator = roots.iterator(); iterator.hasNext();) {
			AbstractWrappedInstance node = (AbstractWrappedInstance) iterator.next();
			node.dispose();			
		}
		
		annotationsTree.setRoot(null);
		annotationsTree = null;
		
		annotationsComboBox.removeAllItems();
		annotationsComboBox.setRenderer(null);
		annotationsComboBox = null;
		
		filterTypeComboBoxUtil.dispose();
		filterTypeComboBoxUtil = null;
		annotComboBoxUtil.dispose();
		annotComboBoxUtil = null;
		
		annotationsTreeRenderer.dispose();
		annotationsTreeRenderer = null;
		annotationsComboBoxRenderer.dispose();
		annotationsComboBoxRenderer = null;
		
		treeFinder.dispose();
		treeFinder = null;
		
		currentInstance = null;
		kb = null;
				
		labeledComponent.removeAllHeaderButtons();
		labeledComponent.setFooterComponent(null);		
		
		viewAction = null;		
		createAction = null;
		refreshAction = null;
		replyAction = null;
		
		labeledComponent = null;
		toolbar = null;
		
		super.dispose();
	}
}
