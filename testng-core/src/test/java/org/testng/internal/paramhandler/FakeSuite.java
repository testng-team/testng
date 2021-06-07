package org.testng.internal.paramhandler;

import com.google.inject.Injector;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.SuiteRunState;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class FakeSuite implements ISuite {

  private XmlTest xmlTest;

  public FakeSuite(XmlTest xmlTest) {
    this.xmlTest = xmlTest;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Map<String, ISuiteResult> getResults() {
    return Maps.newHashMap();
  }

  @Override
  public ITestObjectFactory getObjectFactory() {
    return new ITestObjectFactory() {};
  }

  @Override
  public String getOutputDirectory() {
    return "";
  }

  @Override
  public String getParallel() {
    return "";
  }

  @Override
  public String getParentModule() {
    return "";
  }

  @Override
  public String getGuiceStage() {
    return "";
  }

  @Override
  public String getParameter(String parameterName) {
    return "";
  }

  @Override
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
    return Maps.newHashMap();
  }

  @Override
  public List<IInvokedMethod> getAllInvokedMethods() {
    return Collections.emptyList();
  }

  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    return Collections.emptyList();
  }

  @Override
  public void run() {}

  @Override
  public String getHost() {
    return "";
  }

  @Override
  public SuiteRunState getSuiteState() {
    return new SuiteRunState();
  }

  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
  }

  @Override
  public XmlSuite getXmlSuite() {
    return this.xmlTest.getSuite();
  }

  @Override
  public void addListener(ITestNGListener listener) {}

  @Override
  public Injector getParentInjector() {
    return null;
  }

  @Override
  public void setParentInjector(Injector injector) {}

  @Override
  public List<ITestNGMethod> getAllMethods() {
    return Collections.emptyList();
  }

  @Override
  public Object getAttribute(String name) {
    return new Object();
  }

  @Override
  public void setAttribute(String name, Object value) {}

  @Override
  public Set<String> getAttributeNames() {
    return Sets.newHashSet();
  }

  @Override
  public Object removeAttribute(String name) {
    return new Object();
  }
}
