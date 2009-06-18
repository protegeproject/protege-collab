package edu.stanford.smi.protege.collab.annotation.gui.renderer;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;

import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsIcons;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.FrameWithBrowserText;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class FramesWithAnnotationsRenderer extends FrameRenderer {	
	
	private FrameRenderer encapsulatedFrameRenderer;

	public FramesWithAnnotationsRenderer(FrameRenderer frameRenderer) {
		this.encapsulatedFrameRenderer = frameRenderer;
	}
	
	@Override
	public void load(Object value) {
		//super.load(value);
		encapsulatedFrameRenderer.load(value);
		
		String mainText = encapsulatedFrameRenderer.getMainText();
		Icon mainIcon = encapsulatedFrameRenderer.getMainIcon();
		
		if (value instanceof FrameWithBrowserText) {
			FrameWithBrowserText fbt = (FrameWithBrowserText) value;
			Frame frameValue = fbt.getFrame();
			setMainText(fbt.getBrowserText());
			if (frameValue != null) {
				setMainIcon(frameValue.getIcon());
			}
			value = frameValue;
		} else {
			setMainText(mainText);
			setMainIcon(mainIcon);
		}	
		
		
		if (value == null || !(value instanceof Frame)) {
			return;
		}
		
		Frame frame = (Frame) value;
		
		if (!ChAOUtil.hasAnnotations(frame)) {
			return;
		}
		
		appendText(" ");
		appendIcon(AnnotationsIcons.getCommentIcon());		
	}
	
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		//TT: This code should be checked later.
		encapsulatedFrameRenderer.clear();		
		return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int row, boolean selected, boolean hasFocus) {
		encapsulatedFrameRenderer.clear();
		return super.getListCellRendererComponent(list, value, row, selected, hasFocus);		
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int col) {
		encapsulatedFrameRenderer.clear();
		return super.getTableCellRendererComponent(table, value, selected, hasFocus, row, col);		
	}
	
		
}
	

