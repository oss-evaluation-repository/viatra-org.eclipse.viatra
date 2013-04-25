/*******************************************************************************
 * Copyright (c) 2010-2013, Andras Okros, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Andras Okros - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.patternregistry;

public interface IPatternRegistryListener {

    public void patternAdded(IPatternInfo patternInfo);

    public void patternRemoved(IPatternInfo patternInfo);

    public void patternActivated(IPatternInfo patternInfo);

    public void patternDeactivated(IPatternInfo patternInfo);

}
