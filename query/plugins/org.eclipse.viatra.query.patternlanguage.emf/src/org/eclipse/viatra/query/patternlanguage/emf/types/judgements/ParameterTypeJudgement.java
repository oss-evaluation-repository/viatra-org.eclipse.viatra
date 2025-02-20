/*******************************************************************************
 * Copyright (c) 2010-2016, Zoltan Ujhelyi, IncQuery Labs Ltd.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.query.patternlanguage.emf.types.judgements;

import java.util.Set;

import org.eclipse.viatra.query.patternlanguage.emf.vql.Expression;
import org.eclipse.viatra.query.patternlanguage.emf.vql.Variable;

import com.google.common.collect.ImmutableSet;

/**
 * @author Zoltan Ujhelyi
 * @since 1.3
 *
 */
public class ParameterTypeJudgement extends TypeConformJudgement {

    /**
     * @since 1.4
     */
    public ParameterTypeJudgement(Expression expression, Variable conformsTo) {
        super(expression, conformsTo);
    }
    
    @Override
    public Set<Expression> getDependingExpressions() {
        return ImmutableSet.of();
    }

}
