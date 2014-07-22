/**
 *
 * $Id$
 */
package org.eclipse.viatra.cep.core.metamodels.automaton.validation;

import org.eclipse.emf.common.util.EList;

import org.eclipse.viatra.cep.core.metamodels.automaton.Automaton;
import org.eclipse.viatra.cep.core.metamodels.automaton.EventContext;

import org.eclipse.viatra.cep.core.metamodels.events.Event;

/**
 * A sample validator interface for {@link org.eclipse.viatra.cep.core.metamodels.automaton.InternalModel}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface InternalModelValidator {
    boolean validate();

    boolean validateAutomata(EList<Automaton> value);
    boolean validateLatestEvent(Event value);
    boolean validateContext(EventContext value);
}
