package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.DataProviderHolder;
import org.testng.ITestContext;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.listeners.ListenerFactoryContainer;
import org.testng.internal.listeners.TestClassContainer;
import org.testng.internal.listeners.TestClassDoublingupAsListenerFactory;
import org.testng.internal.listeners.TestClassWithCompositeListener;
import org.testng.internal.listeners.TestClassWithListener;
import org.testng.internal.paramhandler.FakeTestContext;
import org.testng.xml.XmlClass;
import test.SimpleBaseTest;

public class TestListenerHelperTest {

  private IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());

  @Test(dataProvider = "getTestData")
  public void testfindAllListeners(Class<?> clazz, int expectedSize, boolean testFactoryClass) {
    TestListenerHelper.ListenerHolder holder = TestListenerHelper.findAllListeners(clazz, finder);
    assertThat(holder.getListenerClasses()).hasSize(expectedSize);
    if (testFactoryClass) {
      assertThat(holder.getListenerFactoryClass()).isNotNull();
    }
  }

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new Object[][] {
      {TestClassWithListener.class, 1, false},
      {SimpleBaseTest.class, 0, false},
      {TestClassWithCompositeListener.class, 1, true}
    };
  }

  @Test(description = "GITHUB-3095")
  public void testFindAllListenersDuplicateListenerFactories() {
    TestListenerHelper.ListenerHolder result =
        TestListenerHelper.findAllListeners(
            TestClassContainer.TestClassWithDuplicateListenerFactories.class, finder);
    assertThat(result.getListenerFactoryClass()).isNotNull();
  }

  @Test(
      description = "GITHUB-3095",
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp =
          "\nFound more than one class implementing ITestNGListenerFactory:class "
              + "org.testng.internal.listeners.ListenerFactoryContainer\\$DummyListenerFactory2 "
              + "and class org.testng.internal.listeners"
              + ".ListenerFactoryContainer\\$DummyListenerFactory")
  public void testFindAllListenersErrorCondition() {
    TestListenerHelper.findAllListeners(
        TestClassContainer.TestClassWithMultipleUniqueListenerFactories.class, finder);
  }

  @Test(dataProvider = "getFactoryTestData")
  public void testCreateListenerFactory(
      Class<?> testClazz, Class<? extends ITestNGListenerFactory> listenerClazz) {
    ITestContext ctx = new FakeTestContext(testClazz);
    ClassInfoMap classMap = new ClassInfoMap(Collections.singletonList(new XmlClass(testClazz)));
    ITestObjectFactory objectFactory = new ITestObjectFactory() {};
    Configuration configuration = new Configuration();
    configuration.setObjectFactory(objectFactory);
    TestNGClassFinder finder =
        new TestNGClassFinder(
            classMap, Maps.newHashMap(), configuration, ctx, new DataProviderHolder(configuration));
    ITestNGListenerFactory factory =
        TestListenerHelper.createListenerFactory(objectFactory, finder, listenerClazz, ctx);
    assertThat(factory).isNotNull();
  }

  @DataProvider(name = "getFactoryTestData")
  public Object[][] getFactoryTestData() {
    return new Object[][] {
      {TestClassWithListener.class, ListenerFactoryContainer.DummyListenerFactory.class},
      {TestClassDoublingupAsListenerFactory.class, TestClassDoublingupAsListenerFactory.class}
    };
  }
}
