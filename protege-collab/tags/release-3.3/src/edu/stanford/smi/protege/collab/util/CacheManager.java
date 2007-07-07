package edu.stanford.smi.protege.collab.util;

import java.util.HashMap;

import edu.stanford.smi.protege.model.KnowledgeBase;

public class CacheManager {
	//reimplemnent this later
/*
	private HashMap<Object, Cache> obj2cacheMap = new HashMap<Object, Cache>();
	
	private static CacheManager cacheManager;
	
	
	public static CacheManager getCacheManager() {
		if (cacheManager == null) {
			cacheManager = new CacheManager();
		}
		
		return cacheManager;
	}
			
	//get or create
	public ThreeValueCache getThreeValueCache(Object obj) {
		Cache cache = obj2cacheMap.get(obj);
		
		if (cache == null) {
			cache = new ThreeValueCache();
			obj2cacheMap.put(obj, cache);
		}
		
		//make check if instanceof threevaluecache??
		return (ThreeValueCache) cache;
	}
	
	public void clearCache(Object obj) {
		Cache cache = obj2cacheMap.get(obj);
		
		if (cache != null) {
			cache.clearCache();
		}
	}
	
*/	
	
}
