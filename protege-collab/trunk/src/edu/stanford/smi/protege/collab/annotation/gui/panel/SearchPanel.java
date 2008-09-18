package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.annotation.tree.AnnotationsTreeRoot;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.collab.annotation.tree.filter.UnsatisfiableFilter;
import edu.stanford.smi.protege.collab.annotation.tree.gui.ComplexFilterComponent;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableContainer;

public class SearchPanel extends AnnotationsTabPanel {

	private AllowableAction viewAnnotatedEntityAction;

	private ComplexFilterComponent complexFilterComp;
	private TreeFilter<AnnotatableThing> complexFilter;

	public SearchPanel(KnowledgeBase kb) {
		super(kb, "Search");

		fixGUI();
	}

	protected void fixGUI() {
		LabeledComponent labledComponent = getLabeledComponent();
		labledComponent.setFooterComponent(null);

		Collection headerButtonCollection = labledComponent.getHeaderButtons();
		for (int i=0; i < headerButtonCollection.size(); i++) {
			labledComponent.removeHeaderButton(0);
		}

		labledComponent.addHeaderButton(getViewAction());
		labledComponent.addHeaderButton(getViewAnnotatedObjectAction());
		labledComponent.setHeaderComponent(null);
		labledComponent.setHeaderLabel("Search Results");


		SelectableContainer parent = (SelectableContainer) labledComponent.getParent();

		parent.remove(labledComponent);
		parent.remove(getToolbar());

		complexFilterComp = new ComplexFilterComponent(getKnowledgeBase());

		LabeledComponent searchPanel = new LabeledComponent("Search", complexFilterComp.getValueComponent());
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSearch();
			}
		});
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.add(searchButton);
		searchPanel.setFooterComponent(buttonPanel);

		JScrollPane searchScrollPane = new JScrollPane(searchPanel);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchScrollPane, labledComponent);
		Dimension nullDim = new Dimension(0, 0);
		searchScrollPane.setMinimumSize(nullDim);
		labledComponent.setMinimumSize(nullDim);
		splitPane.setDividerLocation(175 + splitPane.getInsets().bottom);
		splitPane.setOneTouchExpandable(true);

		parent.add(splitPane, BorderLayout.CENTER);

	}

	protected AllowableAction getViewAnnotatedObjectAction() {
		if (viewAnnotatedEntityAction == null) {
			viewAnnotatedEntityAction = new AllowableAction("View annotated entity",
					AnnotationsIcons.getOntologyAnnotationIcon(), getAnnotationsTree()) {

						public void actionPerformed(ActionEvent arg0) {
							onViewAnnotatedEntities(getSelection());
						}
			};
		}

		return viewAnnotatedEntityAction;
	}

	protected void onViewAnnotatedEntities(Collection annotations) {
		for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
			Object annotationObj = iterator.next();
			if (annotationObj instanceof Annotation) {
				Annotation annotation = (Annotation) annotationObj;
				Collection annotatedEntities = annotation.getAnnotates();

				if (annotatedEntities != null) {
					for (Iterator iterator2 = annotatedEntities.iterator(); iterator2.hasNext();) {
						AnnotatableThing annotatedEntity = (AnnotatableThing) iterator2.next();
						onViewAnnotatedEntity(annotatedEntity);
					}
				}
			}
		}
	}

	protected void onViewAnnotatedEntity(AnnotatableThing annotatedEntity) {
		Project prj = getKnowledgeBase().getProject();

		if (annotatedEntity instanceof Ontology_Component) {
			Ontology_Component oc = (Ontology_Component) annotatedEntity;

			String name = oc.getCurrentName();
			if (name != null) {
				Instance inst = getKnowledgeBase().getInstance(name);
				if (inst != null) {
					prj.show(inst);
					return;
				}
			}
		}

		//all other cases just show the form
		ChAOKbManager.getChAOKb(getKnowledgeBase()).getProject().show(((AbstractWrappedInstance)annotatedEntity).getWrappedProtegeInstance());
	}

	protected void onSearch() {
		complexFilter = complexFilterComp.getComplexFilter();
		refreshDisplayAfterSearch();
	}

	@Override
	protected void onCreateAnnotation() {
		//	not applicable
	}

	@Override
	public void refreshDisplay() {
		// do nothing
	}

	private void refreshDisplayAfterSearch() {
		//TODO: the search should be executed on the server
		Collection<Annotation> annotationsRoots = ChAOUtil.getAnnotations(getKnowledgeBase());
		//TT: Took out the search through the changes - it is too expensive to do on the client side
		//Collection<Change> changeAnnotationsRoots = ChangeOntologyUtil.getChangeInstances(getKnowledgeBase());

		List allRoots = new ArrayList(annotationsRoots);
		//allRoots.addAll(changeAnnotationsRoots);

		Collection filteredRoots = ChAOUtil.getFilteredCollection(allRoots, complexFilter);
		//Collections.sort(filteredRoots, new AnnotationCreationComparator());

		//hack, reimplement later
		TreeFilter<AnnotatableThing> filter = complexFilter;

		if (filter != null) {
			filter = new UnsatisfiableFilter();
		}
		AnnotationsTreeRoot root = new AnnotationsTreeRoot(filteredRoots, filter);
		getAnnotationsTree().setRoot(root);
	}

	@Override
	public Icon getIcon() {
		return Icons.getIcon(new ResourceKey("object.search"));
	}

}
