package org.testng.internal;

import java.lang.reflect.Method;

import org.testng.annotations.IParameterRenderAnnotation;

public class ParameterRenderHolder {
    Object                     instance;
    Method                     method;
    IParameterRenderAnnotation annotation;

    public ParameterRenderHolder(IParameterRenderAnnotation annotation, Method method,
                                 Object instance) {
        this.annotation = annotation;
        this.method = method;
        this.instance = instance;
    }
}
