package org.testng.internal;

import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;
import org.testng.internal.objects.Dispenser;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;

/**
 * When no {@link ITestNGListenerFactory} implementations are available, TestNG defaults to this
 * implementation for instantiating listeners.
 */
public final class DefaultListenerFactory implements ITestNGListenerFactory {

  private final ITestObjectFactory m_objectFactory;
  private final ITestContext context;

  public DefaultListenerFactory(ITestObjectFactory objectFactory, ITestContext context) {
    this.m_objectFactory = objectFactory;
    this.context = context;
  }

  @Override
  public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
    BasicAttributes ba = new BasicAttributes(null, listenerClass);
    CreationAttributes attributes = new CreationAttributes(context, ba, null);
    return (ITestNGListener) Dispenser.newInstance(this.m_objectFactory).dispense(attributes);
  }
}
