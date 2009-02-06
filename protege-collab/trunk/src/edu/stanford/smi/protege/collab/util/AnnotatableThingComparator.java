package edu.stanford.smi.protege.collab.util;

import java.util.Comparator;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Class;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Individual;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Property;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;

//TODO: Move to change model util
/**
 * Generic comparator for all types of <code>AnnotatableThing</code>.
 * Compares <code>Annotation</code> objects by their creations types,
 * <code>Change</code> objects by their timestamp, and
 * <code>OntologyComponent</code> objects by their name.
 * @author ttania
 *
 */
public class AnnotatableThingComparator implements Comparator<AnnotatableThing> {

	public int compare(AnnotatableThing at1, AnnotatableThing at2) {
		if (at1 instanceof Annotation) {
			if (at2 instanceof Annotation) {
				return compareAnnotations((Annotation)at1, (Annotation) at2);
			} else {
				return -1;
			}
		} else if (at1 instanceof Ontology_Component) {
			if (at2 instanceof Ontology_Component) {
				return compareOntologyComponent((Ontology_Component)at1, (Ontology_Component)at2);
			} else {
				return (at2 instanceof Annotation) ? 1 : -1;
			}
		} else if (at1 instanceof Change) {
			if (at2 instanceof Change) {
				return compareChanges((Change)at1, (Change)at2);
			} else {
				return 1;
			}
		}
		
		if (at1 instanceof AbstractWrappedInstance && 
				at2 instanceof AbstractWrappedInstance) {
			return ((AbstractWrappedInstance)at1).getWrappedProtegeInstance().compareTo
					(((AbstractWrappedInstance)at2).getWrappedProtegeInstance());
		}
		
		return 0;
	}


	private int compareAnnotations(Annotation a1, Annotation a2) {		
		return compareTimestamp(a1.getCreated(), a2.getCreated());
	}
	
	private int compareChanges(Change ch1, Change ch2) {
		return compareTimestamp(ch1.getTimestamp(), ch2.getTimestamp());
	}
	
	private int compareTimestamp(Timestamp t1, Timestamp t2) {
		if (t1 == null) {
			return (t2 == null) ? 0 : 1; 
		} else {
			if (t2 == null) {return -1;}
			return t1.compareTimestamp(t2);
		}		
	}

	private int compareOntologyComponent(Ontology_Component oc1, Ontology_Component oc2) {
		if (oc1.canAs(Ontology_Class.class)) {
			if (oc2.canAs(Ontology_Class.class)) {
				return compareSameOntoCompTypes(oc1, oc2);
			} else {
				return -1;
			}
		} else if (oc1.canAs(Ontology_Property.class)) {
			if (oc2.canAs(Ontology_Property.class)) {
				return compareSameOntoCompTypes(oc1, oc2);
			} else {
				return (oc2.canAs(Ontology_Class.class)) ? 1 : -1; 
			}
		} else if (oc1.canAs(Ontology_Individual.class)) {
			if (oc2.canAs(Ontology_Individual.class)) {
				return compareSameOntoCompTypes(oc1, oc2);
			} else {
				return 1;
			}
		}
		
		if (oc1 instanceof AbstractWrappedInstance && 
				oc2 instanceof AbstractWrappedInstance) {
			return ((AbstractWrappedInstance)oc1).getWrappedProtegeInstance().compareTo
					(((AbstractWrappedInstance)oc2).getWrappedProtegeInstance());
		}
		
		return 0;
	}

	private int compareSameOntoCompTypes(Ontology_Component oc1, Ontology_Component oc2) {
		String name1 = oc1.getCurrentName();
		String name2 = oc2.getCurrentName();
		
		if (name1 != null) {
			return (name2 == null) ? -1 : name1.compareTo(name2);
		} else {
			return (name2 == null) ? 0 : 1;
		}
		//we could also compare by initial name	
	}

}
