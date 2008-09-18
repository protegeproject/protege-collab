package edu.stanford.smi.protege.collab.annotation.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.util.AnnotationCreationComparator;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.collab.annotation.tree.filter.TreeFilter;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LazyTreeNode;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeNode extends LazyTreeNode {
	private TreeFilter<AnnotatableThing> filter;

	public AnnotationsTreeNode(LazyTreeNode parent, AnnotatableThing node) {
        super(parent, node);
    }

	public AnnotationsTreeNode(AnnotationsTreeRoot root, AnnotatableThing node, TreeFilter<AnnotatableThing> filter) {
		this(root, node);
		this.filter = filter;
	}

	@Override
	protected LazyTreeNode createNode(Object o) {
		return new AnnotationsTreeNode(this, (AnnotatableThing) o);
	}

	@Override
	protected int getChildObjectCount() {
		//reimplement!!
		return getChildObjects().size();
	}


	@Override
	protected Collection<AnnotatableThing> getChildObjects() {
		KnowledgeBase kb = ((AbstractWrappedInstance)getAnnotatableThing()).getKnowledgeBase();
		Collection<AnnotatableThing> allChildren = new ArrayList<AnnotatableThing>();
		AnnotatableThing annotThing = getAnnotatableThing();
		if (annotThing != null && annotThing instanceof AnnotatableThing) {
			Collection<Annotation> assocAnnots = annotThing.getAssociatedAnnotations();
			//hack because factory does  not return the most specific objects - will fix later
			for (Annotation ann : assocAnnots) {
				allChildren.add(OntologyJavaMappingUtil.getSpecificObject(kb,
						((AbstractWrappedInstance)ann).getWrappedProtegeInstance(),
						Annotation.class));
			}
		}
		return filter(allChildren);
	}


	protected Collection<AnnotatableThing> filter(Collection<AnnotatableThing> allChildren) {
		if (filter == null) {
			return allChildren;
		}
		return filter.getFilteredCollection(allChildren);
	}

	private AnnotatableThing getAnnotatableThing() {
		return (AnnotatableThing) getUserObject();
	}

	@Override
	protected Comparator getComparator() {
		return new AnnotationCreationComparator();
	}

	public TreeFilter<AnnotatableThing> getFilter() {
		return filter;
	}

	public void setFilter(TreeFilter<AnnotatableThing> filter) {
		this.filter = filter;
	}
}
