package edu.stanford.smi.protege.collab.annotation.gui;

import java.util.HashMap;

import javax.swing.Icon;

import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.util.ComponentUtilities;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class AnnotationsIcons {
	private static HashMap<String, Icon> key2icon = new HashMap<String, Icon>();
		
    private static Icon lookupIcon(String key) {
        Icon icon = (Icon) key2icon.get(key);
        if (icon == null || icon.getIconWidth() == -1) {
            String fileName = key.toString() + ".gif";
            icon = ComponentUtilities.loadImageIcon(AnnotationsIcons.class, "images/" + fileName);
            key2icon.put(key, icon);
        }
        return icon;
    }
    	
    public static Icon getIcon(String key) {
        Icon icon = lookupIcon(key);
        if (icon == null) {
            icon = Icons.getUglyIcon();
        }
        return icon;
    }
	
	public static Icon getCommentIcon() {
		return getIcon("instance_comment_icon");
	}

	public static Icon getChangeAnnotationFullIcon() {
		return getIcon("instance_note_change_full_icon");
	}

	public static Icon getChangeAnnotationIcon() {
		return getIcon("instance_note_change_icon");
	}
	
	public static Icon getInstanceNoteFullIcon() {
		return getIcon("instance_note_full_icon");
	}
	
	public static Icon getOntologyAnnotationFullIcon() {
		return getIcon("instance_note_ontology_full_icon");
	}

	public static Icon getOntologyAnnotationIcon() {
		return getIcon("instance_note_ontology_icon");
	}

	public static Icon getOntologyAnnotationAndChangeIcon() {
		return getIcon("instance_note_and_change_icon");
	}

	
}
