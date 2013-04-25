package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;

import javax.swing.Icon;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protegex.chatPlugin.ChatComponent;
import edu.stanford.smi.protegex.chatPlugin.ChatIcons;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ChatPanel extends AbstractAnnotationsTabPanel {	
	private static final long serialVersionUID = -8861252243145443447L;
	
	private ChatComponent chatComponent;

	public ChatPanel(KnowledgeBase kb) {
		super(kb, "Chat");
		setLabel("Chat with other Protege clients");
		fixGUI();
	}

	protected void fixGUI() {
		LabeledComponent outerLC = (LabeledComponent) this.getComponent(0);
		this.remove(outerLC);	
		chatComponent = new ChatComponent(getKnowledgeBase());		
		add(chatComponent, BorderLayout.CENTER);		
	}

	@Override
	public void setInstance(Instance instance) {
		chatComponent.changeTabTitleDisplay(false);
		super.setInstance(instance);
	}
	

	@Override
	protected void onCreateAnnotation() {}

	@Override
	public void refreshDisplay() {}
	
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
