package com.xstudio.plugin.idea.sj;

import com.intellij.lang.jvm.annotation.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.xstudio.plugin.idea.sj.components.RestListForm;
import com.xstudio.plugin.idea.sj.spring.Annotations;
import com.xstudio.plugin.idea.sj.spring.Mapping;
import com.xstudio.plugin.idea.sj.spring.RequestPath;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * // TODO description
 *
 * @author xiaobiao
 * @version 2020/5/7
 */
public class RequestPathUtil {

    private static Map<String, RestListForm> restListForms = new HashMap<>();

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

    public static RestListForm getRestListForm(Project project) {
        if (!restListForms.containsKey(project.getLocationHash())) {
            RequestPathUtil.restListForms.put(getProjectUniqueCode(project), new RestListForm(project));
        }
        return restListForms.get(getProjectUniqueCode(project));
    }

    /**
     * 获取属性值
     *
     * @param attributeValue Psi属性
     * @return {Object | List}
     */
    public static Object getAttributeValue(JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        if (attributeValue instanceof JvmAnnotationConstantValue) {
            return ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
        } else if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        } else if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<JvmAnnotationAttributeValue> values = ((JvmAnnotationArrayValue) attributeValue).getValues();
            List<Object> list = new ArrayList<>(values.size());
            for (JvmAnnotationAttributeValue value : values) {
                Object o = getAttributeValue(value);
                if (o != null) {
                    list.add(o);
                } else {
                    // 如果是jar包里的JvmAnnotationConstantValue则无法正常获取值
                    try {
                        Class<? extends JvmAnnotationAttributeValue> clazz = value.getClass();
                        Field myElement = clazz.getSuperclass().getDeclaredField("myElement");
                        myElement.setAccessible(true);
                        Object elObj = myElement.get(value);
                        if (elObj instanceof PsiExpression) {
                            PsiExpression expression = (PsiExpression) elObj;
                            list.add(expression.getText());
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
            return list;
        } else if (attributeValue instanceof JvmAnnotationClassValue) {
            return ((JvmAnnotationClassValue) attributeValue).getQualifiedName();
        }
        return null;
    }

    private static List<String> getPath(PsiAnnotation psiAnnotation, Mapping mapping) {
        List<String> paths = new ArrayList<>();
        if (null != psiAnnotation) {
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            for (JvmAnnotationAttribute attribute : attributes) {
                String name = attribute.getAttributeName();
                if (name.equals("value")) {
                    Object attributeValue = getAttributeValue(attribute.getAttributeValue());
                    if (attributeValue instanceof String) {
                        paths.add((String) attributeValue);
                    } else if (attributeValue instanceof List) {
                        //noinspection unchecked,rawtypes
                        List<String> list = (List) attributeValue;
                        for (String item : list) {
                            if (item != null) {
                                item = item.substring(item.lastIndexOf(".") + 1);
                                paths.add(item);
                            }
                        }
                    }
                }
            }

            return paths;
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
