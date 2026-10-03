package org.testng.listeners.github2385;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.listeners.samples.github2385.SonTestClassSample;
import org.testng.listeners.samples.github2385.TestClassAndInterfaceInheritSample;
import org.testng.listeners.samples.github2385.TestClassListener;
import org.testng.listeners.samples.github2385.TestClassListenersInheritSample;
import org.testng.listeners.samples.github2385.TestInterfaceListenersInheritSample;
import org.testng.listeners.samples.github2385.TestListener;
import org.testng.listeners.samples.github2385.TestMultiInheritSameAnnotationSample;
import org.testng.listeners.samples.github2385.TestMultiInheritSample;
import org.testng.listeners.samples.github2385.TestMultiLevelInheritSameAnnotationSample;
import org.testng.listeners.samples.github2385.TestMultiLevelInheritSample;
import org.testng.listeners.samples.github2385.packages.TestPackageListener;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2385")
  public void testExtendClass() {
    TestNG testNG = create(SonTestClassSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testClassAndInterface() {
    TestNG testNG = create(TestClassAndInterfaceInheritSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
    assertThat(TestClassListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testClassListeners() {
    TestNG testNG = create(TestClassListenersInheritSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testInterface() {
    TestNG testNG = create(TestInterfaceListenersInheritSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testMultiInherit() {
    TestNG testNG = create(TestMultiInheritSample.class);
    testNG.run();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
    assertThat(TestClassListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testMultiInheritSameAnnotation() {
    TestNG testNG = create(TestMultiInheritSameAnnotationSample.class);
    testNG.run();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testMultiLevel() {
    TestNG testNG = create(TestMultiLevelInheritSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
    assertThat(TestClassListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testMultiLevelSameAnnotation() {
    TestNG testNG = create(TestMultiLevelInheritSameAnnotationSample.class);
    testNG.run();
    assertThat(TestListener.listenerExecuted).isTrue();
    assertThat(TestListener.listenerMethodInvoked).isTrue();
  }

  @Test(description = "GITHUB-2385")
  public void testPackages() {
    List<XmlPackage> packages = new ArrayList<>();
    XmlPackage xmlPackage = new XmlPackage(TestPackageListener.class.getPackageName());
    packages.add(xmlPackage);
    XmlTest test = new XmlTest();
    test.setName("MyTest");
    test.setXmlPackages(packages);

    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("MySuite");
    xmlSuite.setTests(Collections.singletonList(test));

    test.setXmlSuite(xmlSuite);

    TestPackageListener.listenerMethodInvoked = false;
    TestNG tng = new TestNG();
    tng.setXmlSuites(Collections.singletonList(xmlSuite));
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);
    tng.run();

    // The scan has to find the sample, or the check below passes for the wrong reason: no test
    // runs, and no listener is called either.
    assertThat(adapter.getPassedTests())
        .extracting(result -> result.getMethod().getMethodName())
        .containsExactly("testWithoutImpl");
    // A wider scan would bring in samples that fail or skip, and passed tests alone cannot see
    // them.
    assertThat(adapter.getFailedTests()).isEmpty();
    assertThat(adapter.getSkippedTests()).isEmpty();
    // The listener sits on an interface that no test class implements, so TestNG must not call it.
    assertThat(TestPackageListener.listenerMethodInvoked).isFalse();
  }

  @AfterMethod
  public void reset() {
    TestListener.listenerExecuted = false;
    TestListener.listenerMethodInvoked = false;
    TestClassListener.listenerMethodInvoked = false;
  }
}
