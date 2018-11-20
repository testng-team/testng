package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.reflect.ReflectionHelper;
import test.github1405.TestClassSample;
import test.github765.DuplicateCallsSample;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectionHelperTest {

    @Test
    public void testMethodCount() {
        //Testing exclusion of synthetic methods Refer http://stackoverflow.com/a/5007394 to learn more
        Method[] methods = prune(ReflectionHelper.getLocalMethods(DuplicateCallsSample.class));
        Assert.assertEquals(methods.length, 2);

        //Testing a straight forward use case of retrieving concrete methods
        methods = prune(ReflectionHelper.getLocalMethods(Dog.class));
        Assert.assertEquals(methods.length, 1);

        //When class has no methods count should be zero.
        methods = prune(ReflectionHelper.getLocalMethods(Dinosaur.class));
        Assert.assertEquals(methods.length, 0);

        //Abstract methods should be included.
        methods = prune(ReflectionHelper.getLocalMethods(Dragon.class));
        Assert.assertEquals(methods.length, 2);

        //main methods should be pruned
        methods = prune(ReflectionHelper.getLocalMethods(TestClassSample.class));
        Assert.assertEquals(methods.length, 1);
    }

    /**
     * @param methods - The list of methods extracted from a Class
     * @return - A {@link Method} array which excludes a special method named
     * "jacocoInit" which is getting injected into the test class only when the test
     * is executed via Gradle.
     */
    private static Method[] prune(Method[] methods) {
        List<Method> pruned = Lists.newArrayList(methods.length);
        for (Method method : methods) {
            if (! method.getName().contains("jacocoInit")) {
                pruned.add(method);
            }
        }
        return pruned.toArray(new Method[0]);
    }

    interface Animal {
        void makeSound();
    }

    class Dog implements Animal {

        @Override
        public void makeSound() {

        }
    }

    abstract class Dinosaur implements Animal {}

    abstract class Dragon implements Animal {

        @Override
        public void makeSound() {

        }

        abstract void walk();
    }
}
