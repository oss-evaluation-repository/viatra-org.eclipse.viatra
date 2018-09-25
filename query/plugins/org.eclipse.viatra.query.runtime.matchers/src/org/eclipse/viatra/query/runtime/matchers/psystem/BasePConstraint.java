/*******************************************************************************
 * Copyright (c) 2004-2010 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra.query.runtime.matchers.psystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.viatra.query.runtime.matchers.context.IQueryMetaContext;

/**
 * @author Gabor Bergmann
 * 
 */
public abstract class BasePConstraint implements PConstraint {
    
    
    protected PBody pBody;
    private final Set<PVariable> affectedVariables;
    
    
    private final int sequentialID = nextID++;

    private static int nextID = 0;

    public BasePConstraint(PBody pBody, Set<PVariable> affectedVariables) {
        super();
        this.pBody = pBody;
        this.affectedVariables = new HashSet<PVariable>(affectedVariables);

        for (PVariable pVariable : affectedVariables) {
            pVariable.refer(this);
        }
        pBody.registerConstraint(this);
    }

    @Override
    public String toString() {
        return "PC[" + getClass().getSimpleName() + ":" + toStringRest() + "]";
    }

    protected abstract String toStringRest();

    @Override
    public Set<PVariable> getAffectedVariables() {
        return affectedVariables;
    }
    
    @Override
    public Map<Set<PVariable>, Set<PVariable>> getFunctionalDependencies(IQueryMetaContext context) {
        return Collections.emptyMap();
    }

    @Override
    public void replaceVariable(PVariable obsolete, PVariable replacement) {
        pBody.checkMutability();
        if (affectedVariables.remove(obsolete)) {
            affectedVariables.add(replacement);
            obsolete.unrefer(this);
            replacement.refer(this);
            doReplaceVariable(obsolete, replacement);
        }
    }

    protected abstract void doReplaceVariable(PVariable obsolete, PVariable replacement);

    @Override
    public void delete() {
        pBody.checkMutability();
        for (PVariable pVariable : affectedVariables) {
            pVariable.unrefer(this);
        }
        pBody.unregisterConstraint(this);
    }

    @Override
    public void checkSanity() {
    }

    /**
     * For backwards compatibility. Equivalent to {@link #getBody()}
     */
    public PBody getPSystem() {
        return pBody;
    }
    /**
     * @since 2.1
     */
    @Override
    public PBody getBody() {
        return pBody;
    }
    
    @Override
    public int getMonotonousID() {
        return sequentialID;
    }    
}
