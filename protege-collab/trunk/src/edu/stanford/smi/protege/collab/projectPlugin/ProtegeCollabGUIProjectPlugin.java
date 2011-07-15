package edu.stanford.smi.protege.collab.projectPlugin;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.smi.protege.action.DisplayHtml;
import edu.stanford.smi.protege.collab.annotation.gui.AnnotationsDisplayComponent;
import edu.stanford.smi.protege.collab.annotation.gui.ConfigureCollabProtegeAction;
import edu.stanford.smi.protege.collab.util.ChAOCacheUpdater;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.collab.util.UIUtil;
import edu.stanford.smi.protege.event.ProjectAdapter;
import edu.stanford.smi.protege.event.ProjectListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.plugin.ProjectPluginAdapter;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.ui.ProjectToolBar;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.util.ProjectViewEvent;
import edu.stanford.smi.protege.util.ProjectViewListener;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protegex.changes.ui.CreateChAOProjectDialog;

public class ProtegeCollabGUIProjectPlugin extends ProjectPluginAdapter {

    public static String SHOW_COLLAB_PANEL_PRJ_INFO = "show_collaboration_panel";
    public static String USER_GUIDE_HTML = "http://protegewiki.stanford.edu/index.php/Collaborative_Protege";

    private AnnotationsDisplayComponent annotationsDisplayComponent;
    private JMenu collabMenu;
    private Action enableCollabPanelAction;
    private JCheckBoxMenuItem enableCollabPanelCheckBox;

    //updates the annotation count cache
    private ChAOCacheUpdater chaoCacheUpdater;
    //enables annotation renderers, if tabs are added/closed
    private ProjectViewListener projectViewListener;
    //clean up, if chao closes
    private ProjectListener chaoPrjListener;

    @Override
    public void afterShow(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        KnowledgeBase kb = view.getProject().getKnowledgeBase();

        insertCollabMenu(kb, menuBar);
        if (!UIUtil.isCollaborationPanelEnabled(kb.getProject())) {
            return;
        }
        chaoCacheUpdater = new ChAOCacheUpdater(kb);
        enableCollaborationPanel(kb);
    }

    private boolean ensureChAOKBLoaded(KnowledgeBase kb) {
        KnowledgeBase chaoKB = ChAOKbManager.getChAOKb(kb);
        boolean validChaoKB = isValidChaoKB(chaoKB);
        if (chaoKB != null && validChaoKB) {
            return true;
        }

        if (kb.getProject().isMultiUserClient()) {
            if (chaoKB == null) {
                ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(),
                        "The Collaboration panel could not find the changes/annotation knowledge base (ChAO KB) \n"
                                + "associated to this project. One possible reason is that the\n"
                                + "changes/annotations knowledge base was not configured on the server.\n"
                                + "Please check the configuration of the project on the server side.\n"
                                + "The Collaboration panel will not work at the current time.",
                        "No annotation/changes knowledge base", ModalDialog.MODE_CLOSE);
                return false;
            }

            if (!validChaoKB) {
                ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(),
                        "The changes/annotations knowledge base is not configured correctly on the server.\n"
                                + "Please check the configuration of the project on the server side.\n"
                                + "The Collaboration panel will not work at the current time.",
                        "Misconfigured annotation/changes knowledge base", ModalDialog.MODE_CLOSE);
            }
            return false;
        }

        CreateChAOProjectDialog dialog = new CreateChAOProjectDialog(kb);
        dialog.showDialog();
        chaoKB = dialog.getChangesKb();

        if (chaoKB == null || !isValidChaoKB(chaoKB)) {
            ModalDialog.showMessageDialog(ProjectManager.getProjectManager().getCurrentProjectView(),
                    "Could not find or create the changes and annotations\n"
                            + "ontology (ChAO), or attached ChAO is invalid.\n"
                            + "The collaboration panel will not work in this session.", "No ChAO");
            return false;
        }

        chaoKB = ChAOKbManager.getChAOKb(kb);
        return chaoKB != null;
    }

    private boolean isValidChaoKB(KnowledgeBase chaoKB) {
        if (chaoKB == null) {
            return false;
        }
        AnnotationFactory factory = new AnnotationFactory(chaoKB);
        return factory.getAnnotatableThingClass() != null;
    }

    private void backwardCompatibilityFix(KnowledgeBase kb) {
        //add subject if not added as a template slot of annotation
        KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(kb);
        if (chaoKb == null) {
            return;
        }
        try {
            AnnotationFactory factory = new AnnotationFactory(chaoKb);
            Cls annotationCls = factory.getAnnotationClass();
            Slot subjectSlot = factory.getSubjectSlot();
            if (!annotationCls.hasTemplateSlot(subjectSlot)) {
                annotationCls.addDirectTemplateSlot(subjectSlot);
                Log.getLogger().info("Backwards compatibility fix for the Changes and Annotation ontology (done)");
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Failed to make the backwards compatibility fix for the ChAO Kb", e);
        }
    }

    private void enableCollaborationPanel(KnowledgeBase kb) {
        Log.getLogger().info("Started Collaborative Protege on " + new Date());

        boolean success = ensureChAOKBLoaded(kb);
        if (!success) {
            enableCollabPanelCheckBox.setSelected(false);
            return;
        }

        backwardCompatibilityFix(kb);

        ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
        if (view != null) {
            annotationsDisplayComponent = insertCollabPanel(view);
            if (annotationsDisplayComponent != null) {
                attachProjectViewListener(view);
                UIUtil.adjustTreeFrameRenderers(view);
                UIUtil.adjustAnnotationBrowserPattern(kb);
            }
        }

        UIUtil.setCollaborationPanelEnabled(kb.getProject(), true);

        HasAnnotationCache.fillHasAnnotationCache(kb);
        chaoCacheUpdater.initialize();
        attachChaoProjectListener(kb);

    }

    private void disposeCollaborationPanel() {
        if (annotationsDisplayComponent == null) {
            return;
        }

        ProjectView view = ProjectManager.getProjectManager().getCurrentProjectView();
        KnowledgeBase kb = view.getProject().getKnowledgeBase();

        //remove Chao Prj listener
        try {
            KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(kb);
            if (chaoPrjListener != null && chaoKb != null) {
                chaoKb.getProject().removeProjectListener(chaoPrjListener);
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at removing ChAO Kb listener in Collaborative Protege", e);
        }

        try {
            //detach project view listener if present
            if (view != null && projectViewListener != null) {
                view.removeProjectViewListener(projectViewListener);
            }
            HasAnnotationCache.clearCache();

            JComponent splitPane = (JComponent) annotationsDisplayComponent.getParent();
            JComponent parent = null;
            if (splitPane != null) {
                parent = (JComponent) splitPane.getParent();
                if (parent != null) {
                    parent.remove(splitPane);
                    parent.add(view, BorderLayout.CENTER);
                    parent.revalidate();
                }
            }

            //remove the annotations components
            if (annotationsDisplayComponent != null) {
                ComponentUtilities.dispose(annotationsDisplayComponent);
                annotationsDisplayComponent = null;
            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at disposing the collaboration panel", e);
        }
    }

    private void disableCollaborationPanel(KnowledgeBase kb, boolean reloadUI) {
        Log.getLogger().info("Stopped Collaborative Protege on " + new Date());
        JComponent mainPanel = ProjectManager.getProjectManager().getMainPanel();
        mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        disposeCollaborationPanel();
        if (reloadUI) {
            ProjectManager.getProjectManager().reloadUI(true);
        }

        mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        UIUtil.setCollaborationPanelEnabled(kb.getProject(), false);
        enableCollabPanelCheckBox.setSelected(false);
    }

    private void attachProjectViewListener(ProjectView view) {
        projectViewListener = new ProjectViewListener() {
            public void closed(ProjectViewEvent event) {
            }

            public void saved(ProjectViewEvent event) {
            }

            public void tabAdded(ProjectViewEvent event) {
                UIUtil.adjustTreeFrameRenderer((TabWidget) event.getWidget());
                annotationsDisplayComponent.init();
            }
        };
        view.addProjectViewListener(projectViewListener);
    }

    protected void attachChaoProjectListener(KnowledgeBase kb) {
        chaoPrjListener = getChaoPrjListener();
        KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(kb);
        if (chaoKb != null) {
            chaoKb.getProject().addProjectListener(getChaoPrjListener());
        }
    }

    protected ProjectListener getChaoPrjListener() {
        if (chaoPrjListener == null) {
            chaoPrjListener = new ProjectAdapter() {
                @Override
                public void projectClosed(edu.stanford.smi.protege.event.ProjectEvent event) {
                    disableCollaborationPanel(ProjectManager.getProjectManager().getCurrentProject().getKnowledgeBase(), true);
                };
            };
        }
        return chaoPrjListener;
    }

    private void insertCollabMenu(KnowledgeBase kb, ProjectMenuBar menuBar) {
        collabMenu = new JMenu("Collaboration");
        enableCollabPanelCheckBox = new JCheckBoxMenuItem(getEnableCollabPanelAction(kb));
        enableCollabPanelCheckBox.setSelected(UIUtil.isCollaborationPanelEnabled(kb.getProject()));
        collabMenu.add(enableCollabPanelCheckBox);
        ComponentFactory.addMenuItemNoIcon(collabMenu, new DisplayHtml("Collaboration User's Guide", USER_GUIDE_HTML));
        collabMenu.addSeparator();
        collabMenu.add(new JMenuItem(new ConfigureCollabProtegeAction(kb, this))); //kind of funky
        menuBar.add(collabMenu);
    }

    private Action getEnableCollabPanelAction(final KnowledgeBase kb) {
        if (enableCollabPanelAction == null) {
            enableCollabPanelAction = new AbstractAction("Show Collaboration Panel") {
                private static final long serialVersionUID = -5089945315129263729L;

                public void actionPerformed(ActionEvent arg0) {
                    boolean toEnable = enableCollabPanelCheckBox.isSelected();
                    if (toEnable) {
                        enableCollaborationPanel(kb);
                    } else {
                        disableCollaborationPanel(kb, true);
                    }
                }
            };
        }
        return enableCollabPanelAction;
    }

    private AnnotationsDisplayComponent insertCollabPanel(ProjectView view) {
        try {
            annotationsDisplayComponent = new AnnotationsDisplayComponent(view.getProject().getKnowledgeBase());
        } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Could not insert the collaboration panel", t);
        }

        if (annotationsDisplayComponent == null) {
            return null;
        }

        JComponent parent = (JComponent) view.getParent();
        parent.remove(view);

        final JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setResizeWeight(0.50);
        pane.setDividerLocation(0.75);
        pane.setOneTouchExpandable(true);
        pane.setLeftComponent(view);
        pane.setRightComponent(annotationsDisplayComponent);
        annotationsDisplayComponent.setMinimumSize(new Dimension(0, 0));

        parent.add(pane, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();

        return annotationsDisplayComponent;
    }

    @Override
    public void beforeHide(ProjectView view, ProjectToolBar toolBar, ProjectMenuBar menuBar) {
        JMenuBar mainMenuBar = ProjectManager.getProjectManager().getCurrentProjectMenuBar();
        enableCollabPanelAction = null;
        enableCollabPanelCheckBox = null;
        mainMenuBar.remove(collabMenu);
        collabMenu = null;
        disposeCollaborationPanel();
    }

    @Override
    public void beforeClose(Project p) {
        chaoCacheUpdater.dispose();
        //should be safe here, because this is in the Protege Client UI, and there is only one project loaded at a time
        HasAnnotationCache.clearCache();
    }

    public AnnotationsDisplayComponent getAnnotationsDisplayComponent() {
        return annotationsDisplayComponent;
    }

}
