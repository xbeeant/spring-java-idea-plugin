package com.xstudio.plugin.idea.sj.spring;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobiao
 * @version 2020/5/2
 */
public class Annotations {
    private static final List<RestMapping> REST_CLASZ = new ArrayList<>() {
        {
            add(new RestMapping("request", "RequestMapping", "org.springframework.web.bind.annotation.RequestMapping"));
        }
    };

    private static final List<String> SCHEDULE_SCAN_ANNOTATION = new ArrayList<>() {
        {
            add("Service");
            add("Component");
        }
    };

    private static final List<RestMapping> REST_METHODS = new ArrayList<>() {
        {
            add(new RestMapping("request ", "RequestMapping", "org.springframework.web.bind.annotation.RequestMapping"));
            add(new RestMapping("delete  ", "DeleteMapping", "org.springframework.web.bind.annotation.DeleteMapping"));
            add(new RestMapping("get     ", "GetMapping", "org.springframework.web.bind.annotation.GetMapping"));
            add(new RestMapping("post    ", "PostMapping", "org.springframework.web.bind.annotation.PostMapping"));
            add(new RestMapping("put     ", "PutMapping", "org.springframework.web.bind.annotation.PutMapping"));
            add(new RestMapping("patch   ", "PatchMapping", "org.springframework.web.bind.annotation.PatchMapping"));
        }
    };

    private static final List<RestMapping> SCHEDULE_METHODS = new ArrayList<>() {
        {
            add(new RestMapping("schedule", "Scheduled", "org.springframework.scheduling.annotation.Scheduled"));
        }
    };

    public static List<RestMapping> getRestClasz() {
        return REST_CLASZ;
    }

    public static List<RestMapping> getRestMethods() {
        return REST_METHODS;
    }

    public static List<RestMapping> getScheduleMethods() {
        return SCHEDULE_METHODS;
    }

    public static List<String> getScheduleScanAnnotation() {
        return SCHEDULE_SCAN_ANNOTATION;
    }
}
