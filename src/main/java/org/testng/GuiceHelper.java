package org.testng;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;
import org.testng.annotations.Guice;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.ClassImpl;
import org.testng.internal.InstanceCreator;
import org.testng.internal.annotations.AnnotationHelper;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

@Deprecated
/**
 * @deprecated - This implementation is deprecated as of TestNG <code>7.3.0</code>
 */
public class GuiceHelper {
  private final ITestContext context;
  private static final BiPredicate<Module, Module> CLASS_EQUALITY  = (m,n) -> m.getClass().equals(n.getClass());

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

    List<Module> classLevelModules = getModules(guice, parentInjector, iClass.getRealClass());

    // Get an injector with the class's modules + any defined parent module installed
    // Reuse the previous injector, if any, but don't create a child injector as JIT bindings can conflict
    Injector injector = context.getInjector(classLevelModules);
    if (injector == null) {
      injector = createInjector(parentInjector, context, injectorFactory, classLevelModules);
      context.addInjector(classLevelModules, injector);
    }
    return injector;
  }

  public static Module getParentModule(ITestContext context) {
    Class<? extends Module> parentModule = getParentModuleClass(context);
    if (parentModule == null) {
      return null;
    }

    List<Module> allModules = context.getGuiceModules(parentModule);
    if (!allModules.isEmpty()) {
      if (allModules.size() > 1) {
        throw new IllegalStateException("Found more than 1 module associated with the test <"
        + context.getName() + ">");
      }
      return allModules.get(0);
    }
    Module obj;
    try {
      Constructor<?> moduleConstructor = parentModule.getDeclaredConstructor(ITestContext.class);
      obj = (Module) InstanceCreator.newInstance(moduleConstructor, context);
    } catch (NoSuchMethodException e) {
      obj = (Module) InstanceCreator.newInstance(parentModule);
    }
      context.addGuiceModule(obj);
    return obj;
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Module> getParentModuleClass(ITestContext context) {
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
    return (Class<? extends Module>)parentModule;
  }

  public static Injector createInjector(Injector parent, ITestContext context,
      IInjectorFactory injectorFactory, List<Module> moduleInstances) {
    Stage stage = Stage.DEVELOPMENT;
    String stageString = context.getSuite().getGuiceStage();
    if (isStringNotEmpty(stageString)) {
      stage = Stage.valueOf(stageString);
    }
    moduleInstances.forEach(context::addGuiceModule);
    Module[] modules = moduleInstances.toArray(new Module[0]);

    if (parent == null || getParentModuleClass(context) == null) {
      // there is no parent module in this suite defined therefore tree of injectors shouldn't
      // be created letting individual test modules to redefine bindings between each other
      return injectorFactory.getInjector(null, stage, modules);
    }

    return injectorFactory.getInjector(parent, stage, modules);
  }

  private List<Module> getModules(Guice guice, Injector parentInjector, Class<?> testClass) {
    List<Module> result = Lists.newArrayList();
    for (Class<? extends Module> moduleClass : guice.modules()) {
      List<Module> modules = context.getGuiceModules(moduleClass);
      if (modules != null && !modules.isEmpty()) {
        result.addAll(modules);
        result = Lists.merge(result, CLASS_EQUALITY, modules);
      } else {
        Module instance = parentInjector.getInstance(moduleClass);
        result = Lists.merge(result, CLASS_EQUALITY, Collections.singletonList(instance));
        context.addGuiceModule(instance);
      }
    }
    Class<? extends IModuleFactory> factory = guice.moduleFactory();
    if (factory != IModuleFactory.class) {
      IModuleFactory factoryInstance = parentInjector.getInstance(factory);
      Module module = factoryInstance.createModule(context, testClass);
      if (module != null) {
        result = Lists.merge(result, CLASS_EQUALITY, Collections.singletonList(module));
      }
    }
    result = Lists.merge(result, CLASS_EQUALITY, LazyHolder.getSpiModules());
    return result;
  }

  private static final class LazyHolder {
    private static final List<Module> spiModules = loadModules();

    private static List<Module> loadModules() {
      return StreamSupport
          .stream(ServiceLoader.load(IModule.class).spliterator(), false)
          .map(IModule::getModule)
          .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public static List<Module> getSpiModules() {
      return spiModules;
    }
  }
}