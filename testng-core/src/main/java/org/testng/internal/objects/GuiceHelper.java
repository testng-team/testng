package org.testng.internal.objects;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;
import org.testng.IClass;
import org.testng.IInjectorFactory;
import org.testng.IModule;
import org.testng.IModuleFactory;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.TestNGException;
import org.testng.annotations.Guice;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.ClassHelper;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.invokers.objects.GuiceContext;

class GuiceHelper {
  private final Map<List<Module>, Injector> m_injectors = Maps.newHashMap();
  private final ListMultiMap<Class<? extends Module>, Module> m_guiceModules =
      Maps.newListMultiMap();
  private final String parentModule;
  private final String stageString;
  private final String testName;
  private final ITestContext context;

  private static final BiPredicate<Module, Module> CLASS_EQUALITY =
      (m, n) -> m.getClass().equals(n.getClass());

  GuiceHelper(ITestContext context) {
    parentModule = context.getSuite().getParentModule();
    stageString = context.getSuite().getGuiceStage();
    testName = context.getName();
    this.context = context;
  }

  GuiceHelper(GuiceContext context) {
    parentModule = context.getParentModule();
    stageString = context.getGuiceStage();
    testName = context.getName();
    this.context = null;
  }

  Injector getInjector(IClass iClass, IInjectorFactory injectorFactory) {
    return getInjector(iClass.getRealClass(), injectorFactory);
  }

  Injector getInjector(Class<?> cls, IInjectorFactory injectorFactory) {
    Guice guice = AnnotationHelper.findAnnotationSuperClasses(Guice.class, cls);
    if (guice == null) {
      return null;
    }
    Injector parentInjector = getParentInjector(injectorFactory);

    List<Module> classLevelModules = getModules(guice, parentInjector, cls);

    // Get an injector with the class's modules + any defined parent module installed
    // Reuse the previous injector, if any, but don't create a child injector as JIT bindings can
    // conflict
    Injector injector = getInjector(classLevelModules);
    if (injector == null) {
      injector = createInjector(parentInjector, injectorFactory, classLevelModules);
      addInjector(classLevelModules, injector);
    }
    return injector;
  }

  private Injector getParentInjector(IInjectorFactory factory) {
    // Reuse the previous parent injector, if any
    Injector injector = null;
    ISuite suite = null;
    if (context != null) {
      suite = context.getSuite();
      injector = suite.getParentInjector();
    }
    if (injector == null) {
      Module parentModule = getParentModule();
      injector =
          createInjector(
              null,
              factory,
              parentModule == null
                  ? Collections.emptyList()
                  : Collections.singletonList(parentModule));
      if (suite != null) {
        suite.setParentInjector(injector);
      }
    }
    return injector;
  }

  private void addInjector(List<Module> moduleInstances, Injector injector) {
    m_injectors.put(moduleInstances, injector);
  }

  Injector getInjector(List<Module> moduleInstances) {
    return m_injectors.get(moduleInstances);
  }

  public void addGuiceModule(Module module) {
    Class<? extends Module> cls = module.getClass();
    List<Module> modules = m_guiceModules.get(cls);
    boolean found = modules.stream().anyMatch(each -> each.getClass().equals(cls));
    if (!found) {
      modules.add(module);
    }
  }

  private List<Module> getGuiceModules(Class<? extends Module> cls) {
    return m_guiceModules.get(cls);
  }

  private Module getParentModule() {
    Class<? extends Module> parentModule = getParentModuleClass();
    if (parentModule == null) {
      return null;
    }

    List<Module> allModules = getGuiceModules(parentModule);
    if (!allModules.isEmpty()) {
      if (allModules.size() > 1) {
        throw new IllegalStateException(
            "Found more than 1 module associated with the test <" + testName + ">");
      }
      return allModules.get(0);
    }
    Module obj;
    try {
      Constructor<?> moduleConstructor = parentModule.getDeclaredConstructor(ITestContext.class);
      obj = (Module) InstanceCreator.newInstance(moduleConstructor, context);
    } catch (NoSuchMethodException e) {
      obj = InstanceCreator.newInstance(parentModule);
    }
    addGuiceModule(obj);
    return obj;
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Module> getParentModuleClass() {
    if (isStringEmpty(this.parentModule)) {
      return null;
    }
    Class<?> parentModule = ClassHelper.forName(this.parentModule);
    if (parentModule == null) {
      throw new TestNGException("Cannot load parent Guice module class: " + this.parentModule);
    }
    if (!Module.class.isAssignableFrom(parentModule)) {
      throw new TestNGException("Provided class is not a Guice module: " + parentModule.getName());
    }
    return (Class<? extends Module>) parentModule;
  }

  private Injector createInjector(
      Injector parent, IInjectorFactory injectorFactory, List<Module> moduleInstances) {
    Stage stage = Stage.DEVELOPMENT;
    if (isStringNotEmpty(stageString)) {
      stage = Stage.valueOf(stageString);
    }
    moduleInstances.forEach(this::addGuiceModule);
    Module[] modules = moduleInstances.toArray(new Module[0]);

    if (parent == null || getParentModuleClass() == null) {
      // there is no parent module in this suite defined therefore tree of injectors shouldn't
      // be created letting individual test modules to redefine bindings between each other
      return injectorFactory.getInjector(null, stage, modules);
    }

    return injectorFactory.getInjector(parent, stage, modules);
  }

  private List<Module> getModules(Guice guice, Injector parentInjector, Class<?> testClass) {
    List<Module> result = Lists.newArrayList();
    for (Class<? extends Module> moduleClass : guice.modules()) {
      List<Module> modules = getGuiceModules(moduleClass);
      if (modules != null && !modules.isEmpty()) {
        result.addAll(modules);
        result = Lists.merge(result, CLASS_EQUALITY, modules);
      } else {
        Module instance = parentInjector.getInstance(moduleClass);
        result = Lists.merge(result, CLASS_EQUALITY, Collections.singletonList(instance));
        addGuiceModule(instance);
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
      return StreamSupport.stream(ServiceLoader.load(IModule.class).spliterator(), false)
          .map(IModule::getModule)
          .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public static List<Module> getSpiModules() {
      return spiModules;
    }
  }
}
