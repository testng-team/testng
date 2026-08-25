package org.testng.internal.invokers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.testng.ClassMethodMap;
import org.testng.IClassListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.thread.IWorker;
import org.testng.xml.XmlSuite;

public abstract class AbstractParallelWorker {

  public static AbstractParallelWorker newWorker(
      XmlSuite.ParallelMode mode, boolean groupByInstances) {
    if (XmlSuite.ParallelMode.INSTANCES.equals(mode) && groupByInstances) {
      return new InstanceBasedParallelParallelWorker();
    }
    return new ClassBasedParallelWorker();
  }

  public abstract List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments);

  public static class Arguments {
    private final List<ITestNGMethod> methods;
    private final IInvoker invoker;
    private final ConfigurationGroupMethods configMethods;
    private final ClassMethodMap classMethodMap;
    private final List<IClassListener> listeners;
    private final ITestContext testContext;
    private final IAnnotationFinder finder;

    private Arguments(
        List<ITestNGMethod> methods,
        IInvoker invoker,
        ConfigurationGroupMethods configMethods,
        ClassMethodMap classMethodMap,
        List<IClassListener> listeners,
        ITestContext testContext,
        IAnnotationFinder finder) {
      this.methods = methods;
      this.invoker = invoker;
      this.configMethods = configMethods;
      this.classMethodMap = classMethodMap;
      this.listeners = listeners;
      this.testContext = testContext;
      this.finder = finder;
    }

    public List<ITestNGMethod> getMethods() {
      return Collections.unmodifiableList(methods);
    }

    public IInvoker getInvoker() {
      return invoker;
    }

    public ConfigurationGroupMethods getConfigMethods() {
      return configMethods;
    }

    public ClassMethodMap getClassMethodMap() {
      return classMethodMap;
    }

    List<IClassListener> getListeners() {
      return Collections.unmodifiableList(listeners);
    }

    public ITestContext getTestContext() {
      return testContext;
    }

    public IAnnotationFinder getFinder() {
      return finder;
    }

    public static class Builder {
      private @Nullable List<ITestNGMethod> methods;
      private @Nullable IInvoker invoker;
      private @Nullable ConfigurationGroupMethods configMethods;
      private @Nullable ClassMethodMap classMethodMap;
      private @Nullable List<IClassListener> listeners;
      private @Nullable ITestContext testContext;
      private @Nullable IAnnotationFinder finder;

      public Builder methods(List<ITestNGMethod> methods) {
        this.methods = methods;
        return this;
      }

      public Builder invoker(IInvoker invoker) {
        this.invoker = invoker;
        return this;
      }

      public Builder configMethods(ConfigurationGroupMethods configMethods) {
        this.configMethods = configMethods;
        return this;
      }

      public Builder classMethodMap(ClassMethodMap classMethodMap) {
        this.classMethodMap = classMethodMap;
        return this;
      }

      public Builder listeners(Collection<IClassListener> listeners) {
        this.listeners = new ArrayList<>(listeners);
        return this;
      }

      public Builder testContext(ITestContext testContext) {
        this.testContext = testContext;
        return this;
      }

      public Builder finder(IAnnotationFinder finder) {
        this.finder = finder;
        return this;
      }

      public Arguments build() {
        return new Arguments(
            Objects.requireNonNull(methods),
            Objects.requireNonNull(invoker),
            Objects.requireNonNull(configMethods),
            Objects.requireNonNull(classMethodMap),
            Objects.requireNonNull(listeners),
            Objects.requireNonNull(testContext),
            Objects.requireNonNull(finder));
      }
    }
  }
}
