/*******************************************************************************
 * Copyright (c) 2004-2008 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.query.runtime.rete.single;

import java.util.Collection;
import java.util.Map;

import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.rete.network.Direction;
import org.eclipse.viatra.query.runtime.rete.network.ReteContainer;
import org.eclipse.viatra.query.runtime.rete.network.communication.ddf.DifferentialTimestamp;

/**
 * Simply propagates everything. Might be used to join or fork.
 * 
 * @author Gabor Bergmann
 */
public class TransparentNode extends SingleInputNode {

    public TransparentNode(final ReteContainer reteContainer) {
        super(reteContainer);
    }

    @Override
    public void update(final Direction direction, final Tuple updateElement, final DifferentialTimestamp timestamp) {
        propagateUpdate(direction, updateElement, timestamp);

    }

    @Override
    public void pullInto(final Collection<Tuple> collector, final boolean flush) {
        propagatePullInto(collector, flush);
    }

    @Override
    public void pullIntoWithTimestamp(final Map<Tuple, DifferentialTimestamp> collector, final boolean flush) {
        propagatePullIntoWithTimestamp(collector, flush);
    }
    
}
