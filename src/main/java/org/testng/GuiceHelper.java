package org.testng;

import static java.util.Collections.unmodifiableList;
import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.testng.annotations.Guice;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.ClassImpl;
import org.testng.internal.InstanceCreator;
import org.testng.internal.annotations.AnnotationHelper;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class GuiceHelper {
  private final ITestContext context;

  GuiceHelper(ITestContext context) {
    this.context = context;
  }

  Injector getInjector(IClass iClass, IInjectorFactory injectorFactory) {
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
    Injector parentInjector = ((ClassImpl) iClass).getParentInjector(injectorFactory);

    List<Module> moduleInstances =
        Lists.newArrayList(getModules(guice, parentInjector, iClass.getRealClass()));
    Module parentModule = getParentModule(context);
    if (parentModule != null) {
      moduleInstances.add(parentModule);
    }
    List<Module> moduleLookup = Lists.newArrayList(moduleInstances);

    // Get an injector with the class's modules + any defined parent module installed
    // Reuse the previous injector, if any, but don't create a child injector as JIT bindings can conflict
    Injector injector = context.getInjector(moduleLookup);
    if (injector == null) {
      injector = createInjector(context, injectorFactory, moduleInstances);
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

  /**
   * @param context The test context
   * @param moduleInstances The modules
   * @return The Injector
   * @deprecated - This method stands deprecated as of 7.0.1
   */
  @Deprecated
  public static Injector createInjector(ITestContext context, List<Module> moduleInstances) {
    return createInjector(context, com.google.inject.Guice::createInjector, moduleInstances);
  }

  public static Injector createInjector(ITestContext context,
      IInjectorFactory injectorFactory, List<Module> moduleInstances) {
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
    return injectorFactory.getInjector(stage, fullModules.toArray(new Module[0]));
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
      Module module = factoryInstance.createModule(context, testClass);
      if (module != null) {
        result.add(module);
      }
    }
    result.addAll(LazyHolder.getSpiModules());
    return result;
  }

  private static final class LazyHolder {
    private static final List<Module> spiModules;

    static {
      List<Module> modules = new ArrayList<>();
      for (IModule module : ServiceLoader.load(IModule.class)) {
        modules.add(module.getModule());
      }
      spiModules = unmodifiableList(modules);
    }

    public static List<Module> getSpiModules() {
      return spiModules;
    }
  }
}