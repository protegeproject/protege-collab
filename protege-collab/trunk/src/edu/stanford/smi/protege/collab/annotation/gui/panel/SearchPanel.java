package edu.stanford.smi.protege.collab.annotation.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.SelectableContainer;

public class SearchPanel extends AnnotationsTabPanel {

	public SearchPanel(KnowledgeBase kb) {
		super(kb, "Search Panel");
		
		fixGUI();
	}

	protected void fixGUI() {
		LabeledComponent labledComponent = getLabeledComponent();
		labledComponent.setFooterComponent(null);
				
		Collection headerButtonCollection = labledComponent.getHeaderButtons(); 
		for (int i=0; i < headerButtonCollection.size(); i++) {			
			labledComponent.removeHeaderButton(i);
		}
		
		labledComponent.setHeaderComponent(null);
		labledComponent.setHeaderLabel("Search Results");
		
		SelectableContainer parent = (SelectableContainer) labledComponent.getParent();
		
		parent.remove(labledComponent);
		parent.remove(getToolbar());
		
		LabeledComponent searchPanel = new LabeledComponent("Search", null, true);
		searchPanel.setBackground(Color.GREEN);
		
		JSplitPane topBottomSplitPane = ComponentFactory.createTopBottomSplitPane(searchPanel, labledComponent);		
		labledComponent.setPreferredSize(new Dimension(100, 200));
		topBottomSplitPane.setDividerLocation(150 + topBottomSplitPane.getInsets().bottom);
		
		parent.add(topBottomSplitPane, BorderLayout.CENTER);
		
	}

	@Override
	protected void onCreateAnnotation() {
		//	TODO Auto-generated method stub
	}

	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub

	}

}
