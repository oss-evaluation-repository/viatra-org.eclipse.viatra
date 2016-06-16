/*******************************************************************************
 * Copyright (c) 2010-2016, Grill Balázs, IncQueryLabs
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Grill Balázs - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.matchers.context;

/**
 * These are the different services which can be provided by an {@link IQueryRuntimeContext} implementation.
 * 
 * @author Grill Balázs
 * @since 1.4
 *
 */
public enum IndexingService {

    /**
     * Cardinality information is available. e.g. Number of instances of a class or the number of values in a feature.
     */
    STATISTICS,
    
    /**
     * The indexer can provide notifications about changes in the model.
     */
    NOTIFICATIONS,
    
    /**
     * Enables enumeration of instances and reverse-navigation.
     */
    INSTANCES
    
}
