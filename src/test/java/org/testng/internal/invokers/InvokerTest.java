package org.testng.internal.invokers;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.ArrayList;
import java.util.Map;

public class InvokerTest extends SimpleBaseTest {
    private static Map<String, Integer> valueMap = Maps.newHashMap();
    private static final String SMOKE = "smoketests";
    private static final String FUNCTIONAL_TESTS = "functionaltests";

    private static Map<String, Integer> getValueMap() {
        return valueMap;
    }

    @Test
    public void testClassWithRedundantGroups() {
        valueMap.clear();
        privateRun(ClassWithRedundantGroupNames.class, SMOKE, FUNCTIONAL_TESTS);
        Assert.assertEquals(new Integer(1), valueMap.get(ClassWithRedundantGroupNames.class.getSimpleName()));
    }

    @Test
    public void testClassWithUniqueGroups() {
        valueMap.clear();
        privateRun(ClassWithUniqueGroupNames.class, SMOKE);
        Assert.assertEquals(new Integer(1), valueMap.get(ClassWithUniqueGroupNames.class.getSimpleName()));
    }

    private void privateRun(final Class className, String... groupNames) {
        final XmlSuite suite = new XmlSuite();
        suite.setName("simple-suite");
        final XmlTest xmlTest = new XmlTest(suite);
        xmlTest.setName("simple-test");
        xmlTest.setClasses(new ArrayList<XmlClass>() {{
            add(new XmlClass(className));
        }});
        for (String group : groupNames) {
            xmlTest.addIncludedGroup(group);

        }
        suite.setTests(new ArrayList<XmlTest>() {{
            add(xmlTest);
        }});
        TestNG tng = create();
        tng.setXmlSuites(new ArrayList<XmlSuite>() {{
            add(suite);
        }});
        tng.run();
    }

    static class LocalBase {
        protected void before(String name) {
            Map<String, Integer> map = InvokerTest.getValueMap();
            if (map == null) {
                return;
            }
            Integer value = map.get(name);
            if (value == null) {
                value = 1;
            } else {
                value++;
            }
            map.put(name, value);
        }
    }


    public static class ClassWithUniqueGroupNames extends LocalBase {
        @BeforeGroups (groups = {InvokerTest.SMOKE})
        public void before() {
            String name = getClass().getSimpleName();
            super.before(name);
        }

        @Test (groups = {InvokerTest.SMOKE})
        public void test() {
        }
    }


    public static class ClassWithRedundantGroupNames extends LocalBase {

        @BeforeGroups (groups = {InvokerTest.SMOKE, InvokerTest.FUNCTIONAL_TESTS})
        public void before() {
            String name = getClass().getSimpleName();
            super.before(name);
        }

        @Test (groups = {InvokerTest.SMOKE, InvokerTest.FUNCTIONAL_TESTS})
        public void test() {
        }

    }
}
