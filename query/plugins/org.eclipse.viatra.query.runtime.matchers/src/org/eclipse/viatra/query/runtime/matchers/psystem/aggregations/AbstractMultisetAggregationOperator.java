/*******************************************************************************
 * Copyright (c) 2010-2016, Gabor Bergmann, IncQueryLabs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Gabor Bergmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.matchers.psystem.aggregations;

import java.util.Collection;

/**
 * Base class for aggregation operators.
 * @author Gabor Bergmann
 * @since 1.4
 */
public abstract class AbstractMultisetAggregationOperator<Domain, Accumulator, AggregateResult>
        implements IMultisetAggregationOperator<Domain, Accumulator, AggregateResult> {

}
