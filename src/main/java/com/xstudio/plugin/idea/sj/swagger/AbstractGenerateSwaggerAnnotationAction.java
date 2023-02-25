package com.xstudio.plugin.idea.sj.swagger;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.javadoc.PsiDocComment;
import com.xstudio.plugin.idea.sj.util.PsiUtil;

/**
 * @author beeant
 */
public abstract class AbstractGenerateSwaggerAnnotationAction extends AnAction {
    private PsiElementFactory elementFactory;

    public PsiElementFactory getElementFactory() {
        return elementFactory;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        assert editor != null;
        int startPosition = editor.getSelectionModel().getSelectionStart();

        PsiClass psiClass = PsiUtil.getPsiMethodFromContext(e);
        assert psiClass != null;
        Project project = psiClass.getProject();

        elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

        final PsiFile file = psiClass.getContainingFile();

        PsiElement element = PsiUtil.getJavaElement(PsiUtil.getPsiElement(file, startPosition));
        if (element instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) element;
            PsiParameterList parameterList = psiMethod.getParameterList();
            PsiParameter[] parameters = parameterList.getParameters();
            for (PsiParameter parameter : parameters) {
                WriteCommandAction.writeCommandAction(project, file)
                        .withGlobalUndo()
                        .run(() -> addParameterAnnotation(parameter));

            }
            // method annotation
            WriteCommandAction.writeCommandAction(project, file)
                    .withGlobalUndo()
                    .run(() -> addMethodAnnotation(psiMethod));
        } else if (element instanceof PsiParameter) {
            WriteCommandAction.writeCommandAction(project, file)
                    .withGlobalUndo()
                    .run(() -> addParameterAnnotation((PsiParameter) element));
        } else if (element instanceof PsiClass) {
            WriteCommandAction.writeCommandAction(project, file)
                    .withGlobalUndo()
                    .run(() -> addClassAnnotation((PsiClass) element));
        } else if (element instanceof PsiField) {
            WriteCommandAction.writeCommandAction(project, file)
                    .withGlobalUndo()
                    .run(() -> addFieldAnnotation((PsiField) element));
        }
    }

    /**
     * add field annotation
     *
     * @param psiField parameter
     */
    public abstract void addFieldAnnotation(PsiField psiField);

    /**
     * add parameter annotation
     *
     * @param psiParameter parameter
     */
    public abstract void addParameterAnnotation(PsiParameter psiParameter);

    /**
     * add method annotation
     *
     * @param psiMethod psiMethod
     */
    public abstract void addMethodAnnotation(PsiMethod psiMethod);

    /**
     * add class annotation
     *
     * @param psiClass psiClass
     */
    public abstract void addClassAnnotation(PsiClass psiClass);


    public void addAnnotation(PsiElement psiElement, String annotationText) {

        Project project = psiElement.getProject();

        PsiAnnotation psiAnnotation = getElementFactory().createAnnotationFromText(annotationText, psiElement);
        psiElement.addBefore(psiAnnotation, getInsertBeforeElement(psiElement));

        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformat(psiElement);

    }

    public PsiElement getInsertBeforeElement(PsiElement element) {
        PsiElement psiElement = element.getFirstChild();
        for (PsiElement child : element.getChildren()) {
            if (!(child instanceof PsiDocComment)) {
                psiElement = child;
                break;
            }
        }

        return psiElement;
    }
}
