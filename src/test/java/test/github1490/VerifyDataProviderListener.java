package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.Assert;
import org.testng.DataProviderInformation;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.util.Arrays;

public class VerifyDataProviderListener extends SimpleBaseTest {

    @Test
    public void testInstanceBasedDataProviderInformation() {
        TestNG tng = create(InstanceBasedDataProviderWithListenerAnnotationSample.class);
        tng.run();
        DataProviderInformation before = DataProviderInfoProvider.before;
        DataProviderInformation after = DataProviderInfoProvider.after;
        Assert.assertEquals(before, after);
        Assert.assertEquals(before.getInstance(), after.getInstance());
        Assert.assertEquals(before.getMethod().getName(), "getData");
    }

    @Test
    public void testStaticDataProviderInformation() {
        TestNG tng = create(StaticDataProviderWithListenerAnnotationSample.class);
        tng.run();
        DataProviderInformation before = DataProviderInfoProvider.before;
        DataProviderInformation after = DataProviderInfoProvider.after;
        Assert.assertEquals(before, after);
        Assert.assertNull(before.getInstance());
        Assert.assertEquals(before.getMethod().getName(), "getStaticData");
    }

    @Test
    public void testSimpleDataProviderWithListenerAnnotation() {
        final String prefix = ":" + SimpleDataProviderWithListenerAnnotationSample.class.getName() + ".testMethod";
        runTest(prefix, SimpleDataProviderWithListenerAnnotationSample.class, true);
    }

    @Test
    public void testFactoryPoweredDataProviderWithListenerAnnotation() {
        final String prefix = ":" + FactoryPoweredDataProviderWithListenerAnnotationSample.class.getName();
        runTest(prefix, FactoryPoweredDataProviderWithListenerAnnotationSample.class, true);
    }

    @Test
    public void testSimpleDataProviderWithoutListenerAnnotation() {
        final String prefix = ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
        runTest(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class, false);
    }

    @Test
    public void testFactoryPoweredDataProviderWithoutListenerAnnotation() {
        final String prefix = ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
        runTest(prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class, false);
    }

    @Test
    public void testSimpleDataProviderWithListenerViaSuiteXml() {
        final String prefix = ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
        runTestWithListenerViaSuiteXml(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class);
    }

    @Test
    public void testFactoryPoweredDataProviderWithListenerViaSuiteXml() {
        final String prefix = ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
        runTestWithListenerViaSuiteXml(prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class);
    }

    @Test
    public void testSimpleDataProviderWithListenerAnnotationAndInvolvingInheritance() {
        final String prefix = ":" + SimpleDataProviderWithListenerAnnotationSample1.class.getName() + ".testMethod";
        TestNG tng = create(SimpleDataProviderWithListenerAnnotationSample1.class);
        tng.run();
        assertThat(LocalDataProviderListener.messages)
                .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
    }

    @AfterMethod
    public void resetListenerMessages() {
        LocalDataProviderListener.messages.clear();
    }

    private static void runTestWithListenerViaSuiteXml(String prefix, Class<?> clazz) {
        XmlSuite xmlSuite = createXmlSuite("SampleSuite");
        XmlTest xmlTest = createXmlTest(xmlSuite, "SampleTest");
        createXmlClass(xmlTest, clazz);
        xmlSuite.addListener(LocalDataProviderListener.class.getName());
        TestNG tng = create(xmlSuite);
        tng.run();
        assertThat(LocalDataProviderListener.messages)
                .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
    }

    private static void runTest(String prefix, Class<?> clazz, boolean hasListenerAnnotation) {
        TestNG tng = create(clazz);
        if (!hasListenerAnnotation) {
            tng.addListener(new LocalDataProviderListener());
        }
        tng.run();
        assertThat(LocalDataProviderListener.messages)
                .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
    }

}
