package org.testng.internal.objects;

import org.testng.TestNGException;
import org.testng.internal.InstanceCreator;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.internal.objects.pojo.DetailedAttributes;
import org.testng.internal.objects.pojo.BasicAttributes;

/**
 * A plain vanilla Object dispenser
 */
class SimpleObjectDispenser implements IObjectDispenser {

  @Override
  public void setNextDispenser(IObjectDispenser dispenser) {
    //We are not going to be doing anything with this downstream dispenser since we are
    //last in the chain of responsibility.
    throw new UnsupportedOperationException("Cannot allow adding any further downstream object dispensers.");
  }

  @Override
  public Object dispense(CreationAttributes attributes) {
    DetailedAttributes detailed = attributes.getDetailedAttributes();
    if (detailed != null) {
      return InstanceCreator.createInstance(detailed.getDeclaringClass(),
          detailed.getClasses(), detailed.getXmlTest(), detailed.getFinder(), detailed.getFactory(),
          detailed.isCreate(), detailed.getErrorMsgPrefix());
    }
    BasicAttributes basic = attributes.getBasicAttributes();
    if (basic == null) {
      throw new TestNGException("Encountered problems in creating instances.");
    }
    if (basic.getRawClass() == null) {
      try {
        return InstanceCreator.newInstance(basic.getTestClass().getRealClass());
      } catch (TestNGException e) {
        return null;
      }
    }
    return InstanceCreator.newInstance(basic.getRawClass());
  }
}
