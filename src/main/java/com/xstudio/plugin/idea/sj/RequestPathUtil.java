package com.xstudio.plugin.idea.sj;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.xstudio.plugin.idea.sj.components.RestListForm;
import com.xstudio.plugin.idea.sj.spring.Annotations;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * // TODO description
 *
 * @author xiaobiao
 * @version 2020/5/7
 */
public class RequestPathUtil {

    public static List<RequestPath> getRequestPath(Project project) {
        List<RequestPath> requestPaths = new ArrayList<>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getSortedModules();
        // 获取 request list
        for (Module module : modules) {
            GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
            // search all spring @RestController annotation in module
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get("RestController", project, moduleScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiElement psiElement = psiAnnotation.getParent();
                PsiElement parent = psiElement.getParent();
                // 获取 class 的requestMapping路径
                String parentRequestMapping = null;
                for (Mapping mapping : Annotations.getClasz()) {
                    PsiAnnotation requestMappingAnnotation = ((PsiClassImpl) parent).getAnnotation(mapping.getQualifiedName());
                    parentRequestMapping = getPath(requestMappingAnnotation, mapping);
                    if (null != parentRequestMapping) {
                        break;
                    }
                }

                if (null == parentRequestMapping) {
                    parentRequestMapping = "";
                }

                // 获取各方法的mapping
                PsiMethod[] psiMethods = ((PsiClassImpl) parent).getMethods();
                for (PsiMethod psiMethod : psiMethods) {
                    List<Mapping> methods = Annotations.getMethods();
                    for (Mapping mapping : methods) {
                        PsiAnnotation annotation = psiMethod.getAnnotation(mapping.getQualifiedName());
                        String path = getPath(annotation, mapping);
                        if (null != path) {
                            requestPaths.add(new RequestPath(parentRequestMapping, path, mapping.getMethod(), psiMethod));
                        }
                    }
                }
            }
        }
        return requestPaths;
    }

    private static RestListForm restListForm;

    public static RestListForm getRestListForm(Project project){
        if (null == restListForm) {
            RequestPathUtil.restListForm = new RestListForm(project);
        }
        return restListForm;
    }

    private static String getPath(PsiAnnotation psiAnnotation, Mapping mapping) {
        if (null != psiAnnotation) {
            PsiAnnotationMemberValue value = psiAnnotation.findDeclaredAttributeValue("value");
            if (value instanceof PsiLiteralExpressionImpl) {
                Object path = ((PsiLiteralExpressionImpl) value).getValue();
                return String.valueOf(path);
            }
        }
        return null;
    }

    public static DefaultListModel<RequestPath> getListModel(List<RequestPath> requests) {
        DefaultListModel<RequestPath> listModel = new DefaultListModel<>();
        for (RequestPath request : requests) {
            listModel.addElement(request);
        }
        return listModel;
    }
}
