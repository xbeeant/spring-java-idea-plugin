package com.xstudio.plugin.idea.sj.spring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * @author xiaobiao
 * @version 2020/5/2
 */
public class RequestPath {
    private String path;

    private String method;

    private PsiMethod psiMethod;

    private String moduleName;

    public RequestPath(String controllerPath, String methodPath, String method, PsiMethod psiMethod, String moduleName) {
        String prePath = controllerPath;
        if (controllerPath.endsWith("/")) {
            prePath = controllerPath.substring(0, controllerPath.length() - 1);
        }

        String path = methodPath;
        if (!methodPath.startsWith("/") && !"".equals(path)) {
            path = "/" + methodPath;
        }

        this.path = prePath + path;
        this.method = method;
        this.psiMethod = psiMethod;
        this.moduleName = moduleName;
    }

    public String getPath() {
        if (null == path) {
            return "";
        }
        return path;
    }

    public String getMethod() {
        return method;
    }

    public PsiElement getPsiMethod() {
        return psiMethod;
    }

    public String getModuleName() {
        return moduleName;
    }
}
