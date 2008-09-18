package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ThreeValueCache<K> implements Cache {
	
	// Used to represent that a key has the value null in the cache
	// This is just internal representation and will change later
	private final static Integer NULL_VALLUE = new Integer(Integer.MIN_VALUE);
	
	HashMap<K, Object> key2cacheValueMap = new HashMap<K, Object>();
	
	
	public Object getCacheValue(Object key) {
		Object value = key2cacheValueMap.get(key);
		
		if (value == null || value.equals(NULL_VALLUE)) {
			return null;
		}
		
		return value;
	}
	
	public void putCacheValue(K key, Object value) {
		if (value == null) {
			key2cacheValueMap.put(key, NULL_VALLUE);
		} else {
			key2cacheValueMap.put(key, value);
		}
	}
	
	public void putNullCacheValue(K key) {
		key2cacheValueMap.put(key, NULL_VALLUE);
	}
	
	public void removeCacheValue(K key) {
		key2cacheValueMap.remove(key);
	}
	
	public boolean hasNullCacheValue(K key) {
		Object value = key2cacheValueMap.get(key);
		
		return (value == null ? false : (value.equals(NULL_VALLUE)));
	}
	
	public boolean hasCacheValue(K key) {
		return (key2cacheValueMap.get(key) != null);
	}
		
	public void clearCache() {
		key2cacheValueMap.clear();
	}
	
	public void dumpCache() {
		for (Iterator<K> iter = key2cacheValueMap.keySet().iterator(); iter.hasNext();) {
			K key = iter.next();
			System.out.println(key + ":" + key2cacheValueMap.get(key));			
		}
	}

}
