/*******************************************************************************
 * Copyright (c) 2014-2016 IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Akos Horvath, Abel Hegedus, Akos Menyhert, Tamas Borbas, Marton Bur, Zoltan Ujhelyi, Daniel Segesdi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.runtime.cps.tests

import org.eclipse.viatra.query.runtime.cps.tests.queries.util.MandatoryRequirementsQuerySpecification
import org.eclipse.viatra.query.runtime.cps.tests.queries.util.OptionalRequirementsQuerySpecification
import org.eclipse.viatra.query.runtime.cps.tests.queries.util.RunningAppInstancesQuerySpecification
import org.eclipse.viatra.query.testing.core.api.ViatraQueryTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized)
class LiteralValuesTest extends AbstractQueryComparisonTest {
    
    override getSnapshotUri() {
        "org.eclipse.viatra.query.runtime.cps.tests/models/snapshots/test_literalUsage.snapshot"
    }
    
    @Test
    def void booleanFalseTest() {
        ViatraQueryTest.test(OptionalRequirementsQuerySpecification.instance)
                        .on(scope)
                        .with(snapshot)
                        .with(type.hints)
                        .assertEquals
    }
    @Test
    def void booleanTrueTest() {
        ViatraQueryTest.test(MandatoryRequirementsQuerySpecification.instance)
                        .on(scope)
                        .with(snapshot)
                        .with(type.hints)
                        .assertEquals
    }
    @Test
    def void enumLiteralTest() {
        ViatraQueryTest.test(RunningAppInstancesQuerySpecification.instance)
                        .on(scope)
                        .with(snapshot)
                        .with(type.hints)
                        .assertEquals
    }
}
