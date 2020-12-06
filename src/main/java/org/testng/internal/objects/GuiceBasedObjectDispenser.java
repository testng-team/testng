package org.testng.internal.objects;

import com.google.inject.Injector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.ITestContext;
import org.testng.annotations.Guice;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.internal.objects.pojo.BasicAttributes;

/**
 * A Guice backed Object dispenser that is aware of Dependency Injection
 */
class GuiceBasedObjectDispenser implements IObjectDispenser {

  private IObjectDispenser dispenser;
  private static final Map<Integer, GuiceHelper> helpers = new ConcurrentHashMap<>();

  @Override
  public void setNextDispenser(IObjectDispenser dispenser) {
    this.dispenser = dispenser;
  }

  @Override
  public Object dispense(CreationAttributes attributes) {
    if (attributes.getBasicAttributes() != null) {
      BasicAttributes sa = attributes.getBasicAttributes();
      if (cannotDispense(sa.getTestClass().getRealClass())) {
        return this.dispenser.dispense(attributes);
      }
      ITestContext ctx = attributes.getContext();
      GuiceHelper helper = helpers.computeIfAbsent(ctx.hashCode(), k -> new GuiceHelper(ctx));
      Injector injector = helper.getInjector(sa.getTestClass(), ctx.getInjectorFactory());
      if (injector == null) {
        return null;
      }
      if (sa.getRawClass() == null) {
        return injector.getInstance(sa.getTestClass().getRealClass());
      } else {
        return injector.getInstance(sa.getRawClass());
      }
    }
    //We dont have the ability to process object creation with elaborate attributes
    return this.dispenser.dispense(attributes);
  }

  private static boolean cannotDispense(Class<?> clazz) {
    return AnnotationHelper.findAnnotationSuperClasses(Guice.class, clazz) == null;
  }

}
