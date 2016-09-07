/*******************************************************************************
 * Copyright (c) 2010-2015, Bergmann Gabor, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bergmann Gabor - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.emf;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.viatra.query.runtime.base.api.DataTypeListener;
import org.eclipse.viatra.query.runtime.base.api.FeatureListener;
import org.eclipse.viatra.query.runtime.base.api.IEStructuralFeatureProcessor;
import org.eclipse.viatra.query.runtime.base.api.IndexingLevel;
import org.eclipse.viatra.query.runtime.base.api.InstanceListener;
import org.eclipse.viatra.query.runtime.base.api.NavigationHelper;
import org.eclipse.viatra.query.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.viatra.query.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.viatra.query.runtime.emf.types.EStructuralFeatureInstancesKey;
import org.eclipse.viatra.query.runtime.matchers.context.AbstractQueryRuntimeContext;
import org.eclipse.viatra.query.runtime.matchers.context.IInputKey;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryMetaContext;
import org.eclipse.viatra.query.runtime.matchers.context.IQueryRuntimeContextListener;
import org.eclipse.viatra.query.runtime.matchers.context.IndexingService;
import org.eclipse.viatra.query.runtime.matchers.context.common.JavaTransitiveInstancesKey;
import org.eclipse.viatra.query.runtime.matchers.tuple.FlatTuple;
import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * The EMF-based runtime query context, backed by an IQBase NavigationHelper.
 * 
 * @author Bergmann Gabor
 *
 * <p> TODO: {@link #ensureIndexed(EClass)} may be inefficient if supertype already cached.
 */
public class EMFQueryRuntimeContext extends AbstractQueryRuntimeContext {
	protected final NavigationHelper baseIndex;
    //private BaseIndexListener listener;
    
	protected final Map<EClass, EnumSet<IndexingService>> indexedClasses = Maps.newHashMap();
	protected final Map<EDataType, EnumSet<IndexingService>> indexedDataTypes = Maps.newHashMap();
	protected final Map<EStructuralFeature, EnumSet<IndexingService>> indexedFeatures = Maps.newHashMap();
    
	protected final EMFQueryMetaContext metaContext = EMFQueryMetaContext.INSTANCE;

	protected Logger logger;

    private EMFScope emfScope;

    public EMFQueryRuntimeContext(NavigationHelper baseIndex, Logger logger, EMFScope emfScope) {
        this.baseIndex = baseIndex;
        this.logger = logger;
        //this.listener = new BaseIndexListener(iqEngine);
        this.emfScope = emfScope;
    }
    
    public EMFScope getEmfScope() {
        return emfScope;
    }
    
    /**
     * Utility method to add an indexing service to a given key. Returns true if the requested service was
     * not present before this call.
     * @param map
     * @param key
     * @param service
     * @return
     */
    private static <K> boolean addIndexingService(Map<K, EnumSet<IndexingService>> map, K key, IndexingService service){
        EnumSet<IndexingService> current = map.get(key);
        if (current == null){
            current = EnumSet.of(service);
            map.put(key, current);
            return true;
        }else{
            return current.add(service);
        }
    }
    
    public void dispose() {
        //baseIndex.removeFeatureListener(indexedFeatures, listener);
        indexedFeatures.clear();
        //baseIndex.removeInstanceListener(indexedClasses, listener);
        indexedClasses.clear();
        //baseIndex.removeDataTypeListener(indexedDataTypes, listener);
        indexedDataTypes.clear();
        
        // No need to remove listeners, as NavHelper will be disposed imminently.
    }

    @Override
    public <V> V coalesceTraversals(Callable<V> callable) throws InvocationTargetException {
        return baseIndex.coalesceTraversals(callable);
    }
    
    @Override
    public boolean isCoalescing() {
    	return baseIndex.isCoalescing();
    }
    
    @Override
    public IQueryMetaContext getMetaContext() {
    	return metaContext;
    }
    
    @Override
    public void ensureIndexed(IInputKey key, IndexingService service) {
        ensureEnumerableKey(key);
        if (key instanceof EClassTransitiveInstancesKey) {
            EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
            ensureIndexed(eClass, service);
        } else if (key instanceof EDataTypeInSlotsKey) {
            EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
            ensureIndexed(dataType, service);
        } else if (key instanceof EStructuralFeatureInstancesKey) {
            EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
            ensureIndexed(feature, service);
        } else {
            illegalInputKey(key);
        }
    }
    
    @Override
    public void ensureIndexed(IInputKey key) {
    	this.ensureIndexed(key, IndexingService.INSTANCES);
    }
    
    /**
     * Retrieve the current registered indexing services for the given key. May not null,
     * returns an empty set if no indexing is registered.
     * 
     * @since 1.4
     */
    protected EnumSet<IndexingService> getCurrentIndexingServiceFor(IInputKey key){
        ensureValidKey(key);
        if (key instanceof JavaTransitiveInstancesKey) {
            return EnumSet.noneOf(IndexingService.class);
        } else if (key instanceof EClassTransitiveInstancesKey) {
            EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
            EnumSet<IndexingService> is = indexedClasses.get(eClass);
            return is == null ? EnumSet.noneOf(IndexingService.class) : is; 
        } else if (key instanceof EDataTypeInSlotsKey) {
            EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
            EnumSet<IndexingService> is =  indexedDataTypes.get(dataType);
            return is == null ? EnumSet.noneOf(IndexingService.class) : is;
        } else if (key instanceof EStructuralFeatureInstancesKey) {
            EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
            EnumSet<IndexingService> is =  indexedFeatures.get(feature);
            return is == null ? EnumSet.noneOf(IndexingService.class) : is;
        } else {
            illegalInputKey(key);
            return EnumSet.noneOf(IndexingService.class);
        }
    }
    
    @Override
    public boolean isIndexed(IInputKey key, IndexingService service) {
        return getCurrentIndexingServiceFor(key).contains(service);
    }
    
    @Override
    public boolean isIndexed(IInputKey key) {
    	return isIndexed(key, IndexingService.INSTANCES);
    }
    
    @Override
    public boolean containsTuple(IInputKey key, Tuple seed) {
    	ensureValidKey(key);
    	if (key instanceof JavaTransitiveInstancesKey) {
    		Class<?> instanceClass = forceGetWrapperInstanceClass((JavaTransitiveInstancesKey) key);
			if (instanceClass != null)
				return instanceClass.isInstance(getFromSeed(seed, 0));
			else
				return false;
    	} else {
    		ensureIndexed(key);
    		if (key instanceof EClassTransitiveInstancesKey) {
    			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
    			// instance check not enough, must lookup from index
    			return baseIndex.getAllInstances(eClass).contains(getFromSeed(seed, 0));
    		} else if (key instanceof EDataTypeInSlotsKey) {
    			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
    	    	return baseIndex.getDataTypeInstances(dataType).contains(getFromSeed(seed, 0));
    		} else if (key instanceof EStructuralFeatureInstancesKey) {
    			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
    	    	return baseIndex.findByFeatureValue(getFromSeed(seed, 1), feature).contains(getFromSeed(seed, 0));
    		} else {
    			illegalInputKey(key);
    			return false;
    		}
    	}
    }

	private Class<?> forceGetWrapperInstanceClass(JavaTransitiveInstancesKey key) {
		Class<?> instanceClass;
		try {
			instanceClass = key.forceGetWrapperInstanceClass();
		} catch (ClassNotFoundException e) {
			logger.error("Could not load instance class for type constraint " + key.getWrappedKey(), e);
			instanceClass = null;
		}
		return instanceClass;
	}
    
    @Override
    public Iterable<Tuple> enumerateTuples(IInputKey key, Tuple seed) {
		ensureIndexed(key);
		final Collection<Tuple> result = new HashSet<Tuple>();
		
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return Iterables.transform(baseIndex.getAllInstances(eClass), wrapUnary);
			} else { // fully seeded
				if (containsTuple(key, seed)) 
					result.add(new FlatTuple(seedInstance));
			}
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return Iterables.transform(baseIndex.getDataTypeInstances(dataType), wrapUnary);
			} else { // fully seeded
				if (containsTuple(key, seed)) 
					result.add(new FlatTuple(seedInstance));
			}
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			
			final Object seedSource = getFromSeed(seed, 0);
			final Object seedTarget = getFromSeed(seed, 1);
			if (seedSource == null && seedTarget != null) { 
				final Set<EObject> results = baseIndex.findByFeatureValue(seedTarget, feature);
				return Iterables.transform(results, new Function<Object, Tuple>() {
					@Override
					public Tuple apply(Object obj) {
						return new FlatTuple(obj, seedTarget);
					}
				});
			} else if (seedSource != null && seedTarget != null) { // fully seeded
				if (containsTuple(key, seed)) 
					result.add(new FlatTuple(seedSource, seedTarget));
			} else if (seedSource == null && seedTarget == null) { // fully unseeded
				baseIndex.processAllFeatureInstances(feature, new IEStructuralFeatureProcessor() {
					public void process(EStructuralFeature feature, EObject source, Object target) {
						result.add(new FlatTuple(source, target));
					};
				});
			} else if (seedSource != null && seedTarget == null) { 
				final Set<Object> results = baseIndex.getFeatureTargets((EObject) seedSource, feature);
				return Iterables.transform(results, new Function<Object, Tuple>() {
					public Tuple apply(Object obj) {
						return new FlatTuple(seedSource, obj);
					}
				});
			} 
		} else {
			illegalInputKey(key);
		}
		
		
		return result;
    }

	private static Function<Object, Tuple> wrapUnary = new Function<Object, Tuple>() {
		@Override
		public Tuple apply(Object obj) {
			return new FlatTuple(obj);
		}
	};

    @Override
    public Iterable<? extends Object> enumerateValues(IInputKey key, Tuple seed) {
		ensureIndexed(key);
		
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return baseIndex.getAllInstances(eClass);
			} else {
				// must be unseeded, this is enumerateValues after all!
				illegalEnumerateValues(seed);
			}
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return baseIndex.getDataTypeInstances(dataType);
			} else {
				// must be unseeded, this is enumerateValues after all!
				illegalEnumerateValues(seed);
			}
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			
			Object seedSource = getFromSeed(seed, 0);
			Object seedTarget = getFromSeed(seed, 1);
			if (seedSource == null && seedTarget != null) { 
				return baseIndex.findByFeatureValue(seedTarget, feature);
			} else if (seedSource != null && seedTarget == null) { 
				return baseIndex.getFeatureTargets((EObject) seedSource, feature);
			} else {
				// must be singly unseeded, this is enumerateValues after all!
				illegalEnumerateValues(seed);
			}
		} else {
			illegalInputKey(key);
		}
		return null;
    }
    
    @Override
    public int countTuples(IInputKey key, Tuple seed) {
		ensureIndexed(key, IndexingService.STATISTICS);
		
		if (key instanceof EClassTransitiveInstancesKey) {
			EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return baseIndex.countAllInstances(eClass);
			} else { // fully seeded
				return (containsTuple(key, seed)) ? 1 : 0;
			}
		} else if (key instanceof EDataTypeInSlotsKey) {
			EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
			
			Object seedInstance = getFromSeed(seed, 0);
			if (seedInstance == null) { // unseeded
				return baseIndex.countDataTypeInstances(dataType);
			} else { // fully seeded
				return (containsTuple(key, seed)) ? 1 : 0;
			}
		} else if (key instanceof EStructuralFeatureInstancesKey) {
			EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
			
			final Object seedSource = getFromSeed(seed, 0);
			final Object seedTarget = getFromSeed(seed, 1);
			if (seedSource == null && seedTarget != null) { 
				return baseIndex.findByFeatureValue(seedTarget, feature).size();
			} else if (seedSource != null && seedTarget != null) { // fully seeded
				return (containsTuple(key, seed)) ? 1 : 0;
			} else if (seedSource == null && seedTarget == null) { // fully unseeded
				return baseIndex.countFeatures(feature);
			} else if (seedSource != null && seedTarget == null) { 
				return baseIndex.countFeatureTargets((EObject) seedSource, feature);
			} 
		} else {
			illegalInputKey(key);
		}
		return 0;
    }
    
    
    
	public void ensureEnumerableKey(IInputKey key) {
		ensureValidKey(key);
		if (! metaContext.isEnumerable(key))
			throw new IllegalArgumentException("Key is not enumerable: " + key);
		
	}

	public void ensureValidKey(IInputKey key) {
		metaContext.ensureValidKey(key);
	}
	public void illegalInputKey(IInputKey key) {
		metaContext.illegalInputKey(key);
	}
	public void illegalEnumerateValues(Tuple seed) {
		throw new IllegalArgumentException("Must have exactly one unseeded element in enumerateValues() invocation, received instead: " + seed);
	}

	/**
	 * @deprecated use {@link #ensureIndexed(EClass, IndexingService)} instead
	 * @param eClass
	 */
	@Deprecated
	public void ensureIndexed(EClass eClass){
	    ensureIndexed(eClass, IndexingService.INSTANCES);
	}
	
	/**
     * @since 1.4
     */
	public void ensureIndexed(EClass eClass, IndexingService service) {
        if (addIndexingService(indexedClasses, eClass, service)) {
            final Set<EClass> newClasses = Collections.singleton(eClass);
            if (!baseIndex.isInWildcardMode())
                baseIndex.registerEClasses(newClasses, IndexingLevel.toLevel(service));
            //baseIndex.addInstanceListener(newClasses, listener);
        }
    }

	/**
	 * @deprecated use {@link #ensureIndexed(EDataType, IndexingService)} instead
	 * @param eDataType
	 */
	@Deprecated
	public void ensureIndexed(EDataType eDataType){
	    ensureIndexed(eDataType, IndexingService.INSTANCES);
	}
	
    /**
     * @since 1.4
     */
    public void ensureIndexed(EDataType eDataType, IndexingService service) {
        if (addIndexingService(indexedDataTypes, eDataType, service)) {
            final Set<EDataType> newDataTypes = Collections.singleton(eDataType);
            if (!baseIndex.isInWildcardMode())
                baseIndex.registerEDataTypes(newDataTypes, IndexingLevel.toLevel(service));
            //baseIndex.addDataTypeListener(newDataTypes, listener);
        }
    }

    /**
     * @deprecated use {@link #ensureIndexed(EStructuralFeature, IndexingService)} instead
     * @param feature
     */
    @Deprecated
    public void ensureIndexed(EStructuralFeature feature){
        ensureIndexed(feature, IndexingService.INSTANCES);
    }
    
    /**
     * @since 1.4
     */
    public void ensureIndexed(EStructuralFeature feature, IndexingService service) {
        if (addIndexingService(indexedFeatures, feature, service)) {
            final Set<EStructuralFeature> newFeatures = Collections.singleton(feature);
            if (!baseIndex.isInWildcardMode())
                baseIndex.registerEStructuralFeatures(newFeatures, IndexingLevel.toLevel(service));
            //baseIndex.addFeatureListener(newFeatures, listener);
        }
    }
    

    
    // UPDATE HANDLING SECTION 
    
    /**
     * Abstract internal listener wrapper for a {@link IQueryRuntimeContextListener}. 
     * Due to the overridden equals/hashCode(), it is safe to create a new instance for the same listener.
     * 
     * @author Bergmann Gabor
     */
    private abstract static class ListenerAdapter { 
    	IQueryRuntimeContextListener listener;
		Tuple seed;
		/**
		 * @param listener
		 * @param seed must be non-null
		 */
		public ListenerAdapter(IQueryRuntimeContextListener listener, Object... seed) {
			this.listener = listener;
			this.seed = new FlatTuple(seed);
		}
				
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((listener == null) ? 0 : listener.hashCode());
			result = prime * result + ((seed == null) ? 0 : seed.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj.getClass().equals(this.getClass())))
				return false;
			ListenerAdapter other = (ListenerAdapter) obj;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			if (seed == null) {
				if (other.seed != null)
					return false;
			} else if (!seed.equals(other.seed))
				return false;
			return true;
		}


		@Override
		public String toString() {
			return "Wrapped<Seed:" + seed + ">#" + listener;
		}
		
		
    }
    private static class EClassTransitiveInstancesAdapter extends ListenerAdapter implements InstanceListener {
		private Object seedInstance;
		public EClassTransitiveInstancesAdapter(IQueryRuntimeContextListener listener, Object seedInstance) {
			super(listener, seedInstance);
			this.seedInstance = seedInstance;
		}
    	@Override
    	public void instanceInserted(EClass clazz, EObject instance) {
    		if (seedInstance != null && !seedInstance.equals(instance)) return;
    		listener.update(new EClassTransitiveInstancesKey(clazz), 
    				new FlatTuple(instance), true);
    	}
    	@Override
    	public void instanceDeleted(EClass clazz, EObject instance) {
    		if (seedInstance != null && !seedInstance.equals(instance)) return;
    		listener.update(new EClassTransitiveInstancesKey(clazz), 
    				new FlatTuple(instance), false);
    	}    	
    }
    private static class EDataTypeInSlotsAdapter extends ListenerAdapter implements DataTypeListener {
		private Object seedValue;
		public EDataTypeInSlotsAdapter(IQueryRuntimeContextListener listener, Object seedValue) {
			super(listener, seedValue);
			this.seedValue = seedValue;
		}
		@Override
		public void dataTypeInstanceInserted(EDataType type, Object instance,
				boolean firstOccurrence) {
    		if (firstOccurrence) {
        		if (seedValue != null && !seedValue.equals(instance)) return;
				listener.update(new EDataTypeInSlotsKey(type), 
	    				new FlatTuple(instance), true);
    		}
		}
		@Override
		public void dataTypeInstanceDeleted(EDataType type, Object instance,
				boolean lastOccurrence) {
			if (lastOccurrence) {
        		if (seedValue != null && !seedValue.equals(instance)) return;
	    		listener.update(new EDataTypeInSlotsKey(type), 
	    				new FlatTuple(instance), false);
			}
		}
    }
    private static class EStructuralFeatureInstancesKeyAdapter extends ListenerAdapter implements FeatureListener {
		private Object seedHost;
		private Object seedValue;
		public EStructuralFeatureInstancesKeyAdapter(IQueryRuntimeContextListener listener, Object seedHost, Object seedValue) {
			super(listener, seedHost, seedValue);
			this.seedHost = seedHost;
			this.seedValue = seedValue;
		}
		@Override
		public void featureInserted(EObject host, EStructuralFeature feature,
				Object value) {
    		if (seedHost != null && !seedHost.equals(host)) return;
    		if (seedValue != null && !seedValue.equals(value)) return;
    		listener.update(new EStructuralFeatureInstancesKey(feature), 
    				new FlatTuple(host, value), true);
		}
		@Override
		public void featureDeleted(EObject host, EStructuralFeature feature,
				Object value) {
    		if (seedHost != null && !seedHost.equals(host)) return;
    		if (seedValue != null && !seedValue.equals(value)) return;
    		listener.update(new EStructuralFeatureInstancesKey(feature), 
    				new FlatTuple(host, value), false);
		}    	
    }
    
    @Override
    public void addUpdateListener(IInputKey key, Tuple seed /* TODO ignored */, IQueryRuntimeContextListener listener) {
		// stateless, so NOP
    	if (key instanceof JavaTransitiveInstancesKey) return;

    	ensureIndexed(key);
    	if (key instanceof EClassTransitiveInstancesKey) {
    		EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
    		baseIndex.addInstanceListener(Collections.singleton(eClass), 
    				new EClassTransitiveInstancesAdapter(listener, seed.get(0)));
    	} else if (key instanceof EDataTypeInSlotsKey) {
    		EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
    		baseIndex.addDataTypeListener(Collections.singleton(dataType), 
    				new EDataTypeInSlotsAdapter(listener, seed.get(0)));
    	} else if (key instanceof EStructuralFeatureInstancesKey) {
    		EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
    		baseIndex.addFeatureListener(Collections.singleton(feature), 
    				new EStructuralFeatureInstancesKeyAdapter(listener, seed.get(0), seed.get(1)));
    	} else {
    		illegalInputKey(key);
    	}
    }
    @Override
    public void removeUpdateListener(IInputKey key, Tuple seed, IQueryRuntimeContextListener listener) {
		// stateless, so NOP
    	if (key instanceof JavaTransitiveInstancesKey) return;

    	ensureIndexed(key);
    	if (key instanceof EClassTransitiveInstancesKey) {
    		EClass eClass = ((EClassTransitiveInstancesKey) key).getEmfKey();
    		baseIndex.removeInstanceListener(Collections.singleton(eClass), 
    				new EClassTransitiveInstancesAdapter(listener, seed.get(0)));
    	} else if (key instanceof EDataTypeInSlotsKey) {
    		EDataType dataType = ((EDataTypeInSlotsKey) key).getEmfKey();
    		baseIndex.removeDataTypeListener(Collections.singleton(dataType), 
    				new EDataTypeInSlotsAdapter(listener, seed.get(0)));
    	} else if (key instanceof EStructuralFeatureInstancesKey) {
    		EStructuralFeature feature = ((EStructuralFeatureInstancesKey) key).getEmfKey();
    		baseIndex.removeFeatureListener(Collections.singleton(feature), 
    				new EStructuralFeatureInstancesKeyAdapter(listener, seed.get(0), seed.get(1)));
    	} else {
    		illegalInputKey(key);
    	}
    }    
    
    private Object getFromSeed(Tuple seed, int index) {
    	return seed == null ? null : seed.get(index);
    }
    
    // TODO wrap / unwrap enum literals 
    // TODO use this in all other public methods (maybe wrap & delegate?)
    
    @Override
    public Object unwrapElement(Object internalElement) {
    	return internalElement;
    }
    @Override
    public Tuple unwrapTuple(Tuple internalElements) {
    	return internalElements;
    }
    @Override
    public Object wrapElement(Object externalElement) {
    	return externalElement;
    }
    @Override
    public Tuple wrapTuple(Tuple externalElements) {
    	return externalElements;
    }
    
    @Override
    public void ensureWildcardIndexing(IndexingService service) {
        baseIndex.setWildcardLevel(IndexingLevel.toLevel(service));
    }
    
    @Override
    public void executeAfterTraversal(Runnable runnable) throws InvocationTargetException {
        baseIndex.executeAfterTraversal(runnable);
    }
}

