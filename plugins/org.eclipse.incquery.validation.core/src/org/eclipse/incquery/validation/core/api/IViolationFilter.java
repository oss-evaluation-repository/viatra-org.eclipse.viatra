/*******************************************************************************
 * Copyright (c) 2010-2014, Balint Lorand, Zoltan Ujhelyi, Abel Hegedus, Tamas Szabo, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi, Abel Hegedus, Tamas Szabo - original initial API and implementation
 *   Balint Lorand - revised API and implementation
 *******************************************************************************/

package org.eclipse.incquery.validation.core.api;

/**
 * Interface for filtering violations when retrieving them from a constraint or registering for event notifications.
 * 
 * @author Balint Lorand
 *
 */
public interface IViolationFilter {

    /**
     * Checks the given violation object whether it passes through the filter.
     * 
     * @param violation
     *            The violation to be checked.
     * @return <code>true</code> if the violation passes the filter.
     */
    boolean apply(IViolation violation);

}
