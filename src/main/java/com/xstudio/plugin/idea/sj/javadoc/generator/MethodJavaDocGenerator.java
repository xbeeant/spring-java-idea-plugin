package com.xstudio.plugin.idea.sj.javadoc.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.xstudio.plugin.idea.sj.javadoc.entity.JavaDoc;
import com.xstudio.plugin.idea.sj.translate.BiyingTranslate;
import com.xstudio.plugin.idea.sj.util.JavaBeansUtil;
import com.xstudio.plugin.idea.sj.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodJavaDocGenerator extends AbstractJavaDocGenerator<PsiMethod> {
    public MethodJavaDocGenerator(Project project) {
        super(project);
    }

    @Nullable
    @Override
    public PsiDocComment generate(PsiClass psiClass, @NotNull PsiMethod element) {
        JavaDoc javaDoc = new JavaDoc();
        String name = element.getName();
        // doc for description
        String description = PsiUtil.getDescription(element);

        if (null == description) {
            description = BiyingTranslate.translate(JavaBeansUtil.humpToSpace(name));
        }
        javaDoc.addDescription(description);
        // doc for parameter
        PsiParameterList parameterList = element.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for (PsiParameter parameter : parameters) {
            String className = ((PsiClassReferenceType) parameter.getType()).getClassName();
            javaDoc.addParam(parameter.getName(), className);
        }
        // doc for return
        PsiType resultType = element.getReturnType();
        if (resultType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) resultType;
            String returnClass = psiClassReferenceType.getClassName();
            javaDoc.setRturn(returnClass);

            javaDoc.addSee(psiClassReferenceType.getClassName());

            // doc for see
            PsiType[] resultTypeParameters = psiClassReferenceType.getParameters();
            for (PsiType resultTypeParameter : resultTypeParameters) {
                javaDoc.addSee(((PsiClassReferenceType) resultTypeParameter).getClassName());
            }
        } else {
            PsiPrimitiveType psiPrimitiveType = (PsiPrimitiveType) resultType;
            javaDoc.setRturn(psiPrimitiveType.getName());
        }

        PsiReferenceList throwsList = element.getThrowsList();
        PsiJavaCodeReferenceElement[] referenceElements = throwsList.getReferenceElements();
        for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
            javaDoc.addThrowz(referenceElement.getQualifiedName());
        }


        return psiElementFactory.createDocCommentFromText(javaDoc.toString());
    }
}
