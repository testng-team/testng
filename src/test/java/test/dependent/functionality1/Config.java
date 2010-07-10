package test.dependent.functionality1;

import org.testng.annotations.BeforeSuite;

public class Config {

   @BeforeSuite (groups = "other")
   public void beforeSuite(){
       System.out.println("BeforeSuite group 'other'");
   }
}
