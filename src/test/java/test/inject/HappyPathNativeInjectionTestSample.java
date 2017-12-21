package test.inject;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;

public class HappyPathNativeInjectionTestSample {

    @Test
    public void m1(Method object) {
    }

    @Test
    public void m2(ITestContext object) {
    }

    @Test
    public void m3(XmlTest object) {
    }

    @Test
    public void m4(ITestContext ctx, XmlTest xmlTest) {
    }

    @Test
    public void m5(XmlTest xmlTest, ITestContext ctx) {
    }

    @Test
    public void m6(Method method, ITestContext ctx) {
    }

    @Test
    public void m7(ITestContext ctx, Method method) {
    }

    @Test
    public void m8(Method method, XmlTest xmlTest) {
    }

    @Test
    public void m9(XmlTest xmlTest, Method method) {
    }

    @Test
    public void m10(Method method, XmlTest xmlTest, ITestContext ctx) {
    }

    @Test
    public void m11(Method method, ITestContext ctx, XmlTest xmlTest) {
    }

    @Test
    public void m12(XmlTest xmlTest, Method method, ITestContext ctx) {
    }

    @Test
    public void m13(XmlTest xmlTest, ITestContext ctx, Method method) {
    }

    @Test
    public void m14(ITestContext ctx, Method method, XmlTest xmlTest) {
    }

    @Test
    public void m15(ITestContext ctx, XmlTest xmlTest, Method method) {
    }

}
