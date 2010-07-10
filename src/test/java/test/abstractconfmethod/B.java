package test.abstractconfmethod;

import org.testng.annotations.BeforeMethod;

import test.abstractconfmethod.foo.A;

public abstract class B extends A
{
   @BeforeMethod(dependsOnMethods = {"testSetup"})
   public void doSomethingInMiddle() {}

}
