package com.xstudio.plugin.idea.sj.swagger;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.xstudio.plugin.idea.sj.translate.BiyingTranslate;
import com.xstudio.plugin.idea.sj.util.PsiUtil;

public class GenerateSwaggerAnnotationAction extends AnAction {
    private PsiElementFactory elementFactory;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = LangDataKeys.EDITOR.getData(e.getDataContext());
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
        }
    }

    private void addParameterAnnotation(PsiParameter parameter) {
        String sb = "@ApiParam(value = \"" +
                BiyingTranslate.translate(parameter.getName()) +
                "\", required = true) \n";
        PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText(sb, parameter);
        parameter.addBefore(psiAnnotation, parameter.getFirstChild());
    }

    private void addMethodAnnotation(PsiMethod psiMethod) {
        String description = PsiUtil.getDescription(psiMethod);
        if (null == description) {
            description = BiyingTranslate.translate(psiMethod.getName());
        }

        String sb = "@ApiOperation(value = \"" + description + "\", notes = \"\")";
        PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText(sb, psiMethod);
        PsiDocComment docComment = psiMethod.getDocComment();
        if (null != docComment) {
            psiMethod.addAfter(psiAnnotation, docComment);
        } else {
            psiMethod.addBefore(psiAnnotation, psiMethod.getFirstChild());
        }
    }

    private void addClassAnnotation(PsiClass psiClass) {
        String description = PsiUtil.getDescription(psiClass);
        if (null == description) {
            description = BiyingTranslate.translate(psiClass.getName());
        }
        String sb = "@Api(tags = \"" + description + "\")";
        PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText(sb, psiClass);
        PsiDocComment docComment = psiClass.getDocComment();
        if (null != docComment) {
            psiClass.addAfter(psiAnnotation, docComment);
        } else {
            psiClass.addBefore(psiAnnotation, psiClass.getFirstChild());
        }
    }
}
