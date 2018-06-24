package org.testng.internal;

import org.testng.IDataProviderListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.paramhandler.DataDrivenSampleTestClass;
import org.testng.internal.paramhandler.ExceptionThrowingDataDrivenSampleTestClass;
import org.testng.internal.paramhandler.FakeTestContext;
import org.testng.internal.paramhandler.FakeTestNGMethod;
import org.testng.internal.paramhandler.ParameterizedSampleTestClass;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterHandlerTest extends SimpleBaseTest {
  private ParameterHandler handler;

  @BeforeClass
  public void beforeClass() {
    Collection<IDataProviderListener> listeners = Collections.emptyList();
    IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
    handler = new ParameterHandler(finder, listeners);
  }

  @Test
  public void testCreateParameters() {
    ITestNGMethod testNGMethod =
        new FakeTestNGMethod(ParameterizedSampleTestClass.class, "testMethod");
    runTest(testNGMethod, ParameterHolder.ParameterOrigin.ORIGIN_XML);
  }

  @Test
  public void testCreateParametersUsingDataProvider() {
    XmlTest xmlTest = createXmlTest("suite", "test");
    ITestNGMethod testNGMethod =
        new FakeTestNGMethod(DataDrivenSampleTestClass.class, "testMethod", xmlTest);
    runTest(testNGMethod, ParameterHolder.ParameterOrigin.ORIGIN_DATA_PROVIDER);
  }

  @Test
  public void testCreateParametersUsingDataProviderNegativeCase() {
    XmlTest xmlTest = createXmlTest("suite", "test");
    Class<?> clazz = ExceptionThrowingDataDrivenSampleTestClass.class;
    ITestNGMethod testNGMethod = new FakeTestNGMethod(clazz, "testMethod", xmlTest);
    ParameterHandler.ParameterBag params = invokeParameterCreation(testNGMethod);
    assertThat(params.parameterHolder).isNull();
    assertThat(params.errorResult).isNotNull();
    assertThat(params.errorResult.getThrowable())
        .hasCauseInstanceOf(UnsupportedOperationException.class);
  }

  private void runTest(ITestNGMethod testNGMethod, ParameterHolder.ParameterOrigin origin) {
    ParameterHandler.ParameterBag params = invokeParameterCreation(testNGMethod);
    assertThat(params.parameterHolder).isNotNull();
    assertThat(params.parameterHolder.origin).isEqualByComparingTo(origin);
    Iterator<Object[]> iterators = params.parameterHolder.parameters;
    assertThat(iterators).containsAll(Collections.singletonList(new Object[] {"bar"}));
  }

  private ParameterHandler.ParameterBag invokeParameterCreation(ITestNGMethod method) {
    ITestContext context = new FakeTestContext(method.getRealClass());
    Map<String, String> map = Maps.newHashMap();
    return handler.createParameters(method, map, map, context);
  }
}
