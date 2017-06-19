package test.issue1339;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.ClassHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ClassHelperTest {

    @Test
    public void testGetAvailableMethods() {
        runTest(getExpected(), LittlePanda.class);
    }

    @Test
    public void testGetAvailableMethodsWhenOverrdingIsInvolved() {
        List<String> expected = getExpected("equals","hashCode","toString");
        runTest(expected, BabyPanda.class);
    }

    private static void runTest(List<String> expected, Class<?> whichClass) {
        Set<Method> methods = ClassHelper.getAvailableMethods(whichClass);
        //Intentionally not using assertEquals because when this test is executed via gradle an additional method
        //called "jacocoInit()" is getting added, which does not get added when this test is executed individually
        int size = expected.size();
        Assert.assertTrue(methods.size() >= size, "Number of methods found should have been atleast " + size);
        for (Method method : methods) {
            if ("$jacocoInit".equalsIgnoreCase(method.getName())) {
                continue;
            }
            Assert.assertTrue(expected.contains(method.getName()));
        }
    }

    private static List<String> getExpected(String... additionalMethods) {
        String[] defaultMethods = new String[]{"announcer", "announcer", "inheritable", "inheritable"};
        if (additionalMethods == null) {
            return Arrays.asList(defaultMethods);
        }
        List<String> expected = new ArrayList<>(Arrays.asList(defaultMethods));
        expected.addAll(Arrays.asList(additionalMethods));
        return expected;
    }
}
