package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.reflect.ReflectionHelper;
import test.github765.DuplicateCallsSample;

import java.lang.reflect.Method;
import java.util.List;

public class ReflectionHelperTest {

    @Test
    public void testMethodCount() {
        Method[] methods = prune(ReflectionHelper.getLocalMethods(DuplicateCallsSample.class));
        Assert.assertEquals(methods.length, 2);
        methods = prune(ReflectionHelper.getLocalMethods(Dog.class));
        Assert.assertEquals(methods.length, 1);
        methods = prune(ReflectionHelper.getLocalMethods(Dinosaur.class));
        Assert.assertEquals(methods.length, 0);
        methods = prune(ReflectionHelper.getLocalMethods(Dragon.class));
        Assert.assertEquals(methods.length, 1);
    }

    private static Method[] prune(Method[] methods) {
        List<Method> pruned = Lists.newArrayList(methods.length);
        for (Method method : methods) {
            if (! method.getName().contains("jacocoInit")) {
                pruned.add(method);
            }
        }
        return pruned.toArray(new Method[pruned.size()]);
    }

    interface Animal {
        void makeSound();
    }


    class Dog implements Animal {

        @Override
        public void makeSound() {

        }
    }


    abstract class Dinosaur implements Animal {

    }


    abstract class Dragon implements Animal {

        @Override
        public void makeSound() {

        }

        abstract void walk();
    }
}
