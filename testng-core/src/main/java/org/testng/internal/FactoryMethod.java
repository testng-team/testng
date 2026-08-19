package org.testng.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.testng.DataProviderHolder;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderListener;
import org.testng.IFactory;
import org.testng.IInstanceInfo;
import org.testng.ITestContext;
import org.testng.ITestMethodFinder;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.Lazy;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.ParameterHolder;
import org.testng.xml.XmlTest;

/** This class represents a method annotated with @Factory */
public class FactoryMethod extends BaseTestMethod {

  private final @Nullable IFactoryAnnotation factoryAnnotation;
  private final @Nullable Object m_instance;
  private final ITestContext m_testContext;
  private @Nullable String m_factoryCreationFailedMessage = null;
  private final DataProviderHolder holder;
  private final boolean m_lazy;

  public @Nullable String getFactoryCreationFailedMessage() {
    return m_factoryCreationFailedMessage;
  }

  private void init(
      @Nullable Object instance, IAnnotationFinder annotationFinder, ConstructorOrMethod com) {
    IListenersAnnotation annotation =
        annotationFinder.findAnnotation(com.getDeclaringClass(), IListenersAnnotation.class);
    if (annotation == null) {
      return;
    }
    Class<? extends ITestNGListener>[] listeners = annotation.getValue();
    for (Class<? extends ITestNGListener> listener : listeners) {
      Object obj = instance;
      if (obj == null) {
        try {
          obj = m_objectFactory.newInstance(listener);
        } catch (TestNGException e) {
          // TODO log
        }
      }

      if (obj != null) {
        if (IDataProviderListener.class.isAssignableFrom(obj.getClass())) {
          holder.addListener((IDataProviderListener) obj);
        }
        if (IDataProviderInterceptor.class.isAssignableFrom(obj.getClass())) {
          holder.addInterceptor((IDataProviderInterceptor) obj);
        }
      }
    }
  }

  // This constructor is intentionally created with package visibility because we dont have any
  // callers of this
  // constructor outside of this package.
  FactoryMethod(
      ConstructorOrMethod com,
      IObject.@Nullable IdentifiableObject identifiable,
      IAnnotationFinder annotationFinder,
      ITestContext testContext,
      ITestObjectFactory objectFactory,
      DataProviderHolder holder,
      IConfiguration configuration) {
    super(objectFactory, com.getName(), com, annotationFinder, identifiable);
    this.holder = holder;
    Object instance = IObject.IdentifiableObject.unwrap(identifiable);
    init(instance, annotationFinder, com);
    Utils.checkInstanceOrStatic(instance, com.getMethod());
    Utils.checkReturnType(com.getMethod(), Object[].class, IInstanceInfo[].class);
    Class<?> declaringClass = com.getDeclaringClass();
    if (instance != null && !declaringClass.isAssignableFrom(instance.getClass())) {
      Object embedded = IParameterInfo.embeddedInstance(instance);
      Class<?> cls = (embedded != null ? embedded : instance).getClass();
      String msg =
          "Found a default constructor and also a Factory method when working with "
              + declaringClass.getName()
              + ". Root cause: Mismatch between instance/method classes:["
              + cls.getName()
              + "] ["
              + declaringClass.getName()
              + "]";
      throw new TestNGException(msg);
    }
    if (instance == null
        && com.getMethod() != null
        && !Modifier.isStatic(com.getMethod().getModifiers())) {
      throw new TestNGException(
          "An inner factory method MUST be static. But '"
              + com.getMethod().getName()
              + "' from '"
              + declaringClass.getName()
              + "' is not.");
    }
    if (com.getMethod() != null && !Modifier.isPublic(com.getMethod().getModifiers())) {
      try {
        com.makeAccessible();
      } catch (SecurityException e) {
        throw new TestNGException(com.getMethod().getName() + " must be public", e);
      }
    }

    factoryAnnotation = annotationFinder.findAnnotation(com, IFactoryAnnotation.class);

    m_instance = getInstance();
    m_testContext = testContext;
    m_lazy = resolveLazy(com, configuration);
    NoOpTestClass tc = new NoOpTestClass();
    tc.setTestClass(declaringClass);
    m_testClass = tc;
    m_groups =
        getAllGroups(
            objectFactory, declaringClass, testContext.getCurrentXmlTest(), annotationFinder);
  }

  /**
   * Resolves whether this factory should instantiate lazily, following the precedence hierarchy: an
   * explicit {@code @Factory(lazy=...)} annotation value wins; failing that the suite level {@code
   * lazy-factory} attribute; failing that the {@link IConfiguration} (runner) toggle; defaulting to
   * eager. Lazy is only ever honored for constructor based factories.
   */
  private boolean resolveLazy(ConstructorOrMethod com, IConfiguration configuration) {
    // Method / IInstanceInfo factories always stay eager.
    if (com.getConstructor() == null) {
      return false;
    }
    Lazy annotationLazy = factoryAnnotation == null ? Lazy.UNSET : factoryAnnotation.getLazy();
    if (annotationLazy == null) {
      annotationLazy = Lazy.UNSET;
    }
    switch (annotationLazy) {
      case TRUE:
        return true;
      case FALSE:
        return false;
      case UNSET:
      default:
        Boolean suiteLazy = m_testContext.getCurrentXmlTest().getSuite().getLazyFactory();
        if (suiteLazy != null) {
          return suiteLazy;
        }
        return configuration != null && configuration.isLazyFactoryInstantiation();
    }
  }

  private static String[] getAllGroups(
      ITestObjectFactory objectFactory,
      Class<?> declaringClass,
      XmlTest xmlTest,
      IAnnotationFinder annotationFinder) {
    // Find the groups of the factory => all groups of all test methods
    ITestMethodFinder testMethodFinder =
        new TestNGMethodFinder(objectFactory, new RunInfo(() -> xmlTest), annotationFinder);
    ITestNGMethod[] testMethods = testMethodFinder.getTestMethods(declaringClass, xmlTest);
    Set<String> groups = new HashSet<>();
    for (ITestNGMethod method : testMethods) {
      groups.addAll(Arrays.asList(method.getGroups()));
    }
    return groups.toArray(new String[0]);
  }

  public IParameterInfo[] invoke() {
    List<IParameterInfo> result = new ArrayList<>();

    Map<String, String> allParameterNames = new HashMap<>();
    Parameters.MethodParameters methodParameters =
        new Parameters.MethodParameters(
            m_testContext.getCurrentXmlTest().getAllParameters(),
            findMethodParameters(m_testContext.getCurrentXmlTest()),
            null,
            null,
            m_testContext,
            null /* testResult */);

    ParameterHolder parameterHolder =
        Parameters.handleParameters(
            m_objectFactory,
            this,
            allParameterNames,
            m_instance,
            methodParameters,
            m_testContext.getCurrentXmlTest().getSuite(),
            m_annotationFinder,
            null /* fedInstance */,
            this.holder,
            "@Factory");
    Iterator<Object[]> parameterIterator = parameterHolder.parameters;

    try {
      List<Integer> indices =
          Objects.requireNonNull(factoryAnnotation, "no @Factory annotation on a factory method")
              .getIndices();
      int position = 0;
      IFactory factory = new FactoryDescriptor(getConstructorOrMethod(), m_lazy);
      while (parameterIterator.hasNext()) {
        Object[] parameters = parameterIterator.next();
        if (parameters == null) {
          // skipped value
          continue;
        }
        ConstructorOrMethod com = getConstructorOrMethod();
        if (com.getMethod() != null) {
          Object[] testInstances = (Object[]) com.getMethod().invoke(m_instance, parameters);
          if (testInstances == null) {
            testInstances = new Object[] {};
          }
          if (testInstances.length == 0) {
            this.m_factoryCreationFailedMessage =
                String.format(
                    "The Factory method %s.%s() should have produced at-least one instance.",
                    com.getDeclaringClass().getName(), com.getName());
          }
          // A single invocation can return several instances, so the slot within testInstances is
          // what separates them; the invocation itself only supplies the offset they start at.
          if (indices == null || indices.isEmpty()) {
            for (int slot = 0; slot < testInstances.length; slot++) {
              result.add(
                  new ParameterInfo(
                      testInstances[slot],
                      new FactoryInstance(position, slot, parameters, factory)));
            }
          } else {
            for (Integer index : indices) {
              int slot = index - position;
              if (slot >= 0 && slot < testInstances.length) {
                result.add(
                    new ParameterInfo(
                        testInstances[slot],
                        new FactoryInstance(position, slot, parameters, factory)));
              }
            }
          }
          position += testInstances.length;
        } else {
          if (indices == null || indices.isEmpty() || indices.contains(position)) {
            // A constructor factory produces exactly one instance per invocation, so the slot is
            // always 0 and the invocation index already is the instance's position.
            if (m_lazy) {
              // An Iterator<Object[]> is free to hand back the same array for every row (a reused
              // buffer). Eager construction is unaffected because the constructor consumes the
              // values
              // right away, but a lazy instance retains this array until it instantiates later, so
              // we
              // snapshot the row to keep each instance bound to its own parameters.
              Object[] rowParameters = parameters.clone();
              Constructor<?> constructor = com.requireConstructor();
              result.add(
                  new LazyParameterInfo(
                      new FactoryInstance(position, 0, rowParameters, factory),
                      com.getDeclaringClass(),
                      () -> m_objectFactory.newInstance(constructor, rowParameters)));
            } else {
              Object instance =
                  Objects.requireNonNull(
                      m_objectFactory.newInstance(com.requireConstructor(), parameters),
                      "the object factory produced a @Factory instance");
              result.add(
                  new ParameterInfo(
                      instance, new FactoryInstance(position, 0, parameters, factory)));
            }
          }
          position++;
        }
      }
    } catch (Throwable t) {
      ConstructorOrMethod com = getConstructorOrMethod();
      throw new TestNGException(
          "The factory method "
              + com.getDeclaringClass()
              + "."
              + com.getName()
              + "() threw an exception",
          t);
    } finally {
      // Release any resource backing a lazily-loaded data provider (for example a Stream) once the
      // factory has consumed the rows it needs.
      parameterHolder.close();
    }

    return result.toArray(new IParameterInfo[0]);
  }

  @Override
  public ITestNGMethod clone() {
    throw new IllegalStateException("clone is not supported for FactoryMethod");
  }
}
