/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.query.patternlanguage.emf.annotations;

import java.util.Optional;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.viatra.query.patternlanguage.emf.helper.PatternLanguageHelper;
import org.eclipse.viatra.query.patternlanguage.emf.types.ITypeInferrer;
import org.eclipse.viatra.query.patternlanguage.emf.validation.IIssueCallback;
import org.eclipse.viatra.query.patternlanguage.emf.vql.Expression;
import org.eclipse.viatra.query.patternlanguage.emf.vql.Pattern;
import org.eclipse.viatra.query.patternlanguage.emf.vql.PatternLanguagePackage;
import org.eclipse.viatra.query.patternlanguage.emf.vql.ValueReference;
import org.eclipse.viatra.query.patternlanguage.emf.vql.Variable;
import org.eclipse.viatra.query.runtime.emf.types.EClassTransitiveInstancesKey;
import org.eclipse.viatra.query.runtime.emf.types.EDataTypeInSlotsKey;
import org.eclipse.viatra.query.runtime.matchers.context.IInputKey;
import org.eclipse.viatra.query.runtime.matchers.context.common.JavaTransitiveInstancesKey;

import com.google.inject.Inject;

/**
 * @author Zoltan Ujhelyi
 *
 */
public class AnnotationExpressionValidator {

    private static final String VALIDATOR_BASE_CODE = "org.eclipse.viatra.query.patternlanguage.expression.";
    public static final String GENERAL_ISSUE_CODE = VALIDATOR_BASE_CODE + "general";
    public static final String UNKNOWN_VARIABLE_CODE = VALIDATOR_BASE_CODE + "unknown_variable";
    public static final String UNKNOWN_ATTRIBUTE_CODE = VALIDATOR_BASE_CODE + "unknown_attribute";
    public static final String UNDEFINED_NAME_CODE = VALIDATOR_BASE_CODE + "undefined_name";

    @Inject
    private ITypeInferrer typeInferrer;
    

    /**
     * Validates a path expression referring to a simple pattern parameter
     * 
     * @param expression
     *            the string representation of the path expression. Not inside '$' symbols.
     * @param pattern
     *            the containing pattern
     * @param ref
     *            a reference for the annotation parameter for error localization
     * @param validator
     *            the validator to report the found issues
     * @since 2.0
     */
    public void validateParameterString(String expression, Pattern pattern, ValueReference ref, IIssueCallback validator) {
        if (expression.contains(".")) {
            validator.error("Expression must refer to a single parameter.", ref,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, GENERAL_ISSUE_CODE);
        }

        Optional<Variable> parameter = PatternLanguageHelper.getParameterByName(pattern, expression);
        if (!parameter.isPresent()) {
            validator.error(String.format("Unknown parameter name %s", expression), ref,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, UNKNOWN_VARIABLE_CODE);
            return;
        }
    }

    /**
     * Validates a path expression starting with a parameter of the pattern.
     * 
     * @param expression
     *            the string representation of the path expression. Not inside '$' symbols.
     * @param pattern
     *            the containing pattern
     * @param ref
     *            a reference for the annotation parameter for error localization
     * @param validator
     *            the validator to report the found issues
     * @since 2.0
     */
    public void validateModelExpression(String expression, Pattern pattern, ValueReference ref, IIssueCallback validator) {
        String[] tokens = expression.split("\\.");
        if (expression.isEmpty() || tokens.length == 0) {
            validator.error("Expression must not be empty.", ref, PatternLanguagePackage.Literals.STRING_VALUE__VALUE,
                    GENERAL_ISSUE_CODE);
            return;
        }

        Optional<Variable> parameter = PatternLanguageHelper.getParameterByName(pattern, tokens[0]);
        if (!parameter.isPresent()) {
            validator.error(String.format("Unknown parameter name %s", tokens[0]), ref,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, UNKNOWN_VARIABLE_CODE);
            return;
        }
        
        

        IInputKey type = typeInferrer.getType(parameter.get());
        if (type == null) {
            // Parameter type errors are reported in called method
        } else if (type instanceof JavaTransitiveInstancesKey) {
            Class<?> clazz = ((JavaTransitiveInstancesKey) type).getInstanceClass();
            validateJavaFeatureAccess(clazz, tokens, ref, validator);
        } else if (type instanceof EClassTransitiveInstancesKey) {
            EClassifier classifier = ((EClassTransitiveInstancesKey) type).getEmfKey();
            validateClassifierFeatureAccess(classifier, tokens, ref, validator);
        } else if (type instanceof EDataTypeInSlotsKey){
            EClassifier classifier = ((EDataTypeInSlotsKey) type).getEmfKey();
            validateClassifierFeatureAccess(classifier, tokens, ref, validator);
        } else {
            validator.error(String.format("Label expressions only supported on EMF types, not on %s", type.getPrettyPrintableName()), parameter.get(),
                    PatternLanguagePackage.Literals.VARIABLE__NAME, GENERAL_ISSUE_CODE);
        }
        
    }

    private void validateJavaFeatureAccess(Class<?> clazz, String[] nameTokens, Expression context,
            IIssueCallback validator) {
        if (nameTokens.length != 1) {
            validator.error("For Java objects parameters no feature access is supported.", context,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, GENERAL_ISSUE_CODE);
        }
    }
    
    private void validateClassifierFeatureAccess(EClassifier classifier, String[] nameTokens, Expression context,
            IIssueCallback validator) {
        if (nameTokens.length == 1) {
            checkClassifierFeature(classifier, "name", context, validator, false);
        } else if (nameTokens.length == 2) {
            String featureName = nameTokens[1];
            checkClassifierFeature(classifier, featureName, context, validator, true);
        } else {
            validator.error("Only direct feature references are supported.", context,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, GENERAL_ISSUE_CODE);
        }
    }
    
    /**
     * Checks whether an {@link EClassifier} defines a feature with the selected name; if not, reports an error for the
     * selected reference.
     */
    private void checkClassifierFeature(EClassifier classifier, String featureName, Expression ref,
            IIssueCallback validator, boolean userSpecified) {
        if (classifier instanceof EClass) {
            EClass classDef = (EClass) classifier;
            if (classDef.getEStructuralFeature(featureName) == null) {
                if (userSpecified) {
                validator.error(
                        String.format("Invalid feature type %s in EClass %s", featureName, classifier.getName()),
                        ref, PatternLanguagePackage.Literals.STRING_VALUE__VALUE, UNKNOWN_ATTRIBUTE_CODE);
                } else {
                    validator.warning(String.format(
                                            "EClass %s does not define a name attribute, so the string representation might be inconvinient to use. Perhaps a feature qualifier is missing?",
                            classifier.getName()), ref, PatternLanguagePackage.Literals.STRING_VALUE__VALUE,
                            UNKNOWN_ATTRIBUTE_CODE);
                }
            }
        } else if (classifier == null) {
            return;
        }
    }


    /**
     * Validates a string expression that may contain model references escaped inside '$' symbols.
     * 
     * @param expression
     * @param pattern
     *            the containing pattern
     * @param ref
     *            a reference for the annotation parameter for error localization
     * @param validator
     *            the validator to report the found issues
     * @since 2.0
     */
    public void validateStringExpression(String expression, Pattern pattern, ValueReference ref,
            IIssueCallback validator) {
        StringTokenizer tokenizer = new StringTokenizer(expression, "$", true);
        if (expression.isEmpty() || tokenizer.countTokens() == 0) {
            validator.error("Expression must not be empty.", ref, PatternLanguagePackage.Literals.STRING_VALUE__VALUE,
                    GENERAL_ISSUE_CODE);
            return;
        }
        boolean inExpression = false;
        boolean foundToken = false;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("$")) {
                if (inExpression && !foundToken) {
                    validator.error("Empty reference ($$) in message is not allowed.", ref,
                            PatternLanguagePackage.Literals.STRING_VALUE__VALUE, GENERAL_ISSUE_CODE);
                }
                inExpression = !inExpression;
            } else if (inExpression) {
                validateModelExpression(token, pattern, ref, validator);
                foundToken = true;
            }
        }

            if (inExpression) {
            validator.error("Inconsistent model references - a $ character is missing.", ref,
                    PatternLanguagePackage.Literals.STRING_VALUE__VALUE, GENERAL_ISSUE_CODE);
        }
    }
}
