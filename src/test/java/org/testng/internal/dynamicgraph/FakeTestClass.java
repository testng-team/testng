package org.testng.internal.dynamicgraph;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

public class FakeTestClass implements ITestClass {
    private Class<?> clazz;

    public FakeTestClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public ITestNGMethod[] getTestMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeTestMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterTestMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeClassMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterClassMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeSuiteMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterSuiteMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeGroupsMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterGroupsMethods() {
        return new ITestNGMethod[0];
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public XmlTest getXmlTest() {
        return null;
    }

    @Override
    public XmlClass getXmlClass() {
        return new XmlClass(clazz);
    }

    @Override
    public String getTestName() {
        return "";
    }

    @Override
    public Class<?> getRealClass() {
        return clazz;
    }

    @Override
    public Object[] getInstances(boolean create) {
        return new Object[0];
    }

    @Override
    public int getInstanceCount() {
        return 0;
    }

    @Override
    public long[] getInstanceHashCodes() {
        return new long[0];
    }

    @Override
    public void addInstance(Object instance) {

    }
}
