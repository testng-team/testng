package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.testng.annotations.Guice;
import org.testng.collections.Lists;
import org.testng.internal.ClassImpl;
import org.testng.internal.annotations.AnnotationHelper;

import java.lang.annotation.Annotation;
import java.util.List;

class GuiceHelper {
  private final ITestContext context;

  GuiceHelper(ITestContext context) {
    this.context = context;
  }

  Injector getInjector(IClass iClass) {
    Annotation annotation =
        AnnotationHelper.findAnnotationSuperClasses(Guice.class, iClass.getRealClass());
    if (annotation == null) {
      return null;
    }
    if (iClass instanceof TestClass) {
      iClass = ((TestClass) iClass).getIClass();
    }
    if (!(iClass instanceof ClassImpl)) {
      return null;
    }
    Injector parentInjector = ((ClassImpl) iClass).getParentInjector();

    Guice guice = (Guice) annotation;
    List<Module> moduleInstances =
        Lists.newArrayList(getModules(guice, parentInjector, iClass.getRealClass()));

    // Reuse the previous injector, if any
    Injector injector = context.getInjector(moduleInstances);
    if (injector == null) {
      injector = parentInjector.createChildInjector(moduleInstances);
      context.addInjector(moduleInstances, injector);
    }
    return injector;
  }

  private List<Module> getModules(Guice guice, Injector parentInjector, Class<?> testClass) {
    List<Module> result = Lists.newArrayList();
    for (Class<? extends Module> moduleClass : guice.modules()) {
      List<Module> modules = context.getGuiceModules(moduleClass);
      if (modules != null && !modules.isEmpty()) {
        result.addAll(modules);
      } else {
        Module instance = parentInjector.getInstance(moduleClass);
        result.add(instance);
        context.getGuiceModules(moduleClass).add(instance);
      }
    }
    Class<? extends IModuleFactory> factory = guice.moduleFactory();
    if (factory != IModuleFactory.class) {
      IModuleFactory factoryInstance = parentInjector.getInstance(factory);
      Module moduleClass = factoryInstance.createModule(context, testClass);
      if (moduleClass != null) {
        result.add(moduleClass);
      }
    }

    return result;
  }
}
