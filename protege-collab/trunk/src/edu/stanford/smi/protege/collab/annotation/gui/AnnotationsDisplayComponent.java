package edu.stanford.smi.protege.collab.annotation.gui;

import java.awt.Dimension;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotatableThing;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.code.generator.wrapping.AbstractWrappedInstance;
import edu.stanford.smi.protege.collab.annotation.gui.panel.AbstractAnnotationsTabPanel;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.changes.ChangesKbFrameListener;
import edu.stanford.smi.protege.collab.changes.ClassChangeListener;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.OntologyAnnotationsCache;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableContainer;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;
import edu.stanford.smi.protege.widget.TabWidget;

/**
 * Main class for displaying the collaboration component. It contains the logic
 * for refreshing the tabs when the Protege Tab is switched, listeners for
 * changes in the ChAO Kb, tree selection listeners for changes in the classes
 * tree.
 * 
 * @author Tania Tudorache <tudorache@stanford.edu>
 * 
 */
public class AnnotationsDisplayComponent extends SelectableContainer {

    private static final long serialVersionUID = -4241048417267452940L;

    private KnowledgeBase domainKb;

    private Instance currentInstance;
    private Selectable selectable;

    private JComponent annotationBodyTextComponent;
    private AnnotationsTabHolder annotationsTabHolder;

    private ClassChangeListener classChangeListener;
    private SelectionListener clsTreeSelectionListener;
    private ChangesKbFrameListener changesKbFrameListener;
    private ChangeListener protegeTabChangeListener;
    private ChangeListener collabTabChangeListener;
    private SelectionListener annotTreeSelectionListener;
    
    private OntologyAnnotationsCache ontologyAnnotationsCache;

    public AnnotationsDisplayComponent(KnowledgeBase kb) {
        this.domainKb = kb;
        
        ontologyAnnotationsCache = new OntologyAnnotationsCache(ChAOKbManager.getChAOKb(kb));

        annotationsTabHolder = createAnnotationsTabHolder();
        annotationBodyTextComponent = createAnnotationBodyComponent();

        LabeledComponent labeledComponentTabHolder = new LabeledComponent("Collaboration", annotationsTabHolder, true);
        LabeledComponent labeledComponentText = new LabeledComponent("Details", annotationBodyTextComponent, true);

        final JSplitPane topBottomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        topBottomSplitPane.setTopComponent(labeledComponentTabHolder);
        topBottomSplitPane.setBottomComponent(labeledComponentText);
        labeledComponentTabHolder.setMinimumSize(new Dimension(0, 0));
        labeledComponentText.setMinimumSize(new Dimension(0, 0));
        topBottomSplitPane.setDividerLocation(0.75);
        topBottomSplitPane.setResizeWeight(0.50);
        topBottomSplitPane.setOneTouchExpandable(true);

        classChangeListener = getClassChangeListener();
        setSelectable(annotationsTabHolder);
        attachAnnotationTreeSelectionListener();
        annotationsTabHolder.getTabbedPane().addChangeListener(getCollabTabChangeListener());
        attachProtegeTabTreeListener();
        attachChangeKbListeners();

        add(topBottomSplitPane);
        
        HasAnnotationCache.fillHasAnnotationCache(domainKb);       
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refreshDisplay();
            }
        });
    }

    /*
     * Listeners
     */

    protected void attachChangeKbListeners() {
        KnowledgeBase changesKb = ChAOUtil.getChangesKb(domainKb);

        changesKbFrameListener = new ChangesKbFrameListener(domainKb);
        changesKb.addFrameListener(changesKbFrameListener);
    }

    protected void attachAnnotationTreeSelectionListener() {
        for (AbstractAnnotationsTabPanel annotationPanel : annotationsTabHolder.getTabs()) {
            annotationPanel.addSelectionListener(getAnnotationTreeSelectionListener());
        }
    }

    protected SelectionListener getAnnotationTreeSelectionListener() {
    	if (annotTreeSelectionListener == null) {
    		annotTreeSelectionListener = new SelectionListener() {
    			public void selectionChanged(SelectionEvent event) {
    				SelectableTree annotationsTree = annotationsTabHolder.getSelectedTab().getAnnotationsTree();
    				if (annotationsTree == null) {
    					((InstanceDisplay) annotationBodyTextComponent).setInstance(null);
    				} else {
    					AnnotatableThing thing = (AnnotatableThing) CollectionUtilities.getFirstItem(annotationsTree.getSelection());
    					if (thing == null) {
    						((InstanceDisplay) annotationBodyTextComponent).setInstance(null);
    					} else {
    						Instance annotInstance = ((AbstractWrappedInstance) thing).getWrappedProtegeInstance();
    						((InstanceDisplay) annotationBodyTextComponent).setInstance(annotInstance);
    					}
    				}
    			}
    		};
    	}
    	return annotTreeSelectionListener;
    }

    protected ChangeListener getProtegeTabChangeListener() {
        if (protegeTabChangeListener != null) {
            return protegeTabChangeListener;
        }
        protegeTabChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                //System.out.println("Tab changed " + ((JTabbedPane)e.getSource()).getSelectedComponent()  + "\nselectable before = " + selectable);
                clsTreeSelectionListener = getClsTreeSelectionListener();

                if (selectable != null) {
                    selectable.removeSelectionListener(clsTreeSelectionListener);
                }

                TabWidget tabWidget = (TabWidget) ((JTabbedPane) e.getSource()).getSelectedComponent();
                selectable = UIUtil.getSelectableForTab(tabWidget);

                //do I need this here?
                setInstances(selectable == null ? null : selectable.getSelection());

                if (selectable != null) {
                    selectable.addSelectionListener(clsTreeSelectionListener);
                }
            }
        };
        return protegeTabChangeListener;
    }

    protected ChangeListener getCollabTabChangeListener() {
        if (collabTabChangeListener == null) {
            collabTabChangeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent arg0) {
                    AbstractAnnotationsTabPanel annotTabPanel = annotationsTabHolder.getSelectedTab();
                    //why??
                    if (annotTabPanel == null) {
                        return;
                    }
                    annotTabPanel.setInstance(currentInstance);
                    setSelectable(annotTabPanel.getSelectable());
                    AnnotatableThing annot = (AnnotatableThing) CollectionUtilities.getFirstItem(annotTabPanel
                            .getSelection());
                    Instance wrappedInst = new AnnotationFactory(ChAOUtil.getChangesKb(domainKb))
                            .getWrappedProtegeInstance(annot);
                    ((InstanceDisplay) annotationBodyTextComponent).setInstance(wrappedInst);
                }
            };
        }
        return collabTabChangeListener;
    }

    protected SelectionListener getClsTreeSelectionListener() {
        if (clsTreeSelectionListener == null) {
            clsTreeSelectionListener = new SelectionListener() {
                public void selectionChanged(SelectionEvent event) {
                    setInstances(event.getSelectable().getSelection());
                }
            };
        }
        return clsTreeSelectionListener;
    }

    protected ClassChangeListener getClassChangeListener() {
        if (classChangeListener == null) {
            classChangeListener = new ClassChangeListener() {
                @Override
                public void refreshClassDisplay(FrameEvent event) {
                    refreshAllTabs();
                }
            };
        }
        return classChangeListener;
    }

    private void attachProtegeTabTreeListener() {
        ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
        TabWidget currentTab = view.getSelectedTab();

        selectable = UIUtil.getSelectableForTab(currentTab);
        if (selectable != null) {
            clsTreeSelectionListener = getClsTreeSelectionListener();
            selectable.addSelectionListener(clsTreeSelectionListener);
            setInstances(selectable.getSelection());
        }
        init();
    }

    /*
     * GUI components
     */

    protected AnnotationsTabHolder createAnnotationsTabHolder() {
        annotationsTabHolder = new AnnotationsTabHolder(domainKb);
        annotationsTabHolder.setOntologyAnnotationsCache(ontologyAnnotationsCache);
        return annotationsTabHolder;
    }

    protected JComponent createAnnotationBodyComponent() {
        if (!ChAOUtil.isChangesOntologyPresent(domainKb)) {
            Log.getLogger().warning("Change ontology is not present. Cannot display annotations for it.");
            annotationBodyTextComponent = new InstanceDisplay(ProjectManager.getProjectManager().getCurrentProject());
        } else {
            annotationBodyTextComponent = new InstanceDisplay(ChAOUtil.getChangesKb(domainKb).getProject(), false,
                    false);
        }
        return annotationBodyTextComponent;
    }

    public void setInstance(Instance instance) {
        if (currentInstance != null) {
            currentInstance.removeFrameListener(classChangeListener);
        }
        currentInstance = instance;
        if (currentInstance != null) {
            currentInstance.addFrameListener(classChangeListener);
        }
        annotationsTabHolder.setInstance(currentInstance);
    }

    public void setInstances(Collection instances) {
        //TODO: handle multiple selections
        setInstance(instances == null ? null : (Instance) CollectionUtilities.getFirstItem(instances));
    }

    public Instance getInstance() {
        return currentInstance;
    }

    //TODO: find a better solution for the initialization
    public void init() {
        ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
        protegeTabChangeListener = getProtegeTabChangeListener();
        view.removeChangeListener(protegeTabChangeListener);
        view.addChangeListener(protegeTabChangeListener);
    }

    public void refreshDisplay() {
        annotationsTabHolder.refreshDisplay();
        Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelection());
        ((InstanceDisplay) annotationBodyTextComponent).setInstance(annotInstance);
    }

    protected void refreshAllTabs() {
        annotationsTabHolder.refreshAllTabs();
        Instance annotInstance = (Instance) CollectionUtilities.getFirstItem(annotationsTabHolder.getSelection());
        ((InstanceDisplay) annotationBodyTextComponent).setInstance(annotInstance);
    }

    public void reloadCollabTabs() {
        annotationsTabHolder.reload();
        attachAnnotationTreeSelectionListener();
    }

    public KnowledgeBase getKnowledgeBase() {
        return domainKb;
    }

    /*
     * ********** Dispose ***********
     */

    @Override
    public void dispose() {
        /*
         * Class tree selection listener
         */
        if (selectable != null) {
            try {
                selectable.removeSelectionListener(clsTreeSelectionListener);
            } catch (Throwable t) {
                Log.getLogger().log(Level.WARNING,
                        "Could not detach class tree selection listener when disposing the collaboration panel.", t);
            }
        }

        /*
         * Frame listener on currently selected instance
         */
        try {
            if (currentInstance != null && classChangeListener != null) {
                currentInstance.removeFrameListener(classChangeListener);
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at removing frame listener on " + currentInstance, e);
        }

        /*
         * Annotation tree selection listener
         */
        if (annotTreeSelectionListener != null) {
            for (AbstractAnnotationsTabPanel annotationPanel : annotationsTabHolder.getTabs()) {
                try {
                    annotationPanel.removeSelectionListener(annotTreeSelectionListener);
                } catch (Exception e) {
                    Log.getLogger().log(Level.WARNING,
                            "Error at removing annotation tree listener on " + annotationPanel, e);
                }
            }
        }

        /*
         * Protege Tab change listener
         */
        try {
            ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
            if (view != null && protegeTabChangeListener != null) {
                view.removeChangeListener(protegeTabChangeListener);
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at removing Protege tab listener", e);
        }

        /*
         * Collaborative Tab change listeners
         */
        try {
            annotationsTabHolder.getTabbedPane().removeChangeListener(collabTabChangeListener);
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at removing collab tab change listener", e);
        }

        /*
         * Annotations Tab holder
         */
        try {
            annotationsTabHolder.dispose();
        } catch (Exception e) {
            Log.getLogger().warning("Error at disposing the annotations tab holder");
        }

        /*
         * ChAO Kb related listeners
         */
        KnowledgeBase changesKb = ChAOUtil.getChangesKb(domainKb);
        if (changesKb == null) {
            Log.getLogger().warning("Cannot dispose properly the annotations component because changes kb is null");
        } else {
            try {
                changesKb.removeFrameListener(changesKbFrameListener);
            } catch (Exception e) {
                Log.getLogger().warning("Error at disposing changes ontology kb listener");
            }
            //clear caches        
            HasAnnotationCache.clearCache();
        }
        
        ontologyAnnotationsCache.dispose();

        super.dispose();
    }

}
