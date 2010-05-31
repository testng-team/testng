package test.abstractconfmethod;

import org.testng.annotations.BeforeMethod;

public abstract class B extends A
{
   @BeforeMethod(dependsOnMethods = {"testSetup"})
   public void doSomethingInMiddle() {}

}
