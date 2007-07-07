package edu.stanford.smi.protege.collab.changes;

import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ClassChangeListener implements FrameListener {

	public void browserTextChanged(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void deleted(FrameEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void nameChanged(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownFacetAdded(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownFacetRemoved(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownFacetValueChanged(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownSlotAdded(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownSlotRemoved(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void ownSlotValueChanged(FrameEvent event) {
		refreshClassDisplay(event);
		
	}

	public void visibilityChanged(FrameEvent event) {
		refreshClassDisplay(event);
		
	}


	public void refreshClassDisplay(FrameEvent event) {
		//implement in subclasses
	}


}
