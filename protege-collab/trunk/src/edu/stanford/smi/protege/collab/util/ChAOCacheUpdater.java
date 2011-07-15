package edu.stanford.smi.protege.collab.util;

import java.util.logging.Level;

import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.changes.ChangesKbFrameListener;
import edu.stanford.smi.protege.event.ClsAdapter;
import edu.stanford.smi.protege.event.ClsEvent;
import edu.stanford.smi.protege.event.ClsListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Disposable;
import edu.stanford.smi.protege.util.Log;


/**
 *  Updates the {@link HasAnnotationCache} by using listeners on the domain KB and on ChAO.
 *  It is used by the Collaborative Protege UI, and can be used also
 *  by other applications that make use of the {@link HasAnnotationCache}.
 *
 * @author ttania
 *
 */
public class ChAOCacheUpdater implements Disposable {

    private KnowledgeBase kb;

    // updates the HasAnnotationCache if hierarchical changes occur (child notes count)
    private ClsListener domainKbClsListener;
    // updates the HasAnnotationCache and the OntologyComponentCache if frames are created in ChAO
    private ChangesKbFrameListener chaoKbFrameListener;

    public ChAOCacheUpdater(KnowledgeBase kb) {
        this.kb = kb;
    }

    public void initialize() {
        attachClsListener(kb);
        attachChAOKbListener();
    }

    protected void attachClsListener(KnowledgeBase kb) {
        kb.addClsListener(getClsListener());
    }

    protected void attachChAOKbListener() {
        KnowledgeBase chaoKb = ChAOUtil.getChangesKb(kb);
        chaoKbFrameListener = new ChangesKbFrameListener(kb);
        chaoKb.addFrameListener(chaoKbFrameListener);
    }

    protected ClsListener getClsListener() {
        if (domainKbClsListener == null) {
            domainKbClsListener = new ClsAdapter() {
                @Override
                public void directSuperclassAdded(ClsEvent event) {
                    if (!event.isReplacementEvent()) {
                        HasAnnotationCache.onDirectSuperClassAdded(event.getCls(), event.getSuperclass());
                    }
                }

                @Override
                public void directSuperclassRemoved(ClsEvent event) {
                    if (!event.isReplacementEvent()) {
                        HasAnnotationCache.onDirectSuperClassRemoved(event.getCls(), event.getSuperclass());
                    }
                }

                @Override
                public void directSubclassRemoved(ClsEvent event) {
                    if (!event.isReplacementEvent()) {
                        Cls subcls = event.getSubclass();
                        if (subcls.isBeingDeleted()) {
                            HasAnnotationCache.onClsDeleted(subcls, event.getCls());
                        }
                    }
                }
            };
        }
        return domainKbClsListener;
    }

    public void dispose() {
        if (domainKbClsListener != null) {
            try {
                kb.removeClsListener(domainKbClsListener);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at detaching class listerner for the domain kb used by the HasAnnotationCacheUpdater.", e);
            }
        }

        KnowledgeBase changesKb = ChAOUtil.getChangesKb(kb);

        if (chaoKbFrameListener != null && changesKb != null) {
            try {
                changesKb.removeFrameListener(chaoKbFrameListener);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at detaching ChAO frame listerner used by the HasAnnotationCacheUpdater.", e);
            }
        }
    }

}
