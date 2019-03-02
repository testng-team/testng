package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.testng.annotations.Guice;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.ClassImpl;
import org.testng.internal.InstanceCreator;
import org.testng.internal.annotations.AnnotationHelper;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

public class GuiceHelper {
  private final ITestContext context;

  GuiceHelper(ITestContext context) {
    this.context = context;
  }

  Injector getInjector(IClass iClass) {
    Guice guice =
        AnnotationHelper.findAnnotationSuperClasses(Guice.class, iClass.getRealClass());
    if (guice == null) {
      return null;
    }
    if (iClass instanceof TestClass) {
      iClass = ((TestClass) iClass).getIClass();
    }
    if (!(iClass instanceof ClassImpl)) {
      return null;
    }
    Injector parentInjector = ((ClassImpl) iClass).getParentInjector();

    List<Module> moduleInstances =
        Lists.newArrayList(getModules(guice, parentInjector, iClass.getRealClass()));
    List<Module> moduleLookup = Lists.newArrayList(moduleInstances);
    Module parentModule = getParentModule(context);
    if (parentModule != null) {
      moduleInstances.add(parentModule);
    }

    // Get an injector with the class's modules + any defined parent module installed
    // Reuse the previous injector, if any, but don't create a child injector as JIT bindings can conflict
    Injector injector = context.getInjector(moduleLookup);
    if (injector == null) {
      injector = createInjector(context, moduleInstances);
      context.addInjector(moduleInstances, injector);
    }
    return injector;
  }

  private static Module getParentModule(ITestContext context) {
    if (isStringEmpty(context.getSuite().getParentModule())) {
      return null;
    }
    Class<?> parentModule = ClassHelper.forName(context.getSuite().getParentModule());
    if (parentModule == null) {
      throw new TestNGException(
              "Cannot load parent Guice module class: " + context.getSuite().getParentModule());
    }
    if (!Module.class.isAssignableFrom(parentModule)) {
      throw new TestNGException("Provided class is not a Guice module: " + parentModule.getName());
    }
    try {
      Constructor<?> moduleConstructor = parentModule.getDeclaredConstructor(ITestContext.class);
      return (Module)InstanceCreator.newInstance(moduleConstructor, context);
    } catch (NoSuchMethodException e) {
      return (Module)InstanceCreator.newInstance(parentModule);
    }
  }

  public static Injector createInjector(ITestContext context, List<Module> moduleInstances) {
    Module parentModule = getParentModule(context);
    List<Module> fullModules = Lists.newArrayList(moduleInstances);
    if (parentModule != null) {
      fullModules.add(parentModule);
    }
    Stage stage = Stage.DEVELOPMENT;
    String stageString = context.getSuite().getGuiceStage();
    if (isStringNotEmpty(stageString)) {
      stage = Stage.valueOf(stageString);
    }
    return com.google.inject.Guice.createInjector(stage, fullModules);
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
