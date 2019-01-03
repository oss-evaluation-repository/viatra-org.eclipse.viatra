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

package org.eclipse.viatra.query.runtime.rete.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.viatra.query.runtime.matchers.tuple.Tuple;
import org.eclipse.viatra.query.runtime.rete.network.Direction;
import org.eclipse.viatra.query.runtime.rete.network.Receiver;
import org.eclipse.viatra.query.runtime.rete.network.ReteContainer;
import org.eclipse.viatra.query.runtime.rete.network.communication.ddf.DifferentialTimestamp;
import org.eclipse.viatra.query.runtime.rete.single.SingleInputNode;

/**
 * This node delivers updates to a remote recipient; no updates are propagated further in this network.
 * 
 * @author Gabor Bergmann
 * 
 */
public class RemoteReceiver extends SingleInputNode {

    List<Address<? extends Receiver>> targets;

    public RemoteReceiver(ReteContainer reteContainer) {
        super(reteContainer);
        targets = new ArrayList<Address<? extends Receiver>>();
    }

    public void addTarget(Address<? extends Receiver> target) {
        targets.add(target);
    }

    @Override
    public void pullInto(Collection<Tuple> collector, boolean flush) {
        propagatePullInto(collector, flush);
    }
    
    @Override
    public void pullIntoWithTimestamp(Map<Tuple, DifferentialTimestamp> collector, boolean flush) {
        throw new UnsupportedOperationException();
    }

    public Collection<Tuple> remotePull(boolean flush) {
        return reteContainer.pullContents(this, flush);
    }

    public void update(Direction direction, Tuple updateElement, DifferentialTimestamp timestamp) {
        for (Address<? extends Receiver> ad : targets)
            reteContainer.sendUpdateToRemoteAddress(ad, direction, updateElement);
    }

}
