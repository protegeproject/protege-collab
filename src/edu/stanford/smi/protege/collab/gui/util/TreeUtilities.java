package edu.stanford.smi.protege.collab.gui.util;


/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class TreeUtilities {

	//TODO: Not working!! TBD 
	
	/*
    public static void select(Object o, JTree tree) {
        WaitCursor cursor = new WaitCursor(ProjectManager.getProjectManager().getCurrentProjectView());
        
        Frame frame = (Frame) o;
        ArrayList frames = new ArrayList();
       // getVisiblePathToRoot(frame, frames);
        Collections.reverse(frames);
        ComponentUtilities.setSelectedObjectPath(tree, frames);
        cursor.hide();
    }
    
    
    protected void getVisiblePathToRoot(Frame frame, Collection path) {
        Collection roots = new ArrayList((Collection) ((LazyTreeNode) tree.getModel().getRoot()).getUserObject());
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            Frame root = (Frame) i.next();
            if (!root.isVisible()) {
                i.remove();
            }
        }
        path.add(frame);
        if (!roots.contains(frame)) {
            boolean succeeded = getVisiblePathToRoot(frame, roots, path);
            if (!succeeded) {
                Log.getLogger().warning("No visible path found for " + frame);
            }
        }
    }
    
    protected boolean getVisiblePathToRoot(Frame frame, Collection roots, Collection path) {
        boolean found = false;
        Iterator i = getParents(frame).iterator();
        while (i.hasNext() && !found) {
            Frame parent = (Frame) i.next();
            if (parent.isVisible() && !path.contains(parent)) {
                path.add(parent);
                if (roots.contains(parent)) {
                    found = true;
                } else {
                    found = getVisiblePathToRoot(parent, roots, path);
                }
                if (!found) {
                    path.remove(parent);
                }
            }
        }
        return found;
    }

*/
	
}
