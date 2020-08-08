package com.xstudio.plugin.idea.sj.spring;

/**
 * @author xiaobiao
 * @version 2020/5/2
 */
public class Mapping {
    private final String method;

    private final String name;

    private final String qualifiedName;

    public Mapping(String method, String name, String qualifiedName) {
        this.method = method;
        this.name = name;
        this.qualifiedName = qualifiedName;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }
}
