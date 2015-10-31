package org.testng.junit;

import java.util.List;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 *
 * @author lukas
 */
//NO JUnit specific code here to avoid runtime errors
public abstract class JUnitTestClass implements ITestClass {

    private static final long serialVersionUID = 405598615794850925L;
    private List<ITestNGMethod> m_testMethods = Lists.newArrayList();
    private List<ITestNGMethod> m_beforeClass = Lists.newArrayList();
    private List<ITestNGMethod> m_afterClass = Lists.newArrayList();
    private List<ITestNGMethod> m_beforeTest = Lists.newArrayList();
    private List<ITestNGMethod> m_afterTest = Lists.newArrayList();
    private Class m_realClass;
    private Object[] m_instances;
    private long[] m_instanceHashes;

    public JUnitTestClass(Class test) {
        m_realClass = test;
        m_instances = new Object[]{test};
        m_instanceHashes = new long[]{test.hashCode()};
    }

    List<ITestNGMethod> getTestMethodList() {
      return m_testMethods;
    }
    
    /**
     * @see org.testng.IClass#addInstance(java.lang.Object)
     */
    @Override
    public void addInstance(Object instance) {
        throw new IllegalStateException("addInstance is not supported for JUnit");
    }

    /**
     * @see org.testng.IClass#getName()
     */
    @Override
    public String getName() {
        return m_realClass.getName();
    }

    /**
     * @see org.testng.IClass#getRealClass()
     */
    @Override
    public Class getRealClass() {
        return m_realClass;
    }

    @Override
    public String getTestName() {
        return null;
    }

    @Override
    public XmlTest getXmlTest() {
        return null;
    }

    @Override
    public XmlClass getXmlClass() {
        return null;
    }

    /**
     * @see org.testng.ITestClass#getInstanceCount()
     */
    @Override
    public int getInstanceCount() {
        return 1;
    }

    /**
     * @see org.testng.ITestClass#getInstanceHashCodes()
     */
    @Override
    public long[] getInstanceHashCodes() {
        return m_instanceHashes;
    }

    /**
     * @see org.testng.ITestClass#getInstances(boolean)
     */
    @Override
    public Object[] getInstances(boolean reuse) {
        return m_instances;
    }

    /**
     * @see org.testng.ITestClass#getTestMethods()
     */
    @Override
    public ITestNGMethod[] getTestMethods() {
        return m_testMethods.toArray(new ITestNGMethod[m_testMethods.size()]);
    }

    /**
     * @see org.testng.ITestClass#getBeforeTestMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeTestMethods() {
        return m_beforeTest.toArray(new ITestNGMethod[m_beforeTest.size()]);
    }

    /**
     * @see org.testng.ITestClass#getAfterTestMethods()
     */
    @Override
    public ITestNGMethod[] getAfterTestMethods() {
        return m_afterTest.toArray(new ITestNGMethod[m_afterTest.size()]);
    }

    /**
     * @see org.testng.ITestClass#getBeforeClassMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeClassMethods() {
        return m_beforeClass.toArray(new ITestNGMethod[m_beforeClass.size()]);
    }

    /**
     * @see org.testng.ITestClass#getAfterClassMethods()
     */
    @Override
    public ITestNGMethod[] getAfterClassMethods() {
        return m_afterClass.toArray(new ITestNGMethod[m_afterClass.size()]);
    }

    //features not supported by JUnit
    private static final ITestNGMethod[] EMPTY_METHODARRAY = new ITestNGMethod[0];

    /**
     * @see org.testng.ITestClass#getBeforeSuiteMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeSuiteMethods() {
        return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterSuiteMethods()
     */
    @Override
    public ITestNGMethod[] getAfterSuiteMethods() {
        return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeGroupsMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeGroupsMethods() {
        return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterGroupsMethods()
     */
    @Override
    public ITestNGMethod[] getAfterGroupsMethods() {
        return EMPTY_METHODARRAY;
    }

    //already deprecated stuff, not usable in junit
    /**
     * @see org.testng.ITestClass#getBeforeTestConfigurationMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
        return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterTestConfigurationMethods()
     */
    @Override
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
        return EMPTY_METHODARRAY;
    }
}
