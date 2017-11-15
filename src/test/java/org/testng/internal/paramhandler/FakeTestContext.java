package org.testng.internal.paramhandler;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.testng.IClass;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.collections.Maps;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeTestContext implements ITestContext {
    private XmlTest xmlTest;
    private ISuite suite;

    public FakeTestContext(Class<?> clazz) {
        this(new Class<?>[] {clazz});
    }

    public FakeTestContext(Class<?>... classes) {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName("xml_suite");
        xmlTest = new XmlTest(xmlSuite);
        for (Class<?> clazz : classes) {
            xmlTest.getXmlClasses().add(new XmlClass(clazz));
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("foo", "bar");
        xmlTest.setParameters(map);
        suite = new FakeSuite(xmlTest);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Date getStartDate() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public IResultMap getPassedTests() {
        return null;
    }

    @Override
    public IResultMap getSkippedTests() {
        return null;
    }

    @Override
    public IResultMap getFailedButWithinSuccessPercentageTests() {
        return null;
    }

    @Override
    public IResultMap getFailedTests() {
        return null;
    }

    @Override
    public String[] getIncludedGroups() {
        return new String[0];
    }

    @Override
    public String[] getExcludedGroups() {
        return new String[0];
    }

    @Override
    public String getOutputDirectory() {
        return null;
    }

    @Override
    public ISuite getSuite() {
        return suite;
    }

    @Override
    public ITestNGMethod[] getAllTestMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Collection<ITestNGMethod> getExcludedMethods() {
        return Collections.emptyList();
    }

    @Override
    public IResultMap getPassedConfigurations() {
        return null;
    }

    @Override
    public IResultMap getSkippedConfigurations() {
        return null;
    }

    @Override
    public IResultMap getFailedConfigurations() {
        return null;
    }

    @Override
    public XmlTest getCurrentXmlTest() {
        return xmlTest;
    }

    @Override
    public List<Module> getGuiceModules(Class<? extends Module> cls) {
        return null;
    }

    @Override
    public Injector getInjector(List<Module> moduleInstances) {
        return null;
    }

    @Override
    public Injector getInjector(IClass iClass) {
        return null;
    }

    @Override
    public void addInjector(List<Module> moduleInstances, Injector injector) {

    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {

    }

    @Override
    public Set<String> getAttributeNames() {
        return Collections.emptySet();
    }

    @Override
    public Object removeAttribute(String name) {
        return null;
    }
}
