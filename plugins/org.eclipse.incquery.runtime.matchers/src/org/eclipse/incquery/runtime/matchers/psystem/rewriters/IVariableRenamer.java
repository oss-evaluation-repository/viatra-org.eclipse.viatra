/*******************************************************************************
 * Copyright (c) 2010-2015, stampie, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   stampie - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.matchers.psystem.rewriters;

import org.eclipse.incquery.runtime.matchers.psystem.PVariable;
import org.eclipse.incquery.runtime.matchers.psystem.queries.PQuery;

/**
 * Helper interface to ease the naming of the new variables during flattening
 * 
 * @author Marton Bur
 * 
 */
public interface IVariableRenamer {
    /**
     * Creates a variable name based on a given variable and a given query. It only creates a String, doesn't set
     * anything.
     * 
     * @param pVariable
     * @param query
     * @return the new variable name as a String
     */
    String createVariableName(PVariable pVariable, PQuery query);
    
    public class SameName implements IVariableRenamer {
        @Override
        public String createVariableName(PVariable pVariable, PQuery query) {
            return pVariable.getName();
        }
    }
    
    public class HierarchicalName implements IVariableRenamer {
        @Override
        public String createVariableName(PVariable pVariable, PQuery query) {
            return getShortName(query) + "_" + pVariable.getName();
        }
        
        private String getShortName(PQuery query) {
            String fullyQualifiedName = query.getFullyQualifiedName();
            int beginIndex = fullyQualifiedName.lastIndexOf(".") + 1;
            return fullyQualifiedName.substring(beginIndex);
        }
    }
}