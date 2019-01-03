/*******************************************************************************
 * Copyright (c) 2004-2009 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.query.runtime.rete.index;

import java.util.Collection;
import java.util.Map;

import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.matchers.tuple.TupleMask;
import org.eclipse.viatra.query.runtime.rete.network.Direction;
import org.eclipse.viatra.query.runtime.rete.network.ReteContainer;
import org.eclipse.viatra.query.runtime.rete.network.communication.ddf.DifferentialTimestamp;

/**
 * Performs a left outer join.
 * 
 * @author Gabor Bergmann
 * 
 */
public class OuterJoinNode extends DualInputNode {

    final Tuple defaults;

    /**
     * @param reteContainer
     * @param complementerSecondaryMask
     * @param defaults
     *            the default line to use instead of missing elements if a left tuple has no match
     * 
     */
    public OuterJoinNode(final ReteContainer reteContainer, final TupleMask complementerSecondaryMask,
            final Tuple defaults) {
        super(reteContainer, complementerSecondaryMask);
        this.defaults = defaults;
        this.logic = createLogic();
    }

    @Override
    public Tuple calibrate(final Tuple primary, final Tuple secondary) {
        return unify(primary, secondary);
    }

    private Tuple unifyWithDefaults(final Tuple ps) {
        return unify(ps, defaults);
    }

    private final NetworkStructureChangeSensitiveLogic DEFAULT = new NetworkStructureChangeSensitiveLogic() {

        @Override
        public void pullIntoWithTimestamp(final Map<Tuple, DifferentialTimestamp> collector, final boolean flush) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void pullInto(final Collection<Tuple> collector, final boolean flush) {
            if (primarySlot == null || secondarySlot == null) {
                return;
            }
            if (flush) {
                reteContainer.flushUpdates();
            }

            for (final Tuple signature : primarySlot.getSignatures()) {
                // primaries can not be null due to the contract of IterableIndex.getSignatures()
                final Collection<Tuple> primaries = primarySlot.get(signature);
                final Collection<Tuple> opposites = secondarySlot.get(signature);
                if (opposites != null) {
                    for (final Tuple primary : primaries) {
                        for (final Tuple opposite : opposites) {
                            collector.add(unify(primary, opposite));
                        }
                    }
                } else {
                    for (final Tuple primary : primaries) {
                        collector.add(unifyWithDefaults(primary));
                    }
                }
            }
        }

        @Override
        public void notifyUpdate(final Side side, final Direction direction, final Tuple updateElement,
                final Tuple signature, final boolean change, final DifferentialTimestamp timestamp) {
            final Collection<Tuple> opposites = retrieveOpposites(side, signature);
            switch (side) {
            case PRIMARY:
                if (opposites != null) {
                    for (final Tuple opposite : opposites) {
                        propagateUpdate(direction, unify(updateElement, opposite), timestamp);
                    }
                } else {
                    propagateUpdate(direction, unifyWithDefaults(updateElement), timestamp);
                }
                break;
            case SECONDARY:
                if (opposites != null) {
                    for (final Tuple opposite : opposites) {
                        propagateUpdate(direction, unify(opposite, updateElement), timestamp);
                        if (change) {
                            propagateUpdate(direction.opposite(), unifyWithDefaults(opposite), timestamp);
                        }
                    }
                }
                break;
            case BOTH:
                for (final Tuple opposite : opposites) {
                    propagateUpdate(direction, unify(updateElement, opposite), timestamp);
                    if (updateElement.equals(opposite)) {
                        continue;
                    }
                    propagateUpdate(direction, unify(opposite, updateElement), timestamp);
                }
                if (direction == Direction.REVOKE) {
                    // missed joining with itself
                    propagateUpdate(direction, unify(updateElement, updateElement), timestamp);
                }
            }
        }
    };

    private final NetworkStructureChangeSensitiveLogic RECURSIVE_TIMELY = new NetworkStructureChangeSensitiveLogic() {

        @Override
        public void pullIntoWithTimestamp(final Map<Tuple, DifferentialTimestamp> collector, final boolean flush) {
            if (primarySlot == null || secondarySlot == null) {
                return;
            }
            if (flush) {
                reteContainer.flushUpdates();
            }

            for (final Tuple signature : primarySlot.getSignatures()) {
                // primaries can not be null due to the contract of IterableIndex.getSignatures()
                final Map<Tuple, DifferentialTimestamp> primaries = getWithTimestamp(signature, primarySlot);
                final Map<Tuple, DifferentialTimestamp> opposites = getWithTimestamp(signature, secondarySlot);
                if (opposites != null) {
                    for (final Tuple primary : primaries.keySet()) {
                        final DifferentialTimestamp primaryTimestamp = primaries.get(primary);
                        for (final Tuple opposite : opposites.keySet()) {
                            collector.put(unify(primary, opposite),
                                    primaryTimestamp.max(opposites.get(opposite)));
                        }
                    }
                } else {
                    for (final Tuple primary : primaries.keySet()) {
                        collector.put(unifyWithDefaults(primary), primaries.get(primary));
                    }
                }
            }
        }

        @Override
        public void pullInto(final Collection<Tuple> collector, final boolean flush) {
            OuterJoinNode.this.DEFAULT.pullInto(collector, flush);
        }

        @Override
        public void notifyUpdate(final Side side, final Direction direction, final Tuple updateElement,
                final Tuple signature, final boolean change, final DifferentialTimestamp timestamp) {
            final Indexer oppositeIndexer = getSlot(side.opposite());
            final Map<Tuple, DifferentialTimestamp> opposites = getWithTimestamp(signature, oppositeIndexer);

            switch (side) {
            case PRIMARY:
                if (opposites != null) {
                    for (final Tuple opposite : opposites.keySet()) {
                        propagateUpdate(direction, unify(updateElement, opposite), timestamp.max(opposites.get(opposite)));
                    }
                } else {
                    propagateUpdate(direction, unifyWithDefaults(updateElement), timestamp);
                }
                break;
            case SECONDARY:
                if (opposites != null) {
                    for (final Tuple opposite : opposites.keySet()) {
                        final DifferentialTimestamp oppositeTimestamp = opposites.get(opposite);
                        propagateUpdate(direction, unify(opposite, updateElement), timestamp.max(oppositeTimestamp));
                        if (change) {
                            propagateUpdate(direction.opposite(), unifyWithDefaults(opposite), oppositeTimestamp);
                        }
                    }
                }
                break;
            case BOTH:
                for (final Tuple opposite : opposites.keySet()) {
                    final DifferentialTimestamp oppositeTimestamp = opposites.get(opposite);
                    propagateUpdate(direction, unify(updateElement, opposite), timestamp.max(oppositeTimestamp));
                    if (updateElement.equals(opposite)) {
                        continue;
                    }
                    propagateUpdate(direction, unify(opposite, updateElement), timestamp.max(oppositeTimestamp));
                }
                if (direction == Direction.REVOKE) {
                    // missed joining with itself
                    propagateUpdate(direction, unify(updateElement, updateElement), timestamp);
                }
            }
        }
    };

    @Override
    protected NetworkStructureChangeSensitiveLogic createDefaultLogic() {
        return this.DEFAULT;
    }

    @Override
    protected NetworkStructureChangeSensitiveLogic createRecursiveTimelyLogic() {
        return this.RECURSIVE_TIMELY;
    }

}
