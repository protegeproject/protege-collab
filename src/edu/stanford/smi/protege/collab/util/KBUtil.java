package edu.stanford.smi.protege.collab.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultOntology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.bmir.protegex.chao.util.interval.SimpleTime;
import edu.stanford.smi.protege.code.generator.wrapping.OntologyJavaMappingUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.DefaultSimpleInstance;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.storage.database.AbstractDatabaseFrameDb;
import edu.stanford.smi.protege.storage.database.DatabaseKnowledgeBaseFactory;
import edu.stanford.smi.protege.storage.database.RobustConnection;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;

public class KBUtil {

    private static transient Logger log = Log.getLogger(KBUtil.class);

    private final static String ANNOTATION_ID = "AnnotationId";
    private final static String TIMESTAMP_ID = "TimestampId";
    private final static String TIMESTAMP_DATE = "TimestampDate";
    private final static String TIMESTAMP_SEQ = "TimestampSeq";

    public static Collection<Instance> getInstances(KnowledgeBase kb, Cls type) {
        Collection<Instance> instances = new LinkedHashSet<Instance>();
        instances.addAll(getDirectInstances(kb, type));
        for (Iterator iterator = type.getSubclasses().iterator(); iterator.hasNext();) {
            Cls cls = (Cls) iterator.next();
            instances.addAll(getDirectInstances(kb, cls));
        }
        return instances;
    }

    /**
     * This method will use direct database access to retrieve the direct instances of a class.
     * No additional caching will be done. No additional flags (e.g. included frame) are supported.
     * <p><b>
     * ********** Use this method at your own risk!! ************
     *</b></p>
     * @param kb - the knowledge base
     * @param type - class for which to return the direct instances
     * @return the direct instances of type as a a collection of {@link Instance}
     */
    public static Collection<Instance> getDirectInstances(KnowledgeBase kb, Cls type) {
        Collection<Instance> instances = null;

        if (kb.getKnowledgeBaseFactory() instanceof DatabaseKnowledgeBaseFactory) {
            try {
                instances = getDirectInstancesFromDb(kb, type);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error at computing directly from db the instances of " + type, e);
            }
        }

        if  (instances == null) {
            instances = type.getDirectInstances();
        }

        return instances;
    }

    private static Collection<Instance> getDirectInstancesFromDb(KnowledgeBase kb, Cls type) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("...Using direct database mode to fill get direct instances of " + type );
        }

        RobustConnection rconnection;
        try {
            rconnection = getRobustConnection(kb);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Could not connect to the ChAO database table.", e);
            return null;
        }

        Collection<Instance> instances = null;

        synchronized (kb) {
            try {
                instances = getDirectInstancesFromDb(rconnection, type);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error at creating the top level changes cache (db mode).", e);
                return null;
            } finally {
                rconnection.setIdle(true);
            }
        }

        return instances;
    }

    private static Collection<Instance> getDirectInstancesFromDb(RobustConnection rconnection, Cls type) throws SQLException {
        Collection<Instance> instances = new ArrayList<Instance>();

        KnowledgeBase kb = type.getKnowledgeBase();
        String table = DatabaseKnowledgeBaseFactory.getTableName(kb.getProject().getSources());
        if (table == null) {
            return null;
        }

        String directTypeSlot = kb.getSystemFrames().getDirectTypesSlot().getName();

        String getDirectInstancesQuery =
            "SELECT frame as frame FROM " + table +
                " WHERE slot='" + directTypeSlot + "' AND short_value='" + type.getName() + "'";

        PreparedStatement stmt = rconnection.getPreparedStatement(getDirectInstancesQuery);
        ResultSet rs = stmt.executeQuery();
        try {
            while (rs.next()) {
               String frameId = getValue(rs, "frame");
               instances.add(new DefaultSimpleInstance(kb, new FrameID(frameId)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        return instances;
    }


    /**
     * Fill the ontology annotations cache with calls directly to the DB
     * @param kb
     * @return - a sorted list of specific annotation objects
     */
    public static List<Annotation> getTopLevelAnnotationsFromDb(KnowledgeBase kb) {
        RobustConnection rconnection;
        try {
            rconnection = getRobustConnection(kb);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Could not connect to the ChAO database table.", e);
            return null;
        }

        List<Annotation> topAnnotations = null;

        synchronized (kb) {
            try {
                topAnnotations = getTopLevelAnnotationsFromDb(rconnection, kb);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error at creating the top level changes cache (db mode).", e);
                return null;
            } finally {
                rconnection.setIdle(true);
            }
        }

        return topAnnotations;
    }

    private static List<Annotation> getTopLevelAnnotationsFromDb(RobustConnection rconnection, KnowledgeBase kb) throws SQLException {
        TreeMap<SimpleTime, Instance> annotationsMap = new TreeMap<SimpleTime, Instance>();

        String table = DatabaseKnowledgeBaseFactory.getTableName(kb.getProject().getSources());
        if (table == null) {
            return null;
        }

        String topLevelChangesQuery =
            "SELECT DISTINCT allannotations.frame AS " + ANNOTATION_ID +", timestamp.short_value AS " + TIMESTAMP_ID + ", " +
                "timestampdate.short_value AS " + TIMESTAMP_DATE + ", timestampseq.short_value AS " + TIMESTAMP_SEQ +" FROM " + table +" AS allannotations " +
                     "JOIN " + table +" AS timestamp ON timestamp.slot='timestamp' AND timestamp.frame = allannotations.frame " +
                     "JOIN " + table +" AS timestampdate ON timestampdate.slot='date' AND timestampdate.frame = timestamp.short_value " +
                     "JOIN " + table +" AS timestampseq on timestampseq.slot='sequence' AND timestampseq.frame = timestamp.short_value " +
                          "WHERE (allannotations.slot='body' OR allannotations.slot='subject') " +
                              "AND NOT EXISTS (SELECT * FROM " + table +" AS subchanges " +
                                  "WHERE subchanges.slot='annotates' AND allannotations.frame = subchanges.frame LIMIT 1)";

        PreparedStatement stmt = rconnection.getPreparedStatement(topLevelChangesQuery);
        ResultSet rs = stmt.executeQuery();
        try {
            while (rs.next()) {
               String annotationId = getValue(rs, ANNOTATION_ID);
               String timestampid = getValue(rs, TIMESTAMP_ID);
               String timestampDate = getValue(rs, TIMESTAMP_DATE);
               String timestampSeq = getValue(rs, TIMESTAMP_SEQ);

               Date date = DefaultTimestamp.getDateParsed(timestampDate);
               int seq = timestampSeq == null ? 0 : Integer.parseInt(timestampSeq);

               if (annotationId == null || timestampid == null || date == null) {
                   log.warning("Skipping top level cache entry: " + annotationId + ", " + timestampid + ", " + timestampDate + ", " + timestampSeq + " Result set: " + rs );
               } else {
                   SimpleTime time = new SimpleTime(date, seq);
                   Instance annotationInst = new DefaultSimpleInstance(kb, new FrameID(annotationId));
                   annotationsMap.put(time, annotationInst);
               }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        List<Annotation> annotations = new ArrayList<Annotation>();

        for (SimpleTime time : annotationsMap.keySet()) {
            Instance annInst = annotationsMap.get(time);
            annotations.add(OntologyJavaMappingUtil.getSpecificObject(kb, annInst, Annotation.class));
        }

        return annotations;
    }


    public static Map<Ontology_Component, String> getOntologyComponentsWithAnnotationsFromDb(KnowledgeBase kb) {
        RobustConnection rconnection;
        try {
            rconnection = getRobustConnection(kb);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Could not connect to the ChAO database table.", e);
            return null;
        }

        Map<Ontology_Component, String> ocs = null;

        synchronized (kb) {
            try {
                ocs = getOntologyComponentsWithAnnotationsFromDb(rconnection, kb);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error at creating the top level changes cache (db mode).", e);
                return null;
            } finally {
                rconnection.setIdle(true);
            }
        }

        return ocs;
    }

    private static Map<Ontology_Component, String> getOntologyComponentsWithAnnotationsFromDb(RobustConnection rconnection,  KnowledgeBase kb) throws SQLException {
        Map<Ontology_Component, String> ocs = new HashMap<Ontology_Component, String>();

        String table = DatabaseKnowledgeBaseFactory.getTableName(kb.getProject().getSources());
        if (table == null) {
            return null;
        }

        String getDirectInstancesQuery = "SELECT DISTINCT allAnn.frame AS annotationid, curName.short_value AS curName FROM icd_ann AS allAnn " +
        		    "JOIN icd_ann AS assocAnn ON assocAnn.slot='associatedAnnotations' AND assocAnn.frame=allAnn.frame " +
        		    "JOIN icd_ann AS curName ON curName.slot='currentName' AND curName.frame=allAnn.frame " +
        		        "WHERE allAnn.slot='currentName'";

        PreparedStatement stmt = rconnection.getPreparedStatement(getDirectInstancesQuery);
        ResultSet rs = stmt.executeQuery();
        try {
            while (rs.next()) {
               String frameId = getValue(rs, "annotationid");
               String curName = getValue(rs, "curName");
               ocs.put(new DefaultOntology_Component(new DefaultSimpleInstance(kb, new FrameID(frameId))), curName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        return ocs;
    }

    /**
     * This method does not work if there are imports, because it queries only the active table.
     *
     * @param rconnection
     * @param table
     * @param superclsSlotName
     * @param clsName
     * @return
     * @throws SQLException
     */
    private static Collection<String> getDirectSuperclasses(RobustConnection rconnection, String table, String superclsSlotName, String clsName) throws SQLException {
        Collection<String> superclasses = new HashSet<String>();

        String getDirectInstancesQuery = "SELECT DISTINCT short_value AS  supercls FROM " + table +
        		" WHERE frame='" + clsName + "' and slot='" + superclsSlotName + "'" ;

        PreparedStatement stmt = rconnection.getPreparedStatement(getDirectInstancesQuery);
        ResultSet rs = stmt.executeQuery();
        try {
            while (rs.next()) {
               String supercls = getValue(rs, "supercls");
               superclasses.add(supercls);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return superclasses;
    }

    /*
     * Generic util methods
     */


    private static String getValue(ResultSet rs, String col) {
        String val = null;
        try {
            val = rs.getString(col);
        } catch (SQLException e) { //maybe just one value fails..
           log.log(Level.WARNING, "Error at retrieving top changes cache value: " + rs, e);
        }
        if (val == null || val.equals("")) {
            return null;
        }
        return val;
    }

    private static RobustConnection getRobustConnection(KnowledgeBase kb) throws SQLException {
        kb.getFrameStoreManager().getTerminalFrameStore();
        MergingNarrowFrameStore mnfs = MergingNarrowFrameStore.get(kb);
        NarrowFrameStore nfs = mnfs.getActiveFrameStore();
        AbstractDatabaseFrameDb dbFrameStore = null;
        do {
            if (nfs instanceof AbstractDatabaseFrameDb) {
                dbFrameStore = (AbstractDatabaseFrameDb) nfs;
            }
            nfs = nfs.getDelegate();
        }
        while (nfs != null);
        return dbFrameStore != null ? dbFrameStore.getCurrentConnection() : null;
    }


    /**
     * Computes all paths from a class to the root node by navigating on the
     * direct superclasses slot.
     *
     * @param cls
     *            - the resource name
     *
     * @return a collection of the paths from the resource to the root as strings
     * @throws SQLException
     */
    public static Collection<List<String>> getPathsToRoot(KnowledgeBase kb, String cls) throws SQLException {
        Collection<List<String>> results = new ArrayList<List<String>>();
        String rootClsName = kb.getRootCls().getName();
        if (cls.equals(rootClsName)) {
            results.add((List<String>) CollectionUtilities.createCollection(rootClsName));
            return results;
        }

        String directSuperclassesSlotName = kb.getSystemFrames().getDirectSuperclassesSlot().getName();
        String table = DatabaseKnowledgeBaseFactory.getTableName(kb.getProject().getSources());
        if (table == null) {
            throw new RuntimeException("Cannot use this method on a non-db project");
        }
        RobustConnection rconnection = null;
        try {
            rconnection = getRobustConnection(kb);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Could not connect to database table " + table, e);
        }
        if (rconnection == null) {
            throw new RuntimeException("Cannot connect to database table " + table);
        }

        getPathsToRoot(rconnection, rootClsName, directSuperclassesSlotName, table, cls, new LinkedList<String>(), results);

        return results;
    }

    private static void getPathsToRoot(RobustConnection rconnection, String rootClsName, String directSuperclassesSlotName, String table,
            String resource, List<String> path, Collection<List<String>> pathLists) throws SQLException {
        path.add(0, resource);

        Collection<String> parents = getDirectSuperclasses(rconnection, table, directSuperclassesSlotName, resource);

        for (String parent : parents) {
            if (parent.equals(rootClsName)) {
                List<String> copyPathList = new ArrayList<String>(path);
                copyPathList.add(0, parent);
                pathLists.add(copyPathList);
            } else if (!path.contains(parent)) {
                //if (ModelUtilities.isVisibleInGUI(parent)) { //TODO: do we want this?
                List<String> copyPath = new ArrayList<String>(path);
                getPathsToRoot(rconnection, rootClsName, directSuperclassesSlotName, table, parent, copyPath, pathLists);
                //}
            }
        }
    }

    
    /* ******************************************************************** *
     * TODO BEFORE THE NEXT PROTEGE RELEASE MOVE THE FOLLOWING 4 METHODS INTO 
     * 		THE ModelUtilities CLASS!!!!!!!!!!!
     *      AND 
     *      ACTIVATE THE ModelUtilities_Test JUNIT TEST!!!!!!!
     * ******************************************************************** */
    
    /**
     * Returns all own slot values for the slot <code>slot</code> for every superclass of a class.
     * 
     * @param cls - a class
     * @param slot - a slot
     * 
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     * 		of classes which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnAllSuperclasses(Cls cls, Slot slot) {
    	KnowledgeBase kb = cls.getKnowledgeBase();
    	Cls rootCls = kb.getRootCls();
    	Slot parentSlot = kb.getSystemFrames().getDirectSuperclassesSlot();
    	
    	return getPropertyValuesOnPropertyClosureToRoot(cls, parentSlot, rootCls, slot);
    }
    
    /**
     * Returns all own slot values for the slot <code>slot</code> for every instance 
     * in any of the paths between an instance (<code>resource</code>) and 
     * a root instances (<code>rootResource</code>) following the relationships 
     * defined by the <code>parentSlot</code> slot.
     * 
     * @param resource - a resource
     * @param parentSlot - the slot that is used to traverse the instance graph to the <code>rootResource</code>
     * @param rootResource - the resource that is the
     * @param slot - a slot
     * 
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     * 		of instances which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnPropertyClosureToRoot(
    		Instance resource, Slot parentSlot, Instance rootResource, Slot slot) {
    	Map<Object, List<Instance>> result = new HashMap<Object, List<Instance>>();
    	
    	Collection<List<Instance>> propertyClosureToRoot = getPropertyClosureToRoot(resource, parentSlot, rootResource);
    	Set<Instance> allNodes = new HashSet<Instance>();
    	for (List<Instance> path : propertyClosureToRoot) {
    		allNodes.addAll(path);
    	}
    	
    	for (Instance node : allNodes) {
    		Collection<?> values = node.getOwnSlotValues(slot);
    		for (Object value : values) {
    			if (value != null) {
    				List<Instance> nodeList = result.get(value);
    				if (nodeList == null) {
    					nodeList = new ArrayList<Instance>();
    					result.put(value, nodeList);
    				}
    				nodeList.add(node);
    			}
    		}
    	}
    	
    	return result;
    }

    /**
     * Computes all paths from an instance to the "root instance" node by navigating on a
     * given slot.
     * 
     * @param resource - an instance
     * @param parentSlot - slot to navigate on towards a "root node"
     * @param rootResource - an instance considered as the root of the navigation tree, 
     * 				necessary to stop the navigation. 
     * 
     * @return a collection of the paths from the resource to the root resource
     */
    public static Collection<List<Instance>> getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource) {
        Collection<List<Instance>> results = new ArrayList<List<Instance>>();
        if (resource.equals(rootResource)) {
            results.add(Collections.singletonList(rootResource));
            return results;
        }
        getPropertyClosureToRoot(resource, parentSlot, rootResource, new LinkedList<Instance>(), results);
        return results;
    }
    
    private static void getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource, 
    		List<Instance> path, Collection<List<Instance>> pathLists) {
        path.add(0, resource);

        Collection<?> parents = resource.getOwnSlotValues(parentSlot);

        for (Object parentObject : parents) {
        	if (parentObject instanceof Instance) {
        		Instance parentResource = (Instance) parentObject;
	            if (parentResource.equals(rootResource)) {
	                List<Instance> copyPathList = new ArrayList<Instance>(path);
	                copyPathList.add(0, parentResource);
	                pathLists.add(copyPathList);
	            } else if (!path.contains(parentResource)) {
	                //if (ModelUtilities.isVisibleInGUI(parentResource)) { //TODO: do we want this?
	                List<Instance> copyPath = new ArrayList<Instance>(path);
	                getPropertyClosureToRoot(parentResource, parentSlot, rootResource, copyPath, pathLists);
	                //}
	            }
        	}
        }
    }

    /* ******************************************************************** *
     * END OF TODO 
     * ******************************************************************** */

}
