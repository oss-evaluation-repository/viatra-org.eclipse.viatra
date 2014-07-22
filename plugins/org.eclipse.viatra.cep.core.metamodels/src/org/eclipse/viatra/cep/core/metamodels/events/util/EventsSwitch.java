/**
 */
package org.eclipse.viatra.cep.core.metamodels.events.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.viatra.cep.core.metamodels.events.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.viatra.cep.core.metamodels.events.EventsPackage
 * @generated
 */
public class EventsSwitch<T> extends Switch<T> {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static EventsPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EventsSwitch() {
        if (modelPackage == null) {
            modelPackage = EventsPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @parameter ePackage the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case EventsPackage.EVENT_PATTERN: {
                EventPattern eventPattern = (EventPattern)theEObject;
                T result = caseEventPattern(eventPattern);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.ATOMIC_EVENT_PATTERN: {
                AtomicEventPattern atomicEventPattern = (AtomicEventPattern)theEObject;
                T result = caseAtomicEventPattern(atomicEventPattern);
                if (result == null) result = caseEventPattern(atomicEventPattern);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.COMPLEX_EVENT_PATTERN: {
                ComplexEventPattern complexEventPattern = (ComplexEventPattern)theEObject;
                T result = caseComplexEventPattern(complexEventPattern);
                if (result == null) result = caseEventPattern(complexEventPattern);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.EVENT: {
                Event event = (Event)theEObject;
                T result = caseEvent(event);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.EVENT_SOURCE: {
                EventSource eventSource = (EventSource)theEObject;
                T result = caseEventSource(eventSource);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.COMPLEX_EVENT_OPERATOR: {
                ComplexEventOperator complexEventOperator = (ComplexEventOperator)theEObject;
                T result = caseComplexEventOperator(complexEventOperator);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.OR: {
                OR or = (OR)theEObject;
                T result = caseOR(or);
                if (result == null) result = caseComplexEventOperator(or);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.NEG: {
                NEG neg = (NEG)theEObject;
                T result = caseNEG(neg);
                if (result == null) result = caseComplexEventOperator(neg);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.FOLLOWS: {
                FOLLOWS follows = (FOLLOWS)theEObject;
                T result = caseFOLLOWS(follows);
                if (result == null) result = caseComplexEventOperator(follows);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.UNTIL: {
                UNTIL until = (UNTIL)theEObject;
                T result = caseUNTIL(until);
                if (result == null) result = caseComplexEventOperator(until);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.AND: {
                AND and = (AND)theEObject;
                T result = caseAND(and);
                if (result == null) result = caseComplexEventOperator(and);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case EventsPackage.TIME_WINDOW: {
                TimeWindow timeWindow = (TimeWindow)theEObject;
                T result = caseTimeWindow(timeWindow);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Event Pattern</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Event Pattern</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseEventPattern(EventPattern object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Atomic Event Pattern</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Atomic Event Pattern</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAtomicEventPattern(AtomicEventPattern object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Complex Event Pattern</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Complex Event Pattern</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseComplexEventPattern(ComplexEventPattern object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Event</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Event</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseEvent(Event object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Event Source</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Event Source</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseEventSource(EventSource object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Complex Event Operator</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Complex Event Operator</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseComplexEventOperator(ComplexEventOperator object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>OR</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>OR</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseOR(OR object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>NEG</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>NEG</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseNEG(NEG object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>FOLLOWS</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>FOLLOWS</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseFOLLOWS(FOLLOWS object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>UNTIL</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>UNTIL</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseUNTIL(UNTIL object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>AND</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>AND</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseAND(AND object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>Time Window</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Time Window</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseTimeWindow(TimeWindow object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} //EventsSwitch
