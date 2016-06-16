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
package org.eclipse.viatra.query.runtime.base.api;

import java.util.EnumSet;

import org.eclipse.viatra.query.runtime.matchers.context.IndexingService;

/**
 * The values of this enum denotes the level of indexing the base indexer is capable of.
 * 
 * @author Grill Balázs
 * @since 1.4
 *
 */
public enum IndexingLevel {

    /**
     * No indexing is performed
     */
    NONE,
    
    /**
     * Only cardinality information is stored
     */
    STATISTICS,
    
    /**
     * Notifications are dispatched about the changes
     */
    NOTIFICATIONS,
    
    /**
     * Cardinality information is stored and live notifications are dispatched
     */
    BOTH,
    
    /**
     * Full indexing is performed, set of instances is available
     */
    FULL
    
    ;
 
    private static final IndexingLevel[][] mergeTable = {
                         /* NONE            STATISTICS  NOTIFICATIONS   BOTH     FULL*/
       /* NONE          */{ NONE,           STATISTICS, NOTIFICATIONS,  BOTH,    FULL},
       /* STATISTICS    */{ STATISTICS,     STATISTICS, BOTH,           BOTH,    FULL},
       /* NOTIFICATIONS */{ NOTIFICATIONS,  BOTH,       NOTIFICATIONS,  BOTH,    FULL},
       /* BOTH          */{ BOTH,           BOTH,       BOTH,           BOTH,    FULL},
       /* FULL          */{ FULL,           FULL,       FULL,           FULL,    FULL}
    };
    
    public static IndexingLevel toLevel(IndexingService service){
        switch(service){
        case INSTANCES:
            return IndexingLevel.FULL;
        case NOTIFICATIONS:
            return IndexingLevel.NOTIFICATIONS;
        case STATISTICS:
            return IndexingLevel.STATISTICS;
        default:
           return IndexingLevel.NONE;
        }
    }
    
    public static IndexingLevel toLevel(EnumSet<IndexingService> services){
        IndexingLevel result = NONE;
        for(IndexingService service : services){
            result = result.merge(toLevel(service));
        }
        return result;
    }
    
    /**
     * Merge this level with the given other level, The resulting indexing level will provide the 
     * functionality which conforms to both given levels.
     * 
     * @param other
     * @return
     */
    public IndexingLevel merge(IndexingLevel other){
        if (other == null) return this;
        return mergeTable[this.ordinal()][other.ordinal()];
    }

    /**
     * Tells whether the indexer shall perform separate statistics calculation for this level
     * 
     * @return
     */
    public boolean hasStatistics() {
        return this == IndexingLevel.BOTH || this == IndexingLevel.STATISTICS || this == IndexingLevel.FULL;
    }
    
}
