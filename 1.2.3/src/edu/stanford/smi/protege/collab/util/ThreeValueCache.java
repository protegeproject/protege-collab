package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ThreeValueCache implements Cache {
	
	// Used to represent that a key has the value null in the cache
	// This is just internal representation and will change later
	private final static Integer NULL_VALLUE = new Integer(Integer.MIN_VALUE);
	
	HashMap<Object, Object> key2cacheValueMap = new HashMap<Object, Object>();
	
	
	public Object getCacheValue(Object key) {
		Object value = key2cacheValueMap.get(key);
		
		if (value == null || value.equals(NULL_VALLUE)) {
			return null;
		}
		
		return value;
	}
	
	public void putCacheValue(Object key, Object value) {
		if (value == null) {
			key2cacheValueMap.put(key, NULL_VALLUE);
		} else {
			key2cacheValueMap.put(key, value);
		}
	}
	
	public void putNullCacheValue(Object key) {
		key2cacheValueMap.put(key, NULL_VALLUE);
	}
	
	public void removeCacheValue(Object key) {
		key2cacheValueMap.remove(key);
	}
	
	public boolean hasNullCacheValue(Object key) {
		Object value = key2cacheValueMap.get(key);
		
		return (value == null ? false : (value.equals(NULL_VALLUE)));
	}
	
	public boolean hasCacheValue(Object key) {
		return (key2cacheValueMap.get(key) != null);
	}
		
	public void clearCache() {
		key2cacheValueMap.clear();
	}
	
	public void dumpCache() {
		for (Iterator iter = key2cacheValueMap.keySet().iterator(); iter.hasNext();) {
			Object key = (Object) iter.next();
			System.out.println(key + ":" + key2cacheValueMap.get(key));			
		}
	}

}
