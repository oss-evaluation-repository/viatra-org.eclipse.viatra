/** 
 * Copyright (c) 2010-2016, Abel Hegedus, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Abel Hegedus - initial API and implementation
 */
package org.eclipse.viatra.query.tooling.ui.queryresult.properties

import com.google.common.collect.Lists
import java.util.List
import org.eclipse.ui.views.properties.IPropertyDescriptor
import org.eclipse.ui.views.properties.IPropertySource
import org.eclipse.ui.views.properties.PropertyDescriptor
import org.eclipse.viatra.query.runtime.matchers.backend.QueryEvaluationHint

/** 
 * @author Abel Hegedus
 */
class HintsPropertySource implements IPropertySource {
    final QueryEvaluationHint hint

    new(QueryEvaluationHint hint) {
        this.hint = hint
    }

    override Object getEditableValue() {
        return this
    }
    
    override toString() {
        if(hint.backendHints.empty){
            return "No hints specified"
        }
        return ""
    }

    override IPropertyDescriptor[] getPropertyDescriptors() {
        val String category = "Hints"
        val List<IPropertyDescriptor> hints = Lists.newArrayList()
        hint.backendHints.keySet.forEach[ key |
            val PropertyDescriptor property = new PropertyDescriptor(key, key)
            property.setCategory(category)
            hints.add(property)
        ]
        return hints.toArray(newArrayOfSize(0))
    }

    override Object getPropertyValue(Object id) {
        val hintValue = hint.backendHints.get(id)
        return hintValue
    }

    override boolean isPropertySet(Object id) {
        return false
    }

    override void resetPropertyValue(Object id) {
    }

    override void setPropertyValue(Object id, Object value) {
    }
}
