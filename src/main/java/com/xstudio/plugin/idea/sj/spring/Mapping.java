package com.xstudio.plugin.idea.sj.spring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * @author xiaobiao
 * @version 2020/5/2
 */
public class Mapping {
    private String path;

    private String type;

    private PsiMethod psiMethod;

    private String moduleName;

    public Mapping(String controllerPath, String methodPath, String type, PsiMethod psiMethod, String moduleName) {
        String prePath = controllerPath;
        if (controllerPath.endsWith("/")) {
            prePath = controllerPath.substring(0, controllerPath.length() - 1);
        }

        String path = methodPath;
        if (!methodPath.startsWith("/") && !"".equals(path)) {
            path = "/" + methodPath;
        }

        this.path = prePath + path;
        this.type = type;
        this.psiMethod = psiMethod;
        this.moduleName = moduleName;
    }

    public Mapping(String path, String type, PsiMethod psiMethod, String moduleName) {
        this.path = path;
        this.type = type;
        this.psiMethod = psiMethod;
        this.moduleName = moduleName;
    }

    public String getPath() {
        if (null == path) {
            return "";
        }
        return path;
    }

    public String getType() {
        return type;
    }

    public String getFullpath() {
        return path + type;
    }

    public PsiElement getPsiMethod() {
        return psiMethod;
    }

    public String getModuleName() {
        return moduleName;
    }
}
