/**
 *
 * $Id$
 */
package org.eclipse.viatra.cep.core.metamodels.events.validation;

import org.eclipse.viatra.cep.core.metamodels.events.EventSource;

/**
 * A sample validator interface for {@link org.eclipse.viatra.cep.core.metamodels.events.Event}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface EventValidator {
    boolean validate();

    boolean validateType(String value);
    boolean validateTimestamp(long value);
    boolean validateSource(EventSource value);
}
