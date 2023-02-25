package com.xstudio.plugin.idea.sj.swagger;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.xstudio.plugin.idea.sj.translate.BiyingTranslate;
import com.xstudio.plugin.idea.sj.util.PsiUtil;

/**
 * @author beeant
 */
public class GenerateSwagger2AnnotationAction extends AbstractGenerateSwaggerAnnotationAction {
    @Override
    public void addFieldAnnotation(PsiField psiField) {
        String description = PsiUtil.getDescription(psiField);
        if (null == description) {
            description = BiyingTranslate.translate(psiField.getName());
        }

        String sb = "@ApiModelProperty(name = \"" +
                psiField.getName() +
                "\", description = \"" + description + "\", defaultValue=\"\", example = \"\")";
        addAnnotation(psiField, sb);
    }


    @Override
    public void addParameterAnnotation(PsiParameter psiParameter) {
        String sb = "@ApiParam(value = \"" +
                BiyingTranslate.translate(psiParameter.getName()) +
                "\", required = true)";
        addAnnotation(psiParameter, sb);
    }

    @Override
    public void addMethodAnnotation(PsiMethod psiMethod) {
        String description = PsiUtil.getDescription(psiMethod);
        if (null == description) {
            description = BiyingTranslate.translate(psiMethod.getName());
        }

        String sb = "@ApiOperation(value = \"" + description + "\", notes = \"\")";
        addAnnotation(psiMethod, sb);
    }

    @Override
    public void addClassAnnotation(PsiClass psiClass) {
        String description = PsiUtil.getDescription(psiClass);
        if (null == description) {
            description = BiyingTranslate.translate(psiClass.getName());
        }
        String sb = "@Api(tags = \"" + description + "\")";
        addAnnotation(psiClass, sb);
    }
}
