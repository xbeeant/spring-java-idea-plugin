package com.xstudio.plugin.idea.sj.javadoc.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.javadoc.PsiDocComment;
import com.xstudio.plugin.idea.sj.javadoc.entity.JavaDoc;
import com.xstudio.plugin.idea.sj.translate.BiyingTranslate;
import com.xstudio.plugin.idea.sj.util.JavaBeansUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldJavaDocGenerator extends AbstractJavaDocGenerator<PsiField> {
    public FieldJavaDocGenerator(Project project) {
        super(project);
    }

    @Nullable
    @Override
    public PsiDocComment generate(PsiClass psiClass, @NotNull PsiField element) {
        JavaDoc javaDoc = new JavaDoc();
        String name = element.getText();
        // doc for description
        javaDoc.addDescription(BiyingTranslate.translate(JavaBeansUtil.humpToSpace(name)));

        return psiElementFactory.createDocCommentFromText(javaDoc.toString());
    }
}
