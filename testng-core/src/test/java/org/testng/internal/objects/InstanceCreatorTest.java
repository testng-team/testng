package org.testng.internal.objects;

import java.util.Map;
import org.testng.Assert;
import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestObjectFactory;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.issue1456.TestClassSample;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class InstanceCreatorTest {
  private static final String GITHUB_1456 = "GITHUB-1456";

  @Test(description = GITHUB_1456)
  public void testCreateInstance1WithOneArgStringParamForConstructor() {
    Class<TestClassSample> declaringClass = TestClassSample.class;
    Map<Class<?>, IClass> classes = Maps.newHashMap();
    XmlTest xmlTest = new XmlTest(new XmlSuite());
    xmlTest.setName(GITHUB_1456);
    IAnnotationFinder finder = new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
    ITestObjectFactory objectFactory = new ObjectFactoryImpl();
    Object object =
        SimpleObjectDispenser.createInstance(
            declaringClass, classes, xmlTest, finder, objectFactory, false, "");
    Assert.assertTrue(object instanceof ITest);
    Assert.assertEquals(((ITest) object).getTestName(), GITHUB_1456);
  }
}
