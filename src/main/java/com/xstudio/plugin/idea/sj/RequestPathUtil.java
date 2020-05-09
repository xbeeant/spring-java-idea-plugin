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
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.xstudio.plugin.idea.sj.components.RestListForm;
import com.xstudio.plugin.idea.sj.spring.Annotations;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import java.util.*;

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
            requestPaths.addAll(getRequestPaths(psiAnnotations, module));

            // search all spring @Controller annotation in module
            psiAnnotations = JavaAnnotationIndex.getInstance().get("Controller", project, moduleScope);
            requestPaths.addAll(getRequestPaths(psiAnnotations, module));
        }
        return requestPaths;
    }

    private static List<RequestPath> getRequestPaths(Collection<PsiAnnotation> psiAnnotations, Module module) {
        List<RequestPath> requestPaths = new ArrayList<>();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiElement psiElement = psiAnnotation.getParent();
            PsiElement parent = psiElement.getParent();
            // 获取 class 的requestMapping路径
            List<String> parentRequestMapping = null;
            for (Mapping mapping : Annotations.getClasz()) {
                PsiAnnotation requestMappingAnnotation = ((PsiClassImpl) parent).getAnnotation(mapping.getQualifiedName());
                parentRequestMapping = getPath(requestMappingAnnotation, mapping);
                if (null != parentRequestMapping) {
                    break;
                }
            }

            if (null == parentRequestMapping) {
                parentRequestMapping = Collections.singletonList("");
            }

            // 获取各方法的mapping
            PsiMethod[] psiMethods = ((PsiClassImpl) parent).getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                List<Mapping> mappings = Annotations.getMethods();
                for (Mapping mapping : mappings) {
                    PsiAnnotation annotation = psiMethod.getAnnotation(mapping.getQualifiedName());
                    List<String> path = getPath(annotation, mapping);
                    if (null != path) {
                        for (String parentRequest : parentRequestMapping) {
                            for (String s : path) {
                                List<String> methods = getMethod(annotation, mapping);
                                for (String method : methods) {
                                    requestPaths.add(new RequestPath(parentRequest, s, method, psiMethod, module.getName()));
                                }
                            }
                        }
                    }
                }
            }
        }

        return requestPaths;
    }

    private static List<String> getMethod(PsiAnnotation annotation, Mapping mapping) {
        List<String> methods = new ArrayList<>();
        PsiAnnotationMemberValue memberValue = annotation.findDeclaredAttributeValue("method");
        if (null != memberValue) {
            if (memberValue instanceof PsiReferenceExpressionImpl) {
                String method = ((PsiReferenceExpressionImpl) memberValue).getQualifiedName().substring(14).toLowerCase();
                methods.add(method);
                return methods;
            } else if (memberValue instanceof PsiArrayInitializerMemberValueImpl) {
                PsiAnnotationMemberValue[] values = ((PsiArrayInitializerMemberValueImpl) memberValue).getInitializers();
                for (PsiAnnotationMemberValue member : values) {
                    String method = ((PsiReferenceExpressionImpl) member).getQualifiedName().substring(14).toLowerCase();
                    methods.add(method);
                }
                return methods;
            }
        }

        methods.add(mapping.getMethod());
        return methods;
    }

    private static Map<String, RestListForm> restListForms = new HashMap<>();

    public static RestListForm getRestListForm(Project project) {
        if (!restListForms.containsKey(project.getLocationHash())) {
            RequestPathUtil.restListForms.put(getProjectUniqueCode(project), new RestListForm(project));
        }
        return restListForms.get(getProjectUniqueCode(project));
    }

    private static List<String> getPath(PsiAnnotation psiAnnotation, Mapping mapping) {
        List<String> paths = new ArrayList<>();
        if (null != psiAnnotation) {
            PsiAnnotationMemberValue value = psiAnnotation.findDeclaredAttributeValue("value");
            if (value instanceof PsiLiteralExpressionImpl) {
                Object path = ((PsiLiteralExpressionImpl) value).getValue();
                paths.add(String.valueOf(path));
                return paths;
            } else if (value instanceof PsiArrayInitializerMemberValueImpl) {
                PsiAnnotationMemberValue[] values = ((PsiArrayInitializerMemberValueImpl) value).getInitializers();
                StringBuilder sb = new StringBuilder();
                for (PsiAnnotationMemberValue path : values) {
                    paths.add((String) ((PsiLiteralExpressionImpl) path).getValue());
                }
                return paths;
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

    public static void removeRestListForm(Project project) {
        restListForms.remove(getProjectUniqueCode(project));
    }

    private static String getProjectUniqueCode(Project project) {
        return project.getLocationHash();
    }
}
