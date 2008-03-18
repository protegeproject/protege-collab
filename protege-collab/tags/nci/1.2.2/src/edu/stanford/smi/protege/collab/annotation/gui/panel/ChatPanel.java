package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;

import javax.swing.Icon;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protegex.chatPlugin.ChatComponent;
import edu.stanford.smi.protegex.chatPlugin.ChatIcons;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChatPanel extends AnnotationsTabPanel {
	private ChatComponent chatComponent;

	public ChatPanel(KnowledgeBase kb) {
		super(kb, "Chat");
		
		fixGUI();
	}

	protected void fixGUI() {
		LabeledComponent labledComponent = getLabeledComponent();
		labledComponent.setFooterComponent(null);
			
		SelectableContainer parent = (SelectableContainer) labledComponent.getParent();
		
		parent.remove(labledComponent);
		parent.remove(getToolbar());

		chatComponent = new ChatComponent(getKnowledgeBase());		
		
		parent.add(chatComponent, BorderLayout.CENTER);		
	}

	@Override
	public void setInstance(Instance instance) {
		chatComponent.changeTabTitleDisplay(false);
		super.setInstance(instance);
	}
	

	@Override
	protected void onCreateAnnotation() {
		//	TODO Auto-generated method stub
	}

	@Override
	public void refreshDisplay() {		
		// TODO Auto-generated method stub		
	}
	
	@Override
	public Icon getIcon() {	
		return ChatIcons.getSmileyIcon();
	}

	@Override
	public void dispose() {
		chatComponent.dispose();
		super.dispose();
	}
	
}
