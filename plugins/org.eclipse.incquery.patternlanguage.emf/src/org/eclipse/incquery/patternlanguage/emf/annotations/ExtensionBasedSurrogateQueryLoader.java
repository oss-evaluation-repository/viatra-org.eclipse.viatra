/*******************************************************************************
 * Copyright (c) 2010-2015, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.patternlanguage.emf.annotations;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.incquery.runtime.matchers.context.surrogate.SurrogateQueryRegistry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Abel Hegedus
 *
 */
public class ExtensionBasedSurrogateQueryLoader {

    private static final String DUPLICATE_SURROGATE_QUERY = "Duplicate surrogate query definition %s for feature %s of EClass %s in package %s (FQN in map %s, contributing plug-ins %s, plug-in %s)";
    static final String EXTENSIONID = "org.eclipse.incquery.patternlanguage.emf.surrogatequeryemf";

    private Multimap<String, String> contributingPluginOfFeatureMap = HashMultimap.create();
    private Map<EStructuralFeature, String> contributedSurrogateQueries;

    private static final ExtensionBasedSurrogateQueryLoader instance = new ExtensionBasedSurrogateQueryLoader();

    public static ExtensionBasedSurrogateQueryLoader instance() {
        return instance;
    }

    public void loadKnownSurrogateQueriesIntoRegistry() {
        Map<EStructuralFeature, String> knownSurrogateQueryFQNs = getKnownSurrogateQueryFQNs();
        for (Entry<EStructuralFeature, String> entry : knownSurrogateQueryFQNs.entrySet()) {
            SurrogateQueryRegistry.instance().registerSurrogateQueryForFeature(entry.getKey(), entry.getValue());
        }
    }
    
    public Map<EStructuralFeature, String> getKnownSurrogateQueryFQNs() {
        if(contributedSurrogateQueries != null) {
            return contributedSurrogateQueries;
        }
        contributedSurrogateQueries = Maps.newHashMap();
        if (Platform.isRunning()) {

            final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    EXTENSIONID);
            for (IConfigurationElement e : config) {
                processWellbehavingExtension(e);
            }
        }
        return contributedSurrogateQueries;
    }

    /**
     * @param el
     */
    private void processWellbehavingExtension(IConfigurationElement el) {
        
        try {
            String packageUri = el.getAttribute("package-nsUri");
            String className = el.getAttribute("class-name");
            String featureName = el.getAttribute("feature-name");
            String surrogateQueryFQN = el.getAttribute("surrogate-query-fqn");
            String contributorName = el.getContributor().getName();
            StringBuilder featureIdBuilder = new StringBuilder();
            checkState(packageUri != null, "Package NsURI cannot be null in extension");
            checkState(className != null, "Class name cannot be null in extension");
            checkState(featureName != null, "Feature name cannot be null in extension");
            checkState(surrogateQueryFQN != null, "Query FQN cannot be null in extension");
            
            EPackage pckg = EPackage.Registry.INSTANCE.getEPackage(packageUri);
            featureIdBuilder.append(packageUri);
            checkState(pckg != null, "Package %s not found! (plug-in %s)", packageUri, contributorName);
            
            EClassifier clsr = pckg.getEClassifier(className);
            featureIdBuilder.append("##").append(className);
            checkState(clsr instanceof EClass, "EClassifier %s does not exist in package %s! (plug-in %s)", className, packageUri, contributorName);
            
            EClass cls = (EClass) clsr;
            EStructuralFeature feature = cls.getEStructuralFeature(featureName);
            featureIdBuilder.append("##").append(featureName);
            checkState(feature != null, "Feature %s of EClass %s in package %s not found! (plug-in %s)", featureName, className, packageUri, contributorName);
            String fqnInMap = contributedSurrogateQueries.get(feature);
            if(fqnInMap != null) {
                String duplicateSurrogateFormatString = DUPLICATE_SURROGATE_QUERY;
                Collection<String> contributorPlugins = contributingPluginOfFeatureMap.get(featureIdBuilder.toString());
                String duplicateSurrogateMessage = String.format(duplicateSurrogateFormatString, surrogateQueryFQN, featureName, className, packageUri, fqnInMap, contributorPlugins, contributorName);
                throw new IllegalStateException(duplicateSurrogateMessage);
            }
            contributedSurrogateQueries.put(feature, surrogateQueryFQN);
            contributingPluginOfFeatureMap.put(featureIdBuilder.toString(), contributorName);
        } catch (Exception e) {
            final Logger logger = Logger.getLogger(SurrogateQueryRegistry.class);
            logger.error("Surrogate query registration failed", e);
        }
    }
}
