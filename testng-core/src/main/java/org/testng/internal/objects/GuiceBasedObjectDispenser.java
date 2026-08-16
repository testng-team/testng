package org.testng.internal.objects;

import com.google.inject.Injector;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.invokers.objects.GuiceContext;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;

/** A Guice backed Object dispenser that is aware of Dependency Injection */
class GuiceBasedObjectDispenser implements IObjectDispenser {

  private IObjectDispenser dispenser;
  private static final ReentrantLock lock = new ReentrantLock();

  GuiceBasedObjectDispenser(IObjectDispenser dispenser) {
    this.dispenser = dispenser;
  }

  @Override
  public void setNextDispenser(IObjectDispenser dispenser) {
    this.dispenser = dispenser;
  }

  @Override
  public @Nullable Object dispense(CreationAttributes attributes) {
    if (attributes.getBasicAttributes() == null) {
      // We don't have the ability to process object creation with elaborate attributes
      return this.dispenser.dispense(attributes);
    }
    try {
      lock.lock();
      return dispenseObject(attributes);
    } finally {
      lock.unlock();
    }
  }

  private @Nullable Object dispenseObject(CreationAttributes attributes) {
    BasicAttributes sa = attributes.getBasicAttributes();
    Class<?> rawClass = sa.getRawClass();
    IClass testClass = sa.getTestClass();
    // BasicAttributes lets both halves be null, but no construction site leaves both out:
    // ClassImpl is the only one that omits the raw class, and it supplies itself as the IClass.
    Class<?> cls = testClass == null ? Objects.requireNonNull(rawClass) : testClass.getRealClass();
    if (cannotDispense(cls)) {
      return this.dispenser.dispense(attributes);
    }
    ITestContext ctx = attributes.getContext();
    Injector injector;
    // TODO: remove unused entries from helpers
    if (ctx == null) {
      // No test context means a suite context instead. Nothing enforces that, and one caller
      // reaches here with neither -- see #3377.
      GuiceContext suite = Objects.requireNonNull(attributes.getSuiteContext());
      injector = new GuiceHelper(suite).getInjector(cls, suite.getInjectorFactory());
    } else {
      GuiceHelper helper = (GuiceHelper) ctx.getAttribute(GUICE_HELPER);
      if (helper == null) {
        helper = new GuiceHelper(ctx);
        ctx.setAttribute(GUICE_HELPER, helper);
      }
      injector = helper.getInjector(cls, ctx.getInjectorFactory());
    }
    if (injector == null) {
      return null;
    }
    if (rawClass != null) {
      return injector.getInstance(rawClass);
    }
    // Without a raw class, cls already resolved to the test class above.
    return injector.getInstance(cls);
  }

  private static boolean cannotDispense(Class<?> clazz) {
    return AnnotationHelper.findAnnotationSuperClasses(Guice.class, clazz) == null;
  }
}
