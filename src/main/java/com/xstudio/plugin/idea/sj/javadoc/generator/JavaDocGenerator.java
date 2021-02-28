package com.xstudio.plugin.idea.sj.javadoc.generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JavaDocGenerator<T extends PsiElement> {

    /**
     * Generate java docs.
     *
     *
     * @param psiClass
     * @param element the Element
     * @return the Psi doc comment
     */
    @Nullable
    PsiComment generate(PsiClass psiClass, @NotNull T element);

}
