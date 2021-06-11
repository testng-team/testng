package org.testng.internal.objects;

import com.google.inject.Injector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.invokers.objects.GuiceContext;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;

/** A Guice backed Object dispenser that is aware of Dependency Injection */
class GuiceBasedObjectDispenser implements IObjectDispenser {

  private IObjectDispenser dispenser;
  private static final Map<Object, GuiceHelper> helpers = new ConcurrentHashMap<>();

  @Override
  public void setNextDispenser(IObjectDispenser dispenser) {
    this.dispenser = dispenser;
  }

  @Override
  public Object dispense(CreationAttributes attributes) {
    if (attributes.getBasicAttributes() != null) {
      BasicAttributes sa = attributes.getBasicAttributes();
      Class<?> cls =
          sa.getTestClass() == null ? sa.getRawClass() : sa.getTestClass().getRealClass();
      if (cannotDispense(cls)) {
        return this.dispenser.dispense(attributes);
      }
      ITestContext ctx = attributes.getContext();
      GuiceContext suiteCtx = attributes.getSuiteContext();
      GuiceHelper helper;
      // TODO: remove unused entries from helpers
      if (ctx == null) {
        helper = helpers.computeIfAbsent(suiteCtx, k -> new GuiceHelper(suiteCtx));
      } else {
        helper = helpers.computeIfAbsent(ctx, k -> new GuiceHelper(ctx));
      }
      Injector injector;
      if (ctx == null) {
        injector = helper.getInjector(cls, suiteCtx.getInjectorFactory());
      } else {
        injector = helper.getInjector(cls, ctx.getInjectorFactory());
      }
      if (injector == null) {
        return null;
      }
      if (sa.getRawClass() == null) {
        return injector.getInstance(sa.getTestClass().getRealClass());
      } else {
        return injector.getInstance(sa.getRawClass());
      }
    }
    // We dont have the ability to process object creation with elaborate attributes
    return this.dispenser.dispense(attributes);
  }

  private static boolean cannotDispense(Class<?> clazz) {
    return AnnotationHelper.findAnnotationSuperClasses(Guice.class, clazz) == null;
  }
}
