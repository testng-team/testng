package test.listeners.github2385;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test
  public void testExtendClass() {
    TestNG testNG = create(SonTestClassSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
  }

  @Test
  public void testClassAndInterface() {
    TestNG testNG = create(TestClassAndInterfaceInheritSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
    assertTrue(TestClassListener.listenerMethodInvoked);
  }

  @Test
  public void testClassListeners() {
    TestNG testNG = create(TestClassListenersInheritSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
  }

  @Test
  public void testInterface() {
    TestNG testNG = create(TestInterfaceListenersInheritSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
  }

  @Test
  public void testMultiInherit() {
    TestNG testNG = create(TestMultiInheritSample.class);
    testNG.run();
    assertTrue(TestListener.listenerMethodInvoked);
    assertTrue(TestClassListener.listenerMethodInvoked);
  }

  @Test
  public void testMultiInheritSameAnnotation() {
    TestNG testNG = create(TestMultiInheritSameAnnotationSample.class);
    testNG.run();
    assertTrue(TestListener.listenerMethodInvoked);
  }

  @Test
  public void testMultiLevel() {
    TestNG testNG = create(TestMultiLevelInheritSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
    assertTrue(TestClassListener.listenerMethodInvoked);
  }

  @Test
  public void testMultiLevelSameAnnotation() {
    TestNG testNG = create(TestMultiLevelInheritSameAnnotationSample.class);
    testNG.run();
    assertTrue(TestListener.listenerExecuted);
    assertTrue(TestListener.listenerMethodInvoked);
  }

  @Test
  public void testPackages() {
    List<XmlPackage> packages = new ArrayList<>();
    XmlPackage xmlPackage = new XmlPackage("test.listeners.github2385.packages");
    packages.add(xmlPackage);
    XmlTest test = new XmlTest();
    test.setName("MyTest");
    test.setXmlPackages(packages);

    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("MySuite");
    xmlSuite.setTests(Collections.singletonList(test));

    test.setXmlSuite(xmlSuite);

    TestNG tng = new TestNG();
    tng.setXmlSuites(Collections.singletonList(xmlSuite));
    tng.run();

    assertFalse(TestListener.listenerMethodInvoked);
  }

  @AfterMethod
  public void reset() {
    TestListener.listenerExecuted = false;
    TestListener.listenerMethodInvoked = false;
    TestClassListener.listenerMethodInvoked = false;
  }
}
