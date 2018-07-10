/*******************************************************************************
 * Copyright (c) 2010-2018, Adam Lengyel, Zoltan Ujhelyi, IncQuery Labs Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Adam Lengyel, Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.query.patternlanguage.emf.sirius.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.diagram.AbstractDNode;
import org.eclipse.sirius.diagram.DDiagramElement;
import org.eclipse.sirius.diagram.DEdge;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.diagram.ui.part.SiriusDiagramEditor;
import org.eclipse.sirius.diagram.ui.tools.api.part.IDiagramDialectGraphicalViewer;
import org.eclipse.sirius.ui.business.api.session.IEditingSession;
import org.eclipse.sirius.ui.business.api.session.SessionUIManager;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.viatra.query.patternlanguage.emf.sirius.SiriusVQLGraphicalEditorPlugin;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.BooleanLiteral;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.EClassifierReference;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.EnumValue;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.Expression;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.InterpretableExpression;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.JavaClassReference;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.Literal;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.LocalVariable;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.NumberLiteral;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.Parameter;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.ParameterRef;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.Reference;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.ReferenceType;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.StringLiteral;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.Type;
import org.eclipse.viatra.query.patternlanguage.metamodel.vgql.VgqlFactory;

public class VGQLEditorUtil {
	
	/**
	 * Creates a {@link Reference} instance for the given variable
	 * 
	 * <p><b>The method is called from Sirius as a Java service!</b></p>
	 */
	public static Reference createReference(Expression ex) {
		Reference result = VgqlFactory.eINSTANCE.createReference();
		result.setExpression(ex);
		result.setAggregator(false);
		return result;
	}
	
	/**
     * This method removes the given {@link Parameter} element from the model.
     *  
     *  <p>
     *      During this operation the affected {@link ParameterRef} instances (parameter
     *      references with the same name than the removed parameter has) in the 
     *      pattern bodies have to be replaced with appropriate {@link LocalVariable}
     *      instances in order to get a valid model. This operation requires some tricks
     *      affecting the views of the replaced {@link ParameterRef} instances in order
     *      to preserve them from the 'recreation' by the Sirius (GMF). For more details
     *      see the implementation.
     *  </p>
     * 
     *  <p><b>The method is called from Sirius as a Java service!</b></p>
     * 
     * @param parameter The {@link Parameter} instance to remove
     * @param containerView The view (in the Sirius diagram) of the container element of the parameter
     */
    public static void removeParameter(Parameter parameter, DDiagramElement containerView) {
        for (ParameterRef pr : parameter.getParameterReferences()) {
            // Create new LocalVariable instance
            LocalVariable lv = VgqlFactory.eINSTANCE.createLocalVariable();
            lv.setName(pr.getName());
            pr.getTypes().stream().map(EcoreUtil::copy).forEach(t -> pr.getTypes().add(t));
            
            // Add new LocalVariable instance to the current PatternBody
            pr.getBody().getNodes().add(lv);
            
            // Modify the Constraints to point at the new LocalVariable instance
            for (Reference vr : new ArrayList<>(pr.getReferences())) {
                vr.setExpression(lv);
                lv.getReferences().add(vr);
            }
            
            /**
             * Find view (AbstractDNode element) for the current PatternRef instance
             *  (if everything is fine only one view exists for the given ParameterReference
             *  instance in a body's view (containerView)), and set the target semantic
             *  element to the new LocalVariable instance. This ensures that the diagram
             *  (e.g. the position of the variable element) won't change during parameter
             *  deletion.
             */
            Set<DDiagramElement> diagramElements = findViewsByTarget(containerView, pr);
            if (diagramElements.size() == 1) {
                handleTargetSemanticElementChange(diagramElements.iterator().next(), lv);
            } else {
                SiriusVQLGraphicalEditorPlugin.logError(new IllegalStateException("Not exactly one view exists"
                        + " for the examined parameter reference on the diagram!"));
            }
            
            // Delete current ParameterRef instance from the model
            EcoreUtil.delete(pr);
            
        }
        
        // Delete parameter from the model
        EcoreUtil.delete(parameter);
    }
    
    /**
     * This method creates a label for {@link Literal} elements.
     * 
     *  <p><b>The method is called from Sirius as a Java service!</b></p>
     */ 
    public static String getLiteralValueReferenceLabel(Literal lvr) {
        if (lvr instanceof NumberLiteral) {
            return ((NumberLiteral) lvr).getValue();
        } else if (lvr instanceof StringLiteral) {
            return ((StringLiteral) lvr).getValue();
        } else if (lvr instanceof BooleanLiteral) {
            return Boolean.toString(((BooleanLiteral) lvr).isValue());
        } else if (lvr instanceof EnumValue) {
            return ((EnumValue) lvr).getLiteral().getName();
        }
        
        return "";
    }
    
    /**
     * This helper method calculates label for the given {@link Type} instance based
     *  on its concrete type ({@link EClassifierReference} / {@link ReferenceType} / etc.).
     *  
     *  <p><b>The method is called from Sirius as a Java service!</b></p>
     */
    public static String getTypeLabel(Type type) {
        if (type instanceof EClassifierReference) {
            return ((EClassifierReference) type).getClassifier().getName();
        } else if (type instanceof ReferenceType) {
            return ((ReferenceType) type).getRefname().getEType().getName();
        } else if (type instanceof JavaClassReference) {
            final String fqn = ((JavaClassReference) type).getClassName();
            return fqn == null ? "«UNDEFINED»" : fqn.substring(fqn.lastIndexOf(".") + 1);
        } else {
            return "Unknown type";
        }
    }
    
    /**
     * This method is responsible for opening the embedded Xtext editor on the Sirius
     *  diagram for the given {@code target} element.
     * 
     *  <p><b>The method is called from Sirius as a Java service!</b></p>
     * 
     * @param target The target element for which the embedded editor will be opened
     * @param targetViewEObject The view (in the Sirius diagram) of the target element
     */
    public static void openXtextEmbeddedEditor(InterpretableExpression target, EObject targetViewEObject) {
        if (false == targetViewEObject instanceof DDiagramElement) {
            SiriusVQLGraphicalEditorPlugin.logError(new IllegalArgumentException(
                    "The 'targetView' parameter must be a DDiagramElement instance!"));
        }
        
        IEditorPart editor = Optional.ofNullable(PlatformUI.getWorkbench()).map(IWorkbench::getActiveWorkbenchWindow)
                .map(IWorkbenchWindow::getActivePage).map(IWorkbenchPage::getActiveEditor).orElseThrow(null);
        if (false == editor instanceof SiriusDiagramEditor) {
            SiriusVQLGraphicalEditorPlugin.logError(new IllegalStateException("The currently"
                    + " active editor is not a SiriusDiagramEditor instance!"));
            return;
        }
        
        DDiagramElement targetView = (DDiagramElement) targetViewEObject;
        SiriusDiagramEditor siriusDiagramEditor = (SiriusDiagramEditor) editor;
        DiagramEditPart diagramEditPart = siriusDiagramEditor.getDiagramEditPart();
        
        EditPart editPart = null;
        if (targetView instanceof DEdge) {
            editPart = diagramEditPart.findEditPart(diagramEditPart, ((DEdge) targetView).getSourceNode());
        } else {
            editPart = diagramEditPart.findEditPart(diagramEditPart, targetView);
        }
        if (editPart == null || (false == editPart instanceof IGraphicalEditPart)) {
            SiriusVQLGraphicalEditorPlugin.logError(new IllegalStateException("Can not find"
                    + " appropriate edit part for the target element (" + target + ")"));
            return;
        }
         
        XtextEmbeddedEditor embeddedEditor = new XtextEmbeddedEditor((IGraphicalEditPart) editPart, target, targetView);
        embeddedEditor.showEditor();
    }
    
    /**
     * This method finds the {@link DDiagramElement} instances in a container view with the given target value.
     * 
     * @param containerView The container view in which the diagram element is looked for
     * @param target The semantic element, which has to be set on the <code>target</code> reference
     *  of the diagram element
     * @return The diagram elements in the given container view with the given semantic element on their
     *  {@code target} reference
     */
    private static Set<DDiagramElement> findViewsByTarget(DDiagramElement containerView, EObject target) {
        Set<DDiagramElement> result = new HashSet<>();
        
        EObject element = null;
        Iterator<EObject> it = containerView.eAllContents();
        while (it.hasNext()) {
            element = it.next();
            if ((element instanceof DDiagramElement)
                    && isDiagramElementForTarget((DDiagramElement) element, target)) {
                result.add((DDiagramElement) element);
            }
        }

        return result;
    }
    
    /**
     * This method returns the {@link IDiagramDialectGraphicalViewer} instance belongs to the
     *  given diagram element, or <code>null</code> if it can not be found.
     * 
     * @param diagramElement The element for which the viewer is looked for
     * @return The {@link IDiagramDialectGraphicalViewer} instance belongs to the
     *  given diagram element, or <code>null</code> if it can not be found
     */
    public static IDiagramDialectGraphicalViewer getViewer(DDiagramElement diagramElement) {
        DRepresentation representation = diagramElement.getParentDiagram();
        Session session = SessionManager.INSTANCE.getSession(((DSemanticDiagram) representation).getTarget());
        IEditingSession editingSession = SessionUIManager.INSTANCE.getUISession(session);
        SiriusDiagramEditor editor = (SiriusDiagramEditor) editingSession.getEditor(representation);
        
        return (IDiagramDialectGraphicalViewer) editor.getDiagramGraphicalViewer();
    }
    
    /**
     * This method can be useful, when a {@link DDiagramElement}'s {@code target} reference change,
     *  and one wants to preserve the view belongs to the diagram element. This is useful only if the new
     *  target semantic element is compatible with the {@code Domain Class} set in the mapping of
     *  the diagram element.
     * 
     * @param diagramElement The affected element of the Sirius diagram
     * @param newValue The new value for the diagram element's target reference
     */
    private static void handleTargetSemanticElementChange(DDiagramElement diagramElement, EObject newValue) {
        EObject oldValue = diagramElement.getTarget();
        
        // Find the Viewer for the Sirius diagram
        IDiagramDialectGraphicalViewer viewer = getViewer(diagramElement);
        
        // Get EditParts for the current semantic element
        List<IGraphicalEditPart> editParts = viewer.findEditPartsForElement(diagramElement.getTarget(), IGraphicalEditPart.class);

        // Unregister EditParts for the current semantic element
        for (IGraphicalEditPart editPart : editParts) {
            viewer.unregisterEditPartForSemanticElement(oldValue, editPart);
        }
        
        // Set the new semantic element
        diagramElement.setTarget(newValue);
        
        // Register EditParts for the new semantic element
        for (IGraphicalEditPart editPart : editParts) {
            viewer.registerEditPartForSemanticElement(newValue, editPart);
        }
    }
    
    /**
     * This method is responsible for deciding if the diagram element is the view of the
     *  given target semantic element. 
     * 
     * @param diagramElement The examined view
     * @param target The semantic element for which the view is looked for
     */
    private static boolean isDiagramElementForTarget(DDiagramElement diagramElement, EObject target) {
        if (diagramElement instanceof AbstractDNode) {
            return (diagramElement.getTarget() != null
                    && diagramElement.getTarget().equals(target));
        } else if (diagramElement instanceof DEdge) {
            DEdge edge = (DEdge) diagramElement;
            
            return (isDiagramElementForTarget((DDiagramElement) edge.getSourceNode(), target)
                    || (isDiagramElementForTarget((DDiagramElement) edge.getTargetNode(), target))
                    || (edge.getTarget() != null && edge.getTarget().equals(target)));
        }
        
        return false;
    }
}
