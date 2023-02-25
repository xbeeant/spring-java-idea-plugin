package com.xstudio.plugin.idea.sj.spring;

import com.intellij.lang.jvm.annotation.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.xstudio.plugin.idea.sj.spring.components.MappingListForm;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * // TODO description
 *
 * @author xiaobiao
 * @version 2020/5/7
 */
public class MappingHelper {

    private static final Map<String, MappingListForm> REST_LIST_FORMS = new HashMap<>();

    /**
     * find all rest request mapping in project
     *
     * @param project project
     * @return {@link List}
     * @see List
     * @see Mapping
     */
    private static List<Mapping> findAllRestMapping(Project project) {
        List<Mapping> mappings = new ArrayList<>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);

        Module[] modules = moduleManager.getSortedModules();
        // 获取 request list
        for (Module module : modules) {
            mappings.addAll(findAllRestMapping(project, module));
        }
        return mappings;
    }

    private static List<Mapping> findAllScheduleMapping(Project project) {
        List<Mapping> mappings = new ArrayList<>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);

        Module[] modules = moduleManager.getSortedModules();
        // 获取 request list
        for (Module module : modules) {
            mappings.addAll(findAllScheduleMapping(project, module));
        }
        return mappings;
    }

    private static List<Mapping> findAllScheduleMapping(Project project, Module module) {
        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);

        List<String> annotations = Annotations.getScheduleScanAnnotation();
        List<Mapping> mappings = new ArrayList<>();
        for (String annotation : annotations) {
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(annotation, project, moduleScope);
            List<Mapping> paths = getSchedulePaths(psiAnnotations, module);
            mappings.addAll(paths);
        }

        return mappings;
    }

    /**
     * find all request in module
     *
     * @param project project
     * @param module  module
     * @return {@link List}
     * @see List
     * @see Mapping
     */
    private static List<Mapping> findAllRestMapping(Project project, Module module) {
        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
        // search all spring @RestController annotation in module
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get("RestController", project, moduleScope);
        List<Mapping> mappings = getRequestPaths(psiAnnotations, module);

        // search all spring @Controller annotation in module
        psiAnnotations = JavaAnnotationIndex.getInstance().get("Controller", project, moduleScope);
        mappings.addAll(getRequestPaths(psiAnnotations, module));
        return mappings;
    }

    /**
     * get request paths
     *
     * @param psiAnnotations psiAnnotations
     * @param module         module
     * @return {@link List}
     * @see List
     * @see Mapping
     */
    private static List<Mapping> getSchedulePaths(Collection<PsiAnnotation> psiAnnotations, Module module) {
        List<Mapping> mappings = new ArrayList<>();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            mappings.addAll(getSchedulePaths(psiClass, module, new ArrayList<>()));
        }

        return mappings;
    }

    /**
     * get request paths
     *
     * @param psiAnnotations psiAnnotations
     * @param module         module
     * @return {@link List}
     * @see List
     * @see Mapping
     */
    private static List<Mapping> getRequestPaths(Collection<PsiAnnotation> psiAnnotations, Module module) {
        List<Mapping> mappings = new ArrayList<>();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            mappings.addAll(getRequestPaths(psiClass, module, new ArrayList<>()));
        }

        return mappings;
    }

    /**
     * get path
     *
     * @param psiAnnotation psiAnnotation
     * @param restMapping   mapping
     * @return {@link List}
     * @see List
     * @see String
     */
    private static String getScheduleMappingPath(PsiAnnotation psiAnnotation, RestMapping restMapping) {
        StringBuilder sb = new StringBuilder();
        if (null != psiAnnotation) {
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            for (JvmAnnotationAttribute attribute : attributes) {
                String name = attribute.getAttributeName();
                sb.append(name);
                sb.append("=");
                Object attributeValue = getAttributeValue(attribute.getAttributeValue());
                sb.append(attributeValue);
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return sb.toString();
    }

    private static List<Mapping> getSchedulePaths(PsiClass psiClass, Module module, List<String> parentRequestMapping) {
        List<Mapping> mappings = new ArrayList<>();

        // 获取各方法的mapping
        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            List<RestMapping> restMappings = Annotations.getScheduleMethods();
            for (RestMapping restMapping : restMappings) {
                PsiAnnotation annotation = psiMethod.getAnnotation(restMapping.getQualifiedName());
                if (null != annotation) {
                    String path = getScheduleMappingPath(annotation, restMapping);
                    if (!"".equals(path)) {
                        mappings.add(new Mapping(path, "schedule", psiMethod, module.getName()));
                    }
                }
            }
        }

        return mappings;
    }

    /**
     * get request paths
     *
     * @param psiClass             psiClass
     * @param module               module
     * @param parentRequestMapping parentRequestMapping
     * @return {@link List}
     * @see List
     * @see Mapping
     */
    private static List<Mapping> getRequestPaths(PsiClass psiClass, Module module, List<String> parentRequestMapping) {
        List<Mapping> mappings = new ArrayList<>();
        // 获取 class 的requestMapping路径
        if (CollectionUtils.isEmpty(parentRequestMapping)) {
            for (RestMapping restMapping : Annotations.getRestClasz()) {
                PsiAnnotation requestMappingAnnotation = psiClass.getAnnotation(restMapping.getQualifiedName());
                parentRequestMapping = getRestMappingPath(requestMappingAnnotation, restMapping);
                if (!CollectionUtils.isEmpty(parentRequestMapping)) {
                    break;
                }
            }
            if (CollectionUtils.isEmpty(parentRequestMapping)) {
                parentRequestMapping = Collections.singletonList("");
            }
        }

        PsiClass superClass = psiClass.getSuperClass();
        if (null != superClass) {
            if (!Objects.equals(superClass.getQualifiedName(), "java.lang.Object")) {
                mappings.addAll(getRequestPaths(superClass, module, parentRequestMapping));
            }
        }

        // 获取各方法的mapping
        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            List<RestMapping> restMappings = Annotations.getRestMethods();
            for (RestMapping restMapping : restMappings) {
                PsiAnnotation annotation = psiMethod.getAnnotation(restMapping.getQualifiedName());
                if (null != annotation) {
                    List<String> path = getRestMappingPath(annotation, restMapping);
                    if (!CollectionUtils.isEmpty(path)) {
                        for (String parentRequest : parentRequestMapping) {
                            for (String s : path) {
                                List<String> methods = getMethod(annotation, restMapping);
                                for (String method : methods) {
                                    mappings.add(new Mapping(parentRequest, s, method, psiMethod, module.getName()));
                                }
                            }
                        }
                    } else {
                        for (String parentRequest : parentRequestMapping) {
                            List<String> methods = getMethod(annotation, restMapping);
                            for (String method : methods) {
                                mappings.add(new Mapping(parentRequest, "", method, psiMethod, module.getName()));
                            }
                        }
                    }
                }
            }
        }

        return mappings;
    }

    /**
     * get method
     *
     * @param annotation  annotation
     * @param restMapping mapping
     * @return {@link List}
     * @see List
     * @see String
     */
    private static List<String> getMethod(PsiAnnotation annotation, RestMapping restMapping) {
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

        methods.add(restMapping.getMethod());
        return methods;
    }

    /**
     * get mapping list form
     *
     * @param project project
     * @return {@link MappingListForm}
     * @see MappingListForm
     */
    public static MappingListForm getMappingListForm(Project project) {
        if (!REST_LIST_FORMS.containsKey(project.getLocationHash())) {
            MappingHelper.REST_LIST_FORMS.put(getProjectUniqueCode(project), new MappingListForm(project));
        }
        return REST_LIST_FORMS.get(getProjectUniqueCode(project));
    }

    /**
     * 获取属性值
     *
     * @param attributeValue Psi属性
     * @return {@link Object}
     * @see Object
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

    /**
     * get path
     *
     * @param psiAnnotation psiAnnotation
     * @param restMapping   mapping
     * @return {@link List}
     * @see List
     * @see String
     */
    private static List<String> getRestMappingPath(PsiAnnotation psiAnnotation, RestMapping restMapping) {
        List<String> paths = new ArrayList<>();
        if (null != psiAnnotation) {
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            for (JvmAnnotationAttribute attribute : attributes) {
                String name = attribute.getAttributeName();
                if ("value".equals(name) || "path".equals(name) || "name".equals(name)) {
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
        return new ArrayList<>();
    }

    /**
     * get list model
     *
     * @param requests requests
     * @return {@link DefaultListModel}
     * @see DefaultListModel
     * @see Mapping
     */
    public static DefaultListModel<Mapping> getListModel(List<Mapping> requests) {
        DefaultListModel<Mapping> listModel = new DefaultListModel<>();
        for (Mapping request : requests) {
            listModel.addElement(request);
        }
        return listModel;
    }

    public static void removeRestListForm(Project project) {
        REST_LIST_FORMS.remove(getProjectUniqueCode(project));
    }

    /**
     * get project unique code
     *
     * @param project project
     * @return {@link String}
     * @see String
     */
    private static String getProjectUniqueCode(Project project) {
        return project.getLocationHash();
    }

    public static List<Mapping> findAllMapping(Project project) {
        List<Mapping> mappings = new ArrayList<>();
        // 获取所有rest请求
        List<Mapping> restMappings = MappingHelper.findAllRestMapping(project);
        List<Mapping> scheduleMappings = MappingHelper.findAllScheduleMapping(project);
        mappings.addAll(restMappings);
        mappings.addAll(scheduleMappings);

        return mappings;
    }
}
