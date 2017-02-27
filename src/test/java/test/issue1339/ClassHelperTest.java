package test.issue1339;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.ClassHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ClassHelperTest {

    @Test
    public void testGetAvailableMethods() {
        List<String> expected = Arrays.asList(
            "finalize", "toString", "clone", "announcer", "hashCode", "equals",
            "announcer", "notify", "wait", "wait", "wait", "getClass",
            "inheritable", "inheritable", "notifyAll"
        );
        Set<Method> methods = ClassHelper.getAvailableMethods(LittlePanda.class);
        //Intentionally not using assertEquals because when this test is executed via gradle an additional method
        //called "jacocoInit()" is getting added, which does not get added when this test is executed individually
        Assert.assertTrue(methods.size() >= 15, "Number of methods found should have been atleast 15.");
        for (Method method : methods) {
            if ("$jacocoInit".equalsIgnoreCase(method.getName())) {
                continue;
            }
            Assert.assertTrue(expected.contains(method.getName()));
        }
    }
}
