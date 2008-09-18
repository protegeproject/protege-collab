package edu.stanford.smi.protege.collab.annotation.tree;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.ui.FrameTreeFinder;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LazyTreeRoot;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsTreeFinder extends FrameTreeFinder {
	private JTree tree;
	private Slot ancestorSlot;

	public AnnotationsTreeFinder(KnowledgeBase kb, JTree tree, Slot ancestorSlot) {
		super(kb, tree, "Find annotations in tree");

		this.tree = tree;
		this.ancestorSlot = ancestorSlot;
	}

	@Override
	protected Collection getAncestors(Frame frame) {
		return getKnowledgeBase().getDirectOwnSlotValuesClosure(frame, ancestorSlot);
	}

	@Override
	protected Slot getBrowserSlot(KnowledgeBase kb) {
		return null;
	}

	@Override
	protected Collection getParents(Frame frame) {
		return frame.getOwnSlotValues(ancestorSlot);
	}

	@Override
	protected boolean isCorrectType(Frame frame) {
		return true;
	}


	   @Override
	protected List getMatches(String text, int maxMatches) {
	        Cls kbRoot = getKnowledgeBase().getRootCls();
	        Set matches = getMatchingFrames(text, maxMatches);
	        LazyTreeRoot root = (LazyTreeRoot) tree.getModel().getRoot();
	        Set rootNodes = new HashSet((Collection) root.getUserObject());
	        if (rootNodes.size() != 1 || !equals(CollectionUtilities.getFirstItem(rootNodes), kbRoot)) {
	            // Log.trace("removing bad matches", this, "getMatches");
	            Iterator i = matches.iterator();
	            while (i.hasNext()) {
	                Frame frame = (Frame) i.next();
	                boolean isValid = rootNodes.contains(frame);
	                if (!isValid) {
	                    Collection parents = new HashSet(getAncestors(frame));
	                    isValid = parents.removeAll(rootNodes);
	                }
	                if (!isValid) {
	                    i.remove();
	                }
	            }
	        }
	        List sortedMatches = new ArrayList(matches);
	        Collections.sort(sortedMatches, new FrameComparator());
	        return sortedMatches;
	    }


}